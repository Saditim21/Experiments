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
@Table(name = "LLM_CHAT", indexes = {
        @Index(name = "IDX_LLM_CHAT_ID", columnList = "CHAT_ID", unique = true),
        @Index(name = "IDX_LLM_CHAT_EMPLOYEE", columnList = "EMPLOYEE_ID"),
        @Index(name = "IDX_LLM_CHAT_STATUS", columnList = "STATUS"),
        @Index(name = "IDX_LLM_CHAT_STARTED_AT", columnList = "STARTED_AT")
})
@Entity
public class LlmChat {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotBlank(message = "Chat ID is required")
    @Column(name = "CHAT_ID", nullable = false, unique = true, length = 50)
    private String chatId;

    @InstanceName
    @NotBlank(message = "Chat name is required")
    @Size(min = 2, max = 200, message = "Chat name must be between 2 and 200 characters")
    @Column(name = "CHAT_NAME", nullable = false, length = 200)
    private String chatName;

    @NotNull(message = "Employee is required")
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    @JoinColumn(name = "RELATED_ALERT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private HealthAlert relatedAlert;

    @JoinColumn(name = "RELATED_INFRASTRUCTURE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private InfrastructureHierarchy relatedInfrastructure;

    @NotNull(message = "Started at is required")
    @Column(name = "STARTED_AT", nullable = false)
    private LocalDateTime startedAt;

    @NotNull(message = "Last message at is required")
    @Column(name = "LAST_MESSAGE_AT", nullable = false)
    private LocalDateTime lastMessageAt;

    @NotNull(message = "Status is required")
    @Column(name = "STATUS", nullable = false)
    private String status;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public ChatStatus getStatus() {
        return status == null ? null : ChatStatus.fromId(status);
    }

    public void setStatus(ChatStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public InfrastructureHierarchy getRelatedInfrastructure() {
        return relatedInfrastructure;
    }

    public void setRelatedInfrastructure(InfrastructureHierarchy relatedInfrastructure) {
        this.relatedInfrastructure = relatedInfrastructure;
    }

    public HealthAlert getRelatedAlert() {
        return relatedAlert;
    }

    public void setRelatedAlert(HealthAlert relatedAlert) {
        this.relatedAlert = relatedAlert;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
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
        if (chatId == null) {
            chatId = UUID.randomUUID().toString();
        }
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (lastMessageAt == null) {
            lastMessageAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ChatStatus.ACTIVE.getId();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastMessageAt = LocalDateTime.now();
    }
}
