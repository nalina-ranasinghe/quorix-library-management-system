package com.library.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserWaitlistDto {
    private int waitlistId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime waitlistedAt;
}