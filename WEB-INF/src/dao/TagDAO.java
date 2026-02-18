package dao;

import model.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TagDAO extends BaseDAO {

    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private final AtomicReference<List<Tag>> cache = new AtomicReference<>(null);
    private volatile long cacheExpiry = 0;

    public List<Tag> findAll() {
        long now = System.currentTimeMillis();
        if (cache.get() != null && now < cacheExpiry) {
            return new ArrayList<>(cache.get());
        }
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT * FROM tags ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logError("findAll tags failed", e);
        }
        cache.set(tags);
        cacheExpiry = now + CACHE_TTL_MS;
        return tags;
    }

    private void invalidateCache() {
        cache.set(null);
        cacheExpiry = 0;
    }

    public void initTags() {
        invalidateCache();
        // Helper to ensure tags exist
        String countSql = "SELECT COUNT(*) FROM tags";
        String insertSql = "INSERT INTO tags (name) VALUES (?)";
        
        try (Connection conn = getConnection()) {
            boolean empty = true;
            try (PreparedStatement pstmt = conn.prepareStatement(countSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    empty = false;
                }
            }
            
            if (empty) {
                String[] defaultTags = {"General", "Technology", "Gaming", "Music", "News"};
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    for (String tag : defaultTags) {
                        pstmt.setString(1, tag);
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            logError("initTags failed", e);
        }
    }
}
