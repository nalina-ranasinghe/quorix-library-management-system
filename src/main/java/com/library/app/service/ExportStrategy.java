// New ExportStrategy.java (Strategy Pattern for export formats)
package com.library.app.service;

import java.util.List;
import java.util.Map;

public interface ExportStrategy {
    byte[] export(List<Map<String, Object>> data, String reportName);
}