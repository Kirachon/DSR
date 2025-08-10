package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Table Requirement DTO for table widget requirements
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class TableRequirement {
    private String name;
    private List<String> columns;
    private Map<String, Object> filters;
    private String sortBy;
    private String sortOrder;
    private Integer pageSize;
    private Integer position;
    private String dataSource;
    private Map<String, Object> configuration;
    private String aggregation;
    private String groupBy;
    private Boolean enablePagination;
    private Boolean enableSorting;
    private Boolean enableFiltering;
    private Boolean enableSelection;
    private String selectionMode; // SINGLE, MULTIPLE
    private Map<String, Object> styling;
    private String theme;
    private Boolean showBorders;
    private Boolean showHeader;
    private Boolean showFooter;
    private Boolean enableHover;
    private Boolean enableAlternateRows;
    private List<ColumnDefinition> columnDefinitions;
    private Boolean isRealTime;
    private Integer refreshInterval;
    private String description;
    private Map<String, Object> pagination;
    private Map<String, Object> drillDownConfig;
    private Boolean supportsDrillDown;
    private Map<String, Object> exportConfig;
    private Boolean supportsExport;
    private Boolean enableVirtualScrolling;
    private Boolean enableLazyLoading;
    
    @Data
    public static class ColumnDefinition {
        private String key;
        private String label;
        private String dataType;
        private String format;
        private String alignment;
        private Integer width;
        private Boolean sortable;
        private Boolean filterable;
        private Boolean visible;
        private Boolean resizable;
        private String aggregation;
        private Map<String, Object> styling;
    }
}
