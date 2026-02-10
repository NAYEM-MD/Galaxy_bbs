package dao;

import model.PasswordResetToken;
import util.DBUtil;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * DAO for managing password reset tokens.
 */
public class PasswordResetTokenDAO {

    /**
     * Creates a new reset token for the given user, invalidating any previous tokens.
     *
     * @param userId         the user ID
     * @param rawToken       the raw token string (will be hashed before storing)
     * @param expiresAtEpoch epoch millis when the token expires
     */
    public void createToken(int userId, String rawToken, long expiresAtEpoch) {
        String deleteSql = "DELETE FROM password_reset_tokens WHERE user_id = ?";
        String insertSql = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, used) VALUES (?, ?, ?, 0)";
        String tokenHash = PasswordUtil.hashPassword(rawToken);
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                del.setInt(1, userId);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                ins.setInt(1, userId);
                ins.setString(2, tokenHash);
                ins.setTimestamp(3, new Timestamp(expiresAtEpoch));
                ins.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a valid (not expired, not used) token and returns its associated user ID,
     * or -1 if invalid.
     */
    public int findUserIdByValidToken(String rawToken) {
        String tokenHash = PasswordUtil.hashPassword(rawToken);
        String sql = "SELECT user_id FROM password_reset_tokens " +
                     "WHERE token_hash = ? AND used = 0 AND expires_at > SYSTIMESTAMP";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Marks a token as used.
     */
    public void markTokenUsed(String rawToken) {
        String tokenHash = PasswordUtil.hashPassword(rawToken);
        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE token_hash = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

