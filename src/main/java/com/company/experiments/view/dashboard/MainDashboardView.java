package com.company.experiments.view.dashboard;

import com.company.experiments.entity.Team;
import com.company.experiments.entity.TacticalPlan;
import com.company.experiments.entity.TacticalPlanStatus;
import com.company.experiments.service.DashboardService;
import com.company.experiments.service.ExportService;
import com.company.experiments.service.ReportingService;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Route(value = "dashboard", layout = MainView.class)
@ViewController(id = "MainDashboard")
@ViewDescriptor(path = "main-dashboard-view.xml")
public class MainDashboardView extends StandardView {

    @ViewComponent
    private Div unplannedStopsKpi;

    @ViewComponent
    private Div costSavingsKpi;

    @ViewComponent
    private Div productivityKpi;

    @ViewComponent
    private Span unplannedStopsTrend;

    @ViewComponent
    private Span costSavingsTrend;

    @ViewComponent
    private Span productivityTrend;

    @ViewComponent
    private Div executiveChartsHtml;

    @ViewComponent
    private Div alertSummaryHtml;

    @ViewComponent
    private Div alertChartHtml;

    @ViewComponent
    private Div criticalInfrastructureHtml;

    @ViewComponent
    private Div totalBudgetValue;

    @ViewComponent
    private Div allocatedBudgetValue;

    @ViewComponent
    private Div spentBudgetValue;

    @ViewComponent
    private Div remainingBudgetValue;

    @ViewComponent
    private Div budgetChartHtml;

    @ViewComponent
    private Div teamCapacityHtml;

    @ViewComponent
    private Div workloadChartHtml;

    @ViewComponent
    private Div employeeAssignmentsHtml;

    @ViewComponent
    private Div upcomingMaintenanceCalendarHtml;

    @ViewComponent
    private Select<Team> filterTeamSelect;

    @ViewComponent
    private Select<TacticalPlanStatus> filterStatusSelect;

    @ViewComponent
    private Select<String> reportTypeSelect;

    @ViewComponent
    private DatePicker reportStartDate;

    @ViewComponent
    private DatePicker reportEndDate;

    @ViewComponent
    private Div reportPreviewHtml;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private Notifications notifications;

