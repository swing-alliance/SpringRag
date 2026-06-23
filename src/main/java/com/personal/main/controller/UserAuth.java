package com.personal.main.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.dto.LoginRequest;
import com.personal.main.dto.RegisterRequest;
import com.personal.main.model.User;
import com.personal.main.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserAuth {
    private final AuthService authService;
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = "user_session", defaultValue = "") String token, HttpServletResponse response) {
        try {
            authService.authCookie(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        authService.logout(token);
        Cookie cookie = new Cookie("user_session", "");
        cookie.setMaxAge(0);
        cookie.setPath("/"); 
        response.addCookie(cookie);
        return ResponseEntity.ok("您已成功退出登录！");
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginReq, HttpServletResponse response) {
        try {
            User user = authService.verifyLogin(loginReq);
            
            // 生成随机 Session Token
            String randomToken = UUID.randomUUID().toString();
            authService.saveUserSession(randomToken, user);
            
            // 下发 Cookie
            Cookie cookie = new Cookie("user_session", randomToken);
            cookie.setMaxAge(3600);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.ok("登录成功！当前用户账号: " + user.getAccountId());
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
     @GetMapping("/mainpage")
    public String mainPage(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            return "欢迎访问主页面！当前用户 ID: " + userId;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registerReq,HttpServletResponse response) {
        // 1. 先调用 AuthController 的注册方法，完成用户注册
        User registuser = authService.registerUser(registerReq);
        // 2. 注册成功后，直接调用登录方法，完成自动登录
        if (registuser != null) {
            return "注册成功！";
        } else {
            return "注册失败！"; // 注册失败，直接返回错误消息
        }
    }
}
