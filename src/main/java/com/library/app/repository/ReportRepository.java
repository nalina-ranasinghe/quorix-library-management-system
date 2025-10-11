// New ReportRepository.java
package com.library.app.repository;

import com.library.app.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Report> reportRowMapper = (rs, rowNum) -> {
        Report report = new Report();
        report.setReportId(rs.getInt("report_id"));
        report.setReportName(rs.getString("report_name"));
        report.setGenerationTimestamp(rs.getTimestamp("generation_timestamp").toLocalDateTime());
        report.setGeneratedByUserId(rs.getInt("generated_by_user_id"));
        if (rs.wasNull()) report.setGeneratedByUserId(null);
        report.setDeliveryStatus(rs.getString("delivery_status"));
        report.setRecipientEmail(rs.getString("recipient_email"));
        return report;
    };

    public List<Report> findAll() {
        return jdbcTemplate.query("SELECT * FROM Report", reportRowMapper);
    }

    public Optional<Report> findById(int id) {
        List<Report> reports = jdbcTemplate.query("SELECT * FROM Report WHERE report_id = ?", reportRowMapper, id);
        return reports.stream().findFirst();
    }

    public Report save(Report report) {
        String sql = "INSERT INTO Report (report_name, generated_by_user_id, delivery_status, recipient_email) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, report.getReportName());
            if (report.getGeneratedByUserId() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, report.getGeneratedByUserId());
            }
            ps.setString(3, report.getDeliveryStatus());
            ps.setString(4, report.getRecipientEmail());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            report.setReportId(keyHolder.getKey().intValue());
        }
        return report;
    }

    public void updateDeliveryStatus(int id, String status) {
        jdbcTemplate.update("UPDATE Report SET delivery_status = ? WHERE report_id = ?", status, id);
    }
}