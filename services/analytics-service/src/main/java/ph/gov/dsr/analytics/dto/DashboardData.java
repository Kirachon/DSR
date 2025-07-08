package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dashboard Data DTO containing all dashboard content and metrics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class DashboardData {
    private UUID dashboardId;
    private String dashboardCode;
    private String dashboardName;
    private LocalDateTime generatedAt;
    private LocalDateTime dataAsOf;
    private String status; // READY, LOADING, ERROR, STALE
    
    // Widget data
    private List<KPIWidget> kpiWidgets;
    private List<ChartWidget> chartWidgets;
    private List<TableWidget> tableWidgets;
    private List<GeospatialWidget> geospatialWidgets;
    
    // Summary metrics
    private Map<String, Object> summaryMetrics;
    private Map<String, Object> keyPerformanceIndicators;
    private Map<String, Object> trendAnalysis;
    private Map<String, Object> comparativeAnalysis;
    
    // Data quality metrics
    private DataQualityMetrics dataQuality;
    private Long totalRecords;
    private Long validRecords;
    private Long invalidRecords;
    private Double completenessPercentage;
    private Double accuracyPercentage;
    private Double consistencyPercentage;
    
    // Performance metrics
    private Long loadTime;
    private Long queryTime;
    private Long renderTime;
    private Integer widgetCount;
    private Integer dataPointCount;
    private String cacheStatus; // HIT, MISS, EXPIRED
    private LocalDateTime cacheExpiry;
    
    // Filter state
    private Map<String, Object> appliedFilters;
    private Map<String, Object> availableFilters;
    private String filterSummary;
    
    // Time range
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String timePeriod; // LAST_24H, LAST_7D, LAST_30D, LAST_90D, CUSTOM
    private String timeGranularity; // HOUR, DAY, WEEK, MONTH, QUARTER, YEAR
    
    // Drill-down data
    private Map<String, Object> drillDownData;
    private List<String> availableDrillDowns;
    private String currentDrillDownLevel;
    
    // Alert data
    private List<AlertInfo> activeAlerts;
    private List<AlertInfo> recentAlerts;
    private Integer alertCount;
    private String alertSummary;
    
    // Export information
    private List<String> availableExports;
    private Map<String, String> exportUrls;
    private LocalDateTime lastExported;
    
    // User context
    private String userId;
    private String userRole;
    private List<String> userPermissions;
    private Map<String, Object> userPreferences;
    
    // Error handling
    private List<String> errors;
    private List<String> warnings;
    private List<String> informationalMessages;
    private Boolean hasErrors;
    private Boolean hasWarnings;
    
    // Metadata
    private Map<String, Object> metadata;
    private Map<String, Object> configuration;
    private String version;
    private String generatedBy;
    
    @Data
    @Builder
    public static class DataQualityMetrics {
        private Double completeness;
        private Double accuracy;
        private Double consistency;
        private Double validity;
        private Double uniqueness;
        private Double timeliness;
        private Map<String, Double> fieldQuality;
        private List<String> qualityIssues;
        private String overallScore;
        private String qualityGrade; // A, B, C, D, F
    }
    
    @Data
    @Builder
    public static class AlertInfo {
        private UUID alertId;
        private String alertType; // WARNING, CRITICAL, INFO
        private String title;
        private String message;
        private String severity; // LOW, MEDIUM, HIGH, CRITICAL
        private LocalDateTime triggeredAt;
        private LocalDateTime acknowledgedAt;
        private String acknowledgedBy;
        private Boolean isActive;
        private Boolean isAcknowledged;
        private Map<String, Object> alertData;
        private String actionRequired;
        private String actionUrl;
    }
}
