package com.personal.main.model;

import java.util.Date;

import lombok.Data;


@Data
public class ChatMessage {
    private Long id;              // 对应 id (主键)
    private Long userId;          // 对应 user_id (关联用户)
    private String repoName;      // 对应 repo_name (关联的知识库/仓库名)
    private String roomName;      // 对应 room_name (聊天室房间名/会话名称)
    private String userMessage;   // 对应 user_message (用户说的话)
    private String aiMessage;     // 对应 ai_message (AI回复的话)
    private String systemMessage; // 对应 system_message (当时使用的系统提示词)
    private Date createTime;      // 对应 create_time (对话时间)


    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}
    public String getRepoName() {return repoName;}
    public void setRepoName(String repoName) {this.repoName = repoName;}
    public String getRoomName() {return roomName;}
    public void setRoomName(String roomName) {this.roomName = roomName;}
    public String getUserMessage() {return userMessage;}
    public void setUserMessage(String userMessage) {this.userMessage = userMessage;}
    public String getAiMessage() {return aiMessage;}
    public void setAiMessage(String aiMessage) {this.aiMessage = aiMessage;}
    public String getSystemMessage() {return systemMessage;}
    public void setSystemMessage(String systemMessage) {this.systemMessage = systemMessage;}
    public Date getCreateTime() {return createTime;}
    public void setCreateTime(Date createTime) {this.createTime = createTime;}


}