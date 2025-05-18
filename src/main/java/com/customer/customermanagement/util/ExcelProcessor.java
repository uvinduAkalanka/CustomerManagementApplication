package com.customer.customermanagement.util;

import com.customer.customermanagement.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ExcelProcessor {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Exports customers to Excel format
     */
    public void exportCustomersToExcel(List<Customer> customers, OutputStream outputStream) throws IOException {
        // Use SXSSF for memory efficiency when dealing with large datasets
        try (Workbook workbook = new SXSSFWorkbook(100)) { // Window size of 100 rows
            Sheet sheet = workbook.createSheet("Customers");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Date of Birth");
            headerRow.createCell(2).setCellValue("NIC Number");
            headerRow.createCell(3).setCellValue("Mobile Numbers");

            // Fill data rows
            int rowNum = 1;
            for (Customer customer : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(customer.getName());
                row.createCell(1).setCellValue(
                        customer.getDateOfBirth() != null ?
                                customer.getDateOfBirth().format(DATE_FORMATTER) : "");
                row.createCell(2).setCellValue(customer.getNicNumber());
                row.createCell(3).setCellValue(
                        String.join(", ", customer.getMobileNumbers()));
            }

            // Auto-size columns for better display
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            // Dispose of temporary files
            ((SXSSFWorkbook) workbook).dispose();
        }
    }
}
