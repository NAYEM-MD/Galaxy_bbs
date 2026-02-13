<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Notifications - Galaxy^bbs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=2">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
    <style>
        .notifications-container {
            max-width: 800px;
            margin: 20px auto;
            background: var(--glass-bg);
            backdrop-filter: blur(var(--glass-blur));
            -webkit-backdrop-filter: blur(var(--glass-blur));
            border: 1px solid var(--glass-border);
            border-radius: 8px;
            box-shadow: 0 2px 12px var(--glass-shadow);
        }
        .notifications-header {
            padding: 15px 20px;
            border-bottom: 1px solid var(--border-color);
            color: var(--text-primary);
        }
        .notification-item {
            padding: 15px 20px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: flex-start;
            gap: 15px;
            transition: background 0.2s;
        }
        .notification-item:hover {
            background-color: var(--bg-hover);
        }
        .notification-item.unread {
            background-color: var(--bg-input);
        }
        .notification-icon {
            font-size: 24px;
        }
        .notification-content {
            flex: 1;
            color: var(--text-primary);
        }
        .notification-preview {
            color: var(--text-secondary);
            margin-top: 5px;
            font-style: italic;
        }
        .notification-time {
            color: var(--text-secondary);
            font-size: 12px;
            margin-top: 5px;
        }
        .notification-link {
            text-decoration: none;
            color: inherit;
            display: block;
        }
        .notifications-empty {
            padding: 20px;
            text-align: center;
            color: var(--text-secondary);
        }
    </style>
</head>
<body>

    <jsp:include page="/common/header.jsp" />

    <div class="notifications-container">
        <div class="notifications-header">
            <h2>Notifications</h2>
        </div>
        
        <div class="notifications-list">
            <c:choose>
            <c:when test="${not empty notifications}">
            <c:forEach var="n" items="${notifications}">
            <a href="${pageContext.request.contextPath}/posts/detail?id=${n.postId}#comment-${n.commentId}" class="notification-link">
                <div class="notification-item ${!n.read ? 'unread' : ''}">
                    <div class="notification-icon">💬</div>
                    <div class="notification-content">
                        <div>
                            <strong><c:out value="${n.actorName}"/></strong> commented on your post 
                            <strong>"<c:out value="${n.postTitle}"/>"</strong>
                        </div>
                        <div class="notification-preview">
                            "<c:choose><c:when test="${fn:length(n.commentContent) > 50}"><c:out value="${fn:substring(n.commentContent, 0, 50)}"/>...</c:when><c:otherwise><c:out value="${n.commentContent}"/></c:otherwise></c:choose>"
                        </div>
                        <div class="notification-time"><c:out value="${n.createdAt}"/></div>
                    </div>
                </div>
            </a>
            </c:forEach>
            </c:when>
            <c:otherwise>
            <div class="notifications-empty">
                No notifications yet.
            </div>
            </c:otherwise>
            </c:choose>
        </div>
    </div>

</body>
</html>