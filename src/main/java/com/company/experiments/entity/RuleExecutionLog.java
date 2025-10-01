package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "RULE_EXECUTION_LOG", indexes = {
        @Index(name = "IDX_RULE_EXECUTION_LOG_SCENARIO", columnList = "RULE_SCENARIO_ID"),
        @Index(name = "IDX_RULE_EXECUTION_LOG_EXECUTED_AT", columnList = "EXECUTED_AT"),
        @Index(name = "IDX_RULE_EXECUTION_LOG_SUCCESS", columnList = "SUCCESS")
})
@Entity
public class RuleExecutionLog {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull(message = "Rule scenario is required")
    @JoinColumn(name = "RULE_SCENARIO_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RuleScenario ruleScenario;

    @NotNull(message = "Executed at is required")
    @Column(name = "EXECUTED_AT", nullable = false)
    private LocalDateTime executedAt;

    @NotNull(message = "Success flag is required")
    @Column(name = "SUCCESS", nullable = false)
    private Boolean success;

    @Column(name = "INPUT_DATA", length = 5000)
    private String inputData;

    @Column(name = "OUTPUT_DATA", length = 5000)
    private String outputData;

    @Column(name = "ERROR_MESSAGE", length = 2000)
    private String errorMessage;

    @Column(name = "EXECUTION_TIME_MS")
    private Integer executionTimeMs;

    @JoinColumn(name = "TRIGGERED_BY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee triggeredBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Employee getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(Employee triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOutputData() {
        return outputData;
    }

    public void setOutputData(String outputData) {
        this.outputData = outputData;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public RuleScenario getRuleScenario() {
        return ruleScenario;
    }

    public void setRuleScenario(RuleScenario ruleScenario) {
        this.ruleScenario = ruleScenario;
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

    @InstanceName
    public String getInstanceName() {
        return String.format("%s - %s - %s",
                ruleScenario != null ? ruleScenario.getName() : "",
                executedAt != null ? executedAt.toString() : "",
                success ? "Success" : "Failed");
    }

    @PrePersist
    public void prePersist() {
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
    }
}
