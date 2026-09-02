package com.example.intellierp.repository;

import com.example.intellierp.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.sale.status = 'COMPLETED' AND si.sale.saleDate BETWEEN :start AND :end")
    Long sumUnitsSoldBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT si.product.id, si.product.name, si.product.sku, SUM(si.quantity), SUM(si.totalPrice), SUM(si.profit) " +
           "FROM SaleItem si WHERE si.sale.status = 'COMPLETED' " +
           "GROUP BY si.product.id, si.product.name, si.product.sku " +
           "ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopSellingProducts();

    @Query("SELECT si.product.id, si.product.name, si.product.sku, SUM(si.quantity), SUM(si.totalPrice), SUM(si.profit) " +
           "FROM SaleItem si WHERE si.sale.status = 'COMPLETED' " +
           "GROUP BY si.product.id, si.product.name, si.product.sku " +
           "ORDER BY SUM(si.profit) DESC")
    List<Object[]> findMostProfitableProducts();

    @Query("SELECT si.product.id, si.product.name, si.product.sku, SUM(si.quantity), SUM(si.totalPrice), SUM(si.profit) " +
           "FROM SaleItem si WHERE si.sale.status = 'COMPLETED' " +
           "GROUP BY si.product.id, si.product.name, si.product.sku " +
           "ORDER BY SUM(si.profit) ASC")
    List<Object[]> findLeastProfitableProducts();

    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si WHERE si.product.id = :productId AND si.sale.status = 'COMPLETED' AND si.sale.saleDate BETWEEN :start AND :end")
    Long sumProductQuantitySoldBetween(@Param("productId") Long productId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
