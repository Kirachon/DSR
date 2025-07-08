package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Table Widget DTO for tabular data display
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class TableWidget {
    private UUID widgetId;
    private String name;
    private String title;
    private String description;
    private Integer position;
    private Integer width;
    private Integer height;
    
    // Column configuration
    private List<ColumnDefinition> columns;
    private List<String> visibleColumns;
    private List<String> hiddenColumns;
    private Map<String, Integer> columnWidths;
    private Map<String, String> columnAlignments;
    private Map<String, String> columnFormats;
    
    // Data configuration
    private List<Map<String, Object>> data;
    private Map<String, Object> filters;
    private String sortBy;
    private String sortOrder; // ASC, DESC
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalRecords;
    private String groupBy;
    private String aggregation;
    
    // Pagination configuration
    private Boolean enablePagination;
    private List<Integer> pageSizeOptions;
    private Boolean showPageInfo;
    private Boolean showPageSizeSelector;
    
    // Sorting configuration
    private Boolean enableSorting;
    private Boolean enableMultiSort;
    private List<SortDefinition> sortDefinitions;
    
    // Filtering configuration
    private Boolean enableFiltering;
    private Boolean enableGlobalFilter;
    private Map<String, FilterDefinition> columnFilters;
    private String globalFilterValue;
    
    // Selection configuration
    private Boolean enableSelection;
    private String selectionMode; // SINGLE, MULTIPLE
    private List<String> selectedRows;
    private Boolean enableSelectAll;
    
    // Styling configuration
    private Map<String, Object> styling;
    private String theme; // LIGHT, DARK, STRIPED
    private Boolean showBorders;
    private Boolean showHeader;
    private Boolean showFooter;
    private Boolean enableHover;
    private Boolean enableAlternateRows;
    
    // Export configuration
    private Boolean supportsExport;
    private List<String> exportFormats;
    private Boolean includeHeaders;
    private Boolean includeFilters;
    
    // Drill-down configuration
    private Boolean supportsDrillDown;
    private String drillDownUrl;
    private Map<String, Object> drillDownParams;
    private List<String> drillDownColumns;
    
    // Real-time configuration
    private Boolean isRealTime;
    private Integer refreshInterval;
    private LocalDateTime lastUpdated;
    private LocalDateTime nextUpdate;
    
    // Data source configuration
    private String dataSource;
    private String query;
    private Map<String, Object> queryParams;
    
    // Performance configuration
    private Boolean enableVirtualScrolling;
    private Integer virtualScrollThreshold;
    private Boolean enableLazyLoading;
    
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
    
    @Data
    @Builder
    public static class ColumnDefinition {
        private String key;
        private String label;
        private String dataType; // STRING, NUMBER, DATE, BOOLEAN
        private String format;
        private String alignment; // LEFT, CENTER, RIGHT
        private Integer width;
        private Boolean sortable;
        private Boolean filterable;
        private Boolean visible;
        private Boolean resizable;
        private String aggregation; // SUM, AVG, COUNT, MIN, MAX
        private Map<String, Object> styling;
    }
    
    @Data
    @Builder
    public static class SortDefinition {
        private String column;
        private String direction; // ASC, DESC
        private Integer priority;
    }
    
    @Data
    @Builder
    public static class FilterDefinition {
        private String column;
        private String operator; // EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH, GREATER_THAN, LESS_THAN
        private Object value;
        private String dataType;
    }
}
