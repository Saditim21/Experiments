package com.company.experiments.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "LLM_CHAT_MESSAGE", indexes = {
        @Index(name = "IDX_LLM_CHAT_MESSAGE_CHAT", columnList = "CHAT_ID"),
        @Index(name = "IDX_LLM_CHAT_MESSAGE_TIMESTAMP", columnList = "TIMESTAMP")
})
@Entity
public class LlmChatMessage {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @NotNull(message = "Chat is required")
    @JoinColumn(name = "CHAT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private LlmChat chat;

    @NotNull(message = "Message role is required")
    @Column(name = "MESSAGE_ROLE", nullable = false)
    private String messageRole;

    @NotBlank(message = "Message content is required")
    @Column(name = "MESSAGE_CONTENT", nullable = false, length = 10000)
    private String messageContent;

    @NotNull(message = "Timestamp is required")
    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp;

    @PositiveOrZero(message = "Tokens used must be zero or positive")
    @Column(name = "TOKENS_USED")
    private Integer tokensUsed;

    @JoinTable(name = "LLM_CHAT_MESSAGE_CONTEXT",
            joinColumns = @JoinColumn(name = "MESSAGE_ID"),
            inverseJoinColumns = @JoinColumn(name = "CONTEXT_ID"))
    @ManyToMany
    private List<LlmChatContext> attachedContexts;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public List<LlmChatContext> getAttachedContexts() {
        return attachedContexts;
    }

    public void setAttachedContexts(List<LlmChatContext> attachedContexts) {
        this.attachedContexts = attachedContexts;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public MessageRole getMessageRole() {
        return messageRole == null ? null : MessageRole.fromId(messageRole);
    }

    public void setMessageRole(MessageRole messageRole) {
        this.messageRole = messageRole == null ? null : messageRole.getId();
    }

    public LlmChat getChat() {
        return chat;
    }

    public void setChat(LlmChat chat) {
        this.chat = chat;
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

    @InstanceName
    public String getInstanceName() {
        return String.format("%s - %s",
                messageRole != null ? MessageRole.fromId(messageRole).getId() : "",
                timestamp != null ? timestamp.toString() : "");
    }

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
