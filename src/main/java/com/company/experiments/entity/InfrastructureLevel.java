package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JmixEntity
@Table(name = "INFRASTRUCTURE_LEVEL", indexes = {
        @Index(name = "IDX_INFRASTRUCTURE_LEVEL_CODE", columnList = "CODE", unique = true)
})
@Entity
public class InfrastructureLevel {
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
    @Column(name = "LEVEL_", nullable = false)
    private Integer level;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @JoinColumn(name = "PARENT_LEVEL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private InfrastructureLevel parentLevel;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
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

    public InfrastructureLevel getParentLevel() {
        return parentLevel;
    }

    public void setParentLevel(InfrastructureLevel parentLevel) {
        this.parentLevel = parentLevel;
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
