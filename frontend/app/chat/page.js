// ════════════════════════════════════════════════════════════
// 📌 [React 개념 1: "use client" 선언]
//
// Next.js는 기본적으로 서버에서 HTML을 미리 만들어서 보내줍니다 (서버 컴포넌트).
// 하지만 우리는 useState, useEffect 같은 "브라우저에서만 돌아가는" 기능을 쓸 거라서
// "이 파일은 브라우저(클라이언트)에서 실행해!" 라고 Next.js에게 알려줘야 합니다.
// 이 선언이 없으면 Next.js가 서버에서 실행하려다가 오류를 냅니다.
// ════════════════════════════════════════════════════════════
"use client";

// ════════════════════════════════════════════════════════════
// 📌 [React 개념 2: import — 도구 가져오기]
//
// Java의 import와 완전히 같은 개념입니다.
// "react" 라는 라이브러리에서 useState, useEffect, useRef 세 가지 도구를 가져옵니다.
// useRef는 채팅 히스토리 스크롤 제어와 WebSocket 객체 보관에 사용합니다.
// useRouter는 Next.js의 화면 이동 도구 (Java의 redirect 같은 것).
// ════════════════════════════════════════════════════════════
import { useState, useEffect, useRef } from "react";
import { useRouter } from "next/navigation";

