package com.personal.main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.dto.UserChatAiRequest;
import com.personal.main.service.AiChatRoomService;
import com.personal.main.service.AuthService;
import com.personal.main.service.DeepSeekService;
import com.personal.main.service.RagService;
import com.personal.main.common.Result;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import com.personal.main.model.ChatMessage;
import org.springframework.http.ResponseEntity;
import com.personal.main.model.ChatRoom;

@RestController
@RequiredArgsConstructor
public class DeepSeek {

    private final DeepSeekService deepSeekService;
    private final AuthService authService;
    private final RagService ragService;
    private final AiChatRoomService aiChatRoomService;
    @PostMapping(value = "/api/airesponse", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> streamChat(
            @CookieValue(value = "user_session", defaultValue = "") String token,
            @RequestBody UserChatAiRequest.UserChatAi request) {
        try {
            Long userId = authService.authCookie(token); // 验证用户身份
            Long roomid = request.roomid();
            ChatRoom room = aiChatRoomService.getaichatroominfo(roomid, userId); // 获取当前房间信息
            String apiSource = room.getPlatformSource(); 
            String apiKey = authService.getUserConfigApiKey(userId, apiSource); // 根据用户ID和API来源获取API Key
            String reponame = aiChatRoomService.getRepoNameByRoomId(request.roomid(), userId); // 获取当前房间绑定的知识库名称
            Boolean userag = request.userag();
           
            String systemMessage = request.systemMessage();
            float referratio = request.referratio();
            String message = request.message();
            String context = aiChatRoomService.getHisChatMassageByRoomId(roomid, userId); // 获取当前房间的历史对话上下文
            
            // 向量检索获取相似度 Map 
            Map<Long, Float> refercontextmap = ragService.getIndexMap(message, userId, reponame, referratio);
            List<Long> refercontextids = refercontextmap.keySet().stream().toList();
            String refercontext = ragService.getKnowledgeContentByIds(refercontextids);
            // 1. 用户提问可以立即同步记录入库
            aiChatRoomService.userrecordmsg(roomid, userId, message); 
            // 【关键点】：声明一个内存 StringBuilder，用来在流式传输中缓存 AI 完整的回复
            StringBuilder aiFullResponse = new StringBuilder();
            // 终端临时调试：打印边界线
            System.out.println("\n====== [AI STREAM START] ======");
            return deepSeekService.streamChat(apiKey, message, context, refercontext, systemMessage, userag)
                    .doOnNext(tokenChunk -> {
                        // 2. 实时将碎片拼接进内存，不对数据库产生高频I/O压力
                        if (tokenChunk != null) {
                            aiFullResponse.append(tokenChunk);
                        }
                        // 实时把每个流式单字/词打印在 Java 终端，不换行，实现打字机回显
                        System.out.print(tokenChunk);
                        System.out.flush();
                    })
                    .doOnComplete(() -> {
                        System.out.println("\n====== [AI STREAM END] ======\n");
                        // 3. 【核心修改】：当流正常结束时，从 StringBuilder 提取完整文本，执行单次入库
                        String finalAiReply = aiFullResponse.toString();
                        if (!finalAiReply.isEmpty()) {
                            // 注意：这里传入的应该是拼接好的 finalAiReply，而不是原始的用户提问 message
                            aiChatRoomService.airecordmsg(roomid, userId, finalAiReply);
                        }
                    })
                    .doOnError(err -> {
                        // 4. 消费期/传输期发生异常时，在终端红字打印
                        System.err.println("\n[AI STREAM RUNTIME ERROR]: " + err.getMessage());
                    })
                    // 5. 健壮性优化：流内部报错时，优雅地给前端返回错误文本，而不是直接炸断连接
                    .onErrorReturn("Error: 向量流传输期间发生异常");

        } catch (RuntimeException e) {
            // 捕获组装期异常（如：鉴权失败、API Key 未配置等）
            System.err.println("\n[AI STREAM ASSEMBLY ERROR]: " + e.getMessage());
            return Flux.just("Error: " + e.getMessage());
        }}



        @PostMapping("/api/roommsg")
        public ResponseEntity<Result<List<ChatMessage>>> getRoomMsg(
                @CookieValue(value = "user_session", defaultValue = "") String token,
                @RequestBody UserChatAiRequest.UserChatAi request) {
            try {
                Long userId = authService.authCookie(token); // 验证用户身份
                Long roomId = request.roomid();
               List<ChatMessage> context = aiChatRoomService.getHisChatMsgByRoomId(roomId, userId); // 获取当前房间的历史对话上下文
                return ResponseEntity.ok(Result.success(context));
            } catch (RuntimeException e) {
                return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
            }}
}