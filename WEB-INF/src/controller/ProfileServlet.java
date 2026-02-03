package controller;

import dao.CommentDAO;
import dao.PostDAO;
import dao.UserDAO;
import model.Comment;
import model.Post;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

public class ProfileServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private PostDAO postDAO = new PostDAO();
    private CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        if (username == null || username.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/posts/feed");
            return;
        }

        User profileUser = userDAO.findByUsername(username.trim());
        if (profileUser == null) {
            response.sendRedirect(request.getContextPath() + "/posts/feed");
            return;
        }

        HttpSession session = request.getSession(false);
        int currentUserId = 0;
        if (session != null && session.getAttribute("user") != null) {
            User currentUser = (User) session.getAttribute("user");
            currentUserId = currentUser.getId();
        }

        List<Post> posts = postDAO.findByUserId(profileUser.getId(), currentUserId);
        List<Comment> comments = commentDAO.findByUserId(profileUser.getId());

        String tab = request.getParameter("tab");
        if (tab == null || (!"comments".equals(tab))) {
            tab = "posts";
        }

        request.setAttribute("profileUser", profileUser);
        request.setAttribute("posts", posts);
        request.setAttribute("comments", comments);
        request.setAttribute("tab", tab);
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }
}
