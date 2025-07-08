package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Spatial cluster model for geospatial analysis
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class SpatialCluster {
    
    /**
     * Unique cluster identifier
     */
    private String clusterId;
    
    /**
     * Cluster center coordinates
     */
    private ClusterCenter center;
    
    /**
     * Data points belonging to this cluster
     */
    private List<GeospatialData.GeospatialDataPoint> dataPoints;
    
    /**
     * Cluster radius in kilometers
     */
    private double radiusKm;
    
    /**
     * Cluster density (points per square km)
     */
    private double density;
    
    /**
     * Cluster significance score
     */
    private double significance;
    
    /**
     * Cluster type/category
     */
    private ClusterType type;
    
    /**
     * Statistical properties of the cluster
     */
    private ClusterStatistics statistics;
    
    /**
     * Additional cluster metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Cluster center coordinates
     */
    @Data
    @Builder
    public static class ClusterCenter {
        private double latitude;
        private double longitude;
        private String locationName;
    }
    
    /**
     * Cluster type enumeration
     */
    public enum ClusterType {
        HIGH_DENSITY,
        LOW_DENSITY,
        OUTLIER,
        HOTSPOT,
        COLDSPOT,
        NORMAL
    }
    
    /**
     * Statistical properties of the cluster
     */
    @Data
    @Builder
    public static class ClusterStatistics {
        private double meanValue;
        private double medianValue;
        private double standardDeviation;
        private double minValue;
        private double maxValue;
        private int pointCount;
        private double confidenceLevel;
    }
    
    /**
     * Get the number of data points in this cluster
     */
    public int getPointCount() {
        return dataPoints != null ? dataPoints.size() : 0;
    }
    
    /**
     * Check if this cluster is statistically significant
     */
    public boolean isSignificant() {
        return significance > 0.95; // 95% confidence threshold
    }
    
    /**
     * Calculate cluster area in square kilometers
     */
    public double getAreaSqKm() {
        return Math.PI * radiusKm * radiusKm;
    }
    
    /**
     * Get cluster intensity (significance * density)
     */
    public double getIntensity() {
        return significance * density;
    }
    
    /**
     * Check if a point is within this cluster's radius
     */
    public boolean containsPoint(double latitude, double longitude) {
        if (center == null) return false;
        
        double distance = calculateDistance(
            center.getLatitude(), center.getLongitude(),
            latitude, longitude
        );
        
        return distance <= radiusKm;
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
