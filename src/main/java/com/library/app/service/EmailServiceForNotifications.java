package com.library.app.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class EmailServiceForNotifications {

    private final JavaMailSender mailSender;

    @Value("${library.email.from}")
    private String fromEmailAddress;

    public void sendNotificationEmail(String to, String userName, String notificationType, String message) {
        String subject = getEmailSubject(notificationType);
        String htmlContent = buildHtmlEmailContent(userName, message);
        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmailAddress);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send notification email to " + to + ": " + e.getMessage());
        }
    }

    private String getEmailSubject(String notificationType) {
        return switch (notificationType) {
            case "RESERVATION_CONFIRMATION" -> "Book Reservation Confirmation";
            case "WAITLIST_JOIN" -> "You've Been Added to the Waitlist";
            case "WAITLIST_AVAILABILITY" -> "A Book on Your Waitlist is Available!";
            case "DUE_DATE_REMINDER" -> "Book Due Date Reminder";
            case "OVERDUE_ALERT" -> "Action Required: Overdue Book";
            case "ANNOUNCEMENT" -> "New Library Announcement"; // <-- ADDED THIS
            default -> "New Notification from Quorix Library";
        };
    }

    private String buildHtmlEmailContent(String userName, String message) {

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                    .header { text-align: center; padding-bottom: 20px; border-bottom: 1px solid #ddd; }
                    .content { padding: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
                    .btn { display: inline-block; padding: 10px 20px; margin-top: 15px; background-color: #0d6efd; color: white; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Quorix Library Notification</h1>
                    </div>
                    <div class="content">
                        <h3>Hello, %s!</h3>
                        
                        <p>%s</p>
                        
                        <a href="http://localhost:8080/notifications" class="btn">View Your Notifications</a>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply.</p>
                        <p>&copy; 2025 Quorix Library. All Rights Reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, message);
    }
}