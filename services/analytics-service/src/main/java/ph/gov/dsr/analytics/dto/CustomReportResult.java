package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Custom Report Result DTO for custom report generation results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class CustomReportResult {
    private UUID reportId;
    private String reportCode;
    private String reportName;
    private String description;
    private String reportType; // TABULAR, CHART, DASHBOARD, SUMMARY, DETAILED
    private String category; // OPERATIONAL, FINANCIAL, COMPLIANCE, ANALYTICAL
    
    // Generation details
    private LocalDateTime generatedAt;
    private LocalDateTime dataAsOf;
    private String generatedBy;
    private String status; // COMPLETED, FAILED, PARTIAL, IN_PROGRESS
    private Long generationTime;
    private String version;
    
    // Time period
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private String timePeriod; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    private String timeGranularity;
    
    // Data content
    private List<Map<String, Object>> data;
    private Long totalRecords;
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    
    // Summary statistics
    private Map<String, Object> summaryStatistics;
    private Map<String, Object> aggregatedMetrics;
    private Map<String, Object> keyFindings;
    private List<String> highlights;
    
    // Report sections
    private List<ReportSection> sections;
    private Map<String, Object> executiveSummary;
    private Map<String, Object> methodology;
    private Map<String, Object> conclusions;
    private Map<String, Object> recommendations;
    
    // Visualizations
    private List<ChartWidget> charts;
    private List<TableWidget> tables;
    private List<KPIWidget> kpis;
    private List<GeospatialWidget> maps;
    
    // Filters and parameters
    private Map<String, Object> appliedFilters;
    private Map<String, Object> reportParameters;
    private Map<String, Object> userSelections;
    private String filterSummary;
    
    // Data quality
    private String dataQuality; // HIGH, MEDIUM, LOW
    private Double completeness;
    private Double accuracy;
    private List<String> dataLimitations;
    private List<String> assumptions;
    private List<String> caveats;
    
    // Export information
    private List<String> availableFormats;
    private Map<String, String> exportUrls;
    private String defaultFormat;
    private Boolean supportsScheduling;
    
    // Distribution
    private List<String> recipients;
    private String distributionMethod; // EMAIL, DOWNLOAD, API, DASHBOARD
    private LocalDateTime scheduledDelivery;
    private Boolean isScheduled;
    
    // Performance metrics
    private Long queryTime;
    private Long processingTime;
    private Long renderTime;
    private Integer dataSourceCount;
    private String cacheStatus;
    
    // Error handling
    private String errorMessage;
    private List<String> errors;
    private List<String> warnings;
    private Boolean hasErrors;
    private Boolean hasWarnings;
    
    // Comparison data
    private CustomReportResult previousReport;
    private String comparisonPeriod;
    private Map<String, Object> comparisonMetrics;
    private List<String> significantChanges;
    
    // Drill-down capabilities
    private Boolean supportsDrillDown;
    private List<String> drillDownDimensions;
    private Map<String, Object> drillDownData;
    
    // Security and access
    private String accessLevel; // PUBLIC, INTERNAL, RESTRICTED, CONFIDENTIAL
    private List<String> allowedRoles;
    private List<String> allowedUsers;
    private Boolean isConfidential;
    
    // Metadata
    private Map<String, Object> metadata;
    private String template;
    private String dataSource;
    private List<String> dataSources;
    private Map<String, Object> configuration;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    public static class ReportSection {
        private String sectionId;
        private String title;
        private String description;
        private Integer order;
        private String type; // SUMMARY, DATA, CHART, TABLE, TEXT, KPI
        private Map<String, Object> content;
        private List<String> insights;
        private Map<String, Object> styling;
        private Boolean isVisible;
    }
}
