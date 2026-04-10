// ════════════════════════════════════════════════════════
// 📌 [새로운 부품: MessageBubble 컴포넌트]
//
// 이 파일은 "말풍선 하나를 그리는 역할"만 전담합니다.
// 부모 컴포넌트(ChatPage)로부터 세 가지 데이터(Props)를 받습니다.
//
// 1. msgObj : 메시지 내용 (누가 보냈는지, 무슨 내용인지)
// 2. index : 현재 메시지의 순번 (React가 목록을 관리할 때 필요)
// 3. showDateDivider : 오늘 날짜 선을 그을지 말지 (부모가 계산해서 알려줌)
// 4. isMe : 내가 보낸 건지 남이 보낸 건지 (부모가 계산해서 알려줌)
// ════════════════════════════════════════════════════════

export default function MessageBubble({ msgObj, index, showDateDivider, isMe }) {
    // 1. 시간 변환기 (부모에게 있던 로직을 이사 옴)
    const rawTime = msgObj.sendTime || Date.now();
    const dateObj = new Date(rawTime);
    const timeStr = dateObj.toLocaleTimeString("ko-KR", {
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
    });

    const displayDate = dateObj.toLocaleDateString("ko-KR", {
        month: "long",
        day: "numeric",
    });

    // 2. 이미지가 섞여 있는지 검사
    const isImage =
        msgObj.content &&
        (msgObj.content.startsWith("/uploads/") || msgObj.messageType === "IMAGE");

    // 3. 화면 그리기 (JSX 반환)
    return (
        <div key={index}>
            {/* 날짜 구분선이 필요하다고 부모가 알려주면 그리기 */}
            {showDateDivider && (
                <div className="date-divider">
                    <span>{displayDate}</span>
                </div>
            )}

            {/* 내가 보낸 거면 오른쪽, 남이 보낸 거면 왼쪽 디자인 */}
            {isMe ? (
                <div className="msg-wrapper-me">
                    <div className="msg-row msg-me">
                        <span className="msg-time">{timeStr}</span>
                        <div className="msg-bubble">
                            {isImage ? (
                                <img
                                    src={`http://localhost:8080${msgObj.content}`}
                                    alt="전송된 이미지"
                                    style={{ maxWidth: "200px", borderRadius: "8px" }}
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
                                    style={{ maxWidth: "200px", borderRadius: "8px" }}
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
}
