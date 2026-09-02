package com.example.intellierp.entity;

import com.example.intellierp.entity.enums.PaymentMethod;
import com.example.intellierp.entity.enums.SaleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales", uniqueConstraints = {
        @UniqueConstraint(columnNames = "invoice_number")
})
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "invoice_number", nullable = false, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate = LocalDateTime.now();

    @Column(precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_cost_basis", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCostBasis = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal profit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 25)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SaleStatus status = SaleStatus.COMPLETED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Sale() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.saleDate == null) {
            this.saleDate = LocalDateTime.now();
        }
    }

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
        recalculateTotals();
    }

    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        BigDecimal sumSubtotal = BigDecimal.ZERO;
        BigDecimal sumCost = BigDecimal.ZERO;
        BigDecimal sumProfit = BigDecimal.ZERO;

        for (SaleItem item : items) {
            if (item.getTotalPrice() != null) {
                sumSubtotal = sumSubtotal.add(item.getTotalPrice());
            }
            if (item.getCostPrice() != null && item.getQuantity() != null) {
                sumCost = sumCost.add(item.getCostPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            if (item.getProfit() != null) {
                sumProfit = sumProfit.add(item.getProfit());
            }
        }

        this.subtotal = sumSubtotal;
        this.totalCostBasis = sumCost;

        BigDecimal total = sumSubtotal;
        if (this.tax != null) {
            total = total.add(this.tax);
        }
        if (this.discount != null) {
            total = total.subtract(this.discount);
            sumProfit = sumProfit.subtract(this.discount);
        }
        this.totalAmount = total.max(BigDecimal.ZERO);
        this.profit = sumProfit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalCostBasis() {
        return totalCostBasis;
    }

    public void setTotalCostBasis(BigDecimal totalCostBasis) {
        this.totalCostBasis = totalCostBasis;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
        if (items != null) {
            for (SaleItem item : items) {
                item.setSale(this);
            }
        }
        recalculateTotals();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
