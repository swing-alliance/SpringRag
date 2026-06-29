package com.personal.main.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
@Data
public class KnowledgeChunk {
    private Long id;
    private String repoName;  // 对应 repo_name
    private Long userId;      // 对应 user_id
    private String fileName;    // 对应 file_name
    private String content;
    private String vectorData;  // 对应数据库的 text 字段 (JSON 字符串)
    private List<Float> vector; 
    
    private Date createTime;    // 对应 create_time
}