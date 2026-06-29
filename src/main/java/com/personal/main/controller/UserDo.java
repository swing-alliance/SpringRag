package com.personal.main.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.dto.CreateChunkRequest;
import com.personal.main.service.AuthService;
import com.personal.main.service.RagService;
import com.personal.main.dto.RagRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserDo {
    private final AuthService authService;
    private final RagService ragService;
    @GetMapping("/mainpage")
    public String mainPage(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            return "欢迎访问主页面！当前用户 ID: " + userId;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
    @PostMapping("/usercreatechunk")
    public String postMethodName(@CookieValue(value = "user_session", defaultValue = "") String token,@RequestBody CreateChunkRequest createChunkReq) {
        try {
            Long userId = authService.authCookie(token);
            String[] fileNames = createChunkReq.fileName();
            String[] contents = createChunkReq.content();
            String repoName = createChunkReq.repoName();
            Boolean useLocalModel = createChunkReq.useLocalModel();
            ragService.saveKnowledgeChunk(userId, fileNames, contents, useLocalModel, repoName);
            return ResponseEntity.ok("知识块保存成功！当前用户 ID: " + userId).toString();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("知识块保存失败: " + e.getMessage()).toString();
        }
    }

    @PostMapping("/userdeletechunkbyrepo")
    public String deleteUserChunk(@CookieValue(value = "user_session", defaultValue = "") String token,@RequestBody String repoName) {
        try {
            Long userId = authService.authCookie(token);
            ragService.deleteKnowledgeChunkByRepo(userId, repoName);
            return ResponseEntity.ok("知识块删除成功！当前用户 ID: " + userId).toString();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("知识块删除失败: " + e.getMessage()).toString();
        }}

    @PostMapping("/useraskrag")
    public ResponseEntity<String> askRag(
        @CookieValue(value = "user_session", defaultValue = "") String token,
        @RequestBody RagRequest request // 🎯 用一个 @RequestBody 接收整块 JSON 对象
    ) {
        try {
            // 从 Token 中解析出用户 ID
            Long userId = authService.authCookie(token);
            
            // 从 request 对象中安全地获取字段（注意前端传的是 reponame，全小写）
            String answer = ragService.answerquestion(request.question(), userId, request.reponame());
            
            // 💎 正确姿势：去掉 .toString()，直接返回 ResponseEntity 对象
            return ResponseEntity.ok(answer);
        } catch (RuntimeException e) {
            // 💎 失败时同样直接返回对象，让 Spring 自动下发标准的 400 状态码
            return ResponseEntity.status(400).body("问题处理失败: " + e.getMessage());
        }
    }
}