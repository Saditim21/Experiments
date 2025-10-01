package com.company.experiments.view.yearlybudgetplanningscenario;

import com.company.experiments.entity.YearlyBudgetPlanningScenario;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "yearly-budget-planning-scenarios/:id", layout = MainView.class)
@ViewController(id = "YearlyBudgetPlanningScenario.detail")
@ViewDescriptor(path = "yearly-budget-planning-scenario-detail-view.xml")
@EditedEntityContainer("yearlyBudgetPlanningScenarioDc")
public class YearlyBudgetPlanningScenarioDetailView extends StandardDetailView<YearlyBudgetPlanningScenario> {
}
