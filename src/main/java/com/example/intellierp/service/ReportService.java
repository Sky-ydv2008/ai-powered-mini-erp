package com.example.intellierp.service;

import com.example.intellierp.entity.*;
import com.example.intellierp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private ProfitLossService profitLossService;

    public String generateSalesReportCsv(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = saleRepository.findBySaleDateBetweenOrderBySaleDateDesc(start, end);
        StringWriter writer = new StringWriter();
        writer.append("Invoice Number,Date,Customer,Payment Method,Status,Subtotal,Discount,Tax,Total Amount,Profit\n");

        for (Sale s : sales) {
            writer.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f,%.2f\n",
                    s.getInvoiceNumber(),
                    s.getSaleDate(),
                    s.getCustomer() != null ? s.getCustomer().getName() : "Walk-in Customer",
                    s.getPaymentMethod(),
                    s.getStatus(),
                    s.getSubtotal() != null ? s.getSubtotal() : BigDecimal.ZERO,
                    s.getDiscount() != null ? s.getDiscount() : BigDecimal.ZERO,
                    s.getTax() != null ? s.getTax() : BigDecimal.ZERO,
                    s.getTotalAmount(),
                    s.getProfit()));
        }
        return writer.toString();
    }

    public String generateInventoryReportCsv() {
        List<Product> products = productRepository.findAll();
        StringWriter writer = new StringWriter();
        writer.append("Product Name,SKU,Category,Current Stock,Reorder Level,Safety Stock,Purchase Price,Selling Price,Stock Value,Profit Margin,Status,BCG Class\n");

        for (Product p : products) {
            writer.append(String.format("\"%s\",\"%s\",\"%s\",%d,%d,%d,%.2f,%.2f,%.2f,%.2f%%,\"%s\",\"%s\"\n",
                    p.getName(),
                    p.getSku(),
                    p.getCategory() != null ? p.getCategory().getName() : "Uncategorized",
                    p.getCurrentStock(),
                    p.getReorderLevel(),
                    p.getSafetyStock(),
                    p.getPurchasePrice(),
                    p.getSellingPrice(),
                    p.getStockValue(),
                    p.getProfitMargin(),
                    p.getStatus(),
                    p.getBcgClassification()));
        }
        return writer.toString();
    }

    public String generateSupplierReportCsv() {
        List<Supplier> suppliers = supplierRepository.findAllByOrderByPerformanceScoreDesc();
        StringWriter writer = new StringWriter();
        writer.append("Supplier Name,Contact Person,Email,Phone,Total Purchases,Delayed Orders,On-Time Delivery %,Defect Rate %,Performance Score,Rating,Estimated Loss\n");

        for (Supplier s : suppliers) {
            writer.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%d,%.2f%%,%.2f%%,%.2f,%.1f,%.2f\n",
                    s.getName(),
                    s.getContactPerson() != null ? s.getContactPerson() : "",
                    s.getEmail() != null ? s.getEmail() : "",
                    s.getPhone() != null ? s.getPhone() : "",
                    s.getTotalPurchases(),
                    s.getDelayedOrders(),
                    s.getOnTimeDeliveryRate(),
                    s.getDefectRate(),
                    s.getPerformanceScore(),
                    s.getRating(),
                    s.getEstimatedLoss()));
        }
        return writer.toString();
    }

    public String generateProfitLossReportCsv(String period, LocalDate start, LocalDate end) {
        Map<String, Object> pl = profitLossService.calculateProfitLoss(period, start, end);
        StringWriter writer = new StringWriter();
        writer.append("IntelliERP Profit & Loss Statement\n");
        writer.append("Period: " + pl.get("period") + " (" + pl.get("startDate") + " to " + pl.get("endDate") + ")\n\n");
        writer.append("Metric,Amount (INR)\n");
        writer.append(String.format("Gross Revenue,%.2f\n", pl.get("revenue")));
        writer.append(String.format("Cost of Goods Sold (COGS),%.2f\n", pl.get("costOfGoodsSold")));
        writer.append(String.format("Gross Profit,%.2f\n", pl.get("grossProfit")));
        writer.append(String.format("Gross Margin %%,%.2f%%\n", pl.get("grossMargin")));
        writer.append(String.format("Operating Expenses,%.2f\n", pl.get("operatingExpenses")));
        writer.append(String.format("Net Profit,%.2f\n", pl.get("netProfit")));
        writer.append(String.format("Net Profit Margin %%,%.2f%%\n", pl.get("profitMargin")));
        writer.append(String.format("Total Sales Orders,%s\n", pl.get("salesCount")));
        return writer.toString();
    }
}
