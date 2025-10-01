package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "RULE_SCENARIO", indexes = {
        @Index(name = "IDX_RULE_SCENARIO_CODE", columnList = "CODE", unique = true),
        @Index(name = "IDX_RULE_SCENARIO_TYPE", columnList = "SCENARIO_TYPE"),
        @Index(name = "IDX_RULE_SCENARIO_ACTIVE", columnList = "IS_ACTIVE"),
        @Index(name = "IDX_RULE_SCENARIO_PRIORITY", columnList = "PRIORITY")
})
@Entity
public class RuleScenario {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, underscores and hyphens")
    @Size(min = 2, max = 50, message = "Code must be between 2 and 50 characters")
    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @NotNull(message = "Scenario type is required")
    @Column(name = "SCENARIO_TYPE", nullable = false)
    private String scenarioType;

    @NotNull(message = "Priority is required")
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 100, message = "Priority must not exceed 100")
    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @NotNull(message = "Is active flag is required")
    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @NotBlank(message = "Conditions are required")
    @Column(name = "CONDITIONS", nullable = false, length = 5000)
    private String conditions;

    @NotBlank(message = "Actions are required")
    @Column(name = "ACTIONS", nullable = false, length = 5000)
    private String actions;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @NotNull(message = "Created by is required")
    @JoinColumn(name = "CREATED_BY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee createdBy;

    @NotNull(message = "Created at is required")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "LAST_EXECUTED_AT")
    private LocalDateTime lastExecutedAt;

    @Column(name = "EXECUTION_COUNT")
    private Integer executionCount = 0;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Integer getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }

    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
    }

    public void setLastExecutedAt(LocalDateTime lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public ScenarioType getScenarioType() {
        return scenarioType == null ? null : ScenarioType.fromId(scenarioType);
    }

    public void setScenarioType(ScenarioType scenarioType) {
        this.scenarioType = scenarioType == null ? null : scenarioType.getId();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (executionCount == null) {
            executionCount = 0;
        }
    }
}
