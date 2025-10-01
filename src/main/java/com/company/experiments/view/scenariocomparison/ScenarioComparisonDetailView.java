package com.company.experiments.view.scenariocomparison;

import com.company.experiments.entity.ScenarioComparison;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "scenario-comparisons/:id", layout = MainView.class)
@ViewController(id = "ScenarioComparison.detail")
@ViewDescriptor(path = "scenario-comparison-detail-view.xml")
@EditedEntityContainer("scenarioComparisonDc")
public class ScenarioComparisonDetailView extends StandardDetailView<ScenarioComparison> {
}
