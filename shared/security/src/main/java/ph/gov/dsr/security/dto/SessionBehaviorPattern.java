package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Session Behavior Pattern DTO
 * Contains session-specific behavioral pattern information
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionBehaviorPattern {

    /**
     * Pattern ID
     */
    private String patternId;

    /**
     * Session ID
     */
    private String sessionId;

    /**
     * User ID
     */
    private String userId;

    /**
     * Pattern type
     */
    private String patternType;

    /**
     * Pattern description
     */
    private String description;

    /**
     * Session duration patterns
     */
    private Map<String, Object> durationPatterns;

    /**
     * Activity patterns
     */
    private Map<String, Object> activityPatterns;

    /**
     * Navigation patterns
     */
    private Map<String, Object> navigationPatterns;

    /**
     * Interaction patterns
     */
    private Map<String, Object> interactionPatterns;

    /**
     * Pattern detection timestamp
     */
    private LocalDateTime detectedAt;

    /**
     * Pattern metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if pattern is anomalous
     */
    public boolean isAnomalous() {
        return "ANOMALOUS".equalsIgnoreCase(patternType);
    }

    /**
     * Get pattern age in minutes
     */
    public long getPatternAgeMinutes() {
        if (detectedAt == null) return 0;
        return java.time.Duration.between(detectedAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Create session pattern
     */
    public static SessionBehaviorPattern create(String sessionId, String userId, String type) {
        return SessionBehaviorPattern.builder()
                .patternId(java.util.UUID.randomUUID().toString())
                .sessionId(sessionId)
                .userId(userId)
                .patternType(type)
                .detectedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Validate pattern consistency
     */
    public boolean isValid() {
        return patternId != null && !patternId.trim().isEmpty() &&
               sessionId != null && !sessionId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               patternType != null && !patternType.trim().isEmpty() &&
               detectedAt != null;
    }
}
