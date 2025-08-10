package ph.gov.dsr.datamanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.datamanagement.entity.ArchivedData;
import ph.gov.dsr.datamanagement.entity.RetentionPolicy;
import ph.gov.dsr.datamanagement.entity.Household;
import ph.gov.dsr.datamanagement.entity.HouseholdMember;
import ph.gov.dsr.datamanagement.repository.ArchivedDataRepository;
import ph.gov.dsr.datamanagement.repository.RetentionPolicyRepository;
import ph.gov.dsr.datamanagement.repository.HouseholdRepository;
import ph.gov.dsr.datamanagement.repository.HouseholdMemberRepository;
import ph.gov.dsr.datamanagement.service.HistoricalDataArchivingService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Production implementation of HistoricalDataArchivingService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Service
@Profile("!no-db")
@RequiredArgsConstructor
@Slf4j
public class HistoricalDataArchivingServiceImpl implements HistoricalDataArchivingService {

    private final ArchivedDataRepository archivedDataRepository;
    private final RetentionPolicyRepository retentionPolicyRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final ObjectMapper objectMapper;

    @Value("${dsr.data-management.archiving.retention-period-days:2555}")
    private int defaultRetentionDays;

    @Value("${dsr.data-management.archiving.auto-archive-enabled:true}")
    private boolean autoArchiveEnabled;

    @Value("${dsr.data-management.archiving.archive-batch-size:1000}")
    private int archiveBatchSize;

