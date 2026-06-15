package com.library.app.repository;

import com.library.app.dto.UserWaitlistDto;
import com.library.app.entity.User;
import com.library.app.entity.Waitlist;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WaitlistRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Waitlist> waitlistRowMapper = (rs, rowNum) -> {
        Waitlist w = new Waitlist();
        w.setWaitlistId(rs.getInt("waitlist_id"));
        w.setUserId(rs.getInt("user_id"));
        w.setBookId(rs.getInt("book_id"));
        w.setWaitlistedAt(rs.getTimestamp("waitlisted_at").toLocalDateTime());
        w.setNotified(rs.getBoolean("notified"));
        return w;
    };

    public void save(Waitlist waitlist) {
        String sql = "INSERT INTO Waitlist (user_id, book_id, waitlisted_at, notified) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                waitlist.getUserId(),
                waitlist.getBookId(),
                waitlist.getWaitlistedAt(),
                waitlist.isNotified()
        );
    }

    public boolean existsByUserIdAndBookId(int userId, int bookId) {
        String sql = "SELECT COUNT(*) FROM Waitlist WHERE user_id = ? AND book_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{userId, bookId}, Integer.class);
        return count != null && count > 0;
    }

    public Optional<User> findFirstUserInWaitlist(int bookId) {
        String sql = "SELECT TOP 1 u.* FROM Users u " +
                "JOIN Waitlist w ON u.user_id = w.user_id " +
                "WHERE w.book_id = ? " +
                "ORDER BY w.waitlisted_at ASC";

        // Bug 2 fix: was a stub returning new User() — now fully maps all User fields
        RowMapper<User> userRowMapper = (rs, rowNum) -> {
            User u = new User();
            u.setUserId(rs.getInt("user_id"));
            u.setUsername(rs.getString("username"));
            u.setFullName(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setPhone(rs.getString("phone"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setRole(rs.getString("role"));
            u.setStatus(rs.getString("status"));
            return u;
        };
        return jdbcTemplate.query(sql, new Object[]{bookId}, userRowMapper).stream().findFirst();
    }

    public void deleteByUserIdAndBookId(int userId, int bookId) {
        String sql = "DELETE FROM Waitlist WHERE user_id = ? AND book_id = ?";
        jdbcTemplate.update(sql, userId, bookId);
    }

    //  Find a waitlist entry by its primary key
    public Optional<Waitlist> findById(int waitlistId) {
        String sql = "SELECT * FROM Waitlist WHERE waitlist_id = ?";
        return jdbcTemplate.query(sql, new Object[]{waitlistId}, waitlistRowMapper).stream().findFirst();
    }

    //  Get all waitlisted books for a specific user
    public List<UserWaitlistDto> findWaitlistedBooksByUserId(int userId) {
        String sql = "SELECT w.waitlist_id, b.title, b.author, w.waitlisted_at " +
                "FROM Waitlist w " +
                "JOIN Books b ON w.book_id = b.book_id " +
                "WHERE w.user_id = ? " +
                "ORDER BY w.waitlisted_at ASC";

        RowMapper<UserWaitlistDto> rowMapper = (rs, rowNum) -> {
            UserWaitlistDto dto = new UserWaitlistDto();
            dto.setWaitlistId(rs.getInt("waitlist_id"));
            dto.setBookTitle(rs.getString("title"));
            dto.setBookAuthor(rs.getString("author"));
            dto.setWaitlistedAt(rs.getTimestamp("waitlisted_at").toLocalDateTime());
            return dto;
        };
        return jdbcTemplate.query(sql, new Object[]{userId}, rowMapper);
    }

    //  Delete a waitlist entry by its primary key
    public void deleteById(int waitlistId) {
        String sql = "DELETE FROM Waitlist WHERE waitlist_id = ?";
        jdbcTemplate.update(sql, waitlistId);
    }
}