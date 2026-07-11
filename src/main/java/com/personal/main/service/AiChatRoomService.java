package com.personal.main.service;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.personal.main.mapper.AiChatMapper;
import com.personal.main.model.ChatMessage;
import com.personal.main.model.ChatRoom;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiChatRoomService {
    private final AiChatMapper aiChatMapper;

    public void createaichatroom(ChatRoom chatRoom) {
        try {
            aiChatMapper.createRoom(chatRoom);
        } catch (Exception e) {
                throw new RuntimeException("创建聊天室失败: " + e.getMessage());
        }
    }
    public void deleteaichatroom(Long roomId, Long userId) {
        try {
            aiChatMapper.deleteRoomById(roomId,userId);
        } catch (Exception e) {
            throw new RuntimeException("删除聊天室失败: " + e.getMessage());
        }
    }
    public void updateaichatroomRepo(Long roomId, Long userId, String repoName, 
                                    String roomName, String platformSource) {
        try {
            int rows = aiChatMapper.updateRoomRepo(roomId, userId, repoName, roomName, platformSource);
            if (rows == 0) {
                throw new RuntimeException("未找到对应聊天室或无权限更新");
            }
        } catch (Exception e) {
            throw new RuntimeException("更新聊天室失败: " + e.getMessage(), e);
        }
    }
    public Optional<List<ChatRoom>> getallaichatrooms(Long userId) {
        try {
            List<ChatRoom> chatRooms = aiChatMapper.getChatRoomsById(userId);
            if (chatRooms == null || chatRooms.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(chatRooms);
            }
        }catch (Exception e) {
            throw new RuntimeException("获取所有聊天室失败: " + e.getMessage());
        }
    }

    public void airecordmsg(Long roomId, Long userId,String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setUserId(userId);
        chatMessage.setSenderType(ChatMessage.SENDER_TYPE_AI);
        chatMessage.setContent(content);
        try {
            aiChatMapper.saveMessage(chatMessage);
        } catch (Exception e) {
            throw new RuntimeException("记录AI消息失败: " + e.getMessage());
        }
    }
    public void userrecordmsg(Long roomId, Long userId,String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoomId(roomId);
        chatMessage.setUserId(userId);
        chatMessage.setSenderType(ChatMessage.SENDER_TYPE_USER);
        chatMessage.setContent(content);
        try {
            aiChatMapper.saveMessage(chatMessage);
        } catch (Exception e) {
            throw new RuntimeException("记录用户消息失败: " + e.getMessage());
        }
    }
    public String getHisChatMassageByRoomId(Long roomId, Long userId) {
        try {
            List<ChatMessage> messages = aiChatMapper.listMessagesByRoomId(roomId, userId);
            StringBuilder chatHistory = new StringBuilder();
            for (ChatMessage message : messages) {
                String sender = message.getSenderType() == ChatMessage.SENDER_TYPE_USER ? "用户" : "AI助手";
                chatHistory.append(sender).append(": ").append(message.getContent()).append("\n");
            }
            return chatHistory.toString();
        } catch (Exception e) {
            throw new RuntimeException("获取聊天室消息失败: " + e.getMessage());
        }
    }
    public List<ChatMessage> getHisChatMsgByRoomId(Long roomId, Long userId) {
        try {
            List<ChatMessage> messages = aiChatMapper.listMessagesByRoomId(roomId, userId);
            return messages;
        } catch (Exception e) {
            throw new RuntimeException("获取聊天室消息失败: " + e.getMessage());
        }
    }



    public String getRepoNameByRoomId(Long roomId, Long userId) {
        try {
            return aiChatMapper.getRepoNameByRoomId(roomId, userId);
        } catch (Exception e) {
            throw new RuntimeException("获取聊天室绑定的知识库失败: " + e.getMessage());
        }
    }

    public ChatRoom getaichatroominfo(Long roomId, Long userId) {
        try {
            return aiChatMapper.getChatRoomsById(userId).stream()
                    .filter(room -> room.getId().equals(roomId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到对应聊天室"));
        } catch (Exception e) {
            throw new RuntimeException("获取聊天室信息失败: " + e.getMessage());
        }
    }


}
