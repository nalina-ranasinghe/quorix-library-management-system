package com.library.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminReportDto {
    // UPDATED: Use our new, specific DTO
    private List<MostBorrowedBookDto> mostBorrowedBooks;

    private int totalBorrowingsLast30Days;
    private int newUsersLast30Days;
}