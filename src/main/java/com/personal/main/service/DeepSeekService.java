package com.personal.main.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Arrays;

@Service
public class DeepSeekService {

    @Value("${openai4j.base-url}")
    private String baseUrl;

    public Flux<String> streamChat(String apiKey, String message, String refercontext, String systemMessage) {
        // 1. 初始化 LangChain4j 的流式模型客户端（完全替代 OpenAiClient）
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName("deepseek-chat")
                .temperature(0.3)
                .build();

        // 2. 组装 RAG 提示词
        String finalUserContent = """
                请结合以下检索到的【已知信息】来回答用户的问题。如果已知信息中没有提到，请委婉拒绝，不要胡乱编造。
                【已知信息】:%s
                【用户问题】:%s
                """.formatted(refercontext, message);

        // 3. 创建响应式管道 Sink
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 4. 投递多角色消息并执行流式输出
        model.generate(
                Arrays.asList(
                        SystemMessage.from(systemMessage),
                        UserMessage.from(finalUserContent)
                ),
                new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        // 收到一个字，就吐给前端一个字
                        sink.tryEmitNext(token);
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        // 流传输结束
                        sink.tryEmitComplete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        // 发生异常
                        System.err.println("DeepSeek 流式调用发生异常: " + error.getMessage());
                        sink.tryEmitError(error);
                    }
                }
        );

        return sink.asFlux();
    }
}