package com.personal.main.dto;

public record UserAskAiRequest(String message,String repoName,float referratio,String systemMessage,String apisource,Boolean userag) {}