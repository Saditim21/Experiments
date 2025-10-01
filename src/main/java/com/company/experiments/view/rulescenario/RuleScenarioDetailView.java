package com.company.experiments.view.rulescenario;

import com.company.experiments.entity.RuleScenario;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "ruleScenarios/:id", layout = MainView.class)
@ViewController(id = "RuleScenario.detail")
@ViewDescriptor(path = "rule-scenario-detail-view.xml")
@EditedEntityContainer("ruleScenarioDc")
public class RuleScenarioDetailView extends StandardDetailView<RuleScenario> {
}
