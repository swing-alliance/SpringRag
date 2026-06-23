package com.personal.main.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.personal.main.model.User;

/**
 * 全局 Session 上下文（模拟 Session 存储）
 * 定位：全局静态内存容器
 */
public class MySessionContext {
    
    // 使用线程安全的 ConcurrentHashMap 存储所有在线用户的 Token 映射
    private static final Map<String, User> SESSION_MAP = new ConcurrentHashMap<>();

    // 登录成功时，由 Service 调用存入
    public static void saveUser(String token, User user) {
        SESSION_MAP.put(token, user);
    }

    // 认证时，由 Service 调用取出
    public static User getUser(String token) {
        return SESSION_MAP.get(token);
    }

    // 用户登出时清除
    public static void removeUser(String token) {
        SESSION_MAP.remove(token);
    }
}