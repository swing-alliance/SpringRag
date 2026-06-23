package com.personal.main.model;

// 全部换成 Jakarta Persistence (JPA) 的正规军注解
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity // 1. 👈 告诉 JPA，这是一个需要跟数据库表绑定的实体类
@Table(name = "users") // 2. 👈 对应 MySQL 中的 users 表（注意：JPA 里是 name = "users"）
@Data
public class User {

    @Id // 3. 👈 明确声明这是主键（注意：必须是 jakarta.persistence.Id）
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. 核心！告诉 JPA 你的 MySQL id 是【自增】的
    private Long id;

    private String username;
    private String password;
    private String accountId;
}