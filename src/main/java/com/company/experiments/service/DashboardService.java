package com.company.experiments.service;

import com.company.experiments.entity.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for aggregating dashboard data and calculating KPIs
 */
@Service("DashboardService")
public class DashboardService {

    private final DataManager dataManager;

    public DashboardService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Calculate KPIs for the executive summary
     */
    public Map<String, Object> calculateKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        // Unplanned Stops Reduction %
        kpis.put("unplannedStopsReduction", calculateUnplannedStopsReduction());

        // Maintenance Cost Savings %
        kpis.put("maintenanceCostSavings", calculateMaintenanceCostSavings());

        // Productivity Increase %
        kpis.put("productivityIncrease", calculateProductivityIncrease());

        return kpis;
    }

    private BigDecimal calculateUnplannedStopsReduction() {
        // Simulate calculation: Compare planned vs unplanned maintenance
        // In real implementation, this would query historical data

        Long totalPlans = dataManager.loadValue("select count(e) from TacticalPlan e", Long.class)
                .one();

        Long completedPlans = dataManager.loadValue("select count(e) from TacticalPlan e where e.status = :status", Long.class)
                .parameter("status", TacticalPlanStatus.COMPLETED)
                .one();

        if (totalPlans == 0) {
            return BigDecimal.valueOf(15.5); // Default value
        }

        BigDecimal completionRate = BigDecimal.valueOf(completedPlans)
                .divide(BigDecimal.valueOf(totalPlans), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return completionRate.multiply(BigDecimal.valueOf(0.8)); // Assume 80% correlation
    }

    private BigDecimal calculateMaintenanceCostSavings() {
        // Calculate cost savings compared to baseline
        List<YearlyBudgetPlanningScenario> scenarios = dataManager.load(YearlyBudgetPlanningScenario.class)
                .query("select e from YearlyBudgetPlanningScenario e where e.year = :year")
                .parameter("year", LocalDate.now().getYear())
                .list();

        if (scenarios.isEmpty()) {
            return BigDecimal.valueOf(12.3); // Default value
        }

        BigDecimal totalBudget = scenarios.stream()
                .map(YearlyBudgetPlanningScenario::getTotalBudget)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.planningScenario.year = :year")
                .parameter("year", LocalDate.now().getYear())
                .list();

        BigDecimal totalSpent = plans.stream()
                .map(TacticalPlan::getBudgetAllocated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(12.3);
        }

        BigDecimal savings = totalBudget.subtract(totalSpent)
                .divide(totalBudget, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return savings.max(BigDecimal.ZERO);
    }

    private BigDecimal calculateProductivityIncrease() {
        // Calculate based on planned vs actual completion times
        // Simplified calculation for demonstration

        List<TacticalPlan> completedPlans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.status = :status and e.plannedStartDate >= :date")
                .parameter("status", TacticalPlanStatus.COMPLETED)
                .parameter("date", LocalDate.now().minusMonths(3))
                .list();

        if (completedPlans.isEmpty()) {
            return BigDecimal.valueOf(18.7); // Default value
        }

        // Assume productivity increase based on completion rate
        return BigDecimal.valueOf(18.7).add(BigDecimal.valueOf(completedPlans.size() * 0.5));
    }

    /**
     * Get critical infrastructure alerts
     */
    public Map<String, Object> getCriticalAlerts() {
        Map<String, Object> alerts = new HashMap<>();

        List<InfrastructureHierarchy> criticalInfrastructure = dataManager.load(InfrastructureHierarchy.class)
                .query("select e from InfrastructureHierarchy e where e.criticalityScore >= :score or e.status = :status")
                .parameter("score", 8)
                .parameter("status", InfrastructureStatus.MAINTENANCE)
                .list();

        // Group by infrastructure level
        Map<String, Long> alertsByLevel = criticalInfrastructure.stream()
                .collect(Collectors.groupingBy(
                        infra -> infra.getInfrastructureLevel() != null ? infra.getInfrastructureLevel().getName() : "Unknown",
                        Collectors.counting()
                ));

        alerts.put("totalCritical", criticalInfrastructure.size());
        alerts.put("byLevel", alertsByLevel);
        alerts.put("criticalItems", criticalInfrastructure);

        return alerts;
    }

    /**
     * Get current year budget status
     */
    public Map<String, BigDecimal> getBudgetStatus() {
        Map<String, BigDecimal> budgetStatus = new HashMap<>();

        List<YearlyBudgetPlanningScenario> scenarios = dataManager.load(YearlyBudgetPlanningScenario.class)
                .query("select e from YearlyBudgetPlanningScenario e where e.year = :year")
                .parameter("year", LocalDate.now().getYear())
                .list();

        BigDecimal totalBudget = scenarios.stream()
                .map(YearlyBudgetPlanningScenario::getTotalBudget)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.planningScenario.year = :year")
                .parameter("year", LocalDate.now().getYear())
                .list();

        BigDecimal allocated = plans.stream()
                .map(TacticalPlan::getBudgetAllocated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simulate spent amount (in real app, would track actual expenses)
        BigDecimal spent = allocated.multiply(BigDecimal.valueOf(0.65));

        BigDecimal remaining = totalBudget.subtract(allocated);

        budgetStatus.put("total", totalBudget);
        budgetStatus.put("allocated", allocated);
        budgetStatus.put("spent", spent);
        budgetStatus.put("remaining", remaining);

        return budgetStatus;
    }

    /**
     * Get team utilization data
     */
    public Map<String, Object> getTeamUtilization() {
        Map<String, Object> utilization = new HashMap<>();

        List<Employee> allEmployees = dataManager.load(Employee.class).all().list();
        List<Team> allTeams = dataManager.load(Team.class).all().list();

        // Get assignments for next 30 days
        List<TacticalPlan> upcomingPlans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.plannedStartDate between :start and :end")
                .parameter("start", LocalDate.now())
                .parameter("end", LocalDate.now().plusDays(30))
                .list();

        // Calculate workload per employee
        Map<String, Integer> employeeWorkload = new HashMap<>();
        for (Employee employee : allEmployees) {
            int workloadHours = 0;
            for (TacticalPlan plan : upcomingPlans) {
                if (plan.getAssignedEmployees() != null && plan.getAssignedEmployees().contains(employee)) {
                    Integer duration = plan.getMaintenanceScenario() != null
                            ? plan.getMaintenanceScenario().getEstimatedDuration()
                            : 0;
                    workloadHours += duration != null ? duration : 0;
                }
            }
            employeeWorkload.put(employee.getFirstName() + " " + employee.getLastName(), workloadHours);
        }

        // Calculate team capacity
        Map<String, Map<String, Integer>> teamCapacity = new HashMap<>();
        for (Team team : allTeams) {
            Map<String, Integer> capacity = new HashMap<>();
            long teamSize = allEmployees.stream()
                    .filter(e -> team.equals(e.getTeam()))
                    .count();

            capacity.put("totalEmployees", (int) teamSize);
            capacity.put("availableCapacity", (int) (teamSize * 160)); // 160 hours/month per employee
            capacity.put("allocatedHours", 0); // Calculate from plans

            teamCapacity.put(team.getName(), capacity);
        }

        utilization.put("employeeWorkload", employeeWorkload);
        utilization.put("teamCapacity", teamCapacity);
        utilization.put("totalEmployees", allEmployees.size());

        return utilization;
    }

    /**
     * Get upcoming maintenance activities (next 30 days)
     */
    public List<TacticalPlan> getUpcomingMaintenance(Team filterTeam, TacticalPlanStatus filterStatus) {
        String query = "select e from TacticalPlan e where e.plannedStartDate between :start and :end";

        List<String> conditions = new ArrayList<>();
        if (filterTeam != null) {
            conditions.add("e.assignedTeam = :team");
        }
        if (filterStatus != null) {
            conditions.add("e.status = :status");
        }

        if (!conditions.isEmpty()) {
            query += " and " + String.join(" and ", conditions);
        }

        query += " order by e.plannedStartDate";

        var loader = dataManager.load(TacticalPlan.class)
                .query(query)
                .parameter("start", LocalDate.now())
                .parameter("end", LocalDate.now().plusDays(30));

        if (filterTeam != null) {
            loader.parameter("team", filterTeam);
        }
        if (filterStatus != null) {
            loader.parameter("status", filterStatus);
        }

        return loader.list();
    }

    /**
     * Get all teams for filtering
     */
    public List<Team> getAllTeams() {
        return dataManager.load(Team.class).all().list();
    }
}
