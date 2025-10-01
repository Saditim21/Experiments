package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum EmployeeRole implements EnumClass<String> {

    TECHNICIAN("TECHNICIAN"),
    ENGINEER("ENGINEER"),
    MANAGER("MANAGER"),
    SPECIALIST("SPECIALIST");

    private final String id;

    EmployeeRole(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EmployeeRole fromId(String id) {
        for (EmployeeRole at : EmployeeRole.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
