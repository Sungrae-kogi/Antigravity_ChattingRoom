package com.example.chattingroom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 커스텀 JSON 변환기 생성
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 시간 타입(LocalDateTIme) 번역 사전 추가
        objectMapper.registerModule(new JavaTimeModule());

        // @class 이름표 분이기 위한 설정
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(), ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Redis에 데이터가 깨지지 않고 들어가도록, Key는 글자로, Value는 JSON으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        return template;
    }
}
