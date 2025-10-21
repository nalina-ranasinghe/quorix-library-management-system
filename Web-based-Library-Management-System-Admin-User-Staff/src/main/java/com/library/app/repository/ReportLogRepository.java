package com.library.app.repository;

import com.library.app.dto.ReportLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportLogRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new report generation record into the database.
     */
    public void save(String reportName, int userId, String status) {
        String sql = "INSERT INTO Report (report_name, generated_by_user_id, delivery_status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reportName, userId, status);
    }

    /**
     * Fetches the most recent report logs, joining with the Users table to get the username.
     */
    public List<ReportLogDto> findRecentLogs(int limit) {
        String sql = """
            SELECT TOP (?)
                   r.report_name,
                   r.generation_timestamp,
                   u.username AS generated_by_username,
                   r.delivery_status
            FROM Report r
            JOIN Users u ON r.generated_by_user_id = u.user_id
            ORDER BY r.generation_timestamp DESC
        """;

        RowMapper<ReportLogDto> rowMapper = (rs, rowNum) -> {
            ReportLogDto dto = new ReportLogDto();
            dto.setReportName(rs.getString("report_name"));
            dto.setGenerationTimestamp(rs.getTimestamp("generation_timestamp").toLocalDateTime());
            dto.setGeneratedByUsername(rs.getString("generated_by_username"));
            dto.setDeliveryStatus(rs.getString("delivery_status"));
            return dto;
        };

        return jdbcTemplate.query(sql, new Object[]{limit}, rowMapper);
    }
}