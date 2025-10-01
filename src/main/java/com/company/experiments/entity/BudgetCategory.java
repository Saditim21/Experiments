package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@JmixEntity
@Table(name = "BUDGET_CATEGORY", indexes = {
        @Index(name = "IDX_BUDGET_CATEGORY_CODE", columnList = "CODE", unique = true)
})
@Entity
public class BudgetCategory {
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

    @NotNull(message = "Category type is required")
    @Column(name = "CATEGORY_TYPE", nullable = false)
    private String categoryType;

    @JoinColumn(name = "PARENT_CATEGORY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private BudgetCategory parentCategory;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public BudgetCategory getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(BudgetCategory parentCategory) {
        this.parentCategory = parentCategory;
    }

    public BudgetCategoryType getCategoryType() {
        return categoryType == null ? null : BudgetCategoryType.fromId(categoryType);
    }

    public void setCategoryType(BudgetCategoryType categoryType) {
        this.categoryType = categoryType == null ? null : categoryType.getId();
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