    @Override
    @Transactional
    public HistoricalDataArchivingService.ArchivingResult archiveOldData(String entityType, LocalDateTime cutoffDate) {
        log.info("Starting archival of old data for entity type: {} before date: {}", entityType, cutoffDate);
        
        HistoricalDataArchivingService.ArchivingResult result = new HistoricalDataArchivingService.ArchivingResult();
        result.setArchivedAt(LocalDateTime.now());
        result.setErrors(new ArrayList<>());
        
        try {
            // Get retention policy for entity type
            Optional<RetentionPolicy> policyOpt = retentionPolicyRepository
                    .findByEntityTypeAndIsActiveTrue(entityType);
            
            if (policyOpt.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("No active retention policy found for entity type: " + entityType);
                result.getErrors().add("Missing retention policy for " + entityType);
                return result;
            }
            
            RetentionPolicy policy = policyOpt.get();
            
            // Archive entities based on type
            int archivedCount = 0;
            switch (entityType.toUpperCase()) {
                case "HOUSEHOLD":
                    archivedCount = archiveHouseholds(cutoffDate, policy);
                    break;
                case "HOUSEHOLD_MEMBER":
                    archivedCount = archiveHouseholdMembers(cutoffDate, policy);
                    break;
                default:
                    result.setSuccess(false);
                    result.setMessage("Unsupported entity type for archival: " + entityType);
                    result.getErrors().add("Unsupported entity type: " + entityType);
                    return result;
            }
            
            result.setSuccess(true);
            result.setArchivedCount(archivedCount);
            result.setMessage(String.format("Successfully archived %d %s records", archivedCount, entityType));
            
            log.info("Completed archival of {} {} records", archivedCount, entityType);
            
        } catch (Exception e) {
            log.error("Error during bulk archival for entity type: {}", entityType, e);
            result.setSuccess(false);
            result.setMessage("Error during archival: " + e.getMessage());
            result.getErrors().add(e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public HistoricalDataArchivingService.ArchivingResult archiveEntity(UUID entityId, String entityType, String reason) {
        log.info("Archiving entity: {} of type: {} for reason: {}", entityId, entityType, reason);

        HistoricalDataArchivingService.ArchivingResult result = new HistoricalDataArchivingService.ArchivingResult();
        result.setArchivedAt(LocalDateTime.now());
        result.setErrors(new ArrayList<>());
        
        try {
            // Check if entity is already archived
            if (archivedDataRepository.existsByOriginalEntityIdAndEntityTypeAndArchiveStatus(
                    entityId, entityType, ArchivedData.ArchiveStatus.ACTIVE)) {
                result.setSuccess(false);
                result.setMessage("Entity is already archived");
                result.getErrors().add("Entity already archived: " + entityId);
                return result;
            }
            
            // Get retention policy
            Optional<RetentionPolicy> policyOpt = retentionPolicyRepository
                    .findByEntityTypeAndIsActiveTrue(entityType);
            
            RetentionPolicy policy = policyOpt.orElse(createDefaultRetentionPolicy(entityType));
            
            // Archive the specific entity
            boolean archived = archiveSpecificEntity(entityId, entityType, reason, policy);
            
            if (archived) {
                result.setSuccess(true);
                result.setArchivedCount(1);
                result.setMessage("Entity archived successfully");
            } else {
                result.setSuccess(false);
                result.setMessage("Entity not found or could not be archived");
                result.getErrors().add("Entity not found: " + entityId);
            }
            
        } catch (Exception e) {
            log.error("Error archiving entity: {} of type: {}", entityId, entityType, e);
            result.setSuccess(false);
            result.setMessage("Error during archival: " + e.getMessage());
            result.getErrors().add(e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public HistoricalDataArchivingService.RestoreResult restoreArchivedData(UUID archiveId) {
        log.info("Restoring archived data: {}", archiveId);

        HistoricalDataArchivingService.RestoreResult result = new HistoricalDataArchivingService.RestoreResult();
        result.setRestoredAt(LocalDateTime.now());
        result.setErrors(new ArrayList<>());
        
        try {
            Optional<ArchivedData> archivedOpt = archivedDataRepository.findById(archiveId);
            
            if (archivedOpt.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("Archived data not found");
                result.getErrors().add("Archive ID not found: " + archiveId);
                return result;
            }
            
            ArchivedData archived = archivedOpt.get();
            
            if (archived.isRestored()) {
                result.setSuccess(false);
                result.setMessage("Data has already been restored");
                result.getErrors().add("Archive already restored: " + archiveId);
                return result;
            }
            
            // Restore the data based on entity type
            boolean restored = restoreSpecificEntity(archived);
            
            if (restored) {
                // Mark as restored
                archived.markAsRestored("SYSTEM", "Manual restore request");
                archivedDataRepository.save(archived);
                
                result.setSuccess(true);
                result.setRestoredCount(1);
                result.setMessage("Data restored successfully");
            } else {
                result.setSuccess(false);
                result.setMessage("Failed to restore data");
                result.getErrors().add("Restoration failed for archive: " + archiveId);
            }
            
        } catch (Exception e) {
            log.error("Error restoring archived data: {}", archiveId, e);
            result.setSuccess(false);
            result.setMessage("Error during restoration: " + e.getMessage());
            result.getErrors().add(e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getArchivingStatistics() {
        log.debug("Getting archiving statistics");
        
        try {
            Object[] stats = archivedDataRepository.getArchivingStatistics();
            List<Object[]> countByType = archivedDataRepository.countArchivedDataByEntityType();
            List<Object[]> countByStatus = archivedDataRepository.countArchivedDataByStatus();
            
            Map<String, Object> result = new HashMap<>();
            
            if (stats != null && stats.length >= 6) {
                result.put("totalArchived", stats[0] != null ? ((Number) stats[0]).longValue() : 0L);
                result.put("activeArchives", stats[1] != null ? ((Number) stats[1]).longValue() : 0L);
                result.put("restoredArchives", stats[2] != null ? ((Number) stats[2]).longValue() : 0L);
                result.put("expiredArchives", stats[3] != null ? ((Number) stats[3]).longValue() : 0L);
                result.put("totalSizeBytes", stats[4] != null ? ((Number) stats[4]).longValue() : 0L);
                result.put("lastArchiveDate", stats[5]);
            } else {
                result.put("totalArchived", 0L);
                result.put("activeArchives", 0L);
                result.put("restoredArchives", 0L);
                result.put("expiredArchives", 0L);
                result.put("totalSizeBytes", 0L);
                result.put("lastArchiveDate", null);
            }
            
            // Add count by entity type
            Map<String, Long> countByTypeMap = countByType.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> ((Number) row[1]).longValue()
                    ));
            result.put("countByEntityType", countByTypeMap);
            
            // Add count by status
            Map<String, Long> countByStatusMap = countByStatus.stream()
                    .collect(Collectors.toMap(
                            row -> row[0].toString(),
                            row -> ((Number) row[1]).longValue()
                    ));
            result.put("countByStatus", countByStatusMap);
            
            // Add retention policy count
            long policyCount = retentionPolicyRepository.count();
            result.put("retentionPoliciesCount", policyCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("Error getting archiving statistics", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to retrieve statistics: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public void configureRetentionPolicy(String entityType, int retentionDays, boolean autoArchive) {
        log.info("Configuring retention policy for {}: {} days, auto-archive: {}",
                entityType, retentionDays, autoArchive);

        try {
            // Check if policy already exists
            Optional<RetentionPolicy> existingPolicy = retentionPolicyRepository
                    .findByEntityTypeAndIsActiveTrue(entityType);

            RetentionPolicy policy;
            if (existingPolicy.isPresent()) {
                // Update existing policy
                policy = existingPolicy.get();
                policy.setRetentionDays(retentionDays);
                policy.setAutoArchiveEnabled(autoArchive);
                policy.setUpdatedBy("SYSTEM");
            } else {
                // Create new policy
                policy = new RetentionPolicy();
                policy.setEntityType(entityType);
                policy.setRetentionDays(retentionDays);
                policy.setAutoArchiveEnabled(autoArchive);
                policy.setAutoDeleteEnabled(false);
                policy.setPolicyDescription("Configured retention policy for " + entityType);
                policy.setCreatedBy("SYSTEM");
                policy.setIsActive(true);
            }

            retentionPolicyRepository.save(policy);
            log.info("Retention policy configured successfully for {}", entityType);

        } catch (Exception e) {
            log.error("Error configuring retention policy for {}", entityType, e);
            throw new RuntimeException("Failed to configure retention policy: " + e.getMessage());
        }
    }

    @Override
    public List<HistoricalDataArchivingService.ArchivedDataRecord> getArchivedData(
            String entityType, LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("Getting archived data for type: {} from: {} to: {}", entityType, fromDate, toDate);

        try {
            List<ArchivedData> archivedDataList;

            if (entityType != null && fromDate != null && toDate != null) {
                archivedDataList = archivedDataRepository
                        .findByEntityTypeAndArchivedAtBetweenAndArchiveStatusOrderByArchivedAtDesc(
                                entityType, fromDate, toDate, ArchivedData.ArchiveStatus.ACTIVE);
            } else if (entityType != null) {
                archivedDataList = archivedDataRepository
                        .findByEntityTypeAndArchiveStatusOrderByArchivedAtDesc(
                                entityType, ArchivedData.ArchiveStatus.ACTIVE);
            } else if (fromDate != null && toDate != null) {
                archivedDataList = archivedDataRepository
                        .findByArchivedAtBetweenAndArchiveStatusOrderByArchivedAtDesc(
                                fromDate, toDate, ArchivedData.ArchiveStatus.ACTIVE);
            } else {
                // Get all active archived data (with reasonable limit)
                archivedDataList = archivedDataRepository
                        .findByArchiveStatusOrderByArchivedAtDesc(
                                ArchivedData.ArchiveStatus.ACTIVE, PageRequest.of(0, 1000))
                        .getContent();
            }

            // Convert to interface DTOs
            return archivedDataList.stream()
                    .map(this::convertToArchivedDataRecord)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting archived data", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isEntityArchived(UUID entityId, String entityType) {
        return archivedDataRepository.existsByOriginalEntityIdAndEntityTypeAndArchiveStatus(
                entityId, entityType, ArchivedData.ArchiveStatus.ACTIVE);
    }

    // Helper method to convert entity to DTO
    private HistoricalDataArchivingService.ArchivedDataRecord convertToArchivedDataRecord(ArchivedData archived) {
        HistoricalDataArchivingService.ArchivedDataRecord record =
                new HistoricalDataArchivingService.ArchivedDataRecord();
        record.setArchiveId(archived.getArchiveId());
        record.setOriginalEntityId(archived.getOriginalEntityId());
        record.setEntityType(archived.getEntityType());
        record.setArchivedData(archived.getArchivedData());
        record.setArchiveReason(archived.getArchiveReason());
        record.setArchivedAt(archived.getArchivedAt());
        record.setOriginalCreatedAt(archived.getOriginalCreatedAt());
        return record;
    }

    // Private helper methods

    private int archiveHouseholds(LocalDateTime cutoffDate, RetentionPolicy policy) {
        log.debug("Archiving households before date: {}", cutoffDate);

        // Find households to archive (simplified approach)
        int archivedCount = 0;
        int page = 0;
        List<Household> households;

        do {
            // Use findAll with pagination and filter in memory for now
            // In production, you'd want a custom query method
            households = householdRepository.findAll(PageRequest.of(page, archiveBatchSize)).getContent()
                    .stream()
                    .filter(h -> h.getCreatedAt() != null && h.getCreatedAt().isBefore(cutoffDate))
                    .collect(Collectors.toList());

            for (Household household : households) {
                try {
                    if (archiveHousehold(household, policy, "Automatic archival - retention policy")) {
                        archivedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error archiving household: {}", household.getId(), e);
                }
            }

            page++;
        } while (households.size() == archiveBatchSize);

        return archivedCount;
    }

    private int archiveHouseholdMembers(LocalDateTime cutoffDate, RetentionPolicy policy) {
        log.debug("Archiving household members before date: {}", cutoffDate);

        int archivedCount = 0;
        int page = 0;
        List<HouseholdMember> members;

        do {
            // Use findAll with pagination and filter in memory for now
            members = householdMemberRepository.findAll(PageRequest.of(page, archiveBatchSize)).getContent()
                    .stream()
                    .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isBefore(cutoffDate))
                    .collect(Collectors.toList());

            for (HouseholdMember member : members) {
                try {
                    if (archiveHouseholdMember(member, policy, "Automatic archival - retention policy")) {
                        archivedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error archiving household member: {}", member.getId(), e);
                }
            }

            page++;
        } while (members.size() == archiveBatchSize);

        return archivedCount;
    }

    private boolean archiveSpecificEntity(UUID entityId, String entityType, String reason, RetentionPolicy policy) {
        switch (entityType.toUpperCase()) {
            case "HOUSEHOLD":
                Optional<Household> household = householdRepository.findById(entityId);
                return household.map(h -> archiveHousehold(h, policy, reason)).orElse(false);
            case "HOUSEHOLD_MEMBER":
                Optional<HouseholdMember> member = householdMemberRepository.findById(entityId);
                return member.map(m -> archiveHouseholdMember(m, policy, reason)).orElse(false);
            default:
                log.warn("Unsupported entity type for archival: {}", entityType);
                return false;
        }
    }

    private boolean archiveHousehold(Household household, RetentionPolicy policy, String reason) {
        try {
            String householdJson = objectMapper.writeValueAsString(household);

            ArchivedData archived = new ArchivedData();
            archived.setOriginalEntityId(household.getId());
            archived.setEntityType("HOUSEHOLD");
            archived.setArchivedData(householdJson);
            archived.setArchiveReason(reason);
            archived.setArchivedBy("SYSTEM");
            archived.setOriginalCreatedAt(household.getCreatedAt());
            archived.setOriginalUpdatedAt(household.getUpdatedAt());
            archived.setRetentionUntil(policy.calculateRetentionUntil(LocalDateTime.now()));
            archived.setFileSizeBytes((long) householdJson.length());
            archived.setChecksum(calculateChecksum(householdJson));

            archivedDataRepository.save(archived);

            // Remove from active table
            householdRepository.delete(household);

            return true;
        } catch (Exception e) {
            log.error("Error archiving household: {}", household.getId(), e);
            return false;
        }
    }

    private boolean archiveHouseholdMember(HouseholdMember member, RetentionPolicy policy, String reason) {
        try {
            String memberJson = objectMapper.writeValueAsString(member);

            ArchivedData archived = new ArchivedData();
            archived.setOriginalEntityId(member.getId());
            archived.setEntityType("HOUSEHOLD_MEMBER");
            archived.setArchivedData(memberJson);
            archived.setArchiveReason(reason);
            archived.setArchivedBy("SYSTEM");
            archived.setOriginalCreatedAt(member.getCreatedAt());
            archived.setOriginalUpdatedAt(member.getUpdatedAt());
            archived.setRetentionUntil(policy.calculateRetentionUntil(LocalDateTime.now()));
            archived.setFileSizeBytes((long) memberJson.length());
            archived.setChecksum(calculateChecksum(memberJson));

            archivedDataRepository.save(archived);

            // Remove from active table
            householdMemberRepository.delete(member);

            return true;
        } catch (Exception e) {
            log.error("Error archiving household member: {}", member.getId(), e);
            return false;
        }
    }

    private boolean restoreSpecificEntity(ArchivedData archived) {
        try {
            switch (archived.getEntityType().toUpperCase()) {
                case "HOUSEHOLD":
                    Household household = objectMapper.readValue(archived.getArchivedData(), Household.class);
                    householdRepository.save(household);
                    return true;
                case "HOUSEHOLD_MEMBER":
                    HouseholdMember member = objectMapper.readValue(archived.getArchivedData(), HouseholdMember.class);
                    householdMemberRepository.save(member);
                    return true;
                default:
                    log.warn("Unsupported entity type for restoration: {}", archived.getEntityType());
                    return false;
            }
        } catch (Exception e) {
            log.error("Error restoring entity: {}", archived.getArchiveId(), e);
            return false;
        }
    }

    private RetentionPolicy createDefaultRetentionPolicy(String entityType) {
        RetentionPolicy policy = new RetentionPolicy();
        policy.setEntityType(entityType);
        policy.setRetentionDays(defaultRetentionDays);
        policy.setAutoArchiveEnabled(autoArchiveEnabled);
        policy.setAutoDeleteEnabled(false);
        policy.setPolicyDescription("Default retention policy");
        policy.setCreatedBy("SYSTEM");
        policy.setIsActive(true);
        return retentionPolicyRepository.save(policy);
    }

    private String calculateChecksum(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error calculating checksum", e);
            return null;
        }
    }

    // Result classes
    public static class ArchivingResult {
        private boolean success;
        private String message;
        private int archivedCount;
        private LocalDateTime archivedAt;
        private List<String> errors;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getArchivedCount() { return archivedCount; }
        public void setArchivedCount(int archivedCount) { this.archivedCount = archivedCount; }
        public LocalDateTime getArchivedAt() { return archivedAt; }
        public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }

    public static class RestoreResult {
        private boolean success;
        private String message;
        private int restoredCount;
        private LocalDateTime restoredAt;
        private List<String> errors;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getRestoredCount() { return restoredCount; }
        public void setRestoredCount(int restoredCount) { this.restoredCount = restoredCount; }
        public LocalDateTime getRestoredAt() { return restoredAt; }
        public void setRestoredAt(LocalDateTime restoredAt) { this.restoredAt = restoredAt; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }

    public static class CleanupResult {
        private boolean success;
        private String message;
        private int cleanedCount;
        private LocalDateTime cleanedAt;
        private List<String> errors;

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getCleanedCount() { return cleanedCount; }
        public void setCleanedCount(int cleanedCount) { this.cleanedCount = cleanedCount; }
        public LocalDateTime getCleanedAt() { return cleanedAt; }
        public void setCleanedAt(LocalDateTime cleanedAt) { this.cleanedAt = cleanedAt; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}
