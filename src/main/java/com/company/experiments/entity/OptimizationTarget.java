package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum OptimizationTarget implements EnumClass<String> {

    COST("COST"),
    RISK("RISK"),
    UPTIME("UPTIME"),
    BALANCED("BALANCED");

    private final String id;

    OptimizationTarget(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static OptimizationTarget fromId(String id) {
        for (OptimizationTarget at : OptimizationTarget.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
