package com.library.app.service;

import com.library.app.entity.Report;
import com.library.app.repository.ReportRepository;
import com.library.app.repository.SystemLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final SystemLogRepository systemLogRepository;
    private final ReportFactory reportFactory;
    private final EmailService emailService;
    private final PdfExportStrategy pdfExportStrategy;
    private final CsvExportStrategy csvExportStrategy;

    private Map<String, ExportStrategy> exportStrategies = new HashMap<>();

    @PostConstruct
    public void init() {
        exportStrategies.put("CSV", csvExportStrategy);
        exportStrategies.put("PDF", pdfExportStrategy);
    }

    public Map<String, Object> generateAdminDashboardReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("popularBooks", reportFactory.getReportData("POPULAR_BOOKS"));
        report.put("popularUsers", reportFactory.getReportData("POPULAR_USERS"));
        report.put("overdueBooks", reportFactory.getReportData("OVERDUE"));
        report.put("availableBooks", reportFactory.getReportData("AVAILABLE_BOOKS"));
        report.put("monthlyStats", reportFactory.getReportData("MONTHLY_STATS"));
        report.put("newUsers30Days", reportFactory.getNewUsersLast30Days());
        return report;
    }

    public byte[] generateAndExportReport(String type, String format, Integer userId) {
        List<Map<String, Object>> data = reportFactory.getReportData(type.toUpperCase());
        ExportStrategy strategy = exportStrategies.get(format.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown format: " + format);
        }
        byte[] bytes = strategy.export(data, type);
        Report report = new Report();
        report.setReportName(type.toUpperCase());
        report.setGeneratedByUserId(userId);
        report.setDeliveryStatus("GENERATED");
        report.setRecipientEmail("assistant.manager@example.com");
        reportRepository.save(report);
        systemLogRepository.logAction(userId, "GENERATE_REPORT", "Generated " + type);
        return bytes;
    }

    public void sendReport(int reportId) {
        reportRepository.findById(reportId).ifPresent(report -> {
            String type = report.getReportName();
            List<Map<String, Object>> data = reportFactory.getReportData(type);
            byte[] pdf = pdfExportStrategy.export(data, type);
            try {
                emailService.sendReportEmail(report.getRecipientEmail(), "Library Report: " + type, "Attached is the requested report.", pdf, type + ".pdf");
                reportRepository.updateDeliveryStatus(reportId, "SENT");
                systemLogRepository.logAction(report.getGeneratedByUserId(), "SEND_REPORT", "Sent " + type);
            } catch (Exception e) {
                reportRepository.updateDeliveryStatus(reportId, "FAILED");
                systemLogRepository.logAction(report.getGeneratedByUserId(), "SEND_REPORT_FAILED", "Failed to send " + type + ": " + e.getMessage());
            }
        });
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Map<String, Object>> getStaffAttendance() {
        return reportFactory.getStaffAttendance();
    }

    // Automated monthly overdue report
    @Scheduled(cron = "0 0 1 1 * ?") // Runs at 1 AM on the 1st of every month
    public void generateMonthlyOverdueReport() {
        Report report = new Report();
        report.setReportName("OVERDUE");
        report.setGeneratedByUserId(null); // System-generated
        report.setDeliveryStatus("GENERATED");
        report.setRecipientEmail("assistant.manager@example.com");
        report = reportRepository.save(report);
        systemLogRepository.logAction(null, "AUTO_GENERATE_REPORT", "Generated monthly OVERDUE");
        sendReport(report.getReportId());
    }
}