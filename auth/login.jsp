<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Galaxy^bbs - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</head>
<body>

    <div class="container">
        <div class="logo-section">
            <img src="${pageContext.request.contextPath}/assets/img/logo.png" alt="Galaxy^bbs Logo" class="logo">
            <h1>Galaxy^bbs</h1>
            <p class="subtitle">Welcome back! Please login to continue</p>
        </div>

        <c:if test="${not empty errorMsg}">
            <p class="error-msg"><c:out value="${errorMsg}"/></p>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <div class="form-group">
                <label>Email</label>
                <input type="text" name="email" placeholder="Enter your email" required autofocus>
            </div>

            <div class="form-group">
                <label>Password</label>
                <div class="password-wrapper">
                    <input type="password" name="password" id="password" placeholder="Enter your password" required>
                    <button type="button" class="toggle-password" onclick="togglePassword('password', this)">
                        <svg class="eye-icon" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                    </button>
                </div>
            </div>

            <button type="submit" class="login-btn">Log In</button>
            
            <div class="footer-links">
                <a href="${pageContext.request.contextPath}/auth/forgot-password.jsp" class="forgot-password">Forgot password?</a>
                <p>Don't have an account? <a href="${pageContext.request.contextPath}/auth/register" class="create-account">Create an account</a></p>
            </div>
        </form>
        
        <div class="legal">
            <a href="#">Terms of Service</a> &bull; <a href="#">Privacy Policy</a>
        </div>
    </div>

</body>
</html>
