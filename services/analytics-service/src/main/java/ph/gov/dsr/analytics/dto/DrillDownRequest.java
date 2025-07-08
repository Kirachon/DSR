package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Drill Down Request DTO for hierarchical data exploration requests
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class DrillDownRequest {
    private UUID dashboardId;
    private UUID widgetId;
    private String dimension;
    private String level;
    private String hierarchy;
    private String parentId;
    private String targetLevel;
    private Map<String, Object> filters;
    private Map<String, Object> parameters;
    private String sortBy;
    private String sortOrder; // ASC, DESC
    private Integer pageSize;
    private Integer currentPage;
    private String groupBy;
    private String aggregation; // SUM, COUNT, AVG, MIN, MAX
    private List<String> selectedFields;
    private Map<String, Object> drillDownContext;
    private String userId;
    private String userRole;
    private Boolean includeMetadata;
    private Boolean includeSummary;
    private String format; // JSON, CSV, EXCEL
    private String timeRange;
    private String timeGranularity;
    private Map<String, Object> visualizationHints;

    // Additional fields for compatibility
    private String metricName;
    private List<String> drillDownDimensions;
}
