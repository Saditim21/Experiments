package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum BudgetStatus implements EnumClass<String> {

    DRAFT("DRAFT"),
    APPROVED("APPROVED"),
    IN_USE("IN_USE"),
    CLOSED("CLOSED");

    private final String id;

    BudgetStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static BudgetStatus fromId(String id) {
        for (BudgetStatus at : BudgetStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
