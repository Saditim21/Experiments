package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "YEARLY_BUDGET_PLANNING_SCENARIO")
@Entity
public class YearlyBudgetPlanningScenario {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotNull
    @Column(name = "SCENARIO_NAME", nullable = false)
    private String scenarioName;

    @NotNull
    @Column(name = "YEAR_", nullable = false)
    private Integer year;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "TOTAL_BUDGET", precision = 19, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "OPTIMIZATION_TARGET")
    private String optimizationTarget;

    @Lob
    @Column(name = "CONSTRAINTS")
    private String constraints;

    @JoinColumn(name = "CREATED_BY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee createdBy;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public OptimizationTarget getOptimizationTarget() {
        return optimizationTarget == null ? null : OptimizationTarget.fromId(optimizationTarget);
    }

    public void setOptimizationTarget(OptimizationTarget optimizationTarget) {
        this.optimizationTarget = optimizationTarget == null ? null : optimizationTarget.getId();
    }

    public BigDecimal getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(BigDecimal totalBudget) {
        this.totalBudget = totalBudget;
    }

    public PlanningStatus getStatus() {
        return status == null ? null : PlanningStatus.fromId(status);
    }

    public void setStatus(PlanningStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
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
