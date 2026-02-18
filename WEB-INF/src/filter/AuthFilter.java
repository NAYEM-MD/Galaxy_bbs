package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Protects /posts/* so that only authenticated users (session attribute "user" set) can access.
 * Unauthenticated requests are redirected to /auth/login.
 */
public class AuthFilter implements Filter {

    private static final String USER_ATTR = "user";
    private static final String LOGIN_PATH = "/auth/login";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(USER_ATTR) == null) {
            String redirectUrl = req.getContextPath() + LOGIN_PATH;
            resp.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(request, response);
    }
}
