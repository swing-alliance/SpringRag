package com.personal.main.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.personal.main.context.MySessionContext;
import com.personal.main.dto.LoginRequest;
import com.personal.main.dto.RegisterRequest;
import com.personal.main.mapper.UserMapper;
import com.personal.main.model.User;
import com.personal.main.utils.RandomUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    public User verifyLogin(LoginRequest loginReq) {
        Optional<User> userOptional = userMapper.selectByUsername(loginReq.username());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("登录失败：用户不存在！");
        }
        if (!passwordEncoder.matches(loginReq.password(), userOptional.get().getPassword())) {
            throw new RuntimeException("登录失败：密码错误！");
        }
        return userOptional.get();
    }
    public User registerUser(RegisterRequest registerReq) {
        Optional<User> existingUser = userMapper.selectByUsername(registerReq.username());
        if (existingUser.isPresent()) {
            throw new RuntimeException("注册失败：用户名已存在！");
        }
        String randaccountId = RandomUtil.generateRandomAccountId(); // 生成随机账号ID
        User newUser = new User();
        newUser.setUsername(registerReq.username());
        newUser.setPassword(passwordEncoder.encode(registerReq.password())); // 使用 PasswordEncoder 加密密码
        newUser.setAccountId(randaccountId); // 设置随机账号ID
        return userMapper.insert(newUser) > 0 ? newUser : null; // 返回新注册的用户对象
    }
    public void saveUserSession(String token, User user) {
        MySessionContext.saveUser(token, user);
    }
    public Long authCookie(String token) {
        User currentUser = MySessionContext.getUser(token);
        if (currentUser == null) {
            throw new RuntimeException("未登录或登录已过期！");
        }
        return currentUser.getId();
    }
    public void logout(String token) {
        MySessionContext.removeUser(token);
    }
}