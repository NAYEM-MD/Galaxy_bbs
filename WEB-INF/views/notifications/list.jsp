<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Notification" %>
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
            <%
            List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
            if (notifications != null && !notifications.isEmpty()) {
                for (Notification n : notifications) {
            %>
            <a href="${pageContext.request.contextPath}/posts/detail?id=<%= n.getPostId() %>#comment-<%= n.getCommentId() %>" class="notification-link">
                <div class="notification-item <%= !n.isRead() ? "unread" : "" %>">
                    <div class="notification-icon">💬</div>
                    <div class="notification-content">
                        <div>
                            <strong><%= n.getActorName() %></strong> commented on your post 
                            <strong>"<%= n.getPostTitle() %>"</strong>
                        </div>
                        <div class="notification-preview">
                            "<%= n.getCommentContent() != null && n.getCommentContent().length() > 50 ? n.getCommentContent().substring(0, 50) + "..." : n.getCommentContent() %>"
                        </div>
                        <div class="notification-time"><%= n.getCreatedAt() %></div>
                    </div>
                </div>
            </a>
            <%
                }
            } else {
            %>
            <div class="notifications-empty">
                No notifications yet.
            </div>
            <% } %>
        </div>
    </div>

</body>
</html>