package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Forecast Result DTO for forecasting analysis results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ForecastResult {
    private String forecastId;
    private String metric;
    private String method;
    private LocalDateTime forecastDate;
    private Integer horizon;
    private List<ForecastPoint> forecast;
    private Double accuracy;
    private Double confidenceLevel;
    private Map<String, Object> parameters;
    private Map<String, Object> metrics;
    private String status;
    
    @Data
    @Builder
    public static class ForecastPoint {
        private LocalDateTime date;
        private Double forecastValue;
        private Double lowerBound;
        private Double upperBound;
        private Double confidence;
        private Map<String, Object> metadata;
    }
}
