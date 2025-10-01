package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "SCENARIO_METRICS")
@Entity
public class ScenarioMetrics {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull
    @JoinColumn(name = "SCENARIO_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private YearlyBudgetPlanningScenario scenario;

    @NotNull
    @Column(name = "METRIC_TYPE", nullable = false)
    private String metricType;

    @Column(name = "METRIC_VALUE", precision = 19, scale = 2)
    private BigDecimal metricValue;

    @Column(name = "CALCULATED_AT")
    private LocalDateTime calculatedAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    public MetricType getMetricType() {
        return metricType == null ? null : MetricType.fromId(metricType);
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType == null ? null : metricType.getId();
    }

    public YearlyBudgetPlanningScenario getScenario() {
        return scenario;
    }

    public void setScenario(YearlyBudgetPlanningScenario scenario) {
        this.scenario = scenario;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
