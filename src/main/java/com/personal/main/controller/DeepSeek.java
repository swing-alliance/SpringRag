package com.personal.main.controller;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.dto.UserAskAiRequest;
import com.personal.main.service.AuthService;
import com.personal.main.service.DeepSeekService;
import com.personal.main.service.RagService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class DeepSeek{

    private final DeepSeekService deepSeekService;
    private final AuthService authService;
    private final RagService ragService;

    @PostMapping(value = "/ai/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> streamChat(@CookieValue(value = "user_session", defaultValue = "") String token,@RequestBody UserAskAiRequest request) {
        try {
            Long userId = authService.authCookie(token); // 验证用户身份
            String apiSource = request.apisource();
            String apiKey = authService.getUserConfigApiKey(userId, apiSource); // 根据用户ID和API来源获取API Key
            String reponame = request.repoName();
            Boolean userag = request.userag();
            String systemMessage = request.systemMessage();
            float referratio = request.referratio();
            String message = request.message();
            Map<Long, Float> refercontextmap = ragService.getIndexMap(message, userId, reponame, referratio);
            List<Long> refercontextids = refercontextmap.keySet().stream().toList();
            String refercontext = ragService.getKnowledgeContentByIds(refercontextids);
            return deepSeekService.streamChat(apiKey, message, refercontext, systemMessage, userag);
        } catch (RuntimeException e) {
            return Flux.just("Error: " + e.getMessage());
        }
    }
}