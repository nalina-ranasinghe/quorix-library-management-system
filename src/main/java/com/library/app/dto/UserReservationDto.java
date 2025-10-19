package com.library.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserReservationDto {
    private int reservationId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime reservedAt;
}