package com.personal.main.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.couchbase.ClusterEnvironmentBuilderCustomizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.common.Result;
import com.personal.main.dto.AboutChunkRequest.DeleteChunk;
import com.personal.main.dto.AboutChunkRequest.EditChunk;
import com.personal.main.dto.AboutChunkRequest.deleterepo;
import com.personal.main.dto.CreateChunkRequest;
import com.personal.main.dto.UserChatAiRequest;
import com.personal.main.model.KnowledgeChunk;
import com.personal.main.service.AiChatRoomService;
import com.personal.main.service.AuthService;
import com.personal.main.service.DeepSeekService;
import com.personal.main.service.RagService;
import com.personal.main.model.UserConfig;
import com.personal.main.service.UserDoService;
import lombok.RequiredArgsConstructor;
import com.personal.main.model.ChatRoom;
import com.personal.main.dto.AboutAiChatRoom.getRoomInfo;
import com.personal.main.service.ClusterIndexChunkService;
 

@RestController
@RequiredArgsConstructor
public class AboutRag {
    private final AuthService authService;
    private final RagService ragService;
    private final UserDoService userDoService;
    private final AiChatRoomService aiChatRoomService;
    private final ClusterIndexChunkService clusterIndexChunkService;



      @GetMapping("/api/getchunks")
    public ResponseEntity<Result<List<KnowledgeChunk>>> getChunks(@CookieValue(value = "user_session", defaultValue = "") String token){
                try {
                    Long userid=authService.authCookie(token);
                    List<KnowledgeChunk> chunks=ragService.getChunksById(userid);
                    for(KnowledgeChunk chunk:chunks){
                        chunk.setVectorData(null);
                    }
                    return ResponseEntity.ok(Result.success("成功得到chunks", chunks));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(404,e.getMessage()));
                }
            }
    //创建知识块
    @PostMapping("/api/usercreatechunk")
    public ResponseEntity<Result<String>> postMethodName(@CookieValue(value = "user_session", defaultValue = "") String token,@RequestBody CreateChunkRequest createChunkReq) {
        try {
            Long userId = authService.authCookie(token);
            String[] fileNames = createChunkReq.fileName();
            String[] contents = createChunkReq.content();
            String repoName = createChunkReq.repoName();
            Boolean useLocalModel = createChunkReq.useLocalModel();
            ragService.saveKnowledgeChunk(userId, fileNames, contents, useLocalModel, repoName);
            clusterIndexChunkService.doclusterindex(userId, repoName); // 更新分簇索引
            return ResponseEntity.ok(Result.success("知识块保存成功！当前用户 ID: ",userId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }

    //更新知识块
    @PostMapping("/api/usereditchunk")
    public ResponseEntity<Result<Long>> editchunk(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody EditChunk editChunk) {
        try {
            Long userId = authService.authCookie(token);
            ragService.updateChunk(editChunk);
            Long chunkid=editChunk.id();
            List<KnowledgeChunk> chunk = ragService.getChunks(List.of(chunkid));
            String reponame=chunk.get(0).getRepoName();
            clusterIndexChunkService.doclusterindex(userId, reponame); // 更新分簇索引
            return ResponseEntity.ok(Result.success("知识块更新成功！" , userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }}

//删除知识块
    @PostMapping("/api/userdeletechunk")
    public ResponseEntity<Result<Long>> deletechunk(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody DeleteChunk deleteChunk) {
        try {
            Long userId = authService.authCookie(token);
            Long chunkid=deleteChunk.chunkId();
            List<KnowledgeChunk> chunk = ragService.getChunks(List.of(chunkid));
            String reponame=chunk.get(0).getRepoName();
            clusterIndexChunkService.doclusterindex(userId, reponame); // 更新分簇索引
            return ResponseEntity.ok(Result.success("知识块删除成功！" , deleteChunk.chunkId()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }}
        
        //删除知识库
    @PostMapping("/api/userdeleterepo")
    public ResponseEntity<Result<String>> deleterepo(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody deleterepo deleterepo) {
        try {
            Long userId = authService.authCookie(token);
            ragService.deleteKnowledgeChunkByRepo(userId, deleterepo.repoName());
            return ResponseEntity.ok(Result.success("知识库删除成功！" , deleterepo.repoName()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }}


    @GetMapping("/api/getreponames")
        public ResponseEntity<Result<List<String>>> getChunkNames(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);

            List<KnowledgeChunk> chunks = ragService.getChunksById(userId);
            List<String> repoNames = chunks.stream()
                    .map(KnowledgeChunk::getRepoName)
                    .distinct()          // 如果不希望重复
                    .toList();
            return ResponseEntity.ok(Result.success("成功得到repoName列表", repoNames));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(404, e.getMessage()));
        }
    }



    //获取api来源
    @GetMapping("/api/getapiscourse")
    public ResponseEntity<Result<List<String>>> getUserConfig(
            @CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            List<UserConfig> configs = userDoService.getuserconfig(userId);
            List<String> platformSources = configs.stream()
                    .map(UserConfig::getPlatformSource)
                    .filter(Objects::nonNull)
                    .filter(s -> !s.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Result.success("获取成功", platformSources));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Result.error(400, e.getMessage()));
        }
    }


        /**
     * 获取AI聊天室信息
     */
    @PostMapping("/api/getaichatroominfo")   // 建议改为 POST
    public ResponseEntity<Result<ChatRoom>> getAichatroominfo(
            @CookieValue(value = "user_session", defaultValue = "") String token,
            @RequestBody getRoomInfo getRoomInfo) {

        try {
            Long userId = authService.authCookie(token);
            ChatRoom chatRoom = aiChatRoomService.getaichatroominfo(getRoomInfo.roomId(), userId);
            return ResponseEntity.ok(Result.success("获取成功", chatRoom));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Result.error(400, e.getMessage()));
        }
    }
}
