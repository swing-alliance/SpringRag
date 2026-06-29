package com.personal.main.model;

import lombok.Data; // 确保导入了 lombok 

@Data // 只要留这一个注解，用来自动生成 getter/setter 即可
public class User {

    private Long id;
    
    private String username;
    private String password;
    private String accountId; 

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
    public String getAccountId() {return accountId;}
    public void setAccountId(String accountId) {this.accountId = accountId;}
}