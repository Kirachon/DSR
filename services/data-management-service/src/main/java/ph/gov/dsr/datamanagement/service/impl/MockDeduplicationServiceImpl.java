package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.DeduplicationRequest;
import ph.gov.dsr.datamanagement.dto.DeduplicationResponse;
import ph.gov.dsr.datamanagement.service.DeduplicationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mock implementation of DeduplicationService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockDeduplicationServiceImpl implements DeduplicationService {

    @Override
    public DeduplicationResponse findDuplicates(DeduplicationRequest request) {
        log.info("Mock finding duplicates for entity type: {}", request.getEntityType());
        
        DeduplicationResponse response = new DeduplicationResponse();
        response.setHasDuplicates(false); // Mock: no duplicates found
        response.setTotalMatches(0);
        response.setProcessedAt(LocalDateTime.now());
        response.setMatches(new ArrayList<>());
        response.setRecommendation("PROCEED");
        response.setProcessingTimeMs(200L);
        
        // Occasionally return a potential duplicate for demonstration
        if (request.getEntityData().containsKey("firstName") && 
            "JUAN".equals(request.getEntityData().get("firstName"))) {
            
            response.setHasDuplicates(true);
            response.setTotalMatches(1);
            response.setRecommendation("REVIEW_REQUIRED");
            
            DeduplicationResponse.DuplicateMatch match = new DeduplicationResponse.DuplicateMatch();
            match.setExistingEntityId(UUID.randomUUID());
            match.setSimilarityScore(0.85);
            match.setMatchType("FUZZY");
            
            DeduplicationResponse.FieldMatch fieldMatch = new DeduplicationResponse.FieldMatch();
            fieldMatch.setFieldName("firstName");
            fieldMatch.setNewValue("JUAN");
            fieldMatch.setExistingValue("JUAN CARLOS");
            fieldMatch.setFieldSimilarity(0.8);
            fieldMatch.setMatchReason("Partial name match");
            
            match.setFieldMatches(List.of(fieldMatch));
            
            Map<String, Object> existingData = new HashMap<>();
            existingData.put("firstName", "JUAN CARLOS");
            existingData.put("lastName", "DELA CRUZ");
            existingData.put("psn", "1234-5678-9012");
            match.setExistingEntityData(existingData);
            
            response.setMatches(List.of(match));
        }
        
        return response;
    }

    @Override
    public boolean isDuplicate(String entityType, Map<String, Object> entityData, double threshold) {
        log.info("Mock checking if entity is duplicate for type: {}", entityType);
        
        // Mock logic: return true if firstName is "DUPLICATE"
        return "DUPLICATE".equals(entityData.get("firstName"));
    }

    @Override
    public void mergeDuplicates(UUID primaryEntityId, UUID duplicateEntityId, String entityType) {
        log.info("Mock merging duplicates: {} <- {} for type: {}", 
                primaryEntityId, duplicateEntityId, entityType);
        // Mock implementation - would perform actual merge in real implementation
    }

    @Override
    public void markAsNotDuplicates(UUID entityId1, UUID entityId2, String entityType) {
        log.info("Mock marking entities as not duplicates: {} and {} for type: {}", 
                entityId1, entityId2, entityType);
        // Mock implementation - would store in database in real implementation
    }

    @Override
    public Map<String, Object> getDeduplicationStatistics() {
        log.info("Mock getting deduplication statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDuplicatesFound", 150);
        stats.put("duplicatesResolved", 120);
        stats.put("duplicatesPending", 30);
        stats.put("averageProcessingTimeMs", 250);
        stats.put("lastProcessedAt", LocalDateTime.now().minusHours(2));
        
        return stats;
    }

    @Override
    public void configureMatchingAlgorithm(String algorithm, Map<String, Object> parameters) {
        log.info("Mock configuring matching algorithm: {} with parameters: {}", algorithm, parameters);
        // Mock implementation - would store configuration in real implementation
    }
}
