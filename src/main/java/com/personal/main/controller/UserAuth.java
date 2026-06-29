package com.personal.main.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
            String randomToken = UUID.randomUUID().toString();
            authService.saveUserSession(randomToken, user);
            Cookie cookie = new Cookie("user_session", randomToken);
            cookie.setMaxAge(3600);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            System.out.println(user);
    System.out.println("====== [DEBUG] accountId 的值是: " + user.getAccountId());
            return ResponseEntity.ok("登录成功！当前用户账号: " + user.getAccountId());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerReq,HttpServletResponse response) {
        User registuser = authService.registerUser(registerReq);
        if (registuser != null) {
            return ResponseEntity.ok("注册成功！当前用户账号: " + registuser.getAccountId());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("注册失败！");
        }
    }
    
}
