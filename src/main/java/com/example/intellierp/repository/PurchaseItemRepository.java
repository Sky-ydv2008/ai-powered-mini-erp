package com.example.intellierp.repository;

import com.example.intellierp.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    List<PurchaseItem> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(pi.quantity), 0) FROM PurchaseItem pi WHERE pi.product.id = :productId AND pi.purchase.status = 'RECEIVED'")
    Long sumReceivedQuantityByProduct(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(pi.quantity), 0) FROM PurchaseItem pi WHERE pi.purchase.status = 'RECEIVED' AND pi.purchase.orderDate BETWEEN :startDate AND :endDate")
    Long sumTotalUnitsAddedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
