package com.company.experiments.controller;

import com.company.experiments.entity.*;
import com.company.experiments.service.PlanningOptimizationService;
import io.jmix.core.DataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * REST Controller for planning optimization and AI service integration.
 * Provides endpoints for external ML model integration and optimization services.
 */
@RestController
@RequestMapping("/api/optimization")
public class OptimizationController {

    private static final Logger log = LoggerFactory.getLogger(OptimizationController.class);

    private final PlanningOptimizationService optimizationService;
    private final DataManager dataManager;

    public OptimizationController(PlanningOptimizationService optimizationService, DataManager dataManager) {
        this.optimizationService = optimizationService;
        this.dataManager = dataManager;
    }

    /**
     * Generate optimal tactical plan for a scenario
     * POST /api/optimization/generate-plan
     */
    @PostMapping("/generate-plan")
    public ResponseEntity<OptimizationResponse> generateOptimalPlan(@RequestBody OptimizationRequest request) {
        log.info("Received request to generate optimal plan for scenario ID: {}", request.getScenarioId());

        try {
            YearlyBudgetPlanningScenario scenario = dataManager.load(YearlyBudgetPlanningScenario.class)
                    .id(UUID.fromString(request.getScenarioId()))
                    .one();

            List<TacticalPlan> plans = optimizationService.generateOptimalPlan(scenario);

            OptimizationResponse response = new OptimizationResponse();
            response.setSuccess(true);
            response.setMessage("Generated " + plans.size() + " optimal tactical plans");
            response.setPlansGenerated(plans.size());
            response.setTotalBudgetAllocated(
                    plans.stream()
                            .map(TacticalPlan::getBudgetAllocated)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating optimal plan", e);
            OptimizationResponse response = new OptimizationResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Calculate risk score for infrastructure
     * POST /api/optimization/calculate-risk
     */
    @PostMapping("/calculate-risk")
    public ResponseEntity<RiskScoreResponse> calculateRiskScore(@RequestBody RiskCalculationRequest request) {
        log.info("Calculating risk score for infrastructure ID: {}", request.getInfrastructureId());

        try {
            InfrastructureHierarchy infrastructure = dataManager.load(InfrastructureHierarchy.class)
                    .id(UUID.fromString(request.getInfrastructureId()))
                    .one();

            LocalDate targetDate = request.getTargetDate() != null
                    ? LocalDate.parse(request.getTargetDate())
                    : LocalDate.now();

            BigDecimal riskScore = optimizationService.calculateRiskScore(infrastructure, targetDate);

            RiskScoreResponse response = new RiskScoreResponse();
            response.setInfrastructureId(request.getInfrastructureId());
            response.setInfrastructureName(infrastructure.getName());
            response.setRiskScore(riskScore);
            response.setRiskLevel(getRiskLevel(riskScore));
            response.setCalculatedAt(LocalDate.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating risk score", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Optimize resource allocation
     * POST /api/optimization/allocate-resources
     */
    @PostMapping("/allocate-resources")
    public ResponseEntity<ResourceAllocationResponse> optimizeResourceAllocation(
            @RequestBody ResourceAllocationRequest request
    ) {
        log.info("Optimizing resource allocation for {} plans", request.getPlanIds().size());

        try {
            List<TacticalPlan> plans = dataManager.load(TacticalPlan.class)
                    .query("select e from TacticalPlan e where e.id in :ids")
                    .parameter("ids", request.getPlanIds().stream()
                            .map(UUID::fromString)
                            .collect(java.util.stream.Collectors.toList()))
                    .list();

            List<Employee> employees = dataManager.load(Employee.class).all().list();

            Map<TacticalPlan, List<Employee>> allocation = optimizationService.optimizeResourceAllocation(plans, employees);

            ResourceAllocationResponse response = new ResourceAllocationResponse();
            response.setSuccess(true);
            response.setMessage("Resource allocation optimized");
            response.setAllocations(convertToAllocationMap(allocation));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error optimizing resource allocation", e);
            ResourceAllocationResponse response = new ResourceAllocationResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Balance budget constraints
     * POST /api/optimization/balance-budget
     */
    @PostMapping("/balance-budget")
    public ResponseEntity<BudgetBalanceResponse> balanceBudgetConstraints(@RequestBody BudgetBalanceRequest request) {
        log.info("Balancing budget for scenario ID: {}", request.getScenarioId());

        try {
            YearlyBudgetPlanningScenario scenario = dataManager.load(YearlyBudgetPlanningScenario.class)
                    .id(UUID.fromString(request.getScenarioId()))
                    .one();

            Boolean balanced = optimizationService.balanceBudgetConstraints(scenario);

            BudgetBalanceResponse response = new BudgetBalanceResponse();
            response.setSuccess(true);
            response.setBalanced(balanced);
            response.setMessage(balanced ? "Budget is balanced" : "Budget constraints applied");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error balancing budget", e);
            BudgetBalanceResponse response = new BudgetBalanceResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Placeholder endpoint for external AI/ML model integration
     * POST /api/optimization/ml/predict
     */
    @PostMapping("/ml/predict")
    public ResponseEntity<MLPredictionResponse> predictMaintenanceNeed(@RequestBody MLPredictionRequest request) {
        log.info("ML prediction request received (placeholder implementation)");

        // Placeholder for future ML model integration
        MLPredictionResponse response = new MLPredictionResponse();
        response.setSuccess(true);
        response.setMessage("ML prediction endpoint - ready for integration");
        response.setPredictionAvailable(false);
        response.setNote("This endpoint is a placeholder for future ML model integration");

        return ResponseEntity.ok(response);
    }

    /**
     * Placeholder endpoint for anomaly detection
     * POST /api/optimization/ml/anomaly-detection
     */
    @PostMapping("/ml/anomaly-detection")
    public ResponseEntity<AnomalyDetectionResponse> detectAnomalies(@RequestBody AnomalyDetectionRequest request) {
        log.info("Anomaly detection request received (placeholder implementation)");

        // Placeholder for future anomaly detection integration
        AnomalyDetectionResponse response = new AnomalyDetectionResponse();
        response.setSuccess(true);
        response.setAnomaliesDetected(0);
        response.setMessage("Anomaly detection endpoint - ready for ML integration");

        return ResponseEntity.ok(response);
    }

    // Helper methods and DTOs

    private String getRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "CRITICAL";
        } else if (riskScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return "HIGH";
        } else if (riskScore.compareTo(BigDecimal.valueOf(25)) >= 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private Map<String, List<String>> convertToAllocationMap(Map<TacticalPlan, List<Employee>> allocation) {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<TacticalPlan, List<Employee>> entry : allocation.entrySet()) {
            List<String> employeeNames = entry.getValue().stream()
                    .map(e -> e.getFirstName() + " " + e.getLastName())
                    .collect(java.util.stream.Collectors.toList());
            result.put(entry.getKey().getPlanName(), employeeNames);
        }
        return result;
    }

    // Request/Response DTOs

    public static class OptimizationRequest {
        private String scenarioId;

        public String getScenarioId() {
            return scenarioId;
        }

        public void setScenarioId(String scenarioId) {
            this.scenarioId = scenarioId;
        }
    }

    public static class OptimizationResponse {
        private boolean success;
        private String message;
        private int plansGenerated;
        private BigDecimal totalBudgetAllocated;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getPlansGenerated() {
            return plansGenerated;
        }

        public void setPlansGenerated(int plansGenerated) {
            this.plansGenerated = plansGenerated;
        }

        public BigDecimal getTotalBudgetAllocated() {
            return totalBudgetAllocated;
        }

        public void setTotalBudgetAllocated(BigDecimal totalBudgetAllocated) {
            this.totalBudgetAllocated = totalBudgetAllocated;
        }
    }

    public static class RiskCalculationRequest {
        private String infrastructureId;
        private String targetDate;

        public String getInfrastructureId() {
            return infrastructureId;
        }

        public void setInfrastructureId(String infrastructureId) {
            this.infrastructureId = infrastructureId;
        }

        public String getTargetDate() {
            return targetDate;
        }

        public void setTargetDate(String targetDate) {
            this.targetDate = targetDate;
        }
    }

    public static class RiskScoreResponse {
        private String infrastructureId;
        private String infrastructureName;
        private BigDecimal riskScore;
        private String riskLevel;
        private String calculatedAt;

        public String getInfrastructureId() {
            return infrastructureId;
        }

        public void setInfrastructureId(String infrastructureId) {
            this.infrastructureId = infrastructureId;
        }

        public String getInfrastructureName() {
            return infrastructureName;
        }

        public void setInfrastructureName(String infrastructureName) {
            this.infrastructureName = infrastructureName;
        }

        public BigDecimal getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(BigDecimal riskScore) {
            this.riskScore = riskScore;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getCalculatedAt() {
            return calculatedAt;
        }

        public void setCalculatedAt(String calculatedAt) {
            this.calculatedAt = calculatedAt;
        }
    }

    public static class ResourceAllocationRequest {
        private List<String> planIds;

        public List<String> getPlanIds() {
            return planIds;
        }

        public void setPlanIds(List<String> planIds) {
            this.planIds = planIds;
        }
    }

    public static class ResourceAllocationResponse {
        private boolean success;
        private String message;
        private Map<String, List<String>> allocations;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, List<String>> getAllocations() {
            return allocations;
        }

        public void setAllocations(Map<String, List<String>> allocations) {
            this.allocations = allocations;
        }
    }

    public static class BudgetBalanceRequest {
        private String scenarioId;

        public String getScenarioId() {
            return scenarioId;
        }

        public void setScenarioId(String scenarioId) {
            this.scenarioId = scenarioId;
        }
    }

    public static class BudgetBalanceResponse {
        private boolean success;
        private boolean balanced;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isBalanced() {
            return balanced;
        }

        public void setBalanced(boolean balanced) {
            this.balanced = balanced;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class MLPredictionRequest {
        private String infrastructureId;
        private Map<String, Object> features;

        public String getInfrastructureId() {
            return infrastructureId;
        }

        public void setInfrastructureId(String infrastructureId) {
            this.infrastructureId = infrastructureId;
        }

        public Map<String, Object> getFeatures() {
            return features;
        }

        public void setFeatures(Map<String, Object> features) {
            this.features = features;
        }
    }

    public static class MLPredictionResponse {
        private boolean success;
        private String message;
        private boolean predictionAvailable;
        private String note;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isPredictionAvailable() {
            return predictionAvailable;
        }

        public void setPredictionAvailable(boolean predictionAvailable) {
            this.predictionAvailable = predictionAvailable;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    public static class AnomalyDetectionRequest {
        private String infrastructureId;
        private String startDate;
        private String endDate;

        public String getInfrastructureId() {
            return infrastructureId;
        }

        public void setInfrastructureId(String infrastructureId) {
            this.infrastructureId = infrastructureId;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class AnomalyDetectionResponse {
        private boolean success;
        private int anomaliesDetected;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getAnomaliesDetected() {
            return anomaliesDetected;
        }

        public void setAnomaliesDetected(int anomaliesDetected) {
            this.anomaliesDetected = anomaliesDetected;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
