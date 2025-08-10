package ph.gov.dsr.datamanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for historical data archiving operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface HistoricalDataArchivingService {

    /**
     * Archive old data based on retention policy
     */
    ArchivingResult archiveOldData(String entityType, LocalDateTime cutoffDate);

    /**
     * Archive specific entity
     */
    ArchivingResult archiveEntity(UUID entityId, String entityType, String reason);

    /**
     * Restore archived data
     */
    RestoreResult restoreArchivedData(UUID archiveId);

    /**
     * Get archiving statistics
     */
    Map<String, Object> getArchivingStatistics();

    /**
     * Configure retention policies
     */
    void configureRetentionPolicy(String entityType, int retentionDays, boolean autoArchive);

    /**
     * Get archived data by criteria
     */
    List<ArchivedDataRecord> getArchivedData(String entityType, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Check if entity is archived
     */
    boolean isEntityArchived(UUID entityId, String entityType);

    /**
     * Archiving result
     */
    class ArchivingResult {
        private boolean success;
        private int archivedCount;
        private String message;
        private LocalDateTime archivedAt;
        private List<String> errors;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public int getArchivedCount() { return archivedCount; }
        public void setArchivedCount(int archivedCount) { this.archivedCount = archivedCount; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getArchivedAt() { return archivedAt; }
        public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }

    /**
     * Restore result
     */
    class RestoreResult {
        private boolean success;
        private int restoredCount;
        private String message;
        private LocalDateTime restoredAt;
        private List<String> errors;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public int getRestoredCount() { return restoredCount; }
        public void setRestoredCount(int restoredCount) { this.restoredCount = restoredCount; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getRestoredAt() { return restoredAt; }
        public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }

    /**
     * Archived data record
     */
    class ArchivedDataRecord {
        private UUID archiveId;
        private UUID originalEntityId;
        private String entityType;
        private String archivedData;
        private String archiveReason;
        private LocalDateTime archivedAt;
        private LocalDateTime originalCreatedAt;

        // Getters and setters
        public UUID getArchiveId() { return archiveId; }
        public void setArchiveId(UUID archiveId) { this.archiveId = archiveId; }
        
        public UUID getOriginalEntityId() { return originalEntityId; }
        public void setOriginalEntityId(UUID originalEntityId) { this.originalEntityId = originalEntityId; }
        
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        
        public String getArchivedData() { return archivedData; }
        public void setArchivedData(String archivedData) { this.archivedData = archivedData; }
        
        public String getArchiveReason() { return archiveReason; }
        public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }
        
        public LocalDateTime getArchivedAt() { return archivedAt; }
        public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
        
        public LocalDateTime getOriginalCreatedAt() { return originalCreatedAt; }
        public void setOriginalCreatedAt(LocalDateTime originalCreatedAt) { this.originalCreatedAt = originalCreatedAt; }
    }
}
