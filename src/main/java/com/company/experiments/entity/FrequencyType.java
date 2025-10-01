package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum FrequencyType implements EnumClass<String> {

    DAILY("DAILY"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    QUARTERLY("QUARTERLY"),
    YEARLY("YEARLY"),
    ON_CONDITION("ON_CONDITION");

    private final String id;

    FrequencyType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static FrequencyType fromId(String id) {
        for (FrequencyType at : FrequencyType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
