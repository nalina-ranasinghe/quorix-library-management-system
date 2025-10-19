package com.library.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Waitlist {
    private int waitlistId;
    private int userId;
    private int bookId;
    private LocalDateTime waitlistedAt;
    private boolean notified;
}