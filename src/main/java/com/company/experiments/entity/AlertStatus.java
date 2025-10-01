package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum AlertStatus implements EnumClass<String> {

    NEW("NEW"),
    INVESTIGATING("INVESTIGATING"),
    RESOLVED("RESOLVED"),
    FALSE_POSITIVE("FALSE_POSITIVE");

    private final String id;

    AlertStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AlertStatus fromId(String id) {
        for (AlertStatus at : AlertStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
