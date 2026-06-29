package com.personal.main.model;

import lombok.Data; // 确保导入了 lombok 

@Data // 只要留这一个注解，用来自动生成 getter/setter 即可
public class User {

    private Long id;
    
    private String username;
    private String password;
    private String accountId; 
}