package ph.gov.dsr.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.analytics.entity.Report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Report entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    /**
     * Find report by code
     */
    Optional<Report> findByReportCode(String reportCode);

    /**
     * Find reports by type
     */
    List<Report> findByReportTypeOrderByCreatedAtDesc(Report.ReportType reportType);

    /**
     * Find reports by category
     */
    List<Report> findByCategoryOrderByCreatedAtDesc(Report.ReportCategory category);

    /**
     * Find reports by status
     */
    List<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status);

    /**
     * Find active reports
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findActiveReports();

    /**
     * Find reports by target role
     */
    List<Report> findByTargetRoleOrderByCreatedAtDesc(String targetRole);

    /**
     * Find reports by frequency
     */
    List<Report> findByFrequencyOrderByNextRunTime(Report.ReportFrequency frequency);

    /**
     * Find scheduled reports
     */
    @Query("SELECT r FROM Report r WHERE r.frequency != 'ON_DEMAND' AND r.status = 'ACTIVE' ORDER BY r.nextRunTime")
    List<Report> findScheduledReports();

    /**
     * Find reports due for generation
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' AND r.frequency != 'ON_DEMAND' AND " +
           "r.nextRunTime IS NOT NULL AND r.nextRunTime <= CURRENT_TIMESTAMP")
    List<Report> findReportsDueForGeneration();

    /**
     * Find public reports
     */
    @Query("SELECT r FROM Report r WHERE r.isPublic = true AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findPublicReports();

    /**
     * Find report templates
     */
    @Query("SELECT r FROM Report r WHERE r.isTemplate = true ORDER BY r.reportName")
    List<Report> findReportTemplates();

    /**
     * Find reports by template
     */
    List<Report> findByTemplateIdOrderByCreatedAtDesc(UUID templateId);

    /**
     * Find reports by access level
     */
    List<Report> findByAccessLevelAndStatusOrderByCreatedAtDesc(String accessLevel, Report.ReportStatus status);

    /**
     * Find reports accessible by role
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' AND " +
           "(r.targetRole = :role OR r.targetRole IS NULL OR r.allowedRoles LIKE %:role%) " +
           "ORDER BY r.reportName")
    List<Report> findReportsAccessibleByRole(@Param("role") String role);

    /**
     * Find reports by output format
     */
    List<Report> findByOutputFormatAndStatusOrderByCreatedAtDesc(String outputFormat, Report.ReportStatus status);

    /**
     * Find recently generated reports
     */
    @Query("SELECT r FROM Report r WHERE r.lastRunTime >= :since ORDER BY r.lastRunTime DESC")
    List<Report> findRecentlyGeneratedReports(@Param("since") LocalDateTime since);

    /**
     * Find most downloaded reports
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' ORDER BY r.downloadCount DESC")
    List<Report> findMostDownloadedReports();

    /**
     * Find reports by name pattern
     */
    @Query("SELECT r FROM Report r WHERE LOWER(r.reportName) LIKE LOWER(CONCAT('%', :namePattern, '%')) " +
           "AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findByNamePattern(@Param("namePattern") String namePattern);

    /**
     * Find reports created by user
     */
    List<Report> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Find reports updated by user
     */
    List<Report> findByUpdatedByOrderByUpdatedAtDesc(String updatedBy);

    /**
     * Find reports created within date range
     */
    @Query("SELECT r FROM Report r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Report> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find reports with email enabled
     */
    @Query("SELECT r FROM Report r WHERE r.emailEnabled = true AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findEmailEnabledReports();

    /**
     * Find expired reports
     */
    @Query("SELECT r FROM Report r WHERE r.retentionDays IS NOT NULL AND " +
           "r.lastRunTime < :expiryThreshold AND r.autoDeleteEnabled = true")
    List<Report> findExpiredReports(@Param("expiryThreshold") LocalDateTime expiryThreshold);

    /**
     * Find failed reports
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'FAILED' ORDER BY r.lastRunTime DESC")
    List<Report> findFailedReports();

    /**
     * Find reports with errors
     */
    @Query("SELECT r FROM Report r WHERE r.errorMessage IS NOT NULL ORDER BY r.lastRunTime DESC")
    List<Report> findReportsWithErrors();

    /**
     * Find reports with warnings
     */
    @Query("SELECT r FROM Report r WHERE r.warningCount > 0 ORDER BY r.warningCount DESC")
    List<Report> findReportsWithWarnings();

    /**
     * Find child reports
     */
    List<Report> findByParentReportIdOrderByCreatedAtDesc(UUID parentReportId);

    /**
     * Find root reports (no parent)
     */
    @Query("SELECT r FROM Report r WHERE r.parentReportId IS NULL AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findRootReports();

    /**
     * Count reports by type
     */
    @Query("SELECT r.reportType, COUNT(r) FROM Report r GROUP BY r.reportType")
    List<Object[]> countReportsByType();

    /**
     * Count reports by category
     */
    @Query("SELECT r.category, COUNT(r) FROM Report r GROUP BY r.category")
    List<Object[]> countReportsByCategory();

    /**
     * Count reports by status
     */
    @Query("SELECT r.status, COUNT(r) FROM Report r GROUP BY r.status")
    List<Object[]> countReportsByStatus();

    /**
     * Count reports by frequency
     */
    @Query("SELECT r.frequency, COUNT(r) FROM Report r GROUP BY r.frequency")
    List<Object[]> countReportsByFrequency();

    /**
     * Get report usage statistics
     */
    @Query("SELECT COUNT(r), SUM(r.downloadCount), AVG(r.downloadCount), MAX(r.downloadCount) " +
           "FROM Report r WHERE r.status = 'ACTIVE'")
    Object[] getReportUsageStatistics();

    /**
     * Get report generation statistics
     */
    @Query("SELECT COUNT(r), AVG(r.generationTimeMs), MAX(r.generationTimeMs), " +
           "AVG(r.fileSizeBytes), MAX(r.fileSizeBytes) " +
           "FROM Report r WHERE r.status = 'COMPLETED' AND r.generationTimeMs IS NOT NULL")
    Object[] getReportGenerationStatistics();

    /**
     * Find unused reports (never downloaded or low download count)
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' AND " +
           "(r.downloadCount IS NULL OR r.downloadCount < :minDownloads) " +
           "ORDER BY r.createdAt DESC")
    List<Report> findUnusedReports(@Param("minDownloads") Integer minDownloads);

    /**
     * Find stale reports (not downloaded recently)
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' AND " +
           "(r.lastDownloaded IS NULL OR r.lastDownloaded < :staleThreshold) " +
           "ORDER BY r.lastDownloaded ASC")
    List<Report> findStaleReports(@Param("staleThreshold") LocalDateTime staleThreshold);

    /**
     * Find reports by multiple criteria
     */
    @Query("SELECT r FROM Report r WHERE " +
           "(:reportType IS NULL OR r.reportType = :reportType) AND " +
           "(:category IS NULL OR r.category = :category) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:targetRole IS NULL OR r.targetRole = :targetRole) AND " +
           "(:frequency IS NULL OR r.frequency = :frequency) AND " +
           "(:outputFormat IS NULL OR r.outputFormat = :outputFormat) " +
           "ORDER BY r.createdAt DESC")
    List<Report> findByCriteria(@Param("reportType") Report.ReportType reportType,
                               @Param("category") Report.ReportCategory category,
                               @Param("status") Report.ReportStatus status,
                               @Param("targetRole") String targetRole,
                               @Param("frequency") Report.ReportFrequency frequency,
                               @Param("outputFormat") String outputFormat);

    /**
     * Search reports by text
     */
    @Query("SELECT r FROM Report r WHERE " +
           "(LOWER(r.reportName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(r.reportCode) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "r.status = 'ACTIVE' " +
           "ORDER BY r.reportName")
    List<Report> searchReports(@Param("searchText") String searchText);

    /**
     * Find large reports (by file size)
     */
    @Query("SELECT r FROM Report r WHERE r.fileSizeBytes > :sizeThreshold ORDER BY r.fileSizeBytes DESC")
    List<Report> findLargeReports(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Find slow reports (by generation time)
     */
    @Query("SELECT r FROM Report r WHERE r.generationTimeMs > :timeThreshold ORDER BY r.generationTimeMs DESC")
    List<Report> findSlowReports(@Param("timeThreshold") Long timeThreshold);

    /**
     * Find reports with encryption enabled
     */
    @Query("SELECT r FROM Report r WHERE r.encryptionEnabled = true AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findEncryptedReports();

    /**
     * Find reports with compression enabled
     */
    @Query("SELECT r FROM Report r WHERE r.compressionEnabled = true AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findCompressedReports();

    /**
     * Find reports with watermark enabled
     */
    @Query("SELECT r FROM Report r WHERE r.watermarkEnabled = true AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findWatermarkedReports();

    /**
     * Get report performance metrics
     */
    @Query("SELECT r.reportCode, r.downloadCount, r.lastDownloaded, r.generationTimeMs, r.fileSizeBytes " +
           "FROM Report r WHERE r.status = 'COMPLETED' ORDER BY r.downloadCount DESC")
    List<Object[]> getReportPerformanceMetrics();

    /**
     * Find reports by tag
     */
    @Query("SELECT r FROM Report r WHERE r.tags LIKE %:tag% AND r.status = 'ACTIVE' ORDER BY r.reportName")
    List<Report> findByTag(@Param("tag") String tag);

    /**
     * Update download count
     */
    @Query("UPDATE Report r SET r.downloadCount = r.downloadCount + 1, r.lastDownloaded = CURRENT_TIMESTAMP WHERE r.id = :id")
    void incrementDownloadCount(@Param("id") UUID id);

    /**
     * Update next run time
     */
    @Query("UPDATE Report r SET r.nextRunTime = :nextRunTime WHERE r.id = :id")
    void updateNextRunTime(@Param("id") UUID id, @Param("nextRunTime") LocalDateTime nextRunTime);

    /**
     * Bulk update status
     */
    @Query("UPDATE Report r SET r.status = :newStatus WHERE r.id IN :ids")
    void bulkUpdateStatus(@Param("ids") List<UUID> ids, @Param("newStatus") Report.ReportStatus newStatus);

    /**
     * Delete old reports
     */
    @Query("DELETE FROM Report r WHERE r.status = 'ARCHIVED' AND r.updatedAt < :cutoffDate")
    void deleteOldArchivedReports(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete expired report files
     */
    @Query("UPDATE Report r SET r.filePath = NULL, r.fileSizeBytes = NULL WHERE " +
           "r.retentionDays IS NOT NULL AND r.lastRunTime < :expiryThreshold AND r.autoDeleteEnabled = true")
    void deleteExpiredReportFiles(@Param("expiryThreshold") LocalDateTime expiryThreshold);

    /**
     * Find duplicate report codes
     */
    @Query("SELECT r.reportCode FROM Report r GROUP BY r.reportCode HAVING COUNT(r) > 1")
    List<String> findDuplicateReportCodes();

    /**
     * Get report hierarchy
     */
    @Query("SELECT r FROM Report r WHERE r.parentReportId = :parentId OR " +
           "(r.parentReportId IS NULL AND :parentId IS NULL) " +
           "ORDER BY r.reportName")
    List<Report> findReportHierarchy(@Param("parentId") UUID parentId);

    /**
     * Find reports requiring cleanup
     */
    @Query("SELECT r FROM Report r WHERE " +
           "(r.status = 'FAILED' AND r.lastRunTime < :cleanupThreshold) OR " +
           "(r.filePath IS NOT NULL AND r.retentionDays IS NOT NULL AND " +
           "r.lastRunTime < :expiryThreshold AND r.autoDeleteEnabled = true)")
    List<Report> findReportsRequiringCleanup(@Param("cleanupThreshold") LocalDateTime cleanupThreshold,
                                            @Param("expiryThreshold") LocalDateTime expiryThreshold);

    /**
     * Get storage usage by reports
     */
    @Query("SELECT SUM(r.fileSizeBytes), COUNT(r), AVG(r.fileSizeBytes) FROM Report r WHERE r.fileSizeBytes IS NOT NULL")
    Object[] getStorageUsageStatistics();

    /**
     * Find reports by data source
     */
    List<Report> findByDataSourceAndStatusOrderByCreatedAtDesc(String dataSource, Report.ReportStatus status);

    /**
     * Find reports needing regeneration
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'ACTIVE' AND r.frequency != 'ON_DEMAND' AND " +
           "(r.lastRunTime IS NULL OR " +
           "(r.frequency = 'DAILY' AND r.lastRunTime < :dailyThreshold) OR " +
           "(r.frequency = 'WEEKLY' AND r.lastRunTime < :weeklyThreshold) OR " +
           "(r.frequency = 'MONTHLY' AND r.lastRunTime < :monthlyThreshold))")
    List<Report> findReportsNeedingRegeneration(@Param("dailyThreshold") LocalDateTime dailyThreshold,
                                               @Param("weeklyThreshold") LocalDateTime weeklyThreshold,
                                               @Param("monthlyThreshold") LocalDateTime monthlyThreshold);
}
