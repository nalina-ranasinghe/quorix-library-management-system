package com.library.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Borrowing {
    private int borrowingId;
    private int userId;
    private int bookId;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
}