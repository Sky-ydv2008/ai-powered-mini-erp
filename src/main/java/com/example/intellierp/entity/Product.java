package com.example.intellierp.entity;

import com.example.intellierp.entity.enums.BcgClassification;
import com.example.intellierp.entity.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = "sku")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String sku;

    @Size(max = 50)
    @Column(length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "preferred_supplier_id")
    private Supplier preferredSupplier;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull
    @Min(0)
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @NotNull
    @Min(0)
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    @NotNull
    @Min(0)
    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 10;

    @NotNull
    @Min(0)
    @Column(name = "safety_stock", nullable = false)
    private Integer safetyStock = 5;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays = 7;

    @Column(length = 20)
    private String unit = "pcs";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.HEALTHY;

    @Enumerated(EnumType.STRING)
    @Column(name = "bcg_classification", length = 20)
    private BcgClassification bcgClassification = BcgClassification.CASH_COW;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {
    }

    public Product(String name, String sku, Category category, Supplier supplier,
                   BigDecimal purchasePrice, BigDecimal sellingPrice,
                   Integer currentStock, Integer reorderLevel, Integer safetyStock) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.preferredSupplier = supplier;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.currentStock = currentStock != null ? currentStock : 0;
        this.reorderLevel = reorderLevel != null ? reorderLevel : 10;
        this.safetyStock = safetyStock != null ? safetyStock : 5;
        this.leadTimeDays = supplier != null && supplier.getLeadTimeDays() != null ? supplier.getLeadTimeDays() : 7;
        updateStatus();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        updateStatus();
    }

    public void updateStatus() {
        if (currentStock == null || currentStock <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (safetyStock != null && currentStock <= safetyStock) {
            this.status = ProductStatus.CRITICAL;
        } else if (reorderLevel != null && currentStock <= reorderLevel) {
            this.status = ProductStatus.LOW_STOCK;
        } else {
            this.status = ProductStatus.HEALTHY;
        }
    }

    public BigDecimal getStockValue() {
        if (currentStock == null || purchasePrice == null) {
            return BigDecimal.ZERO;
        }
        return purchasePrice.multiply(BigDecimal.valueOf(currentStock));
    }

    public BigDecimal getProfitMargin() {
        if (sellingPrice == null || purchasePrice == null || sellingPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal profit = sellingPrice.subtract(purchasePrice);
        return profit.divide(sellingPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Supplier getPreferredSupplier() {
        return preferredSupplier;
    }

    public void setPreferredSupplier(Supplier preferredSupplier) {
        this.preferredSupplier = preferredSupplier;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
        updateStatus();
    }

    public Integer getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(Integer reservedStock) {
        this.reservedStock = reservedStock;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
        updateStatus();
    }

    public Integer getSafetyStock() {
        return safetyStock;
    }

    public void setSafetyStock(Integer safetyStock) {
        this.safetyStock = safetyStock;
        updateStatus();
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public BcgClassification getBcgClassification() {
        return bcgClassification;
    }

    public void setBcgClassification(BcgClassification bcgClassification) {
        this.bcgClassification = bcgClassification;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
