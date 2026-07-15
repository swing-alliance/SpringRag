package com.personal.main.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.main.dto.AboutChunkRequest;
import com.personal.main.mapper.RagMapper;
import com.personal.main.model.KnowledgeChunk;
import com.personal.main.model.ClusterIndexChunk;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClusterIndexChunkService {
    private final RagMapper ragMapper;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper; // 用于将 Float 向量转为 JSON 字符串存储

    // 定义一个内部类，用来绑定 chunkId 和它对应的向量
    private static class VectorPoint {
        Long chunkId;
        float[] vector;

        VectorPoint(Long chunkId, List<Float> vectorList) {
            this.chunkId = chunkId;
            this.vector = new float[vectorList.size()];
            for (int i = 0; i < vectorList.size(); i++) {
                this.vector[i] = vectorList.get(i);
            }
        }
    }

    public void doclusterindex(Long userId, String repoName) {
        List<KnowledgeChunk> chunks = ragMapper.getChunksByUserId(userId).stream()
                .filter(chunk -> repoName.equals(chunk.getRepoName()))
                .collect(Collectors.toList());

        if (chunks.isEmpty()) {
            return;
        }

        List<String> texts = chunks.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.toList());

        // 1. 获取对应的嵌入向量
        List<List<Float>> Embeded_datas = embeddingService.getEmbeddings(texts, true);

        // 2. 将 chunks 与向量通过 index 绑定在一起
        List<VectorPoint> points = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            points.add(new VectorPoint(chunks.get(i).getId(), Embeded_datas.get(i)));
        }

        // 3. 计算 K 值并进行 K-Means++ 聚类
        int k = calculateOptimalK(points.size());
        KMeansResult clusterResult = runKMeansPlusPlus(points, k, 20);

        // 4. 遍历聚类结果，生成 ClusterIndexChunk 并批量更新原 chunk 的 cluster_id
        for (int i = 0; i < k; i++) {
            float[] centroid = clusterResult.centroids[i];
            List<VectorPoint> members = clusterResult.assignments.get(i);

            if (members.isEmpty()) continue;

            // 4.1 创建并保存簇索引
            ClusterIndexChunk cluster = new ClusterIndexChunk();
            cluster.setUserId(userId);
            cluster.setRepoName(repoName);
            List<Float> vectorList = new ArrayList<>(centroid.length);
            for (float f : centroid) {
                vectorList.add(f); // 自动装箱为 Float
            }
            cluster.setVectorData(vectorList);
            
            // 插入并获取自增主键 (需要在 XML 里面配 useGeneratedKeys="true" keyProperty="id")
            ragMapper.insertClusterIndex(cluster); 

            // 4.2 收集该簇下所有成员的 chunk ID
            List<Long> chunkIdsToUpdate = members.stream()
                    .map(p -> p.chunkId)
                    .collect(Collectors.toList());

            // 4.3 批量关联更新
            ragMapper.updateClusterIdBatch(chunkIdsToUpdate, cluster.getId());
        }
    }

    private int calculateOptimalK(int numElements) {
    // 1. 如果元素太少，直接归为 1 个簇，不进行无意义的分群
    if (numElements <= 4) {
        return 1;
    }
    
    // 2. 使用经典启发式公式：K ≈ 根号下(N / 2)，或者 4 * 根号(N)
    // 但必须确保 K 远远小于元素总量（比如最多不能超过总数的 30%）
    int calculatedK = (int) Math.sqrt(numElements / 2.0);
    if (calculatedK < 1) {
        calculatedK = 1;
    }
    
    // 3. 终极防御：确保 K 绝不大于元素总数的 1/3，且不超过上限 256
    int maxAllowedK = Math.max(1, numElements / 3);
    int finalK = Math.min(calculatedK, maxAllowedK);
    
    return Math.min(finalK, 256);
}

    private String toJsonString(float[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            return "[]";
        }
    }

    // ================== 聚类内部结果与 K-Means++ 实现 ==================

    private static class KMeansResult {
        float[][] centroids;
        Map<Integer, List<VectorPoint>> assignments;
    }

    private KMeansResult runKMeansPlusPlus(List<VectorPoint> points, int k, int maxIterations) {
        int n = points.size();
        int d = points.get(0).vector.length;
        float[][] centroids = new float[k][d];

        // 1. 初始化质心
        Random rand = new Random();
        centroids[0] = points.get(rand.nextInt(n)).vector.clone();

        float[] minDistances = new float[n];
        Arrays.fill(minDistances, Float.MAX_VALUE);

        for (int i = 1; i < k; i++) {
            float sumDist = 0.0f;
            for (int j = 0; j < n; j++) {
                float dist = distanceSq(points.get(j).vector, centroids[i - 1]);
                if (dist < minDistances[j]) {
                    minDistances[j] = dist;
                }
                sumDist += minDistances[j];
            }

            float target = rand.nextFloat() * sumDist;
            float currentSum = 0.0f;
            int nextCentroidIdx = n - 1;
            for (int j = 0; j < n; j++) {
                currentSum += minDistances[j];
                if (currentSum >= target) {
                    nextCentroidIdx = j;
                    break;
                }
            }
            centroids[i] = points.get(nextCentroidIdx).vector.clone();
        }

        // 2. 迭代微调
        Map<Integer, List<VectorPoint>> assignments = new HashMap<>();
        for (int iter = 0; iter < maxIterations; iter++) {
            assignments.clear();
            for (int i = 0; i < k; i++) assignments.put(i, new ArrayList<>());

            for (VectorPoint p : points) {
                int bestCluster = 0;
                float minDist = Float.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    float dist = distanceSq(p.vector, centroids[i]);
                    if (dist < minDist) {
                        minDist = dist;
                        bestCluster = i;
                    }
                }
                assignments.get(bestCluster).add(p);
            }

            boolean changed = false;
            for (int i = 0; i < k; i++) {
                List<VectorPoint> members = assignments.get(i);
                if (members.isEmpty()) continue;

                float[] newCentroid = new float[d];
                for (VectorPoint p : members) {
                    for (int dim = 0; dim < d; dim++) {
                        newCentroid[dim] += p.vector[dim];
                    }
                }
                for (int dim = 0; dim < d; dim++) {
                    newCentroid[dim] /= members.size();
                }

                if (distanceSq(centroids[i], newCentroid) > 1e-6f) {
                    centroids[i] = newCentroid;
                    changed = true;
                }
            }
            if (!changed) break;
        }

        KMeansResult result = new KMeansResult();
        result.centroids = centroids;
        result.assignments = assignments;
        return result;
    }

    private float distanceSq(float[] a, float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
    }



    public Long getmostrelatecluster(Long userId, String repoName, String question) {
    // 获取问题的嵌入向量
    List<List<Float>> quary_vectors = embeddingService.getEmbeddings(Arrays.asList(question), true);
    if (quary_vectors == null || quary_vectors.isEmpty()) {
        return null; // 或者抛出自定义异常
    }
    List<Float> quary_vector = quary_vectors.get(0);
    List<ClusterIndexChunk> clusterIndexChunks = ragMapper.getClusterIndexChunksByUserIdAndRepoName(userId, repoName);
    if (clusterIndexChunks == null || clusterIndexChunks.isEmpty()) {
        return null; // 没有找到任何聚类簇
    }
    Long mostRelatedClusterId = null;
    float maxSimilarity = -Float.MAX_VALUE; // 初始化为最小值

    // 遍历所有簇，计算余弦相似度，找出最相似的簇
    for (ClusterIndexChunk cluster : clusterIndexChunks) {
        List<Float> clusterVector = cluster.getVectorData();
        
        // 健壮性校验：确保簇向量不为空且维度与问题向量一致
        if (clusterVector == null || clusterVector.size() != quary_vector.size()) {
            continue; 
        }

        // 调用你提供的 MathService 计算相似度
        float similarity = MathService.cosinesimilarity(quary_vector, clusterVector);
        
        if (similarity > maxSimilarity) {
            maxSimilarity = similarity;
            mostRelatedClusterId = cluster.getId();
        }
    }
    return mostRelatedClusterId;
}


}