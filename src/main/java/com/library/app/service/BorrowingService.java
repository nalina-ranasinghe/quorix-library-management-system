package com.library.app.service;

import com.library.app.dto.UserBorrowingDto;
import com.library.app.entity.Book;
import com.library.app.entity.Borrowing;
import com.library.app.entity.User;
import com.library.app.repository.BookRepository;
import com.library.app.repository.BorrowingRepository;
import com.library.app.repository.UserRepository;
import com.library.app.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // --- NOTIFICATION INJECTION ---
    private final WaitlistRepository waitlistRepository;
    private final NotificationService notificationService;

    public List<UserBorrowingDto> getBorrowedBooksForUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return borrowingRepository.findBorrowingsByUserId(user.getUserId());
    }

    @Transactional
    public void renewBorrowing(int borrowingId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new SecurityException("User not found."));
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException("Borrowing record not found."));
        if (borrowing.getUserId() != user.getUserId()) {
            throw new SecurityException("You are not authorized to renew this item.");
        }
        if (!"BORROWED".equalsIgnoreCase(borrowing.getStatus())) {
            throw new IllegalStateException("This book has already been returned and cannot be renewed.");
        }
        borrowingRepository.renew(borrowingId);
    }

    /**
     * This method now handles the logic for a book return.
     * It updates the book's quantity and then checks the waitlist
     * to notify the next user if the book becomes available.
     */
    @Transactional
    public void returnBook(int borrowingId) {
        // Find the original borrowing record
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException("Borrowing record not found."));

        if (!"BORROWED".equalsIgnoreCase(borrowing.getStatus())) {
            throw new IllegalStateException("This book has already been returned.");
        }

        // Update the borrowing record to 'RETURNED'
        borrowingRepository.updateStatusToReturned(borrowingId);

        // Find the book and increment its available quantity
        Book book = bookRepository.findById(borrowing.getBookId())
                .orElseThrow(() -> new IllegalStateException("Book associated with this borrowing not found."));
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.update(book);

        // --- NOTIFICATION TRIGGER: Check waitlist and notify user ---
        Optional<User> waitlistedUser = waitlistRepository.findFirstUserInWaitlist(book.getBookId());

        waitlistedUser.ifPresent(user -> {
            String message = "Good news! A copy of '" + book.getTitle() + "' is now available for you to reserve.";
            notificationService.createNotification(user.getUserId(), message, "WAITLIST_AVAILABILITY");

            // Remove the notified user from the waitlist
            waitlistRepository.deleteByUserIdAndBookId(user.getUserId(), book.getBookId());
        });
    }
}