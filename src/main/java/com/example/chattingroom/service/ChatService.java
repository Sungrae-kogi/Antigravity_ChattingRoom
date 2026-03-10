package com.example.chattingroom.service;

import com.example.chattingroom.dto.MessageDTO;
import com.example.chattingroom.mapper.ChatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMapper chatMapper;

    private final ChatBufferService chatBufferService;

    @Async
    public void saveMessageAsync(String sender, String content) {
//        chatMapper.insertMessage(sender, content);

        // DTO로 포장해서, Redis Buffer로 전송  -> 메시지가 오면 바로 DB에 저장이 아니라, Redis Buffer에 바로 쌓아올림.
        MessageDTO dto = MessageDTO.builder()
                .sender(sender)
                .content(content)
                .build();

        chatBufferService.saveToBuffer(dto);

        log.info("[비동기 DB 저장 완료] 작성자 : {}", sender);
    }

    public List<MessageDTO> getRecentMessages() {
        return chatMapper.selectRecentMessages();
    }

    // 단순조회작업 불필요한 dirtycheck 제외
    @Transactional(readOnly = true)
    public List<MessageDTO> getChatHistory(Long lastId, int limit){
        return chatMapper.getChatHistory(lastId, limit);
    }
}
