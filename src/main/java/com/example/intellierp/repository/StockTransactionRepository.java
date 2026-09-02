package com.example.intellierp.repository;

import com.example.intellierp.entity.StockTransaction;
import com.example.intellierp.entity.enums.StockTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProductIdOrderByTimestampDesc(Long productId);
    List<StockTransaction> findByTypeOrderByTimestampDesc(StockTransactionType type);
    List<StockTransaction> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT st FROM StockTransaction st WHERE (:productId IS NULL OR st.product.id = :productId) AND (:type IS NULL OR st.type = :type) AND (st.timestamp BETWEEN :start AND :end)")
    Page<StockTransaction> filterTransactions(@Param("productId") Long productId,
                                              @Param("type") StockTransactionType type,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end,
                                              Pageable pageable);

    @Query("SELECT COALESCE(SUM(st.quantity), 0) FROM StockTransaction st WHERE st.type = :type AND st.timestamp BETWEEN :start AND :end")
    Long sumQuantityByTypeAndDateBetween(@Param("type") StockTransactionType type,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(st.quantity), 0) FROM StockTransaction st WHERE st.product.id = :productId AND st.type = 'SALE' AND st.timestamp BETWEEN :start AND :end")
    Long sumProductUnitsSoldBetween(@Param("productId") Long productId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
