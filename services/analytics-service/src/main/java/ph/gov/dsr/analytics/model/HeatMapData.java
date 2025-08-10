package ph.gov.dsr.analytics.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Heat map data for visualization
 * 
 * @author DSR Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@Builder
public class HeatMapData {
    
    /**
     * Heat map identifier
     */
    private String heatMapId;
    
    /**
     * Metric being visualized
     */
    private String metric;
    
    /**
     * Geographic bounds
     */
    private GeographicBounds bounds;
    
    /**
     * Grid resolution
     */
    private GridResolution resolution;
    
    /**
     * Heat map grid data
     */
    private double[][] gridValues;
    
    /**
     * Color scheme configuration
     */
    private ColorScheme colorScheme;
    
    /**
     * Contour lines (optional)
     */
    private List<ContourLine> contourLines;
    
    /**
     * Legend information
     */
    private Legend legend;
    
    /**
     * Generation timestamp
     */
    private LocalDateTime generatedAt;
    
    /**
     * Metadata
     */
    private Map<String, Object> metadata;
    
    /**
     * Geographic bounds
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
     * Grid resolution specification
     */
    @Data
    @Builder
    public static class GridResolution {
        private int width;  // number of columns
        private int height; // number of rows
        private double cellSizeKm; // size of each cell in km
    }
    
    /**
     * Color scheme for heat map
     */
    @Data
    @Builder
    public static class ColorScheme {
        private String name; // VIRIDIS, PLASMA, INFERNO, etc.
        private List<ColorStop> colorStops;
        private double minValue;
        private double maxValue;
    }
    
    /**
     * Color stop in the gradient
     */
    @Data
    @Builder
    public static class ColorStop {
        private double value;
        private String color; // hex color code
        private double opacity;
    }
    
    /**
     * Contour line
     */
    @Data
    @Builder
    public static class ContourLine {
        private double value;
        private List<Coordinate> coordinates;
        private String style;
    }
    
    /**
     * Geographic coordinate
     */
    @Data
    @Builder
    public static class Coordinate {
        private double latitude;
        private double longitude;
    }
    
    /**
     * Legend information
     */
    @Data
    @Builder
    public static class Legend {
        private String title;
        private String units;
        private double minValue;
        private double maxValue;
        private List<LegendItem> items;
    }
    
    /**
     * Legend item
     */
    @Data
    @Builder
    public static class LegendItem {
        private double value;
        private String label;
        private String color;
    }
    
    /**
     * Get value at specific grid coordinates
     */
    public double getValueAt(int row, int col) {
        if (gridValues == null || row < 0 || row >= gridValues.length || 
            col < 0 || col >= gridValues[0].length) {
            return Double.NaN;
        }
        return gridValues[row][col];
    }
    
    /**
     * Get value at geographic coordinates
     */
    public double getValueAt(double latitude, double longitude) {
        if (bounds == null || resolution == null) {
            return Double.NaN;
        }
        
        // Convert geographic coordinates to grid coordinates
        int row = (int) ((bounds.getNorthLatitude() - latitude) / 
                        (bounds.getNorthLatitude() - bounds.getSouthLatitude()) * resolution.getHeight());
        int col = (int) ((longitude - bounds.getWestLongitude()) / 
                        (bounds.getEastLongitude() - bounds.getWestLongitude()) * resolution.getWidth());
        
        return getValueAt(row, col);
    }
    
    /**
     * Get statistics about the heat map values
     */
    public HeatMapStatistics getStatistics() {
        if (gridValues == null) {
            return null;
        }
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        int count = 0;
        
        for (double[] row : gridValues) {
            for (double value : row) {
                if (!Double.isNaN(value)) {
                    min = Math.min(min, value);
                    max = Math.max(max, value);
                    sum += value;
                    count++;
                }
            }
        }
        
        return HeatMapStatistics.builder()
                .minValue(min)
                .maxValue(max)
                .meanValue(count > 0 ? sum / count : 0)
                .validCells(count)
                .totalCells(resolution.getWidth() * resolution.getHeight())
                .build();
    }
    
    /**
     * Heat map statistics
     */
    @Data
    @Builder
    public static class HeatMapStatistics {
        private double minValue;
        private double maxValue;
        private double meanValue;
        private int validCells;
        private int totalCells;
    }
}
