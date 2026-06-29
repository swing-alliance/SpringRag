package com.personal.main.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.personal.main.model.User;
@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    Optional<User> selectByUsername(String username);

    @Insert("INSERT INTO users (username, password, account_id) VALUES (#{username}, #{password}, #{accountId})")
    @Options(useGeneratedKeys = true, keyProperty = "id") 
    int insert(User user);

    
}