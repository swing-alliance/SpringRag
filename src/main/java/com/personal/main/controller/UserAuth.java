package com.personal.main.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.common.Result;
import com.personal.main.dto.LoginRequest;
import com.personal.main.dto.RegisterRequest;
import com.personal.main.model.User;
import com.personal.main.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.personal.main.common.Result;
@RestController
@RequiredArgsConstructor
public class UserAuth {
    private final AuthService authService;
    @PostMapping("/api/logout")
    public ResponseEntity<Result<Boolean>> logout(@CookieValue(value = "user_session", defaultValue = "") String token, HttpServletResponse response) {
        try {
            authService.authCookie(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, e.getMessage()));
        }
        authService.logout(token);
        Cookie cookie = new Cookie("user_session", "");
        cookie.setMaxAge(0);
        cookie.setPath("/"); 
        response.addCookie(cookie);
        return ResponseEntity.ok(Result.success(true));
    }
    @PostMapping("/api/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@RequestBody LoginRequest loginReq, HttpServletResponse response) {
        try {
            User user = authService.verifyLogin(loginReq);
            String randomToken = UUID.randomUUID().toString();
            authService.saveUserSession(randomToken, user);

            // Cookie 设置保持不变
            Cookie cookie = new Cookie("user_session", randomToken);
            cookie.setMaxAge(3600);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            Map<String, Object> data = new HashMap<>();
            data.put("accountId", user.getAccountId());

            // 2. 成功：HTTP 状态码返回 200 (OK)，Body 传入包装好的 Result.success
            return ResponseEntity.ok(Result.success("登录成功！", data));

        } catch (RuntimeException e) {
            // 3. 失败：HTTP 状态码返回 401 (Unauthorized)，Body 传入 Result.error
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(401, e.getMessage()));
        }
    }
    
    @PostMapping("/api/register")
    public ResponseEntity<Result<Boolean>> register(@RequestBody RegisterRequest registerReq,HttpServletResponse response) {
        try {
            User registuser = authService.registerUser(registerReq);
            return ResponseEntity.ok(Result.success("注册成功", true));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(404, e.getMessage()));
        }


    }
}
