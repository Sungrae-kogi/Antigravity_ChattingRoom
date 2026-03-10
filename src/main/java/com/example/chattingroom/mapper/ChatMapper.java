package com.example.chattingroom.mapper;

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

    // XML의 id ="getChatHistory" 매핑
    // 파라미터가 2개 이상이므로 @Param으로 이름표를 달아줌.
    List<MessageDTO> getChatHistory(@Param("lastId") Long lastId, @Param("limit") int limit);

    void insertMessageBatch(List<MessageDTO> messageList);
}
