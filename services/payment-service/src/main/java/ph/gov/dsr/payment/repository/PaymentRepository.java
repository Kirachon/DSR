package ph.gov.dsr.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.payment.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payment by internal reference number
     */
    Optional<Payment> findByInternalReferenceNumber(String internalReferenceNumber);

    /**
     * Find payment by FSP reference number
     */
    Optional<Payment> findByFspReferenceNumber(String fspReferenceNumber);

    /**
     * Find payments by household ID
     */
    Page<Payment> findByHouseholdId(UUID householdId, Pageable pageable);

    /**
     * Find payments by program ID
     */
    Page<Payment> findByProgramId(UUID programId, Pageable pageable);

    /**
     * Find payments by status
     */
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);

    /**
     * Find payments by FSP code
     */
    Page<Payment> findByFspCode(String fspCode, Pageable pageable);

    /**
     * Find payments by batch ID
     */
    List<Payment> findByBatch_BatchId(UUID batchId);

    /**
     * Find payments that can be retried
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('FAILED', 'PENDING') AND p.retryCount < p.maxRetryCount")
    List<Payment> findRetryablePayments();

    /**
     * Find payments scheduled for processing
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.scheduledDate <= :currentTime")
    List<Payment> findScheduledPayments(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find payments by date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Page<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate, 
                                 Pageable pageable);

    /**
     * Get payment statistics by status
     */
    @Query("SELECT p.status, COUNT(p), SUM(p.amount) FROM Payment p GROUP BY p.status")
    List<Object[]> getPaymentStatistics();

    /**
     * Get payment statistics by FSP
     */
    @Query("SELECT p.fspCode, COUNT(p), SUM(p.amount) FROM Payment p GROUP BY p.fspCode")
    List<Object[]> getPaymentStatisticsByFsp();

    /**
     * Get total amount by status and date range
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Count payments by status
     */
    long countByStatus(Payment.PaymentStatus status);

    /**
     * Count payments by household ID and status
     */
    long countByHouseholdIdAndStatus(UUID householdId, Payment.PaymentStatus status);

    /**
     * Find failed payments for retry
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < p.maxRetryCount AND p.updatedAt <= :retryAfter")
    List<Payment> findFailedPaymentsForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    /**
     * Find payments by multiple criteria
     */
    @Query("SELECT p FROM Payment p WHERE " +
           "(:householdId IS NULL OR p.householdId = :householdId) AND " +
           "(:programId IS NULL OR p.programId = :programId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:fspCode IS NULL OR p.fspCode = :fspCode) AND " +
           "(:startDate IS NULL OR p.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR p.createdAt <= :endDate)")
    Page<Payment> findByCriteria(@Param("householdId") UUID householdId,
                                @Param("programId") UUID programId,
                                @Param("status") Payment.PaymentStatus status,
                                @Param("fspCode") String fspCode,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);

    /**
     * Check if payment exists for household and program
     */
    boolean existsByHouseholdIdAndProgramIdAndStatus(UUID householdId, UUID programId, Payment.PaymentStatus status);

    /**
     * Get daily payment volume
     */
    @Query("SELECT DATE(p.createdAt), COUNT(p), SUM(p.amount) FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> getDailyPaymentVolume(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Get payments requiring reconciliation
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PROCESSING' AND p.processedDate <= :cutoffTime")
    List<Payment> findPaymentsRequiringReconciliation(@Param("cutoffTime") LocalDateTime cutoffTime);
}
