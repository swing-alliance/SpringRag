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
import com.personal.main.dto.AboutChunkRequest.EditChunk;
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
    int totalSize = chunkSimilarityMap.size();
    long limitSize = (referratio != null && referratio > 0) ? (long) Math.ceil(totalSize / referratio) : totalSize;
    if (limitSize <= 0 && totalSize > 0) {
        limitSize = 1;
    }
    return chunkSimilarityMap.entrySet().stream()
            // 依然先按相似度从高到低降序排序
            .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue())) 
            // 【核心修正】根据你说的 1/ratio 数量截取，只留最高的这部分
            .limit(limitSize) 
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldVal, newVal) -> oldVal,
                    LinkedHashMap::new
            ));}

    //通过多个chunkid，得到相关文档
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
        ragMapper.deleteChunkByUserIdAndRepoName(userId, repoName);
    }
    public void deleteKnowledgeChunkById(Long chunkId,Long userId) {
        ragMapper.deleteChunkById(chunkId, userId);
    }

    public List<KnowledgeChunk> getChunksById(Long userId)
    {
        return ragMapper.getChunksById(userId);
    }


    public void updateChunk(EditChunk chunk) {
        try{
        List<Float> vectorData = embeddingService.getEmbedding(chunk.fileName(), true); // 检查内容是否为空
        if (vectorData == null || vectorData.isEmpty()) {
            throw new IllegalArgumentException("文件内容不能为空");
        }
        EditChunk newChunk = new EditChunk(chunk.id(), chunk.fileName(), chunk.content(), vectorData); // 创建新的EditChunk对象
        ragMapper.updateChunk(newChunk);
        }catch(Exception e){
            throw new RuntimeException("更新chunk失败", e);
        }
    }

}