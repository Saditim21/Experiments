package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "TACTICAL_PLAN")
@Entity
public class TacticalPlan {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotNull
    @Column(name = "PLAN_NAME", nullable = false)
    private String planName;

    @JoinColumn(name = "PLANNING_SCENARIO_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private YearlyBudgetPlanningScenario planningScenario;

    @NotNull
    @JoinColumn(name = "MAINTENANCE_SCENARIO_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TheoreticalMaintenanceScenario maintenanceScenario;

    @Column(name = "PLANNED_START_DATE")
    private LocalDate plannedStartDate;

    @Column(name = "PLANNED_END_DATE")
    private LocalDate plannedEndDate;

    @JoinColumn(name = "ASSIGNED_TEAM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Team assignedTeam;

    @JoinTable(name = "TACTICAL_PLAN_EMPLOYEE_LINK",
            joinColumns = @JoinColumn(name = "TACTICAL_PLAN_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "ID"))
    @ManyToMany
    private List<Employee> assignedEmployees;

    @Column(name = "BUDGET_ALLOCATED", precision = 19, scale = 2)
    private BigDecimal budgetAllocated;

    @Column(name = "STATUS")
    private String status;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public TacticalPlanStatus getStatus() {
        return status == null ? null : TacticalPlanStatus.fromId(status);
    }

    public void setStatus(TacticalPlanStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public BigDecimal getBudgetAllocated() {
        return budgetAllocated;
    }

    public void setBudgetAllocated(BigDecimal budgetAllocated) {
        this.budgetAllocated = budgetAllocated;
    }

    public List<Employee> getAssignedEmployees() {
        return assignedEmployees;
    }

    public void setAssignedEmployees(List<Employee> assignedEmployees) {
        this.assignedEmployees = assignedEmployees;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(Team assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(LocalDate plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
    }

    public TheoreticalMaintenanceScenario getMaintenanceScenario() {
        return maintenanceScenario;
    }

    public void setMaintenanceScenario(TheoreticalMaintenanceScenario maintenanceScenario) {
        this.maintenanceScenario = maintenanceScenario;
    }

    public YearlyBudgetPlanningScenario getPlanningScenario() {
        return planningScenario;
    }

    public void setPlanningScenario(YearlyBudgetPlanningScenario planningScenario) {
        this.planningScenario = planningScenario;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
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
