package com.example.intellierp.service;

import com.example.intellierp.repository.ExpenseRepository;
import com.example.intellierp.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ProfitLossService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public Map<String, Object> calculateProfitLoss(String period, LocalDate customStart, LocalDate customEnd) {
        LocalDate startDate;
        LocalDate endDate;
        LocalDate today = LocalDate.now();

        if (period == null) period = "this_month";

        switch (period.toLowerCase()) {
            case "today":
                startDate = today;
                endDate = today;
                break;
            case "yesterday":
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;
            case "this_week":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            case "last_week":
                startDate = today.minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = today.minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            case "this_month":
                startDate = today.withDayOfMonth(1);
                endDate = today.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "last_month":
                LocalDate lastMonth = today.minusMonths(1);
                startDate = lastMonth.withDayOfMonth(1);
                endDate = lastMonth.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "this_year":
                startDate = today.withDayOfYear(1);
                endDate = today.with(TemporalAdjusters.lastDayOfYear());
                break;
            case "custom":
            default:
                startDate = customStart != null ? customStart : today.withDayOfMonth(1);
                endDate = customEnd != null ? customEnd : today;
                break;
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        BigDecimal revenue = saleRepository.sumRevenueBetween(startDateTime, endDateTime);
        BigDecimal cogs = saleRepository.sumCostBasisBetween(startDateTime, endDateTime);
        BigDecimal grossProfit = revenue.subtract(cogs);
        BigDecimal discounts = saleRepository.sumDiscountsBetween(startDateTime, endDateTime);
        BigDecimal refunds = saleRepository.sumRefundsBetween(startDateTime, endDateTime);
        long salesCount = saleRepository.countCompletedSalesBetween(startDateTime, endDateTime);

        BigDecimal expenses = expenseRepository.sumExpensesBetween(startDate, endDate);
        BigDecimal netProfit = grossProfit.subtract(expenses);

        BigDecimal profitMargin = BigDecimal.ZERO;
        if (revenue.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = netProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal grossMargin = BigDecimal.ZERO;
        if (revenue.compareTo(BigDecimal.ZERO) > 0) {
            grossMargin = grossProfit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("revenue", revenue);
        result.put("costOfGoodsSold", cogs);
        result.put("grossProfit", grossProfit);
        result.put("grossMargin", grossMargin);
        result.put("operatingExpenses", expenses);
        result.put("netProfit", netProfit);
        result.put("profitMargin", profitMargin);
        result.put("discounts", discounts);
        result.put("refunds", refunds);
        result.put("salesCount", salesCount);

        // Daily trend data for charting
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate cur = startDate;
        // Limit daily points to 60 for performance if wide range
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int step = daysDiff > 60 ? (int) Math.ceil(daysDiff / 30.0) : 1;

        while (!cur.isAfter(endDate)) {
            LocalDate nextCur = cur.plusDays(step - 1);
            if (nextCur.isAfter(endDate)) nextCur = endDate;

            LocalDateTime dStart = cur.atStartOfDay();
            LocalDateTime dEnd = nextCur.atTime(LocalTime.MAX);

            BigDecimal dRev = saleRepository.sumRevenueBetween(dStart, dEnd);
            BigDecimal dCogs = saleRepository.sumCostBasisBetween(dStart, dEnd);
            BigDecimal dExp = expenseRepository.sumExpensesBetween(cur, nextCur);
            BigDecimal dGross = dRev.subtract(dCogs);
            BigDecimal dNet = dGross.subtract(dExp);

            Map<String, Object> point = new HashMap<>();
            point.put("date", cur.toString());
            point.put("revenue", dRev);
            point.put("cogs", dCogs);
            point.put("expenses", dExp);
            point.put("grossProfit", dGross);
            point.put("netProfit", dNet);
            trend.add(point);

            cur = cur.plusDays(step);
        }

        result.put("trend", trend);
        return result;
    }
}
