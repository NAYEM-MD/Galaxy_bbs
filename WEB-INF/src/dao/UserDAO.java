package dao;

import model.User;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends BaseDAO {
    
    public User findById(int id) {
        User user = null;
        String sql = "SELECT id, username, password, email, profile_image FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    user.setPassword(rs.getString(3) != null ? rs.getString(3).trim() : null);
                    user.setEmail(rs.getString(4));
                    user.setProfileImage(rs.getString(5));
                }
            }
        } catch (SQLException e) {
            logError("findById failed", e);
        }
        return user;
    }

    public User findByUsername(String username) {
        User user = null;
        String sql = "SELECT id, username, password, email, profile_image FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    String pwd = rs.getString(3);
                    user.setPassword(pwd != null ? pwd.trim() : null);
                    user.setEmail(rs.getString(4));
                    user.setProfileImage(rs.getString(5));
                }
            }
        } catch (SQLException e) {
            logError("findByUsername failed", e);
        }
        return user;
    }
    
    public User findByEmail(String email) {
        User user = null;
        String sql = "SELECT id, username, password, email, profile_image FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email != null ? email : "");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    String pwd = rs.getString(3);
                    user.setPassword(pwd != null ? pwd.trim() : null);
                    user.setEmail(rs.getString(4));
                    user.setProfileImage(rs.getString(5));
                }
            }
        } catch (SQLException e) {
            logError("findByEmail failed", e);
        }
        return user;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, PasswordUtil.hashPassword(user.getPassword()));
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logError("save user failed", e);
        }
    }
    
    /** Returns true if the given password matches the user's stored password. */
    public boolean verifyPassword(int userId, String password) {
        User user = findById(userId);
        if (user == null || user.getPassword() == null) return false;
        return PasswordUtil.verifyPassword(password, user.getPassword());
    }

    public boolean updateEmail(int userId, String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) return false;
        User existing = findByEmail(newEmail.trim());
        if (existing != null && existing.getId() != userId) return false;
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newEmail.trim());
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            logError("updateEmail failed", e);
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) return false;
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.hashPassword(newPassword.trim()));
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            logError("updatePassword failed", e);
            return false;
        }
    }

    /** Updates the profile image path for the user. Path is relative to webapp (e.g. uploads/profiles/xxx.jpg). */
    public boolean updateProfileImage(int userId, String relativePath) {
        String sql = "UPDATE users SET profile_image = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, relativePath);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            logError("updateProfileImage failed", e);
            return false;
        }
    }

    /**
     * Deletes user and their data (notifications, comments, votes, posts). Call only after verifying password.
     */
    public boolean deleteUser(int userId, Connection conn) throws SQLException {
        String delNotifications = "DELETE FROM notifications WHERE user_id = ? OR actor_id = ?";
        String delCommentsByUser = "DELETE FROM comments WHERE user_id = ?";
        String delCommentsOnUserPosts = "DELETE FROM comments WHERE post_id IN (SELECT id FROM posts WHERE user_id = ?)";
        String delVotesByUser = "DELETE FROM votes WHERE user_id = ?";
        String delVotesOnUserPosts = "DELETE FROM votes WHERE post_id IN (SELECT id FROM posts WHERE user_id = ?)";
        String delPollVotes = "DELETE FROM poll_votes WHERE user_id = ?";
        String delPosts = "DELETE FROM posts WHERE user_id = ?";
        String delUser = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(delNotifications)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delCommentsByUser)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delCommentsOnUserPosts)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delVotesByUser)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delVotesOnUserPosts)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delPollVotes)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delPosts)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delUser)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean deleteUser(int userId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean ok = deleteUser(userId, conn);
                conn.commit();
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logError("deleteUser failed", e);
            return false;
        }
    }

    public User checkLogin(String email, String password) {
        User user = findByEmail(email);
        if (user != null) {
            String stored = user.getPassword();
            if (stored != null && PasswordUtil.verifyPassword(password, stored)) {
                // Migrate legacy (plain/SHA-256) to bcrypt on next password use
                if (!stored.startsWith("$2")) {
                    updatePassword(user.getId(), (password != null) ? password.trim() : "");
                    user.setPassword(PasswordUtil.hashPassword((password != null) ? password.trim() : ""));
                }
                return user;
            }
        }
        return null;
    }
}
