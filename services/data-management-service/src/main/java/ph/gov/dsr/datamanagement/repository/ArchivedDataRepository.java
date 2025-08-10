package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.ArchivedData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ArchivedData entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Repository
public interface ArchivedDataRepository extends JpaRepository<ArchivedData, UUID> {

    /**
     * Find archived data by original entity ID and type
     */
    Optional<ArchivedData> findByOriginalEntityIdAndEntityType(UUID originalEntityId, String entityType);

    /**
     * Check if entity is archived
     */
    boolean existsByOriginalEntityIdAndEntityTypeAndArchiveStatus(
            UUID originalEntityId, String entityType, ArchivedData.ArchiveStatus status);

    /**
     * Find archived data by entity type
     */
    List<ArchivedData> findByEntityTypeAndArchiveStatusOrderByArchivedAtDesc(
            String entityType, ArchivedData.ArchiveStatus status);

    /**
     * Find archived data by date range
     */
    List<ArchivedData> findByArchivedAtBetweenAndArchiveStatusOrderByArchivedAtDesc(
            LocalDateTime fromDate, LocalDateTime toDate, ArchivedData.ArchiveStatus status);

    /**
     * Find archived data by entity type and date range
     */
    List<ArchivedData> findByEntityTypeAndArchivedAtBetweenAndArchiveStatusOrderByArchivedAtDesc(
            String entityType, LocalDateTime fromDate, LocalDateTime toDate, ArchivedData.ArchiveStatus status);

    /**
     * Find expired archived data
     */
    @Query("SELECT a FROM ArchivedData a WHERE a.retentionUntil < :currentDate AND a.archiveStatus = 'ACTIVE'")
    List<ArchivedData> findExpiredArchivedData(@Param("currentDate") LocalDateTime currentDate);

    /**
     * Find archived data by archived by user
     */
    List<ArchivedData> findByArchivedByAndArchiveStatusOrderByArchivedAtDesc(
            String archivedBy, ArchivedData.ArchiveStatus status);

    /**
     * Count archived data by entity type
     */
    @Query("SELECT a.entityType, COUNT(a) FROM ArchivedData a WHERE a.archiveStatus = 'ACTIVE' GROUP BY a.entityType")
    List<Object[]> countArchivedDataByEntityType();

    /**
     * Count archived data by status
     */
    @Query("SELECT a.archiveStatus, COUNT(a) FROM ArchivedData a GROUP BY a.archiveStatus")
    List<Object[]> countArchivedDataByStatus();

    /**
     * Get archiving statistics
     */
    @Query("SELECT " +
           "COUNT(a) as totalArchived, " +
           "COUNT(CASE WHEN a.archiveStatus = 'ACTIVE' THEN 1 END) as activeArchives, " +
           "COUNT(CASE WHEN a.archiveStatus = 'RESTORED' THEN 1 END) as restoredArchives, " +
           "COUNT(CASE WHEN a.archiveStatus = 'EXPIRED' THEN 1 END) as expiredArchives, " +
           "SUM(a.fileSizeBytes) as totalSizeBytes, " +
           "MAX(a.archivedAt) as lastArchiveDate " +
           "FROM ArchivedData a")
    Object[] getArchivingStatistics();

    /**
     * Find archived data that can be cleaned up
     */
    @Query("SELECT a FROM ArchivedData a WHERE " +
           "(a.archiveStatus = 'EXPIRED' OR a.archiveStatus = 'RESTORED') " +
           "AND a.updatedAt < :cutoffDate")
    List<ArchivedData> findArchivedDataForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find large archived data files
     */
    @Query("SELECT a FROM ArchivedData a WHERE a.fileSizeBytes > :sizeThreshold " +
           "AND a.archiveStatus = 'ACTIVE' ORDER BY a.fileSizeBytes DESC")
    List<ArchivedData> findLargeArchivedData(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Find archived data with pagination
     */
    Page<ArchivedData> findByArchiveStatusOrderByArchivedAtDesc(
            ArchivedData.ArchiveStatus status, Pageable pageable);

    /**
     * Find archived data by entity type with pagination
     */
    Page<ArchivedData> findByEntityTypeAndArchiveStatusOrderByArchivedAtDesc(
            String entityType, ArchivedData.ArchiveStatus status, Pageable pageable);

    /**
     * Get total archived data size by entity type
     */
    @Query("SELECT a.entityType, SUM(a.fileSizeBytes) FROM ArchivedData a " +
           "WHERE a.archiveStatus = 'ACTIVE' GROUP BY a.entityType")
    List<Object[]> getTotalSizeByEntityType();

    /**
     * Find archived data by archive reason
     */
    List<ArchivedData> findByArchiveReasonContainingIgnoreCaseAndArchiveStatus(
            String reason, ArchivedData.ArchiveStatus status);

    /**
     * Count archived data in date range
     */
    @Query("SELECT COUNT(a) FROM ArchivedData a WHERE a.archivedAt BETWEEN :startDate AND :endDate")
    Long countArchivedDataInDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find recently archived data
     */
    @Query("SELECT a FROM ArchivedData a WHERE a.archivedAt >= :since " +
           "AND a.archiveStatus = 'ACTIVE' ORDER BY a.archivedAt DESC")
    List<ArchivedData> findRecentlyArchivedData(@Param("since") LocalDateTime since);
}
