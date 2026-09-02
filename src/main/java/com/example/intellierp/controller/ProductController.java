package com.example.intellierp.controller;

import com.example.intellierp.entity.Product;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.BcgClassification;
import com.example.intellierp.entity.enums.ProductStatus;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product Catalog, Pricing, Stock & BCG Matrix Classification")
public class ProductController {

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

    @GetMapping
    @Operation(summary = "Get all products", description = "Returns complete list of products with current stock and profit margins")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/paged")
    @Operation(summary = "Get paginated & filtered products", description = "Filter by category and stock status")
    public ResponseEntity<Page<Product>> getProductsPaged(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return ResponseEntity.ok(productService.getProductsPaged(categoryId, status, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name or SKU")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products with low stock (<= reorder level)")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @GetMapping("/critical-stock")
    @Operation(summary = "Get critical stock products (<= safety stock)")
    public ResponseEntity<List<Product>> getCriticalStockProducts() {
        return ResponseEntity.ok(productService.getCriticalStockProducts());
    }

    @GetMapping("/bcg/{classification}")
    @Operation(summary = "Get products by BCG Matrix classification (STAR, CASH_COW, etc.)")
    public ResponseEntity<List<Product>> getProductsByBcg(@PathVariable BcgClassification classification) {
        return ResponseEntity.ok(productService.getProductsByBcg(classification));
    }

    @PostMapping
    @Operation(summary = "Create new product")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product, Authentication auth) {
        return ResponseEntity.ok(productService.createProduct(product, getCurrentUser(auth)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product details")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product, Authentication auth) {
        return ResponseEntity.ok(productService.updateProduct(id, product, getCurrentUser(auth)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (enforces transaction history constraint)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication auth) {
        productService.deleteProduct(id, getCurrentUser(auth));
        return ResponseEntity.noContent().build();
    }
}
