// 1. 입장 시 익명 이름 설정창
let username = "익명";
let ws;
let lastSendTime = 0;

// 입장 로직
document.getElementById("btnJoin").addEventListener("click", startChat);

document.getElementById("nameInput").addEventListener("keyup", function (e) {
    if (e.key === 'Enter')
        startChat();
});

function startChat() {
    let inputName = document.getElementById("nameInput").value.trim();

    if (inputName !== "") {
        username = inputName;
    }

    // 로그인 창 숨김, 채팅창 띄우기
    document.getElementById("loginScreen").style.display = "none";
    document.getElementById("chatScreen").style.display = "block";

    connectWebSocket();
}


// 2. 서버의 "/chat" 주소로 웹소켓 연결
function connectWebSocket() {
    ws = new WebSocket("ws://" + location.host + "/chat?name=" + encodeURIComponent(username));
    ws.onmessage = function (event) {
        let data = event.data;
        let box = document.getElementById("chatBox");

        // 1. 접속자 명단 업데이트인지 확인
        if (data.startsWith("[USERS]")) {
            let userStr = data.substring(7);
            document.getElementById("userList")
                .innerText = "접속자: " + userStr;

        } else {
            // 전달받은 JSON 텍스트를 JS 객체로 변환.
            let msgObj = JSON.parse(data);

            let sender = msgObj.sender;
            let content = msgObj.content;

            // 백엔드에서 넘어온 ISO 시간 데이터를 화면용으로 변환 ** 중요, 관심사의 분리로, 구현하면 해외의 인물과 연락할때 각 국가별로 시간을 프론트단에서 처리해서 표기하게됨.
            let rawTime = msgObj.sendTime;
            let dateObj = new Date(rawTime);

            // 기존 getFormatTime() 대신 서버에서 가져온 시간    -> locales 인자로 전달한 값에 따라 표기법이바뀜. 프론트단에 역할 위임하기에 적합.
            let timeStr = dateObj.toLocaleTimeString('ko-KR', {
                hour: 'numeric',
                minute: '2-digit',
                hour12: true
            });

            // 내가 보낸 메시지라면? (우측 정렬)
            if (sender === username) {
                box.innerHTML +=
                    "<div class='msg-row msg-me'>" +
                    "<span class='msg-time'>" +
                    timeStr + "</span>" +
                    "<div class='msg-bubble'>" +
                    content +
                    "</div></div>";

                // 남이 보낸 메시지라면? (좌측 정렬 + 이름 표시)
            } else {
                box.innerHTML +=
                    "<div class='msg-sender'>" +
                    sender +
                    "</div>" +
                    "<div class='msg-row msg-other'>" +
                    "<div class='msg-bubble'>" + content + "</div>" +
                    "<span class='msg-time'>" +
                    timeStr + "</span>" +
                    "</div>";
            }
            // 3. 메시지 추가 후 스크롤을 맨 아래로 내리기
            box.scrollTop = box.scrollHeight;
        }
    };
}

// 3. 채팅 전송 로직
document.getElementById("btnSend").addEventListener("click", sendMsg);

// 엔터를 쳐도 전송.
document.getElementById("msgInput").addEventListener("keyup", function (e) {
    if (e.key === 'Enter') sendMsg();
});


function sendMsg() {
    let input = document.getElementById("msgInput");
    let text = input.value.trim();

    if (text === "") {
        input.value = "";
        return;
    }

    let now = Date.now();
    if (now - lastSendTime < 1000) {
        alert("도배 방지: 1초 후에 다시 보내주세요!");
        return;
    }

    // 데이터 전송시에도 JS객체를 JSON 텍스트로 포장
    let sendData = {
        sender: username,
        content: text,
    }

    ws.send(JSON.stringify(sendData));
    input.value = "";
}

// 메시지 전송시 표기할 시간 추출
function getFormatTime() {
    let now = new Date();
    let ampm = now.getHours() < 12 ? "오전" : "오후";
    let h = now.getHours() % 12 || 12;
    let m = now.getMinutes();

    // 분이 한 자리수면 앞에 0 붙이기
    if (m < 10) m = "0" + m;
    return ampm + " " + h + ":" + m;
}