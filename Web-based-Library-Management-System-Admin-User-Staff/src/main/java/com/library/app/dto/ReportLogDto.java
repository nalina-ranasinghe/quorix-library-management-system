package com.library.app.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportLogDto {
    private String reportName;
    private LocalDateTime generationTimestamp;
    private String generatedByUsername;
    private String deliveryStatus;
}