package com.company.experiments.view.tacticalplan;

import com.company.experiments.entity.TacticalPlan;
import com.company.experiments.entity.TacticalPlanStatus;
import com.company.experiments.entity.YearlyBudgetPlanningScenario;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "tactical-plans/gantt", layout = MainView.class)
@ViewController(id = "TacticalPlan.gantt")
@ViewDescriptor(path = "tactical-plan-gantt-view.xml")
public class TacticalPlanGanttView extends StandardView {

    @ViewComponent
    private Div ganttChartHtml;

    @ViewComponent
    private Div resourceAllocationChartHtml;

    @ViewComponent
    private CollectionContainer<TacticalPlan> tacticalPlanDc;

    @ViewComponent
    private CollectionLoader<TacticalPlan> tacticalPlanDl;

    @ViewComponent
    private Select<TacticalPlanStatus> filterByStatusSelect;

    @ViewComponent
    private EntityPicker<YearlyBudgetPlanningScenario> filterByScenarioField;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        tacticalPlanDl.load();
        renderGanttChart();
        renderResourceAllocationChart();
    }

    @Subscribe("backButton")
    public void onBackButtonClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, TacticalPlanListView.class).navigate();
    }

    @Subscribe("refreshButton")
    public void onRefreshButtonClick(final ClickEvent<JmixButton> event) {
        tacticalPlanDl.load();
        renderGanttChart();
        renderResourceAllocationChart();
    }

    @Subscribe("filterByStatusSelect")
    public void onFilterByStatusSelectValueChange(final com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<TacticalPlanStatus>, TacticalPlanStatus> event) {
        applyFilters();
    }

    @Subscribe("filterByScenarioField")
    public void onFilterByScenarioFieldValueChange(final com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<EntityPicker<YearlyBudgetPlanningScenario>, YearlyBudgetPlanningScenario> event) {
        applyFilters();
    }

    private void applyFilters() {
        StringBuilder query = new StringBuilder("select e from TacticalPlan e where e.plannedStartDate is not null and e.plannedEndDate is not null");

        if (filterByStatusSelect.getValue() != null) {
            query.append(" and e.status = :status");
        }

        if (filterByScenarioField.getValue() != null) {
            query.append(" and e.planningScenario = :scenario");
        }

        query.append(" order by e.plannedStartDate");

        List<TacticalPlan> filteredPlans = dataManager.load(TacticalPlan.class)
                .query(query.toString())
                .parameter("status", filterByStatusSelect.getValue() != null ? filterByStatusSelect.getValue().getId() : null)
                .parameter("scenario", filterByScenarioField.getValue())
                .list();

        tacticalPlanDc.setItems(filteredPlans);
        renderGanttChart();
        renderResourceAllocationChart();
    }

    private void renderGanttChart() {
        List<TacticalPlan> plans = new ArrayList<>(tacticalPlanDc.getItems());

        if (plans.isEmpty()) {
            ganttChartHtml.removeAll();
            ganttChartHtml.getElement().setProperty("innerHTML", "<div style='padding: 20px; text-align: center; color: #666;'>No tactical plans with dates found.</div>");
            return;
        }

        // Find date range
        LocalDate minDate = plans.stream()
                .map(TacticalPlan::getPlannedStartDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate maxDate = plans.stream()
                .map(TacticalPlan::getPlannedEndDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now().plusMonths(3));

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate);

        StringBuilder html = new StringBuilder();
        html.append("<style>")
                .append(".gantt-container { width: 100%; overflow-x: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }")
                .append(".gantt-legend { display: flex; gap: 20px; margin-bottom: 20px; padding: 10px; background: #f9f9f9; border-radius: 4px; }")
                .append(".legend-item { display: flex; align-items: center; gap: 8px; }")
                .append(".legend-color { width: 20px; height: 20px; border-radius: 3px; }")
                .append(".gantt-timeline { position: relative; min-height: 400px; }")
                .append(".gantt-header-row { display: flex; border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 15px; }")
                .append(".gantt-task-label { width: 300px; flex-shrink: 0; padding: 8px; font-weight: 500; }")
                .append(".gantt-timeline-grid { flex: 1; position: relative; min-width: 800px; }")
                .append(".gantt-months { display: flex; border-bottom: 1px solid #ddd; padding-bottom: 5px; margin-bottom: 10px; }")
                .append(".gantt-month { flex: 1; text-align: center; font-size: 11px; font-weight: bold; color: #666; }")
                .append(".gantt-task-row { display: flex; margin-bottom: 12px; align-items: center; }")
                .append(".gantt-task-info { width: 300px; flex-shrink: 0; padding: 8px; }")
                .append(".gantt-task-name { font-weight: 500; color: #333; }")
                .append(".gantt-task-team { font-size: 11px; color: #666; margin-top: 2px; }")
                .append(".gantt-bars-container { flex: 1; position: relative; height: 40px; }")
                .append(".gantt-bar-wrapper { position: absolute; height: 32px; top: 4px; }")
                .append(".gantt-bar { height: 100%; border-radius: 6px; padding: 0 10px; display: flex; align-items: center; color: white; font-size: 11px; font-weight: 500; box-shadow: 0 2px 4px rgba(0,0,0,0.2); cursor: pointer; transition: all 0.2s; }")
                .append(".gantt-bar:hover { transform: translateY(-2px); box-shadow: 0 4px 8px rgba(0,0,0,0.3); }")
                .append(".gantt-bar-text { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }")
                .append(".status-PLANNED { background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%); }")
                .append(".status-SCHEDULED { background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%); }")
                .append(".status-IN_PROGRESS { background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%); }")
                .append(".status-COMPLETED { background: linear-gradient(135deg, #9E9E9E 0%, #616161 100%); }")
                .append(".status-CANCELLED { background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%); }")
                .append(".gantt-grid-lines { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; }")
                .append(".gantt-grid-line { position: absolute; top: 0; height: 100%; width: 1px; background: #e0e0e0; }")
                .append("</style>");

        html.append("<div class='gantt-container'>");

        // Legend
        html.append("<div class='gantt-legend'>");
        html.append("<div class='legend-item'><div class='legend-color' style='background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%);'></div><span>Planned</span></div>");
        html.append("<div class='legend-item'><div class='legend-color' style='background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);'></div><span>Scheduled</span></div>");
        html.append("<div class='legend-item'><div class='legend-color' style='background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);'></div><span>In Progress</span></div>");
        html.append("<div class='legend-item'><div class='legend-color' style='background: linear-gradient(135deg, #9E9E9E 0%, #616161 100%);'></div><span>Completed</span></div>");
        html.append("<div class='legend-item'><div class='legend-color' style='background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);'></div><span>Cancelled</span></div>");
        html.append("</div>");

        html.append("<div class='gantt-timeline'>");

        // Header
        html.append("<div class='gantt-header-row'>");
        html.append("<div class='gantt-task-label'>Task / Team</div>");
        html.append("<div class='gantt-timeline-grid'>");

        // Month headers
        html.append("<div class='gantt-months'>");
        LocalDate currentMonth = minDate.withDayOfMonth(1);
        while (currentMonth.isBefore(maxDate) || currentMonth.equals(maxDate)) {
            html.append("<div class='gantt-month'>").append(currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))).append("</div>");
            currentMonth = currentMonth.plusMonths(1);
        }
        html.append("</div>");

        html.append("</div></div>");

        // Tasks
        for (TacticalPlan plan : plans) {
            html.append("<div class='gantt-task-row'>");

            // Task info
            html.append("<div class='gantt-task-info'>");
            html.append("<div class='gantt-task-name'>").append(escapeHtml(plan.getPlanName())).append("</div>");
            html.append("<div class='gantt-task-team'>").append(plan.getAssignedTeam() != null ? escapeHtml(plan.getAssignedTeam().getName()) : "Unassigned").append("</div>");
            html.append("</div>");

            // Timeline bars
            html.append("<div class='gantt-bars-container'>");

            // Grid lines
            html.append("<div class='gantt-grid-lines'>");
            currentMonth = minDate.withDayOfMonth(1);
            int monthIndex = 0;
            while (currentMonth.isBefore(maxDate) || currentMonth.equals(maxDate)) {
                long daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(minDate, currentMonth);
                double leftPercent = (double) daysFromStart / totalDays * 100;
                html.append("<div class='gantt-grid-line' style='left: ").append(String.format("%.2f", leftPercent)).append("%;'></div>");
                currentMonth = currentMonth.plusMonths(1);
                monthIndex++;
            }
            html.append("</div>");

            // Task bar
            LocalDate startDate = plan.getPlannedStartDate();
            LocalDate endDate = plan.getPlannedEndDate();

            long daysFromStart = java.time.temporal.ChronoUnit.DAYS.between(minDate, startDate);
            long taskDuration = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

            double leftPercent = (double) daysFromStart / totalDays * 100;
            double widthPercent = (double) taskDuration / totalDays * 100;

            String statusClass = plan.getStatus() != null ? "status-" + plan.getStatus().getId() : "status-PLANNED";
            String duration = calculateDuration(startDate, endDate);

            html.append("<div class='gantt-bar-wrapper' style='left: ").append(String.format("%.2f", leftPercent)).append("%; width: ").append(String.format("%.2f", widthPercent)).append("%;'>");
            html.append("<div class='gantt-bar ").append(statusClass).append("' title='").append(escapeHtml(plan.getPlanName())).append(" (").append(duration).append(")'>");
            html.append("<span class='gantt-bar-text'>").append(escapeHtml(plan.getMaintenanceScenario().getScenarioName())).append("</span>");
            html.append("</div></div>");

            html.append("</div></div>");
        }

        html.append("</div></div>");
        ganttChartHtml.removeAll();
        ganttChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderResourceAllocationChart() {
        List<TacticalPlan> plans = new ArrayList<>(tacticalPlanDc.getItems());

        if (plans.isEmpty()) {
            resourceAllocationChartHtml.removeAll();
            resourceAllocationChartHtml.getElement().setProperty("innerHTML", "<div style='padding: 20px; text-align: center; color: #666;'>No resource allocation data available.</div>");
            return;
        }

        Map<String, Integer> teamAllocation = new HashMap<>();
        Map<String, BigDecimal> teamBudget = new HashMap<>();
        Map<String, Map<String, Integer>> teamStatusCount = new HashMap<>();

        for (TacticalPlan plan : plans) {
            String teamName = plan.getAssignedTeam() != null ? plan.getAssignedTeam().getName() : "Unassigned";
            teamAllocation.merge(teamName, 1, Integer::sum);

            if (plan.getBudgetAllocated() != null) {
                teamBudget.merge(teamName, plan.getBudgetAllocated(), BigDecimal::add);
            }

            // Track status distribution per team
            String status = plan.getStatus() != null ? plan.getStatus().getId() : "PLANNED";
            teamStatusCount.putIfAbsent(teamName, new HashMap<>());
            teamStatusCount.get(teamName).merge(status, 1, Integer::sum);
        }

        BigDecimal totalBudget = teamBudget.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalPlans = plans.size();

        StringBuilder html = new StringBuilder();
        html.append("<style>")
                .append(".resource-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }")
                .append(".resource-summary { display: flex; gap: 20px; margin-bottom: 30px; }")
                .append(".summary-card { flex: 1; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px; color: white; }")
                .append(".summary-card h3 { margin: 0 0 10px 0; font-size: 14px; opacity: 0.9; }")
                .append(".summary-card .value { font-size: 32px; font-weight: bold; margin: 0; }")
                .append(".resource-chart { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 20px; }")
                .append(".resource-card { padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; background: #fafafa; transition: all 0.3s; }")
                .append(".resource-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); transform: translateY(-2px); }")
                .append(".resource-card h4 { margin: 0 0 15px 0; color: #333; font-size: 18px; border-bottom: 2px solid #2196F3; padding-bottom: 8px; }")
                .append(".resource-metrics { display: flex; flex-direction: column; gap: 15px; }")
                .append(".metric-row { display: flex; justify-content: space-between; align-items: center; }")
                .append(".metric-label { font-size: 13px; color: #666; }")
                .append(".metric-value { font-size: 20px; font-weight: bold; color: #2196F3; }")
                .append(".progress-bar-container { margin-top: 15px; }")
                .append(".progress-label { font-size: 12px; color: #666; margin-bottom: 5px; }")
                .append(".progress-bar { height: 24px; background: #e0e0e0; border-radius: 12px; overflow: hidden; position: relative; }")
                .append(".progress-fill { height: 100%; background: linear-gradient(90deg, #4CAF50 0%, #45a049 100%); display: flex; align-items: center; justify-content: center; color: white; font-size: 11px; font-weight: bold; transition: width 0.3s; }")
                .append(".status-breakdown { margin-top: 15px; padding-top: 15px; border-top: 1px solid #ddd; }")
                .append(".status-breakdown-title { font-size: 12px; color: #666; margin-bottom: 8px; font-weight: 500; }")
                .append(".status-chips { display: flex; flex-wrap: wrap; gap: 6px; }")
                .append(".status-chip { padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 500; color: white; }")
                .append(".chip-PLANNED { background: #2196F3; }")
                .append(".chip-SCHEDULED { background: #FF9800; }")
                .append(".chip-IN_PROGRESS { background: #4CAF50; }")
                .append(".chip-COMPLETED { background: #9E9E9E; }")
                .append(".chip-CANCELLED { background: #F44336; }")
                .append("</style>");

        html.append("<div class='resource-container'>");

        // Summary cards
        html.append("<div class='resource-summary'>");
        html.append("<div class='summary-card' style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);'>");
        html.append("<h3>Total Plans</h3>");
        html.append("<p class='value'>").append(totalPlans).append("</p>");
        html.append("</div>");

        html.append("<div class='summary-card' style='background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);'>");
        html.append("<h3>Total Budget</h3>");
        html.append("<p class='value'>$").append(String.format("%,.0f", totalBudget)).append("</p>");
        html.append("</div>");

        html.append("<div class='summary-card' style='background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);'>");
        html.append("<h3>Active Teams</h3>");
        html.append("<p class='value'>").append(teamAllocation.size()).append("</p>");
        html.append("</div>");
        html.append("</div>");

        // Team cards
        html.append("<div class='resource-chart'>");

        for (Map.Entry<String, Integer> entry : teamAllocation.entrySet()) {
            String teamName = entry.getKey();
            Integer count = entry.getValue();
            BigDecimal budget = teamBudget.getOrDefault(teamName, BigDecimal.ZERO);
            double budgetPercent = totalBudget.compareTo(BigDecimal.ZERO) > 0
                    ? budget.divide(totalBudget, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0;

            html.append("<div class='resource-card'>");
            html.append("<h4>").append(escapeHtml(teamName)).append("</h4>");

            html.append("<div class='resource-metrics'>");

            html.append("<div class='metric-row'>");
            html.append("<span class='metric-label'>Plans Assigned</span>");
            html.append("<span class='metric-value'>").append(count).append("</span>");
            html.append("</div>");

            html.append("<div class='metric-row'>");
            html.append("<span class='metric-label'>Budget Allocated</span>");
            html.append("<span class='metric-value'>$").append(String.format("%,.2f", budget)).append("</span>");
            html.append("</div>");

            html.append("</div>");

            // Budget progress bar
            html.append("<div class='progress-bar-container'>");
            html.append("<div class='progress-label'>Budget Share: ").append(String.format("%.1f%%", budgetPercent)).append("</div>");
            html.append("<div class='progress-bar'>");
            html.append("<div class='progress-fill' style='width: ").append(String.format("%.1f", budgetPercent)).append("%;'>");
            if (budgetPercent > 15) {
                html.append(String.format("%.1f%%", budgetPercent));
            }
            html.append("</div></div></div>");

            // Status breakdown
            Map<String, Integer> statusCounts = teamStatusCount.get(teamName);
            if (statusCounts != null && !statusCounts.isEmpty()) {
                html.append("<div class='status-breakdown'>");
                html.append("<div class='status-breakdown-title'>Status Distribution</div>");
                html.append("<div class='status-chips'>");

                for (Map.Entry<String, Integer> statusEntry : statusCounts.entrySet()) {
                    String status = statusEntry.getKey();
                    Integer statusCount = statusEntry.getValue();
                    String statusLabel = status.replace("_", " ");

                    html.append("<span class='status-chip chip-").append(status).append("'>");
                    html.append(statusLabel).append(": ").append(statusCount);
                    html.append("</span>");
                }

                html.append("</div></div>");
            }

            html.append("</div>");
        }

        html.append("</div></div>");
        resourceAllocationChartHtml.removeAll();
        resourceAllocationChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private String calculateDuration(LocalDate start, LocalDate end) {
        if (start == null || end == null) return "N/A";
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (days == 0) return "1 day";
        if (days == 1) return "1 day";
        return days + " days";
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
