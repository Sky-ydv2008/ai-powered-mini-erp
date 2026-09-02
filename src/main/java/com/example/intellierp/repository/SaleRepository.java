package com.example.intellierp.repository;

import com.example.intellierp.entity.Sale;
import com.example.intellierp.entity.enums.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    Optional<Sale> findByInvoiceNumber(String invoiceNumber);
    List<Sale> findByCustomerId(Long customerId);
    List<Sale> findByStatus(SaleStatus status);
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s WHERE (:customerId IS NULL OR s.customer.id = :customerId) AND (:status IS NULL OR s.status = :status) AND (s.saleDate BETWEEN :start AND :end)")
    Page<Sale> filterSales(@Param("customerId") Long customerId,
                           @Param("status") SaleStatus status,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end,
                           Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.totalCostBasis), 0) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumCostBasisBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumProfitBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    long countCompletedSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.discount), 0) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumDiscountsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.status = 'REFUNDED' AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumRefundsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal sumTotalRevenueAll();

    @Query("SELECT COALESCE(SUM(s.profit), 0) FROM Sale s WHERE s.status = 'COMPLETED'")
    BigDecimal sumTotalProfitAll();
}
