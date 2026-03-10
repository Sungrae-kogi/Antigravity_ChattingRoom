package com.example.chattingroom.config;

import com.example.chattingroom.dto.MessageDTO;
import com.example.chattingroom.mapper.ChatMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatBatchScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMapper chatMapper;

    private static final String BUFFER_KEY = "chat:buffer";

    // 20초 마다 실행
    @Scheduled(fixedDelay = 20000)
    @Transactional
    public void processChatBuffer(){
        // 버퍼의 채팅 수를 확인
        Long size = redisTemplate.opsForList().size(BUFFER_KEY);

        // 비어있다면, 할일이없으니 종료.
        if(size == null || size == 0)
            return;

        List<MessageDTO> batchList = new ArrayList<>();

        // 쌓인 메시지를 Redis에서 Pop 해와서 리스트에 담음.
        for(int i=0; i<size; i++){
            Object obj = redisTemplate.opsForList().leftPop(BUFFER_KEY);
            if(obj instanceof MessageDTO){
                batchList.add((MessageDTO) obj);
            }
        }

        // 모아둔 채팅이 있다면 DB에 한번에 저장
        if(!batchList.isEmpty()) {
            chatMapper.insertMessageBatch(batchList);
            log.info("[Batch] {} 개의 채팅을 DB에 일괄 저장했습니다.", batchList.size());
        }
    }

    // 갑작스러운 서버 다운 방어 로직
    @PreDestroy
    public void onShutdown(){
        log.warn("[서버 종료 감지] Redis 잔여 채팅 백업을 시작합니다.");
        processChatBuffer();

        log.warn("[백업 완료] 안전하게 서버를 종료합니다.");
    }

}
