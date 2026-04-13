"use client"; // Next.js에게 "이 파일은 브라우저(클라이언트)에서 자바스크립트가 동작해야 해!"라고 알려주는 마법의 단어입니다.

import { useState } from 'react';
import { useRouter } from 'next/navigation'; // 화면 이동을 도와주는 도구

export default function LoginPage() {
    // 1. 상태(State) 선언: 아이디와 비밀번호를 기억할 공간(변수)을 만듭니다.
    // username: 현재 값, setUsername: 값을 바꾸는 리모컨           username : 현재 글자가 저장될 진짜 변수  -> setUsername()에 타이핑을 하는순간 계속 username에 값이 들어감.
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errorMsg, setErrorMsg] = useState(""); // 에러 메시지를 담을 공간
    const router = useRouter(); // 라우터(네비게이션) 도구 장착!

    // 2. 로그인 버튼을 눌렀을 때 실행될 함수
    const handleLogin = async () => {
        // 백엔드로 통신(fetch)을 보냅니다.
        try {
            const response = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",                     // 폼 전송 방식
                headers: {
                    "Content-Type": "application/json", // "우리가 보내는 건 JSON 형식이야!"
                },
                // 아이디와 비밀번호(State)를 하나로 묶어서 JSON 문자열로 변환해 보냅니다.
                body: JSON.stringify({
                    username: username,
                    password: password
                }),
                // ★ 가장 중요! 이 옵션을 켜야 백엔드가 구워준 JWT 쿠키를 브라우저가 받아먹고 저장합니다.
                credentials: "include",
            });

            const data = await response.json(); // 백엔드가 보내준 답변(JSON)을 뜯어봅니다.

            if (response.ok && data.status === "success") {
                // 3. 로그인이 성공했으므로 프론트엔드가 주도적으로 채팅방(/chat) 화면으로 이동시킵니다!
                router.push("/chat");
            } else {
                // 비밀번호가 틀렸을 때 백엔드가 보내준 에러 메시지를 띄워줍니다.
                setErrorMsg("로그인 실패: " + data.message);
            }
        } catch (error) {
            setErrorMsg("서버와 통신하는 중 문제가 발생했습니다.");
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === "Enter") {
            handleLogin();
        }
    }

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
            <div className="bg-white p-8 rounded-2xl shadow-lg max-w-sm w-full">
                <h1 className="text-2xl font-bold text-center text-gray-800 mb-6">SK 사내 익명 톡</h1>

                {/* 에러 메시지가 있을 때만 뜨는 경고 박스 */}
                {errorMsg && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm" role="alert">
                        <span className="block sm:inline">{errorMsg}</span>
                    </div>
                )}

                <form className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">아이디</label>
                        <input
                            type="text"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="아이디를 입력하세요"
                            // 사용자가 글자를 칠 때마다 그 글자를 State(username)에 곧바로 저장합니다!
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
                        <input
                            type="password"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="비밀번호를 입력하세요"
                            // 마찬가지로 비밀번호를 State(password)에 저장합니다.
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>

                    <button
                        type="button"
                        onClick={handleLogin} // 버튼을 누르면 위에서 만든 handleLogin 함수가 실행됩니다!
                        onKeyDown={handleKeyDown}
                        className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition duration-200"
                    >
                        로그인
                    </button>
                </form>

                <div className="mt-4 text-center text-sm text-gray-600">
                    계정이 없으신가요?
                    <a href="/signup" className="text-blue-500 hover:underline ml-1">회원가입</a>
                </div>
            </div>
        </div>
    );
}
