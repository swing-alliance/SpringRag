package com.personal.main.controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.service.AuthService;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
public class UserDo {
    private final AuthService authService;

    @GetMapping("/mainpage")
    public String mainPage(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            return "欢迎访问主页面！当前用户 ID: " + userId;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}