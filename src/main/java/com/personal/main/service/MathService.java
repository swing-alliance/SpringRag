package com.personal.main.service;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MathService {
    public static float cosinesimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("向量长度不一致");
        }

        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
}
