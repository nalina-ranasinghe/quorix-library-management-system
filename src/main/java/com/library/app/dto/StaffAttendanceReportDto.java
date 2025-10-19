package com.library.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StaffAttendanceReportDto {
    private String staffName;
    private String staffRole; // e.g., 'STAFF', 'ADMIN'
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;
}