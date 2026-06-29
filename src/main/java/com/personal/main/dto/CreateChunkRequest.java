package com.personal.main.dto;

public  record CreateChunkRequest (String repoName,String[] fileName, String[] content,Boolean useLocalModel) {}
