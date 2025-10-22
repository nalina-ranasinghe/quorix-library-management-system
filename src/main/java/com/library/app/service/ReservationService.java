package com.library.app.service;

import com.library.app.dto.UserReservationDto;
import com.library.app.entity.Book;
import com.library.app.entity.Reservation;
import com.library.app.entity.User;
import com.library.app.repository.BookRepository;
import com.library.app.repository.ReservationRepository;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // NOTIFICATION INJECTION
    private final NotificationService notificationService;

    @Transactional
    public void reserveBook(int bookId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Cannot make a reservation."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book with ID " + bookId + " not found."));

        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Sorry, '" + book.getTitle() + "' is out of stock and cannot be reserved.");
        }
        if (reservationRepository.hasActiveReservation(user.getUserId(), bookId)) {
            throw new IllegalStateException("You have already reserved a copy of this book.");
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getUserId());
        reservation.setBookId(book.getBookId());
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setStatus("ACTIVE");
        reservationRepository.save(reservation);

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.update(book);

        //NOTIFICATION TRIGGER: Send a confirmation alert
        String message = "You have successfully reserved '" + book.getTitle() + "'.";
        notificationService.createNotification(user.getUserId(), message, "RESERVATION_CONFIRMATION");
    }

    public List<UserReservationDto> getReservedBooksForUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return reservationRepository.findActiveReservationsByUserId(user.getUserId());
    }

    @Transactional
    public void cancelReservation(int reservationId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new SecurityException("User not found."));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (reservation.getUserId() != user.getUserId()) {
            throw new SecurityException("You are not authorized to cancel this reservation.");
        }
        Book book = bookRepository.findById(reservation.getBookId())
                .orElseThrow(() -> new IllegalStateException("Associated book could not be found."));

        book.setQuantity(book.getQuantity() + 1);
        bookRepository.update(book);

        reservationRepository.deleteById(reservationId);
    }
}