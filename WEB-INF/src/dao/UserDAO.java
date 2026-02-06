package dao;

import model.User;
import util.DBUtil;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    
    public User findById(int id) {
        User user = null;
        String sql = "SELECT id, username, password, email FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    user.setPassword(rs.getString(3) != null ? rs.getString(3).trim() : null);
                    user.setEmail(rs.getString(4));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User findByUsername(String username) {
        User user = null;
        String sql = "SELECT id, username, password, email FROM users WHERE username = ?";
        
        try (Connection conn = DBUtil.getConnection();
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    
    public User findByEmail(String email) {
        User user = null;
        String sql = "SELECT id, username, password, email FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
        
        try (Connection conn = DBUtil.getConnection();
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
                    System.out.println("User found via email: " + email);
                } else {
                    System.out.println("User not found via email: " + email);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, PasswordUtil.hashPassword(user.getPassword()));
            pstmt.setString(3, user.getEmail());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /** Returns true if the given password matches the user's stored password. */
    public boolean verifyPassword(int userId, String password) {
        User user = findById(userId);
        if (user == null || user.getPassword() == null) return false;
        String input = (password != null) ? password.trim() : "";
        
        // Check hashed password
        String hashedInput = PasswordUtil.hashPassword(input);
        if (user.getPassword().equals(hashedInput)) {
            return true;
        }
        
        // Fallback: Check plain text (for legacy users)
        if (user.getPassword().equals(input)) {
            return true;
        }
        
        return false;
    }

    public boolean updateEmail(int userId, String newEmail) {
        if (newEmail == null || newEmail.trim().isEmpty()) return false;
        User existing = findByEmail(newEmail.trim());
        if (existing != null && existing.getId() != userId) return false;
        String sql = "UPDATE users SET email = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newEmail.trim());
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) return false;
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.hashPassword(newPassword.trim()));
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes user and their data (notifications, comments, votes, posts). Call only after verifying password.
     */
    public boolean deleteUser(int userId, Connection conn) throws SQLException {
        String delNotifications = "DELETE FROM notifications WHERE user_id = ? OR actor_id = ?";
        String delComments = "DELETE FROM comments WHERE user_id = ?";
        String delVotes = "DELETE FROM votes WHERE user_id = ?";
        String delPosts = "DELETE FROM posts WHERE user_id = ?";
        String delUser = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(delNotifications)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delComments)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(delVotes)) {
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
        try (Connection conn = DBUtil.getConnection()) {
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
            e.printStackTrace();
            return false;
        }
    }

    public User checkLogin(String email, String password) {
        System.out.println("Checking login for: " + email);
        User user = findByEmail(email);
        if (user != null) {
            System.out.println("User exists. Checking password...");
            String stored = user.getPassword();
            String inputPwd = (password != null) ? password.trim() : "";
            
            // Check hashed password
            String hashedInput = PasswordUtil.hashPassword(inputPwd);
            if (stored != null && stored.equals(hashedInput)) {
                System.out.println("Password match (Hashed)!");
                return user;
            }
            
            // Fallback: Check plain text and migrate
            if (stored != null && stored.equals(inputPwd)) {
                System.out.println("Password match (Plain text). Migrating to hash...");
                updatePassword(user.getId(), inputPwd); // Update DB with hash
                user.setPassword(hashedInput); // Update memory object
                return user;
            }
            
            System.out.println("Password mismatch!");
        }
        return null;
    }
}
