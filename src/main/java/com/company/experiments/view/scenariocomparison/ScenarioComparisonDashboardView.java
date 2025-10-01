package com.company.experiments.view.scenariocomparison;

import com.company.experiments.entity.MetricType;
import com.company.experiments.entity.ScenarioComparison;
import com.company.experiments.entity.ScenarioMetrics;
import com.company.experiments.entity.YearlyBudgetPlanningScenario;
import com.company.experiments.view.main.MainView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "scenario-comparisons/dashboard/:id", layout = MainView.class)
@ViewController(id = "ScenarioComparison.dashboard")
@ViewDescriptor(path = "scenario-comparison-dashboard-view.xml")
@EditedEntityContainer("scenarioComparisonDc")
public class ScenarioComparisonDashboardView extends StandardDetailView<ScenarioComparison> {

    @ViewComponent
    private Div comparisonTableHtml;

    @ViewComponent
    private Div barChartHtml;

    @ViewComponent
    private Div radarChartHtml;

    @ViewComponent
    private Div whatIfResultsHtml;

    @ViewComponent
    private InstanceContainer<ScenarioComparison> scenarioComparisonDc;

    @ViewComponent
    private CollectionLoader<ScenarioMetrics> scenarioMetricsDl;

    @ViewComponent
    private Select<YearlyBudgetPlanningScenario> adjustScenarioSelect;

    @ViewComponent
    private Select<MetricType> adjustMetricSelect;

    @ViewComponent
    private BigDecimalField adjustValueField;

    @ViewComponent
    private H2 titleLabel;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Notifications notifications;

