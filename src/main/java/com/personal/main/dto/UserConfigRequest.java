package com.personal.main.dto;

public class UserConfigRequest {
    public record CreateUserConfigRequest(String platformSource, String apiKey, String baseUrl) {}
    public record UpdateUserConfigRequest(String platformSource, String apiKey, String baseUrl, Integer isActive) {}
    public record DeleteUserConfigRequest(String platformSource) {}
    public record GetUserConfigRequest(String platformSource) {}
    
}
