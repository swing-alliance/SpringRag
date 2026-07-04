package com.personal.main.service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
    public Map<Long, Float> getIndexMap(String question, Long userId, String repoName, Float referratio) {
        List<Float> q_vector = embeddingService.getEmbedding(question, true);
        List<Map<String, Object>> chunks = ragMapper.selectRawMaps(userId, repoName);
        Map<Long, Float> chunkSimilarityMap = new HashMap<>();
        for (Map<String, Object> chunk : chunks) {
            String vectorData = (String) chunk.get("vector_data");
            List<Float> chunkVector = gson.fromJson(vectorData, new TypeToken<List<Float>>() {}.getType());
            double similarity = MathService.cosinesimilarity(q_vector, chunkVector);
            Long chunkId = ((Number) chunk.get("id")).longValue();
            chunkSimilarityMap.put(chunkId, (float) similarity);
        }
        return chunkSimilarityMap.entrySet().stream()
        .filter(e -> e.getValue() >= referratio)
        // 降序排序：e2 比较 e1
        .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue())) 
        .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldVal, newVal) -> oldVal,
                LinkedHashMap::new // 必须显式指定 LinkedHashMap 来维持插入顺序
        ));}

    public String getKnowledgeContentByIds(List<Long> chunkIds) {
        List<KnowledgeChunk> chunks = ragMapper.selectChunksByIds(chunkIds);
        StringBuilder contentBuilder = new StringBuilder();
        for (KnowledgeChunk chunk : chunks) {
            contentBuilder.append("文件名: ").append(chunk.getFileName()).append("\n");
            contentBuilder.append("内容: ").append(chunk.getContent()).append("\n\n");
        }
        return contentBuilder.toString();
    }

    public void deleteKnowledgeChunkByRepo(Long userId, String repoName) {
        ragMapper.deleteByUserIdAndRepoName(userId, repoName);
    }
    public void deleteKnowledgeChunkById(Long userId) {
        ragMapper.deleteAllChunkById(userId);
    }


}