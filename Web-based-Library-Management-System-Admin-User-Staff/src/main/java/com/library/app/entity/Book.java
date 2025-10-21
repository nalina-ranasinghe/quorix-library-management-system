package com.library.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Book {

    private Long id;

    private int bookId;
    private String title;
    private String author;


    @jakarta.validation.constraints.NotBlank(message = "ISBN is required.")
    @jakarta.validation.constraints.Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 digits.")
    @jakarta.validation.constraints.Pattern(regexp = "^[0-9]*$", message = "ISBN must contain only numbers.")
    private String isbn;
    private String category;
    private String location;

    private String status = "Available";

    private int quantity = 1;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}