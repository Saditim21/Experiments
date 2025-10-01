package com.company.experiments.view.yearlybudgetplanningscenario;

import com.company.experiments.entity.YearlyBudgetPlanningScenario;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "yearly-budget-planning-scenarios", layout = MainView.class)
@ViewController(id = "YearlyBudgetPlanningScenario.list")
@ViewDescriptor(path = "yearly-budget-planning-scenario-list-view.xml")
@LookupComponent("yearlyBudgetPlanningScenarioDataGrid")
@DialogMode(width = "64em")
public class YearlyBudgetPlanningScenarioListView extends StandardListView<YearlyBudgetPlanningScenario> {
}
