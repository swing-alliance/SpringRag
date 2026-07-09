package com.personal.main.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.Optional;
import com.personal.main.model.ChatMessage;
import com.personal.main.model.ChatRoom;
@Mapper
public interface AiChatMapper {

    /**
     * 创建聊天室（因为加了唯一索引，如果名字重复，这里会抛出 DataIntegrityViolationException 异常）
     */
    @Insert("INSERT INTO chat_room (user_id, room_name, repo_name) " +
            "VALUES (#{userId}, #{roomName}, #{repoName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createRoom(ChatRoom chatRoom);

    /**
     * 按照聊天室 ID 删除聊天室
     * 加上 user_id 校验，防止越权删除别人的房间
     * 触发外键级联，会自动删除对应的 chat_message 记录
     */
    @Delete("DELETE FROM chat_room WHERE id = #{roomId} AND user_id = #{userId}")
    int deleteRoomById(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 修改指定聊天室绑定的 RAG 知识库 (repo_name)
     */
    @Update("UPDATE chat_room SET repo_name = #{repoName} " +
            "WHERE id = #{roomId} AND user_id = #{userId}")
    int updateRoomRepo(@Param("roomId") Long roomId, 
                       @Param("userId") Long userId, 
                       @Param("repoName") String repoName);

    /**
     * 修改聊天室的名字 (room_name)
     * 注意：如果新名字与其他房间冲突，同样会触发唯一索引限制而报错
     */
    @Update("UPDATE chat_room SET room_name = #{roomName} " +
            "WHERE id = #{roomId} AND user_id = #{userId}")
    int updateRoomName(@Param("roomId") Long roomId, 
                       @Param("userId") Long userId, 
                       @Param("roomName") String roomName);



    /**
     * 新增单条聊天消息
     */
    @Insert("INSERT INTO chat_message (room_id, user_id, sender_type, content) " +
            "VALUES (#{roomId}, #{userId}, #{senderType}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveMessage(ChatMessage chatMessage);


    // ==========================================
    // 删除 (Delete)
    // ==========================================

    /**
     * 逻辑A：清空某个聊天室的所有历史消息（重置当前对话，保留房间）
     */
    @Delete("DELETE FROM chat_message WHERE room_id = #{roomId} AND user_id = #{userId}")
    int clearMessagesByRoomId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * 逻辑B：用户撤回或单条删除某消息
     */
    @Delete("DELETE FROM chat_message WHERE id = #{messageId} AND user_id = #{userId}")
    int deleteMessageById(@Param("messageId") Long messageId, @Param("userId") Long userId);


    // ==========================================
    // 查询 (Select)
    // ==========================================

    /**
     * 加载聊天历史记录（按时间正序排列）
     */
    @Select("SELECT id, room_id, user_id, sender_type, content, create_time " +
            "FROM chat_message WHERE room_id = #{roomId} ORDER BY create_time ASC")
    List<ChatMessage> listMessagesByRoomId(@Param("roomId") Long roomId);



    /**
     * 根据聊天室ID和用户ID，获取该房间当前绑定的 RAG 知识库名称
     * 加上 user_id 校验可以防止用户越权查看其它用户的房间信息
     */
    @Select("SELECT repo_name FROM chat_room WHERE id = #{roomId} AND user_id = #{userId}")
    String getRepoNameByRoomId(@Param("roomId") Long roomId, @Param("userId") Long userId);


    @Select("SELECT * FROM chat_room WHERE user_id = #{userId}")
        List<ChatRoom> getChatRoomsById(@Param("userId") Long userId);
        

}
