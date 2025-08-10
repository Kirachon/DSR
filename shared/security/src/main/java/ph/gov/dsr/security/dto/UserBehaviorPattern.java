package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * User Behavior Pattern DTO
 * Contains user behavioral pattern information
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorPattern {

    /**
     * Pattern ID
     */
    private String patternId;

    /**
     * User ID
     */
    private String userId;

    /**
     * Pattern type
     */
    private String patternType;

    /**
     * Pattern name
     */
    private String patternName;

    /**
     * Pattern description
     */
    private String description;

    /**
     * Pattern data
     */
    private Map<String, Object> patternData;

    /**
     * Pattern frequency
     */
    private String frequency;

    /**
     * Pattern strength
     */
    private String strength;

    /**
     * Pattern creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last observed timestamp
     */
    private LocalDateTime lastObserved;

    /**
     * Pattern metadata
     */
    private Map<String, Object> metadata;

    /**
     * Check if pattern is recent
     */
    public boolean isRecent() {
        if (lastObserved == null) return false;
        return java.time.Duration.between(lastObserved, LocalDateTime.now()).toHours() < 24;
    }

    /**
     * Get pattern age in days
     */
    public long getPatternAgeDays() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
    }

    /**
     * Create behavior pattern
     */
    public static UserBehaviorPattern create(String userId, String type, String name) {
        return UserBehaviorPattern.builder()
                .patternId(java.util.UUID.randomUUID().toString())
                .userId(userId)
                .patternType(type)
                .patternName(name)
                .frequency("REGULAR")
                .strength("MODERATE")
                .createdAt(LocalDateTime.now())
                .lastObserved(LocalDateTime.now())
                .build();
    }

    /**
     * Validate pattern consistency
     */
    public boolean isValid() {
        return patternId != null && !patternId.trim().isEmpty() &&
               userId != null && !userId.trim().isEmpty() &&
               patternType != null && !patternType.trim().isEmpty() &&
               patternName != null && !patternName.trim().isEmpty() &&
               createdAt != null;
    }
}
