package com.company.experiments.service;

import com.company.experiments.entity.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service for optimizing maintenance planning and resource allocation.
 * Provides algorithms for:
 * - Critical path analysis for maintenance scheduling
 * - Resource leveling across teams
 * - Budget optimization with constraint satisfaction
 * - Risk-based prioritization
 */
@Service
public interface PlanningOptimizationService {

    /**
     * Generates an optimal tactical plan based on the yearly budget planning scenario.
     * Uses constraint satisfaction and prioritization algorithms.
     *
     * @param scenario The yearly budget planning scenario
     * @return List of optimized tactical plans
     */
    List<TacticalPlan> generateOptimalPlan(YearlyBudgetPlanningScenario scenario);

    /**
     * Calculates the risk score for a given infrastructure node at a target date.
     * Considers factors like:
     * - Age of infrastructure
     * - Criticality score
     * - Historical failure rates
     * - Time since last maintenance
     *
     * @param infrastructure The infrastructure node to assess
     * @param targetDate The date for risk assessment
     * @return Risk score (0-100, higher = more risk)
     */
    BigDecimal calculateRiskScore(InfrastructureHierarchy infrastructure, LocalDate targetDate);

    /**
     * Optimizes resource allocation by assigning employees to tactical plans.
     * Uses resource leveling algorithms to balance workload.
     *
     * @param plans List of tactical plans needing resources
     * @param employees Available employees
     * @return Map of tactical plans to assigned employees
     */
    Map<TacticalPlan, List<Employee>> optimizeResourceAllocation(
            List<TacticalPlan> plans,
            List<Employee> employees
    );

    /**
     * Balances budget constraints across the scenario.
     * Ensures total allocated budget doesn't exceed available budget.
     *
     * @param scenario The scenario to balance
     * @return true if budget is balanced, false otherwise
     */
    Boolean balanceBudgetConstraints(YearlyBudgetPlanningScenario scenario);

    /**
     * Performs critical path analysis to determine optimal maintenance scheduling.
     * Identifies dependencies and calculates earliest/latest start times.
     *
     * @param scenarios List of maintenance scenarios
     * @return Map of maintenance scenarios to their priority order
     */
    Map<TheoreticalMaintenanceScenario, Integer> performCriticalPathAnalysis(
            List<TheoreticalMaintenanceScenario> scenarios
    );

    /**
     * Applies resource leveling to smooth out resource usage over time.
     * Prevents resource overallocation and underutilization.
     *
     * @param plans List of tactical plans
     * @return Adjusted plans with optimized scheduling
     */
    List<TacticalPlan> applyResourceLeveling(List<TacticalPlan> plans);

    /**
     * Prioritizes maintenance scenarios based on risk, cost, and criticality.
     *
     * @param scenarios List of maintenance scenarios
     * @param optimizationTarget Target for optimization (COST, RISK, UPTIME, BALANCED)
     * @return Sorted list of scenarios by priority
     */
    List<TheoreticalMaintenanceScenario> prioritizeMaintenanceScenarios(
            List<TheoreticalMaintenanceScenario> scenarios,
            OptimizationTarget optimizationTarget
    );
}
