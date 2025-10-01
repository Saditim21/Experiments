package com.company.experiments.view.scenariocomparison;

import com.company.experiments.entity.ScenarioComparison;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "scenario-comparisons", layout = MainView.class)
@ViewController(id = "ScenarioComparison.list")
@ViewDescriptor(path = "scenario-comparison-list-view.xml")
@LookupComponent("scenarioComparisonDataGrid")
@DialogMode(width = "64em")
public class ScenarioComparisonListView extends StandardListView<ScenarioComparison> {

    @Autowired
    private ViewNavigators viewNavigators;

    @Subscribe("dashboardButton")
    public void onDashboardButtonClick(final ClickEvent<JmixButton> event) {
        ScenarioComparison selectedComparison = (ScenarioComparison) getViewData()
                .getContainer("scenarioComparisonDc")
                .getItemOrNull();

        if (selectedComparison != null) {
            viewNavigators.detailView(this, ScenarioComparison.class)
                    .editEntity(selectedComparison)
                    .withViewClass(ScenarioComparisonDashboardView.class)
                    .navigate();
        }
    }
}
