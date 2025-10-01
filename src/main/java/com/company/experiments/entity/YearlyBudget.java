package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity
@Table(name = "YEARLY_BUDGET", indexes = {
        @Index(name = "IDX_YEARLY_BUDGET_YEAR", columnList = "YEAR_"),
        @Index(name = "IDX_YEARLY_BUDGET_CATEGORY", columnList = "BUDGET_CATEGORY_ID"),
        @Index(name = "IDX_YEARLY_BUDGET_TEAM", columnList = "TEAM_ID"),
        @Index(name = "IDX_YEARLY_BUDGET_STATUS", columnList = "STATUS")
})
@Entity
public class YearlyBudget {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    @Column(name = "YEAR_", nullable = false)
    private Integer year;

    @NotNull(message = "Budget category is required")
    @JoinColumn(name = "BUDGET_CATEGORY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BudgetCategory budgetCategory;

    @NotNull(message = "Team is required")
    @JoinColumn(name = "TEAM_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Team team;

    @JoinColumn(name = "INFRASTRUCTURE_LEVEL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private InfrastructureLevel infrastructureLevel;

    @NotNull(message = "Allocated amount is required")
    @Positive(message = "Allocated amount must be positive")
    @Digits(integer = 12, fraction = 2, message = "Allocated amount must have at most 12 integer digits and 2 decimal places")
    @Column(name = "ALLOCATED_AMOUNT", nullable = false, precision = 14, scale = 2)
    private BigDecimal allocatedAmount;

    @PositiveOrZero(message = "Spent amount must be zero or positive")
    @Digits(integer = 12, fraction = 2, message = "Spent amount must have at most 12 integer digits and 2 decimal places")
    @Column(name = "SPENT_AMOUNT", precision = 14, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter uppercase code (e.g., EUR, USD)")
    @Column(name = "CURRENCY", nullable = false, length = 3)
    private String currency = "EUR";

    @NotNull(message = "Status is required")
    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "NOTES", length = 1000)
    private String notes;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BudgetStatus getStatus() {
        return status == null ? null : BudgetStatus.fromId(status);
    }

    public void setStatus(BudgetStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public InfrastructureLevel getInfrastructureLevel() {
        return infrastructureLevel;
    }

    public void setInfrastructureLevel(InfrastructureLevel infrastructureLevel) {
        this.infrastructureLevel = infrastructureLevel;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public BudgetCategory getBudgetCategory() {
        return budgetCategory;
    }

    public void setBudgetCategory(BudgetCategory budgetCategory) {
        this.budgetCategory = budgetCategory;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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
        return String.format("%d - %s - %s",
                year != null ? year : 0,
                budgetCategory != null ? budgetCategory.getName() : "",
                team != null ? team.getName() : "");
    }

    public BigDecimal getRemainingAmount() {
        if (allocatedAmount == null) return BigDecimal.ZERO;
        if (spentAmount == null) return allocatedAmount;
        return allocatedAmount.subtract(spentAmount);
    }

    public BigDecimal getSpentPercentage() {
        if (allocatedAmount == null || allocatedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (spentAmount == null) return BigDecimal.ZERO;
        return spentAmount.multiply(BigDecimal.valueOf(100)).divide(allocatedAmount, 2, java.math.RoundingMode.HALF_UP);
    }
}
