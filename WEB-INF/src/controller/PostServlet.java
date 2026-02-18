package controller;

import dao.CommentDAO;
import dao.PostDAO;
import dao.TagDAO;
import model.Comment;
import model.Post;
import model.Tag;
import model.User;
import model.Poll;
import model.PollOption;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/posts/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2 MB
    maxFileSize = 1024 * 1024 * 10,       // 10 MB
    maxRequestSize = 1024 * 1024 * 50     // 50 MB
)
public class PostServlet extends HttpServlet {
    private PostDAO postDAO = new PostDAO();
    private CommentDAO commentDAO = new CommentDAO();

    private static final int MAX_TITLE_LENGTH = 300;
    private static final int MAX_CONTENT_LENGTH = 50_000;
    private static final int PAGE_SIZE = 20;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "/feed";
        
        HttpSession session = request.getSession(false);
        int currentUserId = 0;
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            currentUserId = user.getId();
        }
        
        switch (path) {
            case "/feed":
                String q = request.getParameter("q");
                int page = 1;
                String pageParam = request.getParameter("page");
                if (pageParam != null && !pageParam.isEmpty()) {
                    try {
                        page = Math.max(1, Integer.parseInt(pageParam));
                    } catch (NumberFormatException ignored) { }
                }
                int offset = (page - 1) * PAGE_SIZE;
                int total;
                List<Post> posts;
                if (q != null && !q.trim().isEmpty()) {
                    total = postDAO.countSearch(q.trim());
                    posts = postDAO.search(q.trim(), currentUserId, offset, PAGE_SIZE);
                    request.setAttribute("searchQuery", q.trim());
                } else {
                    total = postDAO.countAll();
                    posts = postDAO.findAll(currentUserId, offset, PAGE_SIZE);
                }
                int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
                if (totalPages < 1) totalPages = 1;
                request.setAttribute("posts", posts);
                request.setAttribute("currentPage", page);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("totalPosts", total);
                request.getRequestDispatcher("/WEB-INF/views/posts/feed.jsp").forward(request, response);
                break;
            case "/create":
                if (currentUserId == 0) {
                    response.sendRedirect(request.getContextPath() + "/auth/login");
                    return;
                }
                request.getRequestDispatcher("/WEB-INF/views/posts/create.jsp").forward(request, response);
                break;
            case "/detail":
                String idStr = request.getParameter("id");
                if (idStr != null) {
                    int id;
                    try {
                        id = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        response.sendRedirect(request.getContextPath() + "/posts/feed");
                        return;
                    }
                    Post post = postDAO.findById(id, currentUserId);
                    List<Comment> flatComments = commentDAO.findByPostId(id);
                    List<Comment> commentTree = buildCommentTree(flatComments);
                    request.setAttribute("post", post);
                    request.setAttribute("commentTree", commentTree);
                    request.setAttribute("commentCount", flatComments.size());
                    request.getRequestDispatcher("/WEB-INF/views/posts/detail.jsp").forward(request, response);
                } else {
                    response.sendRedirect(request.getContextPath() + "/posts/feed");
                }
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();
        if ("/delete".equals(path)) {
            handleDelete(request, response);
            return;
        }
        if ("/create".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/auth/login");
                return;
            }
            User user = (User) session.getAttribute("user");
            
            String title = request.getParameter("title");
            String content = request.getParameter("content"); // This is the rich text body
            String postType = request.getParameter("postType");

            if (title == null || title.trim().isEmpty()) {
                session.setAttribute("uploadError", "Title is required.");
                response.sendRedirect(request.getContextPath() + "/posts/create");
                return;
            }
            title = title.trim();
            if (title.length() > MAX_TITLE_LENGTH) {
                session.setAttribute("uploadError", "Title must be at most " + MAX_TITLE_LENGTH + " characters.");
                response.sendRedirect(request.getContextPath() + "/posts/create");
                return;
            }
            if (content != null && content.length() > MAX_CONTENT_LENGTH) {
                session.setAttribute("uploadError", "Content must be at most " + MAX_CONTENT_LENGTH + " characters.");
                response.sendRedirect(request.getContextPath() + "/posts/create");
                return;
            }
            int tagId = 1; // Default to "General" since selection is removed from UI
            
            String mediaUrl = null;
            String mediaType = null;
            Poll poll = null;
            
            if ("media".equals(postType)) {
                Part filePart = request.getPart("mediaFile");
                if (filePart != null && filePart.getSize() > 0) {
                    String fileName = getSubmittedFileName(filePart);
                    String contentType = filePart.getContentType();
                    
                    // Determine file extension
                    String extension = "";
                    if (fileName != null) {
                        int i = fileName.lastIndexOf('.');
                        if (i > 0) {
                            extension = fileName.substring(i).toLowerCase();
                        }
                    }
                    
                    // Whitelist: reject .jsp, .exe, etc.
                    if (!ALLOWED_MEDIA_EXTENSIONS.contains(extension)) {
                        request.getSession().setAttribute("uploadError", "Invalid file type. Allowed: jpg, png, gif, webp, mp4, etc.");
                        response.sendRedirect(request.getContextPath() + "/posts/create");
                        return;
                    }
                    
                    // Generate unique filename
                    String uniqueFileName = UUID.randomUUID().toString() + extension;
                    
                    // Get upload directory
                    String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) uploadDir.mkdir();
                    
                    // Save file
                    filePart.write(uploadPath + File.separator + uniqueFileName);
                    
                    mediaUrl = request.getContextPath() + "/uploads/" + uniqueFileName;
                    
                    if (contentType.startsWith("image/")) {
                        mediaType = "image";
                    } else if (contentType.startsWith("video/")) {
                        mediaType = "video";
                    }
                }
            } else if ("poll".equals(postType)) {
                String pollQuestion = request.getParameter("pollQuestion");
                String[] pollOptions = request.getParameterValues("pollOption");
                
                if (pollQuestion != null && !pollQuestion.trim().isEmpty() && 
                    pollOptions != null && pollOptions.length >= 2) {
                    
                    poll = new Poll();
                    poll.setQuestion(pollQuestion);
                    for (String optionText : pollOptions) {
                        if (optionText != null && !optionText.trim().isEmpty()) {
                            poll.addOption(new PollOption(optionText));
                        }
                    }
                    mediaType = "poll";
                    // Clear content if it's a poll, or keep it if we want hybrid posts. 
                    // User UI shows separate tabs, so likely separate.
                    content = null; 
                }
            }
            
            Post post = new Post(title, content, tagId, user.getId(), mediaUrl, mediaType);
            post.setPoll(poll);
            postDAO.save(post);

            // Redirect to the feed after posting
            response.sendRedirect(request.getContextPath() + "/posts/feed");
        }
    }
    
    /**
     * Deletes a post only if the current user is the post owner.
     * Redirects to feed after delete or on any error.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        User user = (User) session.getAttribute("user");
        int currentUserId = user.getId();

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/posts/feed");
            return;
        }
        int postId;
        try {
            postId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/posts/feed");
            return;
        }

        Post post = postDAO.findById(postId, currentUserId);
        if (post == null || post.getUserId() != currentUserId) {
            response.sendRedirect(request.getContextPath() + "/posts/feed");
            return;
        }

        postDAO.delete(postId);
        response.sendRedirect(request.getContextPath() + "/posts/feed");
    }

    private String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null || cd.isEmpty()) return null;
        for (String s : cd.split(";")) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /** Allowed extensions for media upload (whitelist). Rejects .jsp, .exe, etc. */
    private static final java.util.Set<String> ALLOWED_MEDIA_EXTENSIONS = java.util.Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp",
        ".mp4", ".webm", ".mov", ".avi", ".mkv"
    );

    /** Builds a tree of comments (roots with nested replies) from a flat list. */
    private List<Comment> buildCommentTree(List<Comment> flat) {
        Map<Integer, Comment> idToComment = new HashMap<>();
        for (Comment c : flat) {
            idToComment.put(c.getId(), c);
        }
        List<Comment> roots = new ArrayList<>();
        for (Comment c : flat) {
            Integer pid = c.getParentId();
            if (pid == null) {
                roots.add(c);
            } else {
                Comment parent = idToComment.get(pid);
                if (parent != null) {
                    parent.getReplies().add(c);
                } else {
                    roots.add(c);
                }
            }
        }
        // Flatten to one level of reply: all replies under a main comment show at same indent (no sub-replies)
        for (Comment root : roots) {
            List<Comment> flatReplies = flattenReplies(root.getReplies());
            for (Comment c : flatReplies) {
                c.setReplies(new ArrayList<>());
            }
            root.setReplies(flatReplies);
        }
        return roots;
    }

    /** Collects all descendants in order (depth-first) for "1 line of reply" display. */
    private List<Comment> flattenReplies(List<Comment> replies) {
        if (replies == null || replies.isEmpty()) return new ArrayList<>();
        List<Comment> out = new ArrayList<>();
        for (Comment c : replies) {
            out.add(c);
            out.addAll(flattenReplies(c.getReplies()));
        }
        return out;
    }
}
