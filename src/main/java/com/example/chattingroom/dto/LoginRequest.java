package com.example.chattingroom.dto;

import lombok.Data;

/**
 * 로그인 요청 시 사용되는 DTO 클래스입니다.
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
