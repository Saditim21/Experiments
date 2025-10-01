package com.company.experiments.view.tacticalplan;

import com.company.experiments.entity.TacticalPlan;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import io.jmix.flowui.ViewNavigators;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "tactical-plans", layout = MainView.class)
@ViewController(id = "TacticalPlan.list")
@ViewDescriptor(path = "tactical-plan-list-view.xml")
@LookupComponent("tacticalPlanDataGrid")
@DialogMode(width = "64em")
public class TacticalPlanListView extends StandardListView<TacticalPlan> {

    @Autowired
    private ViewNavigators viewNavigators;

    @Subscribe("ganttViewButton")
    public void onGanttViewButtonClick(final ClickEvent<com.vaadin.flow.component.button.Button> event) {
        viewNavigators.view(this, TacticalPlanGanttView.class).navigate();
    }
}
