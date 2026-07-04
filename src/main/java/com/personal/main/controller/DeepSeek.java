// package com.personal.main.controller;
// import org.springframework.web.bind.annotation.CookieValue;
// import org.springframework.web.bind.annotation.GetMapping;

// import org.springframework.web.bind.annotation.RestController;

// import com.personal.main.service.AuthService;
// import com.personal.main.service.DeepSeekService;
// import com.personal.main.dto.UserAskAiRequest;
// import org.springframework.web.bind.annotation.RequestBody;
// import lombok.RequiredArgsConstructor;
// import reactor.core.publisher.Flux;

// @RestController
// @RequiredArgsConstructor
// public class DeepSeek{

//     private final DeepSeekService deepSeekService;
//     private final AuthService authService;
//     @GetMapping(value = "/ai/stream", produces = "text/event-stream;charset=UTF-8")
//     public Flux<String> streamChat(@CookieValue(value = "user_session", defaultValue = "") String token,@RequestBody UserAskAiRequest request) {
//         Long userId = authService.authCookie(token); // 验证用户身份
//         String reponame = request.repoName();
//         Boolean userag = request.userag();
//         if(userag == false) {
//             deepSeekService.streamChat(apiKey, message, refercontext, systemMessage);
//         }
//         float referratio = request.referratio();
//         String message = request.message();
            

//         return deepSeekService.streamChat(apiKey, message, refercontext, systemMessage);
//     }
// }