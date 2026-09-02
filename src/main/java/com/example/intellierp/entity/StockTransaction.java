package com.example.intellierp.entity;

import com.example.intellierp.entity.enums.StockTransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private Integer quantity; // positive for additions, negative for reductions

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StockTransactionType type;

    @Size(max = 100)
    @Column(name = "reference_id", length = 100)
    private String referenceId; // e.g. PO-1001, INV-2024-001

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public StockTransaction() {
    }

    public StockTransaction(Product product, Integer quantity, StockTransactionType type,
                            String referenceId, String reason, User user, Integer balanceAfter) {
        this.product = product;
        this.quantity = quantity;
        this.type = type;
        this.referenceId = referenceId;
        this.reason = reason;
        this.user = user;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public StockTransactionType getType() {
        return type;
    }

    public void setType(StockTransactionType type) {
        this.type = type;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
