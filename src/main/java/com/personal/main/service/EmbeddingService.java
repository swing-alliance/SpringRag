package com.personal.main.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.BgeSmallZhQuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

@Service
public class EmbeddingService {

    // 本地模型（BGE-Small-ZH 量化版，适合中文，纯本地运行）
    private final EmbeddingModel embeddingModelLocal = new BgeSmallZhQuantizedEmbeddingModel();

    // 远程 OpenAI 模型
    private final EmbeddingModel embeddingModelRemote = OpenAiEmbeddingModel.builder()
            .apiKey("demo") 
            .modelName("text-embedding-3-small")
            .dimensions(512)
            .build();

    /**
     * 单个文本转向量
     *
     * @param text     待向量化文本
     * @param useLocal true=使用本地BGE模型，false=使用OpenAI
     * @return 向量列表
     */
    public List<Float> getEmbedding(String text, boolean useLocal) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("文本内容不能为空");
        }
        EmbeddingModel model = useLocal ? embeddingModelLocal : embeddingModelRemote;
        Embedding embedding = model.embed(text).content();
        

        return embedding.vectorAsList();
    }

    /**
     * 批量文本转向量
     *
     * @param texts    待向量化文本列表
     * @param useLocal true=使用本地BGE模型，false=使用OpenAI
     * @return 向量列表的列表
     */
    public List<List<Float>> getEmbeddings(List<String> texts, boolean useLocal) {
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("文本列表不能为空");
        }

        // 过滤空字符串（可选增强）
        List<TextSegment> segments = texts.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(TextSegment::from)
                .collect(Collectors.toList());

        if (segments.isEmpty()) {
            throw new IllegalArgumentException("过滤后没有有效文本");
        }

        EmbeddingModel model = useLocal ? embeddingModelLocal : embeddingModelRemote;
        List<Embedding> embeddings = model.embedAll(segments).content();

        return embeddings.stream()
                .filter(Objects::nonNull)
                .map(Embedding::vectorAsList)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        EmbeddingService embeddingService = new EmbeddingService();
        String text = "这是一个测试文本，用于生成向量表示。";
        List<Float> embedding = embeddingService.getEmbedding(text, true);
        System.out.println("向量长度: " + embedding.size());
        System.out.println("向量内容: " + embedding);
    }
}