package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Custom Report Request DTO for custom report generation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class CustomReportRequest {
    private String reportName;
    private String description;
    private String reportType; // TABULAR, CHART, DASHBOARD, SUMMARY, DETAILED
    private String category; // OPERATIONAL, FINANCIAL, COMPLIANCE, ANALYTICAL
    private String templateId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String timePeriod; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    private String timeGranularity;
    private Map<String, Object> filters;
    private Map<String, Object> parameters;
    private List<String> selectedMetrics;
    private List<String> selectedDimensions;
    private List<String> groupByFields;
    private String sortBy;
    private String sortOrder; // ASC, DESC
    private String aggregation; // SUM, AVG, COUNT, MIN, MAX
    private String format; // PDF, EXCEL, CSV, JSON, HTML
    private Boolean includeCharts;
    private Boolean includeTables;
    private Boolean includeKPIs;
    private Boolean includeSummary;
    private Boolean includeExecutiveSummary;
    private Boolean includeMethodology;
    private Boolean includeRecommendations;
    private String deliveryMethod; // EMAIL, DOWNLOAD, API
    private List<String> recipients;
    private Boolean isScheduled;
    private String scheduleFrequency; // DAILY, WEEKLY, MONTHLY
    private LocalDateTime scheduledDelivery;
    private String accessLevel; // PUBLIC, INTERNAL, RESTRICTED, CONFIDENTIAL
    private String userId;
    private String userRole;
    private Map<String, Object> stylingOptions;
    private Map<String, Object> layoutOptions;
    private Boolean includeDataQualityInfo;
    private Boolean includeMetadata;

    // Additional fields for BusinessIntelligenceService compatibility
    private Map<String, Object> reportConfiguration;
    private List<String> dataSources;
    private Map<String, Object> aggregations;
    private Map<String, Object> formattingOptions;
    private List<String> visualizationRequirements;
}
