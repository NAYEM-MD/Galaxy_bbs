<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="util.TimeAgoUtil" %>
<%@ page import="model.Post" %>
<%@ page import="model.Comment" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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

            <c:choose>
            <c:when test="${not empty post}">
            <div class="detail-post-card">
                <div class="vote-column">
                    <div class="vote-arrow up ${post.userVote == 1 ? 'active' : ''}" onclick="vote(${post.id}, 1, this)">⬆</div>
                    <div class="vote-count">${post.voteCount}</div>
                    <div class="vote-arrow down ${post.userVote == -1 ? 'active' : ''}" onclick="vote(${post.id}, -1, this)">⬇</div>
                </div>
                <div class="detail-content">
                    <div class="post-meta">
                        <span class="community-name"><c:out value="${(empty post.tagName or fn:toLowerCase(post.tagName) == 'general') ? post.authorName : post.tagName}"/></span>
                        <span class="posted-by">Posted by u/<c:out value="${post.authorName}"/> • <% Post _p = (Post)request.getAttribute("post"); out.print(_p != null && _p.getCreatedAt() != null ? TimeAgoUtil.format(_p.getCreatedAt()) : ""); %></span>
                    </div>
                    <h1 class="post-title"><c:out value="${post.title}"/></h1>
                    <div class="post-body-text">
                        <c:out value="${post.content}"/>
                    </div>
                    <c:if test="${not empty post.mediaUrl}">
                    <div class="post-media" style="margin-top: 15px;">
                        <c:if test="${post.mediaType == 'image'}">
                            <img src="<c:out value="${post.mediaUrl}"/>" style="max-width: 100%; border-radius: 4px;">
                        </c:if>
                        <c:if test="${post.mediaType == 'video'}">
                            <video src="<c:out value="${post.mediaUrl}"/>" controls style="max-width: 100%; border-radius: 4px;"></video>
                        </c:if>
                    </div>
                    </c:if>

                    <c:if test="${post.mediaType == 'poll' and not empty post.poll}">
                    <div class="poll-container" id="poll-${post.poll.id}" style="background-color: #d1ecf1; border-radius: 10px; padding: 20px; margin-top: 15px; color: #0c5460;">
                        <h3 style="margin-top: 0; margin-bottom: 15px; font-size: 1.2em; font-weight: bold;"><c:out value="${post.poll.question}"/></h3>
                        <div class="poll-options">
                            <c:forEach var="option" items="${post.poll.options}">
                                <c:set var="percent" value="${post.poll.totalVotes > 0 ? (option.voteCount * 100 / post.poll.totalVotes) : 0}"/>
                                <div class="poll-option-display" onclick="votePoll(${post.poll.id}, ${option.id})" style="position: relative; margin-bottom: 10px; height: 40px; background-color: #ffffff; border-radius: 5px; cursor: pointer; overflow: hidden; display: flex; align-items: center;">
                                    <div class="poll-progress" id="progress-${option.id}" style="position: absolute; left: 0; top: 0; height: 100%; width: ${percent}%; background-color: #bee5eb; z-index: 1; transition: width 0.3s ease;"></div>
                                    <span style="position: relative; z-index: 2; padding-left: 15px; font-weight: 500; flex: 1;"><c:out value="${option.optionText}"/></span>
                                    <span class="poll-percentage" id="percent-${option.id}" style="position: relative; z-index: 2; padding-right: 15px; font-weight: bold;">${percent}%</span>
                                </div>
                            </c:forEach>
                        </div>
                        <div class="poll-footer" style="margin-top: 15px; font-size: 0.9em; display: flex; align-items: center; gap: 10px;">
                            <span class="live-indicator" style="width: 8px; height: 8px; background-color: #28a745; border-radius: 50%; display: inline-block;"></span>
                            <span id="total-votes-${post.poll.id}">Live | ${post.poll.totalVotes} votes</span>
                        </div>
                    </div>
                    </c:if>

                    <div class="post-footer">
                        <div class="footer-btn">💬 ${not empty comments ? fn:length(comments) : 0} Comments</div>
                    </div>
                </div>
            </div>

            <div class="comment-section">
                <c:if test="${not empty sessionScope.user}">
                <div class="comment-input-area">
                    <div class="comment-as">
                        Comment as <a href="#"><c:out value="${sessionScope.user.username}"/></a>
                    </div>
                    <form action="${pageContext.request.contextPath}/comments/create" method="post">
                        <input type="hidden" name="postId" value="${post.id}">
                        <textarea name="content" class="comment-textarea" placeholder="What are your thoughts?"></textarea>
                        <div class="comment-actions">
                            <div class="buttons">
                                <button type="submit" class="btn"><span></span><p data-start="good luck!" data-text="Post Now!" data-title="Comment"></p></button>
                            </div>
                        </div>
                    </form>
                </div>
                </c:if>
                <c:if test="${empty sessionScope.user}">
                    <p style="margin-bottom: 20px;"><a href="${pageContext.request.contextPath}/auth/login" style="color:#0079d3;">Log in</a> to comment.</p>
                </c:if>

                <div class="comments-list">
                    <c:forEach var="comment" items="${comments}">
                    <div class="comment" id="comment-${comment.id}">
                        <div class="comment-header">
                            <span class="comment-author">u/<c:out value="${comment.authorName}"/></span>
                            <span class="comment-time"><% Comment _c = (Comment)pageContext.findAttribute("comment"); out.print(_c != null && _c.getCreatedAt() != null ? TimeAgoUtil.format(_c.getCreatedAt()) : ""); %></span>
                        </div>
                        <div class="comment-body">
                            <c:out value="${comment.content}"/>
                        </div>
                    </div>
                    </c:forEach>
                </div>
            </div>
            </c:when>
            <c:otherwise>
                <p>Post not found.</p>
            </c:otherwise>
            </c:choose>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/vote.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/poll.js"></script>
</body>
</html>
