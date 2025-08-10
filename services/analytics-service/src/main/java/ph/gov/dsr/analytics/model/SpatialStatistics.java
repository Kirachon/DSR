package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Spatial statistics for geospatial analysis
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class SpatialStatistics {
    
    /**
     * Moran's I statistic for spatial autocorrelation
     */
    private double moransI;
    
    /**
     * P-value for Moran's I
     */
    private double moransIPValue;
    
    /**
     * Getis-Ord G statistic
     */
    private double getisOrdG;
    
    /**
     * P-value for Getis-Ord G
     */
    private double getisOrdGPValue;
    
    /**
     * Spatial clustering index
     */
    private double clusteringIndex;
    
    /**
     * Spatial dispersion measure
     */
    private double dispersionIndex;
    
    /**
     * Nearest neighbor statistics
     */
    private NearestNeighborStats nearestNeighborStats;
    
    /**
     * Ripley's K function results
     */
    private RipleysKStats ripleysKStats;
    
    /**
     * Additional spatial metrics
     */
    private Map<String, Double> additionalMetrics;
    
    /**
     * Nearest neighbor analysis results
     */
    @Data
    @Builder
    public static class NearestNeighborStats {
        private double meanDistance;
        private double expectedDistance;
        private double ratio;
        private double zScore;
        private double pValue;
        private String pattern; // CLUSTERED, DISPERSED, RANDOM
    }
    
    /**
     * Ripley's K function analysis
     */
    @Data
    @Builder
    public static class RipleysKStats {
        private double[] distances;
        private double[] kValues;
        private double[] lValues;
        private double maxDeviation;
        private String pattern; // CLUSTERED, DISPERSED, RANDOM
    }
    
    /**
     * Check if spatial autocorrelation is significant
     */
    public boolean hasSignificantAutocorrelation() {
        return moransIPValue < 0.05;
    }
    
    /**
     * Get spatial pattern description
     */
    public String getSpatialPattern() {
        if (hasSignificantAutocorrelation()) {
            if (moransI > 0) {
                return "Clustered";
            } else {
                return "Dispersed";
            }
        }
        return "Random";
    }
    
    /**
     * Check if clustering is statistically significant
     */
    public boolean hasSignificantClustering() {
        return getisOrdGPValue < 0.05 && getisOrdG > 0;
    }
}
