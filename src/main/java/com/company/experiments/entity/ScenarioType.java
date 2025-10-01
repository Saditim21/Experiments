package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ScenarioType implements EnumClass<String> {

    MAINTENANCE_TRIGGER("MAINTENANCE_TRIGGER"),
    RESOURCE_ALLOCATION("RESOURCE_ALLOCATION"),
    BUDGET_CONSTRAINT("BUDGET_CONSTRAINT"),
    RISK_ASSESSMENT("RISK_ASSESSMENT");

    private final String id;

    ScenarioType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ScenarioType fromId(String id) {
        for (ScenarioType at : ScenarioType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
