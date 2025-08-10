package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Geospatial Analysis Result DTO for geographic analysis results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class GeospatialAnalysisResult {
    private String analysisId;
    private String analysisType;
    private String aggregationLevel;
    private String metric;
    private LocalDateTime analysisDate;
    private LocalDateTime dataAsOf;

    // Additional fields for BusinessIntelligenceService compatibility
    private String region;
    private GeospatialData geospatialData;
    private List<ph.gov.dsr.analytics.dto.SpatialCluster> spatialClusters;
    private SpatialStatistics spatialStatistics;
    private List<SpatialHotspot> hotspots;
    private List<HeatMapData> heatMaps;
    private LocalDateTime analysisTimestamp;
    
    // Geographic data
    private List<GeographicFeature> features;
    private Map<String, Object> boundingBox;
    private String projection;
    private String coordinateSystem;
    
    // Statistical summary
    private Map<String, Object> statistics;
    private Double minimum;
    private Double maximum;
    private Double mean;
    private Double median;
    private Double standardDeviation;
    private Long totalFeatures;
    private Long validFeatures;
    
    // Spatial patterns
    private List<SpatialPattern> patterns;
    private List<SpatialCluster> clusters;
    private List<SpatialOutlier> outliers;
    private String dominantPattern;
    
    // Hot spots and cold spots
    private List<HotSpot> hotSpots;
    private List<ColdSpot> coldSpots;
    private String hotSpotMethod; // GETIS_ORD, MORAN_I, LOCAL_MORAN
    private Double confidenceLevel;
    
    // Spatial autocorrelation
    private Double moranI;
    private Double gearyC;
    private String spatialAutocorrelation; // POSITIVE, NEGATIVE, RANDOM
    private Double pValue;
    private String significance;
    
    // Visualization data
    private String colorScheme;
    private List<String> colorBreaks;
    private Map<String, String> legend;
    private Map<String, Object> stylingOptions;
    
    // Export information
    private List<String> availableFormats;
    private Map<String, String> exportUrls;
    private String geoJsonData;
    
    // Performance metrics
    private Long processingTime;
    private Long dataPoints;
    private String cacheStatus;
    
    // Error handling
    private String errorMessage;
    private List<String> warnings;
    private Boolean hasErrors;
    
    // Metadata
    private Map<String, Object> metadata;
    private String dataSource;
    private Map<String, Object> parameters;
    
    @Data
    @Builder
    public static class GeographicFeature {
        private String id;
        private String name;
        private String type; // POINT, POLYGON, LINESTRING
        private Map<String, Object> geometry;
        private Map<String, Object> properties;
        private Double value;
        private String classification;
        private String color;
    }
    
    @Data
    @Builder
    public static class SpatialPattern {
        private String type; // CLUSTERED, DISPERSED, RANDOM
        private String description;
        private Double strength;
        private Double confidence;
        private Map<String, Object> location;
    }
    
    @Data
    @Builder
    public static class SpatialCluster {
        private String id;
        private String type; // HIGH_HIGH, LOW_LOW, HIGH_LOW, LOW_HIGH
        private List<String> featureIds;
        private Map<String, Object> centroid;
        private Double significance;
        private Double zScore;
    }
    
    @Data
    @Builder
    public static class SpatialOutlier {
        private String featureId;
        private String type; // HIGH_OUTLIER, LOW_OUTLIER
        private Double value;
        private Double expectedValue;
        private Double zScore;
        private Double significance;
    }
    
    @Data
    @Builder
    public static class HotSpot {
        private String featureId;
        private String intensity; // HIGH, MEDIUM, LOW
        private Double zScore;
        private Double pValue;
        private String significance;
        private Map<String, Object> location;
    }
    
    @Data
    @Builder
    public static class ColdSpot {
        private String featureId;
        private String intensity; // HIGH, MEDIUM, LOW
        private Double zScore;
        private Double pValue;
        private String significance;
        private Map<String, Object> location;
    }
}
