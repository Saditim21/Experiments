package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ContextType implements EnumClass<String> {

    INFRASTRUCTURE("INFRASTRUCTURE"),
    MAINTENANCE_HISTORY("MAINTENANCE_HISTORY"),
    PROCEDURES("PROCEDURES"),
    ALERTS("ALERTS");

    private final String id;

    ContextType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ContextType fromId(String id) {
        for (ContextType at : ContextType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
