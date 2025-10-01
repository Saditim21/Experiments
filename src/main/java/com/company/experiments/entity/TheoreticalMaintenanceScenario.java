package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "THEORETICAL_MAINTENANCE_SCENARIO", indexes = {
        @Index(name = "IDX_THEORETICAL_MAINTENANCE_SCENARIO_CODE", columnList = "SCENARIO_CODE", unique = true)
})
@Entity
public class TheoreticalMaintenanceScenario {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotNull
    @Column(name = "SCENARIO_NAME", nullable = false)
    private String scenarioName;

    @NotNull
    @Column(name = "SCENARIO_CODE", nullable = false, unique = true)
    private String scenarioCode;

    @NotNull
    @JoinColumn(name = "INFRASTRUCTURE_NODE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private InfrastructureHierarchy infrastructureNode;

    @NotNull
    @Column(name = "MAINTENANCE_TYPE", nullable = false)
    private String maintenanceType;

    @Column(name = "ESTIMATED_DURATION")
    private Integer estimatedDuration;

    @Column(name = "REQUIRED_SKILLS", length = 1000)
    private String requiredSkills;

    @Column(name = "ESTIMATED_COST", precision = 19, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "FREQUENCY")
    private String frequency;

    @Min(1)
    @Max(10)
    @Column(name = "PRIORITY")
    private Integer priority;

    @Lob
    @Column(name = "SAFETY_REQUIREMENTS")
    private String safetyRequirements;

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

    public String getSafetyRequirements() {
        return safetyRequirements;
    }

    public void setSafetyRequirements(String safetyRequirements) {
        this.safetyRequirements = safetyRequirements;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public FrequencyType getFrequency() {
        return frequency == null ? null : FrequencyType.fromId(frequency);
    }

    public void setFrequency(FrequencyType frequency) {
        this.frequency = frequency == null ? null : frequency.getId();
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public MaintenanceType getMaintenanceType() {
        return maintenanceType == null ? null : MaintenanceType.fromId(maintenanceType);
    }

    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType == null ? null : maintenanceType.getId();
    }

    public InfrastructureHierarchy getInfrastructureNode() {
        return infrastructureNode;
    }

    public void setInfrastructureNode(InfrastructureHierarchy infrastructureNode) {
        this.infrastructureNode = infrastructureNode;
    }

    public String getScenarioCode() {
        return scenarioCode;
    }

    public void setScenarioCode(String scenarioCode) {
        this.scenarioCode = scenarioCode;
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
