package dao;

import model.Comment;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    
    public List<Comment> findByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.id, c.content, c.post_id, c.parent_id, c.user_id, c.created_at, u.username as author_name " +
                     "FROM comments c JOIN users u ON c.user_id = u.id WHERE c.post_id = ? ORDER BY c.created_at ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    int i = 1;
                    comment.setId(rs.getInt(i++));
                    comment.setContent(rs.getString(i++));
                    comment.setPostId(rs.getInt(i++));
                    Object parentIdObj = rs.getObject(i++);
                    comment.setParentId((parentIdObj == null || rs.wasNull()) ? null : ((Number) parentIdObj).intValue());
                    comment.setUserId(rs.getInt(i++));
                    comment.setCreatedAt(rs.getTimestamp(i++));
                    comment.setAuthorName(rs.getString(i++));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    /** Comments by a specific user (for profile "My Comments"). */
    public List<Comment> findByUserId(int userId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.id, c.content, c.post_id, c.user_id, c.created_at, u.username as author_name, p.title as post_title " +
                     "FROM comments c JOIN users u ON c.user_id = u.id JOIN posts p ON c.post_id = p.id WHERE c.user_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    int i = 1;
                    comment.setId(rs.getInt(i++));
                    comment.setContent(rs.getString(i++));
                    comment.setPostId(rs.getInt(i++));
                    comment.setUserId(rs.getInt(i++));
                    comment.setCreatedAt(rs.getTimestamp(i++));
                    comment.setAuthorName(rs.getString(i++));
                    comment.setPostTitle(rs.getString(i++));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public void save(Comment comment) {
        String sql = "INSERT INTO comments (content, post_id, parent_id, user_id, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            
            pstmt.setString(1, comment.getContent());
            pstmt.setInt(2, comment.getPostId());
            if (comment.getParentId() != null) {
                pstmt.setInt(3, comment.getParentId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, comment.getUserId());
            if (comment.getCreatedAt() != null) {
                pstmt.setTimestamp(5, new Timestamp(comment.getCreatedAt().getTime()));
            } else {
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Find comment by id (for delete authorization and postId redirect). */
    public Comment findById(int commentId) {
        String sql = "SELECT c.id, c.content, c.post_id, c.parent_id, c.user_id, c.created_at FROM comments c WHERE c.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Comment comment = new Comment();
                    int i = 1;
                    comment.setId(rs.getInt(i++));
                    comment.setContent(rs.getString(i++));
                    comment.setPostId(rs.getInt(i++));
                    Object parentIdObj = rs.getObject(i++);
                    comment.setParentId((parentIdObj == null || rs.wasNull()) ? null : ((Number) parentIdObj).intValue());
                    comment.setUserId(rs.getInt(i++));
                    comment.setCreatedAt(rs.getTimestamp(i++));
                    return comment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(int commentId) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
