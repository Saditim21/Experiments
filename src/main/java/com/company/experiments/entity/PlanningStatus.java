package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum PlanningStatus implements EnumClass<String> {

    DRAFT("DRAFT"),
    UNDER_REVIEW("UNDER_REVIEW"),
    APPROVED("APPROVED"),
    ACTIVE("ACTIVE");

    private final String id;

    PlanningStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static PlanningStatus fromId(String id) {
        for (PlanningStatus at : PlanningStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
