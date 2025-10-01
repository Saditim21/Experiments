package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum AlertType implements EnumClass<String> {

    ANOMALY("ANOMALY"),
    THRESHOLD_BREACH("THRESHOLD_BREACH"),
    PREDICTION("PREDICTION"),
    MANUAL("MANUAL");

    private final String id;

    AlertType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AlertType fromId(String id) {
        for (AlertType at : AlertType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
