package com.example.chattingroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;

    private String sender;
    private String content;

    @Builder.Default
    private LocalDateTime sendTime = LocalDateTime.now();
    // builder패턴이나 정적 static method로 변경??
}
