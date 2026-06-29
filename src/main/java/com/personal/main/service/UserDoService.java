package com.personal.main.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.personal.main.mapper.RagMapper;
import com.personal.main.model.KnowledgeChunk;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserDoService {
    private final RagMapper ragMapper;
    private final EmbeddingService embeddingService;
    public void saveKnowledgeChunk(Long userId, String[]fileNames, String[]contents,Boolean useLocalModel,String repoName) {
        if (fileNames.length != contents.length) {
            throw new IllegalArgumentException("文件名和内容数组长度不一致");
        }
        List<List<Float>> vectors = embeddingService.getEmbeddings(Arrays.asList(fileNames), useLocalModel); // 获取向量表示
        for (int i=0; i<contents.length; i++) {
            // 将向量列表转换为 JSON 字符串
            String vectorData = vectors.get(i).toString();
            // 创建 KnowledgeChunk 对象
            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setUserId(userId);
            chunk.setRepoName(repoName);
            chunk.setFileName(fileNames[i]);
            chunk.setContent(contents[i]);
            chunk.setVectorData(vectorData);
            chunk.setCreateTime(new Date()); // 设置当前时间为创建时间

            // 调用 Mapper 方法保存到数据库
            ragMapper.insertKnowledgeChunk(chunk);
        }
    }

}