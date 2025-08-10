package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.DataIngestionBatch;
import ph.gov.dsr.datamanagement.entity.DataIngestionRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DataIngestionRecord entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface DataIngestionRecordRepository extends JpaRepository<DataIngestionRecord, UUID> {

    /**
     * Find records by batch
     */
    List<DataIngestionRecord> findByBatchOrderByRecordIndexAsc(DataIngestionBatch batch);

    /**
     * Find records by batch ID
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.batch.batchId = :batchId ORDER BY r.recordIndex ASC")
    List<DataIngestionRecord> findByBatchId(@Param("batchId") String batchId);

    /**
     * Find records by status
     */
    List<DataIngestionRecord> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find records by entity type
     */
    List<DataIngestionRecord> findByEntityTypeOrderByCreatedAtDesc(String entityType);

    /**
     * Find records by source record ID
     */
    Optional<DataIngestionRecord> findBySourceRecordId(String sourceRecordId);

    /**
     * Find records that reference a specific entity
     */
    List<DataIngestionRecord> findByEntityId(UUID entityId);

    /**
     * Find duplicate records
     */
    List<DataIngestionRecord> findByStatusAndDuplicateOfIsNotNull(String status);

    /**
     * Find failed records that can be retried
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.status = 'FAILED' AND " +
           "(r.retryCount IS NULL OR r.retryCount < :maxRetries)")
    List<DataIngestionRecord> findFailedRecordsForRetry(@Param("maxRetries") int maxRetries);

    /**
     * Find records with high similarity scores
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.similarityScore > :threshold ORDER BY r.similarityScore DESC")
    List<DataIngestionRecord> findRecordsWithHighSimilarity(@Param("threshold") double threshold);

    /**
     * Count records by status for a batch
     */
    @Query("SELECT r.status, COUNT(r) FROM DataIngestionRecord r WHERE r.batch = :batch GROUP BY r.status")
    List<Object[]> countRecordsByStatusForBatch(@Param("batch") DataIngestionBatch batch);

    /**
     * Find records with long processing times
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.processingTimeMs > :thresholdMs ORDER BY r.processingTimeMs DESC")
    List<DataIngestionRecord> findRecordsWithLongProcessingTime(@Param("thresholdMs") long thresholdMs);

    /**
     * Find records with validation errors
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.validationErrors IS NOT NULL AND r.validationErrors != '[]'")
    List<DataIngestionRecord> findRecordsWithValidationErrors();

    /**
     * Find records with warnings
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.warnings IS NOT NULL AND r.warnings != '[]'")
    List<DataIngestionRecord> findRecordsWithWarnings();

    /**
     * Get processing statistics by entity type
     */
    @Query("SELECT r.entityType, COUNT(r), " +
           "SUM(CASE WHEN r.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.status = 'FAILED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.status = 'DUPLICATE' THEN 1 ELSE 0 END) " +
           "FROM DataIngestionRecord r WHERE r.processedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY r.entityType")
    List<Object[]> getProcessingStatisticsByEntityType(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find records processed within date range
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.processedAt BETWEEN :startDate AND :endDate ORDER BY r.processedAt DESC")
    List<DataIngestionRecord> findRecordsProcessedBetween(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find records by batch with pagination
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.batch = :batch ORDER BY r.recordIndex ASC")
    Page<DataIngestionRecord> findByBatchWithPagination(@Param("batch") DataIngestionBatch batch, Pageable pageable);

    /**
     * Get average processing time by entity type
     */
    @Query("SELECT r.entityType, AVG(r.processingTimeMs) FROM DataIngestionRecord r " +
           "WHERE r.processingTimeMs IS NOT NULL AND r.processedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY r.entityType")
    List<Object[]> getAverageProcessingTimeByEntityType(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find records that need cleanup (older than specified days)
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.processedAt < :cutoffDate")
    List<DataIngestionRecord> findRecordsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count total records processed
     */
    @Query("SELECT COUNT(r) FROM DataIngestionRecord r WHERE r.processedAt BETWEEN :startDate AND :endDate")
    long countRecordsProcessedBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find most recent records by entity type
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.entityType = :entityType AND r.status = 'SUCCESS' " +
           "ORDER BY r.processedAt DESC")
    List<DataIngestionRecord> findRecentSuccessfulRecordsByEntityType(@Param("entityType") String entityType,
                                                                     Pageable pageable);

    /**
     * Find records with specific error patterns
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.errorMessage LIKE %:errorPattern%")
    List<DataIngestionRecord> findRecordsWithErrorPattern(@Param("errorPattern") String errorPattern);

    /**
     * Get duplicate statistics
     */
    @Query("SELECT COUNT(r), AVG(r.similarityScore), MIN(r.similarityScore), MAX(r.similarityScore) " +
           "FROM DataIngestionRecord r WHERE r.status = 'DUPLICATE' AND r.processedAt BETWEEN :startDate AND :endDate")
    Object[] getDuplicateStatistics(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find records that created specific entities
     */
    @Query("SELECT r FROM DataIngestionRecord r WHERE r.entityId IN :entityIds AND r.status = 'SUCCESS'")
    List<DataIngestionRecord> findRecordsByEntityIds(@Param("entityIds") List<UUID> entityIds);
}
