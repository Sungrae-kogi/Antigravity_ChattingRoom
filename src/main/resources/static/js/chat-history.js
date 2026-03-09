// 과거 내역 페이징 전용 자바스크립트 파일

let oldestChatId = null;
let isFetching = false;
let isEnd = false;

async function fetchOldChatHistory() {
    if (isFetching || isEnd) return;
    isFetching = true;

    // 첫 실행 호출 -> oldestChatId 가 null이므로 Param에 오지않았다.
    let url = '/api/chat/history?limit=50';
    if (oldestChatId !== null){
        url += '&lastId=' + oldestChatId;
    }

    try {
        let response = await fetch(url);
        if (!response.ok) throw new Error("API 호출 에러");

        let chatList = await response.json();

        if (chatList.length === 0){
            isEnd = true;
            return;
        }

        let chatBox = document.getElementById("chatBox");
        let target = document.getElementById("observer-target");

        chatList.forEach(chat => {
            //메인 JS에 있는 createMessageBubble 함수
            let bubble = createMessageBubble(chat);
            chatBox.insertBefore(bubble, target.nextSibling);
        });

        oldestChatId = chatList[chatList.length - 1].id;
    } catch (error) {
        console.error("로딩 실패:", error);
    } finally {
        isFetching = false;
    }
}

const observerOptions = {
    root: document.getElementById("chatBox"),
    threshold: 0.1
};

const observer = new IntersectionObserver((entries) => {
    // entries는 감시자가 지켜보고 있는 대상들의 목록, 지금은 대상이 하나이므로 entries[0]으로 확인.
    // isIntersecting ? -> 그 타겟이 사용자 화면 안에 들어왔는가?
    if (entries[0].isIntersecting) {
        fetchOldChatHistory();
    }
}, observerOptions);

window.startChatObserver = function(){
    let target = document.getElementById("observer-target");
    if(target){
        observer.observe(target);
    }
}