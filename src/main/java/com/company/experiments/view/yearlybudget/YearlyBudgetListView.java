package com.company.experiments.view.yearlybudget;

import com.company.experiments.entity.YearlyBudget;
import com.company.experiments.view.budgetdashboard.BudgetDashboardView;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "yearlyBudgets", layout = MainView.class)
@ViewController(id = "YearlyBudget.list")
@ViewDescriptor(path = "yearly-budget-list-view.xml")
@LookupComponent("yearlyBudgetsDataGrid")
@DialogMode(width = "64em")
public class YearlyBudgetListView extends StandardListView<YearlyBudget> {

    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private JmixButton dashboardButton;

    @Subscribe("dashboardButton")
    public void onDashboardButtonClick(ClickEvent<JmixButton> event) {
        viewNavigators.view(this, BudgetDashboardView.class).navigate();
    }
}
