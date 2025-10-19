package com.library.app.repository;

import com.library.app.dto.MostBorrowedBookDto;
import com.library.app.dto.UserBorrowingDto;
import com.library.app.entity.Borrowing;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import com.library.app.dto.MostBorrowedBookDto;
import org.springframework.jdbc.core.RowMapper;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BorrowingRepository {

    private final JdbcTemplate jdbcTemplate;

    // == Reporting Methods ==
    public List<MostBorrowedBookDto> findMostBorrowedBooks(int limit) {
        String sql = "SELECT TOP (?) b.title, b.author, COUNT(bo.book_id) as borrow_count " +
                "FROM Borrowings bo " +
                "JOIN Books b ON bo.book_id = b.book_id " +
                "GROUP BY b.title, b.author " +
                "ORDER BY borrow_count DESC";

        RowMapper<MostBorrowedBookDto> rowMapper = (rs, rowNum) -> {
            MostBorrowedBookDto dto = new MostBorrowedBookDto();
            dto.setTitle(rs.getString("title"));
            dto.setAuthor(rs.getString("author"));
            dto.setBorrowCount(rs.getInt("borrow_count"));
            return dto;
        };
        return jdbcTemplate.query(sql, new Object[]{limit}, rowMapper);
    }

    // Method to count borrowings in the last 30 days
    public int countTotalBorrowingsLast30Days() {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE borrow_date >= DATEADD(day, -30, GETDATE())";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    // == User-Facing Methods ==
    public List<UserBorrowingDto> findBorrowingsByUserId(int userId) {
        String sql = "SELECT bo.borrowing_id, b.title, b.author, bo.borrow_date, bo.due_date, bo.status " +
                "FROM Borrowings bo " +
                "JOIN Books b ON bo.book_id = b.book_id " +
                "WHERE bo.user_id = ? " +
                "ORDER BY bo.borrow_date DESC";

        RowMapper<UserBorrowingDto> rowMapper = (rs, rowNum) -> {
            UserBorrowingDto dto = new UserBorrowingDto();
            dto.setBorrowingId(rs.getInt("borrowing_id"));
            dto.setBookTitle(rs.getString("title"));
            dto.setBookAuthor(rs.getString("author"));
            dto.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
            dto.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
            dto.setStatus(rs.getString("status"));
            return dto;
        };
        return jdbcTemplate.query(sql, new Object[]{userId}, rowMapper);
    }

    public Optional<Borrowing> findById(int borrowingId) {
        String sql = "SELECT * FROM Borrowings WHERE borrowing_id = ?";
        RowMapper<Borrowing> rowMapper = (rs, rowNum) -> {
            Borrowing b = new Borrowing();
            b.setBorrowingId(rs.getInt("borrowing_id"));
            b.setUserId(rs.getInt("user_id"));
            b.setBookId(rs.getInt("book_id"));
            b.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
            b.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
            if (rs.getTimestamp("return_date") != null) {
                b.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
            }
            b.setStatus(rs.getString("status"));
            return b;
        };
        return jdbcTemplate.query(sql, new Object[]{borrowingId}, rowMapper).stream().findFirst();
    }

    public void renew(int borrowingId) {
        String sql = "UPDATE Borrowings SET due_date = DATEADD(day, 7, due_date) WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, borrowingId);
    }


    /**
     * Updates a borrowing record to 'RETURNED' and sets the return date to the current time.
     * @param borrowingId The ID of the borrowing record to update.
     */
    public void updateStatusToReturned(int borrowingId) {
        String sql = "UPDATE Borrowings SET status = 'RETURNED', return_date = GETDATE() WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, borrowingId);
    }
}