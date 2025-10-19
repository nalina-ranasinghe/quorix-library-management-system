package com.library.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reservation {
    private int reservationId;
    private int userId;
    private int bookId;
    private LocalDateTime reservedAt;
    private String status;
}