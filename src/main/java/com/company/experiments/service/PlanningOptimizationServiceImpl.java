package com.company.experiments.service;

import com.company.experiments.entity.*;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service("PlanningOptimizationService")
public class PlanningOptimizationServiceImpl implements PlanningOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(PlanningOptimizationServiceImpl.class);

    private final DataManager dataManager;

    public PlanningOptimizationServiceImpl(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public List<TacticalPlan> generateOptimalPlan(YearlyBudgetPlanningScenario scenario) {
        log.info("Generating optimal plan for scenario: {}", scenario.getScenarioName());

        // Load all maintenance scenarios
        List<TheoreticalMaintenanceScenario> maintenanceScenarios = dataManager.load(TheoreticalMaintenanceScenario.class)
                .all()
                .list();

        // Prioritize based on optimization target
        List<TheoreticalMaintenanceScenario> prioritized = prioritizeMaintenanceScenarios(
                maintenanceScenarios,
                scenario.getOptimizationTarget()
        );

        // Generate tactical plans within budget constraints
        List<TacticalPlan> plans = new ArrayList<>();
        BigDecimal remainingBudget = scenario.getTotalBudget() != null
                ? scenario.getTotalBudget()
                : BigDecimal.ZERO;

        LocalDate startDate = LocalDate.of(scenario.getYear(), 1, 1);

        for (TheoreticalMaintenanceScenario maintenanceScenario : prioritized) {
            if (remainingBudget.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal estimatedCost = maintenanceScenario.getEstimatedCost() != null
                    ? maintenanceScenario.getEstimatedCost()
                    : BigDecimal.ZERO;

            if (estimatedCost.compareTo(remainingBudget) <= 0) {
                TacticalPlan plan = dataManager.create(TacticalPlan.class);
                plan.setPlanName("Optimized: " + maintenanceScenario.getScenarioName());
                plan.setPlanningScenario(scenario);
                plan.setMaintenanceScenario(maintenanceScenario);
                plan.setBudgetAllocated(estimatedCost);
                plan.setStatus(TacticalPlanStatus.PLANNED);

                // Calculate dates based on frequency
                plan.setPlannedStartDate(startDate);
                Integer duration = maintenanceScenario.getEstimatedDuration() != null
                        ? maintenanceScenario.getEstimatedDuration()
                        : 24;
                plan.setPlannedEndDate(startDate.plusDays(duration / 24 + 1));

                plans.add(plan);
                remainingBudget = remainingBudget.subtract(estimatedCost);

                // Advance start date
                startDate = plan.getPlannedEndDate().plusDays(1);
            }
        }

        // Apply resource leveling
        plans = applyResourceLeveling(plans);

        log.info("Generated {} tactical plans within budget", plans.size());
        return plans;
    }

    @Override
    public BigDecimal calculateRiskScore(InfrastructureHierarchy infrastructure, LocalDate targetDate) {
        log.debug("Calculating risk score for infrastructure: {}", infrastructure.getName());

        BigDecimal riskScore = BigDecimal.ZERO;

        // Factor 1: Age-based risk (0-30 points)
        if (infrastructure.getInstallationDate() != null) {
            long ageInYears = ChronoUnit.YEARS.between(infrastructure.getInstallationDate(), targetDate);
            BigDecimal ageRisk = BigDecimal.valueOf(Math.min(ageInYears * 3, 30));
            riskScore = riskScore.add(ageRisk);
        }

        // Factor 2: Criticality score (0-30 points)
        if (infrastructure.getCriticalityScore() != null) {
            BigDecimal criticalityRisk = BigDecimal.valueOf(infrastructure.getCriticalityScore() * 3);
            riskScore = riskScore.add(criticalityRisk);
        }

        // Factor 3: Status-based risk (0-20 points)
        if (infrastructure.getStatus() != null) {
            switch (infrastructure.getStatus()) {
                case INACTIVE:
                    riskScore = riskScore.add(BigDecimal.valueOf(20));
                    break;
                case MAINTENANCE:
                    riskScore = riskScore.add(BigDecimal.valueOf(10));
                    break;
                case ACTIVE:
                    riskScore = riskScore.add(BigDecimal.valueOf(5));
                    break;
            }
        }

        // Factor 4: Time since last maintenance (0-20 points)
        // Assuming no maintenance for now, add 15 points
        riskScore = riskScore.add(BigDecimal.valueOf(15));

        // Cap at 100
        return riskScore.min(BigDecimal.valueOf(100));
    }

    @Override
    public Map<TacticalPlan, List<Employee>> optimizeResourceAllocation(
            List<TacticalPlan> plans,
            List<Employee> employees
    ) {
        log.info("Optimizing resource allocation for {} plans and {} employees", plans.size(), employees.size());

        Map<TacticalPlan, List<Employee>> allocation = new HashMap<>();
        Map<Employee, Integer> employeeWorkload = new HashMap<>();

        // Initialize workload tracking
        for (Employee employee : employees) {
            employeeWorkload.put(employee, 0);
        }

        // Sort plans by priority (using estimated duration as proxy)
        List<TacticalPlan> sortedPlans = plans.stream()
                .sorted((p1, p2) -> {
                    Integer duration1 = p1.getMaintenanceScenario().getEstimatedDuration() != null
                            ? p1.getMaintenanceScenario().getEstimatedDuration()
                            : 0;
                    Integer duration2 = p2.getMaintenanceScenario().getEstimatedDuration() != null
                            ? p2.getMaintenanceScenario().getEstimatedDuration()
                            : 0;
                    return duration2.compareTo(duration1); // Descending
                })
                .collect(Collectors.toList());

        // Allocate employees to plans using round-robin with load balancing
        for (TacticalPlan plan : sortedPlans) {
            // Determine number of employees needed (1-3 based on duration)
            Integer duration = plan.getMaintenanceScenario().getEstimatedDuration() != null
                    ? plan.getMaintenanceScenario().getEstimatedDuration()
                    : 24;
            int employeesNeeded = Math.min(3, Math.max(1, duration / 40));

            // Select employees with lowest workload from same team if possible
            List<Employee> assigned = employees.stream()
                    .filter(e -> plan.getAssignedTeam() == null || e.getTeam() == null ||
                            e.getTeam().equals(plan.getAssignedTeam()))
                    .sorted(Comparator.comparing(employeeWorkload::get))
                    .limit(employeesNeeded)
                    .collect(Collectors.toList());

            // If not enough from same team, add from other teams
            if (assigned.size() < employeesNeeded) {
                assigned.addAll(employees.stream()
                        .filter(e -> !assigned.contains(e))
                        .sorted(Comparator.comparing(employeeWorkload::get))
                        .limit(employeesNeeded - assigned.size())
                        .collect(Collectors.toList()));
            }

            // Update workload
            for (Employee employee : assigned) {
                employeeWorkload.put(employee, employeeWorkload.get(employee) + duration);
            }

            allocation.put(plan, assigned);
        }

        log.info("Resource allocation completed with {} assignments", allocation.size());
        return allocation;
    }

    @Override
    public Boolean balanceBudgetConstraints(YearlyBudgetPlanningScenario scenario) {
        log.info("Balancing budget constraints for scenario: {}", scenario.getScenarioName());

        // Load all tactical plans for this scenario
        List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                .query("select e from TacticalPlan e where e.planningScenario = :scenario")
                .parameter("scenario", scenario)
                .list();

        BigDecimal totalAllocated = plans.stream()
                .map(TacticalPlan::getBudgetAllocated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBudget = scenario.getTotalBudget() != null
                ? scenario.getTotalBudget()
                : BigDecimal.ZERO;

        if (totalAllocated.compareTo(totalBudget) <= 0) {
            log.info("Budget is balanced: {} <= {}", totalAllocated, totalBudget);
            return true;
        }

        log.warn("Budget exceeded: {} > {}. Adjusting...", totalAllocated, totalBudget);

        // Scale down allocations proportionally
        BigDecimal scaleFactor = totalBudget.divide(totalAllocated, 4, RoundingMode.HALF_UP);

        for (TacticalPlan plan : plans) {
            if (plan.getBudgetAllocated() != null) {
                BigDecimal adjusted = plan.getBudgetAllocated().multiply(scaleFactor);
                plan.setBudgetAllocated(adjusted);
                dataManager.save(plan);
            }
        }

        log.info("Budget balanced by scaling down allocations by factor: {}", scaleFactor);
        return true;
    }

    @Override
    public Map<TheoreticalMaintenanceScenario, Integer> performCriticalPathAnalysis(
            List<TheoreticalMaintenanceScenario> scenarios
    ) {
        log.info("Performing critical path analysis on {} scenarios", scenarios.size());

        Map<TheoreticalMaintenanceScenario, Integer> criticalPath = new HashMap<>();

        // Sort by priority and criticality
        List<TheoreticalMaintenanceScenario> sorted = scenarios.stream()
                .sorted((s1, s2) -> {
                    Integer p1 = s1.getPriority() != null ? s1.getPriority() : 5;
                    Integer p2 = s2.getPriority() != null ? s2.getPriority() : 5;

                    int priorityCompare = p2.compareTo(p1); // Higher priority first
                    if (priorityCompare != 0) return priorityCompare;

                    // Then by criticality score
                    Integer c1 = s1.getInfrastructureNode().getCriticalityScore() != null
                            ? s1.getInfrastructureNode().getCriticalityScore()
                            : 5;
                    Integer c2 = s2.getInfrastructureNode().getCriticalityScore() != null
                            ? s2.getInfrastructureNode().getCriticalityScore()
                            : 5;

                    return c2.compareTo(c1); // Higher criticality first
                })
                .collect(Collectors.toList());

        // Assign priority order
        for (int i = 0; i < sorted.size(); i++) {
            criticalPath.put(sorted.get(i), i + 1);
        }

        log.info("Critical path analysis complete with {} ordered scenarios", criticalPath.size());
        return criticalPath;
    }

    @Override
    public List<TacticalPlan> applyResourceLeveling(List<TacticalPlan> plans) {
        log.info("Applying resource leveling to {} plans", plans.size());

        if (plans.isEmpty()) {
            return plans;
        }

        // Sort by planned start date
        List<TacticalPlan> sorted = plans.stream()
                .filter(p -> p.getPlannedStartDate() != null)
                .sorted(Comparator.comparing(TacticalPlan::getPlannedStartDate))
                .collect(Collectors.toList());

        // Track resource usage by date
        Map<LocalDate, Integer> resourceUsageByDate = new HashMap<>();

        final int MAX_CONCURRENT_PLANS = 3; // Maximum concurrent plans

        for (TacticalPlan plan : sorted) {
            LocalDate startDate = plan.getPlannedStartDate();
            LocalDate endDate = plan.getPlannedEndDate();

            if (startDate == null || endDate == null) {
                continue;
            }

            // Check if resources are available
            boolean needsRescheduling = false;
            LocalDate checkDate = startDate;

            while (!checkDate.isAfter(endDate)) {
                int usage = resourceUsageByDate.getOrDefault(checkDate, 0);
                if (usage >= MAX_CONCURRENT_PLANS) {
                    needsRescheduling = true;
                    break;
                }
                checkDate = checkDate.plusDays(1);
            }

            // Reschedule if needed
            if (needsRescheduling) {
                // Find next available slot
                LocalDate newStartDate = findNextAvailableSlot(
                        resourceUsageByDate,
                        startDate,
                        ChronoUnit.DAYS.between(startDate, endDate),
                        MAX_CONCURRENT_PLANS
                );

                long duration = ChronoUnit.DAYS.between(startDate, endDate);
                plan.setPlannedStartDate(newStartDate);
                plan.setPlannedEndDate(newStartDate.plusDays(duration));

                startDate = newStartDate;
                endDate = plan.getPlannedEndDate();

                log.debug("Rescheduled plan {} to {}", plan.getPlanName(), newStartDate);
            }

            // Mark resources as used
            checkDate = startDate;
            while (!checkDate.isAfter(endDate)) {
                resourceUsageByDate.merge(checkDate, 1, Integer::sum);
                checkDate = checkDate.plusDays(1);
            }
        }

        log.info("Resource leveling complete. Adjusted {} plans", sorted.size());
        return plans;
    }

    @Override
    public List<TheoreticalMaintenanceScenario> prioritizeMaintenanceScenarios(
            List<TheoreticalMaintenanceScenario> scenarios,
            OptimizationTarget optimizationTarget
    ) {
        log.info("Prioritizing {} scenarios with target: {}", scenarios.size(), optimizationTarget);

        return scenarios.stream()
                .sorted((s1, s2) -> {
                    double score1 = calculatePriorityScore(s1, optimizationTarget);
                    double score2 = calculatePriorityScore(s2, optimizationTarget);
                    return Double.compare(score2, score1); // Higher score first
                })
                .collect(Collectors.toList());
    }

    private double calculatePriorityScore(
            TheoreticalMaintenanceScenario scenario,
            OptimizationTarget target
    ) {
        double score = 0;

        // Base priority (0-10)
        Integer priority = scenario.getPriority() != null ? scenario.getPriority() : 5;
        score += priority * 10;

        // Criticality (0-10)
        Integer criticality = scenario.getInfrastructureNode().getCriticalityScore() != null
                ? scenario.getInfrastructureNode().getCriticalityScore()
                : 5;
        score += criticality * 10;

        // Risk score
        BigDecimal riskScore = calculateRiskScore(scenario.getInfrastructureNode(), LocalDate.now());
        score += riskScore.doubleValue();

        // Adjust based on optimization target
        if (target != null) {
            switch (target) {
                case COST:
                    // Penalize expensive scenarios
                    BigDecimal cost = scenario.getEstimatedCost() != null
                            ? scenario.getEstimatedCost()
                            : BigDecimal.ZERO;
                    score -= cost.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).doubleValue();
                    break;

                case RISK:
                    // Boost risk score weight
                    score += riskScore.doubleValue() * 0.5;
                    break;

                case UPTIME:
                    // Boost criticality weight
                    score += criticality * 5;
                    break;

                case BALANCED:
                    // Already balanced
                    break;
            }
        }

        return score;
    }

    private LocalDate findNextAvailableSlot(
            Map<LocalDate, Integer> resourceUsageByDate,
            LocalDate startSearchDate,
            long durationDays,
            int maxConcurrent
    ) {
        LocalDate candidateDate = startSearchDate;

        while (true) {
            boolean available = true;
            LocalDate checkDate = candidateDate;

            for (int i = 0; i <= durationDays; i++) {
                int usage = resourceUsageByDate.getOrDefault(checkDate, 0);
                if (usage >= maxConcurrent) {
                    available = false;
                    break;
                }
                checkDate = checkDate.plusDays(1);
            }

            if (available) {
                return candidateDate;
            }

            candidateDate = candidateDate.plusDays(1);

            // Safety: don't search more than 365 days ahead
            if (ChronoUnit.DAYS.between(startSearchDate, candidateDate) > 365) {
                return startSearchDate.plusYears(1);
            }
        }
    }
}
