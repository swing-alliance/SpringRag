package com.personal.main.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 1. 告诉 Spring 启动时来扫描我
public class SecurityConfig {
    @Bean // 2.告诉 Spring 这个方法返回的对象要收进弹药库
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}