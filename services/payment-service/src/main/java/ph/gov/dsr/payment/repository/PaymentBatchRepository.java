package ph.gov.dsr.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.payment.entity.PaymentBatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PaymentBatch entity
 */
@Repository
public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, UUID> {

    /**
     * Find batch by batch number
     */
    Optional<PaymentBatch> findByBatchNumber(String batchNumber);

    /**
     * Find batches by program ID
     */
    Page<PaymentBatch> findByProgramId(UUID programId, Pageable pageable);

    /**
     * Find batches by status
     */
    Page<PaymentBatch> findByStatus(PaymentBatch.BatchStatus status, Pageable pageable);

    /**
     * Find batches scheduled for processing
     */
    @Query("SELECT b FROM PaymentBatch b WHERE b.status = 'PENDING' AND b.scheduledDate <= :currentTime")
    List<PaymentBatch> findScheduledBatches(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find batches by date range
     */
    @Query("SELECT b FROM PaymentBatch b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    Page<PaymentBatch> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    /**
     * Find processing batches that may be stuck
     */
    @Query("SELECT b FROM PaymentBatch b WHERE b.status = 'PROCESSING' AND b.startedDate <= :cutoffTime")
    List<PaymentBatch> findStuckProcessingBatches(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get batch statistics by status
     */
    @Query("SELECT b.status, COUNT(b), SUM(b.totalAmount) FROM PaymentBatch b GROUP BY b.status")
    List<Object[]> getBatchStatistics();

    /**
     * Get batch statistics by program
     */
    @Query("SELECT b.programId, b.programName, COUNT(b), SUM(b.totalAmount) FROM PaymentBatch b GROUP BY b.programId, b.programName")
    List<Object[]> getBatchStatisticsByProgram();

    /**
     * Count batches by status
     */
    long countByStatus(PaymentBatch.BatchStatus status);

    /**
     * Find batches by multiple criteria
     */
    @Query("SELECT b FROM PaymentBatch b WHERE " +
           "(:programId IS NULL OR b.programId = :programId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:startDate IS NULL OR b.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR b.createdAt <= :endDate)")
    Page<PaymentBatch> findByCriteria(@Param("programId") UUID programId,
                                     @Param("status") PaymentBatch.BatchStatus status,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    /**
     * Get recent batches for dashboard
     */
    @Query("SELECT b FROM PaymentBatch b ORDER BY b.createdAt DESC")
    Page<PaymentBatch> findRecentBatches(Pageable pageable);

    /**
     * Check if batch exists for program and date
     */
    boolean existsByProgramIdAndScheduledDateBetween(UUID programId, LocalDateTime startDate, LocalDateTime endDate);
}
