package com.company.experiments.view.infrastructurelevel;

import com.company.experiments.entity.InfrastructureLevel;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "infrastructureLevels/:id", layout = MainView.class)
@ViewController(id = "InfrastructureLevel.detail")
@ViewDescriptor(path = "infrastructure-level-detail-view.xml")
@EditedEntityContainer("infrastructureLevelDc")
public class InfrastructureLevelDetailView extends StandardDetailView<InfrastructureLevel> {
}
