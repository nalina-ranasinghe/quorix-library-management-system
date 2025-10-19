package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // User RowMapper
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        return user;
    };

    // Book RowMapper
    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> {
        Book book = new Book();
        book.setId(rs.getLong("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setCategory(rs.getString("category"));
        book.setLocation(rs.getString("location"));
        book.setQuantity(rs.getInt("quantity"));
        book.setStatus(rs.getString("status"));
        return book;
    };

    // Borrowing RowMapper
    private final RowMapper<Borrowing> borrowingRowMapper = (rs, rowNum) -> {
        Borrowing borrowing = new Borrowing();
        borrowing.setBorrowingId(rs.getInt("borrowing_id"));
        borrowing.setUserId(rs.getInt("user_id"));
        borrowing.setBookId(rs.getInt("book_id"));
        borrowing.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());

        if (rs.getTimestamp("due_date") != null) {
            borrowing.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
        }

        if (rs.getTimestamp("return_date") != null) {
            borrowing.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
        }

        borrowing.setStatus(rs.getString("status"));
        return borrowing;
    };

    // User methods
    public User getUserById(Integer userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
        } catch (Exception e) {
            return null;
        }
    }

    // Book methods
    public Book getBookById(Integer bookId) {
        String sql = "SELECT * FROM Books WHERE book_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, bookRowMapper, bookId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Book> getAvailableBooks() {
        String sql = "SELECT * FROM Books WHERE quantity > 0 AND status = 'Available'";
        return jdbcTemplate.query(sql, bookRowMapper);
    }

    public void updateBookQuantity(Integer bookId, Integer newQuantity) {
        String sql = "UPDATE Books SET quantity = ?, status = ? WHERE book_id = ?";
        String status = newQuantity > 0 ? "Available" : "Borrowed";
        jdbcTemplate.update(sql, newQuantity, status, bookId);
    }

    // Borrowing methods
    public List<Borrowing> getUserBorrowings(Integer userId) {
        String sql = "SELECT b.*, u.full_name as user_name, bk.title as book_title " +
                "FROM Borrowings b " +
                "JOIN Users u ON b.user_id = u.user_id " +
                "JOIN Books bk ON b.book_id = bk.book_id " +
                "WHERE b.user_id = ? ORDER BY b.borrow_date DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Borrowing borrowing = new Borrowing();
            borrowing.setBorrowingId(rs.getInt("borrowing_id"));
            borrowing.setUserId(rs.getInt("user_id"));
            borrowing.setBookId(rs.getInt("book_id"));
            borrowing.setUserName(rs.getString("user_name"));
            borrowing.setBookTitle(rs.getString("book_title"));
            borrowing.setBorrowDate(rs.getTimestamp("borrow_date").toLocalDateTime());
            borrowing.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());

            if (rs.getTimestamp("return_date") != null) {
                borrowing.setReturnDate(rs.getTimestamp("return_date").toLocalDateTime());
            }

            borrowing.setStatus(rs.getString("status"));
            return borrowing;
        }, userId);
    }

    public Borrowing getBorrowingById(Integer borrowingId) {
        String sql = "SELECT * FROM Borrowings WHERE borrowing_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, borrowingRowMapper, borrowingId);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasActiveBorrowing(Integer userId, Integer bookId) {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE user_id = ? AND book_id = ? AND status = 'BORROWED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, bookId);
        return count != null && count > 0;
    }

    public Integer countActiveBorrowingsByUser(Integer userId) {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE user_id = ? AND status = 'BORROWED'";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, userId);
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer createBorrowing(Integer userId, Integer bookId, LocalDateTime borrowDate, LocalDateTime dueDate) {
        String sql = "INSERT INTO Borrowings (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        jdbcTemplate.update(sql, userId, bookId, borrowDate, dueDate);

        // Return the generated borrowing ID
        String idSql = "SELECT MAX(borrowing_id) FROM Borrowings";
        return jdbcTemplate.queryForObject(idSql, Integer.class);
    }

    public void updateBorrowingStatus(Integer borrowingId, String status, LocalDateTime returnDate) {
        String sql = "UPDATE Borrowings SET status = ?, return_date = ? WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, status, returnDate, borrowingId);
    }

    public void updateBorrowingDueDate(Integer borrowingId, LocalDateTime newDueDate) {
        String sql = "UPDATE Borrowings SET due_date = ? WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, newDueDate, borrowingId);
    }

    // Check if a book has any borrowing records
    public boolean hasBorrowingRecords(Integer bookId) {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE book_id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Delete all borrowing records for a book (for cascade deletion)
    public void deleteBorrowingRecordsForBook(Integer bookId) {
        String sql = "DELETE FROM Borrowings WHERE book_id = ?";
        jdbcTemplate.update(sql, bookId);
    }
}
