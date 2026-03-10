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
- **대규모 트래픽 대비 Redis Write-Back 아키텍처**: 초당 수천 건의 채팅이 발생할 때 DB 커넥션이 고갈되는 병목을 막기 위해,
  인메모리 저장소(Redis)를 완충 지대(Buffer)로 활용했습니다.
  채팅 데이터는 0.001초 만에 Redis List에 임시 적재되며,
  @Scheduled 배치 워커가 10초 주기로 데이터를 모아 MariaDB에
  다중 삽입(Bulk Insert)하여 RDBMS의 부하를 극적으로 낮췄습니다.
- **데이터 유실 제로, 우아한 종료(Graceful Shutdown)**:
  스프링 컨테이너의 생명주기를 제어하는 @PreDestroy 센서를 부착하여,
  서버 강제 종료나 배포 시 스케줄러 주기를 기다리지 않고
  Redis의 잔여 데이터를 즉시 DB로 긁어모아 이관하는 방어망을 구축했습니다.
- **UX 중심의 No-Offset 무한 스크롤 & DOM 최적화**:
  과거 대화 내역을 불러올 때 느린 OFFSET 방식 대신
  마지막 읽은 ID(lastId)를 기준점으로 삼는 No-Offset 쿼리로 페이징 성능을 극대화했습니다.
  프론트엔드에서는 IntersectionObserver와 HTML5 <template> 태그를 도입하여
  렌더링을 최적화하고, 과거 데이터 로드 시 발생하는
  화면 널뛰기(Scroll Jump)를 스크롤 높이 보정 로직으로 완벽하게 방어했습니다.
- **OCP를 준수한 업로드 인터페이스 분리 (SoC)**:
  컨트롤러에 얽혀있던 로컬 디스크 파일 저장 로직을
  ImageUploader 인터페이스와 구현체로 완벽히 분리(DI)했습니다.
  이를 통해 향후 AWS S3 등 외부 클라우드 스토리지로 변경 시,
  비즈니스 로직의 코드 수정 없이 부품만 갈아 끼울 수 있는 유연한 설계를 완성했습니다.

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

**3. Redis 버퍼 적재 시 Java 8 LocalDateTime 직렬화(Serialization) 에러**

- **원인**: Redis에 DTO 객체를 JSON으로 직렬화하여 저장할 때,
  Spring Data Redis의 기본 Jackson 변환기(ObjectMapper)가
  Java 8의 새로운 시간 타입(LocalDateTime)을 파싱하지 못해
  InvalidDefinitionException이 발생하며 서버 동작이 중단됨.
- **해결**: RedisConfig에서 커스텀 ObjectMapper를 생성하고,
  JavaTimeModule을 명시적으로 등록하여 시간 변환 번역 사전을 추가했습니다.
  또한 다형성 처리를 위해 @class 메타데이터가 JSON에 포함되도록
  activateDefaultTyping 설정을 추가하여 안전한 직렬화 파이프라인을 복구했습니다.

**4. 무한 스크롤 페이징 시 DTO Type Mismatch 에러 방어**

- **원인**: 프론트엔드에서 다음 페이지 기준점(lastId)을 추출할 때,
  백엔드 DTO에 식별자(id) 필드가 누락되어 JavaScript가 undefined를 반환.
  이 값이 Spring 컨트롤러의 Long 타입으로 바인딩을 시도하다
  NumberFormatException (500 에러) 발생.
- **해결**: 프론트엔드에 undefined 타입 가드(Type Guard)를 추가하여
  잘못된 API 호출을 원천 차단하고, 백엔드 MessageDTO에
  PK 값을 매핑하여 데이터 규격을 완벽하게 동기화했습니다.