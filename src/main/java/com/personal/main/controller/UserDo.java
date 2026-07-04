package com.personal.main.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.dto.CreateChunkRequest;
import com.personal.main.dto.UserConfigRequest;
import com.personal.main.service.AuthService;
import com.personal.main.service.RagService;
import com.personal.main.service.UserDoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserDo {
    private final AuthService authService;
    private final RagService ragService;
    private final UserDoService userDoService;
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
    //创建配置
    @PostMapping("/usercreateconfig")
    public String createUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.CreateUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            userDoService.createuserconfig(userId, request.platformSource(), request.apiKey(), request.baseUrl());
            return ResponseEntity.ok("用户配置创建成功！当前用户 ID: " + userId).toString();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("用户配置创建失败: " + e.getMessage()).toString();
        }
    }
    //更新配置
    @PostMapping("/userupdateconfig")
    public String updateUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.UpdateUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            userDoService.updateuserconfig(userId, request.platformSource(), request.apiKey(), request.baseUrl(), request.isActive());
            return ResponseEntity.ok("用户配置更新成功！当前用户 ID: " + userId).toString();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("用户配置更新失败: " + e.getMessage()).toString();
        }
    }    
    //获取配置
    @PostMapping("/usergetconfig")
    public String getUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.GetUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            var config = userDoService.getuserconfig(userId, request.platformSource());
            return ResponseEntity.ok("用户配置获取成功！当前用户 ID: " + userId + ", 配置: " + config).toString();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("用户配置获取失败: " + e.getMessage()).toString();
        }
    }
    //删除配置
    @PostMapping("/userdeleteconfig")
    public String deleteUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.DeleteUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            userDoService.deleteuserconfig(userId, request.platformSource());
            return ResponseEntity.ok("用户配置删除成功！当前用户 ID: " + userId).toString();
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("用户配置删除失败: " + e.getMessage()).toString();
        }
    }



}