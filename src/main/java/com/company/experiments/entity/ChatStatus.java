package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum ChatStatus implements EnumClass<String> {

    ACTIVE("ACTIVE"),
    ARCHIVED("ARCHIVED");

    private final String id;

    ChatStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ChatStatus fromId(String id) {
        for (ChatStatus at : ChatStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
