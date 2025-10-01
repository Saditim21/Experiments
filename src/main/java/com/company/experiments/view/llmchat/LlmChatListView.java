package com.company.experiments.view.llmchat;

import com.company.experiments.entity.LlmChat;
import com.company.experiments.view.aichat.AiChatInterfaceView;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "llmChats", layout = MainView.class)
@ViewController(id = "LlmChat.list")
@ViewDescriptor(path = "llm-chat-list-view.xml")
@LookupComponent("llmChatsDataGrid")
@DialogMode(width = "64em")
public class LlmChatListView extends StandardListView<LlmChat> {

    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private JmixButton openChatButton;

    @Subscribe("openChatButton")
    public void onOpenChatButtonClick(ClickEvent<JmixButton> event) {
        viewNavigators.view(this, AiChatInterfaceView.class).navigate();
    }
}
