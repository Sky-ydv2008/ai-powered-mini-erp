package com.example.intellierp.service;

import com.example.intellierp.entity.Product;
import com.example.intellierp.entity.StockTransaction;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.ProductStatus;
import com.example.intellierp.entity.enums.StockTransactionType;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.InsufficientStockException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.ProductRepository;
import com.example.intellierp.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public StockTransaction recordStockMovement(Product product, Integer quantity, StockTransactionType type,
                                               String referenceId, String reason, User user) {
        int newStock = (product.getCurrentStock() != null ? product.getCurrentStock() : 0);
        product.setCurrentStock(newStock);
        product.updateStatus();
        productRepository.save(product);

        StockTransaction transaction = new StockTransaction(
                product,
                quantity,
                type,
                referenceId,
                reason,
                user,
                newStock
        );

        return stockTransactionRepository.save(transaction);
    }

    @Transactional
    public StockTransaction adjustStock(Long productId, Integer quantityAdjustment, StockTransactionType type, String reason, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int current = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        int target = current + quantityAdjustment;

        if (target < 0) {
            throw new InsufficientStockException("Cannot adjust stock to a negative number. Current: " + current + ", Adjustment: " + quantityAdjustment);
        }

        product.setCurrentStock(target);
        product.updateStatus();
        productRepository.save(product);

        StockTransaction tx = new StockTransaction(
                product,
                quantityAdjustment,
                type != null ? type : StockTransactionType.ADJUSTMENT,
                "ADJ-" + System.currentTimeMillis(),
                reason,
                user,
                target
        );

        StockTransaction savedTx = stockTransactionRepository.save(tx);
        auditLogService.logAction(user, "STOCK_ADJUSTMENT", "Inventory", product.getId(),
                "Stock adjusted by " + quantityAdjustment + " for " + product.getName() + " (New balance: " + target + ")", null);

        return savedTx;
    }

    public Page<StockTransaction> getStockLedger(Long productId, StockTransactionType type, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        LocalDateTime effectiveStart = start != null ? start : LocalDateTime.now().minusYears(1);
        LocalDateTime effectiveEnd = end != null ? end : LocalDateTime.now().plusDays(1);
        return stockTransactionRepository.filterTransactions(productId, type, effectiveStart, effectiveEnd, pageable);
    }

    public List<StockTransaction> getProductStockHistory(Long productId) {
        return stockTransactionRepository.findByProductIdOrderByTimestampDesc(productId);
    }

    public Map<String, Object> getInventoryOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalStockUnits", productRepository.sumTotalStockUnits());
        overview.put("totalStockValue", productRepository.sumTotalStockValue());
        overview.put("totalProducts", productRepository.count());
        overview.put("healthyProducts", productRepository.countByStatus(ProductStatus.HEALTHY));
        overview.put("lowStockProducts", productRepository.countLowStockProducts());
        overview.put("criticalStockProducts", productRepository.findCriticalStockProducts().size());
        overview.put("outOfStockProducts", productRepository.countOutOfStockProducts());
        return overview;
    }
}
