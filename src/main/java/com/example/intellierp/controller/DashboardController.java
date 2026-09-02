package com.example.intellierp.controller;

import com.example.intellierp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Executive Dashboard KPIs, Summaries & Business Health")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Get complete dashboard KPI data", description = "Returns top KPI cards, sales horizons, inventory stats, top products, and health score")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }

    @GetMapping("/summary")
    @Operation(summary = "Get Today's Business Summary", description = "Detailed breakdown of today's sales, purchases, expenses, and net profit")
    public ResponseEntity<Map<String, Object>> getTodaySummary() {
        return ResponseEntity.ok(dashboardService.getTodayBusinessSummary());
    }

    @GetMapping("/health-score")
    @Operation(summary = "Get Business Health Score (0-100)", description = "Multi-pillar health evaluation across profitability, inventory, suppliers, and customer retention")
    public ResponseEntity<Map<String, Object>> getBusinessHealthScore() {
        return ResponseEntity.ok(dashboardService.calculateBusinessHealthScore());
    }
}
