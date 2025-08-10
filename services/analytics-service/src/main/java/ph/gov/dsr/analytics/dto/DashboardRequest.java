package ph.gov.dsr.analytics.dto;

import lombok.Data;
import ph.gov.dsr.analytics.entity.Dashboard;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dashboard Request DTO for creating and updating dashboards
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class DashboardRequest {
    private String dashboardName;
    private String description;
    private Dashboard.DashboardType dashboardType;
    private Dashboard.DashboardCategory category;
    private String targetRole;
    private UUID userId;
    private Map<String, Object> configuration;
    private Map<String, Object> layoutConfig;
    private Map<String, Object> widgetConfig;
    private Map<String, Object> filterConfig;
    private Integer refreshIntervalSeconds;
    private Boolean autoRefreshEnabled;
    private Boolean isDefault;
    private Boolean isPublic;
    private String accessLevel;
    private List<String> allowedRoles;
    private List<String> allowedUsers;
    private Boolean cacheEnabled;
    private Integer cacheDurationSeconds;
    private Boolean exportEnabled;
    private List<String> exportFormats;
    private Boolean drillDownEnabled;
    private Boolean realTimeEnabled;
    private Boolean alertEnabled;
    private Map<String, Object> alertConfig;
    private UUID parentDashboardId;
    private Integer sortOrder;
    private List<String> tags;
    private Map<String, Object> metadata;
    private String notes;
    private List<KPIRequirement> kpiRequirements;
    private List<ChartRequirement> chartRequirements;
    private List<TableRequirement> tableRequirements;
    private List<GeospatialRequirement> geospatialRequirements;
}
