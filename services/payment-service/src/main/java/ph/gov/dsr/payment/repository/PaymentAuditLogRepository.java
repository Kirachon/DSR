package ph.gov.dsr.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.payment.entity.PaymentAuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for PaymentAuditLog entity
 */
@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, UUID> {

    /**
     * Find audit logs by payment ID
     */
    Page<PaymentAuditLog> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId, Pageable pageable);

    /**
     * Find audit logs by batch ID
     */
    Page<PaymentAuditLog> findByBatchIdOrderByCreatedAtDesc(UUID batchId, Pageable pageable);

    /**
     * Find audit logs by event type
     */
    Page<PaymentAuditLog> findByEventTypeOrderByCreatedAtDesc(PaymentAuditLog.EventType eventType, Pageable pageable);

    /**
     * Find audit logs by user ID
     */
    Page<PaymentAuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find audit logs by correlation ID
     */
    List<PaymentAuditLog> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);

    /**
     * Find audit logs by date range
     */
    @Query("SELECT a FROM PaymentAuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<PaymentAuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * Find error logs
     */
    @Query("SELECT a FROM PaymentAuditLog a WHERE a.errorCode IS NOT NULL OR a.errorMessage IS NOT NULL ORDER BY a.createdAt DESC")
    Page<PaymentAuditLog> findErrorLogs(Pageable pageable);

    /**
     * Find FSP interaction logs
     */
    @Query("SELECT a FROM PaymentAuditLog a WHERE a.eventType IN ('FSP_REQUEST', 'FSP_RESPONSE', 'FSP_WEBHOOK', 'FSP_ERROR') ORDER BY a.createdAt DESC")
    Page<PaymentAuditLog> findFspInteractionLogs(Pageable pageable);

    /**
     * Get audit statistics by event type
     */
    @Query("SELECT a.eventType, COUNT(a) FROM PaymentAuditLog a GROUP BY a.eventType")
    List<Object[]> getAuditStatisticsByEventType();

    /**
     * Count audit logs by payment ID
     */
    long countByPaymentId(UUID paymentId);

    /**
     * Find recent audit logs for dashboard
     */
    @Query("SELECT a FROM PaymentAuditLog a ORDER BY a.createdAt DESC")
    Page<PaymentAuditLog> findRecentAuditLogs(Pageable pageable);
}
