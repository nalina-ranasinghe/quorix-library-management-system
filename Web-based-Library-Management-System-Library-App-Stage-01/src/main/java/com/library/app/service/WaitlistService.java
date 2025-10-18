package com.library.app.service;

import com.library.app.dto.UserWaitlistDto;
import com.library.app.entity.Book;
import com.library.app.entity.User;
import com.library.app.entity.Waitlist;
import com.library.app.repository.BookRepository;
import com.library.app.repository.UserRepository;
import com.library.app.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    // --- NOTIFICATION INJECTION ---
    private final NotificationService notificationService;

    /**
     * Adds a user to the waitlist for a book and triggers a confirmation notification.
     */
    public void addToWaitlist(int bookId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found."));

        if (book.getQuantity() > 0) {
            throw new IllegalStateException("This book is available and cannot be waitlisted.");
        }
        if (waitlistRepository.existsByUserIdAndBookId(user.getUserId(), bookId)) {
            throw new IllegalStateException("You are already on the waitlist for this book.");
        }

        Waitlist waitlistEntry = new Waitlist();
        waitlistEntry.setUserId(user.getUserId());
        waitlistEntry.setBookId(bookId);
        waitlistEntry.setWaitlistedAt(LocalDateTime.now());
        waitlistEntry.setNotified(false);
        waitlistRepository.save(waitlistEntry);


        String message = "You have been added to the waitlist for '" + book.getTitle() + "'. We'll notify you when it's available.";
        notificationService.createNotification(user.getUserId(), message, "WAITLIST_JOIN");
    }

    public List<UserWaitlistDto> getWaitlistedBooksForUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return waitlistRepository.findWaitlistedBooksByUserId(user.getUserId());
    }

    @Transactional
    public void cancelWaitlist(int waitlistId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new SecurityException("User not found."));

        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found."));

        // Security check to ensure the user owns this waitlist entry
        if (waitlist.getUserId() != user.getUserId()) {
            throw new SecurityException("You are not authorized to cancel this waitlist item.");
        }

        waitlistRepository.deleteById(waitlistId);
    }
}