package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Correlation Result DTO
 * Contains results of security event correlation analysis
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationResult {

    /**
     * Correlation ID
     */
    private String correlationId;

    /**
     * Correlation type
     */
    private String correlationType;

    /**
     * Correlation score (0-100)
     */
    private Integer correlationScore;

    /**
     * Confidence level (0-100)
     */
    private Integer confidence;

    /**
     * Correlated events
     */
    private List<String> correlatedEvents;

    /**
     * Correlation patterns
     */
    private List<String> correlationPatterns;

    /**
     * Correlation details
     */
    private Map<String, Object> correlationDetails;

    /**
     * Analysis timestamp
     */
    private LocalDateTime analyzedAt;

    /**
     * Check if correlation is significant
     */
    public boolean isSignificant() {
        return correlationScore != null && correlationScore >= 70;
    }

    /**
     * Get number of correlated events
     */
    public int getCorrelatedEventCount() {
        return correlatedEvents != null ? correlatedEvents.size() : 0;
    }

    /**
     * Create high-correlation result
     */
    public static CorrelationResult highCorrelation(String type, List<String> events, int score) {
        return CorrelationResult.builder()
                .correlationId(java.util.UUID.randomUUID().toString())
                .correlationType(type)
                .correlationScore(score)
                .confidence(85)
                .correlatedEvents(events)
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate correlation result consistency
     */
    public boolean isValid() {
        return correlationId != null && !correlationId.trim().isEmpty() &&
               correlationType != null && !correlationType.trim().isEmpty() &&
               analyzedAt != null &&
               (correlationScore == null || (correlationScore >= 0 && correlationScore <= 100)) &&
               (confidence == null || (confidence >= 0 && confidence <= 100));
    }
}
