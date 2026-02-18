package filter;

import util.CsrfUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Ensures a CSRF token exists in session for GET requests and validates it on POST (and other state-changing methods).
 * Handles multipart/form-data by reading token from Part when getParameter returns null.
 */
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        CsrfUtil.getOrCreateToken(session);

        String method = req.getMethod();
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            String requestToken = req.getParameter("csrfToken");
            if (requestToken == null) {
                requestToken = req.getHeader("X-CSRF-Token");
            }
            if (requestToken == null && isMultipart(req)) {
                requestToken = getCsrfTokenFromMultipart(req);
            }
            if (!CsrfUtil.isValid(session, requestToken)) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isMultipart(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/form-data");
    }

    private String getCsrfTokenFromMultipart(HttpServletRequest req) {
        try {
            Collection<Part> parts = req.getParts();
            for (Part part : parts) {
                if ("csrfToken".equals(part.getName()) && part.getSize() > 0) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8))) {
                        return reader.lines().collect(Collectors.joining()).trim();
                    }
                }
            }
            } catch (Exception e) {
            return null;
        }
        return null;
    }
}
