package com.company.experiments.view.aichat;

import com.company.experiments.entity.*;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Route(value = "aiChat", layout = MainView.class)
@ViewController(id = "AiChatInterface.view")
@ViewDescriptor(path = "ai-chat-interface-view.xml")
public class AiChatInterfaceView extends StandardView {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private Select<LlmChat> chatSelect;

    @ViewComponent
    private Select<Employee> employeeSelect;

    @ViewComponent
    private Div messagesContainer;

    @ViewComponent
    private TextArea messageInput;

    @ViewComponent
    private JmixButton sendButton;

    @ViewComponent
    private JmixButton newChatButton;

    @ViewComponent
    private Select<LlmChatContext> contextSelect;

    @ViewComponent
    private Div attachedContextsContainer;

    private LlmChat currentChat;
    private List<LlmChatMessage> currentMessages = new ArrayList<>();
    private List<LlmChatContext> attachedContexts = new ArrayList<>();

    @Subscribe
    public void onInit(InitEvent event) {
        initializeComponents();
        loadChats();
        loadEmployees();
        loadContexts();
    }

    private void initializeComponents() {
        messageInput.addKeyPressListener(Key.ENTER, e -> {
            if (!e.getModifiers().contains(com.vaadin.flow.component.KeyModifier.SHIFT)) {
                sendMessage();
            }
        });
    }

