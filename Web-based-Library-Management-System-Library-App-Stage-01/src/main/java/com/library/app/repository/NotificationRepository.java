package com.library.app.repository;

import com.library.app.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Notification> notificationRowMapper = (rs, rowNum) -> new Notification(
            rs.getInt("notification_id"), rs.getInt("user_id"), rs.getString("message"),
            rs.getString("type"), rs.getTimestamp("sent_at").toLocalDateTime(), rs.getString("status")
    );

    public void save(Notification notification) {
        String sql = "INSERT INTO Notifications (user_id, message, type, sent_at, status) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, notification.getUserId(), notification.getMessage(), notification.getType(), Timestamp.valueOf(notification.getSentAt()), notification.getStatus());
    }

    public Optional<Notification> findById(int notificationId) {
        String sql = "SELECT * FROM Notifications WHERE notification_id = ?";
        return jdbcTemplate.query(sql, new Object[]{notificationId}, notificationRowMapper).stream().findFirst();
    }

    public List<Notification> findByUserId(Integer userId) {
        String sql = "SELECT * FROM Notifications WHERE user_id = ? ORDER BY sent_at DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, notificationRowMapper);
    }

    public Long countUnreadByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM Notifications WHERE user_id = ? AND status = 'UNREAD'";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, Long.class);
    }

    public void markAllAsReadByUserId(Integer userId) {
        String sql = "UPDATE Notifications SET status = 'READ' WHERE user_id = ? AND status = 'UNREAD'";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteById(int notificationId) {
        String sql = "DELETE FROM Notifications WHERE notification_id = ?";
        jdbcTemplate.update(sql, notificationId);
    }

    public int deleteByTypeAndSentAtBefore(String type, LocalDateTime cutoff) {
        String sql = "DELETE FROM Notifications WHERE type = ? AND sent_at < ?";
        return jdbcTemplate.update(sql, type, Timestamp.valueOf(cutoff));
    }

    /**
     * Updates the status of a single notification to 'READ'.
     * @param notificationId The ID of the notification to update.
     */
    public void markAsReadById(int notificationId) {
        String sql = "UPDATE Notifications SET status = 'READ' WHERE notification_id = ?";
        jdbcTemplate.update(sql, notificationId);
    }

}