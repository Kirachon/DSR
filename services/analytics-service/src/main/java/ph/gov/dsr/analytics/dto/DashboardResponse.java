package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;
import ph.gov.dsr.analytics.entity.Dashboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dashboard Response DTO for dashboard data and metadata
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class DashboardResponse {
    private UUID dashboardId;
    private String dashboardCode;
    private String dashboardName;
    private String description;
    private Dashboard.DashboardType dashboardType;
    private Dashboard.DashboardCategory category;
    private String targetRole;
    private Dashboard.DashboardStatus status;
    private Boolean isDefault;
    private Boolean isPublic;
    private String accessLevel;
    private List<String> allowedRoles;
    private List<String> allowedUsers;
    private Integer viewCount;
    private LocalDateTime lastViewed;
    private LocalDateTime lastRefreshed;
    private Boolean cacheEnabled;
    private Integer cacheDurationSeconds;
    private Boolean exportEnabled;
    private List<String> exportFormats;
    private Boolean drillDownEnabled;
    private Boolean realTimeEnabled;
    private Boolean alertEnabled;
    private Integer version;
    private UUID parentDashboardId;
    private Integer sortOrder;
    private List<String> tags;
    private Map<String, Object> metadata;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Dashboard data
    private DashboardData data;
    private List<KPIWidget> kpiWidgets;
    private List<ChartWidget> chartWidgets;
    private List<TableWidget> tableWidgets;
    private List<GeospatialWidget> geospatialWidgets;
    
    // Performance metrics
    private Long loadTime;
    private Long dataFreshness;
    private String cacheStatus;
    private Map<String, Object> performanceMetrics;
    
    // Error handling
    private String errorMessage;
    private List<String> warnings;
    
    // Usage statistics
    private String usageFrequency;
    private Long dashboardAgeInDays;
    private Boolean isRecentlyCreated;
    private Boolean isFrequentlyUsed;
    
    // Configuration
    private Map<String, Object> layoutConfig;
    private Map<String, Object> widgetConfig;
    private Map<String, Object> filterConfig;
    private Map<String, Object> alertConfig;
    
    // Refresh settings
    private Integer refreshIntervalSeconds;
    private Boolean autoRefreshEnabled;
    private Boolean needsRefresh;
    private Boolean isCached;
    private Boolean isCacheValid;
}
