package com.personal.main.controller;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.common.Result;
import com.personal.main.dto.CreateChunkRequest;
import com.personal.main.dto.UserChatAiRequest;
import com.personal.main.dto.UserConfigRequest;
import com.personal.main.model.ChatRoom;
import com.personal.main.model.User;
import com.personal.main.service.AiChatRoomService;
import com.personal.main.service.AuthService;
import com.personal.main.service.RagService;
import com.personal.main.service.UserDoService;
import com.personal.main.dto.AboutChunkRequest.EditChunk;
import lombok.RequiredArgsConstructor;
import com.personal.main.dto.AboutChunkRequest.DeleteChunk;
import com.personal.main.dto.AboutChunkRequest.deleterepo;
import com.personal.main.model.UserConfig;
import java.util.List;
import com.personal.main.dto.AboutAiChatRoom;
import java.util.Optional;
@RestController
@RequiredArgsConstructor
public class UserDo {
    private final AiChatRoomService aiChatRoomService;
    private final AuthService authService;
    private final RagService ragService;
    private final UserDoService userDoService;
    @GetMapping("/api/getusername")
    public ResponseEntity<Result<Boolean>> mainPage(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            Optional<User> user = authService.getUserById(userId);
            if (user.isPresent()) {
                return ResponseEntity.ok(Result.success(user.get().getUsername(),true));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "用户不存在,请重新登录"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(404,e.getMessage()));
        }
    }
   


    //创建配置
    @PostMapping("/api/usercreateconfig")
    public ResponseEntity<Result<String>> createUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.CreateUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            userDoService.createuserconfig(userId, request.platformSource(), request.apiKey(), request.baseUrl());
            return ResponseEntity.ok(Result.success("用户配置创建成功！ ",userId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }
    //更新配置
    @PostMapping("/api/userupdateconfig")
    public ResponseEntity<Result<String>> updateUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.UpdateUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            userDoService.updateuserconfig(userId, request.platformSource(), request.apiKey(), request.baseUrl(), request.isActive());
            return ResponseEntity.ok(Result.success("用户配置更新成功！当前用户 ID: ",userId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }    
    //获取配置
    @GetMapping("/api/usergetconfig")
    public ResponseEntity<Result<List<UserConfig>>> getUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            List<UserConfig> config = userDoService.getuserconfig(userId);
            if (config!=null && config.size()>0) {
                return ResponseEntity.ok(Result.success("用户配置获取成功！",config));
                } else {
                return ResponseEntity.ok(Result.success("用户配置获取成功！ ",null));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }
    //删除配置
    @PostMapping("/api/userdeleteconfig")
    public ResponseEntity<Result<String>> deleteUserConfig(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody UserConfigRequest.DeleteUserConfigRequest request) {
        try {
            Long userId = authService.authCookie(token);
            userDoService.deleteuserconfig(userId, request.platformSource());
            return ResponseEntity.ok(Result.success("用户配置删除成功！",userId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }
    


    








    //删除聊天室
    @PostMapping("/api/userdeleteaichatroom")
    public ResponseEntity<Result<String>> deleteroom(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody AboutAiChatRoom.deleteroom request) {
        try {
            Long userId = authService.authCookie(token);
            Long roomId = request.roomId();
            aiChatRoomService.deleteaichatroom(roomId, userId);
            return ResponseEntity.ok(Result.success("聊天室删除成功！",roomId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }}


     //创建aichatroom
    @PostMapping("/api/usercreateaichatroom")
    public ResponseEntity<Result<String>> createaichatroom(@CookieValue(value = "user_session", defaultValue = "") String token, @RequestBody AboutAiChatRoom.createRoom request) {
        try {
            Long userId= authService.authCookie(token);
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setUserId(userId);
            chatRoom.setRoomName(request.roomName());
            chatRoom.setRepoName(request.repoName());
            chatRoom.setPlatformSource(request.platformSource());
            aiChatRoomService.createaichatroom(chatRoom);
            return ResponseEntity.ok(Result.success("用户创建房间成功！ ",userId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }
    //更新aichatroom
    @PostMapping("/api/userupdateaichatroom")
    public ResponseEntity<Result<String>> updateaichatroom(
            @CookieValue(value = "user_session", defaultValue = "") String token, 
            @RequestBody AboutAiChatRoom.updateRoom request) {
        try {
            System.out.println("request: " + request);
            Long userId = authService.authCookie(token);
            aiChatRoomService.updateaichatroomRepo(
                    request.roomId(), 
                    userId, 
                    request.repoName(), 
                    request.roomName(), 
                    request.platformSource()
            );
            return ResponseEntity.ok(Result.success("用户更新房间成功！", userId.toString()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }  
    //获取aichatroom
    @GetMapping("/api/usergetaichatroom")
    public ResponseEntity<Result<List<ChatRoom>>> getaichatroom(@CookieValue(value = "user_session", defaultValue = "") String token) {
        try {
            Long userId = authService.authCookie(token);
            Optional<List<ChatRoom>> chatRooms = aiChatRoomService.getallaichatrooms(userId);
            if (!chatRooms.isPresent()) {
                return ResponseEntity.ok(Result.success("用户！",null));
                } else {
                return ResponseEntity.ok(Result.success("用户配置获取成功！ ",chatRooms.get()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Result.error(400, e.getMessage()));
        }
    }


}