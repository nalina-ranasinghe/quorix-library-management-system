package com.library.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserBorrowingDto {
    private int borrowingId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private String status;
}