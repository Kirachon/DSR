package ph.gov.dsr.disaster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive Backup Service
 * Handles automated backups for databases, configurations, and application data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final DatabaseBackupService databaseBackupService;
    private final FileSystemBackupService fileSystemBackupService;
    private final ConfigurationBackupService configBackupService;
    private final EncryptionService encryptionService;
    private final CompressionService compressionService;
    private final StorageService storageService;

    @Value("${dsr.backup.base-path:/backup/dsr}")
    private String backupBasePath;

    @Value("${dsr.backup.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Value("${dsr.backup.compression.enabled:true}")
    private boolean compressionEnabled;

    @Value("${dsr.backup.verification.enabled:true}")
    private boolean verificationEnabled;

    @Value("${dsr.backup.remote-storage.enabled:true}")
    private boolean remoteStorageEnabled;

    // Backup tracking
    private final Map<String, BackupExecution> activeBackups = new ConcurrentHashMap<>();
    private final Map<String, BackupMetadata> backupRegistry = new ConcurrentHashMap<>();

    /**
     * Execute comprehensive backup plan
     */
    public BackupResult executeBackup(BackupPlan plan) {
        try {
            log.info("Starting backup execution: {}", plan.getId());
            
            // Create backup execution context
            BackupExecution execution = BackupExecution.builder()
                .id(UUID.randomUUID().toString())
                .planId(plan.getId())
                .startTime(LocalDateTime.now())
                .status(BackupStatus.IN_PROGRESS)
                .build();
            
            activeBackups.put(execution.getId(), execution);
            
            // Create backup directory
            String backupDir = createBackupDirectory(plan);
            
            List<BackupComponentResult> componentResults = new ArrayList<>();
            
            // Execute backup for each component
            for (String component : plan.getComponents()) {
                BackupComponentResult result = backupComponent(component, backupDir, plan);
                componentResults.add(result);
                
                if (!result.isSuccessful()) {
                    log.error("Component backup failed: {} - {}", component, result.getErrorMessage());
                    // Continue with other components unless critical failure
                    if (result.isCriticalFailure()) {
                        return BackupResult.failed(plan.getId(), "Critical component backup failed: " + component);
                    }
                }
            }
            
            // Create backup manifest
            BackupManifest manifest = createBackupManifest(plan, componentResults, backupDir);
            
            // Apply compression if enabled
            String finalBackupPath = backupDir;
            if (plan.isCompressionEnabled() && compressionEnabled) {
                finalBackupPath = compressBackup(backupDir, plan);
            }
            
            // Apply encryption if enabled
            if (plan.isEncryptionEnabled() && encryptionEnabled) {
                finalBackupPath = encryptBackup(finalBackupPath, plan);
            }
            
            // Verify backup integrity if enabled
            BackupIntegrityResult integrityResult = null;
            if (plan.isVerificationEnabled() && verificationEnabled) {
                integrityResult = verifyBackupIntegrity(finalBackupPath, manifest);
            }
            
            // Upload to remote storage if enabled
            RemoteStorageResult remoteResult = null;
            if (remoteStorageEnabled) {
                remoteResult = uploadToRemoteStorage(finalBackupPath, plan);
            }
            
            // Update execution status
            execution.setEndTime(LocalDateTime.now());
            execution.setStatus(BackupStatus.COMPLETED);
            execution.setBackupPath(finalBackupPath);
            
            // Register backup metadata
            registerBackupMetadata(plan, execution, manifest, integrityResult, remoteResult);
            
            // Cleanup active backup tracking
            activeBackups.remove(execution.getId());
            
            return BackupResult.builder()
                .backupId(execution.getId())
                .planId(plan.getId())
                .successful(true)
                .backupPath(finalBackupPath)
                .componentResults(componentResults)
                .manifest(manifest)
                .integrityResult(integrityResult)
                .remoteStorageResult(remoteResult)
                .executionTime(execution.getExecutionDuration())
                .completedAt(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error executing backup plan: {}", plan.getId(), e);
            return BackupResult.failed(plan.getId(), "Backup execution failed: " + e.getMessage());
        }
    }

    /**
     * Verify backup integrity
     */
    public BackupIntegrityResult verifyBackupIntegrity(String backupId) {
        try {
            BackupMetadata metadata = backupRegistry.get(backupId);
            if (metadata == null) {
                return BackupIntegrityResult.failed("Backup not found: " + backupId);
            }
            
            return verifyBackupIntegrity(metadata.getBackupPath(), metadata.getManifest());
            
        } catch (Exception e) {
            log.error("Error verifying backup integrity: {}", backupId, e);
            return BackupIntegrityResult.failed("Integrity verification failed: " + e.getMessage());
        }
    }

    /**
     * Restore from backup
     */
    public RestoreResult restoreFromBackup(RestoreRequest request) {
        try {
            log.info("Starting restore from backup: {}", request.getBackupId());
            
            BackupMetadata metadata = backupRegistry.get(request.getBackupId());
            if (metadata == null) {
                return RestoreResult.failed("Backup not found: " + request.getBackupId());
            }
            
            // Verify backup integrity before restore
            BackupIntegrityResult integrity = verifyBackupIntegrity(metadata.getBackupPath(), metadata.getManifest());
            if (!integrity.isValid()) {
                return RestoreResult.failed("Backup integrity verification failed");
            }
            
            // Prepare restore environment
            RestoreEnvironment environment = prepareRestoreEnvironment(request);
            
            // Decrypt backup if encrypted
            String restorePath = metadata.getBackupPath();
            if (metadata.isEncrypted()) {
                restorePath = decryptBackup(restorePath, request);
            }
            
            // Decompress backup if compressed
            if (metadata.isCompressed()) {
                restorePath = decompressBackup(restorePath, request);
            }
            
            List<RestoreComponentResult> componentResults = new ArrayList<>();
            
            // Restore each component
            for (String component : request.getComponents()) {
                RestoreComponentResult result = restoreComponent(component, restorePath, request, environment);
                componentResults.add(result);
                
                if (!result.isSuccessful()) {
                    log.error("Component restore failed: {} - {}", component, result.getErrorMessage());
                    if (result.isCriticalFailure()) {
                        return RestoreResult.failed("Critical component restore failed: " + component);
                    }
                }
            }
            
            // Verify restore success
            RestoreVerificationResult verification = verifyRestoreSuccess(request, componentResults);
            
            return RestoreResult.builder()
                .restoreId(UUID.randomUUID().toString())
                .backupId(request.getBackupId())
                .successful(verification.isSuccessful())
                .componentResults(componentResults)
                .verification(verification)
                .restoredAt(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error restoring from backup: {}", request.getBackupId(), e);
            return RestoreResult.failed("Restore failed: " + e.getMessage());
        }
    }

    /**
     * Get backup status
     */
    public BackupStatus getBackupStatus() {
        try {
            // Get latest backup information
            BackupMetadata latestBackup = getLatestBackup();
            
            // Check active backups
            List<BackupExecution> activeBackupList = new ArrayList<>(activeBackups.values());
            
            // Calculate backup statistics
            BackupStatistics statistics = calculateBackupStatistics();
            
            return BackupStatus.builder()
                .latestBackup(latestBackup)
                .activeBackups(activeBackupList)
                .statistics(statistics)
                .lastBackupTime(latestBackup != null ? latestBackup.getCreatedAt() : null)
                .integrityVerified(latestBackup != null && latestBackup.isIntegrityVerified())
                .remoteStorageStatus(getRemoteStorageStatus())
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Error getting backup status", e);
            return BackupStatus.error("Status retrieval failed");
        }
    }

    /**
     * Cleanup old backups
     */
    public void cleanupBackupsOlderThan(LocalDateTime cutoffDate) {
        try {
            log.info("Cleaning up backups older than: {}", cutoffDate);
            
            List<String> backupsToDelete = new ArrayList<>();
            
            for (Map.Entry<String, BackupMetadata> entry : backupRegistry.entrySet()) {
                BackupMetadata metadata = entry.getValue();
                
                if (metadata.getCreatedAt().isBefore(cutoffDate)) {
                    backupsToDelete.add(entry.getKey());
                }
            }
            
            for (String backupId : backupsToDelete) {
                deleteBackup(backupId);
            }
            
            log.info("Cleaned up {} old backups", backupsToDelete.size());
            
        } catch (Exception e) {
            log.error("Error cleaning up old backups", e);
        }
    }

    // Private helper methods

    private String createBackupDirectory(BackupPlan plan) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupDir = Paths.get(backupBasePath, plan.getType().toString().toLowerCase(), timestamp).toString();
        
        Files.createDirectories(Paths.get(backupDir));
        
        log.debug("Created backup directory: {}", backupDir);
        return backupDir;
    }

    private BackupComponentResult backupComponent(String component, String backupDir, BackupPlan plan) {
        try {
            log.debug("Backing up component: {}", component);
            
            switch (component.toLowerCase()) {
                case "database" -> {
                    return databaseBackupService.backupDatabase(backupDir, plan);
                }
                case "redis" -> {
                    return databaseBackupService.backupRedis(backupDir, plan);
                }
                case "configurations" -> {
                    return configBackupService.backupConfigurations(backupDir, plan);
                }
                case "logs" -> {
                    return fileSystemBackupService.backupLogs(backupDir, plan);
                }
                case "documents" -> {
                    return fileSystemBackupService.backupDocuments(backupDir, plan);
                }
                default -> {
                    return BackupComponentResult.failed(component, "Unknown component type");
                }
            }
            
        } catch (Exception e) {
            log.error("Error backing up component: {}", component, e);
            return BackupComponentResult.failed(component, "Backup failed: " + e.getMessage());
        }
    }

    private BackupManifest createBackupManifest(BackupPlan plan, List<BackupComponentResult> results, String backupDir) {
        return BackupManifest.builder()
            .backupId(UUID.randomUUID().toString())
            .planId(plan.getId())
            .backupType(plan.getType())
            .components(plan.getComponents())
            .componentResults(results)
            .backupPath(backupDir)
            .createdAt(LocalDateTime.now())
            .compressed(plan.isCompressionEnabled() && compressionEnabled)
            .encrypted(plan.isEncryptionEnabled() && encryptionEnabled)
            .verified(plan.isVerificationEnabled() && verificationEnabled)
            .build();
    }

    private String compressBackup(String backupDir, BackupPlan plan) throws IOException {
        log.debug("Compressing backup: {}", backupDir);
        
        String compressedPath = backupDir + ".tar.gz";
        compressionService.compressDirectory(backupDir, compressedPath);
        
        // Remove original directory after compression
        fileSystemBackupService.deleteDirectory(backupDir);
        
        return compressedPath;
    }

    private String encryptBackup(String backupPath, BackupPlan plan) throws Exception {
        log.debug("Encrypting backup: {}", backupPath);
        
        String encryptedPath = backupPath + ".enc";
        encryptionService.encryptFile(backupPath, encryptedPath);
        
        // Remove original file after encryption
        Files.deleteIfExists(Paths.get(backupPath));
        
        return encryptedPath;
    }

    private BackupIntegrityResult verifyBackupIntegrity(String backupPath, BackupManifest manifest) {
        try {
            log.debug("Verifying backup integrity: {}", backupPath);
            
            // Verify file exists
            if (!Files.exists(Paths.get(backupPath))) {
                return BackupIntegrityResult.failed("Backup file not found: " + backupPath);
            }
            
            // Calculate and verify checksums
            String actualChecksum = calculateFileChecksum(backupPath);
            String expectedChecksum = manifest.getChecksum();
            
            if (expectedChecksum != null && !actualChecksum.equals(expectedChecksum)) {
                return BackupIntegrityResult.failed("Checksum mismatch");
            }
            
            // Verify backup can be read
            boolean readable = verifyBackupReadability(backupPath, manifest);
            if (!readable) {
                return BackupIntegrityResult.failed("Backup file is not readable");
            }
            
            return BackupIntegrityResult.success(actualChecksum);
            
        } catch (Exception e) {
            log.error("Error verifying backup integrity", e);
            return BackupIntegrityResult.failed("Integrity verification error: " + e.getMessage());
        }
    }

    private RemoteStorageResult uploadToRemoteStorage(String backupPath, BackupPlan plan) {
        try {
            log.debug("Uploading backup to remote storage: {}", backupPath);
            
            return storageService.uploadBackup(backupPath, plan);
            
        } catch (Exception e) {
            log.error("Error uploading to remote storage", e);
            return RemoteStorageResult.failed("Upload failed: " + e.getMessage());
        }
    }

    private void registerBackupMetadata(BackupPlan plan, BackupExecution execution, 
                                      BackupManifest manifest, BackupIntegrityResult integrity,
                                      RemoteStorageResult remoteResult) {
        BackupMetadata metadata = BackupMetadata.builder()
            .backupId(execution.getId())
            .planId(plan.getId())
            .backupPath(execution.getBackupPath())
            .manifest(manifest)
            .createdAt(execution.getStartTime())
            .completedAt(execution.getEndTime())
            .size(calculateBackupSize(execution.getBackupPath()))
            .compressed(manifest.isCompressed())
            .encrypted(manifest.isEncrypted())
            .integrityVerified(integrity != null && integrity.isValid())
            .remoteStorageLocation(remoteResult != null ? remoteResult.getRemoteLocation() : null)
            .build();
        
        backupRegistry.put(execution.getId(), metadata);
    }

    private BackupMetadata getLatestBackup() {
        return backupRegistry.values().stream()
            .max(Comparator.comparing(BackupMetadata::getCreatedAt))
            .orElse(null);
    }

    private BackupStatistics calculateBackupStatistics() {
        int totalBackups = backupRegistry.size();
        long totalSize = backupRegistry.values().stream()
            .mapToLong(BackupMetadata::getSize)
            .sum();
        
        long successfulBackups = backupRegistry.values().stream()
            .filter(BackupMetadata::isIntegrityVerified)
            .count();
        
        return BackupStatistics.builder()
            .totalBackups(totalBackups)
            .successfulBackups((int) successfulBackups)
            .totalSize(totalSize)
            .averageSize(totalBackups > 0 ? totalSize / totalBackups : 0)
            .successRate(totalBackups > 0 ? (double) successfulBackups / totalBackups * 100 : 0)
            .build();
    }

    private RemoteStorageStatus getRemoteStorageStatus() {
        if (!remoteStorageEnabled) {
            return RemoteStorageStatus.DISABLED;
        }
        
        try {
            return storageService.getStorageStatus();
        } catch (Exception e) {
            return RemoteStorageStatus.ERROR;
        }
    }

    private void deleteBackup(String backupId) {
        try {
            BackupMetadata metadata = backupRegistry.get(backupId);
            if (metadata != null) {
                // Delete local backup file
                Files.deleteIfExists(Paths.get(metadata.getBackupPath()));
                
                // Delete from remote storage if exists
                if (metadata.getRemoteStorageLocation() != null) {
                    storageService.deleteRemoteBackup(metadata.getRemoteStorageLocation());
                }
                
                // Remove from registry
                backupRegistry.remove(backupId);
                
                log.debug("Deleted backup: {}", backupId);
            }
        } catch (Exception e) {
            log.error("Error deleting backup: {}", backupId, e);
        }
    }

    private RestoreEnvironment prepareRestoreEnvironment(RestoreRequest request) {
        return RestoreEnvironment.builder()
            .targetEnvironment(request.getTargetEnvironment())
            .restoreMode(request.getRestoreMode())
            .build();
    }

    private String decryptBackup(String encryptedPath, RestoreRequest request) throws Exception {
        String decryptedPath = encryptedPath.replace(".enc", "");
        encryptionService.decryptFile(encryptedPath, decryptedPath);
        return decryptedPath;
    }

    private String decompressBackup(String compressedPath, RestoreRequest request) throws IOException {
        String decompressedPath = compressedPath.replace(".tar.gz", "");
        compressionService.decompressFile(compressedPath, decompressedPath);
        return decompressedPath;
    }

    private RestoreComponentResult restoreComponent(String component, String restorePath, 
                                                  RestoreRequest request, RestoreEnvironment environment) {
        try {
            switch (component.toLowerCase()) {
                case "database" -> {
                    return databaseBackupService.restoreDatabase(restorePath, request, environment);
                }
                case "redis" -> {
                    return databaseBackupService.restoreRedis(restorePath, request, environment);
                }
                case "configurations" -> {
                    return configBackupService.restoreConfigurations(restorePath, request, environment);
                }
                case "logs" -> {
                    return fileSystemBackupService.restoreLogs(restorePath, request, environment);
                }
                case "documents" -> {
                    return fileSystemBackupService.restoreDocuments(restorePath, request, environment);
                }
                default -> {
                    return RestoreComponentResult.failed(component, "Unknown component type");
                }
            }
        } catch (Exception e) {
            log.error("Error restoring component: {}", component, e);
            return RestoreComponentResult.failed(component, "Restore failed: " + e.getMessage());
        }
    }

    private RestoreVerificationResult verifyRestoreSuccess(RestoreRequest request, 
                                                         List<RestoreComponentResult> componentResults) {
        // Verify that all components were restored successfully
        boolean allSuccessful = componentResults.stream()
            .allMatch(RestoreComponentResult::isSuccessful);
        
        return RestoreVerificationResult.builder()
            .successful(allSuccessful)
            .componentResults(componentResults)
            .verifiedAt(LocalDateTime.now())
            .build();
    }

    private String calculateFileChecksum(String filePath) throws Exception {
        // Calculate SHA-256 checksum of the file
        return encryptionService.calculateChecksum(filePath);
    }

    private boolean verifyBackupReadability(String backupPath, BackupManifest manifest) {
        try {
            // Try to read the backup file
            Path path = Paths.get(backupPath);
            return Files.isReadable(path) && Files.size(path) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private long calculateBackupSize(String backupPath) {
        try {
            return Files.size(Paths.get(backupPath));
        } catch (Exception e) {
            return 0;
        }
    }
}
