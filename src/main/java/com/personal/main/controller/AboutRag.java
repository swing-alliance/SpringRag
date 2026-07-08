package com.personal.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.personal.main.common.Result;
import com.personal.main.dto.UserChatAiRequest;
import com.personal.main.model.KnowledgeChunk;
import com.personal.main.service.AiChatRoomService;
import com.personal.main.service.AuthService;
import com.personal.main.service.DeepSeekService;
import com.personal.main.service.RagService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AboutRag {

    private final DeepSeekService deepSeekService;
    private final AuthService authService;
    private final RagService ragService;

    @GetMapping("/api/getchunks")
    public ResponseEntity<Result<List<KnowledgeChunk>>> getChunks(@CookieValue(value = "user_session", defaultValue = "") String token){
                try {
                    Long userid=authService.authCookie(token);
                    List<KnowledgeChunk> chunks=ragService.getChunksById(userid);
                    for(KnowledgeChunk chunk:chunks){
                        chunk.setVectorData(null);
                    }
                    return ResponseEntity.ok(Result.success("成功得到chunks", chunks));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(404,e.getMessage()));
                }
            }

}