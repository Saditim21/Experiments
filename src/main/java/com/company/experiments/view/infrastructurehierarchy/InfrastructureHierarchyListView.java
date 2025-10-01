package com.company.experiments.view.infrastructurehierarchy;

import com.company.experiments.entity.InfrastructureHierarchy;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "infrastructureHierarchies", layout = MainView.class)
@ViewController(id = "InfrastructureHierarchy.list")
@ViewDescriptor(path = "infrastructure-hierarchy-list-view.xml")
@LookupComponent("infrastructureHierarchiesTreeDataGrid")
@DialogMode(width = "64em")
public class InfrastructureHierarchyListView extends StandardListView<InfrastructureHierarchy> {
}
