<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Galaxy^bbs - Forgot Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</head>
<body>

    <div class="container">
        <div class="logo-section">
            <img src="${pageContext.request.contextPath}/assets/img/logo.png" alt="Galaxy^bbs Logo" class="logo">
            <h1>Galaxy^bbs</h1>
            <p class="subtitle">Reset your password</p>
        </div>

        <% if (request.getAttribute("errorMsg") != null) { %>
            <p class="error-msg"><%= request.getAttribute("errorMsg") %></p>
        <% } else if (request.getAttribute("infoMsg") != null) { %>
            <p class="info-msg"><%= request.getAttribute("infoMsg") %></p>
        <% } %>

        <form action="${pageContext.request.contextPath}/auth/forgot-password" method="post">
            <div class="form-group">
                <label>Email</label>
                <input type="email" name="email" placeholder="Enter the email linked to your account" required autofocus>
            </div>

            <button type="submit" class="login-btn">Send reset link</button>
            
            <div class="footer-links">
                <a href="${pageContext.request.contextPath}/auth/login" class="forgot-password">Back to login</a>
                <p>Don't have an account? <a href="${pageContext.request.contextPath}/auth/register" class="create-account">Create an account</a></p>
            </div>
        </form>

        <div class="legal">
            <a href="#">Terms of Service</a> &bull; <a href="#">Privacy Policy</a>
        </div>
    </div>

</body>
</html>

