package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Geospatial Requirement DTO for geospatial widget requirements
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class GeospatialRequirement {
    private String name;
    private String mapType; // CHOROPLETH, HEAT_MAP, POINT_MAP, CLUSTER_MAP, FLOW_MAP
    private String dataLayer;
    private Map<String, Object> filters;
    private Map<String, Object> styling;
    private String aggregationLevel; // REGION, PROVINCE, CITY, BARANGAY
    private Integer position;
    private String dataSource;
    private Map<String, Object> configuration;
    private String baseMap; // STREET, SATELLITE, TERRAIN, HYBRID
    private String projection;
    private Double centerLatitude;
    private Double centerLongitude;
    private Integer zoomLevel;
    private Integer minZoom;
    private Integer maxZoom;
    private Boolean enableZoom;
    private Boolean enablePan;
    private String aggregation;
    private String valueField;
    private String locationField;
    private String latitudeField;
    private String longitudeField;
    private String colorScheme;
    private List<String> colors;
    private String fillColor;
    private String strokeColor;
    private Double fillOpacity;
    private Double strokeOpacity;
    private Integer strokeWidth;
    private Boolean showLegend;
    private String legendPosition;
    private String legendType;
    private Boolean showTooltip;
    private List<String> tooltipFields;
    private Boolean enableSelection;
    private Boolean enableClustering;
    private Integer clusterRadius;
    private List<LayerDefinition> layers;
    private Boolean isRealTime;
    private Integer refreshInterval;
    private String description;
    private Map<String, Object> drillDownConfig;
    private Boolean supportsDrillDown;
    private Map<String, Object> exportConfig;
    private Boolean supportsExport;
    
    @Data
    public static class LayerDefinition {
        private String id;
        private String name;
        private String type;
        private String source;
        private Boolean visible;
        private Double opacity;
        private Integer zIndex;
        private Map<String, Object> styling;
        private Map<String, Object> filters;
    }
}
