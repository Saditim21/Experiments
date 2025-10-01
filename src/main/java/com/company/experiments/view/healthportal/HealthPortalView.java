package com.company.experiments.view.healthportal;

import com.company.experiments.entity.*;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "healthPortal", layout = MainView.class)
@ViewController(id = "HealthPortal.view")
@ViewDescriptor(path = "health-portal-view.xml")
public class HealthPortalView extends StandardView {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private DataGrid<HealthAlert> alertsDataGrid;

    @ViewComponent
    private CollectionContainer<HealthAlert> healthAlertsDc;

    @ViewComponent
    private Select<AlertStatus> statusFilter;

    @ViewComponent
    private Select<AlertSeverity> severityFilter;

    @ViewComponent
    private Div infrastructureTreeContainer;

    @ViewComponent
    private Span criticalCountLabel;

    @ViewComponent
    private Span highCountLabel;

    @ViewComponent
    private Span mediumCountLabel;

    @ViewComponent
    private Span openAlertsCountLabel;

    @ViewComponent
    private JmixButton bulkAssignButton;

    @ViewComponent
    private Select<Employee> employeeSelect;

    private List<InfrastructureHierarchy> allInfrastructureNodes;

    @Subscribe
    public void onInit(InitEvent event) {
        initFilters();
        loadData();
    }

    private void initFilters() {
        statusFilter.setItems(AlertStatus.values());
        statusFilter.setItemLabelGenerator(AlertStatus::getId);
        statusFilter.addValueChangeListener(e -> filterAlerts());

        severityFilter.setItems(AlertSeverity.values());
        severityFilter.setItemLabelGenerator(AlertSeverity::getId);
        severityFilter.addValueChangeListener(e -> filterAlerts());

        List<Employee> employees = dataManager.load(Employee.class)
                .all()
                .fetchPlan("_instance_name")
                .list();
        employeeSelect.setItems(employees);
        employeeSelect.setItemLabelGenerator(Employee::getInstanceName);

        alertsDataGrid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private void loadData() {
        List<HealthAlert> alerts = dataManager.load(HealthAlert.class)
                .query("select e from HealthAlert e order by e.detectedAt desc")
                .fetchPlan("healthAlert-portal-view")
                .list();

        healthAlertsDc.setItems(alerts);
        updateSummaryCards(alerts);
        loadInfrastructureTree(alerts);
    }

    private void filterAlerts() {
        AlertStatus selectedStatus = statusFilter.getValue();
        AlertSeverity selectedSeverity = severityFilter.getValue();

        List<HealthAlert> allAlerts = dataManager.load(HealthAlert.class)
                .query("select e from HealthAlert e order by e.detectedAt desc")
                .fetchPlan("healthAlert-portal-view")
                .list();

        List<HealthAlert> filtered = allAlerts.stream()
                .filter(alert -> selectedStatus == null || alert.getStatus() == selectedStatus)
                .filter(alert -> selectedSeverity == null || alert.getSeverity() == selectedSeverity)
                .collect(Collectors.toList());

        healthAlertsDc.setItems(filtered);
        updateSummaryCards(filtered);
    }

    private void updateSummaryCards(List<HealthAlert> alerts) {
        long critical = alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.CRITICAL).count();
        long high = alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.HIGH).count();
        long medium = alerts.stream().filter(a -> a.getSeverity() == AlertSeverity.MEDIUM).count();
        long open = alerts.stream().filter(HealthAlert::isOpen).count();

