package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Geospatial Widget DTO for map-based visualizations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class GeospatialWidget {
    private UUID widgetId;
    private String name;
    private String title;
    private String description;
    private Integer position;
    private Integer width;
    private Integer height;
    
    // Map configuration
    private String mapType; // CHOROPLETH, HEAT_MAP, POINT_MAP, CLUSTER_MAP, FLOW_MAP
    private String baseMap; // STREET, SATELLITE, TERRAIN, HYBRID
    private String projection; // MERCATOR, ALBERS, ROBINSON
    private Double centerLatitude;
    private Double centerLongitude;
    private Integer zoomLevel;
    private Integer minZoom;
    private Integer maxZoom;
    private Boolean enableZoom;
    private Boolean enablePan;
    
    // Data configuration
    private String dataLayer;
    private List<Map<String, Object>> data;
    private Map<String, Object> filters;
    private String aggregationLevel; // REGION, PROVINCE, CITY, BARANGAY
    private String aggregation; // COUNT, SUM, AVG, MIN, MAX
    private String groupBy;
    private String valueField;
    private String locationField;
    private String latitudeField;
    private String longitudeField;
    
    // Styling configuration
    private Map<String, Object> styling;
    private String colorScheme;
    private List<String> colors;
    private String fillColor;
    private String strokeColor;
    private Double fillOpacity;
    private Double strokeOpacity;
    private Integer strokeWidth;
    private String markerStyle; // CIRCLE, SQUARE, TRIANGLE, CUSTOM
    private Integer markerSize;
    private String markerColor;
    
    // Legend configuration
    private Boolean showLegend;
    private String legendPosition; // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    private String legendType; // CONTINUOUS, DISCRETE, CATEGORICAL
    private List<LegendItem> legendItems;
    
    // Tooltip configuration
    private Boolean showTooltip;
    private List<String> tooltipFields;
    private String tooltipTemplate;
    
    // Interaction configuration
    private Boolean enableSelection;
    private Boolean enableMultiSelection;
    private List<String> selectedFeatures;
    private Boolean enablePopup;
    private String popupTemplate;
    
    // Layer configuration
    private List<LayerDefinition> layers;
    private Boolean enableLayerControl;
    private String activeLayer;
    
    // Clustering configuration
    private Boolean enableClustering;
    private Integer clusterRadius;
    private Integer clusterMaxZoom;
    private String clusterStyle;
    
    // Heat map configuration
    private Double heatMapRadius;
    private Double heatMapBlur;
    private Double heatMapMaxZoom;
    private String heatMapGradient;
    
    // Export configuration
    private Boolean supportsExport;
    private List<String> exportFormats;
    private Integer exportWidth;
    private Integer exportHeight;
    private Integer exportDPI;
    
    // Drill-down configuration
    private Boolean supportsDrillDown;
    private String drillDownUrl;
    private Map<String, Object> drillDownParams;
    private String drillDownLevel;
    
    // Real-time configuration
    private Boolean isRealTime;
    private Integer refreshInterval;
    private LocalDateTime lastUpdated;
    private LocalDateTime nextUpdate;
    
    // Data source configuration
    private String dataSource;
    private String query;
    private Map<String, Object> queryParams;
    private String geometrySource;
    private String boundarySource;
    
    // Performance configuration
    private Boolean enableTileCache;
    private Integer tileCacheDuration;
    private Boolean enableDataCache;
    private Integer dataCacheDuration;
    
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
    public static class LegendItem {
        private String label;
        private String color;
        private Object value;
        private Object minValue;
        private Object maxValue;
        private String pattern;
    }
    
    @Data
    @Builder
    public static class LayerDefinition {
        private String id;
        private String name;
        private String type; // VECTOR, RASTER, TILE
        private String source;
        private Boolean visible;
        private Double opacity;
        private Integer zIndex;
        private Map<String, Object> styling;
        private Map<String, Object> filters;
    }
}
