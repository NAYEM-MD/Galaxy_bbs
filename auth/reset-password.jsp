<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Galaxy^bbs - Reset Password</title>
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
            <p class="subtitle">Choose a new password</p>
        </div>

        <c:if test="${not empty errorMsg}">
            <p class="error-msg"><c:out value="${errorMsg}"/></p>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/reset-password" method="post">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <input type="hidden" name="token" value="${not empty token ? token : param.token}">

            <div class="form-group">
                <label>New Password</label>
                <input type="password" name="password" placeholder="Enter a new password" required>
            </div>

            <div class="form-group">
                <label>Confirm Password</label>
                <input type="password" name="confirmPassword" placeholder="Re-enter your new password" required>
            </div>

            <button type="submit" class="login-btn">Reset password</button>

            <div class="footer-links">
                <a href="${pageContext.request.contextPath}/auth/login" class="forgot-password">Back to login</a>
            </div>
        </form>

        <div class="legal">
            <a href="#">Terms of Service</a> &bull; <a href="#">Privacy Policy</a>
        </div>
    </div>

</body>
</html>

