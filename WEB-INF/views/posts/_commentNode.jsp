<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="util.TimeAgoUtil" %>
<%@ page import="model.Comment" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Expects request attributes: currentComment (Comment), commentDepth (Integer, 0-based) --%>
<c:set var="depth" value="${empty commentDepth ? 0 : commentDepth}"/>
<c:if test="${not empty currentComment}">
<div class="comment-thread-item comment-depth-${depth}" id="comment-${currentComment.id}" data-comment-id="${currentComment.id}">
    <div class="comment-block">
        <div class="comment-header">
            <span class="comment-author">u/<c:out value="${currentComment.authorName}"/>
                <c:if test="${currentComment.userId == post.userId}"><span class="comment-op-badge">OP</span></c:if>
            </span>
            <span class="comment-time"><% Comment _c = (Comment)request.getAttribute("currentComment"); out.print(_c != null && _c.getCreatedAt() != null ? TimeAgoUtil.format(_c.getCreatedAt()) : ""); %></span>
        </div>
        <div class="comment-body">
            <c:out value="${currentComment.content}"/>
        </div>
        <c:if test="${not empty sessionScope.user}">
        <div class="comment-actions-bar">
            <button type="button" class="comment-action-btn reply-btn" data-parent-id="${currentComment.id}" data-author="<c:out value="${currentComment.authorName}"/>">Reply</button>
            <c:if test="${sessionScope.user.id == currentComment.userId}">
            <form action="${pageContext.request.contextPath}/comments/delete" method="post" class="comment-delete-form">
                <input type="hidden" name="commentId" value="${currentComment.id}">
                <button type="submit" class="comment-action-btn delete-btn">Delete</button>
            </form>
            </c:if>
        </div>
        <div class="comment-reply-form-wrapper" id="reply-form-${currentComment.id}" style="display: none;">
            <form action="${pageContext.request.contextPath}/comments/create" method="post" class="comment-reply-form">
                <input type="hidden" name="postId" value="${post.id}">
                <input type="hidden" name="parentId" value="${currentComment.id}">
                <textarea name="content" class="comment-reply-textarea" placeholder="Write a reply..."></textarea>
                <div class="comment-reply-actions">
                    <button type="button" class="comment-reply-cancel">Cancel</button>
                    <button type="submit" class="comment-reply-submit">Reply</button>
                </div>
            </form>
        </div>
        </c:if>
    </div>
    <c:if test="${not empty currentComment.replies}">
    <div class="comment-replies">
        <c:forEach var="reply" items="${currentComment.replies}">
            <c:set var="currentComment" value="${reply}" scope="request"/>
            <c:set var="commentDepth" value="${depth + 1}" scope="request"/>
            <jsp:include page="/WEB-INF/views/posts/_commentNode.jsp"/>
        </c:forEach>
    </div>
    </c:if>
</div>
</c:if>
