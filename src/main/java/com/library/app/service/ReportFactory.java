package com.library.app.service;

import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportFactory {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public List<Map<String, Object>> getReportData(String reportType) {
        return switch (reportType) {
            case "POPULAR_BOOKS" -> jdbcTemplate.queryForList(
                    "SELECT b.title, COUNT(br.borrowing_id) as borrow_count " +
                            "FROM Books b JOIN Borrowings br ON b.book_id = br.book_id " +
                            "GROUP BY b.title ORDER BY borrow_count DESC"
            );
            case "POPULAR_USERS" -> jdbcTemplate.queryForList(
                    "SELECT u.full_name, COUNT(br.borrowing_id) as borrow_count " +
                            "FROM Users u JOIN Borrowings br ON u.user_id = br.user_id " +
                            "GROUP BY u.full_name ORDER BY borrow_count DESC"
            );
            case "OVERDUE" -> jdbcTemplate.queryForList(
                    "SELECT u.full_name, b.title, br.due_date " +
                            "FROM Borrowings br JOIN Users u ON br.user_id = u.user_id JOIN Books b ON br.book_id = b.book_id " +
                            "WHERE br.return_date IS NULL AND br.due_date < GETDATE()"
            );
            case "AVAILABLE_BOOKS" -> jdbcTemplate.queryForList(
                    "SELECT title, quantity FROM Books WHERE status = 'AVAILABLE' AND quantity > 0"
            );
            case "MONTHLY_STATS" -> jdbcTemplate.queryForList(
                    "SELECT YEAR(borrow_date) as year, MONTH(borrow_date) as month, COUNT(*) as count " +
                            "FROM Borrowings GROUP BY YEAR(borrow_date), MONTH(borrow_date) ORDER BY year DESC, month DESC"
            );
            default -> throw new IllegalArgumentException("Unknown report type: " + reportType);
        };
    }

    public int getNewUsersLast30Days() {
        return userRepository.countNewUsersLast30Days();
    }

    // Added method to fix jdbcTemplate access issue
    public List<Map<String, Object>> getStaffAttendance() {
        return jdbcTemplate.queryForList(
                "SELECT u.full_name, sa.check_in_time, sa.check_out_time, sa.notes " +
                        "FROM StaffAttendance sa JOIN Users u ON sa.user_id = u.user_id " +
                        "ORDER BY sa.check_in_time DESC"
        );
    }
}