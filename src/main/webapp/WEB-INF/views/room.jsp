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
<div class="chat-header-bar" style="display:flex; justify-content:space-between; align-items:center; padding:10px 20px; background:#fff; border-bottom:1px solid #ddd;">
    <!-- 💡 변경 포인트: 세션 대신 Spring Security가 들고 있는 유저 정보(Principal)에서 이름 꺼내기! -->
    <div><strong>환영합니다, ${pageContext.request.userPrincipal.name}님!</strong></div>
    <a href="/api/auth/logout" class="btn btn-sm btn-outline-danger">로그아웃</a>
</div>


<div class="chat-wrap" id="chatScreen">
    <div class="header">SK 사내 익명 톡</div>
    <!-- Store username in a hidden element for chat.js to pick up -->
    <input type="hidden" id="sessionUsername" value="${pageContext.request.userPrincipal.name}" />
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