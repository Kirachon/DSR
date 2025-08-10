package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Spatial Statistics DTO for spatial analysis statistics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class SpatialStatistics {
    private String statisticsId;
    private Double moranI;
    private Double gearyC;
    private String spatialAutocorrelation; // POSITIVE, NEGATIVE, RANDOM
    private Double pValue;
    private String significance;
    private Double mean;
    private Double median;
    private Double standardDeviation;
    private Double minimum;
    private Double maximum;
    private Integer totalFeatures;
    private Map<String, Object> additionalMetrics;
}
