package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Geospatial data model for analytics
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class GeospatialData {
    
    /**
     * Geographic region identifier
     */
    private String region;
    
    /**
     * Coordinate boundaries
     */
    private GeographicBounds bounds;
    
    /**
     * Data points with geographic coordinates
     */
    private List<GeospatialDataPoint> dataPoints;
    
    /**
     * Metrics included in this dataset
     */
    private List<String> metrics;
    
    /**
     * Time range of the data
     */
    private TimeRange timeRange;
    
    /**
     * Data collection timestamp
     */
    private LocalDateTime collectedAt;
    
    /**
     * Metadata about the data source
     */
    private Map<String, Object> metadata;
    
    /**
     * Data quality indicators
     */
    private DataQuality quality;
    
    /**
     * Geographic bounds definition
     */
    @Data
    @Builder
    public static class GeographicBounds {
        private double northLatitude;
        private double southLatitude;
        private double eastLongitude;
        private double westLongitude;
    }
    
    /**
     * Individual geospatial data point
     */
    @Data
    @Builder
    public static class GeospatialDataPoint {
        private double latitude;
        private double longitude;
        private String locationName;
        private Map<String, Object> values;
        private LocalDateTime timestamp;
    }
    
    /**
     * Time range specification
     */
    @Data
    @Builder
    public static class TimeRange {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String granularity; // DAILY, WEEKLY, MONTHLY, etc.
    }
    
    /**
     * Data quality assessment
     */
    @Data
    @Builder
    public static class DataQuality {
        private double completeness; // 0-1
        private double accuracy; // 0-1
        private double timeliness; // 0-1
        private List<String> issues;
    }
    
    /**
     * Get total number of data points
     */
    public int getDataPointCount() {
        return dataPoints != null ? dataPoints.size() : 0;
    }
    
    /**
     * Check if data covers the specified region
     */
    public boolean coversRegion(String regionId) {
        return region != null && region.equals(regionId);
    }
    
    /**
     * Get data points within a specific time range
     */
    public List<GeospatialDataPoint> getDataPointsInRange(LocalDateTime start, LocalDateTime end) {
        if (dataPoints == null) return List.of();
        
        return dataPoints.stream()
                .filter(point -> point.getTimestamp() != null)
                .filter(point -> !point.getTimestamp().isBefore(start))
                .filter(point -> !point.getTimestamp().isAfter(end))
                .toList();
    }
}
