package com.company.experiments.view.theoreticalmaintenancescenario;

import com.company.experiments.entity.TheoreticalMaintenanceScenario;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "theoretical-maintenance-scenarios", layout = MainView.class)
@ViewController(id = "TheoreticalMaintenanceScenario.list")
@ViewDescriptor(path = "theoretical-maintenance-scenario-list-view.xml")
@LookupComponent("theoreticalMaintenanceScenarioDataGrid")
@DialogMode(width = "64em")
public class TheoreticalMaintenanceScenarioListView extends StandardListView<TheoreticalMaintenanceScenario> {
}
