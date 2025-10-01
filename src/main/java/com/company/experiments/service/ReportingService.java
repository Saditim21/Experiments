package com.company.experiments.service;

import com.company.experiments.entity.*;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating various reports
 */
@Service("ReportingService")
public class ReportingService {

    private final DataManager dataManager;
    private final PlanningOptimizationService optimizationService;

    public ReportingService(DataManager dataManager, PlanningOptimizationService optimizationService) {
        this.dataManager = dataManager;
        this.optimizationService = optimizationService;
    }

    /**
     * Generate monthly maintenance report
     */
    public Map<String, Object> generateMonthlyMaintenanceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.plannedStartDate between :start and :end")
                .parameter("start", startDate)
                .parameter("end", endDate)
                .list();

        // Summary statistics
        long totalPlans = plans.size();
        long completedPlans = plans.stream()
                .filter(p -> TacticalPlanStatus.COMPLETED.equals(p.getStatus()))
                .count();
        long inProgressPlans = plans.stream()
                .filter(p -> TacticalPlanStatus.IN_PROGRESS.equals(p.getStatus()))
                .count();
        long plannedPlans = plans.stream()
                .filter(p -> TacticalPlanStatus.PLANNED.equals(p.getStatus()) ||
                             TacticalPlanStatus.SCHEDULED.equals(p.getStatus()))
                .count();

        BigDecimal completionRate = totalPlans > 0
                ? BigDecimal.valueOf(completedPlans).divide(BigDecimal.valueOf(totalPlans), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Group by maintenance type
        Map<MaintenanceType, Long> byType = plans.stream()
                .filter(p -> p.getMaintenanceScenario() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getMaintenanceScenario().getMaintenanceType(),
                        Collectors.counting()
                ));

        // Total budget allocated
        BigDecimal totalBudget = plans.stream()
                .map(TacticalPlan::getBudgetAllocated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("reportTitle", "Monthly Maintenance Report");
        report.put("period", startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("totalPlans", totalPlans);
        report.put("completedPlans", completedPlans);
        report.put("inProgressPlans", inProgressPlans);
        report.put("plannedPlans", plannedPlans);
        report.put("completionRate", completionRate);
        report.put("byMaintenanceType", byType);
        report.put("totalBudget", totalBudget);
        report.put("detailedPlans", plans);

        return report;
    }

    /**
     * Generate budget utilization report
     */
    public Map<String, Object> generateBudgetUtilizationReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        List<YearlyBudgetPlanningScenario> scenarios = dataManager.load(YearlyBudgetPlanningScenario.class)
                .query("select e from YearlyBudgetPlanningScenario e where e.year >= :startYear and e.year <= :endYear")
                .parameter("startYear", startDate.getYear())
                .parameter("endYear", endDate.getYear())
                .list();

        BigDecimal totalBudget = scenarios.stream()
                .map(YearlyBudgetPlanningScenario::getTotalBudget)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.plannedStartDate between :start and :end")
                .parameter("start", startDate)
                .parameter("end", endDate)
                .list();

        BigDecimal totalAllocated = plans.stream()
                .map(TacticalPlan::getBudgetAllocated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simulate spent amount (70% of allocated)
        BigDecimal totalSpent = totalAllocated.multiply(BigDecimal.valueOf(0.7));
        BigDecimal remaining = totalBudget.subtract(totalAllocated);

        BigDecimal utilizationRate = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalAllocated.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        // Budget by optimization target
        Map<OptimizationTarget, BigDecimal> byTarget = scenarios.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getOptimizationTarget() != null ? s.getOptimizationTarget() : OptimizationTarget.BALANCED,
                        Collectors.mapping(
                                YearlyBudgetPlanningScenario::getTotalBudget,
                                Collectors.reducing(BigDecimal.ZERO, (a, b) -> a.add(b != null ? b : BigDecimal.ZERO))
                        )
                ));

        report.put("reportTitle", "Budget Utilization Report");
        report.put("period", startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("totalBudget", totalBudget);
        report.put("totalAllocated", totalAllocated);
        report.put("totalSpent", totalSpent);
        report.put("remaining", remaining);
        report.put("utilizationRate", utilizationRate);
        report.put("byOptimizationTarget", byTarget);
        report.put("scenarios", scenarios);
        report.put("plans", plans);

        return report;
    }

    /**
     * Generate infrastructure health report
     */
    public Map<String, Object> generateInfrastructureHealthReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        List<InfrastructureHierarchy> allInfrastructure = dataManager.load(InfrastructureHierarchy.class)
                .all()
                .list();

