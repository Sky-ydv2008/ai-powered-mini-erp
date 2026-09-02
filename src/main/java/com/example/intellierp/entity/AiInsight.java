package com.example.intellierp.entity;

import com.example.intellierp.entity.enums.InsightSeverity;
import com.example.intellierp.entity.enums.InsightStatus;
import com.example.intellierp.entity.enums.InsightType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_insights")
public class AiInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InsightType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightSeverity severity;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "metric_summary", length = 255)
    private String metricSummary;

    @Column(name = "evidence_json", columnDefinition = "TEXT")
    private String evidenceJson;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "financial_impact", precision = 15, scale = 2)
    private BigDecimal financialImpact = BigDecimal.ZERO;

    @Column(name = "impact_description", columnDefinition = "TEXT")
    private String impactDescription;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore = new BigDecimal("90.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InsightStatus status = InsightStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AiInsight() {
    }

    public AiInsight(InsightType type, InsightSeverity severity, String title, String metricSummary,
                     String evidenceJson, String rootCause, BigDecimal financialImpact,
                     String impactDescription, String recommendation, BigDecimal confidenceScore) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.metricSummary = metricSummary;
        this.evidenceJson = evidenceJson;
        this.rootCause = rootCause;
        this.financialImpact = financialImpact != null ? financialImpact : BigDecimal.ZERO;
        this.impactDescription = impactDescription;
        this.recommendation = recommendation;
        this.confidenceScore = confidenceScore != null ? confidenceScore : new BigDecimal("90.00");
        this.status = InsightStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = InsightStatus.ACTIVE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InsightType getType() {
        return type;
    }

    public void setType(InsightType type) {
        this.type = type;
    }

    public InsightSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(InsightSeverity severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMetricSummary() {
        return metricSummary;
    }

    public void setMetricSummary(String metricSummary) {
        this.metricSummary = metricSummary;
    }

    public String getEvidenceJson() {
        return evidenceJson;
    }

    public void setEvidenceJson(String evidenceJson) {
        this.evidenceJson = evidenceJson;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public BigDecimal getFinancialImpact() {
        return financialImpact;
    }

    public void setFinancialImpact(BigDecimal financialImpact) {
        this.financialImpact = financialImpact;
    }

    public String getImpactDescription() {
        return impactDescription;
    }

    public void setImpactDescription(String impactDescription) {
        this.impactDescription = impactDescription;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public InsightStatus getStatus() {
        return status;
    }

    public void setStatus(InsightStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
