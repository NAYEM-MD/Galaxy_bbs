package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String content;
    private int postId;
    private Integer parentId;   // null = top-level comment
    private int userId;
    private String authorName; // Transient field for display
    private String postTitle;  // Transient: for "My Comments" profile list
    private Date createdAt;
    private List<Comment> replies = new ArrayList<>(); // Nested replies for display

    public Comment() {}

    public Comment(String content, int postId, int userId) {
        this.content = content;
        this.postId = postId;
        this.userId = userId;
        this.createdAt = new Date();
    }

    public Comment(String content, int postId, Integer parentId, int userId) {
        this.content = content;
        this.postId = postId;
        this.parentId = parentId;
        this.userId = userId;
        this.createdAt = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies != null ? replies : new ArrayList<>(); }
}
