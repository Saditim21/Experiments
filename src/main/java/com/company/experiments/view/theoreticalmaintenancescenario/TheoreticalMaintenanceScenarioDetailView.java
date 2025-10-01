package com.company.experiments.view.theoreticalmaintenancescenario;

import com.company.experiments.entity.TheoreticalMaintenanceScenario;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "theoretical-maintenance-scenarios/:id", layout = MainView.class)
@ViewController(id = "TheoreticalMaintenanceScenario.detail")
@ViewDescriptor(path = "theoretical-maintenance-scenario-detail-view.xml")
@EditedEntityContainer("theoreticalMaintenanceScenarioDc")
public class TheoreticalMaintenanceScenarioDetailView extends StandardDetailView<TheoreticalMaintenanceScenario> {
}
