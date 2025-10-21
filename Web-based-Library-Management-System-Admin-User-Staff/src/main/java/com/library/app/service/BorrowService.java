package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {

    @Autowired
    private DatabaseService databaseService; // Uses the new JDBC service

    // Configuration constants
    private static final int MAX_BORROWING_LIMIT = 5;
    private static final String ACTIVE_USER_STATUS = "Active";
    private static final String AVAILABLE_BOOK_STATUS = "Available";
    private static final String BORROWED_STATUS = "BORROWED";
    private static final String RETURNED_STATUS = "RETURNED";

    @Transactional
    public String borrowBook(Integer userId, Integer bookId, Integer days) {
        // 1. Validate User
        Optional<User> userOpt = databaseService.findUserById(userId);
        if (userOpt.isEmpty()) {
            return "❌ Error: User not found with ID: " + userId;
        }
        User user = userOpt.get();
        if (!ACTIVE_USER_STATUS.equalsIgnoreCase(user.getStatus())) {
            return "🚫 Forbidden: User account is not '" + ACTIVE_USER_STATUS + "'. Cannot borrow.";
        }

        // 2. Validate Book
        Optional<Book> bookOpt = databaseService.findBookById(bookId);
        if (bookOpt.isEmpty()) {
            return "❌ Error: Book not found with ID: " + bookId;
        }
        Book book = bookOpt.get();
        if (book.getQuantity() <= 0 || !AVAILABLE_BOOK_STATUS.equalsIgnoreCase(book.getStatus())) {
            return "⚠️ Warning: Book '" + book.getTitle() + "' is currently unavailable.";
        }

        // 3. Check Existing Borrowing
        if (databaseService.hasActiveBorrowingByUserAndBook(userId, bookId)) {
            return "⚠️ Warning: User already has an active loan for '" + book.getTitle() + "'.";
        }

        // 4. Check Borrowing Limit
        int activeBorrowings = databaseService.countActiveBorrowingsByUser(userId);
        if (activeBorrowings >= MAX_BORROWING_LIMIT) {
            return "⚠️ Warning: User has reached the maximum borrowing limit (" + MAX_BORROWING_LIMIT + ").";
        }

        // 5. Perform Borrowing
        try {
            LocalDateTime borrowDate = LocalDateTime.now();
            LocalDateTime dueDate = borrowDate.plusDays(days);
            databaseService.insertBorrowing(userId, bookId, borrowDate, dueDate);
            databaseService.updateBookQuantityAndStatus(bookId, book.getQuantity() - 1); // Use combined method

            return "✅ Success: Book '" + book.getTitle() + "' borrowed. Due: " + dueDate.toLocalDate();
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during borrowBook: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error: System error during borrowing.";
        }
    }

    @Transactional
    public String returnBook(Integer borrowingId) {
        // 1. Find Borrowing Record
        Optional<Borrowing> borrowingOpt = databaseService.findBorrowingById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return "❌ Error: Borrowing record not found with ID: " + borrowingId;
        }
        Borrowing borrowing = borrowingOpt.get();
        if (RETURNED_STATUS.equalsIgnoreCase(borrowing.getStatus())) {
            return "⚠️ Warning: Already returned.";
        }

        // 2. Perform Return
        try {
            LocalDateTime returnDate = LocalDateTime.now();
            databaseService.updateBorrowingStatusAndReturnDate(borrowingId, RETURNED_STATUS, returnDate);

            // 3. Update Book Quantity
            Optional<Book> bookOpt = databaseService.findBookById(borrowing.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                databaseService.updateBookQuantityAndStatus(borrowing.getBookId(), book.getQuantity() + 1); // Use combined method
            } else {
                System.err.println("Warning: Book not found for return, ID: " + borrowing.getBookId());
            }

            return "✅ Success: Book returned.";
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during returnBook: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error: System error during return.";
        }
    }

    @Transactional
    public String renewBook(Integer borrowingId, Integer additionalDays) {
        // 1. Validate Inputs
        if (borrowingId == null || borrowingId <= 0 || additionalDays == null || additionalDays <= 0) {
            return "❌ Error: Invalid Borrowing ID or additional days provided.";
        }

        // 2. Find Borrowing Record
        Optional<Borrowing> borrowingOpt = databaseService.findBorrowingById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return "❌ Error: Borrowing record not found with ID: " + borrowingId;
        }
        Borrowing borrowing = borrowingOpt.get();

        // 3. Validate Renewal Eligibility
        if (!BORROWED_STATUS.equalsIgnoreCase(borrowing.getStatus())) {
            return "⚠️ Warning: Cannot renew book with status: " + borrowing.getStatus();
        }
        if (borrowing.getDueDate() == null) {
            return "❌ Error: Borrowing record missing due date.";
        }
        // Use start of day for comparison to avoid time issues
        if (borrowing.getDueDate().isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())) {
            return "⚠️ Warning: Cannot renew an overdue book.";
        }

        // 4. Perform Renewal
        try {
            LocalDateTime newDueDate = borrowing.getDueDate().plusDays(additionalDays);
            databaseService.updateBorrowingDueDate(borrowingId, newDueDate);
            return "✅ Success: Renewed. New due date: " + newDueDate.toLocalDate();
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during renewBook: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error: System error during renewal.";
        }
    }

    // --- Methods for Existing UI ---
    public List<Borrowing> getUserBorrowings(Integer userId) {
        if (userId == null || userId <= 0) return List.of();
        return databaseService.findBorrowingsByUserId(userId);
    }

    public List<Book> getAvailableBooks() {
        return databaseService.findAvailableBooks();
    }
}