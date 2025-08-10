package ph.gov.dsr.datamanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for deduplication operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class DeduplicationRequest {

    @NotBlank(message = "Entity type is required")
    private String entityType; // HOUSEHOLD, INDIVIDUAL

    @NotNull(message = "Entity data is required")
    private Map<String, Object> entityData;

    private double matchThreshold = 0.8; // Similarity threshold for matches

    private String[] matchingFields; // Fields to use for matching

    private boolean includePartialMatches = true;

    private int maxResults = 10; // Maximum number of potential matches to return

    private String matchingAlgorithm = "FUZZY"; // FUZZY, EXACT, PHONETIC
}
