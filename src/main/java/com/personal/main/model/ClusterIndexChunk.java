package com.personal.main.model;
import java.util.List;
public class ClusterIndexChunk {
    Long Id;
    Long UserId;
    String RepoName;
    List<Float> VectorData;
    
    public Long getId() {
        return Id;
    }
    public void setId(Long id) {
        Id = id;
    }
    public Long getUserId() {
        return UserId;
    }
    public void setUserId(Long userId) {
        UserId = userId;
    }
    public String getRepoName() {
        return RepoName;
    }
    public void setRepoName(String repoName) {
        RepoName = repoName;
    }
    public List<Float> getVectorData() {
        return VectorData;
    }
    public void setVectorData(List<Float> vectorData) {
        VectorData = vectorData;
    }
}
