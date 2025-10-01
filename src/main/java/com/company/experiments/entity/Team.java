package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "TEAM", indexes = {
        @Index(name = "IDX_TEAM_CODE", columnList = "CODE", unique = true)
})
@Entity
public class Team {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, underscores and hyphens")
    @Size(min = 2, max = 20, message = "Code must be between 2 and 20 characters")
    @Column(name = "CODE", nullable = false, unique = true, length = 20)
    private String code;

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "DEPARTMENT", length = 100)
    private String department;

    @JoinColumn(name = "TEAM_LEAD_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Employee teamLead;

    @JoinTable(name = "TEAM_INFRASTRUCTURE_RESPONSIBILITY",
            joinColumns = @JoinColumn(name = "TEAM_ID"),
            inverseJoinColumns = @JoinColumn(name = "INFRASTRUCTURE_HIERARCHY_ID"))
    @ManyToMany
    private List<InfrastructureHierarchy> infrastructureResponsibility;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public List<InfrastructureHierarchy> getInfrastructureResponsibility() {
        return infrastructureResponsibility;
    }

    public void setInfrastructureResponsibility(List<InfrastructureHierarchy> infrastructureResponsibility) {
        this.infrastructureResponsibility = infrastructureResponsibility;
    }

    public Employee getTeamLead() {
        return teamLead;
    }

    public void setTeamLead(Employee teamLead) {
        this.teamLead = teamLead;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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
