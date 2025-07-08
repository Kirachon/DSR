package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Geospatial Data DTO for geographic data representation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class GeospatialData {
    private String dataId;
    private String region;
    private String dataType;
    private LocalDateTime timestamp;
    private List<GeographicFeature> features;
    private Map<String, Object> metadata;
    private String projection;
    private Map<String, Object> boundingBox;
    private List<String> metrics;
    private Map<String, Object> statistics;
    
    @Data
    @Builder
    public static class GeographicFeature {
        private String id;
        private String name;
        private String type; // POINT, POLYGON, LINESTRING
        private Map<String, Object> geometry;
        private Map<String, Object> properties;
        private Double latitude;
        private Double longitude;
        private Map<String, Object> attributes;
    }
}
