package com.example.intellierp.controller;

import com.example.intellierp.dto.SaleCreateDto;
import com.example.intellierp.entity.Sale;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.SaleStatus;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "POS Terminal, Sales Checkout, Invoices & Order Management")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping
    @Operation(summary = "Get all sales")
    public ResponseEntity<List<Sale>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated sales", description = "Filter by customer, status, and date range")
    public ResponseEntity<Page<Sale>> getSalesPaged(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(saleService.getSalesPaged(customerId, status, start, end,
                PageRequest.of(page, size, Sort.by("saleDate").descending())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<Sale> getSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @Operation(summary = "Get sale by invoice number")
    public ResponseEntity<Sale> getSaleByInvoiceNumber(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(saleService.getSaleByInvoiceNumber(invoiceNumber));
    }

    @PostMapping
    @Operation(summary = "Create sale (POS Checkout)", description = "Deducts stock automatically, computes COGS and profit, logs stock ledger, and records customer lifetime spend")
    public ResponseEntity<Sale> createSale(@Valid @RequestBody SaleCreateDto dto, Authentication auth) {
        return ResponseEntity.ok(saleService.createSale(dto, getCurrentUser(auth)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel sale", description = "Restores product inventory and updates customer metrics")
    public ResponseEntity<Sale> cancelSale(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, Authentication auth) {
        String reason = body != null ? body.get("reason") : "Customer requested cancellation";
        return ResponseEntity.ok(saleService.cancelSale(id, reason, getCurrentUser(auth)));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund sale", description = "Restores product inventory and records return in stock ledger")
    public ResponseEntity<Sale> refundSale(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, Authentication auth) {
        String reason = body != null ? body.get("reason") : "Customer return";
        return ResponseEntity.ok(saleService.refundSale(id, reason, getCurrentUser(auth)));
    }
}
