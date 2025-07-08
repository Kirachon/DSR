package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Trend Detection Result DTO for trend detection analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class TrendDetectionResult {
    private String trendId;
    private String metric;
    private String trendDirection; // UPWARD, DOWNWARD, STABLE, VOLATILE
    private Double trendSlope;
    private Double trendIntercept;
    private String trendStrength; // STRONG, MODERATE, WEAK, NONE
    private Double trendCorrelation;
    private Double rSquared;
    private String trendEquation;
    private LocalDateTime detectionDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<TrendPoint> trendPoints;
    private Map<String, Object> parameters;
    private String method;
    private Double confidence;
    
    @Data
    @Builder
    public static class TrendPoint {
        private LocalDateTime timestamp;
        private Double actualValue;
        private Double trendValue;
        private Double residual;
        private Double confidence;
    }
}
