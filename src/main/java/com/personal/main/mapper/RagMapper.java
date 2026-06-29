package com.personal.main.mapper;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import com.personal.main.model.KnowledgeChunk;
import java.util.Map;
@Mapper
public interface RagMapper {


    @Insert("INSERT INTO knowledge_chunk (user_id, repo_name,file_name, content, vector_data, create_time) " +
        "VALUES (#{userId}, #{repoName}, #{fileName}, #{content}, #{vectorData}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertKnowledgeChunk(KnowledgeChunk chunk);


    @Delete("DELETE FROM knowledge_chunk WHERE user_id = #{userId} AND repo_name = #{repoName}")
    int deleteByUserIdAndRepoName(@Param("userId") Long userId, @Param("repoName") String repoName);

    // 每一行数据都会被包成一个 Map (比如: {"id": 1, "vector_data": "[0.1, 0.2]"})
    @Select("SELECT id, vector_data FROM knowledge_chunk WHERE user_id = #{userId} AND repo_name = #{repoName}")
    List<Map<String, Object>> selectRawMaps(@Param("userId") Long userId, @Param("repoName") String repoName);


    @Delete("DELETE FROM knowledge_chunk WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}