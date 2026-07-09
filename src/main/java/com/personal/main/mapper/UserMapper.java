package com.personal.main.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.personal.main.model.User;
import com.personal.main.model.UserConfig;
@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    Optional<User> selectByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id} LIMIT 1")
    Optional<User> selectById(Long id);


    @Insert("INSERT INTO users (username, password, account_id) VALUES (#{username}, #{password}, #{accountId})")
    @Options(useGeneratedKeys = true, keyProperty = "id") 
    int insert(User user);

    // 1. 根据 user_id 和 platform_source 查询用户配置
    @Select("SELECT * FROM user_config WHERE user_id = #{userId} AND platform_source = #{platformSource} LIMIT 1")
    Optional<UserConfig> selectUserConfigByUserIdAndPlatformSource(
            @Param("userId") Long userId, 
            @Param("platformSource") String platformSource
    );

    @Select("SELECT * FROM user_config WHERE user_id = #{userId}")
    Optional<UserConfig> selectUserConfigByUserId(Long userId);

    /**
     * 2. 创建配置
     * useGeneratedKeys = true 会把自增的 id 自动回填到 userConfig 对象中
     */
    @Insert("INSERT INTO user_config(user_id, platform_source, api_key, base_url, is_active) " +
            "VALUES(#{userId}, #{platformSource}, #{apiKey}, #{baseUrl}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUserConfigApi(UserConfig userConfig);

    /**
     * 3. 修改配置
     * 动态根据 user_id 和 platform_source 去更新具体的密钥和中转路径
     */
    @Update("UPDATE user_config SET api_key = #{apiKey}, base_url = #{baseUrl}, " +
            "is_active = #{isActive}, update_time = CURRENT_TIMESTAMP " +
            "WHERE user_id = #{userId} AND platform_source = #{platformSource}")
    int updateUserConfigApi(UserConfig userConfig);

    //4. 根据 user_id 和 platform_source 删除配置
    @Delete("DELETE FROM user_config WHERE user_id = #{userId} AND platform_source = #{platformSource}")
    int deleteUserConfigApi(@Param("userId") Long userId, @Param("platformSource") String platformSource);

    

    
}