package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "SCENARIO_COMPARISON")
@Entity
public class ScenarioComparison {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotNull
    @Column(name = "COMPARISON_NAME", nullable = false)
    private String comparisonName;

    @NotNull
    @JoinColumn(name = "BASELINE_SCENARIO_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private YearlyBudgetPlanningScenario baselineScenario;

    @JoinTable(name = "SCENARIO_COMPARISON_ALTERNATIVE_LINK",
            joinColumns = @JoinColumn(name = "SCENARIO_COMPARISON_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "YEARLY_BUDGET_PLANNING_SCENARIO_ID", referencedColumnName = "ID"))
    @ManyToMany
    private List<YearlyBudgetPlanningScenario> alternativeScenarios;

    @Lob
    @Column(name = "COMPARISON_METRICS")
    private String comparisonMetrics;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @JoinColumn(name = "CREATED_BY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee createdBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getComparisonMetrics() {
        return comparisonMetrics;
    }

    public void setComparisonMetrics(String comparisonMetrics) {
        this.comparisonMetrics = comparisonMetrics;
    }

    public List<YearlyBudgetPlanningScenario> getAlternativeScenarios() {
        return alternativeScenarios;
    }

    public void setAlternativeScenarios(List<YearlyBudgetPlanningScenario> alternativeScenarios) {
        this.alternativeScenarios = alternativeScenarios;
    }

    public YearlyBudgetPlanningScenario getBaselineScenario() {
        return baselineScenario;
    }

    public void setBaselineScenario(YearlyBudgetPlanningScenario baselineScenario) {
        this.baselineScenario = baselineScenario;
    }

    public String getComparisonName() {
        return comparisonName;
    }

    public void setComparisonName(String comparisonName) {
        this.comparisonName = comparisonName;
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
