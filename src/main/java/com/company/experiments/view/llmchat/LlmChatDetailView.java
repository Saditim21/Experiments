package com.company.experiments.view.llmchat;

import com.company.experiments.entity.LlmChat;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "llmChats/:id", layout = MainView.class)
@ViewController(id = "LlmChat.detail")
@ViewDescriptor(path = "llm-chat-detail-view.xml")
@EditedEntityContainer("llmChatDc")
public class LlmChatDetailView extends StandardDetailView<LlmChat> {
}
