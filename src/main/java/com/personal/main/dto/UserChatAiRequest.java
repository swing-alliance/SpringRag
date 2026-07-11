package com.personal.main.dto;
public class UserChatAiRequest {
    public record UserChatAi(String message,Long roomid,float referratio,String systemMessage,Boolean userag) {}
}

