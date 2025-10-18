package com.library.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminReportDto {

    private List<MostBorrowedBookDto> mostBorrowedBooks;

    private int totalBorrowingsLast30Days;
    private int newUsersLast30Days;
}