package com.library.app.service;

import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowService {

    @Autowired
    private DatabaseService databaseService;

    @Transactional
    public String borrowBook(Integer userId, Integer bookId, Integer days) {
        try {
            // Check if user exists
            User user = databaseService.getUserById(userId);
            if (user == null) {
                return "User not found";
            }

            // Check if book exists and is available
            Book book = databaseService.getBookById(bookId);
            if (book == null) {
                return "Book not found";
            }

            if (book.getQuantity() <= 0 || !"Available".equals(book.getStatus())) {
                return "Book is not available for borrowing";
            }

            // Check if user already borrowed this book
            if (databaseService.hasActiveBorrowing(userId, bookId)) {
                return "User has already borrowed this book";
            }

            // Check user's active borrowings limit
            Integer activeBorrowings = databaseService.countActiveBorrowingsByUser(userId);
            if (activeBorrowings >= 5) {
                return "User has reached maximum borrowing limit (5 books)";
            }

            // Create borrowing record
            LocalDateTime borrowDate = LocalDateTime.now();
            LocalDateTime dueDate = borrowDate.plusDays(days);
            databaseService.createBorrowing(userId, bookId, borrowDate, dueDate);

            // Update book quantity (this will also update status to "Borrowed" if quantity becomes 0)
            databaseService.updateBookQuantity(bookId, book.getQuantity() - 1);

            return "Book borrowed successfully. Due date: " + dueDate;

        } catch (Exception e) {
            return "Error borrowing book: " + e.getMessage();
        }
    }

    @Transactional
    public String returnBook(Integer borrowingId) {
        try {
            Borrowing borrowing = databaseService.getBorrowingById(borrowingId);
            if (borrowing == null) {
                return "Borrowing record not found";
            }

            if ("RETURNED".equals(borrowing.getStatus())) {
                return "Book is already returned";
            }

            // Update borrowing record
            LocalDateTime returnDate = LocalDateTime.now();
            databaseService.updateBorrowingStatus(borrowingId, "RETURNED", returnDate);

            // Update book quantity
            Book book = databaseService.getBookById(borrowing.getBookId());
            if (book != null) {
                databaseService.updateBookQuantity(borrowing.getBookId(), book.getQuantity() + 1);
            }

            return "Book returned successfully. Return date: " + returnDate;

        } catch (Exception e) {
            return "Error returning book: " + e.getMessage();
        }
    }

    @Transactional
    public String renewBook(Integer borrowingId, Integer additionalDays) {
        try {
            Borrowing borrowing = databaseService.getBorrowingById(borrowingId);
            if (borrowing == null) {
                return "Borrowing record not found";
            }

            if (!"BORROWED".equals(borrowing.getStatus())) {
                return "Only borrowed books can be renewed";
            }

            if (borrowing.getDueDate().isBefore(LocalDateTime.now())) {
                return "Cannot renew overdue book";
            }

            // Extend due date
            LocalDateTime newDueDate = borrowing.getDueDate().plusDays(additionalDays);
            databaseService.updateBorrowingDueDate(borrowingId, newDueDate);

            return "Book renewed successfully. New due date: " + newDueDate;

        } catch (Exception e) {
            return "Error renewing book: " + e.getMessage();
        }
    }

    public List<Borrowing> getUserBorrowings(Integer userId) {
        return databaseService.getUserBorrowings(userId);
    }

    public List<Book> getAvailableBooks() {
        return databaseService.getAvailableBooks();
    }
}
