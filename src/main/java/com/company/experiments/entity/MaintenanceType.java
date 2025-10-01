package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum MaintenanceType implements EnumClass<String> {

    PREVENTIVE("PREVENTIVE"),
    PREDICTIVE("PREDICTIVE"),
    CORRECTIVE("CORRECTIVE"),
    SHUTDOWN("SHUTDOWN");

    private final String id;

    MaintenanceType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static MaintenanceType fromId(String id) {
        for (MaintenanceType at : MaintenanceType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
