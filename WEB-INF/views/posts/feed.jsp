<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Galaxy^bbs</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/feed.css">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js"></script>
</head>
<body>

    <jsp:include page="/common/header.jsp" />

    <div class="main-container">
        <!-- Feed Column -->
        <div class="feed-column">
            <c:if test="${not empty searchQuery and fn:trim(searchQuery) != ''}">
                <div class="post-card">
                    <div class="post-content" style="padding: 12px 16px; border-bottom: 1px solid rgba(128,128,128,0.3); font-size: 14px;">
                        Showing results for "<strong><c:out value="${searchQuery}"/></strong>"
                    </div>
                </div>
            </c:if>

            <c:choose>
            <c:when test="${not empty posts}">
            <c:forEach var="post" items="${posts}">
            <div class="post-card" data-href="${pageContext.request.contextPath}/posts/detail?id=${post.id}">
                <div class="vote-column">
                    <div class="vote-arrow up ${post.userVote == 1 ? 'active' : ''}" onclick="vote(${post.id}, 1, this); event.stopPropagation();">⬆</div>
                    <div class="vote-count">${post.voteCount}</div>
                    <div class="vote-arrow down ${post.userVote == -1 ? 'active' : ''}" onclick="vote(${post.id}, -1, this); event.stopPropagation();">⬇</div>
                </div>
                <div class="post-content">
                    <c:if test="${not empty sessionScope.user and sessionScope.user.id == post.userId}">
                    <div class="post-menu">
                        <button type="button" class="post-menu-trigger" aria-label="Post options" aria-haspopup="true" aria-expanded="false">⋯</button>
                        <div class="post-menu-dropdown" role="menu" aria-hidden="true">
                            <form method="post" action="${pageContext.request.contextPath}/posts/delete" class="post-menu-form">
                                <input type="hidden" name="id" value="${post.id}">
                                <button type="submit" class="post-menu-item post-menu-item-delete" role="menuitem">Delete Post</button>
                            </form>
                        </div>
                    </div>
                    </c:if>
                    <div class="post-meta">
                        <span class="community-name"><c:out value="${(empty post.tagName or fn:toLowerCase(post.tagName) == 'general') ? post.authorName : post.tagName}"/></span>
                        <span class="posted-by">Posted by <c:out value="${post.authorName}"/></span>
                    </div>
                    <h3 class="post-title"><c:out value="${post.title}"/></h3>
                    <div class="post-preview">
                        <c:choose><c:when test="${empty post.content}"></c:when><c:when test="${fn:length(post.content) > 200}"><c:out value="${fn:substring(post.content, 0, 200)}"/>...</c:when><c:otherwise><c:out value="${post.content}"/></c:otherwise></c:choose>
                    </div>
                    <c:if test="${not empty post.mediaUrl}">
                    <div class="post-media" style="margin-top: 10px;">
                        <c:if test="${post.mediaType == 'image'}">
                            <img src="<c:out value="${post.mediaUrl}"/>" style="max-width: 100%; max-height: 500px; border-radius: 4px;">
                        </c:if>
                        <c:if test="${post.mediaType == 'video'}">
                            <video src="<c:out value="${post.mediaUrl}"/>" controls style="max-width: 100%; max-height: 500px; border-radius: 4px;"></video>
                        </c:if>
                    </div>
                    </c:if>

                    <c:if test="${post.mediaType == 'poll' and not empty post.poll}">
                    <div class="poll-container" id="poll-${post.poll.id}" onclick="event.stopPropagation()">
                        <h3><c:out value="${post.poll.question}"/></h3>
                        <div class="poll-options">
                            <c:forEach var="option" items="${post.poll.options}">
                                <c:set var="percent" value="${post.poll.totalVotes > 0 ? (option.voteCount * 100 / post.poll.totalVotes) : 0}"/>
                                <div class="poll-option-display" onclick="votePoll(${post.poll.id}, ${option.id}); event.stopPropagation()">
                                    <div class="poll-progress" id="progress-${option.id}" style="width: ${percent}%;"></div>
                                    <span class="poll-option-text"><c:out value="${option.optionText}"/></span>
                                    <span class="poll-percentage" id="percent-${option.id}">${percent}%</span>
                                </div>
                            </c:forEach>
                        </div>
                        <div class="poll-footer">
                            <span class="live-indicator"></span>
                            <span id="total-votes-${post.poll.id}">Live | ${post.poll.totalVotes} votes</span>
                        </div>
                    </div>
                    </c:if>

                    <div class="post-footer">
                        <div class="footer-btn">💬 ${post.commentCount} Comments</div>
                    </div>
                </div>
            </div>
            </c:forEach>
            </c:when>
            <c:otherwise>
                <c:if test="${not empty searchQuery and fn:trim(searchQuery) != ''}">
                <div class="post-card">
                    <div class="post-content" style="padding: 20px; text-align: center;">
                        <p>No posts found for "<strong><c:out value="${searchQuery}"/></strong>".</p>
                    </div>
                </div>
                </c:if>
                <c:if test="${empty searchQuery or fn:trim(searchQuery) == ''}">
                <div class="post-card">
                    <div class="post-content" style="padding: 20px; text-align: center;">
                        <p>No posts yet. Be the first to create one!</p>
                    </div>
                </div>
                </c:if>
            </c:otherwise>
            </c:choose>
        </div>

    </div>

    <script src="${pageContext.request.contextPath}/assets/js/vote.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/poll.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/post-menu.js"></script>
</body>
</html>
