package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Forecast Accuracy DTO for forecast accuracy metrics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ForecastAccuracy {
    private String accuracyId;
    private String metric;
    private String method;
    private Double mae; // Mean Absolute Error
    private Double mse; // Mean Squared Error
    private Double rmse; // Root Mean Squared Error
    private Double mape; // Mean Absolute Percentage Error
    private Double smape; // Symmetric Mean Absolute Percentage Error
    private Double r2; // R-squared
    private Double accuracy;
    private String accuracyLevel; // HIGH, MEDIUM, LOW
    private Map<String, Object> additionalMetrics;
    private String description;
}
