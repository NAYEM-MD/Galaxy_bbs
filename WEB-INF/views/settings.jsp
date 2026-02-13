<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${empty user}"><c:redirect url="${pageContext.request.contextPath}/auth/login"/></c:if>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Settings - Galaxy^bbs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/settings.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</head>
<body>

    <jsp:include page="/common/header.jsp" />

    <div class="settings-container">
        <h1 class="settings-title">Settings</h1>

        <c:if test="${not empty message}">
        <p class="settings-message"><c:out value="${message}"/></p>
        </c:if>
        <c:if test="${not empty error}">
        <p class="settings-error"><c:out value="${error}"/></p>
        </c:if>

        <div class="settings-section">
            <h2 class="settings-section-title">Change Email Address</h2>
            <form method="post" action="${pageContext.request.contextPath}/settings" class="settings-form">
                <input type="hidden" name="action" value="updateEmail">
                <div class="form-group">
                    <label for="newEmail">New email</label>
                    <input type="email" id="newEmail" name="newEmail" value="<c:out value="${user.email}"/>" placeholder="Enter new email" required class="settings-input">
                </div>
                <button type="submit" class="btn-primary settings-btn">Update Email</button>
            </form>
        </div>

        <div class="settings-section">
            <h2 class="settings-section-title">Username</h2>
            <p class="settings-readonly"><c:out value="${user.username}"/> <span class="settings-hint">(cannot be changed)</span></p>
        </div>

        <div class="settings-section">
            <h2 class="settings-section-title">Change Password</h2>
            <form method="post" action="${pageContext.request.contextPath}/settings" class="settings-form">
                <input type="hidden" name="action" value="changePassword">
                <div class="form-group">
                    <label for="oldPassword">Current password</label>
                    <input type="password" id="oldPassword" name="oldPassword" placeholder="Enter current password" required class="settings-input" autocomplete="current-password">
                </div>
                <div class="form-group">
                    <label for="newPassword">New password</label>
                    <input type="password" id="newPassword" name="newPassword" placeholder="Enter new password (if current is correct)" required class="settings-input" autocomplete="new-password">
                </div>
                <button type="submit" class="btn-primary settings-btn">Change Password</button>
            </form>
        </div>

        <div class="settings-section settings-section-danger">
            <h2 class="settings-section-title">Delete Account</h2>
            <p class="settings-warning">This will permanently delete your account and all your posts and comments. This cannot be undone.</p>
            <form method="post" action="${pageContext.request.contextPath}/settings" class="settings-form" onsubmit="return confirm('Are you sure? This cannot be undone.');">
                <input type="hidden" name="action" value="deleteAccount">
                <div class="form-group">
                    <label for="deletePassword">Enter your password to confirm</label>
                    <input type="password" id="deletePassword" name="deletePassword" placeholder="Same as login password" required class="settings-input" autocomplete="current-password">
                </div>
                <button type="submit" class="btn-danger settings-btn">Delete Account</button>
            </form>
        </div>
    </div>

</body>
</html>
