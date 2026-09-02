package com.example.intellierp.controller;

import com.example.intellierp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Business Intelligence Reports & CSV Exports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping(value = "/sales/csv", produces = "text/csv")
    @Operation(summary = "Export sales report to CSV")
    public ResponseEntity<String> exportSalesCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime s = start != null ? start : LocalDateTime.now().minusMonths(1);
        LocalDateTime e = end != null ? end : LocalDateTime.now();
        String csv = reportService.generateSalesReportCsv(s, e);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sales_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/inventory/csv", produces = "text/csv")
    @Operation(summary = "Export inventory valuation & status report to CSV")
    public ResponseEntity<String> exportInventoryCsv() {
        String csv = reportService.generateInventoryReportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"inventory_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/suppliers/csv", produces = "text/csv")
    @Operation(summary = "Export supplier performance and loss report to CSV")
    public ResponseEntity<String> exportSuppliersCsv() {
        String csv = reportService.generateSupplierReportCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supplier_performance_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/profit-loss/csv", produces = "text/csv")
    @Operation(summary = "Export Profit & Loss statement to CSV")
    public ResponseEntity<String> exportProfitLossCsv(
            @RequestParam(defaultValue = "this_month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csv = reportService.generateProfitLossReportCsv(period, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"profit_loss_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
