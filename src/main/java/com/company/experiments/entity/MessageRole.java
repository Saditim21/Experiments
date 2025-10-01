package com.company.experiments.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum MessageRole implements EnumClass<String> {

    USER("USER"),
    ASSISTANT("ASSISTANT"),
    SYSTEM("SYSTEM");

    private final String id;

    MessageRole(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static MessageRole fromId(String id) {
        for (MessageRole at : MessageRole.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
