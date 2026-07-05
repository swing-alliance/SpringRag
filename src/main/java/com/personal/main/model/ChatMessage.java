package com.personal.main.model;

import java.util.Date;

import lombok.Data;

@Data
public class ChatMessage {
    private Long id;
    private Long roomId;
    private Long userId;
    private Integer senderType;//发送者类型：1-用户(User), 2-AI助手(Assistant), 3-系统通知(System)
    private String content;
    private Date createTime;
    public static final int SENDER_TYPE_USER = 1;
    public static final int SENDER_TYPE_AI = 2;
    public static final int SENDER_TYPE_SYSTEM = 3;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getSenderType() { return senderType; }
    public void setSenderType(Integer senderType) { this.senderType = senderType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    
}