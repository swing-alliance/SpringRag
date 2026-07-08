package com.personal.main.dto; // 根据你的项目修改包名
import java.util.List;
public class AboutChunkRequest {
    // 将首字母大写，符合 Java 类/Record 规范
    public record EditChunk(
        Long id,          // 对齐前端传过来的 chunkId (前端传的是 id)
        String fileName,  // 改为 String，单文件
        String content  ,  // 改为 String，单文本块内容
        List<Float> vectorData // 改为 List<Float>，向量数据
    ) {}
}