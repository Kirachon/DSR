package ph.gov.dsr.analytics.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Geospatial Analysis Request DTO for geographic data analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
public class GeospatialAnalysisRequest {
    private String analysisType; // CHOROPLETH, HEAT_MAP, CLUSTER, DENSITY, PROXIMITY
    private String aggregationLevel; // REGION, PROVINCE, CITY, BARANGAY
    private String metric;
    private String aggregation; // SUM, COUNT, AVG, MIN, MAX
    private Map<String, Object> filters;
    private String timeRange;
    private String timeGranularity;
    private Double centerLatitude;
    private Double centerLongitude;
    private Integer zoomLevel;
    private String boundaryType; // ADMINISTRATIVE, CUSTOM, BUFFER
    private List<String> selectedRegions;
    private String colorScheme;
    private Integer colorBuckets;
    private String classificationMethod; // EQUAL_INTERVAL, QUANTILE, NATURAL_BREAKS
    private Boolean includeLabels;
    private Boolean includeTooltips;
    private String outputFormat; // GEOJSON, SHAPEFILE, KML
    private String projection; // WGS84, UTM, ALBERS
    private Map<String, Object> stylingOptions;
    private String userId;
    private Boolean includeStatistics;
    private Boolean includeMetadata;

    // Additional fields for BusinessIntelligenceService compatibility
    private String region;
    private List<String> metrics;
    private Map<String, Object> clusteringParameters;
    private List<String> statisticalMethods;
    private Map<String, Object> hotspotParameters;
    private Map<String, Object> heatMapConfiguration;
}
