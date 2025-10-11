package com.library.app.service;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

@Component
public class CsvExportStrategy implements ExportStrategy {
    @Override
    public byte[] export(List<Map<String, Object>> data, String reportName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos))) {
            if (!data.isEmpty()) {
                String[] headers = data.get(0).keySet().toArray(new String[0]);
                writer.writeNext(headers);
                for (Map<String, Object> row : data) {
                    String[] rowData = new String[headers.length];
                    for (int i = 0; i < headers.length; i++) {
                        rowData[i] = row.get(headers[i]).toString();
                    }
                    writer.writeNext(rowData);
                }
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error exporting CSV", e);
        }
    }
}