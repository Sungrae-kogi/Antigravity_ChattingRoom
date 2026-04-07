JSP에서 Next.js(React)로의 마이그레이션 적용 계획
JSP(서버 렌더링) 방식에서 Next.js(클라이언트/서버 분리) 방식으로 넘어가기 위해 필요한 아키텍처 변경 및 작업 계획입니다. 한 번도 경험해보지 않으셨더라도 차근차근 따라오시면 배울 수 있도록 구성했습니다.

핵심 개념 변경점 (개요)
현재 (JSP): Spring Boot 서버가 포트 8080에서 HTML 화면(JSP)도 그려서 브라우저에 보내주고, 데이터 통신(WebSocket)도 한꺼번에 처리합니다.
변경 후 (Next.js):
프론트엔드 서버(Next.js): 포트 3000 번 등에서 실행되며, 오직 예쁜 화면(React 컴포넌트)을 그리고 라우팅 처리를 담당합니다.
백엔드 서버(Spring Boot): 포트 8080 번에서 실행되며, 화면은 전혀 신경 쓰지 않고 오로지 데이터(JSON) 응답과 WebSocket 통신만 담당하는 순수 API 서버가 됩니다.
WARNING

두 서버의 포트가 다르기 때문에, 프론트엔드에서 백엔드로 바로 통신을 시도하면 CORS (Cross-Origin Resource Sharing) 에러라는 보안 정책에 막히게 됩니다. 이를 해결하는 작업이 필수적입니다.

1. 프론트엔드(Next.js) 환경 구축
Node.js 가 설치되어 있는지 확인합니다.
프로젝트 최상단 폴더(SKThemeChattingRoom 혹은 별도 폴더)에서 npx create-next-app@latest frontend 명령어로 Next.js 프로젝트를 초기화합니다.
초기화 시 Tailwind CSS 사용 여부를 확인 후 선택합니다. (기존 바닐라 CSS를 살릴지 결정 필요)
2. 백엔드(Spring Boot) API 서버로 개조
화면을 반환하던 기능(return "room", return "redirect:/" 등)을 데이터를 반환하는 REST API 방식으로 수정해야 합니다.

[MODIFY] SecurityConfig.java
CorsConfigurationSource Bean을 추가하여 http://localhost:3000 (Next.js 주소)로부터의 요청을 허용합니다 (setAllowCredentials(true) 필수 - JWT 토큰이 담긴 쿠키를 주고받기 위해).
[MODIFY] WebSocketConfig.java
엔드포인트 설정에서 .setAllowedOrigins("http://localhost:3000")을 명시하여 Next.js 서버에서 오는 소켓 연결을 허용합니다.
[MODIFY] AuthController.java 및 기타 @Controller들
현재 @Controller로 되어 화면을 반환하는 컨트롤러들을 @RestController 스타일 (또는 @ResponseBody)로 부분 전환합니다.
로그인 성공 시 redirect:/room 으로 이동하는 것은 프론트엔드 역할이므로, 상태 메시지만 "SUCCESS" 와 같이 JSON으로 반환하고 처리는 프론트엔드의 React 코드(useRouter 등)에 맡기게 수정합니다.
3. 화면(React 컴포넌트) 마이그레이션
JSP와 JS 코드를 합쳐 React 컴포넌트로 만듭니다.

3-1. 로그인 / 회원가입 페이지
Next.js 의 app/login/page.jsx, app/signup/page.jsx 페이지 생성.
React의 useState 훅을 사용해 아이디, 비밀번호 입력값을 관리.
fetch 함수 등으로 백엔드 API (/api/auth/login) 통신. 중요한 것은 { credentials: 'include' } 옵션을 포함해 통신해야만 백엔드가 구워준 JWT 쿠키를 브라우저가 저장할 수 있습니다.
3-2. 채팅 페이지
Next.js 의 app/chat/page.jsx 생성.
기존 바닐라 자바스크립트의 ws = new WebSocket(...) 코드는 React의 useEffect, useRef 훅을 사용해 컴포넌트 생명주기에 맞춰 안전하게 연결/해제되도록 작업합니다.
기존에 문자열로 태그를 이어 붙여 그리던 방식(HTML String) 대신, React의 state에 메시지 배열을 저장하고 JSX로 깔끔하게 반복(map) 렌더링시킵니다.
User Review Required (체크 및 결정 사항)
이 계획을 승인해 주시면 1번 단계(프로젝트 생성)부터 진행하겠습니다. 진행하기 전 아래 사항에 대해 확인 부탁드립니다!

Node.js 설치 확인: 로컬 컴퓨터에 Node.js가 설치되어 있으신가요? 안되어 있다면 설치부터 하셔야 합니다. (터미널에서 node -v 쳤을 때 버전이 나오면 됩니다)
프로젝트 구조: SKThemeChattingRoom 폴더 내부에 frontend 라는 이름의 새 폴더를 만들고 거기에 Next.js 를 세팅하는 구조로 가도 될까요?
디자인(CSS): 기존 일반 CSS를 가져와서 사용할까요, 아니면 React 진영에서 많이 쓰이는 Tailwind CSS라는 방식을 이번 기회에 적용해볼까요?