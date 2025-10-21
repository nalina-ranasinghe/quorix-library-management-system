package com.library.app.service;

import com.library.app.dto.AdminReportDto;
import com.library.app.dto.MostBorrowedBookDto; // <-- IMPORT the new DTO
import com.library.app.dto.*;
import com.library.app.repository.BorrowingRepository;
import com.library.app.repository.ReportRepository;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ReportLogService reportLogService;
    private final EmailService emailService;

    public AdminReportDto generateAdminDashboardReport(Principal principal) {
        int borrowingsLastMonth = borrowingRepository.countTotalBorrowingsLast30Days();
        int newUsersLastMonth = userRepository.countNewUsersLast30Days();
        AdminReportDto reportDto = new AdminReportDto();
        reportDto.setTotalBorrowingsLast30Days(borrowingsLastMonth);
        reportDto.setNewUsersLast30Days(newUsersLastMonth);

        // --- FIX: Add null check ---
        if (principal != null) {
            reportLogService.logReportGeneration("Dashboard Summary", principal.getName());
        }
        return reportDto;
    }

    public Map<String, Object> generateUsagePatternReport(Principal principal) {
        List<PopularBookReportDto> popularBooks = reportRepository.findPopularBooks(10);
        List<TopUserReportDto> topUsers = reportRepository.findTopUsers(10);
        Map<String, Object> reportData = Map.of("popularBooks", popularBooks, "topUsers", topUsers);

        // --- FIX: Add null check ---
        if (principal != null) {
            reportLogService.logReportGeneration("Usage Patterns Report", principal.getName());
        }
        return reportData;
    }

    public Map<String, Object> generateOperationalReport(Principal principal) {
        List<OverdueBookReportDto> overdueBooks = reportRepository.findOverdueBooks();
        List<AvailableBookReportDto> availableBooks = reportRepository.findAvailableBooks();
        List<StaffAttendanceReportDto> todaysAttendance = reportRepository.findTodaysStaffAttendance();
        Map<String, Object> reportData = Map.of(
                "overdueBooks", overdueBooks,
                "availableBooks", availableBooks,
                "todaysAttendance", todaysAttendance
        );

        // --- FIX: Add null check ---
        if (principal != null) {
            reportLogService.logReportGeneration("Operational Report", principal.getName());
        }
        return reportData;
    }

    @Transactional(readOnly = true)
    public void sendOperationalReportByEmail(String recipientEmail) {
        // This now safely calls the method with a null principal
        Map<String, Object> reportData = generateOperationalReport(null);

        Map<String, Object> emailModel = Map.of(
                "reportData", reportData,
                "generationDate", LocalDateTime.now()
        );

        String subject = "Library Operational Report - " + LocalDateTime.now().toLocalDate();
        emailService.sendHtmlEmail(recipientEmail, subject, "operational-report", emailModel);
    }
}