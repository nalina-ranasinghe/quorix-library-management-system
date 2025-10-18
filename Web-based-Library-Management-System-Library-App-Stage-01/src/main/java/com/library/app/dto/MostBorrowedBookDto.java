package com.library.app.dto;

import lombok.Data;

@Data
public class MostBorrowedBookDto {
    private String title;
    private String author;
    private int borrowCount;
}