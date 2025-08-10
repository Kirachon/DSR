package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Spatial Hotspot DTO for hotspot analysis results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class SpatialHotspot {
    private String hotspotId;
    private String featureId;
    private String type; // HOT, COLD
    private String intensity; // HIGH, MEDIUM, LOW
    private Double zScore;
    private Double pValue;
    private String significance;
    private Double latitude;
    private Double longitude;
    private Map<String, Object> attributes;
    private String description;
}
