package com.example.intellierp.repository;

import com.example.intellierp.entity.Customer;
import com.example.intellierp.entity.enums.CustomerTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByTier(CustomerTier tier);
    List<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    List<Customer> findTop10ByOrderByTotalSpendDesc();

    long countByTier(CustomerTier tier);

    @Query("SELECT COALESCE(SUM(c.totalSpend), 0) FROM Customer c")
    BigDecimal sumTotalCustomerSpend();

    @Query("SELECT c FROM Customer c WHERE c.lastPurchaseDate < :cutoffDate OR c.lastPurchaseDate IS NULL")
    List<Customer> findInactiveCustomers(@Param("cutoffDate") LocalDateTime cutoffDate);
}
