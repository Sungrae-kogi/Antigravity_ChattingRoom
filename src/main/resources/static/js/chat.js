// 1. Session Username
let username = document.getElementById("sessionUsername").value || "익명";
let ws;
let lastSendTime = 0;

// 입장 로직 (Auto-start)
window.addEventListener("DOMContentLoaded", () => {
    connectWebSocket();
    if (window.startChatObserver) {
        window.startChatObserver();
    }
});

// 이전 메시지의 날짜를 기억하고 있을 전역 변수
let lastMessageDate = "";

// ★ 추가된 부분: 두 JS 파일이 공유할 말풍선 조립 공장
window.createMessageBubble = function(msgObj) {
    let rawTime = msgObj.sendTime || Date.now();
    let dateObj = new Date(rawTime);
    let timeStr = dateObj.toLocaleTimeString('ko-KR', {
        hour: 'numeric', minute: '2-digit', hour12: true
    });

    // 메시지가 파일 업로드일 경우 -> 전처리
    let displayContent = msgObj.content;
    if (displayContent.startsWith("/uploads/")) {
        displayContent = "<img src='" + displayContent +
            "' style='max-width: 200px; border-radius: 8px;'>";
    }

    let wrapper = document.createElement("div");

    // 메시지 블럭 HTML 태그작성
    // ***** 관심사의 분리 가 필요, -> 데이터를 다루는 로직(JS)와 화면을 그리는 껍데기 (HTML)은 물리적으로 분리되어야 한다. ************     -> REACT, VUE가 나오게된것.
    if (msgObj.sender === username) {
        wrapper.className = "msg-wrapper-me";
        wrapper.innerHTML =
            "<div class='msg-row msg-me'>" +
            "<span class='msg-time'>" + timeStr + "</span>" +
            "<div class='msg-bubble'>" + displayContent +
            "</div></div>";
    } else {
        wrapper.className = "msg-wrapper-other";
        wrapper.innerHTML =
            "<div class='msg-sender'>" + msgObj.sender + "</div>" +
            "<div class='msg-row msg-other'>" +
            "<div class='msg-bubble'>" + displayContent + "</div>" +
            "<span class='msg-time'>" + timeStr + "</span></div>";
    }
    return wrapper;
};

// 넘어온 메시지를 화면에 표기해주는 함수 (수정됨)
function renderMessage(msgObj) {
    let box = document.getElementById("chatBox");
    let rawTime = msgObj.sendTime || Date.now();
    let dateObj = new Date(rawTime);
    let currentDateStr = dateObj.toLocaleDateString('ko-KR');

    if (lastMessageDate !== currentDateStr) {
        let displayDate = dateObj.toLocaleDateString('ko-KR', {
            month: 'long', day: 'numeric'
        });

        // innerHTML 대신 DOM 객체 생성 방식으로 통일
        let dateDiv = document.createElement("div");
        dateDiv.className = "date-divider";
        dateDiv.innerHTML = "<span>" + displayDate + "</span>";
        box.appendChild(dateDiv);

        lastMessageDate = currentDateStr;
    }

    // ★ 문자열 이어붙이기 대신 공통 공장에서 만든 객체를 삽입
    let bubble = window.createMessageBubble(msgObj);
    box.appendChild(bubble);

    // 메시지 추가 후 스크롤을 맨 아래로 내리기
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

document.getElementById('imageInput').addEventListener('change', function () {
    let file = this.files[0];
    if (!file) return;

    // 서버 전송 전 사전 용량 차단
    const MAX_SIZE = 5 * 1024 * 1024;

    if (file.size > MAX_SIZE) {
        alert("파일 용량은 5MB를 초과할 수 없습니다.");
        this.value=''; //입력창 초기화
        return; // 함수를 즉시 종료, 서버 전송 x
    }

    // 로딩 UI 및 다중 클릭 방지
    let inputBtn = this;
    let chatBox = document.getElementById("chatBox");

    // 버튼 잠금 (중복 클릭 방지)
    inputBtn.disabled = true;

    // 임시 로딩 말풍선을 메모리에 조립
    let loadingDiv = document.createElement("div");
    loadingDiv.className = "msg-row msg-me";
    loadingDiv.id = "temp-loading";
    loadingDiv.innerHTML = "<div class='msg-bubble'>" + "<div class='loader'></div>" + "사진 업로드 중...</div>";

    chatBox.appendChild(loadingDiv);
    chatBox.scrollTop = chatBox.scrollHeight;

    // HTTP 통신을 위한 FormData를 생성
    let formData = new FormData();
    formData.append("file", file);

    // 작성한 백엔드 창구로 사진을 전송 -> fetch 를 사용
    fetch('/api/upload/image', {
        method: 'POST',
        body: formData
    }).then(async response => {
        // fetch 는 인터넷이 끊기지 않는 이상 서버 에러를 받아도 통신은 성공했다 하고 catch가 아닌 then으로 진입을하므로, flag 처리해야한다.
        if (!response.ok) {
            // 백엔드에서 만든 JSON 상자를 연다.
            let errorData = await response.json();

            // 상자 안의 error를 throw
            throw new Error(errorData.messages);
        }
        return response.text();
    }).then(imageUrl => {
        document.getElementById("temp-loading").remove();

        //서버 저장이 끝나고 URL이 돌아오면, 웹소켓을 통해 채팅방에 URL을 전송
        let msgObj = {
            sender: username,
            content: imageUrl,
            messageType: "IMAGE"    // 텍스트와 구분하기 위한 꼬리표
        };
        ws.send(JSON.stringify(msgObj));
    }).catch(error => {
        // 실패 : 로딩창 지우고 에러 메시지 -> 전송이 실패인데? loading이 떠있다? -> 삭제
        let loadingEl = document.getElementById("temp-loading");
        if (loadingEl) {
            loadingEl.remove();
        }

        alert(error.message || "사진 업로드에 실패했습니다.");
    })
        .finally(() => {
            //통신 종료 : 어떤 결과든 버튼 원상복구
            inputBtn.disabled = false;
            inputBtn.value = '';
        });

    // 전송 후 입력창 초기화
    this.value = '';
})