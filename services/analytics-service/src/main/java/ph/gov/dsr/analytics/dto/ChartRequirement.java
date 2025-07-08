package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Chart Requirement DTO for chart widget requirements
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class ChartRequirement {
    private String name;
    private String chartType; // LINE, BAR, PIE, AREA, SCATTER, BUBBLE, DONUT
    private String xAxis;
    private String yAxis;
    private List<String> series;
    private Map<String, Object> filters;
    private Map<String, Object> styling;
    private Integer position;
    private String dataSource;
    private Map<String, Object> configuration;
    private String aggregation;
    private String groupBy;
    private String sortBy;
    private String sortOrder;
    private Integer limit;
    private String timeGranularity;
    private Boolean showLegend;
    private String legendPosition;
    private Boolean showTooltip;
    private Boolean showDataLabels;
    private String theme;
    private List<String> colors;
    private String colorScheme;
    private Boolean enableAnimation;
    private Boolean enableZoom;
    private Boolean enablePan;
    private Boolean isRealTime;
    private Integer refreshInterval;
    private String description;
    private Map<String, Object> drillDownConfig;
    private Boolean supportsDrillDown;
    private Map<String, Object> exportConfig;
    private Boolean supportsExport;
}
