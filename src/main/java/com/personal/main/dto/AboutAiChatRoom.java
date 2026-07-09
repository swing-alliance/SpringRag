package com.personal.main.dto; // 根据你的项目修改包名
public class AboutAiChatRoom {
    // 将首字母大写，符合 Java 类/Record 规范
    public record createRoom(
        String roomName,  // 改为 String，单文件
        String repoName  // 改为 String，单文本块内容
    ) {}
    public record deleteroom(
        Long roomId        // 对齐前端传过来的 chunkId (前端传的是 id)
    ) {}
    public record updateRoom(
        Long roomId,  // 改为 String，单文件
        String repoName  // 改为 String，单文本块内容
    ) {}
}