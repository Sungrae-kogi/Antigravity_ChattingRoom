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
  서버가 DB에서 최근 50건의(채팅범위 변경가능) 채팅 내역을 조회하여
  Gemini API(Google AI Studio)로 전송합니다.
- **유니캐스트(Unicast) 응답 라우팅:** 진행 중인 대화의 흐름을
  방해하지 않기 위해, 방 전체(Broadcast)가 아닌
  **요청한 사용자에게만** 1:1 시스템 메시지로 결과를 전달합니다.
- **논블로킹(Non-blocking) 비동기 통신:** 외부 AI API의
  응답 대기 시간(3~5초) 동안 채팅 서버의 메인 스레드가
  멈추는 병목 현상을 막기 위해, `WebClient`를 활용한
  비동기 HTTP 통신망을 구축하여 실시간 성능을 방어합니다.
## 🖼 투 트랙(Two-Track) 이미지 업로드 및 렌더링
무거운 미디어 파일을 실시간 채팅망에 직접 태우지 않고,
업로드 채널(HTTP)과 메시징 채널(WebSocket)을 완벽히 분리한
실무형 아키텍처입니다.

**[작동 원리 및 최적화]**
- **경량화 브로드캐스팅:** 파일은 서버의 외부 스토리지에
  안전하게 저장(`file:uploads/`)하고, 브라우저가 사진을
  그려낼 수 있는 가벼운 URL 경로만 채팅방에 브로드캐스팅합니다.
- **정적 리소스 보안 및 캐싱:** `WebMvcConfigurer`를 통해
  특정 경로(`/uploads/**`)만 외부 접근을 제한적으로 허용합니다.
  또한 `Cache-Control` 헤더에 1년의 캐시(`max-age`)를 부여하여,
  새로고침 시 발생하는 불필요한 서버 데이터 요청 트래픽을
  원천 차단했습니다.



## 🛡 글로벌 예외 처리 아키텍처
서버의 안정성을 방어하고 프론트엔드와의 협업 효율을 높이기 위해
`@RestControllerAdvice` 기반의 전역 예외 처리망을 구축했습니다.

**[핵심 방어선 구축]**
- **예외 깔때기(Funnel) 구조:** `MaxUploadSizeExceededException`(용량 초과),
  `WebClientResponseException`(AI 통신 장애) 등
  발생 빈도가 높은 예외부터 최상위 `Exception`까지
  계층적으로 에러를 낚아채어 서버 다운을 완벽히 방지합니다.
- **표준 에러 규격(ErrorResponse DTO):** 에러 발생 시 단순 텍스트가 아닌 `{ status, error, message }`
  형태의 일관된 JSON 규격으로 포장하여 프론트엔드에 반환합니다.
- **DRY 원칙 적용:** 정적 팩토리 메서드를 도입하여
  에러 객체 생성 시 발생하는 중복 코드를 제거하고
  유지보수성을 높였습니다.

## 💡 주요 트러블슈팅 (Trouble Shooting)

**1. WebClient와 Gemini API 연동 시 404 인코딩 이슈**
- **원인:** WebClient가 요청 URL의 콜론(`:`)을 `%3A`로
  자동 인코딩하여 구글 서버가 엔드포인트를 찾지 못하는 현상 발생.
- **해결:** `UriBuilder`의 `uri(url + "?key={key}", apiKey)`
  치환 방식을 적용하여 스프링의 과잉 인코딩을 제어하고
  안전한 통신 구조를 확립했습니다.

**2. GitHub Actions CI 환경 ApplicationContext 로드 실패**
- **원인:** CI 환경에 실제 외부 API Key(`application.properties`)가
  존재하지 않아 스프링 빈(Bean) 의존성 주입 단계에서 서버 기동 실패.
- **해결:** `src/test/resources`에 테스트 전용 설정 파일을 분리하여
  CI 환경을 완벽히 고립(Isolation)시키고 빌드 파이프라인을
  성공적으로 복구했습니다.
