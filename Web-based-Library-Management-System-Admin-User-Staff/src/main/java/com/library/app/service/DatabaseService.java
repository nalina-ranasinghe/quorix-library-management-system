package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- RowMappers (userRowMapper, bookRowMapper, borrowingRowMapper remain the same) ---
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> { /* ... as before ... */
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        return user;
    };
    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> { /* ... as before ... */
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
    private final RowMapper<Borrowing> borrowingRowMapper = (rs, rowNum) -> { /* ... as before ... */
        Borrowing b = new Borrowing();
        b.setBorrowingId(rs.getInt("borrowing_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setBookId(rs.getInt("book_id"));
        Timestamp borrowTimestamp = rs.getTimestamp("borrow_date");
        b.setBorrowDate(borrowTimestamp != null ? borrowTimestamp.toLocalDateTime() : null);
        Timestamp dueTimestamp = rs.getTimestamp("due_date");
        b.setDueDate(dueTimestamp != null ? dueTimestamp.toLocalDateTime() : null);
        Timestamp returnTimestamp = rs.getTimestamp("return_date");
        b.setReturnDate(returnTimestamp != null ? returnTimestamp.toLocalDateTime() : null);
        b.setStatus(rs.getString("status"));
        return b;
    };


    // --- User and Book Methods (remain the same) ---
    public Optional<User> findUserById(Integer userId) { /* ... as before ... */
        String sql = "SELECT user_id, username, full_name, email, phone, role, status FROM Users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    public Optional<Book> findBookById(Integer bookId) { /* ... as before ... */
        String sql = "SELECT book_id, title, author, isbn, category, location, quantity, status FROM Books WHERE book_id = ?";
        try {
            Book book = jdbcTemplate.queryForObject(sql, bookRowMapper, bookId);
            return Optional.ofNullable(book);
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    public void updateBookQuantityAndStatus(Integer bookId, Integer newQuantity) { /* ... as before ... */
        if (newQuantity < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        String sql = "UPDATE Books SET quantity = ?, status = ? WHERE book_id = ?";
        String newStatus = (newQuantity > 0) ? "Available" : "Borrowed";
        jdbcTemplate.update(sql, newQuantity, newStatus, bookId);
    }


    // --- Borrowing Methods ---
    public Optional<Borrowing> findBorrowingById(Integer borrowingId) { /* ... as before ... */
        String sql = "SELECT borrowing_id, user_id, book_id, borrow_date, due_date, return_date, status FROM Borrowings WHERE borrowing_id = ?";
        try {
            Borrowing borrowing = jdbcTemplate.queryForObject(sql, borrowingRowMapper, borrowingId);
            return Optional.ofNullable(borrowing);
        } catch (EmptyResultDataAccessException e) { return Optional.empty(); }
    }
    public boolean hasActiveBorrowingByUserAndBook(Integer userId, Integer bookId) { /* ... as before ... */
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE user_id = ? AND book_id = ? AND status = 'BORROWED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, bookId);
        return count != null && count > 0;
    }
    public int countActiveBorrowingsByUser(Integer userId) { /* ... as before ... */
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE user_id = ? AND status = 'BORROWED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return (count != null) ? count : 0;
    }
    public void insertBorrowing(Integer userId, Integer bookId, LocalDateTime borrowDate, LocalDateTime dueDate) { /* ... as before ... */
        String sql = "INSERT INTO Borrowings (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        jdbcTemplate.update(sql, userId, bookId, borrowDate, dueDate);
    }
    public void updateBorrowingDueDate(Integer borrowingId, LocalDateTime newDueDate) { /* ... as before ... */
        String sql = "UPDATE Borrowings SET due_date = ? WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, newDueDate, borrowingId);
    }

    // --- REMOVED updateBorrowingStatusAndReturnDate ---
    // public void updateBorrowingStatusAndReturnDate(...)

    // --- NEW: Method to delete a borrowing record by ID ---
    /**
     * Deletes a borrowing record from the database.
     * @param borrowingId The ID of the borrowing record to delete.
     * @return The number of rows affected (should be 1 if successful).
     */
    public int deleteBorrowingById(Integer borrowingId) {
        String sql = "DELETE FROM Borrowings WHERE borrowing_id = ?";
        return jdbcTemplate.update(sql, borrowingId);
    }
    // --- End of New Method ---


    // --- Methods for Existing UI (remain the same) ---
    public List<Borrowing> findBorrowingsByUserId(Integer userId) { /* ... as before ... */
        String sql = """
            SELECT b.*, u.full_name as user_name, bk.title as book_title
            FROM Borrowings b JOIN Users u ON b.user_id = u.user_id JOIN Books bk ON b.book_id = bk.book_id
            WHERE b.user_id = ? ORDER BY b.borrow_date DESC
            """;
        RowMapper<Borrowing> detailedBorrowingMapper = (rs, rowNum) -> {
            Borrowing borrowing = borrowingRowMapper.mapRow(rs, rowNum);
            borrowing.setUserName(rs.getString("user_name"));
            borrowing.setBookTitle(rs.getString("book_title"));
            return borrowing;
        };
        return jdbcTemplate.query(sql, detailedBorrowingMapper, userId);
    }
    public List<Book> findAvailableBooks() { /* ... as before ... */
        String sql = "SELECT book_id, title, author, isbn, category, location, quantity, status FROM Books WHERE quantity > 0 AND status = 'Available'";
        return jdbcTemplate.query(sql, bookRowMapper);
    }
    public boolean hasBorrowingRecords(Integer bookId) { /* ... as before ... */
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE book_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
        return count != null && count > 0;
    }
    public void deleteBorrowingRecordsForBook(Integer bookId) { /* ... as before ... */
        String sql = "DELETE FROM Borrowings WHERE book_id = ?";
        jdbcTemplate.update(sql, bookId);
    }
}