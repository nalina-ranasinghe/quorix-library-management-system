// New Report.java (entity)
package com.library.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Report {
    private Integer reportId;
    private String reportName;
    private LocalDateTime generationTimestamp;
    private Integer generatedByUserId;
    private String deliveryStatus;
    private String recipientEmail;
}