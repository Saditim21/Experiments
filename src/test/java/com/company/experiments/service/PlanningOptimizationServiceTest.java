package com.company.experiments.service;

import com.company.experiments.entity.*;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningOptimizationServiceTest {

    @Mock
    private DataManager dataManager;

    @InjectMocks
    private PlanningOptimizationServiceImpl optimizationService;

    private YearlyBudgetPlanningScenario testScenario;
    private InfrastructureHierarchy testInfrastructure;
    private InfrastructureLevel testLevel;
    private TheoreticalMaintenanceScenario testMaintenanceScenario;
    private TacticalPlan testPlan;
    private Employee testEmployee;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        // Create test infrastructure level
        testLevel = new InfrastructureLevel();
        testLevel.setId(UUID.randomUUID());
        testLevel.setName("Test Level");
        testLevel.setLevel(1);

        // Create test infrastructure
        testInfrastructure = new InfrastructureHierarchy();
        testInfrastructure.setId(UUID.randomUUID());
        testInfrastructure.setName("Test Infrastructure");
        testInfrastructure.setCode("TEST-001");
        testInfrastructure.setInfrastructureLevel(testLevel);
        testInfrastructure.setCriticalityScore(8);
        testInfrastructure.setInstallationDate(LocalDate.now().minusYears(5));
        testInfrastructure.setStatus(InfrastructureStatus.ACTIVE);

        // Create test scenario
        testScenario = new YearlyBudgetPlanningScenario();
        testScenario.setId(UUID.randomUUID());
        testScenario.setScenarioName("Test Scenario 2024");
        testScenario.setYear(2024);
        testScenario.setTotalBudget(BigDecimal.valueOf(100000));
        testScenario.setOptimizationTarget(OptimizationTarget.BALANCED);

        // Create test maintenance scenario
        testMaintenanceScenario = new TheoreticalMaintenanceScenario();
        testMaintenanceScenario.setId(UUID.randomUUID());
        testMaintenanceScenario.setScenarioName("Preventive Maintenance");
        testMaintenanceScenario.setScenarioCode("PM-001");
        testMaintenanceScenario.setInfrastructureNode(testInfrastructure);
        testMaintenanceScenario.setMaintenanceType(MaintenanceType.PREVENTIVE);
        testMaintenanceScenario.setEstimatedCost(BigDecimal.valueOf(5000));
        testMaintenanceScenario.setEstimatedDuration(48);
        testMaintenanceScenario.setPriority(8);

        // Create test team
        testTeam = new Team();
        testTeam.setId(UUID.randomUUID());
        testTeam.setName("Maintenance Team A");
        testTeam.setCode("TEAM-A");

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setId(UUID.randomUUID());
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@test.com");
        testEmployee.setEmployeeCode("EMP001");
        testEmployee.setTeam(testTeam);
        testEmployee.setRole(EmployeeRole.MAINTENANCE_TECHNICIAN);
        testEmployee.setHireDate(LocalDate.now().minusYears(2));

        // Create test tactical plan
        testPlan = new TacticalPlan();
        testPlan.setId(UUID.randomUUID());
        testPlan.setPlanName("Test Plan");
        testPlan.setPlanningScenario(testScenario);
        testPlan.setMaintenanceScenario(testMaintenanceScenario);
        testPlan.setPlannedStartDate(LocalDate.now().plusDays(10));
        testPlan.setPlannedEndDate(LocalDate.now().plusDays(12));
        testPlan.setBudgetAllocated(BigDecimal.valueOf(5000));
        testPlan.setStatus(TacticalPlanStatus.PLANNED);
    }

    @Test
    void testCalculateRiskScore_WithAllFactors() {
        // When
        BigDecimal riskScore = optimizationService.calculateRiskScore(testInfrastructure, LocalDate.now());

        // Then
        assertNotNull(riskScore);
        assertTrue(riskScore.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(riskScore.compareTo(BigDecimal.valueOf(100)) <= 0);

        // Risk should be elevated due to age (5 years) and high criticality (8)
        assertTrue(riskScore.compareTo(BigDecimal.valueOf(40)) > 0,
                "Risk score should be > 40 for 5-year-old critical infrastructure");
    }

    @Test
    void testCalculateRiskScore_NewInfrastructure() {
        // Given
        testInfrastructure.setInstallationDate(LocalDate.now());
        testInfrastructure.setCriticalityScore(3);

        // When
        BigDecimal riskScore = optimizationService.calculateRiskScore(testInfrastructure, LocalDate.now());

        // Then
        assertTrue(riskScore.compareTo(BigDecimal.valueOf(30)) < 0,
                "New infrastructure with low criticality should have low risk");
    }

    @Test
    void testCalculateRiskScore_InactiveStatus() {
        // Given
        testInfrastructure.setStatus(InfrastructureStatus.INACTIVE);

        // When
        BigDecimal riskScore = optimizationService.calculateRiskScore(testInfrastructure, LocalDate.now());

        // Then
        assertTrue(riskScore.compareTo(BigDecimal.valueOf(50)) > 0,
                "Inactive infrastructure should have elevated risk");
    }

    @Test
    void testGenerateOptimalPlan_WithinBudget() {
        // Given
        when(dataManager.load(TheoreticalMaintenanceScenario.class)).thenReturn(
                new DataManager.FluentValueLoader<>(dataManager, TheoreticalMaintenanceScenario.class) {
                    @Override
                    public List<TheoreticalMaintenanceScenario> list() {
                        return Arrays.asList(testMaintenanceScenario);
                    }
                }
        );

        when(dataManager.create(TacticalPlan.class)).thenReturn(new TacticalPlan());

        // When
        List<TacticalPlan> plans = optimizationService.generateOptimalPlan(testScenario);

        // Then
        assertNotNull(plans);
        assertTrue(plans.size() > 0);

        BigDecimal totalCost = plans.stream()
                .map(TacticalPlan::getBudgetAllocated)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertTrue(totalCost.compareTo(testScenario.getTotalBudget()) <= 0,
                "Total allocated budget should not exceed scenario budget");
    }

    @Test
    void testOptimizeResourceAllocation_LoadBalancing() {
        // Given
        List<TacticalPlan> plans = Arrays.asList(testPlan);
        List<Employee> employees = Arrays.asList(testEmployee);

        // When
        Map<TacticalPlan, List<Employee>> allocation = optimizationService.optimizeResourceAllocation(plans, employees);

        // Then
        assertNotNull(allocation);
        assertEquals(1, allocation.size());
        assertTrue(allocation.containsKey(testPlan));
        assertFalse(allocation.get(testPlan).isEmpty());
    }

    @Test
    void testOptimizeResourceAllocation_MultipleEmployees() {
        // Given
        Employee employee2 = new Employee();
        employee2.setId(UUID.randomUUID());
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@test.com");
        employee2.setEmployeeCode("EMP002");
        employee2.setTeam(testTeam);
        employee2.setRole(EmployeeRole.MAINTENANCE_TECHNICIAN);
        employee2.setHireDate(LocalDate.now().minusYears(1));

        List<TacticalPlan> plans = Arrays.asList(testPlan);
        List<Employee> employees = Arrays.asList(testEmployee, employee2);

        // When
        Map<TacticalPlan, List<Employee>> allocation = optimizationService.optimizeResourceAllocation(plans, employees);

        // Then
        assertNotNull(allocation);
        List<Employee> assignedEmployees = allocation.get(testPlan);
        assertNotNull(assignedEmployees);
        assertTrue(assignedEmployees.size() <= 3, "Should not assign more than 3 employees per plan");
    }

    @Test
    void testBalanceBudgetConstraints_UnderBudget() {
        // Given
        when(dataManager.load(TacticalPlan.class))
                .thenReturn(new DataManager.FluentValueLoader<>(dataManager, TacticalPlan.class) {
                    @Override
                    public List<TacticalPlan> list() {
                        testPlan.setBudgetAllocated(BigDecimal.valueOf(40000));
                        return Arrays.asList(testPlan);
                    }
                });

        // When
        Boolean balanced = optimizationService.balanceBudgetConstraints(testScenario);

        // Then
        assertTrue(balanced);
    }

    @Test
    void testBalanceBudgetConstraints_OverBudget() {
        // Given
        TacticalPlan plan1 = new TacticalPlan();
        plan1.setBudgetAllocated(BigDecimal.valueOf(60000));

        TacticalPlan plan2 = new TacticalPlan();
        plan2.setBudgetAllocated(BigDecimal.valueOf(50000));

        when(dataManager.load(TacticalPlan.class))
                .thenReturn(new DataManager.FluentValueLoader<>(dataManager, TacticalPlan.class) {
                    @Override
                    public List<TacticalPlan> list() {
                        return Arrays.asList(plan1, plan2);
                    }
                });

        when(dataManager.save(any(TacticalPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Boolean balanced = optimizationService.balanceBudgetConstraints(testScenario);

        // Then
        assertTrue(balanced);
        verify(dataManager, atLeastOnce()).save(any(TacticalPlan.class));
    }

    @Test
    void testPerformCriticalPathAnalysis() {
        // Given
        TheoreticalMaintenanceScenario scenario2 = new TheoreticalMaintenanceScenario();
        scenario2.setId(UUID.randomUUID());
        scenario2.setScenarioName("Corrective Maintenance");
        scenario2.setInfrastructureNode(testInfrastructure);
        scenario2.setPriority(5);

        List<TheoreticalMaintenanceScenario> scenarios = Arrays.asList(testMaintenanceScenario, scenario2);

        // When
        Map<TheoreticalMaintenanceScenario, Integer> criticalPath =
                optimizationService.performCriticalPathAnalysis(scenarios);

        // Then
        assertNotNull(criticalPath);
        assertEquals(2, criticalPath.size());

        // Higher priority should come first
        assertTrue(criticalPath.get(testMaintenanceScenario) < criticalPath.get(scenario2),
                "Higher priority scenario should have lower order number");
    }

    @Test
    void testApplyResourceLeveling() {
        // Given
        TacticalPlan plan1 = new TacticalPlan();
        plan1.setPlannedStartDate(LocalDate.now());
        plan1.setPlannedEndDate(LocalDate.now().plusDays(5));
        plan1.setPlanName("Plan 1");
        plan1.setMaintenanceScenario(testMaintenanceScenario);

        TacticalPlan plan2 = new TacticalPlan();
        plan2.setPlannedStartDate(LocalDate.now().plusDays(1));
        plan2.setPlannedEndDate(LocalDate.now().plusDays(6));
        plan2.setPlanName("Plan 2");
        plan2.setMaintenanceScenario(testMaintenanceScenario);

        List<TacticalPlan> plans = Arrays.asList(plan1, plan2);

        // When
        List<TacticalPlan> leveledPlans = optimizationService.applyResourceLeveling(plans);

        // Then
        assertNotNull(leveledPlans);
        assertEquals(2, leveledPlans.size());

        // Plans should have valid dates
        for (TacticalPlan plan : leveledPlans) {
            assertNotNull(plan.getPlannedStartDate());
            assertNotNull(plan.getPlannedEndDate());
            assertTrue(plan.getPlannedEndDate().isAfter(plan.getPlannedStartDate()) ||
                    plan.getPlannedEndDate().equals(plan.getPlannedStartDate()));
        }
    }

    @Test
    void testPrioritizeMaintenanceScenarios_CostOptimization() {
        // Given
        TheoreticalMaintenanceScenario expensiveScenario = new TheoreticalMaintenanceScenario();
        expensiveScenario.setId(UUID.randomUUID());
        expensiveScenario.setScenarioName("Expensive Maintenance");
        expensiveScenario.setInfrastructureNode(testInfrastructure);
        expensiveScenario.setPriority(5);
        expensiveScenario.setEstimatedCost(BigDecimal.valueOf(20000));

        testMaintenanceScenario.setEstimatedCost(BigDecimal.valueOf(5000));

        List<TheoreticalMaintenanceScenario> scenarios = Arrays.asList(testMaintenanceScenario, expensiveScenario);

        // When
        List<TheoreticalMaintenanceScenario> prioritized =
                optimizationService.prioritizeMaintenanceScenarios(scenarios, OptimizationTarget.COST);

        // Then
        assertNotNull(prioritized);
        assertEquals(2, prioritized.size());
    }

    @Test
    void testPrioritizeMaintenanceScenarios_RiskOptimization() {
        // Given
        InfrastructureHierarchy oldInfra = new InfrastructureHierarchy();
        oldInfra.setId(UUID.randomUUID());
        oldInfra.setName("Old Infrastructure");
        oldInfra.setCode("OLD-001");
        oldInfra.setInfrastructureLevel(testLevel);
        oldInfra.setCriticalityScore(9);
        oldInfra.setInstallationDate(LocalDate.now().minusYears(15));
        oldInfra.setStatus(InfrastructureStatus.MAINTENANCE);

        TheoreticalMaintenanceScenario riskyCenario = new TheoreticalMaintenanceScenario();
        riskyCenario.setId(UUID.randomUUID());
        riskyCenario.setScenarioName("High Risk Maintenance");
        riskyCenario.setInfrastructureNode(oldInfra);
        riskyCenario.setPriority(9);

        List<TheoreticalMaintenanceScenario> scenarios = Arrays.asList(testMaintenanceScenario, riskyCenario);

        // When
        List<TheoreticalMaintenanceScenario> prioritized =
                optimizationService.prioritizeMaintenanceScenarios(scenarios, OptimizationTarget.RISK);

        // Then
        assertNotNull(prioritized);
        assertEquals(2, prioritized.size());
        // High risk scenario should be prioritized
        assertEquals(riskyCenario, prioritized.get(0));
    }

    @Test
    void testPrioritizeMaintenanceScenarios_UptimeOptimization() {
        // Given
        testInfrastructure.setCriticalityScore(10);

        InfrastructureHierarchy lowCriticalityInfra = new InfrastructureHierarchy();
        lowCriticalityInfra.setId(UUID.randomUUID());
        lowCriticalityInfra.setName("Non-Critical Infrastructure");
        lowCriticalityInfra.setCode("NC-001");
        lowCriticalityInfra.setInfrastructureLevel(testLevel);
        lowCriticalityInfra.setCriticalityScore(3);
        lowCriticalityInfra.setInstallationDate(LocalDate.now().minusYears(2));
        lowCriticalityInfra.setStatus(InfrastructureStatus.ACTIVE);

        TheoreticalMaintenanceScenario lowCriticalityScenario = new TheoreticalMaintenanceScenario();
        lowCriticalityScenario.setId(UUID.randomUUID());
        lowCriticalityScenario.setScenarioName("Low Priority Maintenance");
        lowCriticalityScenario.setInfrastructureNode(lowCriticalityInfra);
        lowCriticalityScenario.setPriority(3);

        List<TheoreticalMaintenanceScenario> scenarios = Arrays.asList(testMaintenanceScenario, lowCriticalityScenario);

        // When
        List<TheoreticalMaintenanceScenario> prioritized =
                optimizationService.prioritizeMaintenanceScenarios(scenarios, OptimizationTarget.UPTIME);

        // Then
        assertNotNull(prioritized);
        assertEquals(2, prioritized.size());
        // High criticality scenario should be first
        assertEquals(testMaintenanceScenario, prioritized.get(0));
    }
}
