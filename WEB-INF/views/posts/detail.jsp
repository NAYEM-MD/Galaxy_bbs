<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.Post" %>
<%@ page import="model.Comment" %>
<%@ page import="model.User" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Post Detail - Galaxy^bbs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/feed.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/post.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</head>
<body>

    <jsp:include page="/common/header.jsp" />

    <div class="post-detail-container">
        <div class="detail-left">
            <div class="post-header-bar">
                <div class="back-link" onclick="location.href='${pageContext.request.contextPath}/posts/feed'" style="cursor: pointer;">
                    ⬅ Back to feed
                </div>
            </div>

            <%
            Post post = (Post) request.getAttribute("post");
            if (post != null) {
            %>
            <div class="detail-post-card">
                <div class="vote-column">
                    <div class="vote-arrow up <%= post.getUserVote() == 1 ? "active" : "" %>" onclick="vote(<%= post.getId() %>, 1, this)">⬆</div>
                    <div class="vote-count"><%= post.getVoteCount() %></div>
                    <div class="vote-arrow down <%= post.getUserVote() == -1 ? "active" : "" %>" onclick="vote(<%= post.getId() %>, -1, this)">⬇</div>
                </div>
                <div class="detail-content">
                    <div class="post-meta">
                        <span class="community-name"><%= (post.getTagName() != null && !"general".equalsIgnoreCase(post.getTagName())) ? post.getTagName() : post.getAuthorName() %></span>
                        <span class="posted-by">Posted by u/<%= post.getAuthorName() %> • <%
    Date pa = post.getCreatedAt();
    if (pa != null) {
        long diff = System.currentTimeMillis() - pa.getTime();
        long sec = diff / 1000, min = sec / 60, hr = min / 60, day = hr / 24;
        if (day > 0) out.print(day + "d ago");
        else if (hr > 0) out.print(hr + "h ago");
        else if (min > 0) out.print(min + "m ago");
        else if (sec >= 5) out.print(sec + "s ago");
        else out.print("just now");
    } else out.print("");
%></span>
                    </div>
                    <h1 class="post-title"><%= post.getTitle() %></h1>
                    <div class="post-body-text">
                        <%= post.getContent() != null ? post.getContent() : "" %>
                    </div>
                    <% if (post.getMediaUrl() != null) { %>
                    <div class="post-media" style="margin-top: 15px;">
                        <% if ("image".equals(post.getMediaType())) { %>
                            <img src="<%= post.getMediaUrl() %>" style="max-width: 100%; border-radius: 4px;">
                        <% } else if ("video".equals(post.getMediaType())) { %>
                            <video src="<%= post.getMediaUrl() %>" controls style="max-width: 100%; border-radius: 4px;"></video>
                        <% } %>
                    </div>
                    <% } %>

                    <% if ("poll".equals(post.getMediaType()) && post.getPoll() != null) { 
                        int totalVotes = 0;
                        for (model.PollOption option : post.getPoll().getOptions()) {
                            totalVotes += option.getVoteCount();
                        }
                    %>
                    <div class="poll-container" id="poll-<%= post.getPoll().getId() %>" style="background-color: #d1ecf1; border-radius: 10px; padding: 20px; margin-top: 15px; color: #0c5460;">
                        <h3 style="margin-top: 0; margin-bottom: 15px; font-size: 1.2em; font-weight: bold;"><%= post.getPoll().getQuestion() %></h3>
                        <div class="poll-options">
                            <% for (model.PollOption option : post.getPoll().getOptions()) { 
                                int percent = totalVotes > 0 ? (int)((double)option.getVoteCount() / totalVotes * 100) : 0;
                            %>
                                <div class="poll-option-display" onclick="votePoll(<%= post.getPoll().getId() %>, <%= option.getId() %>)" style="position: relative; margin-bottom: 10px; height: 40px; background-color: #ffffff; border-radius: 5px; cursor: pointer; overflow: hidden; display: flex; align-items: center;">
                                    <div class="poll-progress" id="progress-<%= option.getId() %>" style="position: absolute; left: 0; top: 0; height: 100%; width: <%= percent %>%; background-color: #bee5eb; z-index: 1; transition: width 0.3s ease;"></div>
                                    <span style="position: relative; z-index: 2; padding-left: 15px; font-weight: 500; flex: 1;"><%= option.getOptionText() %></span>
                                    <span class="poll-percentage" id="percent-<%= option.getId() %>" style="position: relative; z-index: 2; padding-right: 15px; font-weight: bold;"><%= percent %>%</span>
                                </div>
                            <% } %>
                        </div>
                        <div class="poll-footer" style="margin-top: 15px; font-size: 0.9em; display: flex; align-items: center; gap: 10px;">
                            <span class="live-indicator" style="width: 8px; height: 8px; background-color: #28a745; border-radius: 50%; display: inline-block;"></span>
                            <span id="total-votes-<%= post.getPoll().getId() %>">Live | <%= totalVotes %> votes</span>
                        </div>
                    </div>
                    <% } %>

                    <div class="post-footer">
                        <div class="footer-btn">💬 <%= request.getAttribute("comments") != null ? ((List)request.getAttribute("comments")).size() : 0 %> Comments</div>
                    </div>
                </div>
            </div>

            <div class="comment-section">
                <% 
                User user = (User) session.getAttribute("user");
                if (user != null) {
                %>
                <div class="comment-input-area">
                    <div class="comment-as">
                        Comment as <a href="#"><%= user.getUsername() %></a>
                    </div>
                    <form action="${pageContext.request.contextPath}/comments/create" method="post">
                        <input type="hidden" name="postId" value="<%= post.getId() %>">
                        <textarea name="content" class="comment-textarea" placeholder="What are your thoughts?"></textarea>
                        <div class="comment-actions">
                            <div class="buttons">
                                <button type="submit" class="btn"><span></span><p data-start="good luck!" data-text="Post Now!" data-title="Comment"></p></button>
                            </div>
                        </div>
                    </form>
                </div>
                <% } else { %>
                    <p style="margin-bottom: 20px;"><a href="${pageContext.request.contextPath}/auth/login" style="color:#0079d3;">Log in</a> to comment.</p>
                <% } %>

                <div class="comments-list">
                    <%
                    List<Comment> comments = (List<Comment>) request.getAttribute("comments");
                    if (comments != null) {
                        for (Comment comment : comments) {
                    %>
                    <div class="comment" id="comment-<%= comment.getId() %>">
                        <div class="comment-header">
                            <span class="comment-author">u/<%= comment.getAuthorName() %></span>
                            <span class="comment-time"><%
    Date ca = comment.getCreatedAt();
    if (ca != null) {
        long diff = System.currentTimeMillis() - ca.getTime();
        long sec = diff / 1000, min = sec / 60, hr = min / 60, day = hr / 24;
        if (day > 0) out.print(day + "d ago");
        else if (hr > 0) out.print(hr + "h ago");
        else if (min > 0) out.print(min + "m ago");
        else if (sec >= 5) out.print(sec + "s ago");
        else out.print("just now");
    } else out.print("");
%></span>
                        </div>
                        <div class="comment-body">
                            <%= comment.getContent() %>
                        </div>
                    </div>
                    <%
                        }
                    }
                    %>
                </div>
            </div>
            <% } else { %>
                <p>Post not found.</p>
            <% } %>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/assets/js/vote.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/poll.js"></script>
</body>
</html>