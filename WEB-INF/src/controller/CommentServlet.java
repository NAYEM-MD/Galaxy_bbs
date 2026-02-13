package controller;

import dao.CommentDAO;
import dao.NotificationDAO;
import dao.PostDAO;
import model.Comment;
import model.Notification;
import model.Post;
import model.User;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/comments/*")
public class CommentServlet extends HttpServlet {
    private CommentDAO commentDAO = new CommentDAO();
    private PostDAO postDAO = new PostDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/create".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            User user = (User) session.getAttribute("user");
            
            String content = request.getParameter("content");
            int postId = Integer.parseInt(request.getParameter("postId"));
            Integer parentId = null;
            String parentIdStr = request.getParameter("parentId");
            if (parentIdStr != null && !parentIdStr.trim().isEmpty()) {
                try {
                    parentId = Integer.parseInt(parentIdStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            Comment comment = new Comment(content, postId, parentId, user.getId());
            commentDAO.save(comment);
            
            // Create notification if the commenter is not the post author
            Post post = postDAO.findById(postId);
            if (post != null && post.getUserId() != user.getId()) {
                Notification notification = new Notification(
                    post.getUserId(), // Recipient (Post Author)
                    user.getId(),     // Actor (Commenter)
                    postId,
                    comment.getId(),  // Note: comment.getId() might be 0 if DAO doesn't set it back. 
                                      // CommentDAO.save usually doesn't update the object with ID unless implemented.
                                      // Let's check CommentDAO. If it doesn't, we can pass null or 0.
                                      // For linking, postId is enough for now, but commentId helps deep linking.
                    "comment"
                );
                notificationDAO.createNotification(notification);
            }
            
            // Redirect back to the post detail and scroll to the new comment
            int newCommentId = comment.getId();
            String anchor = newCommentId > 0 ? "#comment-" + newCommentId : "";
            response.sendRedirect(request.getContextPath() + "/posts/detail?id=" + postId + anchor);
        } else if ("/delete".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            User user = (User) session.getAttribute("user");
            String commentIdStr = request.getParameter("commentId");
            if (commentIdStr == null || commentIdStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/posts/feed");
                return;
            }
            int commentId;
            try {
                commentId = Integer.parseInt(commentIdStr.trim());
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/posts/feed");
                return;
            }
            Comment comment = commentDAO.findById(commentId);
            if (comment == null) {
                response.sendRedirect(request.getContextPath() + "/posts/feed");
                return;
            }
            if (comment.getUserId() != user.getId()) {
                response.sendRedirect(request.getContextPath() + "/posts/detail?id=" + comment.getPostId());
                return;
            }
            commentDAO.delete(commentId);
            response.sendRedirect(request.getContextPath() + "/posts/detail?id=" + comment.getPostId());
        }
    }
}