package filter;

import dao.NotificationDAO;
import model.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Sets unread notification count in request for logged-in users.
 * Replaces direct NotificationDAO calls from header.jsp.
 */
public class NotificationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj instanceof User) {
                User user = (User) userObj;
                NotificationDAO dao = new NotificationDAO();
                int unreadCount = dao.getUnreadCount(user.getId());
                req.setAttribute("unreadCount", Integer.valueOf(unreadCount));
            }
        }

        chain.doFilter(request, response);
    }
}
