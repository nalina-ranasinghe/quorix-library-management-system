// Create this file in: src/main/java/com/library/app/dto/TopUserReportDto.java
package com.library.app.dto;

import lombok.Data;

@Data
public class TopUserReportDto {
    private String username;
    private String fullName;
    private String email;
    private int borrowCount;
}