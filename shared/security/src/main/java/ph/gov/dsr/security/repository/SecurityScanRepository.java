package ph.gov.dsr.security.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.security.entity.SecurityScan;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for SecurityScan entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Repository
public interface SecurityScanRepository extends JpaRepository<SecurityScan, UUID> {

    /**
     * Find security scan by scan ID
     */
    Optional<SecurityScan> findByScanId(String scanId);

    /**
     * Find scans by scan type
     */
    List<SecurityScan> findByScanTypeOrderByCreatedAtDesc(String scanType);

    /**
     * Find scans by scan tool
     */
    List<SecurityScan> findByScanToolOrderByCreatedAtDesc(String scanTool);

    /**
     * Find scans by target type
     */
    List<SecurityScan> findByTargetTypeOrderByCreatedAtDesc(String targetType);

    /**
     * Find scans by target identifier
     */
    List<SecurityScan> findByTargetIdentifierOrderByCreatedAtDesc(String targetIdentifier);

    /**
     * Find scans by status
     */
    List<SecurityScan> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find running scans
     */
    List<SecurityScan> findByStatusOrderByStartedAtDesc(String status);

    /**
     * Find completed scans
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.status = 'COMPLETED' ORDER BY s.completedAt DESC")
    List<SecurityScan> findCompletedScans();

    /**
     * Find failed scans
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.status = 'FAILED' ORDER BY s.createdAt DESC")
    List<SecurityScan> findFailedScans();

    /**
     * Find scans initiated by user
     */
    List<SecurityScan> findByInitiatedByOrderByCreatedAtDesc(UUID initiatedBy);

    /**
     * Find scheduled scans
     */
    List<SecurityScan> findByScheduledTrueOrderByNextRunDateAsc();

    /**
     * Find recurring scans
     */
    List<SecurityScan> findByRecurringTrueOrderByNextRunDateAsc();

    /**
     * Find scans due for execution
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.scheduled = true AND s.nextRunDate <= :currentTime AND s.status != 'RUNNING' ORDER BY s.nextRunDate ASC")
    List<SecurityScan> findScansDueForExecution(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find scans by date range
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<SecurityScan> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find scans by completion date range
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.completedAt BETWEEN :startDate AND :endDate ORDER BY s.completedAt DESC")
    List<SecurityScan> findByCompletionDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Find baseline scans
     */
    List<SecurityScan> findByBaselineScanTrueOrderByCreatedAtDesc();

    /**
     * Find scans with baseline comparison
     */
    List<SecurityScan> findByBaselineScanIdIsNotNullOrderByCreatedAtDesc();

    /**
     * Find scans by priority
     */
    List<SecurityScan> findByPriorityOrderByCreatedAtDesc(String priority);

    /**
     * Find high-priority scans
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.priority IN ('HIGH', 'CRITICAL') ORDER BY s.createdAt DESC")
    List<SecurityScan> findHighPriorityScans();

    /**
     * Find scans with high findings
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.criticalFindings > 0 OR s.highFindings > 0 ORDER BY s.completedAt DESC")
    List<SecurityScan> findScansWithHighFindings();

    /**
     * Find scans with critical findings
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.criticalFindings > 0 ORDER BY s.completedAt DESC")
    List<SecurityScan> findScansWithCriticalFindings();

    /**
     * Find recent scans (last 24 hours)
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<SecurityScan> findRecentScans(@Param("since") LocalDateTime since);

    /**
     * Find long-running scans
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.status = 'RUNNING' AND s.startedAt < :cutoffTime ORDER BY s.startedAt ASC")
    List<SecurityScan> findLongRunningScans(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find archived scans
     */
    List<SecurityScan> findByArchivedTrueOrderByArchivedAtDesc();

    /**
     * Find non-archived scans
     */
    List<SecurityScan> findByArchivedFalseOrderByCreatedAtDesc();

    /**
     * Find scans eligible for archiving
     */
    @Query("SELECT s FROM SecurityScan s WHERE s.archived = false AND s.completedAt < :cutoffDate")
    List<SecurityScan> findEligibleForArchiving(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count scans by status in date range
     */
    @Query("SELECT COUNT(s) FROM SecurityScan s WHERE s.status = :status AND s.createdAt BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") String status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Count scans by scan type in date range
     */
    @Query("SELECT COUNT(s) FROM SecurityScan s WHERE s.scanType = :scanType AND s.createdAt BETWEEN :startDate AND :endDate")
    Long countByScanTypeAndDateRange(@Param("scanType") String scanType,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find scans by multiple criteria
     */
    @Query("SELECT s FROM SecurityScan s WHERE " +
           "(:scanType IS NULL OR s.scanType = :scanType) AND " +
           "(:scanTool IS NULL OR s.scanTool = :scanTool) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:priority IS NULL OR s.priority = :priority) AND " +
           "(:initiatedBy IS NULL OR s.initiatedBy = :initiatedBy) AND " +
           "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR s.createdAt <= :endDate) " +
           "ORDER BY s.createdAt DESC")
    Page<SecurityScan> findByCriteria(@Param("scanType") String scanType,
                                      @Param("scanTool") String scanTool,
                                      @Param("status") String status,
                                      @Param("priority") String priority,
                                      @Param("initiatedBy") UUID initiatedBy,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    /**
     * Get scan statistics by type
     */
    @Query("SELECT s.scanType, COUNT(s) FROM SecurityScan s WHERE s.createdAt BETWEEN :startDate AND :endDate GROUP BY s.scanType")
    List<Object[]> getScanTypeStatistics(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Get scan statistics by status
     */
    @Query("SELECT s.status, COUNT(s) FROM SecurityScan s WHERE s.createdAt BETWEEN :startDate AND :endDate GROUP BY s.status")
    List<Object[]> getStatusStatistics(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Get average scan duration by type
     */
    @Query("SELECT s.scanType, AVG(s.durationMinutes) FROM SecurityScan s WHERE s.durationMinutes IS NOT NULL GROUP BY s.scanType")
    List<Object[]> getAverageDurationByScanType();

    /**
     * Get scan trends
     */
    @Query("SELECT DATE(s.createdAt), COUNT(s) FROM SecurityScan s WHERE s.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(s.createdAt) ORDER BY DATE(s.createdAt)")
    List<Object[]> getScanTrends(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Get findings summary
     */
    @Query("SELECT SUM(s.criticalFindings), SUM(s.highFindings), SUM(s.mediumFindings), SUM(s.lowFindings) FROM SecurityScan s WHERE s.completedAt BETWEEN :startDate AND :endDate")
    Object[] getFindingsSummary(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
}