        criticalCountLabel.setText(String.valueOf(critical));
        highCountLabel.setText(String.valueOf(high));
        mediumCountLabel.setText(String.valueOf(medium));
        openAlertsCountLabel.setText(String.valueOf(open));
    }

    private void loadInfrastructureTree(List<HealthAlert> alerts) {
        infrastructureTreeContainer.removeAll();

        List<InfrastructureHierarchy> infrastructureNodes = dataManager.load(InfrastructureHierarchy.class)
                .all()
                .fetchPlan("_instance_name")
                .list();

        allInfrastructureNodes = infrastructureNodes;

        // Map infrastructure nodes to their alert counts and max severity
        Map<UUID, List<HealthAlert>> alertsByNode = alerts.stream()
                .filter(HealthAlert::isOpen)
                .collect(Collectors.groupingBy(a -> a.getInfrastructureNode().getId()));

        TreeGrid<InfrastructureHierarchy> treeGrid = new TreeGrid<>();
        treeGrid.setWidthFull();
        treeGrid.setHeight("500px");

        TreeData<InfrastructureHierarchy> treeData = new TreeData<>();

        // Build tree structure
        Map<UUID, InfrastructureHierarchy> nodeMap = infrastructureNodes.stream()
                .collect(Collectors.toMap(InfrastructureHierarchy::getId, n -> n));

        for (InfrastructureHierarchy node : infrastructureNodes) {
            if (node.getParentNode() == null) {
                treeData.addItem(null, node);
            } else {
                InfrastructureHierarchy parent = nodeMap.get(node.getParentNode().getId());
                if (parent != null) {
                    treeData.addItem(parent, node);
                }
            }
        }

        treeGrid.setDataProvider(new TreeDataProvider<>(treeData));

        treeGrid.addComponentHierarchyColumn(node -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
            layout.setSpacing(true);

            Span nameSpan = new Span(node.getName());

            List<HealthAlert> nodeAlerts = alertsByNode.getOrDefault(node.getId(), Collections.emptyList());

            if (!nodeAlerts.isEmpty()) {
                AlertSeverity maxSeverity = nodeAlerts.stream()
                        .map(HealthAlert::getSeverity)
                        .max(Comparator.comparingInt(this::getSeverityPriority))
                        .orElse(AlertSeverity.LOW);

                Span statusBadge = new Span(String.valueOf(nodeAlerts.size()));
                statusBadge.getStyle()
                        .set("background-color", getSeverityColor(maxSeverity))
                        .set("color", "white")
                        .set("padding", "2px 8px")
                        .set("border-radius", "12px")
                        .set("font-size", "0.85em")
                        .set("font-weight", "bold");
                statusBadge.setTitle(maxSeverity.getId() + " - " + nodeAlerts.size() + " alerts");

                layout.add(nameSpan, statusBadge);
            } else {
                Span healthyBadge = new Span("✓");
                healthyBadge.getStyle()
                        .set("color", "#4CAF50")
                        .set("font-weight", "bold")
                        .set("font-size", "1.2em");
                layout.add(nameSpan, healthyBadge);
            }

            return layout;
        }).setHeader("Infrastructure Node").setAutoWidth(true);

        treeGrid.addColumn(node -> {
            List<HealthAlert> nodeAlerts = alertsByNode.getOrDefault(node.getId(), Collections.emptyList());
            return nodeAlerts.isEmpty() ? "Healthy" : "Attention Required";
        }).setHeader("Status").setAutoWidth(true);

        infrastructureTreeContainer.add(treeGrid);
    }

    private String getSeverityColor(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "#D32F2F";
            case HIGH -> "#F57C00";
            case MEDIUM -> "#FBC02D";
            case LOW -> "#388E3C";
        };
    }

    private int getSeverityPriority(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    @Subscribe("bulkAssignButton")
    public void onBulkAssignButtonClick(ClickEvent<JmixButton> event) {
        Employee selectedEmployee = employeeSelect.getValue();
        Set<HealthAlert> selectedAlerts = alertsDataGrid.getSelectedItems();

        if (selectedEmployee == null) {
            Notification.show("Please select an employee", 3000, Notification.Position.MIDDLE);
            return;
        }

        if (selectedAlerts.isEmpty()) {
            Notification.show("Please select at least one alert", 3000, Notification.Position.MIDDLE);
            return;
        }

        for (HealthAlert alert : selectedAlerts) {
            alert.setAssignedTo(selectedEmployee);
            if (alert.getStatus() == AlertStatus.NEW) {
                alert.setStatus(AlertStatus.INVESTIGATING);
            }
            dataManager.save(alert);
        }

        Notification.show(String.format("Assigned %d alerts to %s",
                selectedAlerts.size(), selectedEmployee.getInstanceName()),
                3000, Notification.Position.MIDDLE);

        loadData();
        alertsDataGrid.deselectAll();
        employeeSelect.clear();
    }

    @Subscribe("refreshButton")
    public void onRefreshButtonClick(ClickEvent<JmixButton> event) {
        loadData();
        Notification.show("Data refreshed", 2000, Notification.Position.BOTTOM_END);
    }
}
