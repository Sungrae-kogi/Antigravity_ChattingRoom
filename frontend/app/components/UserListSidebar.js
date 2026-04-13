// ════════════════════════════════════════════════════════════
// 📌 [React 컴포넌트: 접속자 명단 사이드바]
//
// 이 파일은 메인 채팅방(chat/page.js) 우측에 나타날 사이드바 UI만을 담당합니다.
// 이렇게 화면을 부품(Component)으로 쪼개 놓으면, 나중에 코드를 유지보수하기가 
// 훨씬 쉬워지고 메인 채팅방의 코드가 깔끔해집니다!
// ════════════════════════════════════════════════════════════

// 컴포넌트는 항상 대문자로 시작하는 이름을 가집니다.
// (users) 라고 적힌 부분은 'Props(프롭스)'라고 부르며, 
// 부모 컴포넌트(chat/page.js)가 넘겨준 데이터(접속자 명단 배열)를 받아오는 매개변수입니다.
export default function UserListSidebar({ users }) {
    return (
        // 사이드바의 겉 테두리(컨테이너) 디자인
        // w-64: 너비를 16rem(약 256px)로 고정, bg-white: 하얀 배경,
        // shadow-sm: 가벼운 그림자, rounded-xl: 둥근 둥근 모서리,
        // ml-4: 왼쪽 채팅창과의 간격(여백) 1rem 띄우기
        <div className="w-64 bg-white shadow-sm rounded-xl flex flex-col overflow-hidden ml-4">
            
            {/* ── 사이드바 타이틀 ── */}
            <div className="bg-gray-100 py-4 px-5 border-b border-gray-200">
                <h2 className="text-gray-800 font-bold text-lg flex items-center">
                    👥 현재 접속자 (
                    {/* 배열의 길이를 측정하면 접속자 숫자가 됩니다! */}
                    <span className="text-blue-600 ml-1">{users.length}</span>
                    )
                </h2>
            </div>

            {/* ── 접속자 리스트 컨테이너 ── */}
            {/* overflow-y-auto: 접속자가 많아지면 안에서만 스크롤 되도록 만듭니다. */}
            <div className="flex-1 p-3 overflow-y-auto" style={{ maxHeight: "480px" }}>
                
                {/* 
                  📌 [React 배열 렌더링 파트]
                  여기가 핵심입니다! users 배열에 들어있는 이름들을 
                  .map() 함수를 돌려서 하나씩 <div> 고기만두로 빚어냅니다.
                */}
                {users.length > 0 ? (
                    <ul className="space-y-2">
                        {users.map((username, index) => (
                            <li 
                                // index는 반복문 안에서 각 항목을 구분하는 고유 번호(주민번호) 같은 역할입니다.
                                key={index} 
                                className="flex items-center px-3 py-2 bg-gray-50 rounded-lg border border-gray-100 hover:bg-gray-100 transition-colors"
                            >
                                {/* 초록색 동그라미(온라인 불빛 효과) */}
                                <span className="relative flex h-3 w-3 mr-3">
                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                                    <span className="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
                                </span>
                                
                                {/* 실제 유저 이름 */}
                                <span className="text-gray-700 font-medium text-sm">
                                    {username}
                                </span>
                            </li>
                        ))}
                    </ul>
                ) : (
                    // 배열이 텅 비어있을 0명일 때 보여줄 알림 문자 
                    <div className="text-center text-gray-500 text-sm mt-10">
                        접속자가 없습니다.
                    </div>
                )}
            </div>
        </div>
    );
}
