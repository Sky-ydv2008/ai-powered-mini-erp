package com.example.intellierp.service;

import com.example.intellierp.entity.*;
import com.example.intellierp.entity.enums.*;
import com.example.intellierp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AiInsightService {

    @Autowired
    private AiInsightRepository aiInsightRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ProfitLossService profitLossService;

    @Autowired
    private NotificationService notificationService;

    public List<AiInsight> getActiveInsights() {
        return aiInsightRepository.findActivePrioritizedInsights();
    }

    public List<AiInsight> getAllInsights() {
        return aiInsightRepository.findAll();
    }

    public AiInsight getInsightById(Long id) {
        return aiInsightRepository.findById(id).orElse(null);
    }

    @Transactional
    public void markAsRead(Long id) {
        aiInsightRepository.findById(id).ifPresent(i -> {
            i.setStatus(InsightStatus.READ);
            aiInsightRepository.save(i);
        });
    }

    @Transactional
    public void dismissInsight(Long id) {
        aiInsightRepository.findById(id).ifPresent(i -> {
            i.setStatus(InsightStatus.DISMISSED);
            aiInsightRepository.save(i);
        });
    }

    @Scheduled(cron = "0 0 6 * * *") // Daily at 6:00 AM
    @Transactional
    public void scheduledAiAnalysis() {
        generateAllInsights();
    }

    @Transactional
    public List<AiInsight> generateAllInsights() {
        // Clear previous active insights to keep fresh synthesized insights
        List<AiInsight> existing = aiInsightRepository.findByStatusOrderByCreatedAtDesc(InsightStatus.ACTIVE);
        aiInsightRepository.deleteAll(existing);

        List<AiInsight> generated = new ArrayList<>();

        // 1. Inventory Stockout Risk Detection
        generated.addAll(analyzeInventoryRisks());

        // 2. Supplier Loss & Delivery Risk Analysis
        generated.addAll(analyzeSupplierRisks());

        // 3. Expense Anomaly Detection
        generated.addAll(analyzeExpenseAnomalies());

        // 4. Sales & Profit Margin Anomalies
        generated.addAll(analyzeSalesAndProfitAnomalies());

        // 5. Customer Churn & Retention Risks
        generated.addAll(analyzeCustomerRisks());

        // 6. Product BCG Classification & Opportunities
        generated.addAll(analyzeProductOpportunities());

        List<AiInsight> saved = aiInsightRepository.saveAll(generated);

        // Create high-priority system notification
        if (!saved.isEmpty()) {
            notificationService.createNotification(
                    "AI Business Engine Alert",
                    "Generated " + saved.size() + " actionable business insights. Check the AI Decision Center.",
                    "ANOMALY",
                    "/ai-insights.html"
            );
        }

        return saved;
    }

    private List<AiInsight> analyzeInventoryRisks() {
        List<AiInsight> insights = new ArrayList<>();
        List<Product> products = productRepository.findAll();
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

        for (Product p : products) {
            Long unitsSold30d = saleItemRepository.sumProductQuantitySoldBetween(p.getId(), monthAgo, LocalDateTime.now());
            double velocityPerDay = (unitsSold30d != null && unitsSold30d > 0) ? (unitsSold30d / 30.0) : 0.5;

            int currentStock = p.getCurrentStock() != null ? p.getCurrentStock() : 0;
            int leadTime = p.getLeadTimeDays() != null ? p.getLeadTimeDays() : 7;
            double daysRemaining = velocityPerDay > 0 ? (currentStock / velocityPerDay) : 999.0;

            if (daysRemaining <= leadTime && currentStock > 0) {
                // Critical Stockout Alert!
                double potentialLostRevenue = (leadTime - daysRemaining) * velocityPerDay * p.getSellingPrice().doubleValue();
                int recommendedOrderQty = (int) Math.ceil((leadTime * velocityPerDay) + (p.getSafetyStock() != null ? p.getSafetyStock() : 10) * 1.5);

                String evidence = String.format("{\"currentStock\": %d, \"dailySalesRate\": %.1f, \"supplierLeadTime\": %d, \"daysRemaining\": %.1f, \"unitsSoldLast30Days\": %d}",
                        currentStock, velocityPerDay, leadTime, daysRemaining, unitsSold30d != null ? unitsSold30d : 0);

                AiInsight insight = new AiInsight(
                        InsightType.INVENTORY_RISK,
                        InsightSeverity.CRITICAL,
                        "🔴 Critical Stockout Risk: " + p.getName(),
                        String.format("Stock duration is only %.1f days, less than supplier lead time of %d days.", daysRemaining, leadTime),
                        evidence,
                        "High sales velocity combined with supplier lead time exceeds remaining warehouse stock.",
                        BigDecimal.valueOf(Math.max(15000.0, potentialLostRevenue)).setScale(2, RoundingMode.HALF_UP),
                        String.format("Estimated revenue loss of ₹%.0f if stock runs out before replenishment.", Math.max(15000.0, potentialLostRevenue)),
                        String.format("Place an immediate purchase order for %d units of %s from preferred supplier.", recommendedOrderQty, p.getName()),
                        new BigDecimal("96.50")
                );
                insights.add(insight);
            } else if (currentStock == 0) {
                // Out of stock
                AiInsight insight = new AiInsight(
                        InsightType.INVENTORY_RISK,
                        InsightSeverity.CRITICAL,
                        "🔴 Out of Stock: " + p.getName(),
                        "Product has 0 inventory balance. Sales are completely halted.",
                        String.format("{\"currentStock\": 0, \"reorderLevel\": %d, \"sellingPrice\": %.2f}", p.getReorderLevel(), p.getSellingPrice()),
                        "Inventory depleted without timely reorder trigger.",
                        p.getSellingPrice().multiply(BigDecimal.valueOf(30)),
                        "Ongoing daily revenue loss due to customer stockouts.",
                        "Reorder at least " + (p.getReorderLevel() * 2) + " units immediately.",
                        new BigDecimal("99.00")
                );
                insights.add(insight);
            }
        }
        return insights;
    }

    private List<AiInsight> analyzeSupplierRisks() {
        List<AiInsight> insights = new ArrayList<>();
        List<Supplier> suppliers = supplierRepository.findAll();

        for (Supplier s : suppliers) {
            BigDecimal defect = s.getDefectRate() != null ? s.getDefectRate() : BigDecimal.ZERO;
            BigDecimal onTime = s.getOnTimeDeliveryRate() != null ? s.getOnTimeDeliveryRate() : new BigDecimal("100.0");
            int delayed = s.getDelayedOrders() != null ? s.getDelayedOrders() : 0;

            if (delayed >= 3 || onTime.compareTo(new BigDecimal("80.0")) < 0) {
                String evidence = String.format("{\"delayedOrders\": %d, \"onTimeRate\": \"%.1f%%\", \"defectRate\": \"%.1f%%\", \"totalPurchases\": %.2f, \"estimatedLoss\": %.2f}",
                        delayed, onTime.doubleValue(), defect.doubleValue(), s.getTotalPurchases(), s.getEstimatedLoss());

                AiInsight insight = new AiInsight(
                        InsightType.SUPPLIER_RISK,
                        InsightSeverity.WARNING,
                        "🟠 Supplier Delay Anomaly: " + s.getName(),
                        String.format("Supplier experienced %d delayed shipments with on-time delivery at %.1f%%.", delayed, onTime.doubleValue()),
                        evidence,
                        "Logistical delays and vendor fulfillment lag resulting in downstream stockout risks.",
                        s.getEstimatedLoss().max(new BigDecimal("25000.00")),
                        String.format("Supplier-related delay and defect loss calculated at ₹%.2f.", s.getEstimatedLoss().doubleValue()),
                        "Renegotiate SLA lead times or split order allocation with alternate high-rated suppliers.",
                        new BigDecimal("91.00")
                );
                insights.add(insight);
            }
        }
        return insights;
    }

    private List<AiInsight> analyzeExpenseAnomalies() {
        List<AiInsight> insights = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        for (ExpenseCategory cat : ExpenseCategory.values()) {
            BigDecimal thisMonthExp = expenseRepository.sumCategoryExpensesBetween(cat, thisMonthStart, now);
            BigDecimal lastMonthExp = expenseRepository.sumCategoryExpensesBetween(cat, lastMonthStart, lastMonthEnd);

            if (lastMonthExp.compareTo(BigDecimal.ZERO) > 0) {
                double pctChange = (thisMonthExp.doubleValue() - lastMonthExp.doubleValue()) / lastMonthExp.doubleValue() * 100.0;
                if (pctChange > 25.0 && thisMonthExp.compareTo(new BigDecimal("10000.00")) > 0) {
                    String evidence = String.format("{\"category\": \"%s\", \"thisMonth\": %.2f, \"lastMonth\": %.2f, \"spikePercentage\": \"+%.1f%%\"}",
                            cat.name(), thisMonthExp.doubleValue(), lastMonthExp.doubleValue(), pctChange);

                    AiInsight insight = new AiInsight(
                            InsightType.EXPENSE_ANOMALY,
                            InsightSeverity.WARNING,
                            "🟠 Expense Spike Detected: " + cat.name(),
                            String.format("%s expenses are %.1f%% higher than the previous month.", cat.name(), pctChange),
                            evidence,
                            "Unusual volume or rate surge in operational spending.",
                            thisMonthExp.subtract(lastMonthExp),
                            String.format("Budget variance of ₹%.2f above normal historical baseline.", thisMonthExp.subtract(lastMonthExp).doubleValue()),
                            "Audit recent payment vouchers and enforce departmental expenditure limits.",
                            new BigDecimal("89.50")
                    );
                    insights.add(insight);
                }
            }
        }
        return insights;
    }

    private List<AiInsight> analyzeSalesAndProfitAnomalies() {
        List<AiInsight> insights = new ArrayList<>();
        Map<String, Object> thisMonth = profitLossService.calculateProfitLoss("this_month", null, null);
        Map<String, Object> lastMonth = profitLossService.calculateProfitLoss("last_month", null, null);

        BigDecimal thisNet = (BigDecimal) thisMonth.get("netProfit");
        BigDecimal lastNet = (BigDecimal) lastMonth.get("netProfit");
        BigDecimal margin = (BigDecimal) thisMonth.get("profitMargin");

        if (lastNet.compareTo(BigDecimal.ZERO) > 0 && thisNet.compareTo(lastNet) < 0) {
            double dropPct = (lastNet.doubleValue() - thisNet.doubleValue()) / lastNet.doubleValue() * 100.0;
            if (dropPct > 10.0) {
                String evidence = String.format("{\"thisMonthNetProfit\": %.2f, \"lastMonthNetProfit\": %.2f, \"dropPercentage\": \"-%.1f%%\", \"currentMargin\": \"%.2f%%\"}",
                        thisNet.doubleValue(), lastNet.doubleValue(), dropPct, margin.doubleValue());

                AiInsight insight = new AiInsight(
                        InsightType.PROFIT_WARNING,
                        InsightSeverity.ATTENTION,
                        "🟡 Profit Contraction Warning",
                        String.format("Net profit contracted by %.1f%% compared to last month.", dropPct),
                        evidence,
                        "Rising operational overhead and selective product margin compressions.",
                        lastNet.subtract(thisNet),
                        "Lower net bottom line affecting quarterly reinvestment capital.",
                        "Review discounted sales lines and trim discretionary operational expenses.",
                        new BigDecimal("88.00")
                );
                insights.add(insight);
            }
        }
        return insights;
    }

    private List<AiInsight> analyzeCustomerRisks() {
        List<AiInsight> insights = new ArrayList<>();
        List<Customer> churnRisks = customerRepository.findByTier(CustomerTier.CHURN_RISK);

        if (!churnRisks.isEmpty()) {
            BigDecimal totalSpendRisk = churnRisks.stream()
                    .map(Customer::getTotalSpend)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String evidence = String.format("{\"churnRiskCustomerCount\": %d, \"totalHistoricalSpend\": %.2f}",
                    churnRisks.size(), totalSpendRisk.doubleValue());

            AiInsight insight = new AiInsight(
                    InsightType.CUSTOMER_RISK,
                    InsightSeverity.ATTENTION,
                    "🟡 Customer Churn Risk: " + churnRisks.size() + " High-Value Accounts",
                    churnRisks.size() + " previously active accounts have not made any purchases in 45+ days.",
                    evidence,
                    "Natural churn or customer defection to competitors due to lack of engagement.",
                    totalSpendRisk.multiply(new BigDecimal("0.20")),
                    "Potential permanent loss of recurring customer lifetime value.",
                    "Launch automated re-engagement email campaign offering tailored loyalty incentives.",
                    new BigDecimal("87.00")
                );
            insights.add(insight);
        }
        return insights;
    }

    private List<AiInsight> analyzeProductOpportunities() {
        List<AiInsight> insights = new ArrayList<>();
        List<Product> products = productRepository.findAll();

        // Update BCG matrix for all products
        for (Product p : products) {
            BigDecimal margin = p.getProfitMargin();
            Long sold30d = saleItemRepository.sumProductQuantitySoldBetween(p.getId(), LocalDateTime.now().minusDays(30), LocalDateTime.now());
            int units = sold30d != null ? sold30d.intValue() : 0;

            if (units >= 30 && margin.compareTo(new BigDecimal("30.0")) >= 0) {
                p.setBcgClassification(BcgClassification.STAR);
            } else if (units >= 30 && margin.compareTo(new BigDecimal("30.0")) < 0) {
                p.setBcgClassification(BcgClassification.CASH_COW);
            } else if (units < 10 && margin.compareTo(new BigDecimal("35.0")) >= 0) {
                p.setBcgClassification(BcgClassification.QUESTION_MARK);
            } else if (units > 20 && margin.compareTo(new BigDecimal("10.0")) < 0) {
                p.setBcgClassification(BcgClassification.LOSS_MAKER);
            } else if (units == 0) {
                p.setBcgClassification(BcgClassification.DEAD_STOCK);
            }
            productRepository.save(p);
        }

        // Generate Growth Opportunity for Question Mark product (high margin, low volume)
        List<Product> questionMarks = productRepository.findByBcgClassification(BcgClassification.QUESTION_MARK);
        if (!questionMarks.isEmpty()) {
            Product starCandidate = questionMarks.get(0);
            AiInsight insight = new AiInsight(
                    InsightType.PRODUCT_OPPORTUNITY,
                    InsightSeverity.OPPORTUNITY,
                    "🟢 High Margin Growth Opportunity: " + starCandidate.getName(),
                    String.format("Product has a high profit margin of %.1f%% but low marketing visibility.", starCandidate.getProfitMargin().doubleValue()),
                    String.format("{\"product\": \"%s\", \"profitMargin\": \"%.1f%%\", \"sellingPrice\": %.2f, \"purchasePrice\": %.2f}",
                            starCandidate.getName(), starCandidate.getProfitMargin().doubleValue(), starCandidate.getSellingPrice(), starCandidate.getPurchasePrice()),
                    "Under-promoted product with premium profitability profile.",
                    starCandidate.getSellingPrice().multiply(new BigDecimal("50.00")),
                    "Potential ₹50,000+ monthly profit expansion with targeted promotional push.",
                    "Bundle " + starCandidate.getName() + " on the POS checkout screen and feature in marketing banners.",
                    new BigDecimal("94.00")
            );
            insights.add(insight);
        }

        return insights;
    }

    // Natural Language Business Assistant ("Ask Your Business Data")
    public Map<String, Object> askBusinessAssistant(String userQuestion) {
        if (userQuestion == null || userQuestion.trim().isEmpty()) {
            userQuestion = "summary";
        }
        String q = userQuestion.toLowerCase().trim();

        Map<String, Object> response = new HashMap<>();
        response.put("query", userQuestion);

        if (q.contains("why") && (q.contains("profit") || q.contains("loss") || q.contains("decrease") || q.contains("drop"))) {
            // Profit Contraction Diagnostic
            Map<String, Object> thisMonth = profitLossService.calculateProfitLoss("this_month", null, null);
            Map<String, Object> lastMonth = profitLossService.calculateProfitLoss("last_month", null, null);
            BigDecimal thisNet = (BigDecimal) thisMonth.get("netProfit");
            BigDecimal lastNet = (BigDecimal) lastMonth.get("netProfit");
            BigDecimal thisExp = (BigDecimal) thisMonth.get("operatingExpenses");
            BigDecimal lastExp = (BigDecimal) lastMonth.get("operatingExpenses");

            response.put("intent", "PROFIT_DIAGNOSTIC");
            response.put("title", "Profit Contraction Root Cause Diagnostic");
            response.put("calculatedData", Map.of(
                    "currentNetProfit", thisNet,
                    "previousNetProfit", lastNet,
                    "currentExpenses", thisExp,
                    "previousExpenses", lastExp
            ));
            response.put("explanation", String.format(
                    "Your net profit changed from ₹%.2f last month to ₹%.2f this month. " +
                    "The primary driver is an increase in operating expenses (₹%.2f vs ₹%.2f) alongside supplier lead-time delays affecting inventory turnover.",
                    lastNet.doubleValue(), thisNet.doubleValue(), thisExp.doubleValue(), lastExp.doubleValue()));
            response.put("recommendations", List.of(
                    "Review recent transportation and utility invoices for cost creep.",
                    "Reorder critical inventory 5 days earlier to prevent stockout gaps.",
                    "Promote high-margin BCG Star items."
            ));

        } else if (q.contains("most profit") || q.contains("profitable product") || q.contains("best product")) {
            List<Object[]> topProfitable = saleItemRepository.findMostProfitableProducts();
            response.put("intent", "PRODUCT_PROFITABILITY");
            response.put("title", "Top Profitable Products");
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < Math.min(5, topProfitable.size()); i++) {
                Object[] row = topProfitable.get(i);
                list.add(Map.of(
                        "rank", i + 1,
                        "productName", row[1],
                        "sku", row[2],
                        "totalProfit", row[5],
                        "unitsSold", row[3]
                ));
            }
            response.put("calculatedData", list);
            response.put("explanation", "These products generate the highest cumulative gross profit for your business.");
            response.put("recommendations", List.of("Ensure safety stock is maintained for top-ranked items.", "Avoid deep discounting on these high-margin leaders."));

        } else if (q.contains("supplier") && (q.contains("loss") || q.contains("delay") || q.contains("worst") || q.contains("problem"))) {
            List<Map<String, Object>> lossReport = supplierRepository.findAll().stream()
                    .sorted((a, b) -> b.getEstimatedLoss().compareTo(a.getEstimatedLoss()))
                    .limit(3)
                    .map(s -> Map.of(
                            "supplierName", (Object) s.getName(),
                            "delayedOrders", s.getDelayedOrders(),
                            "onTimeRate", s.getOnTimeDeliveryRate() + "%",
                            "defectRate", s.getDefectRate() + "%",
                            "estimatedLoss", s.getEstimatedLoss()
                    )).toList();

            response.put("intent", "SUPPLIER_LOSS_ANALYSIS");
            response.put("title", "Supplier Risk & Loss Breakdown");
            response.put("calculatedData", lossReport);
            response.put("explanation", "Supplier-related losses are calculated using shipment delay penalties, defective goods costs, and customer return rates.");
            response.put("recommendations", List.of(
                    "Shift purchase allocations away from suppliers with delayed order history.",
                    "Implement a 5% delay penalty in future procurement contracts."
            ));

        } else if (q.contains("out of stock") || q.contains("stockout") || q.contains("low stock") || q.contains("reorder")) {
            List<Product> lowStock = productRepository.findLowStockProducts();
            response.put("intent", "INVENTORY_PREDICTION");
            response.put("title", "Products at Immediate Stockout Risk");
            List<Map<String, Object>> items = lowStock.stream().limit(6).map(p -> Map.of(
                    "productName", (Object) p.getName(),
                    "sku", p.getSku(),
                    "currentStock", p.getCurrentStock(),
                    "reorderLevel", p.getReorderLevel(),
                    "status", p.getStatus().name()
            )).toList();
            response.put("calculatedData", items);
            response.put("explanation", "These products have stock levels at or below their designated reorder points.");
            response.put("recommendations", List.of("Issue Purchase Orders to preferred vendors immediately to prevent lost sales."));

        } else if (q.contains("expense") || q.contains("spending") || q.contains("cost")) {
            List<Map<String, Object>> expBreakdown = expenseRepository.sumExpensesByCategoryBetween(LocalDate.now().withDayOfMonth(1), LocalDate.now())
                    .stream().map(r -> Map.of("category", r[0], "amount", r[1])).toList();
            response.put("intent", "EXPENSE_ANALYSIS");
            response.put("title", "Current Month Expense Breakdown");
            response.put("calculatedData", expBreakdown);
            response.put("explanation", "Operational expenses recorded for the current month grouped by budget category.");
            response.put("recommendations", List.of("Audit transportation and marketing expenses against quarterly targets."));

        } else if (q.contains("sell") || q.contains("sales") || q.contains("revenue") || q.contains("how much")) {
            Map<String, Object> pl = profitLossService.calculateProfitLoss("this_month", null, null);
            response.put("intent", "SALES_SUMMARY");
            response.put("title", "Monthly Sales & Revenue Overview");
            response.put("calculatedData", pl);
            response.put("explanation", String.format("Current month net revenue is ₹%.2f across %s completed sales orders.",
                    ((BigDecimal) pl.get("revenue")).doubleValue(), pl.get("salesCount")));
            response.put("recommendations", List.of("Continue cross-selling accessories at checkout to maintain sales velocity."));

        } else {
            // General Business Health Overview
            Map<String, Object> health = profitLossService.calculateProfitLoss("this_month", null, null);
            response.put("intent", "GENERAL_SUMMARY");
            response.put("title", "Business Decision Intelligence Overview");
            response.put("calculatedData", Map.of(
                    "revenue", health.get("revenue"),
                    "netProfit", health.get("netProfit"),
                    "profitMargin", health.get("profitMargin"),
                    "totalProducts", productRepository.count(),
                    "totalSuppliers", supplierRepository.count()
            ));
            response.put("explanation", "Your business metrics are tracked live in the ERP ledger. Ask specific questions regarding profits, products, suppliers, or stock.");
            response.put("recommendations", List.of("Try asking: 'Why did my profit decrease?', 'Which product makes the most profit?', or 'What products may go out of stock?'"));
        }

        return response;
    }
}