    private void loadChats() {
        List<LlmChat> chats = dataManager.load(LlmChat.class)
                .query("select e from LlmChat e where e.status = :status order by e.lastMessageAt desc")
                .parameter("status", ChatStatus.ACTIVE.getId())
                .fetchPlan("_instance_name")
                .list();

        chatSelect.setItems(chats);
        chatSelect.setItemLabelGenerator(LlmChat::getChatName);
        chatSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                loadChat(e.getValue());
            }
        });
    }

    private void loadEmployees() {
        List<Employee> employees = dataManager.load(Employee.class)
                .all()
                .fetchPlan("_instance_name")
                .list();

        employeeSelect.setItems(employees);
        employeeSelect.setItemLabelGenerator(Employee::getInstanceName);
    }

    private void loadContexts() {
        List<LlmChatContext> contexts = dataManager.load(LlmChatContext.class)
                .all()
                .fetchPlan("_instance_name")
                .list();

        contextSelect.setItems(contexts);
        contextSelect.setItemLabelGenerator(LlmChatContext::getName);
    }

    private void loadChat(LlmChat chat) {
        currentChat = chat;
        currentMessages.clear();
        attachedContexts.clear();

        List<LlmChatMessage> messages = dataManager.load(LlmChatMessage.class)
                .query("select e from LlmChatMessage e where e.chat.id = :chatId order by e.timestamp asc")
                .parameter("chatId", chat.getId())
                .fetchPlan("llmChatMessage-with-contexts")
                .list();

        currentMessages.addAll(messages);
        renderMessages();
        updateAttachedContextsDisplay();
    }

    private void renderMessages() {
        messagesContainer.removeAll();

        VerticalLayout chatLayout = new VerticalLayout();
        chatLayout.setPadding(true);
        chatLayout.setSpacing(true);
        chatLayout.setWidthFull();

        for (LlmChatMessage message : currentMessages) {
            chatLayout.add(createMessageBubble(message));
        }

        messagesContainer.add(chatLayout);
    }

    private Div createMessageBubble(LlmChatMessage message) {
        MessageRole role = message.getMessageRole();
        boolean isUser = role == MessageRole.USER;

        Div bubble = new Div();
        bubble.getStyle()
                .set("background-color", isUser ? "#E3F2FD" : "#F5F5F5")
                .set("border-radius", "12px")
                .set("padding", "12px 16px")
                .set("margin-bottom", "12px")
                .set("max-width", "70%")
                .set("align-self", isUser ? "flex-end" : "flex-start")
                .set("border-left", "4px solid " + (isUser ? "#2196F3" : "#9E9E9E"));

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);

        Span roleLabel = new Span(role.getId());
        roleLabel.getStyle()
                .set("font-weight", "bold")
                .set("color", isUser ? "#1976D2" : "#616161")
                .set("font-size", "0.9em");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Span timeLabel = new Span(message.getTimestamp().format(formatter));
        timeLabel.getStyle()
                .set("font-size", "0.8em")
                .set("color", "#999");

        header.add(roleLabel, timeLabel);

        Span content = new Span(message.getMessageContent());
        content.getStyle()
                .set("display", "block")
                .set("margin-top", "8px")
                .set("white-space", "pre-wrap")
                .set("word-wrap", "break-word");

        if (message.getTokensUsed() != null && message.getTokensUsed() > 0) {
            Span tokensLabel = new Span("Tokens: " + message.getTokensUsed());
            tokensLabel.getStyle()
                    .set("font-size", "0.75em")
                    .set("color", "#999")
                    .set("display", "block")
                    .set("margin-top", "4px");
            bubble.add(header, content, tokensLabel);
        } else {
            bubble.add(header, content);
        }

        return bubble;
    }

    @Subscribe("sendButton")
    public void onSendButtonClick(ClickEvent<JmixButton> event) {
        sendMessage();
    }

    private void sendMessage() {
        String messageText = messageInput.getValue();
        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        if (currentChat == null) {
            Notification.show("Please select or create a chat first", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Create user message
        LlmChatMessage userMessage = dataManager.create(LlmChatMessage.class);
        userMessage.setChat(currentChat);
        userMessage.setMessageRole(MessageRole.USER);
        userMessage.setMessageContent(messageText);
        userMessage.setTimestamp(LocalDateTime.now());
        userMessage.setAttachedContexts(new ArrayList<>(attachedContexts));
        dataManager.save(userMessage);

        currentMessages.add(userMessage);

        // Simulate AI response
        LlmChatMessage assistantMessage = dataManager.create(LlmChatMessage.class);
        assistantMessage.setChat(currentChat);
        assistantMessage.setMessageRole(MessageRole.ASSISTANT);
        assistantMessage.setMessageContent(generateMockAiResponse(messageText));
        assistantMessage.setTimestamp(LocalDateTime.now());
        assistantMessage.setTokensUsed((int) (Math.random() * 500 + 100));
        dataManager.save(assistantMessage);

        currentMessages.add(assistantMessage);

        // Update chat last message time
        currentChat.setLastMessageAt(LocalDateTime.now());
        dataManager.save(currentChat);

        messageInput.clear();
        renderMessages();
    }

    private String generateMockAiResponse(String userMessage) {
        // Mock AI response - in production, this would call an actual LLM API
        String response = "Thank you for your message: \"" + userMessage + "\"\n\n";
        response += "As an AI maintenance assistant, I can help you with:\n";
        response += "- Infrastructure diagnostics and recommendations\n";
        response += "- Maintenance procedure guidance\n";
        response += "- Alert analysis and troubleshooting\n";
        response += "- Historical maintenance insights\n\n";
        response += "How can I assist you further with your maintenance needs?";
        return response;
    }

    @Subscribe("newChatButton")
    public void onNewChatButtonClick(ClickEvent<JmixButton> event) {
        Employee selectedEmployee = employeeSelect.getValue();
        if (selectedEmployee == null) {
            Notification.show("Please select an employee", 3000, Notification.Position.MIDDLE);
            return;
        }

        LlmChat newChat = dataManager.create(LlmChat.class);
        newChat.setChatId(UUID.randomUUID().toString());
        newChat.setChatName("Chat - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        newChat.setEmployee(selectedEmployee);
        newChat.setStatus(ChatStatus.ACTIVE);
        newChat.setStartedAt(LocalDateTime.now());
        newChat.setLastMessageAt(LocalDateTime.now());
        dataManager.save(newChat);

        loadChats();
        chatSelect.setValue(newChat);
        Notification.show("New chat created", 2000, Notification.Position.BOTTOM_END);
    }

    @Subscribe("attachContextButton")
    public void onAttachContextButtonClick(ClickEvent<JmixButton> event) {
        LlmChatContext selectedContext = contextSelect.getValue();
        if (selectedContext == null) {
            Notification.show("Please select a context to attach", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (!attachedContexts.contains(selectedContext)) {
            attachedContexts.add(selectedContext);
            updateAttachedContextsDisplay();
            Notification.show("Context attached: " + selectedContext.getName(), 2000, Notification.Position.BOTTOM_END);
        } else {
            Notification.show("Context already attached", 2000, Notification.Position.MIDDLE);
        }
    }

    @Subscribe("clearContextsButton")
    public void onClearContextsButtonClick(ClickEvent<JmixButton> event) {
        attachedContexts.clear();
        updateAttachedContextsDisplay();
        Notification.show("All contexts cleared", 2000, Notification.Position.BOTTOM_END);
    }

    private void updateAttachedContextsDisplay() {
        attachedContextsContainer.removeAll();

        if (attachedContexts.isEmpty()) {
            Span emptyLabel = new Span("No contexts attached");
            emptyLabel.getStyle().set("color", "#999").set("font-style", "italic");
            attachedContextsContainer.add(emptyLabel);
            return;
        }

        VerticalLayout contextList = new VerticalLayout();
        contextList.setPadding(false);
        contextList.setSpacing(true);

        for (LlmChatContext context : attachedContexts) {
            HorizontalLayout contextItem = new HorizontalLayout();
            contextItem.setAlignItems(HorizontalLayout.Alignment.CENTER);
            contextItem.setWidthFull();

            Span badge = new Span(context.getContextType().getId().substring(0, 1));
            badge.getStyle()
                    .set("background-color", getContextTypeColor(context.getContextType()))
                    .set("color", "white")
                    .set("width", "24px")
                    .set("height", "24px")
                    .set("border-radius", "50%")
                    .set("display", "inline-flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("font-weight", "bold")
                    .set("font-size", "0.8em");

            Span nameLabel = new Span(context.getName());
            nameLabel.getStyle().set("flex", "1");

            contextItem.add(badge, nameLabel);
            contextList.add(contextItem);
        }

        attachedContextsContainer.add(contextList);
    }

    private String getContextTypeColor(ContextType type) {
        return switch (type) {
            case INFRASTRUCTURE -> "#2196F3";
            case MAINTENANCE_HISTORY -> "#4CAF50";
            case PROCEDURES -> "#FF9800";
            case ALERTS -> "#F44336";
        };
    }
}
