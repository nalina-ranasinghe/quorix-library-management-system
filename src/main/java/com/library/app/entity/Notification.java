package com.library.app.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Notification {
    private Integer notificationId;

    @NotNull(message = "User is required")
    private Integer userId;

    @NotBlank(message = "Message is required")
    @Size(max = 255, message = "Message cannot exceed 255 characters")
    private String message;

    private String type;
    private LocalDateTime sentAt;
    private String status;

    public Notification() {
    }

    public Notification(Integer userId, String message, String type) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.sentAt = LocalDateTime.now();
        this.status = "UNREAD";
    }

    public Notification(Integer notificationId, Integer userId, String message, String type, LocalDateTime sentAt, String status) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.sentAt = sentAt;
        this.status = status;
    }
}