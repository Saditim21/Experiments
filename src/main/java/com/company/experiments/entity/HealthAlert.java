package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "HEALTH_ALERT", indexes = {
        @Index(name = "IDX_HEALTH_ALERT_ID", columnList = "ALERT_ID", unique = true),
        @Index(name = "IDX_HEALTH_ALERT_INFRASTRUCTURE", columnList = "INFRASTRUCTURE_NODE_ID"),
        @Index(name = "IDX_HEALTH_ALERT_STATUS", columnList = "STATUS"),
        @Index(name = "IDX_HEALTH_ALERT_SEVERITY", columnList = "SEVERITY"),
        @Index(name = "IDX_HEALTH_ALERT_DETECTED_AT", columnList = "DETECTED_AT")
})
@Entity
public class HealthAlert {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotBlank(message = "Alert ID is required")
    @Column(name = "ALERT_ID", nullable = false, unique = true, length = 50)
    private String alertId;

    @NotNull(message = "Infrastructure node is required")
    @JoinColumn(name = "INFRASTRUCTURE_NODE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private InfrastructureHierarchy infrastructureNode;

    @NotNull(message = "Alert type is required")
    @Column(name = "ALERT_TYPE", nullable = false)
    private String alertType;

    @NotNull(message = "Severity is required")
    @Column(name = "SEVERITY", nullable = false)
    private String severity;

    @NotNull(message = "Detection time is required")
    @Column(name = "DETECTED_AT", nullable = false)
    private LocalDateTime detectedAt;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(name = "DESCRIPTION", nullable = false, length = 2000)
    private String description;

    @Column(name = "SENSOR_DATA", length = 5000)
    private String sensorData;

    @Column(name = "PREDICTED_FAILURE_DATE")
    private LocalDate predictedFailureDate;

    @DecimalMin(value = "0.0", message = "Probability score must be between 0 and 100")
    @DecimalMax(value = "100.0", message = "Probability score must be between 0 and 100")
    @Digits(integer = 3, fraction = 2, message = "Probability score must have at most 3 integer digits and 2 decimal places")
    @Column(name = "PROBABILITY_SCORE", precision = 5, scale = 2)
    private BigDecimal probabilityScore;

    @NotNull(message = "Status is required")
    @Column(name = "STATUS", nullable = false)
    private String status;

    @JoinColumn(name = "ASSIGNED_TO_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee assignedTo;

    @Column(name = "RESOLVED_AT")
    private LocalDateTime resolvedAt;

    @Column(name = "RESOLUTION_NOTES", length = 2000)
    private String resolutionNotes;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Employee getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Employee assignedTo) {
        this.assignedTo = assignedTo;
    }

    public AlertStatus getStatus() {
        return status == null ? null : AlertStatus.fromId(status);
    }

    public void setStatus(AlertStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public BigDecimal getProbabilityScore() {
        return probabilityScore;
    }

    public void setProbabilityScore(BigDecimal probabilityScore) {
        this.probabilityScore = probabilityScore;
    }

    public LocalDate getPredictedFailureDate() {
        return predictedFailureDate;
    }

    public void setPredictedFailureDate(LocalDate predictedFailureDate) {
        this.predictedFailureDate = predictedFailureDate;
    }

    public String getSensorData() {
        return sensorData;
    }

    public void setSensorData(String sensorData) {
        this.sensorData = sensorData;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public AlertSeverity getSeverity() {
        return severity == null ? null : AlertSeverity.fromId(severity);
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity == null ? null : severity.getId();
    }

    public AlertType getAlertType() {
        return alertType == null ? null : AlertType.fromId(alertType);
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType == null ? null : alertType.getId();
    }

    public InfrastructureHierarchy getInfrastructureNode() {
        return infrastructureNode;
    }

    public void setInfrastructureNode(InfrastructureHierarchy infrastructureNode) {
        this.infrastructureNode = infrastructureNode;
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
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
                alertId,
                severity != null ? AlertSeverity.fromId(severity).getId() : "",
                infrastructureNode != null ? infrastructureNode.getName() : "");
    }

    public boolean isOpen() {
        AlertStatus currentStatus = getStatus();
        return currentStatus == AlertStatus.NEW || currentStatus == AlertStatus.INVESTIGATING;
    }

    public boolean isCriticalOrHigh() {
        AlertSeverity sev = getSeverity();
        return sev == AlertSeverity.CRITICAL || sev == AlertSeverity.HIGH;
    }
}
