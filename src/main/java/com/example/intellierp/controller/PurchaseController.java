package com.example.intellierp.controller;

import com.example.intellierp.dto.PurchaseCreateDto;
import com.example.intellierp.entity.Purchase;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.PurchaseStatus;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.PurchaseService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@Tag(name = "Purchases", description = "Purchase Order Lifecycle, Procurement & Automatic Stock Receipt")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping
    @Operation(summary = "Get all purchase orders")
    public ResponseEntity<List<Purchase>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated purchase orders", description = "Filter by supplier, status, and date range")
    public ResponseEntity<Page<Purchase>> getPurchasesPaged(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(purchaseService.getPurchasesPaged(supplierId, status, startDate, endDate,
                PageRequest.of(page, size, Sort.by("orderDate").descending())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase order by ID")
    public ResponseEntity<Purchase> getPurchaseById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getPurchaseById(id));
    }

    @PostMapping
    @Operation(summary = "Create new purchase order", description = "Create purchase order with line items and vendor information")
    public ResponseEntity<Purchase> createPurchase(@Valid @RequestBody PurchaseCreateDto dto, Authentication auth) {
        return ResponseEntity.ok(purchaseService.createPurchase(dto, getCurrentUser(auth)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update purchase order status", description = "Changing status to RECEIVED automatically increments inventory stock and creates ledger entries")
    public ResponseEntity<Purchase> updatePurchaseStatus(
            @PathVariable Long id,
            @RequestParam PurchaseStatus status,
            Authentication auth) {
        return ResponseEntity.ok(purchaseService.updateStatus(id, status, getCurrentUser(auth)));
    }
}
