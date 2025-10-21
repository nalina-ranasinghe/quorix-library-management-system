package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException; // Import for exception handling
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet; // Required for RowMapper lambda
import java.sql.SQLException; // Required for RowMapper lambda
import java.sql.Timestamp; // For handling dates
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- RowMappers ---

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        // Assuming password hash is needed for some logic, otherwise remove
        // user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status")); // Crucial for borrow check
        // Add createdAt/updatedAt if needed
        return user;
    };

    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> {
        Book book = new Book();
        book.setId(rs.getLong("book_id")); // Use Long if your entity uses Long
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setCategory(rs.getString("category"));
        book.setLocation(rs.getString("location"));
        book.setQuantity(rs.getInt("quantity")); // Crucial for borrow check
        book.setStatus(rs.getString("status"));   // Crucial for borrow check
        // Add createdAt/updatedAt if needed
        return book;
    };

    private final RowMapper<Borrowing> borrowingRowMapper = (rs, rowNum) -> {
        Borrowing b = new Borrowing();
        b.setBorrowingId(rs.getInt("borrowing_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setBookId(rs.getInt("book_id"));
        // Handle potential null timestamps gracefully
        Timestamp borrowTimestamp = rs.getTimestamp("borrow_date");
        b.setBorrowDate(borrowTimestamp != null ? borrowTimestamp.toLocalDateTime() : null);
        Timestamp dueTimestamp = rs.getTimestamp("due_date");
        b.setDueDate(dueTimestamp != null ? dueTimestamp.toLocalDateTime() : null);
        Timestamp returnTimestamp = rs.getTimestamp("return_date");
        b.setReturnDate(returnTimestamp != null ? returnTimestamp.toLocalDateTime() : null);
        b.setStatus(rs.getString("status"));
        // Add joined fields if needed by specific queries (like getUserBorrowings)
        // b.setUserName(rs.getString("user_name")); // Example
        // b.setBookTitle(rs.getString("book_title")); // Example
        return b;
    };

    // --- User Methods ---

    public Optional<User> findUserById(Integer userId) {
        String sql = "SELECT user_id, username, full_name, email, phone, role, status FROM Users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // Return empty Optional if not found
        }
    }

    // --- Book Methods ---

    public Optional<Book> findBookById(Integer bookId) {
        // Ensure column names match your actual table definition
        String sql = "SELECT book_id, title, author, isbn, category, location, quantity, status FROM Books WHERE book_id = ?";
        try {
            Book book = jdbcTemplate.queryForObject(sql, bookRowMapper, bookId);
            return Optional.ofNullable(book);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /** Updates book quantity and automatically sets status based on quantity. */
    public void updateBookQuantityAndStatus(Integer bookId, Integer newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative."); // Basic validation
        }
        String sql = "UPDATE Books SET quantity = ?, status = ? WHERE book_id = ?";
        // Determine status based on the new quantity
        String newStatus = (newQuantity > 0) ? "Available" : "Borrowed"; // Or "Unavailable" if 0 means out of stock
        jdbcTemplate.update(sql, newQuantity, newStatus, bookId);
    }

    // --- Borrowing Methods ---

    public Optional<Borrowing> findBorrowingById(Integer borrowingId) {
        String sql = "SELECT borrowing_id, user_id, book_id, borrow_date, due_date, return_date, status FROM Borrowings WHERE borrowing_id = ?";
        try {
            Borrowing borrowing = jdbcTemplate.queryForObject(sql, borrowingRowMapper, borrowingId);
            return Optional.ofNullable(borrowing);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /** Checks if a user has an active loan for a specific book. */
    public boolean hasActiveBorrowingByUserAndBook(Integer userId, Integer bookId) {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE user_id = ? AND book_id = ? AND status = 'BORROWED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, bookId);
        return count != null && count > 0;
    }

    /** Counts active loans for a specific user. */
    public int countActiveBorrowingsByUser(Integer userId) {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE user_id = ? AND status = 'BORROWED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return (count != null) ? count : 0; // Return 0 if null
    }

    /** Inserts a new borrowing record. */
    public void insertBorrowing(Integer userId, Integer bookId, LocalDateTime borrowDate, LocalDateTime dueDate) {
        String sql = "INSERT INTO Borrowings (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        jdbcTemplate.update(sql, userId, bookId, borrowDate, dueDate);
        // Note: We don't retrieve the ID here, adjust if needed
    }

    /** Updates the status and return date of a borrowing record. */
    public void updateBorrowingStatusAndReturnDate(Integer borrowingId, String status, LocalDateTime returnDate) {
        String sql = "UPDATE Borrowings SET status = ?, return_date = ? WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, status, returnDate, borrowingId);
    }

    /** Updates the due date of a borrowing record. */
    public void updateBorrowingDueDate(Integer borrowingId, LocalDateTime newDueDate) {
        String sql = "UPDATE Borrowings SET due_date = ? WHERE borrowing_id = ?";
        jdbcTemplate.update(sql, newDueDate, borrowingId);
    }

    // --- Methods for Existing UI ---

    /** Fetches borrowings for a user, joining with User and Book tables. */
    public List<Borrowing> findBorrowingsByUserId(Integer userId) {
        String sql = """
            SELECT
                b.borrowing_id, b.user_id, b.book_id, b.borrow_date, b.due_date, b.return_date, b.status,
                u.full_name as user_name,
                bk.title as book_title
            FROM Borrowings b
            JOIN Users u ON b.user_id = u.user_id
            JOIN Books bk ON b.book_id = bk.book_id
            WHERE b.user_id = ?
            ORDER BY b.borrow_date DESC
            """;

        // RowMapper specific to this query with joined fields
        RowMapper<Borrowing> detailedBorrowingMapper = (rs, rowNum) -> {
            Borrowing borrowing = borrowingRowMapper.mapRow(rs, rowNum); // Use base mapper first
            borrowing.setUserName(rs.getString("user_name"));
            borrowing.setBookTitle(rs.getString("book_title"));
            return borrowing;
        };

        return jdbcTemplate.query(sql, detailedBorrowingMapper, userId);
    }

    /** Fetches books currently available. */
    public List<Book> findAvailableBooks() {
        String sql = "SELECT book_id, title, author, isbn, category, location, quantity, status FROM Books WHERE quantity > 0 AND status = 'Available'";
        return jdbcTemplate.query(sql, bookRowMapper);
    }

    // You might need these from your original DatabaseService if BookService uses them
    public boolean hasBorrowingRecords(Integer bookId) {
        String sql = "SELECT COUNT(*) FROM Borrowings WHERE book_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, bookId);
        return count != null && count > 0;
    }

    public void deleteBorrowingRecordsForBook(Integer bookId) {
        String sql = "DELETE FROM Borrowings WHERE book_id = ?";
        jdbcTemplate.update(sql, bookId);
    }
}