# SK Theme Chatting Room 💬
Spring Boot 백엔드와 Next.js 프론트엔드가 분리된 모던 웹 아키텍처 기반 실시간 익명 웹 채팅 서비스 (저작권 이슈 없음)

## 📌 프로젝트 소개
단일 채팅방에 접속한 여러 사용자가 실시간으로 대화를 나눌 수 있는 웹 메신저입니다. 
기존 모놀리식(Monolithic) 구조에서 **프론트엔드(Next.js)와 백엔드(Spring Boot API)를 완전히 분리한 MSA 지향적 구조**로 성공적으로 마이그레이션 하였으며, JWT 기반의 Stateless 보안 통신과 Redis 비동기 DB 저장을 통해 극강의 성능과 모던 웹 환경을 제공합니다.

---

## 🛠 기술 스택
- **Backend (API Server):** Java, Spring Boot, WebSocket, MyBatis, Spring Security(JWT)
- **Database / Cache:** MariaDB, Redis
- **Frontend (UI/UX):** React, Next.js, Tailwind CSS
- **Test & CI:** JUnit5, Mockito, AssertJ, Testcontainers, GitHub Actions, Docker

---

## ✨ 주요 기능 및 모던 아키텍처 (업데이트 완료)

### 🎯 1. 프론트엔드 / 백엔드 완전 분리 (Decoupled Architecture) [NEW]
- **Spring Boot API 서버 (포트 8080):** 렌더링 역할을 모두 덜어내고, 오직 JSON 데이터 응답과 비즈니스 로직, 웹소켓 통신만을 담당하는 순수 RESTful API 서버로 진화했습니다. CORS 제어를 통해 프론트엔드 출처(Origin)를 안전하게 허용합니다.
- **Next.js 프론트엔드 서버 (포트 3000):** React와 Tailwind CSS를 기반으로 한 SPA(Single Page Application) 환경을 구축하여, 깜빡임 없는 빠르고 쾌적한 데스크탑 앱 수준의 사용자 경험을 제공합니다.

### 🔐 2. Stateless JWT 보안 및 웹소켓 연동 [NEW]
- 기존 메모리 기반 세션 로그인 방식을 과감히 버리고, **토큰 기반의 JWT(JSON Web Token)** 방식으로 마이그레이션 했습니다.
- 보안이 생명인 토큰은 브라우저의 자바스크립트가 탈취하지 못하도록 `HttpOnly` 쿠키에 담겨 안전하게 서버 간(교차 출처, CORS)에 전달됩니다.
- 채팅을 위해 연결되는 WebSocket 통신 역시 HTTP Handshake 단계에서 JWT 토큰을 낚아채어 Spring Security Context로 안전하게 신원을 확인하는 `HandshakeInterceptor` 커스텀 보안망이 구축되어 있습니다.

### 🚀 3. 대규모 트래픽 대비 Redis Write-Back
- 초당 수천 건의 채팅이 발생할 때 DB 커넥션이 고갈되는 병목을 막기 위해, 인메모리 저장소(Redis)를 완충 지대(Buffer)로 활용했습니다. 
- 채팅 데이터는 0.001초 만에 Redis List에 임시 적재되며, `@Scheduled` 배치 워커가 10초 주기로 데이터를 모아 MariaDB에 다중 삽입(Bulk Insert)하여 RDBMS의 부하를 극적으로 낮췄습니다.

### 🤖 4. AI 채팅 요약 에이전트 (Gemini API)
- 채팅방에 늦게 참여하거나 대화 흐름을 놓친 사용자를 위해, AI가 최근 대화의 문맥을 분석하여 요약해 주는 스마트 봇 기능입니다. `!요약` 명령어로 호출하며, 비동기 `WebClient`를 통해 서버 병목을 막고 요청자에게만 결과를 반환(Unicast)합니다.

