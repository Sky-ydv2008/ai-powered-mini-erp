package com.example.intellierp.controller;

import com.example.intellierp.entity.Supplier;
import com.example.intellierp.entity.User;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier Directory, Performance Scoring, and Supplier Loss Detector")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth != null && auth.getName() != null) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Returns suppliers with live performance score, star rating, and loss analysis")
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping("/loss-detector")
    @Operation(summary = "Get Supplier Loss Detector Report", description = "Quantifies monetary losses from defective goods, late deliveries, and return rates per supplier")
    public ResponseEntity<List<Map<String, Object>>> getSupplierLossDetectorReport() {
        return ResponseEntity.ok(supplierService.getSupplierLossDetectorReport());
    }

    @GetMapping("/recommendation/{productId}")
    @Operation(summary = "Get AI Supplier Recommendation for a product", description = "Recommends best supplier based on price, reliability, defect rate, and on-time performance")
    public ResponseEntity<Map<String, Object>> getSupplierRecommendation(@PathVariable Long productId) {
        return ResponseEntity.ok(supplierService.getSupplierRecommendation(productId));
    }

    @PostMapping
    @Operation(summary = "Create supplier")
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier, Authentication auth) {
        return ResponseEntity.ok(supplierService.createSupplier(supplier, getCurrentUser(auth)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update supplier details")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier supplier, Authentication auth) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplier, getCurrentUser(auth)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id, Authentication auth) {
        supplierService.deleteSupplier(id, getCurrentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
