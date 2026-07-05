package com.personal.main.model;

import java.util.Date;

import lombok.Data;

/**
 * 聊天室实体类
 * 对应数据库表: chat_room
 */
@Data
public class ChatRoom {
    private Long id;
    private Long userId;
    private String roomName;
    private String repoName;
    private Date createTime;
    private Date updateTime;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}