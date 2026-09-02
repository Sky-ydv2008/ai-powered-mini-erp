package com.example.intellierp.controller;

import com.example.intellierp.entity.Product;
import com.example.intellierp.entity.StockTransaction;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.StockTransactionType;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.InventoryService;
import com.example.intellierp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Real-time Stock Matrix, Adjustments & Audit Ledger")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get inventory overview metrics", description = "Total stock units, stock value, healthy, low-stock, and out-of-stock counts")
    public ResponseEntity<Map<String, Object>> getInventoryOverview() {
        return ResponseEntity.ok(inventoryService.getInventoryOverview());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get all low-stock products")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @PostMapping("/adjust")
    @Operation(summary = "Perform manual stock adjustment", description = "Adjust stock with reason, updating inventory balance and writing to stock ledger")
    public ResponseEntity<StockTransaction> adjustStock(
            @RequestParam Long productId,
            @RequestParam Integer quantityAdjustment,
            @RequestParam(required = false, defaultValue = "ADJUSTMENT") StockTransactionType type,
            @RequestParam String reason,
            Authentication auth) {
        return ResponseEntity.ok(inventoryService.adjustStock(productId, quantityAdjustment, type, reason, getCurrentUser(auth)));
    }

    @GetMapping("/ledger")
    @Operation(summary = "Get paginated Stock Movement Ledger", description = "Audit trail of all purchases, sales, returns, adjustments, and damages")
    public ResponseEntity<Page<StockTransaction>> getStockLedger(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) StockTransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(inventoryService.getStockLedger(productId, type, start, end, PageRequest.of(page, size, Sort.by("timestamp").descending())));
    }

    @GetMapping("/history/{productId}")
    @Operation(summary = "Get stock transaction history for a specific product")
    public ResponseEntity<List<StockTransaction>> getProductStockHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStockHistory(productId));
    }
}
