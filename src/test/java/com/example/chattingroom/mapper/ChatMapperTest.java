package com.example.chattingroom.mapper;

import com.example.chattingroom.BaseIntegrationTest;
import com.example.chattingroom.dto.MessageDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
    SpringBootTest는 통합 테스트 환경 -> SpringBoot와 DB를 통째료 실행하므로 테스트에 시간이 소요.

    단위 테스트는 ChatService 라는 자바 파일 하나만 떼어내 빠른 시간에 검증.
    ChatService의 검증에는 @AutoWired로 주입받는 ChatMapper가 반드시 필요한데 이건 DB와 연관되어있다.
    이것을 Mockito 라이브러리가 가짜(Mock) 객체를 만들어서 연기를 시키게 함.


    단위 테스트 : 1개의 클래스나 메서드 (주로 Service 로직) -> Mock 객체 등 사용
    통합 테스트 : 여러 컴포넌트의 연동 (Mapper/Repository/Controller 등) DB쿼리나 네트워크 통신같이 외부 시스템과 직접 맞닿아 있는 곳은
        진짜 환경(또는 Testcontainers 같은 완벽한 가짜 환경)을 띄우는 통합 테스트가 필요.

 */
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatMapperTest extends BaseIntegrationTest {

    @Autowired
    private ChatMapper chatMapper;

    @Test
    @DisplayName("DB에 채팅을 넣고 조회하면 정상적으로 나와야 한다.")
    void insertAndSelectTest(){

        // Given
        String testSender = "테스트봇";
        String testContent = "도커에서 실행 중입니다.";

        // When
        chatMapper.insertMessage(testSender,testContent);

        List<MessageDTO> recentList = chatMapper.selectRecentMessages();

        // Then
        // 리스트가 비어있지 않아야 함을 보장해야한다.
        assertThat(recentList).isNotEmpty();

        // 작성한 쿼리는 시간순(ASC)이므로 방금 넣은 최신 채팅은 리스트의 맨 마지막에 존재해야만 합니다.
        MessageDTO lastMsg = recentList.get(recentList.size()-1);

        // lastMsg의 작성자와, 내용이 마지막으로 전송한 채팅과 일치해야만 합니다.
        assertThat(lastMsg.getSender()).isEqualTo(testSender);
        assertThat(lastMsg.getContent()).isEqualTo(testContent);

    }
}
