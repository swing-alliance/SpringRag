package com.personal.main.model;

import java.util.Date;

import lombok.Data;

/**
 * 对应数据库表 user_config (用户API密钥配置表) 的实体类
 */
@Data
public class UserConfig {
    
    private Long id;                // 对应 id (主键ID)
    private Long userId;            // 对应 user_id (关联的用户ID)
    private String platformSource;  // 对应 platform_source (API来源：DEEPSEEK 或 OPENAI)
    private String apiKey;          // 对应 api_key (用户密钥)
    private String baseUrl;         // 对应 base_url (可选：用户自定义的中转地址)
    private Integer isActive;       // 对应 is_active (是否启用：1启用，0禁用)
    private Date createTime;        // 对应 create_time (创建时间)
    private Date updateTime;        // 对应 update_time (更新时间)

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}
    public String getPlatformSource() {return platformSource;}
    public void setPlatformSource(String platformSource) {this.platformSource = platformSource;}
    public String getApiKey() {return apiKey;}
    public void setApiKey(String apiKey) {this.apiKey = apiKey;}
    public String getBaseUrl() {return baseUrl;}
    public void setBaseUrl(String baseUrl) {this.baseUrl = baseUrl;}
    public Integer getIsActive() {return isActive;}
    public void setIsActive(Integer isActive) {this.isActive = isActive;}
    public Date getCreateTime() {return createTime;}
    public void setCreateTime(Date createTime) {this.createTime = createTime;}
    public Date getUpdateTime() {return updateTime;}
    public void setUpdateTime(Date updateTime) {this.updateTime = updateTime;}
}