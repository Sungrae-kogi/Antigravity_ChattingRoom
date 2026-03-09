package com.example.chattingroom.controller;

import com.example.chattingroom.dto.MessageDTO;
import com.example.chattingroom.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatService chatService;

    @GetMapping("/history")
    public ResponseEntity<List<MessageDTO>> getChatHistory(
            // lastId가 들어오지 않으면 null 처리
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "50") int limit
    ){
        List<MessageDTO> history = chatService.getChatHistory(lastId, limit);

        return ResponseEntity.ok(history);
    }
}
