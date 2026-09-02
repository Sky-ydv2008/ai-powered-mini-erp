package com.example.intellierp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnore
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal profit;

    public SaleItem() {
    }

    public SaleItem(Sale sale, Product product, Integer quantity, BigDecimal sellingPrice, BigDecimal costPrice, BigDecimal discount) {
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.costPrice = costPrice != null ? costPrice : BigDecimal.ZERO;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        recalculate();
    }

    public void recalculate() {
        if (this.sellingPrice != null && this.quantity != null) {
            BigDecimal sub = this.sellingPrice.multiply(BigDecimal.valueOf(this.quantity));
            BigDecimal disc = this.discount != null ? this.discount : BigDecimal.ZERO;
            this.totalPrice = sub.subtract(disc).max(BigDecimal.ZERO);

            BigDecimal totalCost = (this.costPrice != null ? this.costPrice : BigDecimal.ZERO)
                    .multiply(BigDecimal.valueOf(this.quantity));
            this.profit = this.totalPrice.subtract(totalCost);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
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
        recalculate();
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
        recalculate();
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
        recalculate();
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        recalculate();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}
