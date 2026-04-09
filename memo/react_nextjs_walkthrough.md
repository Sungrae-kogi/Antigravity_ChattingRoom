# 🗺️ SK 사내 익명 톡 — React & Next.js 흐름 이해 가이드

> 우리가 만든 프로젝트를 보면서 React와 Next.js가 **왜 이런 구조**를 갖는지 이해하기 위한 안내서입니다.

---

## 1. 전체 구조 — 프론트엔드 / 백엔드 분리

```
브라우저
  │  HTTP / WebSocket
  ▼
[ 프론트엔드 ] localhost:3000   ← Next.js (React)
  │  HTTP / WebSocket
  ▼
[ 백엔드 ]    localhost:8080   ← Spring Boot (Java)
  │
  ▼
[ DB ]                         ← MySQL
```

**예전 방식 (JSP):**
Spring Boot가 화면(JSP)까지 만들어서 브라우저에게 완성된 HTML을 줬습니다.

**지금 방식 (Next.js + REST API):**
- Spring Boot는 **데이터(JSON)만** 줍니다.
- Next.js가 데이터를 받아서 **화면을 그립니다.**
- 역할이 명확하게 분리됩니다.

---

## 2. Next.js — 파일 위치가 곧 URL 주소

```
frontend/app/
├── layout.js          ← 모든 페이지의 공통 껍데기 (<html>, <body>)
├── page.js            ← localhost:3000/
├── login/
│   └── page.js        ← localhost:3000/login
├── signup/
│   └── page.js        ← localhost:3000/signup
└── chat/
    └── page.js        ← localhost:3000/chat
```

> **핵심:** `page.js`라는 이름은 Next.js의 약속(예약어)입니다.
> 이 이름이어야만 해당 폴더 경로가 URL로 등록됩니다.
>
> `layout.js`는 모든 `page.js`를 `{children}` 자리에 끼워 넣는 공통 틀입니다.

---

## 3. React — 컴포넌트가 뭔가요?

React에서 **화면의 모든 것은 함수**로 만들어집니다. 이 함수를 **컴포넌트**라고 불러요.

```js
// login/page.js
export default function LoginPage() {
    // ... 로직 ...

    return (
        <div>           // ← 이게 화면에 그려질 HTML (JSX라고 부름)
            <h1>SK 사내 익명 톡</h1>
            <input ... />
            <button>로그인</button>
        </div>
    );
}
```

| 구분 | 예전 JSP 방식 | React 방식 |
|---|---|---|
| 화면 단위 | `.jsp` 파일 | 컴포넌트 함수 |
| 데이터 표시 | `${변수}` | `{변수}` |
| 자바스크립트 | 별도 `.js` 파일 | 함수 안에 같이 작성 |

---

## 4. JSX — JavaScript 안의 HTML처럼 생긴 문법

`return()` 안에 있는 `<div>`, `<h1>` 같은 게 **JSX**입니다.
HTML과 거의 같은데, 딱 두 가지가 다릅니다:

```jsx
// HTML 에서는:      class="..."    onclick="..."
// JSX 에서는:   className="..."   onClick={함수}

<button
  className="w-full bg-blue-600 ..."   // class 대신 className (class는 JS 예약어)
  onClick={handleLogin}                 // onclick 대신 onClick (카멜케이스)
>
  로그인
</button>
```

중괄호 `{}`는 **"여기서부터 JavaScript야"** 라는 신호입니다:

```jsx
<h1>안녕하세요, {username} 님</h1>
//               ↑ JS 변수값이 여기 들어감
```

---

## 5. useState — 화면과 연결된 변수 ⭐ 가장 중요!

```js
const [username, setUsername] = useState("");
//     ↑ 현재 값    ↑ 값을 바꾸는 함수       ↑ 초기값
```

**왜 그냥 변수를 안 쓰나요?**

```js
// ❌ 일반 변수 — 값이 바뀌어도 화면이 안 바뀜
let username = "";
username = "hong";  // 타이핑해도 입력창에 반영 안 됨

// ✅ useState — 값이 바뀌면 화면이 자동으로 다시 그려짐
const [username, setUsername] = useState("");
setUsername("hong");  // → 화면의 입력창도 즉시 바뀜 (자동)
```

**로그인 페이지 실제 사용 예:**

```jsx
<input
  value={username}                              // input의 현재 값 = State
  onChange={(e) => setUsername(e.target.value)} // 타이핑 → setUsername() → 화면 반영
/>
```

사용자가 한 글자 칠 때마다 → `onChange` 실행 → `setUsername()` 호출 → `username` 업데이트 → 입력창 갱신, 이 과정이 자동으로 일어납니다.

---

## 6. 전체 앱 흐름 따라가기

### 🔵 흐름 1: 회원가입 (`signup/page.js`)

```
사용자가 아이디/비밀번호 타이핑
    → onChange마다 useState 업데이트
    ↓
"가입완료" 버튼 클릭 → handleSignup() 실행
    ↓
fetch("http://localhost:8080/api/auth/signup", { method: "POST", body: JSON })
    ↓
백엔드가 DB에 유저 저장 후 { status: "success" } 반환
    ↓
router.push("/login") → 로그인 페이지로 이동
```

---

### 🔵 흐름 2: 로그인 (`login/page.js`)

```
사용자가 아이디/비밀번호 타이핑
    → onChange마다 useState 업데이트
    ↓
"로그인" 버튼 클릭 → handleLogin() 실행
    ↓
fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    body: JSON,
    credentials: "include"   ← ★ 이게 없으면 JWT 쿠키를 못 받음
})
    ↓
백엔드가 JWT 토큰을 쿠키에 구워서 응답
    ↓ 브라우저가 JWT 쿠키를 자동으로 저장
router.push("/chat") → 채팅방으로 이동
```

