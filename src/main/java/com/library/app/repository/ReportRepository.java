// Create this file in: src/main/java/com/library/app/repository/ReportRepository.java
package com.library.app.repository;
import com.library.app.dto.OverdueBookReportDto;
import com.library.app.dto.PopularBookReportDto;
import com.library.app.dto.TopUserReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.library.app.dto.AvailableBookReportDto;
import com.library.app.dto.StaffAttendanceReportDto;
import java.time.LocalDate;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Finds the most borrowed books, ordered by the number of times they have been borrowed.
     *
     * @param limit The maximum number of books to return.
     * @return A list of popular books.
     */
    public List<PopularBookReportDto> findPopularBooks(int limit) {
        String sql = """
            SELECT TOP (?)
                   b.title,
                   b.author,
                   b.isbn,
                   COUNT(bo.book_id) AS borrow_count
            FROM Borrowings bo
            JOIN Books b ON bo.book_id = b.book_id
            GROUP BY b.title, b.author, b.isbn
            ORDER BY borrow_count DESC
            """;

        RowMapper<PopularBookReportDto> rowMapper = (rs, rowNum) -> {
            PopularBookReportDto dto = new PopularBookReportDto();
            dto.setTitle(rs.getString("title"));
            dto.setAuthor(rs.getString("author"));
            dto.setIsbn(rs.getString("isbn"));
            dto.setBorrowCount(rs.getInt("borrow_count"));
            return dto;
        };

        return jdbcTemplate.query(sql, new Object[]{limit}, rowMapper);
    }

    /**
     * Finds the top users based on the number of books they have borrowed.
     *
     * @param limit The maximum number of users to return.
     * @return A list of top users.
     */
    public List<TopUserReportDto> findTopUsers(int limit) {
        String sql = """
            SELECT TOP (?)
                   u.username,
                   u.full_name,
                   u.email,
                   COUNT(bo.user_id) AS borrow_count
            FROM Borrowings bo
            JOIN Users u ON bo.user_id = u.user_id
            GROUP BY u.username, u.full_name, u.email
            ORDER BY borrow_count DESC
            """;

        RowMapper<TopUserReportDto> rowMapper = (rs, rowNum) -> {
            TopUserReportDto dto = new TopUserReportDto();
            dto.setUsername(rs.getString("username"));
            dto.setFullName(rs.getString("full_name"));
            dto.setEmail(rs.getString("email"));
            dto.setBorrowCount(rs.getInt("borrow_count"));
            return dto;
        };


        return jdbcTemplate.query(sql, new Object[]{limit}, rowMapper);
    }
    // ... existing methods like findPopularBooks and findTopUsers ...

    /**
     * Finds all books that are currently borrowed and past their due date.
     * @return A list of overdue borrowings.
     */
    public List<OverdueBookReportDto> findOverdueBooks() {
        String sql = """
        SELECT
               b.title AS book_title,
               u.full_name AS borrower_name,
               u.email AS borrower_email,
               bo.due_date,
               DATEDIFF(day, bo.due_date, GETDATE()) AS days_overdue
        FROM Borrowings bo
        JOIN Books b ON bo.book_id = b.book_id
        JOIN Users u ON bo.user_id = u.user_id
        WHERE bo.status = 'BORROWED' AND bo.due_date < GETDATE()
        ORDER BY days_overdue DESC
        """;

        RowMapper<OverdueBookReportDto> rowMapper = (rs, rowNum) -> {
            OverdueBookReportDto dto = new OverdueBookReportDto();
            dto.setBookTitle(rs.getString("book_title"));
            dto.setBorrowerName(rs.getString("borrower_name"));
            dto.setBorrowerEmail(rs.getString("borrower_email"));
            dto.setDueDate(rs.getDate("due_date").toLocalDate());
            dto.setDaysOverdue(rs.getInt("days_overdue"));
            return dto;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
    public List<AvailableBookReportDto> findAvailableBooks() {
        String sql = """
        SELECT
               title,
               author,
               isbn,
               quantity,
               location
        FROM Books
        WHERE quantity > 0 AND status = 'AVAILABLE'
        ORDER BY title
        """;

        RowMapper<AvailableBookReportDto> rowMapper = (rs, rowNum) -> {
            AvailableBookReportDto dto = new AvailableBookReportDto();
            dto.setTitle(rs.getString("title"));
            dto.setAuthor(rs.getString("author"));
            dto.setIsbn(rs.getString("isbn"));
            dto.setQuantity(rs.getInt("quantity"));
            dto.setLocation(rs.getString("location"));
            return dto;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
    public List<StaffAttendanceReportDto> findTodaysStaffAttendance() {
        String sql = """
        SELECT
            u.full_name AS staff_name,
            u.role AS staff_role,
            sa.check_in_time,
            sa.check_out_time,
            sa.status
        FROM StaffAttendance sa
        JOIN Users u ON sa.user_id = u.user_id
        WHERE CONVERT(date, sa.check_in_time) = CONVERT(date, GETDATE())
          AND u.role IN ('STAFF', 'ADMIN')
        ORDER BY sa.check_in_time DESC
    """;

        RowMapper<StaffAttendanceReportDto> rowMapper = (rs, rowNum) -> {
            StaffAttendanceReportDto dto = new StaffAttendanceReportDto();
            dto.setStaffName(rs.getString("staff_name"));
            dto.setStaffRole(rs.getString("staff_role"));
            dto.setCheckInTime(rs.getTimestamp("check_in_time").toLocalDateTime());
            if (rs.getTimestamp("check_out_time") != null) {
                dto.setCheckOutTime(rs.getTimestamp("check_out_time").toLocalDateTime());
            }
            dto.setStatus(rs.getString("status"));
            return dto;
        };

        return jdbcTemplate.query(sql, rowMapper);
    }
}