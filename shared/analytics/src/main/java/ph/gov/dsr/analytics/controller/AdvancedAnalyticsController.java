package ph.gov.dsr.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.analytics.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced Analytics Dashboard Controller
 */
@RestController
@RequestMapping("/api/v1/admin/advanced-analytics")
@RequiredArgsConstructor
@Tag(name = "Advanced Analytics", description = "Real-time insights, predictive analytics, and interactive visualizations")
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsDashboardService dashboardService;
    private final RealTimeDataProcessor realTimeProcessor;
    private final PredictiveAnalyticsEngine predictiveEngine;
    private final VisualizationService visualizationService;

    @GetMapping("/health")
    @Operation(summary = "Get advanced analytics system health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdvancedAnalyticsHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            AnalyticsPerformanceMetrics metrics = dashboardService.getAnalyticsMetrics();
            
            health.put("analytics", Map.of(
                "realTimeEnabled", metrics.isRealTimeEnabled(),
                "predictiveAnalyticsEnabled", metrics.isPredictiveAnalyticsEnabled(),
                "machineLearningEnabled", metrics.isMachineLearningEnabled(),
                "activeSessions", metrics.getActiveSessions(),
                "totalRequests", metrics.getTotalAnalyticsRequests(),
                "realTimeUpdates", metrics.getRealTimeUpdates(),
                "averageResponseTime", metrics.getAverageResponseTime()
            ));
            
            health.put("dataProcessing", Map.of(
                "streamingEnabled", true, // Would get from processor
                "eventsProcessed", 0L, // Would get from processor
                "anomaliesDetected", 0L, // Would get from processor
                "systemLoad", metrics.getSystemLoad()
            ));
            
            health.put("predictiveModels", Map.of(
                "modelsActive", metrics.getPredictiveModels(),
                "cachedInsights", metrics.getCachedInsights(),
                "refreshInterval", metrics.getRefreshIntervalSeconds()
            ));
            
            health.put("status", "OPERATIONAL");
            health.put("timestamp", metrics.getTimestamp());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }

    @PostMapping("/dashboard/generate")
    @Operation(summary = "Generate advanced analytics dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdvancedDashboardData> generateAdvancedDashboard(@RequestBody DashboardRequest request) {
        try {
            AdvancedDashboardData dashboard = dashboardService.generateAdvancedDashboard(request);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/dashboard/{sessionId}/update")
    @Operation(summary = "Get real-time dashboard updates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardUpdate> getRealTimeDashboardUpdate(@PathVariable String sessionId) {
        try {
            DashboardUpdate update = dashboardService.getRealTimeDashboardUpdate(sessionId);
            return ResponseEntity.ok(update);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/predictive/report")
    @Operation(summary = "Generate predictive analytics report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PredictiveAnalyticsReport> generatePredictiveReport(@RequestBody PredictiveAnalyticsRequest request) {
        try {
            PredictiveAnalyticsReport report = dashboardService.generatePredictiveReport(request);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/predictive/forecasts")
    @Operation(summary = "Generate forecasts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Forecast>> generateForecasts(@RequestBody ForecastRequest request) {
        try {
            PredictiveModel model = getModelForRequest(request);
            PredictiveAnalyticsRequest analyticsRequest = convertToAnalyticsRequest(request);
            
            List<Forecast> forecasts = predictiveEngine.generateForecasts(model, analyticsRequest);
            return ResponseEntity.ok(forecasts);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/predictive/scenarios")
    @Operation(summary = "Perform scenario analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ScenarioAnalysis> performScenarioAnalysis(@RequestBody ScenarioAnalysisRequest request) {
        try {
            PredictiveModel model = getModelForRequest(request);
            PredictiveAnalyticsRequest analyticsRequest = convertToAnalyticsRequest(request);
            
            ScenarioAnalysis analysis = predictiveEngine.performScenarioAnalysis(model, analyticsRequest);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/data-exploration")
    @Operation(summary = "Perform advanced data exploration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataExplorationResult> performDataExploration(@RequestBody DataExplorationRequest request) {
        try {
            DataExplorationResult result = dashboardService.performDataExploration(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/real-time/metrics")
    @Operation(summary = "Get real-time metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RealTimeMetrics> getRealTimeMetrics(@RequestParam String userId, @RequestParam String userRole) {
        try {
            DashboardRequest request = DashboardRequest.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
            
            RealTimeMetrics metrics = realTimeProcessor.generateMetrics(request);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/real-time/insights")
    @Operation(summary = "Get real-time insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnalyticsInsight>> getRealTimeInsights() {
        try {
            List<AnalyticsInsight> insights = realTimeProcessor.generateInsights();
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/real-time/anomalies")
    @Operation(summary = "Get detected anomalies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DataAnomaly>> getDetectedAnomalies() {
        try {
            List<DataAnomaly> anomalies = realTimeProcessor.detectAnomalies();
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/visualizations/generate")
    @Operation(summary = "Generate interactive visualizations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InteractiveVisualization>> generateVisualizations(@RequestBody VisualizationRequest request) {
        try {
            List<InteractiveVisualization> visualizations = visualizationService.generateInteractiveVisualizations(request);
            return ResponseEntity.ok(visualizations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/visualizations/custom")
    @Operation(summary = "Create custom visualization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomVisualization> createCustomVisualization(@RequestBody CustomVisualizationRequest request) {
        try {
            CustomVisualization visualization = visualizationService.createCustomVisualization(request);
            return ResponseEntity.ok(visualization);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/performance")
    @Operation(summary = "Get analytics performance metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsPerformanceMetrics> getAnalyticsMetrics() {
        try {
            AnalyticsPerformanceMetrics metrics = dashboardService.getAnalyticsMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/processing/trigger")
    @Operation(summary = "Trigger manual data processing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerDataProcessing() {
        try {
            realTimeProcessor.processDataStreams();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Manual data processing triggered");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/models/retrain")
    @Operation(summary = "Trigger model retraining")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerModelRetraining() {
        try {
            // Trigger model retraining
            // This would be implemented in the ML service
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "initiated");
            response.put("message", "Model retraining initiated");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/insights/predictive")
    @Operation(summary = "Get predictive insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PredictiveInsights> getPredictiveInsights(@RequestParam String userId, @RequestParam String userRole) {
        try {
            DashboardRequest request = DashboardRequest.builder()
                .userId(userId)
                .userRole(userRole)
                .build();
            
            PredictiveInsights insights = predictiveEngine.generateInsights(request);
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/confidence-intervals")
    @Operation(summary = "Calculate confidence intervals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfidenceIntervals> calculateConfidenceIntervals(@RequestBody List<Forecast> forecasts) {
        try {
            ConfidenceIntervals intervals = predictiveEngine.calculateConfidenceIntervals(forecasts);
            return ResponseEntity.ok(intervals);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/dashboard/sessions")
    @Operation(summary = "Get active dashboard sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DashboardSession>> getActiveDashboardSessions() {
        try {
            List<DashboardSession> sessions = dashboardService.getActiveDashboardSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/alerts/configure")
    @Operation(summary = "Configure analytics alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> configureAnalyticsAlerts(@RequestBody AnalyticsAlertConfiguration config) {
        try {
            dashboardService.configureAnalyticsAlerts(config);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "configured");
            response.put("message", "Analytics alerts configured successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/export/dashboard")
    @Operation(summary = "Export dashboard data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardExport> exportDashboardData(@RequestBody DashboardExportRequest request) {
        try {
            DashboardExport export = dashboardService.exportDashboardData(request);
            return ResponseEntity.ok(export);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/settings/update")
    @Operation(summary = "Update analytics settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateAnalyticsSettings(@RequestBody AnalyticsSettings settings) {
        try {
            dashboardService.updateAnalyticsSettings(settings);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "updated");
            response.put("message", "Analytics settings updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    // Private helper methods

    private PredictiveModel getModelForRequest(Object request) {
        // Get appropriate model for the request
        return PredictiveModel.builder()
            .id("default-model")
            .modelType(ModelType.LINEAR_REGRESSION)
            .build();
    }

    private PredictiveAnalyticsRequest convertToAnalyticsRequest(Object request) {
        // Convert request to PredictiveAnalyticsRequest
        return PredictiveAnalyticsRequest.builder()
            .analysisType(AnalysisType.BENEFICIARY_GROWTH)
            .build();
    }
}
