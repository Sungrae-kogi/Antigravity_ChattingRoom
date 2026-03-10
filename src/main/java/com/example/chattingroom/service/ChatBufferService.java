package com.example.chattingroom.service;

import com.example.chattingroom.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBufferService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 메인 대기열 바구니
    private static final String BUFFER_KEY = "chat:buffer";

    // 채팅을 치면 해당 함수가 호출
    public void saveToBuffer(MessageDTO message){
        // Redis의 List 구조
        redisTemplate.opsForList().rightPush(BUFFER_KEY, message);

        log.info("Redis 임시 보관 완료 : "+ message.getContent());
    }
}
