// New SystemLogRepository.java
package com.library.app.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SystemLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public void logAction(Integer userId, String action, String details) {
        String sql = "INSERT INTO SystemLogs (user_id, action, details) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, action, details);
    }
}