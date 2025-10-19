package com.library.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Book {
    private int bookId;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String location;
    private int quantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}