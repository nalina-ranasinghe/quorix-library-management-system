package com.library.app.service;

import com.library.app.entity.Notification;
import com.library.app.entity.User;
import com.library.app.repository.NotificationRepository;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailServiceForNotifications emailService;


    private final Map<Integer, UserNotificationPreferences> userPreferences = new ConcurrentHashMap<>();
    private final Map<Integer, Announcement> announcements = new ConcurrentHashMap<>();
    private final AtomicInteger announcementIdCounter = new AtomicInteger(1);

    public void createNotification(Integer userId, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot create notification for non-existent user."));

        Notification notification = new Notification(userId, message, type);
        notificationRepository.save(notification);

        if (getUserPreferences(userId).isReceiveEmailNotifications()) {
            emailService.sendNotificationEmail(user.getEmail(), user.getFullName(), type, message);
        }
    }

    public void deleteNotification(int notificationId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new SecurityException("User not found."));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        if (notification.getUserId() != user.getUserId()) {
            throw new SecurityException("You are not authorized to delete this notification.");
        }
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Creates an in-memory announcement AND sends an email to all users.
     */
    public void createAnnouncement(String title, String message, String createdBy) {
        // 1. Save the announcement in memory (your original code)
        int id = announcementIdCounter.getAndIncrement();
        announcements.put(id, new Announcement(id, title, message, createdBy, LocalDateTime.now()));

        // --- 2. NEW: Send an email to all users ---
        String fullMessage = String.format("Title: %s. Message: %s", title, message);
        String notificationType = "ANNOUNCEMENT";

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // Check each user's email preferences before sending
            if (getUserPreferences(user.getUserId()).isReceiveEmailNotifications()) {
                emailService.sendNotificationEmail(user.getEmail(), user.getFullName(), notificationType, fullMessage);
            }
        }
    }

    public List<Announcement> getAnnouncements() {
        return announcements.values().stream()
                .sorted(Comparator.comparing(Announcement::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUserId(userId);
    }

    public Long getUnreadCount(Integer userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    public UserNotificationPreferences getUserPreferences(Integer userId) {
        return userPreferences.computeIfAbsent(userId, id -> new UserNotificationPreferences());
    }

    public void updateUserPreferences(Integer userId, UserNotificationPreferences preferences) {
        userPreferences.put(userId, preferences);
    }

    public void sendDueDateReminder(Integer userId, String bookTitle, int daysUntilDue) {
        String message = String.format("Reminder: Your book '%s' is due in %d day(s).", bookTitle, daysUntilDue);
        createNotification(userId, message, "DUE_DATE_REMINDER");
    }

    public void sendOverdueAlert(Integer userId, String bookTitle, int daysOverdue) {
        String message = String.format("Alert: Your book '%s' is now %d day(s) overdue.", bookTitle, daysOverdue);
        createNotification(userId, message, "OVERDUE_ALERT");
    }

    public static class UserNotificationPreferences {
        private boolean receiveEmailNotifications = true;
        public boolean isReceiveEmailNotifications() { return receiveEmailNotifications; }
        public void setReceiveEmailNotifications(boolean receiveEmailNotifications) { this.receiveEmailNotifications = receiveEmailNotifications; }
    }

    public static record Announcement(Integer id, String title, String message, String createdBy, LocalDateTime createdAt) {
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /**
     * Marks a single notification as read.
     * Includes a security check to ensure the user owns the notification.
     * @param notificationId The ID of the notification.
     * @param username The username of the currently logged-in user.
     */
    public void markAsRead(int notificationId, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new SecurityException("User not found."));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        if (notification.getUserId() != user.getUserId()) {
            throw new SecurityException("You are not authorized to modify this notification.");
        }

        notificationRepository.markAsReadById(notificationId);
    }

}