package com.company.experiments.view.infrastructurehierarchy;

import com.company.experiments.entity.InfrastructureHierarchy;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "infrastructureHierarchies/:id", layout = MainView.class)
@ViewController(id = "InfrastructureHierarchy.detail")
@ViewDescriptor(path = "infrastructure-hierarchy-detail-view.xml")
@EditedEntityContainer("infrastructureHierarchyDc")
public class InfrastructureHierarchyDetailView extends StandardDetailView<InfrastructureHierarchy> {
}
