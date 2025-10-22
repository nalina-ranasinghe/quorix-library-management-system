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
    private DatabaseService databaseService;

    // Configuration constants (remain the same)
    private static final int MAX_BORROWING_LIMIT = 5;
    private static final String ACTIVE_USER_STATUS = "Active";
    private static final String AVAILABLE_BOOK_STATUS = "Available";
    private static final String BORROWED_STATUS = "BORROWED";
    // RETURNED_STATUS constant is no longer strictly needed for this logic but can remain

    // borrowBook method (remains the same)
    @Transactional
    public String borrowBook(Integer userId, Integer bookId, Integer days) { /* ... as before ... */
        Optional<User> userOpt = databaseService.findUserById(userId);
        if (userOpt.isEmpty()) return "❌ Error: User not found with ID: " + userId;
        User user = userOpt.get();
        if (!ACTIVE_USER_STATUS.equalsIgnoreCase(user.getStatus())) return "🚫 Forbidden: User account is not '" + ACTIVE_USER_STATUS + "'. Cannot borrow.";
        Optional<Book> bookOpt = databaseService.findBookById(bookId);
        if (bookOpt.isEmpty()) return "❌ Error: Book not found with ID: " + bookId;
        Book book = bookOpt.get();
        if (book.getQuantity() <= 0 || !AVAILABLE_BOOK_STATUS.equalsIgnoreCase(book.getStatus())) return "⚠️ Warning: Book '" + book.getTitle() + "' is currently unavailable.";
        if (databaseService.hasActiveBorrowingByUserAndBook(userId, bookId)) return "⚠️ Warning: User already has an active loan for '" + book.getTitle() + "'.";
        int activeBorrowings = databaseService.countActiveBorrowingsByUser(userId);
        if (activeBorrowings >= MAX_BORROWING_LIMIT) return "⚠️ Warning: User has reached the maximum borrowing limit (" + MAX_BORROWING_LIMIT + ").";
        try {
            LocalDateTime borrowDate = LocalDateTime.now(); LocalDateTime dueDate = borrowDate.plusDays(days);
            databaseService.insertBorrowing(userId, bookId, borrowDate, dueDate);
            databaseService.updateBookQuantityAndStatus(bookId, book.getQuantity() - 1);
            return "✅ Success: Book '" + book.getTitle() + "' borrowed. Due: " + dueDate.toLocalDate();
        } catch (Exception e) { System.err.println("CRITICAL ERROR during borrowBook: " + e.getMessage()); e.printStackTrace(); return "❌ Error: System error during borrowing."; }
    }


    // *** MODIFIED returnBook METHOD ***
    @Transactional
    public String returnBook(Integer borrowingId) {
        // 1. Find Borrowing Record to get book ID
        Optional<Borrowing> borrowingOpt = databaseService.findBorrowingById(borrowingId);
        if (borrowingOpt.isEmpty()) {
            return "❌ Error: Borrowing record not found with ID: " + borrowingId;
        }
        Borrowing borrowing = borrowingOpt.get();

        // Optional: Check if already returned (though deletion handles this implicitly)
        // if (RETURNED_STATUS.equalsIgnoreCase(borrowing.getStatus())) {
        //     return "⚠️ Warning: Already returned.";
        // }

        // 2. Perform Return: Delete the record
        try {
            int rowsAffected = databaseService.deleteBorrowingById(borrowingId);
            if (rowsAffected == 0) {
                // Should not happen if findBorrowingById succeeded, but good practice
                return "❌ Error: Could not find borrowing record to delete (ID: " + borrowingId + ").";
            }

            // 3. Update Book Quantity (increment)
            Optional<Book> bookOpt = databaseService.findBookById(borrowing.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                databaseService.updateBookQuantityAndStatus(borrowing.getBookId(), book.getQuantity() + 1);
            } else {
                // Log a warning if the associated book is missing
                System.err.println("Warning: Book not found during return, ID: " + borrowing.getBookId() + " for Borrowing ID: " + borrowingId);
            }

            return "✅ Success: Book return processed (record deleted)."; // Updated message
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR during returnBook (delete): " + e.getMessage());
            e.printStackTrace();
            return "❌ Error: System error during return process.";
        }
    }
    // *** END OF MODIFIED METHOD ***


    // renewBook method (remains the same)
    @Transactional
    public String renewBook(Integer borrowingId, Integer additionalDays) { /* ... as before ... */
        if (borrowingId == null || borrowingId <= 0 || additionalDays == null || additionalDays <= 0) return "❌ Error: Invalid Borrowing ID or additional days provided.";
        Optional<Borrowing> borrowingOpt = databaseService.findBorrowingById(borrowingId);
        if (borrowingOpt.isEmpty()) return "❌ Error: Borrowing record not found with ID: " + borrowingId;
        Borrowing borrowing = borrowingOpt.get();
        if (!BORROWED_STATUS.equalsIgnoreCase(borrowing.getStatus())) return "⚠️ Warning: Cannot renew book with status: " + borrowing.getStatus();
        if (borrowing.getDueDate() == null) return "❌ Error: Borrowing record missing due date.";
        if (borrowing.getDueDate().isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())) return "⚠️ Warning: Cannot renew an overdue book.";
        try {
            LocalDateTime newDueDate = borrowing.getDueDate().plusDays(additionalDays);
            databaseService.updateBorrowingDueDate(borrowingId, newDueDate);
            return "✅ Success: Renewed. New due date: " + newDueDate.toLocalDate();
        } catch (Exception e) { System.err.println("CRITICAL ERROR during renewBook: " + e.getMessage()); e.printStackTrace(); return "❌ Error: System error during renewal."; }
    }


    // --- Methods for Existing UI (remain the same) ---
    public List<Borrowing> getUserBorrowings(Integer userId) { /* ... as before ... */
        if (userId == null || userId <= 0) return List.of();
        return databaseService.findBorrowingsByUserId(userId);
    }
    public List<Book> getAvailableBooks() { /* ... as before ... */
        return databaseService.findAvailableBooks();
    }
}