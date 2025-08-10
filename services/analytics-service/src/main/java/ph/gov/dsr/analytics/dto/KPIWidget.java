package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * KPI Widget DTO for Key Performance Indicator widgets
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class KPIWidget {
    private UUID widgetId;
    private String name;
    private String title;
    private String description;
    private String metric;
    private String aggregation;
    private Object value;
    private Object previousValue;
    private Double changePercentage;
    private String changeDirection; // UP, DOWN, STABLE
    private String format;
    private String unit;
    private String prefix;
    private String suffix;
    private Integer decimalPlaces;
    private Double threshold;
    private String thresholdType; // ABOVE, BELOW, BETWEEN
    private Double warningThreshold;
    private Double criticalThreshold;
    private String status; // NORMAL, WARNING, CRITICAL
    private String color;
    private String backgroundColor;
    private String icon;
    private String iconColor;
    private Integer position;
    private Integer width;
    private Integer height;
    private Map<String, Object> filters;
    private Map<String, Object> styling;
    private Map<String, Object> configuration;
    private LocalDateTime lastUpdated;
    private LocalDateTime nextUpdate;
    private String dataSource;
    private String query;
    private Boolean isRealTime;
    private Boolean hasAlert;
    private Map<String, Object> alertConfig;
    private String trend; // INCREASING, DECREASING, STABLE
    private Double trendPercentage;
    private String trendPeriod;
    private Map<String, Object> metadata;
    private String errorMessage;
    private Boolean isLoading;
    private Boolean hasError;
    
    // Drill-down support
    private Boolean supportsDrillDown;
    private String drillDownUrl;
    private Map<String, Object> drillDownParams;
    
    // Export support
    private Boolean supportsExport;
    private String exportFormat;
    
    // Comparison data
    private Object targetValue;
    private Object benchmarkValue;
    private String comparisonPeriod;
    private Map<String, Object> historicalData;
}
