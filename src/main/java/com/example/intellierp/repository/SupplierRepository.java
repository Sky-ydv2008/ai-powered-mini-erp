package com.example.intellierp.repository;

import com.example.intellierp.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByNameIgnoreCase(String name);
    List<Supplier> findByNameContainingIgnoreCase(String name);
    List<Supplier> findAllByOrderByPerformanceScoreDesc();

    @Query("SELECT COALESCE(SUM(s.totalPurchases), 0) FROM Supplier s")
    BigDecimal sumTotalPurchases();

    @Query("SELECT COALESCE(SUM(s.estimatedLoss), 0) FROM Supplier s")
    BigDecimal sumTotalSupplierLoss();

    @Query("SELECT COALESCE(AVG(s.onTimeDeliveryRate), 0) FROM Supplier s")
    BigDecimal averageOnTimeDeliveryRate();

    @Query("SELECT s FROM Supplier s WHERE s.delayedOrders > 0 ORDER BY s.delayedOrders DESC")
    List<Supplier> findDelayedSuppliers();
}
