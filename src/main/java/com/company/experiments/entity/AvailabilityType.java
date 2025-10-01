package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum AvailabilityType implements EnumClass<String> {

    AVAILABLE("AVAILABLE"),
    VACATION("VACATION"),
    SICK("SICK"),
    TRAINING("TRAINING");

    private final String id;

    AvailabilityType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AvailabilityType fromId(String id) {
        for (AvailabilityType at : AvailabilityType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
