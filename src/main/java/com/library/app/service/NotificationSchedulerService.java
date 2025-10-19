package com.library.app.service;

import com.library.app.dto.UserBorrowingDto;
import com.library.app.entity.User;
import com.library.app.repository.BorrowingRepository;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationSchedulerService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Run daily at 8 AM to check for due dates
    @Scheduled(cron = "0 0 8 * * ?" ) // 8:00 AM every day
    public void checkDueDateReminders() {
        // Get all users
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            // Get current borrowings for this user
            List<UserBorrowingDto> userBorrowings = borrowingRepository.findBorrowingsByUserId(user.getUserId());

            for (UserBorrowingDto borrowing : userBorrowings) {
                // Only check active borrowings (not returned)
                if ("BORROWED".equalsIgnoreCase(borrowing.getStatus()) || "ACTIVE".equalsIgnoreCase(borrowing.getStatus())) {
                    LocalDateTime dueDate = borrowing.getDueDate();
                    LocalDateTime today = LocalDateTime.now();

                    long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

                    // Send reminder if due in 3 days or less (but not overdue yet)
                    if (daysUntilDue <= 3 && daysUntilDue >= 0) {
                        notificationService.sendDueDateReminder(
                                user.getUserId(),
                                borrowing.getBookTitle(),
                                (int) daysUntilDue
                        );
                        System.out.println("Due date reminder sent for: " + borrowing.getBookTitle());
                    }

                    // Send overdue alert if past due date
                    if (daysUntilDue < 0) {
                        int daysOverdue = (int) Math.abs(daysUntilDue);
                        notificationService.sendOverdueAlert(
                                user.getUserId(),
                                borrowing.getBookTitle(),
                                daysOverdue
                        );
                        System.out.println("Overdue alert sent for: " + borrowing.getBookTitle());
                    }
                }
            }
        }
        System.out.println("Due date check completed at: " + LocalDateTime.now());
    }
}