// ════════════════════════════════════════════════════════════
// 📌 [React 개념 3: 컴포넌트(Component) — React의 핵심 단위]
//
// React에서 화면의 모든 것은 "컴포넌트"라는 단위의 함수로 이루어집니다.
// 이 함수는 HTML(JSX)을 반환(return) 하면, React가 그걸 화면에 그려줍니다.
// "export default" 는 이 파일을 대표하는 함수라는 뜻입니다.
//   → Next.js가 /chat 주소로 접속하면 이 함수를 자동으로 실행합니다.
// ════════════════════════════════════════════════════════════
export default function ChatPage() {

    // ════════════════════════════════════════════════════════
    // 📌 [React 개념 4: useState — 화면과 연동된 변수]
    //
    // useState는 "이 값이 바뀌면 화면을 자동으로 다시 그려줘!" 라는 계약입니다.
    //
    // 사용법: const [값, 값을바꾸는함수] = useState(초기값);
    //
    // 기존 chat.js 에서는:
    //   let messages = []; → 배열에 push 후 직접 DOM 조작
    // React에서는:
    //   setMessages(...)를 호출하는 것만으로 화면이 자동 갱신됩니다.
    //
    // ⚠️ 주의: 값을 바꾸려면 반드시 set함수()를 써야 합니다.
    //   messages.push(새메시지) → ❌ 화면이 안 바뀜
    //   setMessages([...]) → ✅ 화면이 자동으로 다시 그려짐
    // ════════════════════════════════════════════════════════

    // 채팅 메시지 목록 (백엔드에서 받은 메시지들이 쌓이는 배열)
    const [messages, setMessages] = useState([]);

    // 접속자 목록 문자열 (기존 [USERS] 형식으로 받아서 파싱)
    const [users, setUsers] = useState("접속자 없음");

    // 입력창에 타이핑하는 텍스트
    const [inputText, setInputText] = useState("");

    // 현재 로그인한 내 유저명 (로그인 후 /api/auth/me로 조회)
    const [myUsername, setMyUsername] = useState("");

    // 이미지 업로드 중인지 여부 (true일 때 로딩 표시)
    const [isUploading, setIsUploading] = useState(false);

    // ════════════════════════════════════════════════════════
    // 📌 [React 개념 5: useRef — 리렌더링에도 살아남는 변수]
    //
    // useState와 다르게, 값이 바뀌어도 화면을 다시 그리지 않습니다.
    // 대신 렌더링이 일어나도 값이 초기화되지 않고 유지됩니다.
    //
    // 두 가지 용도:
    //   1) WebSocket 객체 보관
    //      → ws = new WebSocket(...)을 전역 변수 대신 이걸로 저장합니다.
    //      → useState로 WebSocket을 저장하면 set할 때마다 화면이 재렌더링 되기 때문.
    //
    //   2) DOM 요소에 직접 접근 (스크롤 제어 등)
    //      → HTML에서 <div ref={chatBoxRef}> 처럼 달아두면
    //        chatBoxRef.current가 그 div 요소를 가리킵니다.
    //        마치 document.getElementById("chatBox")와 동일한 효과입니다.
    // ════════════════════════════════════════════════════════

    // WebSocket 객체 보관 (기존 chat.js의 let ws; 와 동일한 역할)
    const wsRef = useRef(null);

    // 채팅 박스 DOM에 직접 접근하기 위한 ref (스크롤 자동이동에 사용)
    const chatBoxRef = useRef(null);

    // 이미지 파일 input 요소에 접근하기 위한 ref
    const imageInputRef = useRef(null);

    // 도배 방지용 마지막 전송 시각 (기존 let lastSendTime = 0; 과 동일)
    const lastSendTimeRef = useRef(0);

    // ── 히스토리 페이징 관련 Ref (기존 chat-history.js의 전역변수들과 동일) ──
    // 왜 useState가 아니라 useRef? → 이 값들이 바뀔 때 화면을 다시 그릴 필요가 없어서.
    // 페이징 제어용 내부 플래그들이지, UI에 직접 표시되는 값이 아닙니다.

    // 가장 오래된 메시지의 id (다음 페이지 로드 시 이 id 기준으로 요청)
    // → 기존: let oldestChatId = null;
    const oldestChatIdRef = useRef(null);

    // 현재 히스토리를 불러오는 중인지 (중복 요청 방지)
    // → 기존: let isFetching = false;
    const isFetchingRef = useRef(false);

    // 더 이상 로드할 히스토리가 없는지
    // → 기존: let isEnd = false;
    const isEndRef = useRef(false);

    // IntersectionObserver가 감시할 div 요소 (chatBox 맨 위에 위치)
    // → 기존: document.getElementById("observer-target")
    const observerTargetRef = useRef(null);

    // 화면 이동 도구
    const router = useRouter();

    // ════════════════════════════════════════════════════════
    // 📌 [React 개념 6: useEffect — 컴포넌트의 생명주기 관리]
    //
    // 생명주기란? 컴포넌트가 화면에 "나타나고" → "살아있다가" → "사라지는" 흐름입니다.
    //
    // useEffect(() => {
    //     // 여기: 컴포넌트가 화면에 나타날 때(Mount) 실행
    //     return () => {
    //         // 여기: 컴포넌트가 화면에서 사라질 때(Unmount) 실행 (청소)
    //     };
    // }, [의존성배열]);
    //
    // 의존성배열:
    //   []        → "처음 마운트 시 딱 한 번만 실행"  ← 우리가 쓸 방식
    //   [someVar] → "someVar가 바뀔 때마다 실행"
    //   없음      → "렌더링 할 때마다 실행" (거의 안 씀)
    //
    // 기존 chat.js의:
    //   window.addEventListener("DOMContentLoaded", () => { connectWebSocket(); })
    // 와 완전히 동일한 역할입니다!
    // ════════════════════════════════════════════════════════

    // [Effect 1] 컴포넌트가 처음 마운트될 때: 내 유저명을 백엔드에서 받아오기
    useEffect(() => {
        // /api/auth/me 는 현재 JWT 쿠키를 확인해서 내 유저명을 반환하는 API입니다.
        // 로그인을 안 한 상태면 401 에러가 나고, 그럼 로그인 페이지로 보냅니다.
        fetch("http://localhost:8080/api/auth/me", {
            credentials: "include", // JWT 쿠키를 함께 보내야 인증이 됩니다!
        })
            .then((res) => {
                if (!res.ok) {
                    // 인증 실패 → 로그인 페이지로 강제 이동
                    router.push("/login");
                    return null;
                }
                return res.json();
            })
            .then((data) => {
                if (data) {
                    setMyUsername(data.username); // 내 유저명을 State에 저장
                }
            })
            .catch(() => {
                router.push("/login");
            });
    }, []); // [] → 처음 한 번만 실행

    // [Effect 2] myUsername이 정해진 후 → WebSocket 연결
    // 의존성 배열에 [myUsername]을 넣어서 "myUsername이 설정된 직후"에만 실행되게 합니다.
    useEffect(() => {
        // username이 아직 비어있으면 (첫 렌더 시) 연결 시도 안 함
        if (!myUsername) return;

        // ── WebSocket 연결 ──────────────────────────────────
        // 기존 chat.js: ws = new WebSocket("ws://" + location.host + "/chat?name=...");
        // React에서는: wsRef.current에 저장합니다.
        const ws = new WebSocket("ws://localhost:8080/chat");
        wsRef.current = ws;

        // ── 메시지 수신 이벤트 핸들러 ──────────────────────
        // 기존 chat.js: ws.onmessage = function(event) { ... }
        // 동일한 역할이지만, DOM 조작 대신 setMessages()로 State를 업데이트합니다.
        ws.onmessage = (event) => {
            const data = event.data;

            // [USERS] 로 시작하면 → 접속자 명단 업데이트
            if (data.startsWith("[USERS]")) {
                const userStr = data.substring(7);
                setUsers(userStr || "접속자 없음");
                return;
            }

            // 그 외 → 채팅 메시지 JSON 파싱
            try {
                const msgObj = JSON.parse(data);
                // ════════════════════════════════════════════
                // 📌 [핵심 패턴: 배열 State 업데이트]
                //
                // setMessages(prev => [...prev, msgObj])
                //
                // prev = 현재 messages 배열의 최신 상태
                // [...prev, msgObj] = 기존 배열을 펼치고 새 메시지를 뒤에 추가
                //
                // 기존 방식: messages.push(msg)
                // React 방식: "기존 배열을 복사하고 새 요소를 추가한 새 배열"로 교체
                //
                // 왜 push를 안 쓰나요?
                // React는 "배열 자체가 바뀌었는지"로 변화를 감지합니다.
                // push는 같은 배열 객체를 수정하므로 React가 변화를 못 잡습니다.
                // 새 배열([...prev, msgObj])을 만들어야 React가 "아 바뀌었구나!" 합니다.
                // ════════════════════════════════════════════
                setMessages((prev) => [...prev, msgObj]);

                // ── 새 메시지를 받으면 스크롤을 맨 아래로 ──
                // 히스토리 로드(fetchHistory)와 이 부분을 분리하는 것이 핵심입니다.
                // → 히스토리: 스크롤 위치 유지 (fetchHistory 안에서 prevScrollHeight 보정)
                // → 새 메시지: 스크롤 맨 아래로 (여기서 처리)
                //
                // requestAnimationFrame을 쓰는 이유:
                // setMessages()를 호출한 직후는 React가 아직 DOM을 업데이트하기 전입니다.
                // 바로 scrollTop = scrollHeight 를 해도 새 메시지가 아직 그려지지 않아서
                // 높이가 반영이 안 됩니다.
                // requestAnimationFrame은 "다음 화면 렌더링 직전에 실행해줘"라는 뜻으로,
                // React가 DOM을 업데이트한 직후 실행이 보장됩니다.
                requestAnimationFrame(() => {
                    if (chatBoxRef.current) {
                        chatBoxRef.current.scrollTop = chatBoxRef.current.scrollHeight;
                    }
                });
            } catch (e) {
                console.error("메시지 파싱 오류:", e);
            }
        };

        ws.onerror = (err) => {
            console.error("WebSocket 오류:", err);
        };

        ws.onclose = () => {
            console.log("WebSocket 연결 종료됨");
        };

        // ── 클린업 함수 (언마운트 시 자동 실행) ─────────────
        // 기존 JS는 페이지를 벗어나도 소켓을 닫는 로직이 없었습니다.
        // React의 return () => {} 구문이 컴포넌트가 사라질 때 자동으로 ws.close()를 호출합니다.
        // 예: 로그아웃 후 /login 으로 이동하면 이 함수가 실행되어 소켓을 깔끔하게 정리합니다.
        return () => {
            ws.close();
        };
    }, [myUsername]); // myUsername이 설정된 직후에 실행

    // ════════════════════════════════════════════════════════
    // 📌 [히스토리 페이징 함수]
    //
    // 기존 chat-history.js의 fetchOldChatHistory() 와 동일한 역할입니다.
    // REST API(/api/chat/history)를 호출해서 이전 메시지들을 가져옵니다.
    //
    // 핵심 포인트: 히스토리는 messages 배열 "앞에" 붙여야 합니다.
    //   새 메시지: [...prev, newMsg]  → 뒤에 추가 (아래에 표시)
    //   히스토리:  [...history, ...prev] → 앞에 추가 (위에 표시)
    //
    // 그리고 히스토리 로드 후 스크롤이 확 위로 튀면 안 됩니다.
    // 로드 전 scrollHeight를 기억해두고, 로드 후 그 차이만큼 보정합니다.
    // ════════════════════════════════════════════════════════
    const fetchHistory = async () => {
        // 중복 요청 방지 / 더 이상 데이터 없으면 즉시 종료
        if (isFetchingRef.current || isEndRef.current) return;      // isFetchingRef.current = "지금 요청 중인가?" || isEndRef.current = "데이터가 끝났는가?"
        isFetchingRef.current = true;                               // "지금 요청 중"으로 변경, lock 하는것과 동일

        // 로드 전 현재 스크롤 높이를 기억 (나중에 스크롤 위치 보정에 사용)
        const prevScrollHeight = chatBoxRef.current?.scrollHeight || 0; // chatBoxRef.current 가 null일 경우 undefined가 나오는데, 이럴 경우 0으로 대체.

        // URL 구성: 처음 호출이면 lastId 없이, 이후 호출은 lastId 포함
        // → 기존: let url = '/api/chat/history?limit=50';
        //          if (oldestChatId !== null) url += '&lastId=' + oldestChatId;
        let url = "http://localhost:8080/api/chat/history?limit=50";
        if (oldestChatIdRef.current !== null) {
            url += `&lastId=${oldestChatIdRef.current}`;
        }

        try {
            const res = await fetch(url, { credentials: "include" });
            if (!res.ok) throw new Error("히스토리 API 오류");

            const chatList = await res.json();

            // 빈 배열이 오면 더 이상 데이터 없음 → 종료 플래그 세팅
            if (chatList.length === 0) {
                isEndRef.current = true;
                return;
            }

            // 가장 오래된 메시지의 id를 기억 (다음 페이지 요청에 사용)
            // 백엔드가 최신순으로 내려준다면 마지막 요소, 오래된순이면 첫 요소
            // 기존 코드 기준: chatList[chatList.length - 1].id
            oldestChatIdRef.current = chatList[chatList.length - 1].id;

            // 히스토리를 messages 배열 "앞에" 붙이기
            // chatList는 최신→오래된 순으로 오므로, 화면 표시용으로 뒤집어 붙입니다.
            setMessages((prev) => [...chatList.reverse(), ...prev]);

            // ── 스크롤 위치 보정 ──────────────────────────────
            // 히스토리가 위에 추가되면 스크롤이 확 위로 튀는 문제가 발생합니다.
            // 해결: 새로운 scrollHeight - 이전 scrollHeight = 추가된 콘텐츠 높이
            //       그만큼 scrollTop을 더해주면 사용자가 보던 위치가 유지됩니다.
            // requestAnimationFrame: DOM이 실제로 렌더링된 다음 프레임에 실행
            //   → setMessages 직후 바로 실행하면 아직 DOM에 반영이 안 되어 있어서
            //     scrollHeight가 변하지 않은 상태일 수 있습니다.
            requestAnimationFrame(() => {
                if (chatBoxRef.current) {
                    const newScrollHeight = chatBoxRef.current.scrollHeight;
                    chatBoxRef.current.scrollTop += newScrollHeight - prevScrollHeight;
                }
            });
        } catch (err) {
            console.error("히스토리 로드 실패:", err);
        } finally {
            isFetchingRef.current = false;
        }
    };

    // [Effect 3] IntersectionObserver 등록
    // 기존 chat-history.js의 observer 설정과 동일한 역할입니다.
    //
    // IntersectionObserver란?
    //   특정 DOM 요소가 "화면에 보이는지"를 감시하는 브라우저 내장 API입니다.
    //   스크롤 이벤트를 계속 감청하는 것보다 훨씬 성능이 좋습니다.
    //
    // 동작 흐름:
    //   1. chatBox 맨 위에 1px 짜리 투명 div (observerTargetRef) 를 배치
    //   2. 사용자가 스크롤을 최상단까지 올리면 그 div가 화면에 들어옴
    //   3. Observer가 감지 → fetchHistory() 호출
    useEffect(() => {
        // observerTarget div가 아직 DOM에 없으면 대기
        if (!observerTargetRef.current) return;

        const observer = new IntersectionObserver(
            (entries) => {
                // entries[0].isIntersecting: 감시 대상이 화면 안에 들어왔는지
                if (entries[0].isIntersecting) {
                    fetchHistory();
                }
            },
            {
                root: chatBoxRef.current, // chatBox 영역 안에서만 감시
                threshold: 0.1,           // 10% 이상 보이면 감지
            }
        );

        observer.observe(observerTargetRef.current);

        // 클린업: 컴포넌트 언마운트 시 Observer 해제
        return () => observer.disconnect();
    }, [myUsername]); // myUsername이 설정된 후(= 로그인 확인 후)에 등록

    // ════════════════════════════════════════════════════════
    // 📌 [일반 함수들 — 이벤트 핸들러]
    //
    // 기존 chat.js에서 addEventListener로 연결하던 함수들을 그냥 함수로 선언합니다.
    // JSX에서 onClick={함수명} 형태로 연결합니다.
    // ════════════════════════════════════════════════════════

    // 메시지 전송 함수 (기존 chat.js의 sendMsg() 와 동일)
    const handleSend = () => {
        const text = inputText.trim();
        if (!text) return;

        // 도배 방지: 1초에 1번만 전송 가능
        const now = Date.now();
        if (now - lastSendTimeRef.current < 1000) {
            alert("도배 방지: 1초 후에 다시 보내주세요!");
            return;
        }
        lastSendTimeRef.current = now;

        // WebSocket이 열려있는지 확인 후 전송
        // wsRef.current는 useRef로 저장한 WebSocket 객체입니다.
        if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
            const sendData = {
                sender: myUsername,
                content: text,
            };
            wsRef.current.send(JSON.stringify(sendData));
        }

        // 입력창 초기화
        // 기존: input.value = ""
        // React: setInputText("") → State를 빈 문자열로 바꾸면 input도 자동으로 빈칸이 됩니다.
        setInputText("");
    };

    // 엔터 키 전송
    const handleKeyDown = (e) => {
        if (e.key === "Enter") handleSend();
    };

    // 로그아웃 함수
    const handleLogout = async () => {
        await fetch("http://localhost:8080/api/auth/logout", {
            credentials: "include",
        });
        router.push("/login");
    };


    // 이미지 업로드 함수 (기존 chat.js이미지 업로드 로직과 동일)
    const handleImageUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const MAX_SIZE = 5 * 1024 * 1024; // 5MB
        if (file.size > MAX_SIZE) {
            alert("파일 용량은 5MB를 초과할 수 없습니다.");
            e.target.value = "";
            return;
        }

        // isUploading State를 true로 → 업로딩 중 UI 표시 (State로 로딩 상태 관리)
        setIsUploading(true);

        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await fetch("http://localhost:8080/api/upload/image", {
                method: "POST",
                body: formData,
                credentials: "include",
            });

            if (!res.ok) throw new Error("업로드 실패");

            const imageUrl = await res.text();

            // 업로드 성공 → WebSocket으로 이미지 URL 전송
            if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
                const msgObj = {
                    sender: myUsername,
                    content: imageUrl,
                    messageType: "IMAGE",
                };
                wsRef.current.send(JSON.stringify(msgObj));
            }
        } catch (err) {
            alert(err.message || "사진 업로드에 실패했습니다.");
        } finally {
            // 성공이든 실패든 로딩 상태 해제
            setIsUploading(false);
            e.target.value = "";
        }
    };

    // ════════════════════════════════════════════════════════
    // 📌 [메시지 렌더링 헬퍼 함수]
    //
    // 기존 chat.js: createMessageBubble(msgObj) 함수가 DOM 요소를 직접 만들었습니다.
    //   → wrapper.innerHTML = "<div ...>" + ... + "</div>"
    //
    // React에서는: JSX를 반환하는 함수로 만듭니다.
    //   → 문자열로 HTML을 짜 맞추는 대신, JSX 태그를 반환합니다.
    //   → 훨씬 가독성이 좋고, 타입 안전도 됩니다.
    // ════════════════════════════════════════════════════════
    const renderMessageBubble = (msgObj, index) => {
        const rawTime = msgObj.sendTime || Date.now();
        const dateObj = new Date(rawTime);
        const timeStr = dateObj.toLocaleTimeString("ko-KR", {
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
        });
        const currentDateStr = dateObj.toLocaleDateString("ko-KR");

        // 날짜 구분선이 필요한지 체크
        const showDateDivider =
            index === 0 ||
            new Date(messages[index - 1]?.sendTime || 0).toLocaleDateString( // 이전 메시지의 날짜 값과 현재의 값이 다른가? -> 구분선 표기해야함
                "ko-KR"
            ) !== currentDateStr;

        const displayDate = dateObj.toLocaleDateString("ko-KR", {
            month: "long",
            day: "numeric",
        });

        // 이미지 메시지인지 체크
        const isImage =
            msgObj.content &&
            (msgObj.content.startsWith("/uploads/") ||
                msgObj.messageType === "IMAGE");

        const isMe = msgObj.sender === myUsername;

        // ════════════════════════════════════════════════════
        // 📌 [React 개념 7: JSX — JavaScript + HTML 혼합 문법]
        //
        // JSX는 JS 안에서 HTML처럼 생긴 코드를 쓸 수 있게 해주는 문법입니다.
        // 컴파일 시 React.createElement(...) 호출로 변환됩니다.
        //
        // 주의사항:
        //   - HTML class → JSX에서는 className (class는 JS 예약어라서)
        //   - {} 안에는 JS 표현식 사용 가능: {isMe ? "나" : "상대방"}
        //   - 모든 태그는 반드시 닫혀야 함: <br /> (self-closing)
        // ════════════════════════════════════════════════════
        return (
            // ════════════════════════════════════════════════
            // 📌 [React 개념 8: key prop — 목록 렌더링 필수]
            //
            // React가 배열을 map()으로 렌더링할 때, 각 요소에 고유한 key를 줘야 합니다.
            // key가 있어야 React가 "이 요소는 새로 생겼고, 저 요소는 그대로구나"를
            // 효율적으로 파악할 수 있습니다.
            // 여기서는 index를 쓰지만, 실제 서비스에서는 message.id 같은 고유값을 씁니다.
            // ════════════════════════════════════════════════
            <div key={index}>
                {/* 날짜 구분선 (기존 chat.js의 date-divider와 동일) */}
                {showDateDivider && (           // showDateDivider가 true일 때만 렌더링
                    <div className="date-divider">
                        <span>{displayDate}</span>
                    </div>
                )}

                {/* 내 메시지: 오른쪽 / 상대방 메시지: 왼쪽 */}
                {isMe ? (
                    <div className="msg-wrapper-me">
                        <div className="msg-row msg-me">
                            <span className="msg-time">{timeStr}</span>
                            <div className="msg-bubble">
                                {isImage ? (
                                    <img
                                        src={`http://localhost:8080${msgObj.content}`}
                                        alt="전송된 이미지"
                                        style={{
                                            maxWidth: "200px",
                                            borderRadius: "8px",
                                        }}
                                    />
                                ) : (
                                    msgObj.content
                                )}
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="msg-wrapper-other">
                        <div className="msg-sender">{msgObj.sender}</div>
                        <div className="msg-row msg-other">
                            <div className="msg-bubble">
                                {isImage ? (
                                    <img
                                        src={`http://localhost:8080${msgObj.content}`}
                                        alt="전송된 이미지"
                                        style={{
                                            maxWidth: "200px",
                                            borderRadius: "8px",
                                        }}
                                    />
                                ) : (
                                    msgObj.content
                                )}
                            </div>
                            <span className="msg-time">{timeStr}</span>
                        </div>
                    </div>
                )}
            </div>
        );
    };

    // ════════════════════════════════════════════════════════
    // 📌 [React 개념 9: return — 화면에 그릴 JSX 반환]     export default ChatPage()의 return
    //
    // 컴포넌트 함수의 return() 안에 있는 JSX가 실제 화면에 표시됩니다.
    // State(상태)가 바뀔 때마다 이 return 부분이 다시 실행되어 화면이 갱신됩니다.
    //
    // JSX 규칙: return 안에는 반드시 하나의 최상위 태그만 있어야 합니다.
    //   → 여러 태그가 필요하면 <div>로 감싸거나 <> </> (Fragment)를 씁니다.
    // ════════════════════════════════════════════════════════
    return (
        // min-h-screen: 최소 높이를 화면 전체로, bg-[#f4f5f7]: 기존 CSS의 배경색 유지
        <div className="min-h-screen bg-[#f4f5f7] flex flex-col items-center p-4">

            {/* ── 상단 헤더 바 (로그인 유저 표시 + 로그아웃) ── */}
            <div className="w-full max-w-md mb-2 flex justify-between items-center px-3 py-2 bg-white rounded-xl shadow-sm">
                <span className="text-sm font-medium text-gray-700">
                    👤 <strong>{myUsername || "..."}</strong> 님
                </span>
                <button
                    onClick={handleLogout}
                    className="text-sm text-white font-semibold px-3 py-1 rounded-lg transition-all duration-200"
                    style={{ backgroundColor: "#E3000F" }}
                >
                    로그아웃
                </button>
            </div>

            {/* ── 채팅 메인 래퍼 ── */}
            <div className="chat-wrap w-full max-w-md flex flex-col"
                style={{ borderRadius: "10px", overflow: "hidden", boxShadow: "0 4px 12px rgba(0,0,0,0.1)" }}>

                {/* ── 상단 타이틀 헤더 (SK 레드→오렌지 그라데이션) ── */}
                <div
                    className="header text-white text-center py-4 font-bold text-lg"
                    style={{
                        background: "linear-gradient(to right, #E3000F, #F6A800)",
                    }}
                >
                    SK 사내 익명 톡 💬
                </div>

                {/* ── 접속자 명단 바 ── */}
                <div className="user-list">
                    👥 접속자: {users}
                </div>

                {/* ── 채팅 메시지 영역 ──
                    ref={chatBoxRef}: 이 div를 chatBoxRef로 잡아두었다가
                    메시지가 추가될 때 scrollTop 을 조작합니다.
                    (기존 document.getElementById("chatBox")와 동일한 역할)
                */}
                <div
                    id="chatBox"
                    ref={chatBoxRef}
                    style={{
                        height: "420px",
                        padding: "15px",
                        overflowY: "auto",
                        background: "#fafafa",
                        borderBottom: "1px solid #eee",
                    }}
                >
                    {/* ── 스크롤 감지 타겟 div ──
                        기존 room.jsp의 <div id="observer-target" style="height:1px"> 와 동일.
                        chatBox 맨 위에 위치하며, IntersectionObserver가 이걸 감시합니다.
                        사용자가 스크롤을 최상단까지 올리면 이 div가 화면에 들어오고,
                        그 순간 fetchHistory()가 실행됩니다.
                    */}
                    <div ref={observerTargetRef} style={{ height: "1px" }} />
                    {/* ════════════════════════════════════════════════
                        📌 [React 개념 10: .map() — 배열 반복 렌더링]

                        기존 chat.js: box.appendChild(bubble) 을 반복
                        React: 배열.map((요소, 인덱스) => JSX)

                        messages 배열의 각 메시지를 renderMessageBubble()로 변환해서
                        목록으로 그려줍니다.

                        State(messages)가 바뀔 때마다 이 map도 다시 실행 →
                        새 메시지가 화면에 자동으로 나타납니다!
                        ════════════════════════════════════════════════ */}
                    {messages.map((msg, index) =>
                        renderMessageBubble(msg, index)
                    )}

                    {/* 이미지 업로드 중 로딩 말풍선 */}
                    {isUploading && (
                        <div className="msg-wrapper-me">
                            <div className="msg-row msg-me">
                                <div className="msg-bubble" style={{ backgroundColor: "#F6A800", color: "white" }}>
                                    <div className="loader" style={{ display: "inline-block" }}></div>
                                    사진 업로드 중...
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* ── 하단 입력창 영역 ── */}
                <div className="input-wrap" style={{ display: "flex", padding: "10px", backgroundColor: "#fff" }}>

                    {/* 사진 버튼 → 숨겨진 file input을 클릭하게 해줌 */}
                    <button
                        onClick={() => imageInputRef.current?.click()}
                        className="text-sm px-3 mr-2 rounded-lg font-medium transition-all duration-200"
                        style={{
                            backgroundColor: "#f0f0f0",
                            color: "#555",
                            border: "1px solid #ddd",
                        }}
                        title="사진 전송"
                    >
                        📷
                    </button>

                    {/* 실제 파일 선택 input (화면에는 보이지 않음, display:none) */}
                    <input
                        type="file"
                        accept="image/*"
                        ref={imageInputRef}
                        onChange={handleImageUpload}
                        style={{ display: "none" }}
                    />

                    {/* 텍스트 입력창
                        value={inputText}: State와 input 값을 양방향으로 묶음 (Controlled Input)
                        onChange: 타이핑할 때마다 State를 업데이트
                        → 이걸 "제어 컴포넌트(Controlled Component)" 패턴이라고 합니다.
                    */}
                    <input
                        id="msgInput"
                        type="text"
                        value={inputText}
                        onChange={(e) => setInputText(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="메시지 입력..."
                        style={{
                            flex: 1,
                            padding: "10px",
                            border: "1px solid #ddd",
                            borderRadius: "5px",
                            outline: "none",
                        }}
                    />

                    {/* 전송 버튼 */}
                    <button
                        id="btnSend"
                        onClick={handleSend}
                        className="btn-send font-bold ml-2 px-4 rounded-lg text-white transition-all duration-200"
                        style={{ backgroundColor: "#E3000F" }}
                    >
                        전송
                    </button>
                </div>
            </div>
        </div>
    );
}
