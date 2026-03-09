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

    @Async
    public void saveMessageAsync(String sender, String content) {
        chatMapper.insertMessage(sender, content);

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
