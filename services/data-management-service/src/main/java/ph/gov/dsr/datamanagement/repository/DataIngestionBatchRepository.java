package ph.gov.dsr.datamanagement.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.datamanagement.entity.DataIngestionBatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DataIngestionBatch entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface DataIngestionBatchRepository extends JpaRepository<DataIngestionBatch, UUID> {

    /**
     * Find batch by batch ID
     */
    Optional<DataIngestionBatch> findByBatchId(String batchId);

    /**
     * Check if batch ID exists
     */
    boolean existsByBatchId(String batchId);

    /**
     * Find batches by source system
     */
    List<DataIngestionBatch> findBySourceSystemOrderByCreatedAtDesc(String sourceSystem);

    /**
     * Find batches by status
     */
    List<DataIngestionBatch> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find batches by data type
     */
    List<DataIngestionBatch> findByDataTypeOrderByCreatedAtDesc(String dataType);

    /**
     * Find batches by submitted user
     */
    List<DataIngestionBatch> findBySubmittedByOrderByCreatedAtDesc(String submittedBy);

    /**
     * Find batches created within date range
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
    List<DataIngestionBatch> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find batches by processing priority
     */
    List<DataIngestionBatch> findByProcessingPriorityOrderByCreatedAtDesc(String processingPriority);

    /**
     * Find in-progress batches
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.status = 'PROCESSING' ORDER BY b.startedAt ASC")
    List<DataIngestionBatch> findInProgressBatches();

    /**
     * Find failed batches that can be retried
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.status = 'FAILED' AND b.completedAt > :since ORDER BY b.completedAt DESC")
    List<DataIngestionBatch> findFailedBatchesSince(@Param("since") LocalDateTime since);

    /**
     * Find batches with high error rates
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.totalRecords > 0 AND " +
           "(CAST(b.failedRecords AS double) / CAST(b.totalRecords AS double)) > :errorThreshold " +
           "ORDER BY b.completedAt DESC")
    List<DataIngestionBatch> findBatchesWithHighErrorRate(@Param("errorThreshold") double errorThreshold);

    /**
     * Get batch statistics by source system
     */
    @Query("SELECT b.sourceSystem, COUNT(b), SUM(b.totalRecords), SUM(b.successfulRecords), SUM(b.failedRecords) " +
           "FROM DataIngestionBatch b WHERE b.completedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY b.sourceSystem")
    List<Object[]> getBatchStatisticsBySourceSystem(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Get batch statistics by data type
     */
    @Query("SELECT b.dataType, COUNT(b), SUM(b.totalRecords), SUM(b.successfulRecords), SUM(b.failedRecords) " +
           "FROM DataIngestionBatch b WHERE b.completedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY b.dataType")
    List<Object[]> getBatchStatisticsByDataType(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find batches with long processing times
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.processingTimeMs > :thresholdMs ORDER BY b.processingTimeMs DESC")
    List<DataIngestionBatch> findBatchesWithLongProcessingTime(@Param("thresholdMs") long thresholdMs);

    /**
     * Find recent batches with pagination
     */
    @Query("SELECT b FROM DataIngestionBatch b ORDER BY b.createdAt DESC")
    Page<DataIngestionBatch> findRecentBatches(Pageable pageable);

    /**
     * Count batches by status
     */
    @Query("SELECT b.status, COUNT(b) FROM DataIngestionBatch b GROUP BY b.status")
    List<Object[]> countBatchesByStatus();

    /**
     * Find batches that need cleanup (older than specified days)
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.completedAt < :cutoffDate AND b.status IN ('SUCCESS', 'FAILED', 'PARTIAL')")
    List<DataIngestionBatch> findBatchesForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get average processing time by source system
     */
    @Query("SELECT b.sourceSystem, AVG(b.processingTimeMs) FROM DataIngestionBatch b " +
           "WHERE b.processingTimeMs IS NOT NULL AND b.completedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY b.sourceSystem")
    List<Object[]> getAverageProcessingTimeBySourceSystem(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find duplicate batches (same source system, data type, and similar timing)
     */
    @Query("SELECT b FROM DataIngestionBatch b WHERE b.sourceSystem = :sourceSystem AND b.dataType = :dataType " +
           "AND b.createdAt BETWEEN :startTime AND :endTime AND b.id != :excludeId")
    List<DataIngestionBatch> findPotentialDuplicateBatches(@Param("sourceSystem") String sourceSystem,
                                                          @Param("dataType") String dataType,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("excludeId") UUID excludeId);
}
