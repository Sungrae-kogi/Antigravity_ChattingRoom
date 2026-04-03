package com.example.chattingroom.security;

import com.example.chattingroom.dto.UserDto;
import com.example.chattingroom.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto = userMapper.findByUsername(username);

        if (userDto == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Spring Security의 User 객체를 생성하여 반환합니다.
        // 비밀번호 비교는 Security가 내부적으로 수행합니다.
        return User.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .roles("USER") // 기본 권한 설정
                .build();
    }
}

/*
 * 웹 소켓 통신의 Principal 동작 원리 이해 코드
 * import java.security.Principal; // 자바 표준 보안 인터페이스(명찰)
 * 
 * @MessageMapping("/chat/message")
 * public void receiveMessage(MessageDTO message, Principal principal) {
 * 
 * // 1. "봉투에 적힌 이름은 안 믿어!"
 * // 파이프(소켓)에 Security가 붙여둔 명찰(Principal)에서 진짜 이름을 꺼냄
 * String realSender = principal.getName();
 * 
 * // 2. 만약 조작범이라면 여기서 에러를 내거나, 진짜 이름으로 덮어씌움
 * message.setSender(realSender);
 * 
 * chatService.saveMessage(realSender, message.getContent());
 * }
 * 
 * 
 */