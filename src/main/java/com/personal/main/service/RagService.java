package com.personal.main.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.personal.main.mapper.RagMapper;
import com.personal.main.model.KnowledgeChunk;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class RagService {
    private final RagMapper ragMapper;
    private final EmbeddingService embeddingService;
    private final Gson gson;
    @Transactional
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
            ragMapper.insertKnowledgeChunk(chunk);
        }
    }
    public String answerquestion(String question,Long userId,String repoName) {
        // 1. 获取当前问题的向量表示
        List<Float> q_vector = embeddingService.getEmbedding(question, true); 
        
        // 2. 从数据库捞出所有的知识块原始数据 (包含 id 和 vector_data JSON 串)
        List<Map<String, Object>> chunks = ragMapper.selectRawMaps(userId, repoName); 
        
        // 3. 使用 StringBuilder 替代 String 拼接，提升循环内拼接的效率
        StringBuilder answerBuilder = new StringBuilder(); 
        // 4. 遍历计算相似度
        for (Map<String, Object> chunk : chunks) {
            String vectorData = (String) chunk.get("vector_data");
            
            //核心补全：使用 Gson 将数据库里的 JSON 字符串完美转回 List<Float>
            List<Float> chunkVector = gson.fromJson(vectorData, new TypeToken<List<Float>>(){}.getType());
            
            // 5. 计算问题向量与当前知识块向量的余弦相似度
            double similarity = MathService.cosinesimilarity(q_vector, chunkVector);
            
            // 6. 提取 id 并拼接结果。注意：Number 转型可以完美兼容不同数据库驱动对 id 的识别
            Long chunkId = ((Number) chunk.get("id")).longValue();
            answerBuilder.append("与块 [").append(chunkId).append("] 相似度: ").append(similarity).append("\n");
            System.out.println("与块 [" + chunkId + "] 相似度: " + similarity);
        }
        return answerBuilder.toString();
    }


    public void deleteKnowledgeChunkByRepo(Long userId, String repoName) {
        ragMapper.deleteByUserIdAndRepoName(userId, repoName);
    }
    public void deleteKnowledgeChunkById(Long userId) {
        ragMapper.deleteById(userId);
    }

}