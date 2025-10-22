package com.library.app.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OverdueBookReportDto {
    private String bookTitle;
    private String borrowerName;
    private String borrowerEmail;
    private LocalDate dueDate;
    private int daysOverdue;
}