package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Drill Down Result DTO for hierarchical data exploration
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class DrillDownResult {
    private String drillDownId;
    private String parentId;
    private String level;
    private String dimension;
    private String hierarchy;
    private LocalDateTime generatedAt;
    
    // Navigation context
    private List<String> breadcrumb;
    private String currentLevel;
    private String parentLevel;
    private List<String> availableLevels;
    private Boolean hasParent;
    private Boolean hasChildren;
    private String navigationPath;
    
    // Data results
    private List<Map<String, Object>> data;
    private Long totalRecords;
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    private String sortBy;
    private String sortOrder;
    
    // Aggregated metrics
    private Map<String, Object> summaryMetrics;
    private Map<String, Object> aggregatedValues;
    private Map<String, Object> calculatedFields;
    private Map<String, Object> percentageBreakdown;
    
    // Filtering context
    private Map<String, Object> appliedFilters;
    private Map<String, Object> inheritedFilters;
    private Map<String, Object> levelSpecificFilters;
    private List<String> availableFilters;
    
    // Hierarchical structure
    private List<HierarchyLevel> hierarchyLevels;
    private Map<String, Object> parentData;
    private List<Map<String, Object>> childrenData;
    private List<Map<String, Object>> siblingData;
    
    // Performance metrics
    private Long queryTime;
    private Long processingTime;
    private Long renderTime;
    private Integer dataPoints;
    private String cacheStatus;
    
    // Visualization hints
    private String recommendedVisualization;
    private List<String> supportedChartTypes;
    private Map<String, Object> visualizationConfig;
    private String defaultGrouping;
    
    // Export capabilities
    private Boolean supportsExport;
    private List<String> exportFormats;
    private Map<String, String> exportUrls;
    
    // Further drill-down options
    private List<DrillDownOption> drillDownOptions;
    private Boolean supportsFurtherDrillDown;
    private String nextLevel;
    private List<String> drillDownDimensions;
    
    // Data quality
    private String dataQuality;
    private Double completeness;
    private List<String> dataIssues;
    private Integer missingValues;
    
    // User context
    private String userId;
    private String userRole;
    private List<String> userPermissions;
    private Map<String, Object> userPreferences;
    
    // Error handling
    private String errorMessage;
    private Boolean hasErrors;
    private List<String> warnings;
    
    // Metadata
    private Map<String, Object> metadata;
    private String dataSource;
    private String query;
    private Map<String, Object> queryParameters;
    private LocalDateTime dataAsOf;
    
    @Data
    @Builder
    public static class HierarchyLevel {
        private String level;
        private String name;
        private String description;
        private Integer order;
        private Boolean isActive;
        private Boolean hasData;
        private Long recordCount;
        private List<String> availableFields;
    }
    
    @Data
    @Builder
    public static class DrillDownOption {
        private String dimension;
        private String level;
        private String name;
        private String description;
        private Boolean isAvailable;
        private Long recordCount;
        private String url;
        private Map<String, Object> parameters;
    }
}
