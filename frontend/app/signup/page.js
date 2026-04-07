"use client";

import { useState } from 'react';
import { useRouter } from 'next/navigation';

export default function SignupPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const router = useRouter();

    const handleSignup = async () => {
        try {
            // 회원가입은 백엔드의 /api/auth/signup 주소로 보냅니다!
            const response = await fetch("http://localhost:8080/api/auth/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ 
                    username: username, 
                    password: password 
                }),
                // 회원가입할 때는 쿠키가 아직 없지만, CORS 통신 규칙을 위해 적어줍니다.
                credentials: "include", 
            });

            const data = await response.json();

            if (response.ok && data.status === "success") {
                alert("회원가입 성공! 이제 로그인 해주세요.");
                // 성공하면 프론트엔드가 주도적으로 로그인 페이지(/login)로 이동시킵니다!
                router.push("/login"); 
            } else {
                alert("회원가입 실패: " + data.message);
            }
        } catch (error) {
            alert("서버와 통신하는 중 문제가 발생했습니다.");
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
            <div className="bg-white p-8 rounded-2xl shadow-lg max-w-sm w-full">
                <h1 className="text-2xl font-bold text-center text-gray-800 mb-6">회원가입</h1>
                
                <form className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">사용할 아이디</label>
                        <input 
                            type="text" 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="아이디를 입력하세요"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                    </div>
                    
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">사용할 비밀번호</label>
                        <input 
                            type="password" 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="비밀번호를 입력하세요"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                    </div>

                    <button 
                        type="button"
                        onClick={handleSignup}
                        className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition duration-200"
                    >
                        가입완료
                    </button>
                </form>

                <div className="mt-4 text-center text-sm text-gray-600">
                    이미 계정이 있으신가요? 
                    <a href="/login" className="text-blue-500 hover:underline ml-1">로그인 하러가기</a>
                </div>
            </div>
        </div>
    );
}
