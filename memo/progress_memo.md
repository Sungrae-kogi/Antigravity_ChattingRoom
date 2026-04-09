# 📝 Next.js 마이그레이션 진행 현황 메모

다음에 오셔서 바로 이어서 하실 수 있도록 현재까지의 작업과 남은 작업을 정리해두었습니다.

---

### ✅ 완료된 작업 (Done)

1.  **백엔드 API 서버 개조 (Spring Boot)**
    - `@RestController` 도입: 화면(JSP) 대신 JSON 데이터를 반환하도록 변경.
    - **CORS 설정**: 프론트엔드(localhost:3000)에서 오는 요청을 허용하고, JWT 쿠키를 주고받을 수 있게 설정(`AllowCredentials(true)`).
    - **보안 설정**: 로그인 실패 시 리다이렉트 대신 401 에러를 반환하도록 수정.
    - **WebSocket 보안**: `HandshakeInterceptor`를 통해 웹소켓 연결 시에도 JWT 토큰으로 신원을 확인하도록 보안망 구축.

2.  **프론트엔드 프로젝트 세팅 (Next.js)**
    - `frontend` 폴더 생성 및 Next.js 프로젝트 초기화.
    - **Tailwind CSS** 설치 및 적용.

3.  **로그인 & 회원가입 기능 (React)**
    - `app/login/page.js`: `useState`(상태)와 `useRouter`(이동)를 활용한 로그인 UI 및 백엔드 통신(fetch) 로직 구현.
    - `app/signup/page.js`: 회원가입 UI 및 백엔드 통신 로직 구현.
    - **JWT 쿠키 연동**: 로그인이 성공하면 브라우저가 백엔드로부터 받은 JWT 쿠키를 안전하게 저장하도록 설정 완료.

4.  **문서화**
    - `README.md`를 모던 웹 아키텍처에 맞게 전면 수정.

---

### ⏳ 남은 작업 (To-do)

1.  **대망의 채팅창 마이그레이션 (Next Step!)**
    - `app/chat/page.js` 생성: 기존 `room.jsp`의 UI를 Tailwind CSS로 예쁘게 이식하기.
    - **React 방식의 WebSocket 연결**: 컴포넌트가 화면에 나타날 때(Mount) 소켓을 연결하고, 사라질 때(Unmount) 연결을 끊는 `useEffect` 훅 활용하기.
    - **실시간 메시지 리스트 관리**: 채팅이 올 때마다 `State` 배열을 업데이트하여 화면이 자동으로 변하는 React의 마법 구현하기.

2.  **전체 통합 테스트**
    - 로그인 -> 채팅방 이동 -> 메시지 전송 및 수신 시나리오 전체 점검.
    - 기존의 부가 기능(AI 요약, 이미지 업로드)들이 프론트엔드에서도 잘 동작하는지 확인.

---

### 💡 다음에 개발을 재개할 때!
1. 백엔드(Spring Boot)를 IDE에서 평소처럼 실행합니다.
2. 터미널 하나를 더 열어 `cd frontend` 후 `npm.cmd run dev`를 입력해 프론트엔드를 가동합니다.
3. 브라우저에서 `http://localhost:3000/login`으로 접속하면 끝!
