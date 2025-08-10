package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.service.HistoricalDataArchivingService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock implementation of HistoricalDataArchivingService for testing and development
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockHistoricalDataArchivingServiceImpl implements HistoricalDataArchivingService {

    private final Map<String, Integer> retentionPolicies = new HashMap<>();
    private final Map<UUID, ArchivedDataRecord> archivedData = new HashMap<>();
    private final Map<String, Integer> archivingStats = new HashMap<>();

    @Override
    public ArchivingResult archiveOldData(String entityType, LocalDateTime cutoffDate) {
        log.info("Mock archiving old data for entity type: {} before date: {}", entityType, cutoffDate);
        
        ArchivingResult result = new ArchivingResult();
        result.setSuccess(true);
        result.setArchivedCount(5); // Mock count
        result.setMessage("Mock archiving completed successfully");
        result.setArchivedAt(LocalDateTime.now());
        result.setErrors(new ArrayList<>());
        
        // Update statistics
        archivingStats.put("totalArchived", archivingStats.getOrDefault("totalArchived", 0) + 5);
        
        return result;
    }

    @Override
    public ArchivingResult archiveEntity(UUID entityId, String entityType, String reason) {
        log.info("Mock archiving entity: {} of type: {} for reason: {}", entityId, entityType, reason);
        
        ArchivingResult result = new ArchivingResult();
        result.setSuccess(true);
        result.setArchivedCount(1);
        result.setMessage("Entity archived successfully");
        result.setArchivedAt(LocalDateTime.now());
        result.setErrors(new ArrayList<>());
        
        // Create mock archived record
        ArchivedDataRecord record = new ArchivedDataRecord();
        record.setArchiveId(UUID.randomUUID());
        record.setOriginalEntityId(entityId);
        record.setEntityType(entityType);
        record.setArchivedData("{\"mockData\": \"archived\"}");
        record.setArchiveReason(reason);
        record.setArchivedAt(LocalDateTime.now());
        record.setOriginalCreatedAt(LocalDateTime.now().minusDays(30));
        
        archivedData.put(record.getArchiveId(), record);
        
        return result;
    }

    @Override
    public RestoreResult restoreArchivedData(UUID archiveId) {
        log.info("Mock restoring archived data: {}", archiveId);
        
        RestoreResult result = new RestoreResult();
        
        if (archivedData.containsKey(archiveId)) {
            result.setSuccess(true);
            result.setRestoredCount(1);
            result.setMessage("Data restored successfully");
            result.setRestoredAt(LocalDateTime.now());
            result.setErrors(new ArrayList<>());
            
            // Remove from archived data
            archivedData.remove(archiveId);
        } else {
            result.setSuccess(false);
            result.setRestoredCount(0);
            result.setMessage("Archive not found");
            result.setErrors(Arrays.asList("Archive ID not found: " + archiveId));
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getArchivingStatistics() {
        log.info("Mock getting archiving statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArchived", archivingStats.getOrDefault("totalArchived", 0));
        stats.put("totalRestored", archivingStats.getOrDefault("totalRestored", 0));
        stats.put("currentArchivedCount", archivedData.size());
        stats.put("retentionPoliciesCount", retentionPolicies.size());
        stats.put("lastArchiveDate", LocalDateTime.now().minusHours(2));
        
        return stats;
    }

    @Override
    public void configureRetentionPolicy(String entityType, int retentionDays, boolean autoArchive) {
        log.info("Mock configuring retention policy for {}: {} days, auto: {}", 
                entityType, retentionDays, autoArchive);
        
        retentionPolicies.put(entityType, retentionDays);
    }

    @Override
    public List<ArchivedDataRecord> getArchivedData(String entityType, LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Mock getting archived data for type: {} from: {} to: {}", entityType, fromDate, toDate);
        
        return archivedData.values().stream()
                .filter(record -> entityType == null || entityType.equals(record.getEntityType()))
                .filter(record -> fromDate == null || record.getArchivedAt().isAfter(fromDate))
                .filter(record -> toDate == null || record.getArchivedAt().isBefore(toDate))
                .toList();
    }

    @Override
    public boolean isEntityArchived(UUID entityId, String entityType) {
        log.debug("Mock checking if entity is archived: {} of type: {}", entityId, entityType);
        
        return archivedData.values().stream()
                .anyMatch(record -> record.getOriginalEntityId().equals(entityId) && 
                                  record.getEntityType().equals(entityType));
    }
}
