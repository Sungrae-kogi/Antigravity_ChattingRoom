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

// 이전 메시지의 날짜를 기억하고 있을 전역 변수
let lastMessageDate = "";

// 넘어온 메시지를 화면에 표기해주는 함수.
function renderMessage(msgObj){

    let box = document.getElementById("chatBox");

    // 백엔드에서 넘어온 ISO 시간 데이터를 화면용으로 변환 ** 중요, 관심사의 분리로, 구현하면 해외의 인물과 연락할때 각 국가별로 시간을 프론트단에서 처리해서 표기하게됨.
    let rawTime = msgObj.sendTime;
    let dateObj = new Date(rawTime);

    // 기존 getFormatTime() 대신 서버에서 가져온 시간    -> locales 인자로 전달한 값에 따라 표기법이바뀜. 프론트단에 역할 위임하기에 적합.
    let currentDateStr = dateObj.toLocaleDateString('ko-KR');

    // 새로운 메시지가 도착 -> 날짜가 바뀌었는가?
    if(lastMessageDate !== currentDateStr){
        let displayDate = dateObj.toLocaleDateString('ko-KR', {
            month: 'long',
            day: 'numeric'
        });

        box.innerHTML += "<div class='date-divider'>" + "<span>" + displayDate + "</span>" + "</div>";

        lastMessageDate = currentDateStr;
    }

    // 날짜가 바뀐게 아니라면 -> 기존 시간 반환
    let timeStr = dateObj.toLocaleTimeString('ko-KR', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });

    let sender = msgObj.sender;
    let content = msgObj.content;

    // 만약 이미지 경로라면 img 태그로 덮어씌운다.
    let displayContent = content;

    if (content.startsWith("/uploads/")){
        displayContent = "<img src='" + content + "' style='max-width: 200px; " + "border-radius: 8px;'>";
    }

    // 내가 보낸 메시지라면? (우측 정렬)
    if (sender === username) {
        box.innerHTML +=
            "<div class='msg-row msg-me'>" +
            "<span class='msg-time'>" +
            timeStr + "</span>" +
            "<div class='msg-bubble'>" +
            displayContent +
            "</div></div>";

        // 남이 보낸 메시지라면? (좌측 정렬 + 이름 표시)
    } else {
        box.innerHTML +=
            "<div class='msg-sender'>" +
            sender +
            "</div>" +
            "<div class='msg-row msg-other'>" +
            "<div class='msg-bubble'>" + displayContent + "</div>" +
            "<span class='msg-time'>" +
            timeStr + "</span>" +
            "</div>";
    }
    // 3. 메시지 추가 후 스크롤을 맨 아래로 내리기
    box.scrollTop = box.scrollHeight;
}

// 2. 서버의 "/chat" 주소로 웹소켓 연결
function connectWebSocket() {
    ws = new WebSocket("ws://" + location.host + "/chat?name=" + encodeURIComponent(username));

    // 모듈화 리펙토링 부분. onmessage 함수안에 대량의 로직이 들어간 상황.
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

            // 파이프라인 분리 : 모듈화
            renderMessage(msgObj);
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

document.getElementById('imageInput').addEventListener('change', function() {
    let file = this.files[0];
    if (!file) return;

    // 서버 전송 전 사전 용량 차단
    const MAX_SIZE = 5 * 1024 * 1024;

    if (file.size > MAX_SIZE) {
        alert("파일 용량은 5MB를 초과할 수 없습니다.");
        this.value(''); //입력창 초기화
        return; // 함수를 즉시 종료, 서버 전송 x
    }

    // HTTP 통신을 위한 FormData를 생성
    let formData = new FormData();
    formData.append("file", file);

    // 작성한 백엔드 창구로 사진을 전송 -> fetch 를 사용
    fetch('/api/upload/image', {
        method: 'POST',
        body: formData
    }).then(async response => {
        // fetch 는 인터넷이 끊기지 않는 이상 서버 에러를 받아도 통신은 성공했다 하고 catch가 아닌 then으로 진입을하므로, flag 처리해야한다.
        if(!response.ok){
            // 백엔드에서 만든 JSON 상자를 연다.
            let errorData = await response.json();

            // 상자 안의 error를 throw
            throw new Error(errorData.messages);
        }
        return response.text();
    }).then(imageUrl => {
            //서버 저장이 끝나고 URL이 돌아오면, 웹소켓을 통해 채팅방에 URL을 전송
            let msgObj = {
                sender: username,
                content: imageUrl,
                messageType: "IMAGE"    // 텍스트와 구분하기 위한 꼬리표
            };
            ws.send(JSON.stringify(msgObj));
        }).catch(error => alert("사진 업로드에 실패했습니다."));

    // 전송 후 입력창 초기화
    this.value = '';
})