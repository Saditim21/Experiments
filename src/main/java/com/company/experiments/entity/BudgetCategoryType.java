package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum BudgetCategoryType implements EnumClass<String> {

    PREVENTIVE("PREVENTIVE"),
    CORRECTIVE("CORRECTIVE"),
    PREDICTIVE("PREDICTIVE"),
    IMPROVEMENT("IMPROVEMENT");

    private final String id;

    BudgetCategoryType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static BudgetCategoryType fromId(String id) {
        for (BudgetCategoryType at : BudgetCategoryType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
