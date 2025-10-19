// Create this file in: src/main/java/com/library/app/dto/PopularBookReportDto.java
package com.library.app.dto;

import lombok.Data;

@Data
public class PopularBookReportDto {
    private String title;
    private String author;
    private String isbn;
    private int borrowCount;
}