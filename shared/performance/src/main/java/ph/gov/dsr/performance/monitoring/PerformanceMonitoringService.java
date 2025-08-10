package ph.gov.dsr.performance.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Comprehensive Performance Monitoring Service
 * Real-time monitoring, alerting, and automated optimization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringService {

    private final MetricsCollectionService metricsCollector;
    private final AlertingService alertingService;
    private final PerformanceAnalyzer performanceAnalyzer;
    private final AutoOptimizationService autoOptimizer;
    private final DashboardService dashboardService;

    @Value("${dsr.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    @Value("${dsr.monitoring.real-time:true}")
    private boolean realTimeMonitoring;

    @Value("${dsr.monitoring.auto-optimization:true}")
    private boolean autoOptimizationEnabled;

    @Value("${dsr.monitoring.collection-interval:30}")
    private int collectionIntervalSeconds;

    // Performance tracking
    private final Map<String, ServicePerformanceMetrics> serviceMetrics = new ConcurrentHashMap<>();
    private final Map<String, SystemPerformanceMetrics> systemMetrics = new ConcurrentHashMap<>();
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();
    private final AtomicLong totalMetricsCollected = new AtomicLong(0);
    private final AtomicLong alertsTriggered = new AtomicLong(0);

    /**
     * Real-time performance monitoring
     */
    @Scheduled(fixedRateString = "${dsr.monitoring.collection-interval:30000}")
    public void performRealTimeMonitoring() {
        if (!monitoringEnabled || !realTimeMonitoring) {
            return;
        }

        try {
            log.debug("Performing real-time performance monitoring");
            
            // Collect system metrics
            collectSystemMetrics();
            
            // Collect service metrics
            collectServiceMetrics();
            
            // Collect application metrics
            collectApplicationMetrics();
            
            // Collect infrastructure metrics
            collectInfrastructureMetrics();
            
            // Analyze performance trends
            analyzePerformanceTrends();
            
            // Check alert conditions
            checkAlertConditions();
            
            // Trigger auto-optimization if needed
            if (autoOptimizationEnabled) {
                triggerAutoOptimization();
            }
            
            totalMetricsCollected.incrementAndGet();
            
        } catch (Exception e) {
            log.error("Error during real-time performance monitoring", e);
        }
    }

    /**
     * Collect comprehensive performance metrics
     */
    public PerformanceSnapshot collectPerformanceSnapshot() {
        try {
            log.debug("Collecting comprehensive performance snapshot");
            
            // System metrics
            SystemMetrics systemMetrics = metricsCollector.collectSystemMetrics();
            
            // Service metrics
            Map<String, ServiceMetrics> serviceMetrics = metricsCollector.collectServiceMetrics();
            
            // Database metrics
            DatabaseMetrics databaseMetrics = metricsCollector.collectDatabaseMetrics();
            
            // Cache metrics
            CacheMetrics cacheMetrics = metricsCollector.collectCacheMetrics();
            
            // Network metrics
            NetworkMetrics networkMetrics = metricsCollector.collectNetworkMetrics();
            
            // Application metrics
            ApplicationMetrics applicationMetrics = metricsCollector.collectApplicationMetrics();
            
            return PerformanceSnapshot.builder()
                .timestamp(LocalDateTime.now())
                .systemMetrics(systemMetrics)
                .serviceMetrics(serviceMetrics)
                .databaseMetrics(databaseMetrics)
                .cacheMetrics(cacheMetrics)
                .networkMetrics(networkMetrics)
                .applicationMetrics(applicationMetrics)
                .overallHealthScore(calculateOverallHealthScore(systemMetrics, serviceMetrics, databaseMetrics))
                .build();
                
        } catch (Exception e) {
            log.error("Error collecting performance snapshot", e);
            return PerformanceSnapshot.error("Failed to collect metrics: " + e.getMessage());
        }
    }

    /**
     * Get performance monitoring statistics
     */
    public PerformanceMonitoringStats getMonitoringStats() {
        return PerformanceMonitoringStats.builder()
            .monitoringEnabled(monitoringEnabled)
            .realTimeMonitoring(realTimeMonitoring)
            .autoOptimizationEnabled(autoOptimizationEnabled)
            .totalMetricsCollected(totalMetricsCollected.get())
            .alertsTriggered(alertsTriggered.get())
            .servicesMonitored(serviceMetrics.size())
            .activeAlertRules(alertRules.size())
            .lastCollectionTime(getLastCollectionTime())
            .collectionIntervalSeconds(collectionIntervalSeconds)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Create custom alert rule
     */
    public void createAlertRule(AlertRule rule) {
        try {
            alertRules.put(rule.getId(), rule);
            log.info("Created alert rule: {} - {}", rule.getId(), rule.getName());
            
            // Validate rule configuration
            if (!rule.isValid()) {
                log.warn("Alert rule {} has invalid configuration", rule.getId());
            }
            
        } catch (Exception e) {
            log.error("Error creating alert rule", e);
        }
    }

    /**
     * Get performance analysis report
     */
    public PerformanceAnalysisReport getPerformanceAnalysis(String timeRange) {
        try {
            log.debug("Generating performance analysis report for timeRange: {}", timeRange);
            
            // Analyze performance trends
            PerformanceTrendAnalysis trendAnalysis = performanceAnalyzer.analyzeTrends(timeRange);
            
            // Identify bottlenecks
            List<PerformanceBottleneck> bottlenecks = performanceAnalyzer.identifyBottlenecks();
            
            // Generate optimization recommendations
            List<OptimizationRecommendation> recommendations = performanceAnalyzer.generateRecommendations();
            
            // Calculate performance scores
            PerformanceScores scores = performanceAnalyzer.calculatePerformanceScores();
            
            return PerformanceAnalysisReport.builder()
                .timeRange(timeRange)
                .trendAnalysis(trendAnalysis)
                .bottlenecks(bottlenecks)
                .recommendations(recommendations)
                .performanceScores(scores)
                .generatedAt(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error generating performance analysis report", e);
            return PerformanceAnalysisReport.error(timeRange, e.getMessage());
        }
    }

    /**
     * Trigger manual optimization
     */
    public OptimizationResult triggerOptimization(OptimizationType type) {
        try {
            log.info("Triggering manual optimization: {}", type);
            
            return autoOptimizer.performOptimization(type);
            
        } catch (Exception e) {
            log.error("Error triggering optimization", e);
            return OptimizationResult.failed(type, e.getMessage());
        }
    }

    /**
     * Get real-time dashboard data
     */
    public DashboardData getRealTimeDashboardData() {
        try {
            // Get current performance snapshot
            PerformanceSnapshot snapshot = collectPerformanceSnapshot();
            
            // Get recent alerts
            List<Alert> recentAlerts = alertingService.getRecentAlerts(24); // Last 24 hours
            
            // Get performance trends
            PerformanceTrends trends = performanceAnalyzer.getRecentTrends();
            
            // Get system status
            SystemStatus systemStatus = calculateSystemStatus(snapshot);
            
            return DashboardData.builder()
                .snapshot(snapshot)
                .recentAlerts(recentAlerts)
                .trends(trends)
                .systemStatus(systemStatus)
                .lastUpdated(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error getting dashboard data", e);
            return DashboardData.error(e.getMessage());
        }
    }

    // Private helper methods

    private void collectSystemMetrics() {
        try {
            SystemPerformanceMetrics metrics = SystemPerformanceMetrics.builder()
                .cpuUsage(metricsCollector.getCpuUsage())
                .memoryUsage(metricsCollector.getMemoryUsage())
                .diskUsage(metricsCollector.getDiskUsage())
                .networkIO(metricsCollector.getNetworkIO())
                .loadAverage(metricsCollector.getLoadAverage())
                .timestamp(LocalDateTime.now())
                .build();
            
            systemMetrics.put("system", metrics);
            
        } catch (Exception e) {
            log.error("Error collecting system metrics", e);
        }
    }

    private void collectServiceMetrics() {
        try {
            List<String> services = Arrays.asList(
                "registration-service", "data-management-service", "payment-service",
                "eligibility-service", "interoperability-service", "grievance-service",
                "analytics-service", "api-gateway"
            );
            
            for (String serviceName : services) {
                ServicePerformanceMetrics metrics = metricsCollector.collectServiceMetrics(serviceName);
                if (metrics != null) {
                    serviceMetrics.put(serviceName, metrics);
                }
            }
            
        } catch (Exception e) {
            log.error("Error collecting service metrics", e);
        }
    }

    private void collectApplicationMetrics() {
        try {
            // Collect JVM metrics
            metricsCollector.collectJvmMetrics();
            
            // Collect thread pool metrics
            metricsCollector.collectThreadPoolMetrics();
            
            // Collect garbage collection metrics
            metricsCollector.collectGcMetrics();
            
        } catch (Exception e) {
            log.error("Error collecting application metrics", e);
        }
    }

    private void collectInfrastructureMetrics() {
        try {
            // Collect database metrics
            metricsCollector.collectDatabaseMetrics();
            
            // Collect cache metrics
            metricsCollector.collectCacheMetrics();
            
            // Collect message queue metrics
            metricsCollector.collectMessageQueueMetrics();
            
        } catch (Exception e) {
            log.error("Error collecting infrastructure metrics", e);
        }
    }

    private void analyzePerformanceTrends() {
        try {
            // Analyze trends for each service
            for (Map.Entry<String, ServicePerformanceMetrics> entry : serviceMetrics.entrySet()) {
                String serviceName = entry.getKey();
                ServicePerformanceMetrics metrics = entry.getValue();
                
                performanceAnalyzer.analyzeServiceTrends(serviceName, metrics);
            }
            
            // Analyze system trends
            SystemPerformanceMetrics systemMetrics = this.systemMetrics.get("system");
            if (systemMetrics != null) {
                performanceAnalyzer.analyzeSystemTrends(systemMetrics);
            }
            
        } catch (Exception e) {
            log.error("Error analyzing performance trends", e);
        }
    }

    private void checkAlertConditions() {
        try {
            for (AlertRule rule : alertRules.values()) {
                if (rule.isEnabled()) {
                    boolean conditionMet = evaluateAlertCondition(rule);
                    
                    if (conditionMet) {
                        triggerAlert(rule);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error checking alert conditions", e);
        }
    }

    private boolean evaluateAlertCondition(AlertRule rule) {
        try {
            // Get current metric value
            double currentValue = metricsCollector.getMetricValue(rule.getMetricName());
            
            // Evaluate condition
            return rule.evaluateCondition(currentValue);
            
        } catch (Exception e) {
            log.error("Error evaluating alert condition for rule: {}", rule.getId(), e);
            return false;
        }
    }

    private void triggerAlert(AlertRule rule) {
        try {
            Alert alert = Alert.builder()
                .id(UUID.randomUUID().toString())
                .ruleId(rule.getId())
                .ruleName(rule.getName())
                .severity(rule.getSeverity())
                .message(rule.generateAlertMessage())
                .timestamp(LocalDateTime.now())
                .build();
            
            alertingService.sendAlert(alert);
            alertsTriggered.incrementAndGet();
            
            log.warn("Alert triggered: {} - {}", rule.getName(), alert.getMessage());
            
        } catch (Exception e) {
            log.error("Error triggering alert for rule: {}", rule.getId(), e);
        }
    }

    private void triggerAutoOptimization() {
        try {
            // Check if optimization is needed
            if (autoOptimizer.isOptimizationNeeded()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        autoOptimizer.performAutomaticOptimization();
                    } catch (Exception e) {
                        log.error("Error during automatic optimization", e);
                    }
                });
            }
            
        } catch (Exception e) {
            log.error("Error triggering auto-optimization", e);
        }
    }

    private double calculateOverallHealthScore(SystemMetrics systemMetrics, 
                                             Map<String, ServiceMetrics> serviceMetrics,
                                             DatabaseMetrics databaseMetrics) {
        try {
            double systemScore = calculateSystemHealthScore(systemMetrics);
            double serviceScore = calculateServiceHealthScore(serviceMetrics);
            double databaseScore = calculateDatabaseHealthScore(databaseMetrics);
            
            // Weighted average
            return (systemScore * 0.3) + (serviceScore * 0.5) + (databaseScore * 0.2);
            
        } catch (Exception e) {
            log.error("Error calculating overall health score", e);
            return 0.0;
        }
    }

    private double calculateSystemHealthScore(SystemMetrics metrics) {
        if (metrics == null) return 0.0;
        
        double cpuScore = Math.max(0, 100 - metrics.getCpuUsage());
        double memoryScore = Math.max(0, 100 - metrics.getMemoryUsage());
        double diskScore = Math.max(0, 100 - metrics.getDiskUsage());
        
        return (cpuScore + memoryScore + diskScore) / 3.0;
    }

    private double calculateServiceHealthScore(Map<String, ServiceMetrics> serviceMetrics) {
        if (serviceMetrics.isEmpty()) return 0.0;
        
        double totalScore = 0.0;
        int healthyServices = 0;
        
        for (ServiceMetrics metrics : serviceMetrics.values()) {
            if (metrics.isHealthy()) {
                totalScore += metrics.getHealthScore();
                healthyServices++;
            }
        }
        
        return healthyServices > 0 ? totalScore / healthyServices : 0.0;
    }

    private double calculateDatabaseHealthScore(DatabaseMetrics metrics) {
        if (metrics == null) return 0.0;
        
        // Calculate based on connection pool, query performance, etc.
        return metrics.getOverallHealthScore();
    }

    private SystemStatus calculateSystemStatus(PerformanceSnapshot snapshot) {
        double healthScore = snapshot.getOverallHealthScore();
        
        if (healthScore >= 90) return SystemStatus.EXCELLENT;
        if (healthScore >= 75) return SystemStatus.GOOD;
        if (healthScore >= 60) return SystemStatus.WARNING;
        if (healthScore >= 40) return SystemStatus.CRITICAL;
        return SystemStatus.DOWN;
    }

    private LocalDateTime getLastCollectionTime() {
        return systemMetrics.values().stream()
            .map(SystemPerformanceMetrics::getTimestamp)
            .max(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now().minusMinutes(1));
    }
}
