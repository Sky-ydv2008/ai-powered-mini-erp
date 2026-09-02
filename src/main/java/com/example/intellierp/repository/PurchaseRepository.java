package com.example.intellierp.repository;

import com.example.intellierp.entity.Purchase;
import com.example.intellierp.entity.enums.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByOrderNumber(String orderNumber);
    List<Purchase> findBySupplierId(Long supplierId);
    List<Purchase> findByStatus(PurchaseStatus status);
    List<Purchase> findByOrderDateBetweenOrderByOrderDateDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Purchase p WHERE (:supplierId IS NULL OR p.supplier.id = :supplierId) AND (:status IS NULL OR p.status = :status) AND (p.orderDate BETWEEN :startDate AND :endDate)")
    Page<Purchase> filterPurchases(@Param("supplierId") Long supplierId,
                                   @Param("status") PurchaseStatus status,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.totalCost), 0) FROM Purchase p WHERE p.status != 'CANCELLED' AND p.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalCostBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.orderDate BETWEEN :startDate AND :endDate")
    long countBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.totalCost), 0) FROM Purchase p WHERE p.status != 'CANCELLED'")
    BigDecimal sumTotalCostAll();
}
