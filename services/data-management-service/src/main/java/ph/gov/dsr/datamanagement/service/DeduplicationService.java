package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.dto.DeduplicationRequest;
import ph.gov.dsr.datamanagement.dto.DeduplicationResponse;

import java.util.Map;
import java.util.UUID;

/**
 * Service interface for deduplication operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface DeduplicationService {

    /**
     * Find potential duplicates for entity
     */
    DeduplicationResponse findDuplicates(DeduplicationRequest request);

    /**
     * Check if entity is duplicate
     */
    boolean isDuplicate(String entityType, Map<String, Object> entityData, double threshold);

    /**
     * Merge duplicate entities
     */
    void mergeDuplicates(UUID primaryEntityId, UUID duplicateEntityId, String entityType);

    /**
     * Mark entities as not duplicates
     */
    void markAsNotDuplicates(UUID entityId1, UUID entityId2, String entityType);

    /**
     * Get deduplication statistics
     */
    Map<String, Object> getDeduplicationStatistics();

    /**
     * Configure matching algorithm parameters
     */
    void configureMatchingAlgorithm(String algorithm, Map<String, Object> parameters);
}
