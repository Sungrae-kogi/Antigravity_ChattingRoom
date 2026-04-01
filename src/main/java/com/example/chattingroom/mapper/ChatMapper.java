package com.example.chattingroom.mapper;

import com.example.chattingroom.dto.ChatHistoryQuery;
import com.example.chattingroom.dto.MessageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {
    void insertMessage(
            @Param("sender") String sender,
            @Param("content") String content
    );

    List<MessageDTO> selectRecentMessages();

    List<MessageDTO> getChatHistory(ChatHistoryQuery query);

    void insertMessageBatch(List<MessageDTO> messageList);
}
