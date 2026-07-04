package com.personal.main.service; // 换成你自己的包名

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import  dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.chat.SystemMessage;
import dev.ai4j.openai4j.chat.UserMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class DeepSeekService {

    @Value("${openai4j.base-url}")
    private String baseUrl;

    public Flux<String> streamChat(String apiKey, String message, String refercontext, String systemMessage) {
        // 1. 创建客户端
        OpenAiClient client = OpenAiClient.builder()
                .baseUrl(baseUrl)
                .openAiApiKey(apiKey)
                .build();

        // 2. 组装 RAG 提示词
        String finalUserContent = """
                请结合以下检索到的【已知信息】来回答用户的问题。如果已知信息中没有提到，请委婉拒绝，不要胡乱编造。
                【已知信息】:%s
                【用户问题】:%s
                """.formatted(refercontext, message);

        // 3. 完美兼容的多角色消息请求体
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("deepseek-chat")
                .temperature(0.3) 
                .messages(java.util.Arrays.asList(
                    SystemMessage.from(systemMessage),
                    UserMessage.from(finalUserContent)
                ))
                .build();

        // 4. 创建 Sink 管道
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 5. 执行流式请求
        try {
            client.chatCompletion(request)
                  .onPartialResponse(partialResponse -> {
                      if (partialResponse.choices() != null && !partialResponse.choices().isEmpty()) {
                          String content = partialResponse.choices().get(0).delta().content();
                          if (content != null) {
                              sink.tryEmitNext(content); 
                          }
                      }
                  })
                  .onComplete(sink::tryEmitComplete)
                  .onError(err -> {
                      System.err.println("DeepSeek 流式调用发生异常: " + err.getMessage());
                      sink.tryEmitError(err);
                  })
                  .execute(); 
        } catch (Exception e) {
            sink.tryEmitError(e);
        }
        // 返回 Flux，并在前端主动断开时，尝试做清理
        return sink.asFlux();
    }
}