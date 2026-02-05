<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Post" %>
<%@ page import="model.Comment" %>
<%@ page import="model.User" %>
<%
    User profileUser = (User) request.getAttribute("profileUser");
    List<Post> posts = (List<Post>) request.getAttribute("posts");
    List<Comment> comments = (List<Comment>) request.getAttribute("comments");
    String tab = (String) request.getAttribute("tab");
    if (tab == null) tab = "posts";
    User currentUser = (User) session.getAttribute("user");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= profileUser != null ? profileUser.getUsername() : "" %> - Galaxy^bbs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/feed.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</head>
<body>

    <jsp:include page="/common/header.jsp" />

    <div class="profile-container">
        <% if (profileUser != null) { %>
        <div class="profile-header">
            <h1 class="profile-username"><%= profileUser.getUsername() %></h1>
            <div class="profile-tabs">
                <a href="${pageContext.request.contextPath}/profile?username=<%= profileUser.getUsername() %>&tab=posts" class="profile-tab <%= "posts".equals(tab) ? "active" : "" %>">My Posts</a>
                <a href="${pageContext.request.contextPath}/profile?username=<%= profileUser.getUsername() %>&tab=comments" class="profile-tab <%= "comments".equals(tab) ? "active" : "" %>">My Comments</a>
            </div>
        </div>

        <div class="profile-content">
            <% if ("posts".equals(tab)) { %>
            <div class="profile-posts">
                <% if (posts != null && !posts.isEmpty()) { %>
                    <% for (Post post : posts) { %>
                <div class="post-card" data-href="${pageContext.request.contextPath}/posts/detail?id=<%= post.getId() %>">
                    <div class="vote-column">
                        <div class="vote-arrow up <%= post.getUserVote() == 1 ? "active" : "" %>" onclick="vote(<%= post.getId() %>, 1, this); event.stopPropagation();">⬆</div>
                        <div class="vote-count"><%= post.getVoteCount() %></div>
                        <div class="vote-arrow down <%= post.getUserVote() == -1 ? "active" : "" %>" onclick="vote(<%= post.getId() %>, -1, this); event.stopPropagation();">⬇</div>
                    </div>
                    <div class="post-content">
                        <% if (currentUser != null && currentUser.getId() == post.getUserId()) { %>
                        <div class="post-menu">
                            <button type="button" class="post-menu-trigger" aria-label="Post options" aria-haspopup="true" aria-expanded="false">⋯</button>
                            <div class="post-menu-dropdown" role="menu" aria-hidden="true">
                                <form method="post" action="${pageContext.request.contextPath}/posts/delete" class="post-menu-form">
                                    <input type="hidden" name="id" value="<%= post.getId() %>">
                                    <button type="submit" class="post-menu-item post-menu-item-delete" role="menuitem">Delete Post</button>
                                </form>
                            </div>
                        </div>
                        <% } %>
                        <div class="post-meta">
                            <span class="community-name">r/<%= post.getTagName() != null ? post.getTagName() : "general" %></span>
                            <span class="posted-by">Posted by <%= post.getAuthorName() %></span>
                        </div>
                        <h3 class="post-title"><%= post.getTitle() %></h3>
                        <div class="post-preview">
                            <%= post.getContent() != null ? (post.getContent().length() > 200 ? post.getContent().substring(0, 200) + "..." : post.getContent()) : "" %>
                        </div>
                        <% if (post.getMediaUrl() != null && !"poll".equals(post.getMediaType())) { %>
                        <div class="post-media">
                            <% if ("image".equals(post.getMediaType())) { %>
                                <img src="<%= post.getMediaUrl() %>" alt="" class="post-media-img">
                            <% } else if ("video".equals(post.getMediaType())) { %>
                                <video src="<%= post.getMediaUrl() %>" controls class="post-media-video"></video>
                            <% } %>
                        </div>
                        <% } %>
                        <div class="post-footer">
                            <div class="footer-btn">💬 Comments</div>
                        </div>
                    </div>
                </div>
                    <% } %>
                <% } else { %>
                <div class="profile-empty">No posts yet.</div>
                <% } %>
            </div>
            <% } else { %>
            <div class="profile-comments">
                <% if (comments != null && !comments.isEmpty()) { %>
                    <% for (Comment comment : comments) { %>
                <div class="comment-card">
                    <div class="comment-card-meta">
                        <a href="${pageContext.request.contextPath}/posts/detail?id=<%= comment.getPostId() %>" class="comment-post-link"><%= comment.getPostTitle() != null ? comment.getPostTitle() : "Post" %></a>
                    </div>
                    <div class="comment-card-body"><%= comment.getContent() != null ? comment.getContent() : "" %></div>
                    <div class="comment-card-time"><%= comment.getCreatedAt() != null ? comment.getCreatedAt() : "" %></div>
                </div>
                    <% } %>
                <% } else { %>
                <div class="profile-empty">No comments yet.</div>
                <% } %>
            </div>
            <% } %>
        </div>
        <% } else { %>
        <div class="profile-empty">User not found.</div>
        <% } %>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/vote.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/post-menu.js"></script>
</body>
</html>
