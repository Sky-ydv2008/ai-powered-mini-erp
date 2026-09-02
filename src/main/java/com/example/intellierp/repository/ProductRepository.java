package com.example.intellierp.repository;

import com.example.intellierp.entity.Product;
import com.example.intellierp.entity.enums.BcgClassification;
import com.example.intellierp.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
    boolean existsBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByBcgClassification(BcgClassification bcgClassification);

    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.reorderLevel ORDER BY p.currentStock ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.safetyStock ORDER BY p.currentStock ASC")
    List<Product> findCriticalStockProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProducts(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.id = :categoryId) AND (:status IS NULL OR p.status = :status)")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId, @Param("status") ProductStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.purchasePrice * p.currentStock), 0) FROM Product p")
    BigDecimal sumTotalStockValue();

    @Query("SELECT COALESCE(SUM(p.currentStock), 0) FROM Product p")
    Long sumTotalStockUnits();

    long countByStatus(ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock <= p.reorderLevel")
    long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.currentStock = 0")
    long countOutOfStockProducts();
}
