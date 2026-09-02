package com.example.intellierp.repository;

import com.example.intellierp.entity.AiInsight;
import com.example.intellierp.entity.enums.InsightSeverity;
import com.example.intellierp.entity.enums.InsightStatus;
import com.example.intellierp.entity.enums.InsightType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {
    List<AiInsight> findByStatusOrderByCreatedAtDesc(InsightStatus status);
    List<AiInsight> findByType(InsightType type);
    List<AiInsight> findBySeverity(InsightSeverity severity);
    long countByStatus(InsightStatus status);

    @Query("SELECT i FROM AiInsight i WHERE (:type IS NULL OR i.type = :type) AND (:severity IS NULL OR i.severity = :severity) AND (:status IS NULL OR i.status = :status) ORDER BY i.createdAt DESC")
    Page<AiInsight> filterInsights(@Param("type") InsightType type,
                                   @Param("severity") InsightSeverity severity,
                                   @Param("status") InsightStatus status,
                                   Pageable pageable);

    @Query("SELECT i FROM AiInsight i WHERE i.status = 'ACTIVE' ORDER BY CASE i.severity WHEN 'CRITICAL' THEN 1 WHEN 'WARNING' THEN 2 WHEN 'ATTENTION' THEN 3 WHEN 'OPPORTUNITY' THEN 4 ELSE 5 END, i.createdAt DESC")
    List<AiInsight> findActivePrioritizedInsights();
}
