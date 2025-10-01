package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "LLM_CHAT_CONTEXT", indexes = {
        @Index(name = "IDX_LLM_CHAT_CONTEXT_TYPE", columnList = "CONTEXT_TYPE"),
        @Index(name = "IDX_LLM_CHAT_CONTEXT_INFRASTRUCTURE", columnList = "INFRASTRUCTURE_NODE_ID"),
        @Index(name = "IDX_LLM_CHAT_CONTEXT_CREATED_AT", columnList = "CREATED_AT")
})
@Entity
public class LlmChatContext {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @NotNull(message = "Context type is required")
    @Column(name = "CONTEXT_TYPE", nullable = false)
    private String contextType;

    @JoinColumn(name = "INFRASTRUCTURE_NODE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private InfrastructureHierarchy infrastructureNode;

    @NotBlank(message = "Context data is required")
    @Column(name = "CONTEXT_DATA", nullable = false, length = 10000)
    private String contextData;

    @Column(name = "EMBEDDINGS", length = 5000)
    private String embeddings;

    @NotNull(message = "Created at is required")
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(String embeddings) {
        this.embeddings = embeddings;
    }

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }

    public InfrastructureHierarchy getInfrastructureNode() {
        return infrastructureNode;
    }

    public void setInfrastructureNode(InfrastructureHierarchy infrastructureNode) {
        this.infrastructureNode = infrastructureNode;
    }

    public ContextType getContextType() {
        return contextType == null ? null : ContextType.fromId(contextType);
    }

    public void setContextType(ContextType contextType) {
        this.contextType = contextType == null ? null : contextType.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
