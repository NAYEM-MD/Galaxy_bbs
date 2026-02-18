<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%-- Default tab for My Posts / My Comments --%>
<c:if test="${empty tab}"><c:set var="tab" value="posts" scope="request"/></c:if>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="csrf-token" content="${sessionScope.csrfToken}">
    <title><c:out value="${not empty profileUser ? profileUser.username : ''}"/> - Galaxy^bbs</title>
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
        <c:if test="${not empty profileUser}">
        <div class="profile-header">
            <h1 class="profile-username"><c:out value="${profileUser.username}"/></h1>

            <%-- Profile picture: DB image or default avatar (JSTL conditional rendering) --%>
            <div class="profile-avatar-wrap">
                <c:choose>
                    <c:when test="${not empty profileUser.profileImage}">
                        <img src="${pageContext.request.contextPath}/${profileUser.profileImage}" alt="Profile" class="profile-avatar" id="profileAvatarImg">
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/assets/img/default-avatar.svg" alt="Default avatar" class="profile-avatar" id="profileAvatarImg">
                    </c:otherwise>
                </c:choose>
            </div>

            <%-- Upload Photo (owner only) and View Photo (all users) --%>
            <div class="profile-actions">
                <c:if test="${not empty sessionScope.user and sessionScope.user.id == profileUser.id}">
                <form action="${pageContext.request.contextPath}/profile/upload" method="post" enctype="multipart/form-data" class="profile-upload-form" data-upload-url="${pageContext.request.contextPath}/profile/upload">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <label class="profile-photo-btn profile-photo-btn-upload">
                        <input type="file" name="profilePhoto" accept="image/*" class="profile-photo-input">
                        Upload Photo
                    </label>
                </form>
                </c:if>
                <button type="button" class="profile-photo-btn profile-photo-btn-view" id="viewPhotoBtn" aria-label="View photo">View Photo</button>
            </div>

            <%-- Tabs: My Posts | My Comments --%>
            <div class="profile-tabs">
                <a href="<c:url value='/profile'><c:param name='username' value='${profileUser.username}'/><c:param name='tab' value='posts'/></c:url>" class="profile-tab ${tab == 'posts' ? 'active' : ''}">My Posts</a>
                <a href="<c:url value='/profile'><c:param name='username' value='${profileUser.username}'/><c:param name='tab' value='comments'/></c:url>" class="profile-tab ${tab == 'comments' ? 'active' : ''}">My Comments</a>
            </div>
        </div>

        <div class="profile-content">
            <c:if test="${tab == 'posts'}">
            <div class="profile-posts">
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
                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                    <input type="hidden" name="id" value="${post.id}">
                                    <button type="submit" class="post-menu-item post-menu-item-delete" role="menuitem">Delete Post</button>
                                </form>
                            </div>
                        </div>
                        </c:if>
                        <div class="post-meta">
                            <span class="community-name">r/<c:out value="${empty post.tagName ? 'general' : post.tagName}"/></span>
                            <span class="posted-by">Posted by <c:out value="${post.authorName}"/></span>
                        </div>
                        <h3 class="post-title"><c:out value="${post.title}"/></h3>
                        <div class="post-preview">
                            <c:choose><c:when test="${empty post.content}"></c:when><c:when test="${fn:length(post.content) > 200}"><c:out value="${fn:substring(post.content, 0, 200)}"/>...</c:when><c:otherwise><c:out value="${post.content}"/></c:otherwise></c:choose>
                        </div>
                        <c:if test="${not empty post.mediaUrl and post.mediaType != 'poll'}">
                        <div class="post-media">
                            <c:if test="${post.mediaType == 'image'}">
                                <img src="<c:out value="${post.mediaUrl}"/>" alt="" class="post-media-img">
                            </c:if>
                            <c:if test="${post.mediaType == 'video'}">
                                <video src="<c:out value="${post.mediaUrl}"/>" controls class="post-media-video"></video>
                            </c:if>
                        </div>
                        </c:if>
                        <div class="post-footer">
                            <div class="footer-btn">💬 Comments</div>
                        </div>
                    </div>
                </div>
                </c:forEach>
                </c:when>
                <c:otherwise>
                <div class="profile-empty">No posts yet.</div>
                </c:otherwise>
                </c:choose>
            </div>
            </c:if>
            <c:if test="${tab == 'comments'}">
            <div class="profile-comments">
                <c:choose>
                <c:when test="${not empty comments}">
                <c:forEach var="comment" items="${comments}">
                <div class="comment-card">
                    <div class="comment-card-meta">
                        <a href="${pageContext.request.contextPath}/posts/detail?id=${comment.postId}" class="comment-post-link"><c:out value="${empty comment.postTitle ? 'Post' : comment.postTitle}"/></a>
                    </div>
                    <div class="comment-card-body"><c:out value="${comment.content}"/></div>
                    <div class="comment-card-time">
                        <fmt:formatDate value="${comment.createdAt}" type="both" dateStyle="short" timeStyle="short"/>
                    </div>
                </div>
                </c:forEach>
                </c:when>
                <c:otherwise>
                <div class="profile-empty">No comments yet.</div>
                </c:otherwise>
                </c:choose>
            </div>
            </c:if>
        </div>
        </c:if>
        <c:if test="${empty profileUser}">
        <div class="profile-empty">User not found.</div>
        </c:if>
    </div>

    <%-- Modal for View Photo (plain JS, no reload) --%>
    <div id="profilePhotoModal" class="profile-photo-modal" role="dialog" aria-modal="true" aria-label="Profile photo" aria-hidden="true">
        <div class="profile-photo-modal-backdrop"></div>
        <div class="profile-photo-modal-content">
            <button type="button" class="profile-photo-modal-close" id="profilePhotoModalClose" aria-label="Close">×</button>
            <img id="profilePhotoModalImg" src="" alt="Profile photo" class="profile-photo-modal-img">
        </div>
    </div>

    <%-- Crop/Resize modal: choose which part of the photo shows in the circle, then Save or Cancel --%>
    <c:if test="${not empty sessionScope.user and sessionScope.user.id == profileUser.id}">
    <div id="profileCropModal" class="profile-crop-modal" role="dialog" aria-modal="true" aria-label="Resize and position photo" aria-hidden="true">
        <div class="profile-crop-modal-backdrop"></div>
        <div class="profile-crop-modal-content">
            <h3 class="profile-crop-title">Adjust photo for profile circle</h3>
            <div class="profile-crop-preview-wrap">
                <canvas id="profileCropCanvas" class="profile-crop-canvas" width="280" height="280"></canvas>
            </div>
            <div class="profile-crop-controls">
                <label class="profile-crop-zoom-label">Zoom</label>
                <input type="range" id="profileCropZoom" class="profile-crop-zoom" min="0.5" max="3" step="0.05" value="1">
            </div>
            <div class="profile-crop-actions">
                <button type="button" class="profile-photo-btn profile-crop-cancel" id="profileCropCancel">Cancel</button>
                <button type="button" class="profile-photo-btn profile-crop-save" id="profileCropSave">Save</button>
            </div>
        </div>
    </div>
    </c:if>

    <script src="${pageContext.request.contextPath}/assets/js/vote.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/post-menu.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/profile.js"></script>
</body>
</html>
