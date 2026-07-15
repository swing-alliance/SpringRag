package com.personal.main.mapper;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.personal.main.dto.AboutChunkRequest.EditChunk;
import com.personal.main.model.ClusterIndexChunk;
import com.personal.main.model.KnowledgeChunk;
@Mapper
public interface RagMapper {


    @Insert("INSERT INTO knowledge_chunk (user_id, repo_name,file_name, content, vector_data, create_time) " +
        "VALUES (#{userId}, #{repoName}, #{fileName}, #{content}, #{vectorData}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertKnowledgeChunk(KnowledgeChunk chunk);


    @Delete("DELETE FROM knowledge_chunk WHERE user_id = #{userId} AND repo_name = #{repoName}")
    int deleteChunkByUserIdAndRepoName(@Param("userId") Long userId, @Param("repoName") String repoName);

    // 每一行数据都会被包成一个 Map (比如: {"id": 1, "vector_data": "[0.1, 0.2]"})
    @Select("SELECT id, vector_data FROM knowledge_chunk WHERE user_id = #{userId} AND repo_name = #{repoName}")
    List<Map<String, Object>> selectRawMaps(@Param("userId") Long userId, @Param("repoName") String repoName);


    @Delete("DELETE FROM knowledge_chunk WHERE id = #{id} and user_id = #{userId}")
    int deleteChunkById(@Param("id") Long chunkId, @Param("userId") Long userId);

    @Select("""
        <script>
        SELECT * FROM knowledge_chunk 
        <where>
            <choose>
                <when test="ids != null and ids.size() > 0">
                    id IN 
                    <foreach collection='ids' item='item' open='(' separator=',' close=')'>
                        #{item}
                    </foreach>
                </when>
                <otherwise>
                    1 = 0
                </otherwise>
            </choose>
        </where>
        </script>
        """)
List<KnowledgeChunk> selectChunksByIds(@Param("ids") List<Long> ids);



        @Select("SELECT id, repo_name, user_id, file_name, content, vector_data, create_time " +
        "FROM knowledge_chunk WHERE user_id = #{userId}")
        List<KnowledgeChunk> getChunksByUserId(@Param("userId") Long userId);


        @Update("UPDATE knowledge_chunk SET " +
        "content = #{content}, " +
        "file_name = #{fileName}, " +
        "vector_data = #{vectorData, typeHandler=com.personal.main.handler.ListFloatJsonHandler} " +
        "WHERE id = #{id}")
        int updateChunk(EditChunk chunk);





        // ==================== 1. 增 ====================
    @Insert("INSERT INTO ClusterIndexChunk (user_id, repo_name, vector_data) " +
            "VALUES (#{userId}, #{repoName}, #{vectorData, typeHandler=com.personal.main.handler.ListFloatJsonHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertClusterIndex(ClusterIndexChunk clusterIndexChunk);


    // ==================== 2. 改 ====================
    // 批量把多个chunk的cluster_id设置为指定的clusterId
    @Update("<script>" +
            "UPDATE knowledge_chunk SET cluster_id = #{clusterId} WHERE id IN " +
            "<foreach collection='chunkIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateClusterIdBatch(@Param("chunkIds") List<Long> chunkIds, @Param("clusterId") Long clusterId);

    // 清空某个知识库下的所有簇索引
    @Update("UPDATE knowledge_chunk SET cluster_id = NULL " +
            "WHERE user_id = #{userId} AND repo_name = #{repoName}")
    int clearClusterIdByRepo(@Param("userId") Long userId, @Param("repoName") String repoName);


    // ==================== 3. 查 ====================
    @Select("SELECT id, repo_name, user_id, file_name, content, vector_data, cluster_id, create_time " +
            "FROM knowledge_chunk WHERE cluster_id = #{clusterId}")
    @Results({
        @Result(column = "vector_data", property = "vectorData", 
                typeHandler = com.personal.main.handler.ListFloatJsonHandler.class)
    })
    List<KnowledgeChunk> getChunksByClusterId(@Param("clusterId") Long clusterId);

    @Select("SELECT id, repo_name, user_id, file_name, content, vector_data, cluster_id, create_time " +
            "FROM knowledge_chunk " +
            "WHERE user_id = #{userId} AND repo_name = #{repoName} AND cluster_id IS NULL")
    @Results({
        @Result(column = "vector_data", property = "vectorData", 
                typeHandler = com.personal.main.handler.ListFloatJsonHandler.class)
    })
    List<KnowledgeChunk> getUnclusteredChunks(@Param("userId") Long userId, @Param("repoName") String repoName);


    @Select("SELECT id, user_id, repo_name, vector_data " + 
        "FROM ClusterIndexChunk " +
        "WHERE user_id = #{userId} AND repo_name = #{repoName}")
    @Results({
        @Result(column = "id", property = "Id"),
        @Result(column = "user_id", property = "UserId"),
        @Result(column = "repo_name", property = "RepoName"),
        @Result(column = "vector_data", property = "VectorData", 
                typeHandler = com.personal.main.handler.ListFloatJsonHandler.class) 
    })
    List<ClusterIndexChunk> getClusterIndexChunksByUserIdAndRepoName(
            @Param("userId") Long userId, 
            @Param("repoName") String repoName
    );
    

    // ==================== 4. 删 ====================
    @Delete("DELETE FROM ClusterIndexChunk WHERE id = #{id}")
    int deleteClusterIndexById(@Param("id") Long id);

    @Delete("DELETE FROM ClusterIndexChunk WHERE user_id = #{userId} AND repo_name = #{repoName}")
    int deleteClusterIndicesByRepo(@Param("userId") Long userId, @Param("repoName") String repoName);







}