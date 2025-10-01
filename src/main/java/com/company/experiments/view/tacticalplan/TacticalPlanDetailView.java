package com.company.experiments.view.tacticalplan;

import com.company.experiments.entity.TacticalPlan;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "tactical-plans/:id", layout = MainView.class)
@ViewController(id = "TacticalPlan.detail")
@ViewDescriptor(path = "tactical-plan-detail-view.xml")
@EditedEntityContainer("tacticalPlanDc")
public class TacticalPlanDetailView extends StandardDetailView<TacticalPlan> {
}
