package ph.gov.dsr.datamanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for deduplication operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class DeduplicationResponse {

    private boolean hasDuplicates;

    private int totalMatches;

    private LocalDateTime processedAt;

    private List<DuplicateMatch> matches;

    private String recommendation; // PROCEED, REVIEW_REQUIRED, REJECT

    private long processingTimeMs;

    // Additional fields for test compatibility
    private String entityType; // HOUSEHOLD, INDIVIDUAL

    private String matchingAlgorithm; // FUZZY, EXACT, PHONETIC

    private double matchThreshold; // Similarity threshold used
    
    @Data
    public static class DuplicateMatch {
        private UUID existingEntityId;
        private double similarityScore;
        private String matchType; // EXACT, FUZZY, PHONETIC
        private List<FieldMatch> fieldMatches;
        private Map<String, Object> existingEntityData;
    }
    
    @Data
    public static class FieldMatch {
        private String fieldName;
        private String newValue;
        private String existingValue;
        private double fieldSimilarity;
        private String matchReason;
    }
}
