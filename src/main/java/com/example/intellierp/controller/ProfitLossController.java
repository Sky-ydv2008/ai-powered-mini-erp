package com.example.intellierp.controller;

import com.example.intellierp.service.ProfitLossService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/profit-loss")
@Tag(name = "Profit & Loss", description = "Financial Performance Center & Margin Analytics")
public class ProfitLossController {

    @Autowired
    private ProfitLossService profitLossService;

    @GetMapping
    @Operation(summary = "Calculate Profit & Loss statement", description = "Calculates Revenue, COGS, Gross Profit, Expenses, Net Profit, and Margins for selected time period")
    public ResponseEntity<Map<String, Object>> getProfitLoss(
            @RequestParam(defaultValue = "this_month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(profitLossService.calculateProfitLoss(period, startDate, endDate));
    }
}
