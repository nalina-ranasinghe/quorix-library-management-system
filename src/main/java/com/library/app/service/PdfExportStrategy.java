package com.library.app.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class PdfExportStrategy implements ExportStrategy {
    @Override
    public byte[] export(List<Map<String, Object>> data, String reportName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph(reportName));
            if (!data.isEmpty()) {
                String[] headers = data.get(0).keySet().toArray(new String[0]);
                PdfPTable table = new PdfPTable(headers.length);
                for (String header : headers) {
                    table.addCell(header);
                }
                for (Map<String, Object> row : data) {
                    for (String header : headers) {
                        table.addCell(row.get(header).toString());
                    }
                }
                document.add(table);
            }
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error exporting PDF", e);
        }
    }
}