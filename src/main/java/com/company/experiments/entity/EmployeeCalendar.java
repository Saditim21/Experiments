package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JmixEntity
@Table(name = "EMPLOYEE_CALENDAR", indexes = {
        @Index(name = "IDX_EMPLOYEE_CALENDAR_EMPLOYEE_DATE", columnList = "EMPLOYEE_ID, DATE_", unique = true)
})
@Entity
public class EmployeeCalendar {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull(message = "Employee is required")
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    @NotNull(message = "Date is required")
    @Column(name = "DATE_", nullable = false)
    private LocalDate date;

    @NotNull(message = "Availability type is required")
    @Column(name = "AVAILABILITY_TYPE", nullable = false)
    private String availabilityType;

    @PositiveOrZero(message = "Hours available must be zero or positive")
    @DecimalMax(value = "24.00", message = "Hours available cannot exceed 24 hours")
    @Digits(integer = 3, fraction = 2, message = "Hours available must have at most 3 integer digits and 2 decimal places")
    @Column(name = "HOURS_AVAILABLE", precision = 5, scale = 2)
    private BigDecimal hoursAvailable;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    @Column(name = "NOTES", length = 500)
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

    public BigDecimal getHoursAvailable() {
        return hoursAvailable;
    }

    public void setHoursAvailable(BigDecimal hoursAvailable) {
        this.hoursAvailable = hoursAvailable;
    }

    public AvailabilityType getAvailabilityType() {
        return availabilityType == null ? null : AvailabilityType.fromId(availabilityType);
    }

    public void setAvailabilityType(AvailabilityType availabilityType) {
        this.availabilityType = availabilityType == null ? null : availabilityType.getId();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
        return String.format("%s - %s", employee != null ? employee.getInstanceName() : "", date);
    }
}
