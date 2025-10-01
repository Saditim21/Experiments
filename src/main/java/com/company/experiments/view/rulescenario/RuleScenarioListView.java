package com.company.experiments.view.rulescenario;

import com.company.experiments.entity.RuleScenario;
import com.company.experiments.view.main.MainView;
import com.company.experiments.view.rulebuilder.RuleBuilderView;
import com.company.experiments.view.ruleexecutionlog.RuleExecutionLogListView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "ruleScenarios", layout = MainView.class)
@ViewController(id = "RuleScenario.list")
@ViewDescriptor(path = "rule-scenario-list-view.xml")
@LookupComponent("ruleScenariosDataGrid")
@DialogMode(width = "64em")
public class RuleScenarioListView extends StandardListView<RuleScenario> {

    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private JmixButton ruleBuilderButton;

    @ViewComponent
    private JmixButton executionLogsButton;

    @Subscribe("ruleBuilderButton")
    public void onRuleBuilderButtonClick(ClickEvent<JmixButton> event) {
        viewNavigators.view(this, RuleBuilderView.class).navigate();
    }

    @Subscribe("executionLogsButton")
    public void onExecutionLogsButtonClick(ClickEvent<JmixButton> event) {
        viewNavigators.view(this, RuleExecutionLogListView.class).navigate();
    }
}
