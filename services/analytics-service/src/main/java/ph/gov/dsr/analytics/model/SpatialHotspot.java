package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Spatial hotspot detection result
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class SpatialHotspot {
    
    /**
     * Hotspot identifier
     */
    private String hotspotId;
    
    /**
     * Hotspot type
     */
    private HotspotType type;
    
    /**
     * Center coordinates
     */
    private HotspotCenter center;
    
    /**
     * Hotspot intensity
     */
    private double intensity;
    
    /**
     * Statistical significance
     */
    private double significance;
    
    /**
     * Confidence level
     */
    private double confidence;
    
    /**
     * Z-score
     */
    private double zScore;
    
    /**
     * P-value
     */
    private double pValue;
    
    /**
     * Hotspot radius in kilometers
     */
    private double radiusKm;
    
    /**
     * Data points within the hotspot
     */
    private List<GeospatialData.GeospatialDataPoint> dataPoints;
    
    /**
     * Additional attributes
     */
    private Map<String, Object> attributes;
    
    /**
     * Hotspot type enumeration
     */
    public enum HotspotType {
        HIGH_HIGH,    // High values surrounded by high values
        LOW_LOW,      // Low values surrounded by low values
        HIGH_LOW,     // High values surrounded by low values (outlier)
        LOW_HIGH,     // Low values surrounded by high values (outlier)
        NOT_SIGNIFICANT
    }
    
    /**
     * Hotspot center coordinates
     */
    @Data
    @Builder
    public static class HotspotCenter {
        private double latitude;
        private double longitude;
        private String locationName;
        private String administrativeArea;
    }
    
    /**
     * Check if hotspot is statistically significant
     */
    public boolean isSignificant() {
        return pValue < 0.05;
    }
    
    /**
     * Check if this is a high-value hotspot
     */
    public boolean isHighValueHotspot() {
        return type == HotspotType.HIGH_HIGH && isSignificant();
    }
    
    /**
     * Check if this is a low-value coldspot
     */
    public boolean isLowValueColdspot() {
        return type == HotspotType.LOW_LOW && isSignificant();
    }
    
    /**
     * Check if this is an outlier
     */
    public boolean isOutlier() {
        return (type == HotspotType.HIGH_LOW || type == HotspotType.LOW_HIGH) && isSignificant();
    }
    
    /**
     * Get hotspot description
     */
    public String getDescription() {
        if (!isSignificant()) {
            return "Not statistically significant";
        }
        
        return switch (type) {
            case HIGH_HIGH -> "High-value hotspot";
            case LOW_LOW -> "Low-value coldspot";
            case HIGH_LOW -> "High-value outlier";
            case LOW_HIGH -> "Low-value outlier";
            case NOT_SIGNIFICANT -> "Not significant";
        };
    }
    
    /**
     * Get the number of data points in this hotspot
     */
    public int getPointCount() {
        return dataPoints != null ? dataPoints.size() : 0;
    }
    
    /**
     * Calculate hotspot area in square kilometers
     */
    public double getAreaSqKm() {
        return Math.PI * radiusKm * radiusKm;
    }
}
