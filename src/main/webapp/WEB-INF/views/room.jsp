<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="spring"
           uri="http://www.springframework.org/tags" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>SK 메신저</title>
<%--    <link rel="stylesheet" href="/css/chat.css">--%>
    <spring:url value="/css/chat.css"
                var="cssUrl" />
    <link rel="stylesheet"
          href="${cssUrl}" />
</head>
<body>
<div class="chat-wrap" id="loginScreen">
    <div class="header">SK 사내 익명 톡</div>
    <div class="login-box">
        <h3 style="color:#333">닉네임을 설정해주세요</h3>

        <input type="text" id="nameInput" placeholder="ex) : 홍길동"/>
        <button class="btn-send" id="btnJoin" style="margin-top:10px; width: 100%; padding:12px;">
            입장하기
        </button>
    </div>
</div>


<div class="chat-wrap" id="chatScreen"
     style="display: none;">
    <div class="header">SK 사내 익명 톡</div>
    <div id="userList" class="user-list">
        접속자:
    </div>
    <div id="chatBox">
        <div id="observer-target" style="height: 1px;"></div>
    </div>
    <div class="input-wrap">
        <input type="file" id="imageInput" accept="image/*" style="display: none;">
        <button onclick="document.getElementById('imageInput').click()">📷 사진</button>
        <input type="text" id="msgInput"
               placeholder="메시지 입력..."/>
        <button class="btn-send" id="btnSend">
            전송
        </button>
    </div>
</div>

<%--<script src="/js/chat.js"></script>--%>
<spring:url value="/js/chat.js"
            var="jsUrl" />
<script src="${jsUrl}"></script>

<spring:url value="/js/chat-history.js" var="historyJsUrl" />
<script src="${historyJsUrl}"></script>
</body>
</html>