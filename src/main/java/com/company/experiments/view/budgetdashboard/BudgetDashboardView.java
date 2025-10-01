package com.company.experiments.view.budgetdashboard;

import com.company.experiments.entity.BudgetCategory;
import com.company.experiments.entity.Team;
import com.company.experiments.entity.YearlyBudget;
import com.company.experiments.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "budgetDashboard", layout = MainView.class)
@ViewController(id = "BudgetDashboard.view")
@ViewDescriptor(path = "budget-dashboard-view.xml")
public class BudgetDashboardView extends StandardView {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private ComboBox<Integer> yearFilterCombo;

    @ViewComponent
    private Div dashboardContent;

    @ViewComponent
    private Span totalAllocatedLabel;

    @ViewComponent
    private Span totalSpentLabel;

    @ViewComponent
    private Span totalRemainingLabel;

    @ViewComponent
    private Span overallPercentageLabel;

    private Integer selectedYear;

    @Subscribe
    public void onInit(InitEvent event) {
        initYearFilter();
        selectedYear = Year.now().getValue();
        yearFilterCombo.setValue(selectedYear);
        loadDashboardData();
    }

    private void initYearFilter() {
        List<Integer> years = new ArrayList<>();
        int currentYear = Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear + 5; i++) {
            years.add(i);
        }
        yearFilterCombo.setItems(years);
        yearFilterCombo.addValueChangeListener(e -> {
            selectedYear = e.getValue();
            if (selectedYear != null) {
                loadDashboardData();
            }
        });
    }

    private void loadDashboardData() {
        List<YearlyBudget> budgets = dataManager.load(YearlyBudget.class)
                .query("select e from YearlyBudget e " +
                        "where e.year = :year " +
                        "order by e.budgetCategory.name, e.team.name")
                .parameter("year", selectedYear)
                .fetchPlan("yearlyBudget-dashboard-view")
                .list();

        updateSummaryCards(budgets);
        renderTables(budgets);
    }

    private void updateSummaryCards(List<YearlyBudget> budgets) {
        BigDecimal totalAllocated = budgets.stream()
                .map(YearlyBudget::getAllocatedAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = budgets.stream()
                .map(YearlyBudget::getSpentAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalAllocated.subtract(totalSpent);

        String currency = budgets.isEmpty() ? "EUR" : budgets.get(0).getCurrency();

        totalAllocatedLabel.setText(String.format("%.2f %s", totalAllocated, currency));
        totalSpentLabel.setText(String.format("%.2f %s", totalSpent, currency));
        totalRemainingLabel.setText(String.format("%.2f %s", totalRemaining, currency));

        BigDecimal percentage = totalAllocated.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(BigDecimal.valueOf(100)).divide(totalAllocated, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        overallPercentageLabel.setText(String.format("%.2f%%", percentage));

        // Color coding
        if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            overallPercentageLabel.getStyle().set("color", "red");
        } else if (percentage.compareTo(BigDecimal.valueOf(80)) > 0) {
            overallPercentageLabel.getStyle().set("color", "orange");
        } else {
            overallPercentageLabel.getStyle().set("color", "green");
        }
    }

    private void renderTables(List<YearlyBudget> budgets) {
        dashboardContent.removeAll();

        VerticalLayout tablesLayout = new VerticalLayout();
        tablesLayout.setWidthFull();
        tablesLayout.setSpacing(true);
        tablesLayout.setPadding(true);

        // Budget by Category Table
        Component categoryTable = createBudgetByCategoryTable(budgets);
        tablesLayout.add(categoryTable);

        // Budget by Team Table
        Component teamTable = createBudgetByTeamTable(budgets);
        tablesLayout.add(teamTable);

        dashboardContent.add(tablesLayout);
    }

    private Component createBudgetByCategoryTable(List<YearlyBudget> budgets) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("border", "1px solid #e0e0e0");

        H3 title = new H3("Budget Allocation by Category");
        title.getStyle().set("margin-top", "0");
        layout.add(title);

        Map<BudgetCategory, BigDecimal> categoryAllocated = budgets.stream()
                .collect(Collectors.groupingBy(
                        YearlyBudget::getBudgetCategory,
                        Collectors.reducing(BigDecimal.ZERO, YearlyBudget::getAllocatedAmount, BigDecimal::add)
                ));

        Map<BudgetCategory, BigDecimal> categorySpent = budgets.stream()
                .collect(Collectors.groupingBy(
                        YearlyBudget::getBudgetCategory,
                        Collectors.reducing(BigDecimal.ZERO, b -> b.getSpentAmount() != null ? b.getSpentAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));

        // Header row
        HorizontalLayout header = createTableHeader("Category", "Allocated", "Spent", "Remaining", "%");
        layout.add(header);

        // Data rows
        for (BudgetCategory category : categoryAllocated.keySet()) {
            BigDecimal allocated = categoryAllocated.get(category);
            BigDecimal spent = categorySpent.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal remaining = allocated.subtract(spent);
            BigDecimal percentage = allocated.compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100)).divide(allocated, 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            HorizontalLayout row = createTableRow(
                    category.getName(),
                    String.format("%.2f", allocated),
                    String.format("%.2f", spent),
                    String.format("%.2f", remaining),
                    String.format("%.2f%%", percentage)
            );
            layout.add(row);
        }

        return layout;
    }

    private Component createBudgetByTeamTable(List<YearlyBudget> budgets) {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("background-color", "#ffffff")
                .set("border-radius", "8px")
                .set("padding", "16px")
                .set("border", "1px solid #e0e0e0");

        H3 title = new H3("Budget Allocation by Team");
        title.getStyle().set("margin-top", "0");
        layout.add(title);

        Map<Team, BigDecimal> teamAllocated = budgets.stream()
                .collect(Collectors.groupingBy(
                        YearlyBudget::getTeam,
                        Collectors.reducing(BigDecimal.ZERO, YearlyBudget::getAllocatedAmount, BigDecimal::add)
                ));

        Map<Team, BigDecimal> teamSpent = budgets.stream()
                .collect(Collectors.groupingBy(
                        YearlyBudget::getTeam,
                        Collectors.reducing(BigDecimal.ZERO, b -> b.getSpentAmount() != null ? b.getSpentAmount() : BigDecimal.ZERO, BigDecimal::add)
                ));

        // Header row
        HorizontalLayout header = createTableHeader("Team", "Allocated", "Spent", "Remaining", "%");
        layout.add(header);

        // Data rows
        for (Team team : teamAllocated.keySet()) {
            BigDecimal allocated = teamAllocated.get(team);
            BigDecimal spent = teamSpent.getOrDefault(team, BigDecimal.ZERO);
            BigDecimal remaining = allocated.subtract(spent);
            BigDecimal percentage = allocated.compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100)).divide(allocated, 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            HorizontalLayout row = createTableRow(
                    team.getName(),
                    String.format("%.2f", allocated),
                    String.format("%.2f", spent),
                    String.format("%.2f", remaining),
                    String.format("%.2f%%", percentage)
            );
            layout.add(row);
        }

        return layout;
    }

    private HorizontalLayout createTableHeader(String... headers) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "8px")
                .set("border-bottom", "2px solid #ddd")
                .set("font-weight", "bold");

        for (String text : headers) {
            Span cell = new Span(text);
            cell.getStyle().set("flex", "1");
            header.add(cell);
        }

        return header;
    }

    private HorizontalLayout createTableRow(String... values) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.getStyle()
                .set("padding", "8px")
                .set("border-bottom", "1px solid #eee");

        for (String text : values) {
            Span cell = new Span(text);
            cell.getStyle().set("flex", "1");
            row.add(cell);
        }

        return row;
    }
}
