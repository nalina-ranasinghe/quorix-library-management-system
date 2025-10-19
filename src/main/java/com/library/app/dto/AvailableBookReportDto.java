package com.library.app.dto;

import lombok.Data;

@Data
public class AvailableBookReportDto {
    private String title;
    private String author;
    private String isbn;
    private int quantity;
    private String location;
}