    private Map<UUID, Map<MetricType, BigDecimal>> adjustments = new HashMap<>();
    private Map<UUID, Map<MetricType, BigDecimal>> originalMetrics = new HashMap<>();

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison != null) {
            titleLabel.setText("Comparison: " + comparison.getComparisonName());
            loadMetrics();
            renderComparisonTable();
            renderBarChart();
            renderRadarChart();
            initializeWhatIfTools();
        }
    }

    @Subscribe("backButton")
    public void onBackButtonClick(final ClickEvent<JmixButton> event) {
        viewNavigators.view(this, ScenarioComparisonListView.class).navigate();
    }

    @Subscribe("refreshButton")
    public void onRefreshButtonClick(final ClickEvent<JmixButton> event) {
        loadMetrics();
        renderComparisonTable();
        renderBarChart();
        renderRadarChart();
        renderWhatIfResults();
        notifications.create("Data refreshed successfully").show();
    }

    @Subscribe("exportButton")
    public void onExportButtonClick(final ClickEvent<JmixButton> event) {
        exportComparisonReport();
    }

    @Subscribe("applyAdjustmentButton")
    public void onApplyAdjustmentButtonClick(final ClickEvent<JmixButton> event) {
        applyWhatIfAdjustment();
    }

    @Subscribe("resetAdjustmentsButton")
    public void onResetAdjustmentsButtonClick(final ClickEvent<JmixButton> event) {
        adjustments.clear();
        renderWhatIfResults();
        notifications.create("All adjustments reset").show();
    }

    private void loadMetrics() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        List<UUID> scenarioIds = new ArrayList<>();
        scenarioIds.add(comparison.getBaselineScenario().getId());
        if (comparison.getAlternativeScenarios() != null) {
            scenarioIds.addAll(comparison.getAlternativeScenarios().stream()
                    .map(YearlyBudgetPlanningScenario::getId)
                    .collect(Collectors.toList()));
        }

        scenarioMetricsDl.setParameter("scenarioIds", scenarioIds);
        scenarioMetricsDl.load();

        // Store original metrics
        originalMetrics.clear();
        List<ScenarioMetrics> metrics = dataManager.load(ScenarioMetrics.class)
                .query("select e from ScenarioMetrics e where e.scenario.id in :ids")
                .parameter("ids", scenarioIds)
                .list();

        for (ScenarioMetrics metric : metrics) {
            UUID scenarioId = metric.getScenario().getId();
            originalMetrics.putIfAbsent(scenarioId, new HashMap<>());
            originalMetrics.get(scenarioId).put(metric.getMetricType(), metric.getMetricValue());
        }
    }

    private void initializeWhatIfTools() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        List<YearlyBudgetPlanningScenario> allScenarios = new ArrayList<>();
        allScenarios.add(comparison.getBaselineScenario());
        if (comparison.getAlternativeScenarios() != null) {
            allScenarios.addAll(comparison.getAlternativeScenarios());
        }

        adjustScenarioSelect.setItems(allScenarios);
        adjustScenarioSelect.setItemLabelGenerator(YearlyBudgetPlanningScenario::getScenarioName);

        renderWhatIfResults();
    }

    private void renderComparisonTable() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        List<YearlyBudgetPlanningScenario> allScenarios = new ArrayList<>();
        allScenarios.add(comparison.getBaselineScenario());
        if (comparison.getAlternativeScenarios() != null) {
            allScenarios.addAll(comparison.getAlternativeScenarios());
        }

        StringBuilder html = new StringBuilder();
        html.append("<style>")
                .append(".comparison-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }")
                .append(".comparison-table { width: 100%; border-collapse: collapse; }")
                .append(".comparison-table th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px; text-align: left; font-weight: 600; }")
                .append(".comparison-table td { padding: 12px; border-bottom: 1px solid #e0e0e0; }")
                .append(".comparison-table tr:hover { background: #f5f5f5; }")
                .append(".metric-label { font-weight: 500; color: #333; }")
                .append(".baseline-value { background: #e3f2fd; font-weight: bold; }")
                .append(".better-value { color: #4CAF50; font-weight: bold; }")
                .append(".worse-value { color: #F44336; font-weight: bold; }")
                .append(".delta { font-size: 11px; margin-left: 8px; }")
                .append("</style>");

        html.append("<div class='comparison-container'>");
        html.append("<table class='comparison-table'>");

        // Header
        html.append("<tr><th>Metric</th>");
        for (YearlyBudgetPlanningScenario scenario : allScenarios) {
            html.append("<th>").append(escapeHtml(scenario.getScenarioName())).append("</th>");
        }
        html.append("</tr>");

        // Metrics rows
        MetricType[] metricTypes = {MetricType.COST, MetricType.UPTIME, MetricType.RISK, MetricType.EFFICIENCY};
        for (MetricType metricType : metricTypes) {
            html.append("<tr>");
            html.append("<td class='metric-label'>").append(metricType.getId()).append("</td>");

            BigDecimal baselineValue = getMetricValue(comparison.getBaselineScenario().getId(), metricType);

            for (int i = 0; i < allScenarios.size(); i++) {
                YearlyBudgetPlanningScenario scenario = allScenarios.get(i);
                BigDecimal value = getMetricValue(scenario.getId(), metricType);

                String cellClass = i == 0 ? "baseline-value" : "";
                html.append("<td class='").append(cellClass).append("'>");

                if (value != null) {
                    html.append(formatMetricValue(metricType, value));

                    if (i > 0 && baselineValue != null) {
                        BigDecimal delta = value.subtract(baselineValue);
                        BigDecimal deltaPercent = baselineValue.compareTo(BigDecimal.ZERO) != 0
                                ? delta.divide(baselineValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                                : BigDecimal.ZERO;

                        boolean isBetter = isBetterValue(metricType, delta);
                        String deltaClass = isBetter ? "better-value" : "worse-value";
                        String sign = delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

                        html.append("<span class='delta ").append(deltaClass).append("'>");
                        html.append("(").append(sign).append(String.format("%.1f%%", deltaPercent)).append(")");
                        html.append("</span>");
                    }
                } else {
                    html.append("N/A");
                }

                html.append("</td>");
            }

            html.append("</tr>");
        }

        // Budget row
        html.append("<tr>");
        html.append("<td class='metric-label'>Total Budget</td>");
        for (int i = 0; i < allScenarios.size(); i++) {
            YearlyBudgetPlanningScenario scenario = allScenarios.get(i);
            String cellClass = i == 0 ? "baseline-value" : "";
            html.append("<td class='").append(cellClass).append("'>");
            if (scenario.getTotalBudget() != null) {
                html.append("$").append(String.format("%,.2f", scenario.getTotalBudget()));
            } else {
                html.append("N/A");
            }
            html.append("</td>");
        }
        html.append("</tr>");

        html.append("</table></div>");

        comparisonTableHtml.removeAll();
        comparisonTableHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderBarChart() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        List<YearlyBudgetPlanningScenario> allScenarios = new ArrayList<>();
        allScenarios.add(comparison.getBaselineScenario());
        if (comparison.getAlternativeScenarios() != null) {
            allScenarios.addAll(comparison.getAlternativeScenarios());
        }

        StringBuilder html = new StringBuilder();
        html.append("<style>")
                .append(".bar-chart-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }")
                .append(".bar-group { margin-bottom: 30px; }")
                .append(".bar-label { font-weight: 600; color: #333; margin-bottom: 10px; }")
                .append(".bar-item { display: flex; align-items: center; margin-bottom: 8px; }")
                .append(".bar-name { width: 150px; font-size: 12px; color: #666; }")
                .append(".bar-visual { flex: 1; height: 30px; position: relative; background: #e0e0e0; border-radius: 4px; overflow: hidden; }")
                .append(".bar-fill { height: 100%; display: flex; align-items: center; padding: 0 10px; color: white; font-size: 11px; font-weight: bold; transition: width 0.3s; }")
                .append(".bar-value { margin-left: 10px; font-weight: bold; color: #333; min-width: 80px; }")
                .append(".bar-cost { background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%); }")
                .append(".bar-uptime { background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%); }")
                .append(".bar-risk { background: linear-gradient(90deg, #fa709a 0%, #fee140 100%); }")
                .append(".bar-efficiency { background: linear-gradient(90deg, #30cfd0 0%, #330867 100%); }")
                .append("</style>");

        html.append("<div class='bar-chart-container'>");

        MetricType[] metricTypes = {MetricType.COST, MetricType.UPTIME, MetricType.RISK, MetricType.EFFICIENCY};
        String[] barClasses = {"bar-cost", "bar-uptime", "bar-risk", "bar-efficiency"};

        for (int m = 0; m < metricTypes.length; m++) {
            MetricType metricType = metricTypes[m];
            String barClass = barClasses[m];

            html.append("<div class='bar-group'>");
            html.append("<div class='bar-label'>").append(metricType.getId()).append("</div>");

            BigDecimal maxValue = allScenarios.stream()
                    .map(s -> getMetricValue(s.getId(), metricType))
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);

            for (YearlyBudgetPlanningScenario scenario : allScenarios) {
                BigDecimal value = getMetricValue(scenario.getId(), metricType);
                double percent = value != null && maxValue.compareTo(BigDecimal.ZERO) > 0
                        ? value.divide(maxValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                        : 0;

                html.append("<div class='bar-item'>");
                html.append("<div class='bar-name'>").append(escapeHtml(scenario.getScenarioName())).append("</div>");
                html.append("<div class='bar-visual'>");
                html.append("<div class='bar-fill ").append(barClass).append("' style='width: ").append(String.format("%.1f", percent)).append("%;'>");
                if (percent > 20) {
                    html.append(formatMetricValue(metricType, value));
                }
                html.append("</div></div>");
                html.append("<div class='bar-value'>").append(formatMetricValue(metricType, value)).append("</div>");
                html.append("</div>");
            }

            html.append("</div>");
        }

        html.append("</div>");

        barChartHtml.removeAll();
        barChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderRadarChart() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        List<YearlyBudgetPlanningScenario> allScenarios = new ArrayList<>();
        allScenarios.add(comparison.getBaselineScenario());
        if (comparison.getAlternativeScenarios() != null) {
            allScenarios.addAll(comparison.getAlternativeScenarios());
        }

        // Normalize all metrics to 0-100 scale
        Map<MetricType, BigDecimal> maxValues = new HashMap<>();
        MetricType[] metricTypes = {MetricType.COST, MetricType.UPTIME, MetricType.RISK, MetricType.EFFICIENCY};

        for (MetricType metricType : metricTypes) {
            BigDecimal max = allScenarios.stream()
                    .map(s -> getMetricValue(s.getId(), metricType))
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ONE);
            maxValues.put(metricType, max);
        }

        StringBuilder html = new StringBuilder();
        html.append("<style>")
                .append(".radar-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); text-align: center; }")
                .append(".radar-canvas { width: 100%; max-width: 400px; height: 400px; margin: 0 auto; position: relative; }")
                .append(".radar-info { margin-top: 20px; display: flex; justify-content: center; gap: 20px; flex-wrap: wrap; }")
                .append(".radar-legend { display: flex; align-items: center; gap: 8px; }")
                .append(".legend-box { width: 20px; height: 20px; border-radius: 3px; }")
                .append("</style>");

        html.append("<div class='radar-container'>");
        html.append("<svg class='radar-canvas' viewBox='0 0 400 400'>");

        // Draw background grid
        double cx = 200, cy = 200;
        double[] radiusLevels = {40, 80, 120, 160, 200};

        for (double r : radiusLevels) {
            html.append("<circle cx='").append(cx).append("' cy='").append(cy)
                    .append("' r='").append(r)
                    .append("' fill='none' stroke='#e0e0e0' stroke-width='1'/>");
        }

        // Draw axes
        int numAxes = metricTypes.length;
        double angleStep = 2 * Math.PI / numAxes;

        for (int i = 0; i < numAxes; i++) {
            double angle = i * angleStep - Math.PI / 2;
            double x2 = cx + 200 * Math.cos(angle);
            double y2 = cy + 200 * Math.sin(angle);

            html.append("<line x1='").append(cx).append("' y1='").append(cy)
                    .append("' x2='").append(x2).append("' y2='").append(y2)
                    .append("' stroke='#ccc' stroke-width='1'/>");

            // Labels
            double labelX = cx + 220 * Math.cos(angle);
            double labelY = cy + 220 * Math.sin(angle);

            html.append("<text x='").append(labelX).append("' y='").append(labelY)
                    .append("' text-anchor='middle' fill='#333' font-size='12' font-weight='bold'>")
                    .append(metricTypes[i].getId())
                    .append("</text>");
        }

        // Draw scenario polygons
        String[] colors = {"#2196F3", "#FF9800", "#4CAF50", "#F44336", "#9C27B0"};

        for (int s = 0; s < allScenarios.size(); s++) {
            YearlyBudgetPlanningScenario scenario = allScenarios.get(s);
            String color = colors[s % colors.length];

            StringBuilder points = new StringBuilder();

            for (int i = 0; i < numAxes; i++) {
                MetricType metricType = metricTypes[i];
                BigDecimal value = getMetricValue(scenario.getId(), metricType);
                BigDecimal maxValue = maxValues.get(metricType);

                double normalized = value != null && maxValue.compareTo(BigDecimal.ZERO) > 0
                        ? value.divide(maxValue, 4, RoundingMode.HALF_UP).doubleValue()
                        : 0;

                double angle = i * angleStep - Math.PI / 2;
                double radius = normalized * 200;
                double x = cx + radius * Math.cos(angle);
                double y = cy + radius * Math.sin(angle);

                points.append(x).append(",").append(y).append(" ");
            }

            html.append("<polygon points='").append(points)
                    .append("' fill='").append(color)
                    .append("' fill-opacity='0.2' stroke='").append(color)
                    .append("' stroke-width='2'/>");
        }

        html.append("</svg>");

        // Legend
        html.append("<div class='radar-info'>");
        for (int s = 0; s < allScenarios.size(); s++) {
            String color = colors[s % colors.length];
            html.append("<div class='radar-legend'>");
            html.append("<div class='legend-box' style='background: ").append(color).append(";'></div>");
            html.append("<span>").append(escapeHtml(allScenarios.get(s).getScenarioName())).append("</span>");
            html.append("</div>");
        }
        html.append("</div>");

        html.append("</div>");

        radarChartHtml.removeAll();
        radarChartHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void renderWhatIfResults() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        List<YearlyBudgetPlanningScenario> allScenarios = new ArrayList<>();
        allScenarios.add(comparison.getBaselineScenario());
        if (comparison.getAlternativeScenarios() != null) {
            allScenarios.addAll(comparison.getAlternativeScenarios());
        }

        StringBuilder html = new StringBuilder();
        html.append("<style>")
                .append(".whatif-container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }")
                .append(".whatif-table { width: 100%; border-collapse: collapse; }")
                .append(".whatif-table th { background: #f5f5f5; padding: 10px; text-align: left; border-bottom: 2px solid #ddd; }")
                .append(".whatif-table td { padding: 10px; border-bottom: 1px solid #e0e0e0; }")
                .append(".adjusted-value { background: #fff3cd; font-weight: bold; }")
                .append(".original-value { color: #999; text-decoration: line-through; font-size: 11px; }")
                .append("</style>");

        html.append("<div class='whatif-container'>");

        if (adjustments.isEmpty()) {
            html.append("<p style='text-align: center; color: #666; padding: 40px;'>No adjustments applied yet. Use the controls above to simulate changes.</p>");
        } else {
            html.append("<table class='whatif-table'>");
            html.append("<tr><th>Scenario</th><th>Metric</th><th>Original</th><th>Adjusted</th><th>Change</th></tr>");

            for (Map.Entry<UUID, Map<MetricType, BigDecimal>> scenarioEntry : adjustments.entrySet()) {
                UUID scenarioId = scenarioEntry.getKey();
                YearlyBudgetPlanningScenario scenario = allScenarios.stream()
                        .filter(s -> s.getId().equals(scenarioId))
                        .findFirst()
                        .orElse(null);

                if (scenario == null) continue;

                for (Map.Entry<MetricType, BigDecimal> metricEntry : scenarioEntry.getValue().entrySet()) {
                    MetricType metricType = metricEntry.getKey();
                    BigDecimal adjustedValue = metricEntry.getValue();
                    BigDecimal originalValue = originalMetrics.getOrDefault(scenarioId, new HashMap<>())
                            .get(metricType);

                    if (originalValue == null) continue;

                    BigDecimal change = adjustedValue.subtract(originalValue);
                    BigDecimal changePercent = originalValue.compareTo(BigDecimal.ZERO) != 0
                            ? change.divide(originalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

                    html.append("<tr>");
                    html.append("<td>").append(escapeHtml(scenario.getScenarioName())).append("</td>");
                    html.append("<td>").append(metricType.getId()).append("</td>");
                    html.append("<td>").append(formatMetricValue(metricType, originalValue)).append("</td>");
                    html.append("<td class='adjusted-value'>").append(formatMetricValue(metricType, adjustedValue)).append("</td>");
                    html.append("<td>").append(String.format("%+.1f%%", changePercent)).append("</td>");
                    html.append("</tr>");
                }
            }

            html.append("</table>");
        }

        html.append("</div>");

        whatIfResultsHtml.removeAll();
        whatIfResultsHtml.getElement().setProperty("innerHTML", html.toString());
    }

    private void applyWhatIfAdjustment() {
        YearlyBudgetPlanningScenario selectedScenario = adjustScenarioSelect.getValue();
        MetricType selectedMetric = adjustMetricSelect.getValue();
        BigDecimal adjustmentPercent = adjustValueField.getValue();

        if (selectedScenario == null || selectedMetric == null || adjustmentPercent == null) {
            notifications.create("Please fill all fields").show();
            return;
        }

        BigDecimal originalValue = originalMetrics.getOrDefault(selectedScenario.getId(), new HashMap<>())
                .get(selectedMetric);

        if (originalValue == null) {
            notifications.create("No original metric found").show();
            return;
        }

        BigDecimal adjustmentFactor = BigDecimal.ONE.add(adjustmentPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal adjustedValue = originalValue.multiply(adjustmentFactor);

        adjustments.putIfAbsent(selectedScenario.getId(), new HashMap<>());
        adjustments.get(selectedScenario.getId()).put(selectedMetric, adjustedValue);

        renderWhatIfResults();
        notifications.create("Adjustment applied successfully").show();
    }

    private void exportComparisonReport() {
        ScenarioComparison comparison = scenarioComparisonDc.getItem();
        if (comparison == null) return;

        StringBuilder report = new StringBuilder();
        report.append("=== SCENARIO COMPARISON REPORT ===\n\n");
        report.append("Comparison Name: ").append(comparison.getComparisonName()).append("\n");
        report.append("Created: ").append(comparison.getCreatedAt() != null
                ? comparison.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "N/A").append("\n");
        report.append("Created By: ").append(comparison.getCreatedBy() != null
                ? comparison.getCreatedBy().getFirstName() + " " + comparison.getCreatedBy().getLastName()
                : "N/A").append("\n\n");

        report.append("Baseline Scenario: ").append(comparison.getBaselineScenario().getScenarioName()).append("\n");
        report.append("Alternative Scenarios:\n");
        if (comparison.getAlternativeScenarios() != null) {
            for (YearlyBudgetPlanningScenario alt : comparison.getAlternativeScenarios()) {
                report.append("  - ").append(alt.getScenarioName()).append("\n");
            }
        }

        report.append("\n=== METRICS COMPARISON ===\n\n");

        List<YearlyBudgetPlanningScenario> allScenarios = new ArrayList<>();
        allScenarios.add(comparison.getBaselineScenario());
        if (comparison.getAlternativeScenarios() != null) {
            allScenarios.addAll(comparison.getAlternativeScenarios());
        }

        MetricType[] metricTypes = {MetricType.COST, MetricType.UPTIME, MetricType.RISK, MetricType.EFFICIENCY};
        for (MetricType metricType : metricTypes) {
            report.append(metricType.getId()).append(":\n");
            for (YearlyBudgetPlanningScenario scenario : allScenarios) {
                BigDecimal value = getMetricValue(scenario.getId(), metricType);
                report.append("  ").append(scenario.getScenarioName()).append(": ")
                        .append(formatMetricValue(metricType, value)).append("\n");
            }
            report.append("\n");
        }

        notifications.create("Report generated:\n" + report.toString())
                .withDuration(5000)
                .show();
    }

    private BigDecimal getMetricValue(UUID scenarioId, MetricType metricType) {
        // Check if there's an adjustment
        if (adjustments.containsKey(scenarioId) && adjustments.get(scenarioId).containsKey(metricType)) {
            return adjustments.get(scenarioId).get(metricType);
        }

        // Return original value
        return originalMetrics.getOrDefault(scenarioId, new HashMap<>()).get(metricType);
    }

    private String formatMetricValue(MetricType metricType, BigDecimal value) {
        if (value == null) return "N/A";

        switch (metricType) {
            case COST:
                return "$" + String.format("%,.2f", value);
            case UPTIME:
                return String.format("%.2f%%", value);
            case RISK:
                return String.format("%.1f", value);
            case EFFICIENCY:
                return String.format("%.2f%%", value);
            default:
                return value.toString();
        }
    }

    private boolean isBetterValue(MetricType metricType, BigDecimal delta) {
        switch (metricType) {
            case COST:
            case RISK:
                return delta.compareTo(BigDecimal.ZERO) < 0; // Lower is better
            case UPTIME:
            case EFFICIENCY:
                return delta.compareTo(BigDecimal.ZERO) > 0; // Higher is better
            default:
                return false;
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