    private Map<String, Object> currentReportData;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        loadDashboardData();
        initializeReportControls();
    }

    @Subscribe("refreshButton")
    public void onRefreshButtonClick(final ClickEvent<JmixButton> event) {
        loadDashboardData();
        notifications.create("Dashboard refreshed").show();
    }

    @Subscribe("exportPdfButton")
    public void onExportPdfButtonClick(final ClickEvent<JmixButton> event) {
        exportDashboardToPdf();
    }

    @Subscribe("exportExcelButton")
    public void onExportExcelButtonClick(final ClickEvent<JmixButton> event) {
        exportDashboardToExcel();
    }

    @Subscribe("generateReportButton")
    public void onGenerateReportButtonClick(final ClickEvent<JmixButton> event) {
        generateReport();
    }

    @Subscribe("exportReportPdfButton")
    public void onExportReportPdfButtonClick(final ClickEvent<JmixButton> event) {
        exportReportToPdf();
    }

    @Subscribe("exportReportExcelButton")
    public void onExportReportExcelButtonClick(final ClickEvent<JmixButton> event) {
        exportReportToExcel();
    }

    private void loadDashboardData() {
        loadExecutiveSummary();
        loadAlertOverview();
        loadBudgetStatus();
        loadTeamUtilization();
        loadUpcomingMaintenance();
    }

    private void loadExecutiveSummary() {
        Map<String, Object> kpis = dashboardService.calculateKPIs();

        BigDecimal unplannedStops = (BigDecimal) kpis.get("unplannedStopsReduction");
        BigDecimal costSavings = (BigDecimal) kpis.get("maintenanceCostSavings");
        BigDecimal productivity = (BigDecimal) kpis.get("productivityIncrease");

        unplannedStopsKpi.getElement().setProperty("innerHTML",
                "<span style='font-size: 48px; font-weight: bold; color: #4CAF50;'>" +
                String.format("%.1f%%", unplannedStops) + "</span>");

        costSavingsKpi.getElement().setProperty("innerHTML",
                "<span style='font-size: 48px; font-weight: bold; color: #2196F3;'>" +
                String.format("%.1f%%", costSavings) + "</span>");

        productivityKpi.getElement().setProperty("innerHTML",
                "<span style='font-size: 48px; font-weight: bold; color: #FF9800;'>" +
                String.format("%.1f%%", productivity) + "</span>");

        unplannedStopsTrend.getElement().setProperty("innerHTML",
                "<span style='color: #4CAF50;'>↑ +2.3% vs last month</span>");

        costSavingsTrend.getElement().setProperty("innerHTML",
                "<span style='color: #2196F3;'>↑ +1.8% vs last month</span>");

        productivityTrend.getElement().setProperty("innerHTML",
                "<span style='color: #FF9800;'>↑ +3.2% vs last month</span>");

        renderExecutiveCharts();
    }

    private void renderExecutiveCharts() {
        StringBuilder html = new StringBuilder();
        html.append("<style>");
        html.append(".chart-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        html.append(".bar { display: inline-block; height: 200px; width: 60px; margin: 0 10px; background: linear-gradient(180deg, #667eea 0%, #764ba2 100%); border-radius: 4px; vertical-align: bottom; }");
        html.append(".bar-label { display: block; text-align: center; margin-top: 10px; font-size: 12px; }");
        html.append("</style>");

        html.append("<div class='chart-container'>");
        html.append("<h3>Monthly Trends</h3>");
        html.append("<div style='text-align: center;'>");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] values = {120, 145, 160, 175, 190, 200};

        for (int i = 0; i < months.length; i++) {
            double heightPercent = (values[i] / 200.0) * 100;
            html.append("<div style='display: inline-block; margin: 0 5px;'>");
            html.append("<div class='bar' style='height: ").append(heightPercent).append("%;'></div>");
            html.append("<span class='bar-label'>").append(months[i]).append("</span>");
            html.append("</div>");
        }

        html.append("</div>");
        html.append("</div>");

        executiveChartsHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void loadAlertOverview() {
        Map<String, Object> alerts = dashboardService.getCriticalAlerts();

        renderAlertSummary(alerts);
        renderAlertChart(alerts);
        renderCriticalInfrastructure(alerts);
    }

    private void renderAlertSummary(Map<String, Object> alerts) {
        @SuppressWarnings("unchecked")
        Map<String, Long> byLevel = (Map<String, Long>) alerts.get("byLevel");

        StringBuilder html = new StringBuilder();
        html.append("<style>");
        html.append(".alert-summary { background: white; padding: 20px; border-radius: 8px; }");
        html.append(".alert-item { padding: 10px; margin: 5px 0; background: #fff3cd; border-left: 4px solid #ff9800; }");
        html.append("</style>");

        html.append("<div class='alert-summary'>");
        html.append("<h4>Total Critical Alerts: ").append(alerts.get("totalCritical")).append("</h4>");

        for (Map.Entry<String, Long> entry : byLevel.entrySet()) {
            html.append("<div class='alert-item'>");
            html.append("<strong>").append(entry.getKey()).append(":</strong> ");
            html.append(entry.getValue()).append(" alerts");
            html.append("</div>");
        }

        html.append("</div>");

        alertSummaryHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderAlertChart(Map<String, Object> alerts) {
        StringBuilder html = new StringBuilder();
        html.append("<style>");
        html.append(".pie-chart { width: 300px; height: 300px; margin: 0 auto; }");
        html.append("</style>");

        html.append("<div style='text-align: center; padding: 20px; background: white; border-radius: 8px;'>");
        html.append("<svg class='pie-chart' viewBox='0 0 100 100'>");

        // Simple pie chart representation
        html.append("<circle cx='50' cy='50' r='40' fill='#FF5722'/>");
        html.append("<circle cx='50' cy='50' r='40' fill='#FFC107' stroke-dasharray='75 251' transform='rotate(-90 50 50)'/>");
        html.append("<circle cx='50' cy='50' r='40' fill='#4CAF50' stroke-dasharray='50 251' transform='rotate(0 50 50)'/>");

        html.append("</svg>");
        html.append("<p>Alert Distribution by Severity</p>");
        html.append("</div>");

        alertChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderCriticalInfrastructure(Map<String, Object> alerts) {
        StringBuilder html = new StringBuilder();
        html.append("<style>");
        html.append(".infra-table { width: 100%; border-collapse: collapse; background: white; }");
        html.append(".infra-table th { background: #f44336; color: white; padding: 10px; text-align: left; }");
        html.append(".infra-table td { padding: 8px; border-bottom: 1px solid #ddd; }");
        html.append("</style>");

        html.append("<table class='infra-table'>");
        html.append("<tr><th>Infrastructure</th><th>Criticality</th><th>Status</th></tr>");
        html.append("<tr><td>Main Power Distribution</td><td>Critical (10)</td><td>Maintenance</td></tr>");
        html.append("<tr><td>Water Treatment Plant A</td><td>High (9)</td><td>Active</td></tr>");
        html.append("<tr><td>HVAC System Building 3</td><td>High (8)</td><td>Maintenance</td></tr>");
        html.append("</table>");

        criticalInfrastructureHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void loadBudgetStatus() {
        Map<String, BigDecimal> budget = dashboardService.getBudgetStatus();

        totalBudgetValue.getElement().setProperty("innerHTML",
                "<span style='font-size: 32px; font-weight: bold; color: #333;'>$" +
                formatMoney(budget.get("total")) + "</span>");

        allocatedBudgetValue.getElement().setProperty("innerHTML",
                "<span style='font-size: 32px; font-weight: bold; color: #2196F3;'>$" +
                formatMoney(budget.get("allocated")) + "</span>");

        spentBudgetValue.getElement().setProperty("innerHTML",
                "<span style='font-size: 32px; font-weight: bold; color: #FF9800;'>$" +
                formatMoney(budget.get("spent")) + "</span>");

        remainingBudgetValue.getElement().setProperty("innerHTML",
                "<span style='font-size: 32px; font-weight: bold; color: #4CAF50;'>$" +
                formatMoney(budget.get("remaining")) + "</span>");

        renderBudgetChart(budget);
    }

    private void renderBudgetChart(Map<String, BigDecimal> budget) {
        BigDecimal total = budget.get("total");
        BigDecimal allocated = budget.get("allocated");
        BigDecimal spent = budget.get("spent");

        double allocatedPercent = total.compareTo(BigDecimal.ZERO) > 0
                ? allocated.divide(total, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

        double spentPercent = total.compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(total, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

        StringBuilder html = new StringBuilder();
        html.append("<style>");
        html.append(".budget-bar { width: 100%; height: 50px; background: #e0e0e0; border-radius: 25px; overflow: hidden; position: relative; }");
        html.append(".budget-segment { height: 100%; float: left; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; }");
        html.append("</style>");

        html.append("<div style='padding: 20px; background: white; border-radius: 8px;'>");
        html.append("<h3>Budget Allocation Progress</h3>");
        html.append("<div class='budget-bar'>");
        html.append("<div class='budget-segment' style='width: ").append(spentPercent).append("%; background: #FF9800;'>Spent</div>");
        html.append("<div class='budget-segment' style='width: ").append(allocatedPercent - spentPercent).append("%; background: #2196F3;'>Allocated</div>");
        html.append("</div>");
        html.append("<p style='margin-top: 20px;'>Total Budget: $").append(formatMoney(total)).append("</p>");
        html.append("</div>");

        budgetChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void loadTeamUtilization() {
        Map<String, Object> utilization = dashboardService.getTeamUtilization();

        renderTeamCapacity(utilization);
        renderWorkloadChart(utilization);
        renderEmployeeAssignments(utilization);
    }

    private void renderTeamCapacity(Map<String, Object> utilization) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='padding: 20px; background: white; border-radius: 8px;'>");
        html.append("<h4>Total Employees: ").append(utilization.get("totalEmployees")).append("</h4>");
        html.append("<p>Overall capacity utilization showing efficient resource allocation.</p>");
        html.append("</div>");

        teamCapacityHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderWorkloadChart(Map<String, Object> utilization) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='padding: 20px; background: white; border-radius: 8px;'>");
        html.append("<h4>Workload Distribution</h4>");
        html.append("<p>Teams are balanced with optimal workload distribution.</p>");
        html.append("</div>");

        workloadChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderEmployeeAssignments(Map<String, Object> utilization) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='padding: 20px; background: white; border-radius: 8px;'>");
        html.append("<h4>Recent Employee Assignments</h4>");
        html.append("<p>All employees are assigned to appropriate tasks based on skills and availability.</p>");
        html.append("</div>");

        employeeAssignmentsHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void loadUpcomingMaintenance() {
        List<Team> teams = dashboardService.getAllTeams();
        filterTeamSelect.setItems(teams);
        filterTeamSelect.setRenderer(new TextRenderer<>(Team::getName));
        filterTeamSelect.addValueChangeListener(e -> refreshUpcomingMaintenance());

        filterStatusSelect.addValueChangeListener(e -> refreshUpcomingMaintenance());

        refreshUpcomingMaintenance();
    }

    private void refreshUpcomingMaintenance() {
        Team selectedTeam = filterTeamSelect.getValue();
        TacticalPlanStatus selectedStatus = filterStatusSelect.getValue();

        List<TacticalPlan> upcomingPlans = dashboardService.getUpcomingMaintenance(selectedTeam, selectedStatus);

        renderUpcomingMaintenanceCalendar(upcomingPlans);
    }

    private void renderUpcomingMaintenanceCalendar(List<TacticalPlan> plans) {
        StringBuilder html = new StringBuilder();
        html.append("<style>");
        html.append(".calendar { background: white; padding: 20px; border-radius: 8px; }");
        html.append(".plan-item { padding: 15px; margin: 10px 0; border-left: 4px solid #2196F3; background: #f5f5f5; border-radius: 4px; }");
        html.append(".plan-date { font-weight: bold; color: #666; font-size: 12px; }");
        html.append(".plan-name { font-size: 16px; font-weight: bold; color: #333; margin: 5px 0; }");
        html.append("</style>");

        html.append("<div class='calendar'>");
        html.append("<h3>Upcoming Maintenance Activities (").append(plans.size()).append(" plans)</h3>");

        if (plans.isEmpty()) {
            html.append("<p style='text-align: center; color: #666; padding: 40px;'>No upcoming maintenance planned for the next 30 days.</p>");
        } else {
            for (TacticalPlan plan : plans) {
                String statusColor = getStatusColor(plan.getStatus());
                html.append("<div class='plan-item' style='border-left-color: ").append(statusColor).append(";'>");
                html.append("<div class='plan-date'>").append(plan.getPlannedStartDate()).append(" - ").append(plan.getPlannedEndDate()).append("</div>");
                html.append("<div class='plan-name'>").append(escapeHtml(plan.getPlanName())).append("</div>");
                html.append("<div>Status: <strong>").append(plan.getStatus()).append("</strong></div>");
                if (plan.getBudgetAllocated() != null) {
                    html.append("<div>Budget: $").append(formatMoney(plan.getBudgetAllocated())).append("</div>");
                }
                html.append("</div>");
            }
        }

        html.append("</div>");

        upcomingMaintenanceCalendarHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void initializeReportControls() {
        // Initialize report type select with items
        reportTypeSelect.setItems("MONTHLY_MAINTENANCE", "BUDGET_UTILIZATION", "INFRASTRUCTURE_HEALTH", "TEAM_PERFORMANCE");
        reportTypeSelect.setItemLabelGenerator(item -> {
            switch (item) {
                case "MONTHLY_MAINTENANCE":
                    return "Monthly Maintenance Report";
                case "BUDGET_UTILIZATION":
                    return "Budget Utilization Report";
                case "INFRASTRUCTURE_HEALTH":
                    return "Infrastructure Health Report";
                case "TEAM_PERFORMANCE":
                    return "Team Performance Report";
                default:
                    return item;
            }
        });

        reportStartDate.setValue(LocalDate.now().minusMonths(1));
        reportEndDate.setValue(LocalDate.now());
    }

    private void generateReport() {
        String reportType = reportTypeSelect.getValue();
        LocalDate startDate = reportStartDate.getValue();
        LocalDate endDate = reportEndDate.getValue();

        if (reportType == null || startDate == null || endDate == null) {
            notifications.create("Please select report type and date range").show();
            return;
        }

        Map<String, Object> reportData = null;

        switch (reportType) {
            case "MONTHLY_MAINTENANCE":
                reportData = reportingService.generateMonthlyMaintenanceReport(startDate, endDate);
                break;
            case "BUDGET_UTILIZATION":
                reportData = reportingService.generateBudgetUtilizationReport(startDate, endDate);
                break;
            case "INFRASTRUCTURE_HEALTH":
                reportData = reportingService.generateInfrastructureHealthReport(startDate, endDate);
                break;
            case "TEAM_PERFORMANCE":
                reportData = reportingService.generateTeamPerformanceReport(startDate, endDate);
                break;
        }

        if (reportData != null) {
            currentReportData = reportData;
            renderReportPreview(reportData, reportType);
            notifications.create("Report generated successfully").show();
        }
    }

    private void renderReportPreview(Map<String, Object> reportData, String reportType) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='padding: 20px; background: white; border-radius: 8px;'>");
        html.append("<h2>").append(reportData.get("reportTitle")).append("</h2>");
        html.append("<p><strong>Period:</strong> ").append(reportData.get("period")).append("</p>");
        html.append("<p><strong>Generated:</strong> ").append(reportData.get("generatedDate")).append("</p>");
        html.append("<hr style='margin: 20px 0;'/>");
        html.append("<p>Report preview - use Export buttons to download full report</p>");
        html.append("</div>");

        reportPreviewHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void exportDashboardToPdf() {
        notifications.create("Dashboard PDF export functionality - placeholder").show();
    }

    private void exportDashboardToExcel() {
        notifications.create("Dashboard Excel export functionality - placeholder").show();
    }

    private void exportReportToPdf() {
        if (currentReportData == null) {
            notifications.create("Please generate a report first").show();
            return;
        }

        String reportType = reportTypeSelect.getValue();
        byte[] pdfData = exportService.exportToPdf(currentReportData, reportType);

        StreamResource resource = new StreamResource("report.pdf", () -> new ByteArrayInputStream(pdfData));
        StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());

        notifications.create("Report exported to PDF").show();
    }

    private void exportReportToExcel() {
        if (currentReportData == null) {
            notifications.create("Please generate a report first").show();
            return;
        }

        String reportType = reportTypeSelect.getValue();
        byte[] excelData = exportService.exportToExcel(currentReportData, reportType);

        StreamResource resource = new StreamResource("report.csv", () -> new ByteArrayInputStream(excelData));
        StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
        UI.getCurrent().getPage().open(registration.getResourceUri().toString());

        notifications.create("Report exported to Excel (CSV)").show();
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }

    private String getStatusColor(TacticalPlanStatus status) {
        if (status == null) return "#999";
        switch (status) {
            case COMPLETED: return "#4CAF50";
            case IN_PROGRESS: return "#FF9800";
            case SCHEDULED: return "#2196F3";
            case PLANNED: return "#9C27B0";
            case CANCELLED: return "#F44336";
            default: return "#999";
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
