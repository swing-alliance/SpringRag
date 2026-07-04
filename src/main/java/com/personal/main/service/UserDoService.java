package com.personal.main.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.personal.main.mapper.RagMapper;
import com.personal.main.mapper.UserMapper;
import com.personal.main.model.KnowledgeChunk;
import com.personal.main.model.UserConfig;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserDoService {
    private final RagMapper ragMapper;
    private final UserMapper userMapper;
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

    public void createuserconfig(Long userId, String platformSource, String apiKey, String baseUrl) {
        Optional<UserConfig> existingConfig = userMapper.selectUserConfigByUserIdAndPlatformSource(userId, platformSource);
        if (!existingConfig.isPresent()) {
            existingConfig = Optional.of(new UserConfig());
            existingConfig.get().setUserId(userId);
            existingConfig.get().setPlatformSource(platformSource);
            existingConfig.get().setApiKey(apiKey);
            existingConfig.get().setBaseUrl(baseUrl);
            existingConfig.get().setIsActive(1);
            userMapper.insertUserConfigApi(existingConfig.get());
        }else{
            throw new RuntimeException("配置已存在，无法创建新配置！");
        }
    }

    public void updateuserconfig(Long userId, String platformSource, String apiKey, String baseUrl,Integer isActive) {
        Optional<UserConfig> existingConfig = userMapper.selectUserConfigByUserIdAndPlatformSource(userId, platformSource);
        if (existingConfig.isPresent()) {
            existingConfig.get().setApiKey(apiKey);
            existingConfig.get().setBaseUrl(baseUrl);
            existingConfig.get().setIsActive(isActive);
            userMapper.updateUserConfigApi(existingConfig.get());
        } else {
            throw new RuntimeException("配置不存在，无法更新！");
        }
    }

    public UserConfig getuserconfig(Long userId, String platformSource) {
        Optional<UserConfig> existingConfig = userMapper.selectUserConfigByUserIdAndPlatformSource(userId, platformSource);
        if (existingConfig.isPresent()) {
            UserConfig config = existingConfig.get();
            return config;
        } else {
            throw new RuntimeException("配置不存在！");
        }
    }

    public void deleteuserconfig(Long userId, String platformSource) {
        Optional<UserConfig> existingConfig = userMapper.selectUserConfigByUserIdAndPlatformSource(userId, platformSource);
        if (existingConfig.isPresent()) {
            userMapper.deleteUserConfigApi(userId, platformSource);
        } else {
            throw new RuntimeException("配置不存在或错误，无法删除！");
        }
    }

    

}