        // Calculate risk scores for all infrastructure
        Map<InfrastructureHierarchy, BigDecimal> riskScores = new HashMap<>();
        for (InfrastructureHierarchy infra : allInfrastructure) {
            BigDecimal risk = optimizationService.calculateRiskScore(infra, LocalDate.now());
            riskScores.put(infra, risk);
        }

        // Categorize by risk level
        long critical = riskScores.values().stream()
                .filter(r -> r.compareTo(BigDecimal.valueOf(75)) >= 0)
                .count();
        long high = riskScores.values().stream()
                .filter(r -> r.compareTo(BigDecimal.valueOf(50)) >= 0 && r.compareTo(BigDecimal.valueOf(75)) < 0)
                .count();
        long medium = riskScores.values().stream()
                .filter(r -> r.compareTo(BigDecimal.valueOf(25)) >= 0 && r.compareTo(BigDecimal.valueOf(50)) < 0)
                .count();
        long low = riskScores.values().stream()
                .filter(r -> r.compareTo(BigDecimal.valueOf(25)) < 0)
                .count();

        // Group by status
        Map<InfrastructureStatus, Long> byStatus = allInfrastructure.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getStatus() != null ? i.getStatus() : InfrastructureStatus.ACTIVE,
                        Collectors.counting()
                ));

        // Average criticality score
        BigDecimal avgCriticality = allInfrastructure.stream()
                .map(InfrastructureHierarchy::getCriticalityScore)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allInfrastructure.size()), 2, RoundingMode.HALF_UP);

        // Top 10 highest risk items
        List<Map<String, Object>> topRiskItems = riskScores.entrySet().stream()
                .sorted(Map.Entry.<InfrastructureHierarchy, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey().getName());
                    item.put("code", entry.getKey().getCode());
                    item.put("riskScore", entry.getValue());
                    item.put("criticality", entry.getKey().getCriticalityScore());
                    item.put("status", entry.getKey().getStatus());
                    return item;
                })
                .collect(Collectors.toList());

        report.put("reportTitle", "Infrastructure Health Report");
        report.put("period", startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("totalInfrastructure", allInfrastructure.size());
        report.put("criticalRisk", critical);
        report.put("highRisk", high);
        report.put("mediumRisk", medium);
        report.put("lowRisk", low);
        report.put("byStatus", byStatus);
        report.put("avgCriticality", avgCriticality);
        report.put("topRiskItems", topRiskItems);

        return report;
    }

    /**
     * Generate team performance report
     */
    public Map<String, Object> generateTeamPerformanceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        List<Team> allTeams = dataManager.load(Team.class).all().list();
        List<Employee> allEmployees = dataManager.load(Employee.class).all().list();

        List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.plannedStartDate between :start and :end")
                .parameter("start", startDate)
                .parameter("end", endDate)
                .list();

        // Performance by team
        List<Map<String, Object>> teamPerformance = new ArrayList<>();

        for (Team team : allTeams) {
            Map<String, Object> teamData = new HashMap<>();

            long teamSize = allEmployees.stream()
                    .filter(e -> team.equals(e.getTeam()))
                    .count();

            List<TacticalPlan> teamPlans = plans.stream()
                    .filter(p -> team.equals(p.getAssignedTeam()))
                    .collect(Collectors.toList());

            long completedPlans = teamPlans.stream()
                    .filter(p -> TacticalPlanStatus.COMPLETED.equals(p.getStatus()))
                    .count();

            BigDecimal completionRate = teamPlans.size() > 0
                    ? BigDecimal.valueOf(completedPlans).divide(BigDecimal.valueOf(teamPlans.size()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            BigDecimal totalBudget = teamPlans.stream()
                    .map(TacticalPlan::getBudgetAllocated)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            teamData.put("teamName", team.getName());
            teamData.put("teamCode", team.getCode());
            teamData.put("teamSize", teamSize);
            teamData.put("totalPlans", teamPlans.size());
            teamData.put("completedPlans", completedPlans);
            teamData.put("completionRate", completionRate);
            teamData.put("totalBudget", totalBudget);

            teamPerformance.add(teamData);
        }

        // Overall statistics
        BigDecimal avgCompletionRate = teamPerformance.stream()
                .map(t -> (BigDecimal) t.get("completionRate"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(teamPerformance.size()), 2, RoundingMode.HALF_UP);

        report.put("reportTitle", "Team Performance Report");
        report.put("period", startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) +
                            " - " + endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        report.put("totalTeams", allTeams.size());
        report.put("totalEmployees", allEmployees.size());
        report.put("avgCompletionRate", avgCompletionRate);
        report.put("teamPerformance", teamPerformance);

        return report;
    }
}