> **JWT 쿠키란?** 로그인 도장 같은 거예요.
> 이후 모든 API 요청에 자동으로 첨부됩니다.
> `credentials: "include"` 옵션이 없으면 브라우저가 쿠키를 주고받지 않습니다.

---

### 🔵 흐름 3: 채팅방 진입 — useEffect 첫 번째

채팅방에 들어오면 가장 먼저 **"나는 누구인가?"** 를 확인합니다:

```js
// 컴포넌트가 화면에 나타날 때(Mount) 딱 한 번만 실행
useEffect(() => {
    fetch("http://localhost:8080/api/auth/me", {
        credentials: "include",  // 저장된 JWT 쿠키를 함께 전송
    })
    .then(res => {
        if (!res.ok) router.push("/login");  // 쿠키 없으면 → 로그인으로 추방
        return res.json();
    })
    .then(data => setMyUsername(data.username));  // 내 이름을 State에 저장

}, []);  // [] = 처음 딱 한 번만 실행
```

**useEffect란?**
"컴포넌트가 화면에 나타났을 때 이걸 실행해줘" 라는 명령입니다.
예전 JS의 `DOMContentLoaded` 이벤트와 같은 역할이에요.

---

### 🔵 흐름 4: WebSocket 연결 — useEffect 두 번째

```js
// myUsername이 정해진 직후 WebSocket 연결
useEffect(() => {
    if (!myUsername) return;  // 아직 이름 없으면 대기

    const ws = new WebSocket("ws://localhost:8080/chat");
    wsRef.current = ws;  // 소켓 객체를 useRef에 보관

    ws.onmessage = (event) => {
        const msgObj = JSON.parse(event.data);
        setMessages(prev => [...prev, msgObj]);  // 메시지 추가 → 화면 자동 갱신
    };

    return () => {
        ws.close();  // 채팅방 떠날 때 소켓 자동 종료 (클린업)
    };

}, [myUsername]);  // myUsername이 바뀔 때 실행
```

| 구분 | 예전 JS (chat.js) | React (chat/page.js) |
|---|---|---|
| 시작 시점 | `DOMContentLoaded` 이벤트 | `useEffect(() => {}, [])` |
| 종료 시점 | 없음 (메모리 누수!) | `return () => ws.close()` |
| 메시지 수신 | `box.appendChild(bubble)` DOM 직접 조작 | `setMessages(...)` State 업데이트 |

---

### 🔵 흐름 5: 메시지 수신 → 화면 자동 갱신

```
WebSocket에서 메시지 수신
    ↓
setMessages(prev => [...prev, 새메시지])
//           ↑ 기존 배열    ↑ 새 메시지 추가
    ↓
React가 messages State 변화를 감지
    ↓
화면의 {messages.map(...)} 부분을 자동으로 다시 실행
    ↓
새 메시지 말풍선이 화면에 나타남 ✅
```

```jsx
// 메시지 배열을 화면에 그리는 코드
{messages.map((msg, index) =>
    renderMessageBubble(msg, index)  // 각 메시지 → JSX 말풍선으로 변환
)}
```

> `map()`은 배열의 각 원소를 JSX로 변환해주는 JS 기본 함수입니다.
> `messages`에 새 항목이 추가될 때마다 이 `map()`이 다시 실행됩니다.

---

## 7. useRef — 화면 갱신 없는 변수

```js
const wsRef = useRef(null);       // WebSocket 객체 보관 (예전: let ws;)
const chatBoxRef = useRef(null);  // DOM 요소 직접 접근 (예전: getElementById)
```

**왜 useState를 안 쓰나요?**

```
useState로 WebSocket 저장하면:
  setWs(ws) → State 변경 → 화면 전체 리렌더링 → useEffect 또 실행 → 무한루프!

useRef로 저장하면:
  wsRef.current = ws → 그냥 값만 저장, 화면 리렌더링 없음 ✅
```

값이 바뀌어도 화면을 다시 그릴 필요가 없는 것들은 모두 `useRef`로 관리합니다.

---

## 8. React의 핵심 3총사 요약

| Hook | 역할 | 언제 씀? |
|---|---|---|
| `useState` | 화면과 연결된 변수 | 입력값, 메시지 목록, 유저명 등 화면에 표시되는 값 |
| `useEffect` | 생명주기 관리 | 마운트 시 실행할 것 (API 호출, WebSocket 연결 등) |
| `useRef` | 화면 갱신 없는 변수 보관 | WebSocket 객체, DOM 직접 접근, 타이머 등 |

---

## ✅ 전체 흐름 한눈에 보기

```
localhost:3000/signup
    → useState로 입력값 관리
    → 가입완료 버튼 → fetch → 백엔드 DB 저장
    → router.push("/login")
         │
         ▼
localhost:3000/login
    → useState로 입력값 관리
    → 로그인 버튼 → fetch (credentials: "include")
    → 백엔드가 JWT 쿠키 발급 → 브라우저 자동 저장
    → router.push("/chat")
         │
         ▼
localhost:3000/chat
    → [Effect 1] /api/auth/me 호출 → 내 이름 확인 → setMyUsername()
    → [Effect 2] myUsername 확정 후 WebSocket 연결 → wsRef에 저장
    → 메시지 수신 → setMessages() → map()으로 화면 자동 갱신
    → 로그아웃 → ws.close() (클린업) → router.push("/login")
```
