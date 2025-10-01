package com.company.experiments.view.infrastructurelevel;

import com.company.experiments.entity.InfrastructureLevel;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "infrastructureLevels", layout = MainView.class)
@ViewController(id = "InfrastructureLevel.list")
@ViewDescriptor(path = "infrastructure-level-list-view.xml")
@LookupComponent("infrastructureLevelsDataGrid")
@DialogMode(width = "64em")
public class InfrastructureLevelListView extends StandardListView<InfrastructureLevel> {
}
