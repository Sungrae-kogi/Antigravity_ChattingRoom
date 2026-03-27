package com.example.chattingroom.mapper;

import com.example.chattingroom.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertUser(UserDto userDto);
    UserDto findByUsername(String username);
}
