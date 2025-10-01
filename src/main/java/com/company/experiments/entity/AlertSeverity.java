package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum AlertSeverity implements EnumClass<String> {

    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    CRITICAL("CRITICAL");

    private final String id;

    AlertSeverity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AlertSeverity fromId(String id) {
        for (AlertSeverity at : AlertSeverity.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
