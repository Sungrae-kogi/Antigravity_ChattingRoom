package com.example.chattingroom.service;

import com.example.chattingroom.dto.UserDto;
import com.example.chattingroom.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean register(String username, String password) {
        // Check if username exists
        if (userMapper.findByUsername(username) != null) {
            return false;
        }
        
        // Hash password and save
        String encodedPassword = passwordEncoder.encode(password);
        UserDto newUser = UserDto.builder()
                .username(username)
                .password(encodedPassword)
                .build();
                
        userMapper.insertUser(newUser);
        return true;
    }

    public UserDto login(String username, String password) {
        UserDto user = userMapper.findByUsername(username);
        
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null; // Invalid credentials
    }
}
