package com.personal.main.dto;
public class UserChatAiRequest {
    public record UserChatAi(String message,Long roomid,float referratio,String systemMessage,String apisource,Boolean userag) {}
    public record UserCreateRoom(String roomName,String repoName) {}
    public record UserDeleteRoom(Long roomid) {}
    public record UserGetRoomMsg(Long roomid) {}
}

