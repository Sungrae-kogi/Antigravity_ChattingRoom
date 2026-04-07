<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Sign In - SKTheme Chat</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { background-color: #f8f9fa; display: flex; justify-content: center; align-items: center; height: 100vh; }
        .auth-container { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
    </style>
</head>
<body>
    <div class="auth-container">
        <h2 class="text-center mb-4">SKTheme Chat</h2>
        
        <% if (request.getAttribute("message") != null) { %>
            <div class="alert alert-success">${message}</div>
        <% } %>
        
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger">${error}</div>
        <% } %>
        
        <!-- 💡 변경 포인트: 폼 데이터를 우리가 만든 컨트롤러 주소(/api/auth/login)로 쏘도록 변경! -->
        <form action="/api/auth/login" method="post">
            <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <input type="text" class="form-control" id="username" name="username" required>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <input type="password" class="form-control" id="password" name="password" required>
            </div>
            <button type="submit" class="btn btn-primary w-100 mb-3">Sign In</button>
        </form>
        <div class="text-center">
            <p>Don't have an account? <a href="/signup">Sign up</a></p>
        </div>
    </div>
</body>
</html>
