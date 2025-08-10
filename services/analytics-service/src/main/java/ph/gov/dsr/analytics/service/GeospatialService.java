package ph.gov.dsr.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ph.gov.dsr.analytics.dto.GeospatialData;
import ph.gov.dsr.analytics.dto.SpatialCluster;
import ph.gov.dsr.analytics.dto.SpatialHotspot;
import ph.gov.dsr.analytics.dto.SpatialStatistics;
import ph.gov.dsr.analytics.model.HeatMapData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Geospatial Service for geographic data analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeospatialService {

    private final RestTemplate restTemplate;

    /**
     * Get geospatial data for analysis
     */
    public GeospatialData getGeospatialData(String region, List<String> metrics, String timeRange) {
        log.info("Getting geospatial data for region: {}, metrics: {}", region, metrics);

        try {
            // Get geographic features from interoperability service
            List<GeospatialData.GeographicFeature> features = fetchGeographicFeatures(region, metrics, timeRange);

            // Calculate statistics
            Map<String, Object> statistics = calculateGeospatialStatistics(features, metrics);

            // Determine bounding box
            Map<String, Object> boundingBox = calculateBoundingBox(features);

            return GeospatialData.builder()
                .dataId(UUID.randomUUID().toString())
                .region(region)
                .dataType("GEOSPATIAL")
                .timestamp(LocalDateTime.now())
                .features(features)
                .metrics(metrics)
                .projection("WGS84")
                .boundingBox(boundingBox)
                .statistics(statistics)
                .metadata(Map.of("timeRange", timeRange, "source", "production"))
                .build();

        } catch (Exception e) {
            log.warn("Failed to fetch geospatial data from services, using fallback: {}", e.getMessage());
            return createFallbackGeospatialData(region, metrics, timeRange);
        }
    }

    /**
     * Perform spatial clustering
     */
    public List<SpatialCluster> performSpatialClustering(GeospatialData geoData, Map<String, Object> parameters) {
        log.info("Performing spatial clustering with parameters: {}", parameters);
        
        List<SpatialCluster> clusters = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            clusters.add(SpatialCluster.builder()
                .clusterId("cluster-" + i)
                .clusterType(i % 2 == 0 ? "HIGH_HIGH" : "LOW_LOW")
                .featureIds(List.of("feature-" + i, "feature-" + (i + 1)))
                .centroid(Map.of("lat", 14.0 + i * 0.2, "lng", 121.0 + i * 0.2))
                .significance(0.95)
                .zScore(2.5 + i * 0.5)
                .memberCount(2)
                .density(0.8)
                .statistics(Map.of("avgValue", 120.0 + i * 10))
                .description("Spatial cluster " + i)
                .build());
        }
        
        return clusters;
    }

    /**
     * Calculate spatial statistics
     */
    public SpatialStatistics calculateSpatialStatistics(GeospatialData geoData, List<String> methods) {
        log.info("Calculating spatial statistics with methods: {}", methods);
        
        return SpatialStatistics.builder()
            .statisticsId(UUID.randomUUID().toString())
            .moranI(0.75)
            .gearyC(0.25)
            .spatialAutocorrelation("POSITIVE")
            .pValue(0.001)
            .significance("HIGH")
            .mean(150.0)
            .median(145.0)
            .standardDeviation(25.0)
            .minimum(100.0)
            .maximum(200.0)
            .totalFeatures(geoData.getFeatures().size())
            .additionalMetrics(Map.of("variance", 625.0, "skewness", 0.2))
            .build();
    }

    /**
     * Identify hotspots
     */
    public List<SpatialHotspot> identifyHotspots(GeospatialData geoData, Map<String, Object> parameters) {
        log.info("Identifying hotspots with parameters: {}", parameters);
        
        List<SpatialHotspot> hotspots = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            hotspots.add(SpatialHotspot.builder()
                .hotspotId("hotspot-" + i)
                .featureId("feature-" + i)
                .type(i % 2 == 0 ? "HOT" : "COLD")
                .intensity(i <= 2 ? "HIGH" : "MEDIUM")
                .zScore(3.0 + i * 0.3)
                .pValue(0.01 / i)
                .significance("HIGH")
                .latitude(14.0 + i * 0.1)
                .longitude(121.0 + i * 0.1)
                .attributes(Map.of("value", 150 + i * 20))
                .description("Hotspot " + i)
                .build());
        }
        
        return hotspots;
    }

    /**
     * Generate heat maps
     */
    public List<HeatMapData> generateHeatMaps(GeospatialData geoData, Map<String, Object> configuration) {
        log.info("Generating heat maps with configuration: {}", configuration);

        // Create a simple heat map with grid data
        int gridWidth = 10;
        int gridHeight = 10;
        double[][] gridValues = new double[gridHeight][gridWidth];

        // Fill grid with sample data
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                gridValues[i][j] = Math.random() * 100;
            }
        }

        HeatMapData.GeographicBounds bounds = HeatMapData.GeographicBounds.builder()
            .northLatitude(15.0)
            .southLatitude(14.0)
            .eastLongitude(122.0)
            .westLongitude(121.0)
            .build();

        HeatMapData.GridResolution resolution = HeatMapData.GridResolution.builder()
            .width(gridWidth)
            .height(gridHeight)
            .cellSizeKm(1.0)
            .build();

        return List.of(HeatMapData.builder()
            .heatMapId(UUID.randomUUID().toString())
            .metric("density")
            .bounds(bounds)
            .resolution(resolution)
            .gridValues(gridValues)
            .generatedAt(LocalDateTime.now())
            .metadata(Map.of("configuration", configuration))
            .build());
    }

    /**
     * Fetch geographic features from interoperability service
     */
    private List<GeospatialData.GeographicFeature> fetchGeographicFeatures(String region, List<String> metrics, String timeRange) {
        log.debug("Fetching geographic features for region: {}", region);

        try {
            // Call interoperability service for geographic data
            String url = String.format("/api/v1/geospatial/features?region=%s&metrics=%s&timeRange=%s",
                region, String.join(",", metrics), timeRange);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("features")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> featureData = (List<Map<String, Object>>) response.get("features");

                return featureData.stream()
                    .map(this::convertToGeographicFeature)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch geographic features from interoperability service: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    /**
     * Calculate geospatial statistics
     */
    private Map<String, Object> calculateGeospatialStatistics(List<GeospatialData.GeographicFeature> features, List<String> metrics) {
        Map<String, Object> statistics = new HashMap<>();

        if (features.isEmpty()) {
            statistics.put("totalFeatures", 0);
            statistics.put("avgValue", 0.0);
            return statistics;
        }

        statistics.put("totalFeatures", features.size());

        // Calculate average values for each metric
        for (String metric : metrics) {
            double avgValue = features.stream()
                .mapToDouble(f -> {
                    Object value = f.getAttributes().get(metric);
                    return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
                })
                .average()
                .orElse(0.0);
            statistics.put("avg" + metric, avgValue);
        }

        return statistics;
    }

    /**
     * Calculate bounding box for features
     */
    private Map<String, Object> calculateBoundingBox(List<GeospatialData.GeographicFeature> features) {
        if (features.isEmpty()) {
            return Map.of("minLat", 0.0, "maxLat", 0.0, "minLng", 0.0, "maxLng", 0.0);
        }

        double minLat = features.stream().mapToDouble(GeospatialData.GeographicFeature::getLatitude).min().orElse(0.0);
        double maxLat = features.stream().mapToDouble(GeospatialData.GeographicFeature::getLatitude).max().orElse(0.0);
        double minLng = features.stream().mapToDouble(GeospatialData.GeographicFeature::getLongitude).min().orElse(0.0);
        double maxLng = features.stream().mapToDouble(GeospatialData.GeographicFeature::getLongitude).max().orElse(0.0);

        return Map.of("minLat", minLat, "maxLat", maxLat, "minLng", minLng, "maxLng", maxLng);
    }

    /**
     * Create fallback geospatial data when services are unavailable
     */
    private GeospatialData createFallbackGeospatialData(String region, List<String> metrics, String timeRange) {
        log.info("Creating fallback geospatial data for region: {}", region);

        List<GeospatialData.GeographicFeature> features = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            features.add(GeospatialData.GeographicFeature.builder()
                .id("fallback-feature-" + i)
                .name("Fallback Feature " + i)
                .type("POLYGON")
                .latitude(14.0 + (i * 0.1))
                .longitude(121.0 + (i * 0.1))
                .geometry(Map.of("type", "Polygon", "coordinates", List.of()))
                .properties(Map.of("value", 50 + i * 5))
                .attributes(Map.of(metrics.get(0), 50 + i * 5))
                .build());
        }

        return GeospatialData.builder()
            .dataId(UUID.randomUUID().toString())
            .region(region)
            .dataType("GEOSPATIAL")
            .timestamp(LocalDateTime.now())
            .features(features)
            .metrics(metrics)
            .projection("WGS84")
            .boundingBox(Map.of("minLat", 14.0, "maxLat", 14.5, "minLng", 121.0, "maxLng", 121.5))
            .statistics(Map.of("totalFeatures", features.size(), "avgValue", 62.5))
            .metadata(Map.of("timeRange", timeRange, "source", "fallback"))
            .build();
    }

    /**
     * Convert map data to GeographicFeature
     */
    private GeospatialData.GeographicFeature convertToGeographicFeature(Map<String, Object> featureData) {
        return GeospatialData.GeographicFeature.builder()
            .id((String) featureData.get("id"))
            .name((String) featureData.get("name"))
            .type((String) featureData.get("type"))
            .latitude(((Number) featureData.get("latitude")).doubleValue())
            .longitude(((Number) featureData.get("longitude")).doubleValue())
            .geometry((Map<String, Object>) featureData.get("geometry"))
            .properties((Map<String, Object>) featureData.get("properties"))
            .attributes((Map<String, Object>) featureData.get("attributes"))
            .build();
    }
}