### 🖼️ 5. 투 트랙(Two-Track) 미디어 전송 아키텍처
- 무거운 이미지 파일은 별도의 안전한 HTTP 채널(Multipart)로 업로드하고, 가벼운 결과 URL만 WebSocket으로 브로드캐스팅하여 실시간 통신의 속도 저하를 원천 차단했습니다.

### 📜 6. UX 중심의 No-Offset 무한 스크롤 방어
- 마지막 읽은 ID(lastId)를 기준점으로 삼는 No-Offset 쿼리로 페이징 성능을 극대화했으며, React 프론트엔드에서 데이터 로드 시 화면 널뛰기 현상을 방어하기 위한 스크롤 컴포넌트 관리가 이루어집니다.

---

## 🚀 로컬 실행 방법

### 1단계: Backend (Spring Boot) 서버 가동
1. **설정 세팅:** `src/main/resources/application.properties.template`을 복사하여 `application.properties`로 이름을 변경하고, 본인의 로컬 MariaDB, Redis, Gemini API 키를 입력합니다.
2. **DB 세팅:** 아래 쿼리로 테이블을 생성합니다.
   ```sql
   CREATE TABLE CHAT_MESSAGE (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       sender VARCHAR(50) NOT NULL,
       content TEXT NOT NULL,
       send_time DATETIME DEFAULT CURRENT_TIMESTAMP
   );
   ```
3. IDE에서 Spring Boot 애플리케이션을 실행합니다. (**http://localhost:8080** 구동 완료)

### 2단계: Frontend (Next.js) 서버 가동
1. 터미널을 열고 `frontend/` 디렉토리로 이동합니다.
   ```bash
   cd frontend
   ```
2. 필요 패키지를 설치하고 개발 서버를 가동합니다.
   ```bash
   npm install
   npm run dev
   ```
3. 브라우저를 열고 **http://localhost:3000** 에 접속하여 서비스를 이용합니다!

---

## 💡 주요 트러블슈팅 (Trouble Shooting)

**1. 프론트/백엔드 분리로 인한 무단 접근 CORS 차단 이슈 [NEW]**
- **원인:** 포트 3000번(Next.js)에서 8080번(Spring)으로 요청을 보낼 때, 특히 인증 정보인 쿠키(JWT)를 포함하여 요청하면 브라우저 보안 정책에 의해 통신이 거부되는 현상 발생.
- **해결:** Spring의 `SecurityConfig`에서 `CorsConfigurationSource`를 추가하여, 명확하게 `http://localhost:3000` 도메인의 자격 증명(Credentials)을 허용하도록 백엔드 출입증을 발급하여 해결했습니다.

**2. WebSocket Handshake 시 인증 우회(Anonymous) 문제 [NEW]**
- **원인:** JWT를 쿠키로 교환하게 되면서, 기존처럼 서버 세션에서 바로 회원명을 꺼내올 수 없음. 웹소켓 최초 연결 시 익명 사용자로 접속되는 문제 발생.
- **해결:** `HandshakeInterceptor`를 커스터마이징하여, 소켓 연결 직전에 `SecurityContextHolder`에 보관된 JWT 파싱된 신분증(Authentication)을 가로채고 추출하여 소켓 세션 속성에 안전하게 맵핑했습니다.

**3. Redis 버퍼 적재 시 Java 8 LocalDateTime 직렬화 에러**
- **원인:** Spring Data Redis의 기본 Jackson 변환기가 LocalDateTime 타입을 JSON으로 파싱하지 못해 `InvalidDefinitionException` 발생.
- **해결:** 커스텀 ObjectMapper를 만들고 `JavaTimeModule` 등록 및 `@class` 메타데이터 처리를 추가하여 파이프라인 복구.

**4. WebClient와 Gemini API 연동 시 404 인코딩 이슈**
- **원인:** WebClient가 요청 URL의 콜론(`:`)을 `%3A`로 자동 인코딩하여 구글 서버가 엔드포인트를 찾지 못하는 현상 발생.
- **해결:** `UriBuilder`의 `uri(url + "?key={key}", apiKey)` 치환 방식을 적용해 과도한 자동 인코딩을 제어했습니다.
