# 2026-04-03 Spring Security 도입 진행 상황 (세션 기반)

## 📌 현재까지 완료된 작업 (Step 1 ~ Step 3)

1. **Spring Security 기반 환경 설정 완료**
   - `pom.xml`에 Spring Security 의존성 추가
   - 기존의 수동 인증 인터셉터(`AuthInterceptor`) 철거 완료

2. **SecurityConfig 방화벽 구축**
   - `/signin`, `/signup` 등은 모든 사용자 허용
   - `/room`, `/api/chat/**` 등은 인증된 사용자만 허용
   - 폼 로그인(Form Login) 및 자동 로그아웃 처리 위임 설정 적용 (CSRF 임시 비활성화)

3. **UserDetailsService 연동 및 로그인/회원가입 리팩터링**
   - DB 통신용 `CustomUserDetailsService` 추가 및 `User` 객체 매핑
   - 기존 `AuthController`, `UserService`에 있던 수동 세션 검증/생성/삭제(`login()`, `logout()`) 코드 전면 제거 (Security가 알아서 처리하도록 위임)
   - 보안 강화를 위해 회원가입 시 `@RequestParam`을 사용하던 방식을 개선하여 `SignupRequest` DTO 클래스로 캡슐화 처리 완료 (로그 누출 방지)

---

## 🚀 앞으로 진행할 작업 (Step 4 ~)

1. **Step 4: WebSocket (채팅) 보안 연동 핵심 작업**
   - 웹소켓은 최초 연결(Handshake) 시 HTTP로 허락을 구한 뒤 소켓으로 업그레이드됨.
   - 이때 기존에 남아있는 HTTP 로그인 세션에서 "유저 신분증(Authentication/Principal)"을 꺼냄.
   - 꺼낸 신분증을 새로 만들어질 웹소켓 파이프(WebSocket Session)에 영구적으로 "명찰"로 달아두는 인터셉터/Security 설정 추가.

2. **채팅 컨트롤러 리팩터링 및 조작 방어**
   - `ChatController` 등의 `@MessageMapping` 메서드 파라미터에 `Principal principal` 주입.
   - 프론트엔드가 보내온 `MessageDTO`의 `sender` 이름표를 맹신하지 않고, **명찰(`principal.getName()`)에서 꺼낸 진짜 사용자 이름으로 바꿔치기(강제 덮어쓰기)**하여 해킹 방지 및 신뢰도 확보.

3. **최종 테스트 및 UI 점검**
   - 현재 프론트엔드 코드(JSP, JS) 수정 사항 여부 점검 (에러 처리, 로그인 화면 뷰 등)
   - 다중 접속 환경에서 정상적으로 실시간 브로드캐스트가 되는지 검증.
