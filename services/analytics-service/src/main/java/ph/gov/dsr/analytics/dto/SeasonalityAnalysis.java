package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Seasonality Analysis DTO for seasonal pattern analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class SeasonalityAnalysis {
    private String analysisId;
    private String metric;
    private Boolean hasSeasonality;
    private String seasonalPattern; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private Double seasonalStrength;
    private Map<String, Double> seasonalFactors;
    private List<SeasonalPeak> seasonalPeaks;
    private Integer period;
    private Double amplitude;
    private Double phase;
    private String method;
    private Double confidence;
    private Map<String, Object> parameters;
    
    @Data
    @Builder
    public static class SeasonalPeak {
        private String period;
        private Double value;
        private String description;
        private Double significance;
    }
}
