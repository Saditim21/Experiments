package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum MetricType implements EnumClass<String> {

    COST("COST"),
    UPTIME("UPTIME"),
    RISK("RISK"),
    EFFICIENCY("EFFICIENCY");

    private final String id;

    MetricType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static MetricType fromId(String id) {
        for (MetricType at : MetricType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
