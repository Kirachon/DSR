package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Spatial Cluster DTO for spatial clustering results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class SpatialCluster {
    private String clusterId;
    private String clusterType; // HIGH_HIGH, LOW_LOW, HIGH_LOW, LOW_HIGH
    private List<String> featureIds;
    private Map<String, Object> centroid;
    private Double significance;
    private Double zScore;
    private Integer memberCount;
    private Double density;
    private Map<String, Object> statistics;
    private String description;
}
