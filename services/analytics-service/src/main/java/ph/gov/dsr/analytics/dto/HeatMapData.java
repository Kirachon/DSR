package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Heat Map Data DTO for heat map visualization data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class HeatMapData {
    private String heatMapId;
    private String name;
    private String metric;
    private List<HeatMapPoint> points;
    private Map<String, Object> configuration;
    private String colorScheme;
    private List<String> colorBreaks;
    private Double intensity;
    private Map<String, Object> boundingBox;
    
    @Data
    @Builder
    public static class HeatMapPoint {
        private Double latitude;
        private Double longitude;
        private Double value;
        private Double weight;
        private String label;
        private Map<String, Object> properties;
    }
}
