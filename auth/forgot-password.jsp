<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Galaxy^bbs - Forgot Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</head>
<body>

    <div class="container">
        <div class="logo-section">
            <img src="${pageContext.request.contextPath}/assets/img/logo.png" alt="Galaxy^bbs Logo" class="logo">
            <h1>Galaxy^bbs</h1>
            <p class="subtitle">Reset your password</p>
        </div>

        <c:if test="${not empty errorMsg}">
            <p class="error-msg"><c:out value="${errorMsg}"/></p>
        </c:if>
        <c:if test="${not empty infoMsg}">
            <p class="info-msg"><c:out value="${infoMsg}"/></p>
        </c:if>

        <form id="forgotForm" action="${pageContext.request.contextPath}/auth/forgot-password" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <div class="form-group">
                <label>Email</label>
                <input type="email" name="email" placeholder="Enter the email linked to your account" required autofocus>
            </div>

            <button id="forgotSubmit" type="submit" class="login-btn">Send reset link</button>
            
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

