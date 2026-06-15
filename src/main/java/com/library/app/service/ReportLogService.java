package com.library.app.service;

import com.library.app.dto.PopularBookReportDto;
import com.library.app.dto.TopUserReportDto;
import com.library.app.dto.ReportLogDto;
import com.library.app.repository.ReportLogRepository;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportLogService {

    private final ReportLogRepository reportLogRepository;
    private final UserRepository userRepository;

    public void logReportGeneration(String reportName, String username) {
        // Find the user's ID from their username
        java.util.Optional<com.library.app.entity.User> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty()) {
            System.err.println("Warning: Attempted to log report for non-existent user: " + username);
            return;
        }
        int userId = userOpt.get().getUserId();

        // Save the log with a 'SUCCESS' status
        reportLogRepository.save(reportName, userId, "SUCCESS");
    }

    public List<ReportLogDto> getRecentLogs() {
        return reportLogRepository.findRecentLogs(10); // Get the last 10 logs
    }
}