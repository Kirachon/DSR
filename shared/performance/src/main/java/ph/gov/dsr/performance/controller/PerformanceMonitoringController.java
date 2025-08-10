package ph.gov.dsr.performance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ph.gov.dsr.performance.monitoring.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performance Monitoring Management Controller
 */
@RestController
@RequestMapping("/api/v1/admin/performance")
@RequiredArgsConstructor
@Tag(name = "Performance Monitoring", description = "Comprehensive performance monitoring and optimization")
public class PerformanceMonitoringController {

    private final PerformanceMonitoringService performanceService;
    private final MetricsCollectionService metricsService;
    private final AlertingService alertingService;
    private final AutoOptimizationService optimizationService;

    @GetMapping("/health")
    @Operation(summary = "Get performance monitoring system health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPerformanceHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            PerformanceMonitoringStats stats = performanceService.getMonitoringStats();
            AlertingStats alertStats = alertingService.getAlertingStats();
            
            health.put("monitoring", Map.of(
                "enabled", stats.isMonitoringEnabled(),
                "realTimeEnabled", stats.isRealTimeMonitoring(),
                "autoOptimizationEnabled", stats.isAutoOptimizationEnabled(),
                "totalMetricsCollected", stats.getTotalMetricsCollected(),
                "servicesMonitored", stats.getServicesMonitored(),
                "lastCollectionTime", stats.getLastCollectionTime()
            ));
            
            health.put("alerting", Map.of(
                "enabled", alertStats.isAlertingEnabled(),
                "activeAlerts", alertStats.getActiveAlerts(),
                "alertsLast24Hours", alertStats.getAlertsLast24Hours(),
                "criticalAlertsLast24Hours", alertStats.getCriticalAlertsLast24Hours(),
                "escalationsActive", alertStats.getEscalationsActive()
            ));
            
            health.put("status", "OPERATIONAL");
            health.put("timestamp", stats.getTimestamp());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }

    @GetMapping("/metrics/snapshot")
    @Operation(summary = "Get current performance snapshot")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceSnapshot> getPerformanceSnapshot() {
        try {
            PerformanceSnapshot snapshot = performanceService.collectPerformanceSnapshot();
            return ResponseEntity.ok(snapshot);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/system")
    @Operation(summary = "Get system metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemMetrics> getSystemMetrics() {
        try {
            SystemMetrics metrics = metricsService.collectSystemMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/services")
    @Operation(summary = "Get all service metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, ServiceMetrics>> getServiceMetrics() {
        try {
            Map<String, ServiceMetrics> metrics = metricsService.collectServiceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/services/{serviceName}")
    @Operation(summary = "Get specific service metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePerformanceMetrics> getServiceMetrics(@PathVariable String serviceName) {
        try {
            ServicePerformanceMetrics metrics = metricsService.collectServiceMetrics(serviceName);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/database")
    @Operation(summary = "Get database metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DatabaseMetrics> getDatabaseMetrics() {
        try {
            DatabaseMetrics metrics = metricsService.collectDatabaseMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/cache")
    @Operation(summary = "Get cache metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CacheMetrics> getCacheMetrics() {
        try {
            CacheMetrics metrics = metricsService.collectCacheMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/network")
    @Operation(summary = "Get network metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NetworkMetrics> getNetworkMetrics() {
        try {
            NetworkMetrics metrics = metricsService.collectNetworkMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/application")
    @Operation(summary = "Get application metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationMetrics> getApplicationMetrics() {
        try {
            ApplicationMetrics metrics = metricsService.collectApplicationMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/analysis/{timeRange}")
    @Operation(summary = "Get performance analysis report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceAnalysisReport> getPerformanceAnalysis(@PathVariable String timeRange) {
        try {
            PerformanceAnalysisReport report = performanceService.getPerformanceAnalysis(timeRange);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get real-time dashboard data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardData> getDashboardData() {
        try {
            DashboardData data = performanceService.getRealTimeDashboardData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/optimization/trigger")
    @Operation(summary = "Trigger manual optimization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OptimizationResult> triggerOptimization(@RequestParam OptimizationType type) {
        try {
            OptimizationResult result = performanceService.triggerOptimization(type);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get active alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        try {
            List<Alert> alerts = alertingService.getActiveAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/alerts/recent/{hours}")
    @Operation(summary = "Get recent alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Alert>> getRecentAlerts(@PathVariable int hours) {
        try {
            List<Alert> alerts = alertingService.getRecentAlerts(hours);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/alerts/{alertId}/resolve")
    @Operation(summary = "Resolve alert")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resolveAlert(
            @PathVariable String alertId,
            @RequestParam String resolvedBy,
            @RequestParam String resolution) {
        try {
            alertingService.resolveAlert(alertId, resolvedBy, resolution);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "resolved");
            response.put("alertId", alertId);
            response.put("resolvedBy", resolvedBy);
            response.put("message", "Alert resolved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/alerts/rules")
    @Operation(summary = "Create alert rule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createAlertRule(@RequestBody AlertRule rule) {
        try {
            performanceService.createAlertRule(rule);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "created");
            response.put("ruleId", rule.getId());
            response.put("ruleName", rule.getName());
            response.put("message", "Alert rule created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/notifications/channels")
    @Operation(summary = "Configure notification channel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> configureNotificationChannel(@RequestBody NotificationChannel channel) {
        try {
            alertingService.configureNotificationChannel(channel);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "configured");
            response.put("channelId", channel.getId());
            response.put("channelType", channel.getType().toString());
            response.put("message", "Notification channel configured successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/notifications/channels/{channelId}/test")
    @Operation(summary = "Test notification channel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationTestResult> testNotificationChannel(@PathVariable String channelId) {
        try {
            NotificationTestResult result = alertingService.testNotificationChannel(channelId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/monitoring/trigger")
    @Operation(summary = "Trigger manual monitoring collection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerMonitoring() {
        try {
            performanceService.performRealTimeMonitoring();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "completed");
            response.put("message", "Manual monitoring collection performed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get performance monitoring statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceMonitoringStats> getMonitoringStatistics() {
        try {
            PerformanceMonitoringStats stats = performanceService.getMonitoringStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/metrics/{metricName}")
    @Operation(summary = "Get specific metric value")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetricValue(@PathVariable String metricName) {
        try {
            double value = metricsService.getMetricValue(metricName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("metricName", metricName);
            response.put("value", value);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/optimization/recommendations")
    @Operation(summary = "Get optimization recommendations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OptimizationRecommendation>> getOptimizationRecommendations() {
        try {
            List<OptimizationRecommendation> recommendations = optimizationService.getRecommendations();
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/trends/{timeRange}")
    @Operation(summary = "Get performance trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerformanceTrends> getPerformanceTrends(@PathVariable String timeRange) {
        try {
            PerformanceTrends trends = performanceService.getPerformanceTrends(timeRange);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
