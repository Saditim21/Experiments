package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "INFRASTRUCTURE_HIERARCHY", indexes = {
        @Index(name = "IDX_INFRASTRUCTURE_HIERARCHY_CODE", columnList = "CODE", unique = true)
})
@Entity
public class InfrastructureHierarchy {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @NotNull
    @Column(name = "CODE", nullable = false, unique = true)
    private String code;

    @NotNull
    @JoinColumn(name = "INFRASTRUCTURE_LEVEL_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private InfrastructureLevel infrastructureLevel;

    @JoinColumn(name = "PARENT_NODE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private InfrastructureHierarchy parentNode;

    @Column(name = "ASSET_TYPE")
    private String assetType;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "INSTALLATION_DATE")
    private LocalDate installationDate;

    @Min(1)
    @Max(10)
    @Column(name = "CRITICALITY_SCORE")
    private Integer criticalityScore;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Integer getCriticalityScore() {
        return criticalityScore;
    }

    public void setCriticalityScore(Integer criticalityScore) {
        this.criticalityScore = criticalityScore;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public InfrastructureStatus getStatus() {
        return status == null ? null : InfrastructureStatus.fromId(status);
    }

    public void setStatus(InfrastructureStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public InfrastructureHierarchy getParentNode() {
        return parentNode;
    }

    public void setParentNode(InfrastructureHierarchy parentNode) {
        this.parentNode = parentNode;
    }

    public InfrastructureLevel getInfrastructureLevel() {
        return infrastructureLevel;
    }

    public void setInfrastructureLevel(InfrastructureLevel infrastructureLevel) {
        this.infrastructureLevel = infrastructureLevel;
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
}
