package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.util.Map;

/**
 * KPI Requirement DTO for KPI widget requirements
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class KPIRequirement {
    private String name;
    private String metric;
    private String aggregation;
    private Map<String, Object> filters;
    private String format;
    private String unit;
    private Double target;
    private Double threshold;
    private String thresholdType;
    private String color;
    private Integer position;
    private String dataSource;
    private Map<String, Object> configuration;
    private Boolean isRealTime;
    private Integer refreshInterval;
    private String description;
    private String category;
    private String businessOwner;
    private String technicalOwner;
    private Map<String, Object> styling;
    private Map<String, Object> alertConfig;
    private Boolean hasAlert;
    private String trendDirection;
    private String comparisonPeriod;
    private Map<String, Object> drillDownConfig;
    private Boolean supportsDrillDown;
}
