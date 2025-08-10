package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Chart Widget DTO for various chart types
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ChartWidget {
    private UUID widgetId;
    private String name;
    private String title;
    private String description;
    private String chartType; // LINE, BAR, PIE, AREA, SCATTER, BUBBLE, DONUT, etc.
    private String subType; // STACKED, GROUPED, HORIZONTAL, etc.
    private Integer position;
    private Integer width;
    private Integer height;
    
    // Axes configuration
    private String xAxis;
    private String yAxis;
    private String xAxisLabel;
    private String yAxisLabel;
    private String xAxisType; // CATEGORY, NUMERIC, DATETIME
    private String yAxisType; // NUMERIC, PERCENTAGE
    private Boolean showXAxisGrid;
    private Boolean showYAxisGrid;
    private Boolean showXAxisLabels;
    private Boolean showYAxisLabels;
    
    // Data configuration
    private List<String> series;
    private List<Map<String, Object>> data;
    private Map<String, Object> filters;
    private String aggregation;
    private String groupBy;
    private String sortBy;
    private String sortOrder;
    private Integer limit;
    
    // Styling configuration
    private Map<String, Object> styling;
    private List<String> colors;
    private String colorScheme;
    private Boolean showLegend;
    private String legendPosition; // TOP, BOTTOM, LEFT, RIGHT
    private Boolean showTooltip;
    private Boolean showDataLabels;
    private String theme; // LIGHT, DARK
    
    // Animation configuration
    private Boolean enableAnimation;
    private Integer animationDuration;
    private String animationType;
    
    // Interaction configuration
    private Boolean enableZoom;
    private Boolean enablePan;
    private Boolean enableSelection;
    private Boolean enableCrosshair;
    private Boolean enableBrush;
    
    // Export configuration
    private Boolean supportsExport;
    private List<String> exportFormats;
    
    // Drill-down configuration
    private Boolean supportsDrillDown;
    private String drillDownUrl;
    private Map<String, Object> drillDownParams;
    
    // Real-time configuration
    private Boolean isRealTime;
    private Integer refreshInterval;
    private LocalDateTime lastUpdated;
    private LocalDateTime nextUpdate;
    
    // Data source configuration
    private String dataSource;
    private String query;
    private Map<String, Object> queryParams;
    
    // Alert configuration
    private Boolean hasAlert;
    private Map<String, Object> alertConfig;
    
    // Performance metrics
    private Long loadTime;
    private Integer dataPoints;
    private String renderingEngine;
    
    // Error handling
    private String errorMessage;
    private Boolean isLoading;
    private Boolean hasError;
    
    // Metadata
    private Map<String, Object> metadata;
    private Map<String, Object> configuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
