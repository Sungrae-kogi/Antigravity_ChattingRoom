# SK Theme Chatting Room 💬
Spring Boot와 WebSocket을 활용한
실시간 익명 웹 채팅 서비스 (저작권 이슈 없음)

## 📌 프로젝트 소개
단일 채팅방에 접속한 여러 사용자가
실시간으로 대화를 나눌 수 있는 웹 메신저입니다.
메모리 기반의 세션 관리와 비동기 DB 저장을 통해
빠른 채팅 전송 속도를 보장합니다.

## 🛠 기술 스택
- **Backend:** Java, Spring Boot, WebSocket, MyBatis
- **Database:** MariaDB
- **Frontend:** JSP, HTML/CSS, Vanilla JS
- **Test & CI:** JUnit5, Mockito, AssertJ,
  Testcontainers, GitHub Actions, Docker

## ✨ 주요 기능 및 아키텍처
- **실시간 다중 통신:** WebSocket 기반 지연 없는 1:N 채팅
- **비동기 DB 저장:** `@Async`를 활용해
  메인 스레드 병목 없이 백그라운드에서 채팅 내역 영구 저장
- **투 트랙(Two-Track) 미디어 전송:**
  무거운 이미지 파일은 HTTP(Multipart)로 업로드하고,
  가벼운 결과 URL만 WebSocket으로 브로드캐스팅하여
  실시간 통신의 병목 현상을 원천 차단하는 아키텍처 적용
- **CI 자동화 파이프라인:** GitHub Actions를 활용하여
  메인 브랜치 Push 시 자동 빌드 및 통합 테스트 수행
- **멱등성 있는 통합 테스트:** Testcontainers를 도입하여
  로컬 DB 환경에 의존하지 않는 독립적인 테스트 환경 구축

## 🚀 로컬 실행 방법

1. **설정 파일 복사 및 세팅**
   `src/main/resources` 경로의 `application.properties.template`
   파일을 복사하여 `application.properties`로 이름을 변경합니다.
   본인의 로컬 MariaDB 계정 정보로 내부 빈칸을 채워주세요.

2. **DB 테이블 생성**
   MariaDB에 접속하여 아래 쿼리로 테이블을 생성합니다.
   ```sql
   CREATE TABLE CHAT_MESSAGE (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       sender VARCHAR(50) NOT NULL,
       content TEXT NOT NULL,
       send_time DATETIME DEFAULT CURRENT_TIMESTAMP
   );

## 🤖 AI 채팅 요약 에이전트 (Gemini API)
채팅방에 늦게 참여하거나 대화 흐름을 놓친 사용자를 위해,
AI가 최근 대화의 문맥을 분석하여 요약해 주는 스마트 봇 기능입니다.

**[작동 원리 및 주요 아키텍처]**
- **명령어 기반 트리거:** 사용자가 채팅창에 `!요약`을 입력하면,
  서버가 DB에서 최근 30분간의 채팅 내역을 조회하여
  Gemini API(Google AI Studio)로 전송합니다.
- **유니캐스트(Unicast) 응답 라우팅:** 진행 중인 대화의 흐름을
  방해하지 않기 위해, 방 전체(Broadcast)가 아닌
  **요청한 사용자에게만** 1:1 시스템 메시지로 결과를 전달합니다.
- **논블로킹(Non-blocking) 비동기 통신:** 외부 AI API의
  응답 대기 시간(3~5초) 동안 채팅 서버의 메인 스레드가
  멈추는 병목 현상을 막기 위해, `WebClient`를 활용한
  비동기 HTTP 통신망을 구축하여 실시간 성능을 방어합니다.