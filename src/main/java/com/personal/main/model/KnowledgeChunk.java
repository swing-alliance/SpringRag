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

    
    private Date createTime;    // 对应 create_time
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getRepoName() {return repoName;}
    public void setRepoName(String repoName) {this.repoName = repoName;}
    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}
    public String getFileName() {return fileName;}
    public void setFileName(String fileName) {this.fileName = fileName;}
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
    public String getVectorData() {return vectorData;}
    public void setVectorData(String vectorData) {this.vectorData = vectorData;}

    public Date getCreateTime() {return createTime;}
    public void setCreateTime(Date createTime) {this.createTime = createTime;}
}