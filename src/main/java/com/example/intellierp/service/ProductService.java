package com.example.intellierp.service;

import com.example.intellierp.entity.Product;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.BcgClassification;
import com.example.intellierp.entity.enums.ProductStatus;
import com.example.intellierp.entity.enums.StockTransactionType;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.ProductRepository;
import com.example.intellierp.repository.PurchaseItemRepository;
import com.example.intellierp.repository.SaleItemRepository;
import com.example.intellierp.repository.StockTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private PurchaseItemRepository purchaseItemRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AuditLogService auditLogService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProductsPaged(Long categoryId, ProductStatus status, Pageable pageable) {
        return productRepository.filterProducts(categoryId, status, pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
    }

    public List<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> getCriticalStockProducts() {
        return productRepository.findCriticalStockProducts();
    }

    public List<Product> getProductsByBcg(BcgClassification bcg) {
        return productRepository.findByBcgClassification(bcg);
    }

    @Transactional
    public Product createProduct(Product product, User currentUser) {
        if (product.getPurchasePrice() == null || product.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Purchase price cannot be negative");
        }
        if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Selling price cannot be negative");
        }
        if (product.getCurrentStock() == null || product.getCurrentStock() < 0) {
            throw new BadRequestException("Stock quantity cannot be negative");
        }
        if (productRepository.existsBySku(product.getSku())) {
            throw new BadRequestException("Product with SKU '" + product.getSku() + "' already exists");
        }

        product.updateStatus();
        Product saved = productRepository.save(product);

        // Record initial stock if greater than 0
        if (saved.getCurrentStock() > 0) {
            inventoryService.recordStockMovement(
                    saved,
                    saved.getCurrentStock(),
                    StockTransactionType.ADJUSTMENT,
                    "INIT-" + saved.getId(),
                    "Initial stock balance",
                    currentUser
            );
        }

        auditLogService.logAction(currentUser, "CREATE_PRODUCT", "Product", saved.getId(),
                "Created product " + saved.getName() + " (SKU: " + saved.getSku() + ")", null);

        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, Product details, User currentUser) {
        Product product = getProductById(id);

        if (details.getPurchasePrice() != null && details.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Purchase price cannot be negative");
        }
        if (details.getSellingPrice() != null && details.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Selling price cannot be negative");
        }

        product.setName(details.getName());
        product.setCategory(details.getCategory());
        product.setPreferredSupplier(details.getPreferredSupplier());
        product.setPurchasePrice(details.getPurchasePrice());
        product.setSellingPrice(details.getSellingPrice());
        product.setReorderLevel(details.getReorderLevel());
        product.setSafetyStock(details.getSafetyStock());
        product.setLeadTimeDays(details.getLeadTimeDays());
        product.setUnit(details.getUnit());
        product.setBarcode(details.getBarcode());
        if (details.getBcgClassification() != null) {
            product.setBcgClassification(details.getBcgClassification());
        }

        product.updateStatus();
        Product saved = productRepository.save(product);

        auditLogService.logAction(currentUser, "UPDATE_PRODUCT", "Product", saved.getId(),
                "Updated product " + saved.getName(), null);

        return saved;
    }

    @Transactional
    public void deleteProduct(Long id, User currentUser) {
        Product product = getProductById(id);

        // Business Rule: Cannot delete a product with historical transactions
        if (!saleItemRepository.findByProductId(id).isEmpty() || !purchaseItemRepository.findByProductId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete product '" + product.getName() + "' because historical sales or purchase records exist. Consider setting stock to 0 instead.");
        }

        productRepository.delete(product);
        auditLogService.logAction(currentUser, "DELETE_PRODUCT", "Product", id,
                "Deleted product " + product.getName(), null);
    }
}
