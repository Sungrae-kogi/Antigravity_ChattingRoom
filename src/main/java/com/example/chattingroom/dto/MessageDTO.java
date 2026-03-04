package com.example.chattingroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private String sender;
    private String content;

    private LocalDateTime sendTime = LocalDateTime.now();
    // builder패턴이나 정적 static method로 변경??
}
