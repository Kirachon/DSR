package ph.gov.dsr.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PaymentAuditLog entity for tracking all payment-related activities
 */
@Entity
@Table(name = "payment_audit_logs", indexes = {
    @Index(name = "idx_audit_payment_id", columnList = "payment_id"),
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id")
    private UUID auditId;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "batch_id")
    private UUID batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "fsp_request", columnDefinition = "text")
    private String fspRequest;

    @Column(name = "fsp_response", columnDefinition = "text")
    private String fspResponse;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "additional_data", columnDefinition = "jsonb")
    private String additionalData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Event type enumeration for audit logging
     */
    public enum EventType {
        PAYMENT_CREATED("Payment created"),
        PAYMENT_SUBMITTED("Payment submitted to FSP"),
        PAYMENT_PROCESSING("Payment processing started"),
        PAYMENT_COMPLETED("Payment completed successfully"),
        PAYMENT_FAILED("Payment failed"),
        PAYMENT_CANCELLED("Payment cancelled"),
        PAYMENT_REFUNDED("Payment refunded"),
        PAYMENT_RETRY("Payment retry attempted"),
        BATCH_CREATED("Payment batch created"),
        BATCH_STARTED("Payment batch processing started"),
        BATCH_COMPLETED("Payment batch processing completed"),
        BATCH_FAILED("Payment batch processing failed"),
        BATCH_CANCELLED("Payment batch cancelled"),
        FSP_REQUEST("Request sent to FSP"),
        FSP_RESPONSE("Response received from FSP"),
        FSP_WEBHOOK("Webhook received from FSP"),
        FSP_ERROR("FSP error occurred"),
        RECONCILIATION_STARTED("Reconciliation process started"),
        RECONCILIATION_COMPLETED("Reconciliation process completed"),
        RECONCILIATION_DISCREPANCY("Reconciliation discrepancy found"),
        SYSTEM_ERROR("System error occurred"),
        CONFIGURATION_CHANGED("Configuration changed"),
        MANUAL_INTERVENTION("Manual intervention performed");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Static factory methods for common audit events
    public static PaymentAuditLog paymentCreated(UUID paymentId, String userId) {
        return PaymentAuditLog.builder()
            .paymentId(paymentId)
            .eventType(EventType.PAYMENT_CREATED)
            .description("Payment created")
            .userId(userId)
            .build();
    }

    public static PaymentAuditLog statusChanged(UUID paymentId, String oldStatus, String newStatus, String userId) {
        return PaymentAuditLog.builder()
            .paymentId(paymentId)
            .eventType(EventType.PAYMENT_PROCESSING)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .description(String.format("Payment status changed from %s to %s", oldStatus, newStatus))
            .userId(userId)
            .build();
    }

    public static PaymentAuditLog fspRequest(UUID paymentId, String request, String correlationId) {
        return PaymentAuditLog.builder()
            .paymentId(paymentId)
            .eventType(EventType.FSP_REQUEST)
            .fspRequest(request)
            .correlationId(correlationId)
            .description("Request sent to FSP")
            .build();
    }

    public static PaymentAuditLog fspResponse(UUID paymentId, String response, String correlationId) {
        return PaymentAuditLog.builder()
            .paymentId(paymentId)
            .eventType(EventType.FSP_RESPONSE)
            .fspResponse(response)
            .correlationId(correlationId)
            .description("Response received from FSP")
            .build();
    }

    public static PaymentAuditLog error(UUID paymentId, String errorCode, String errorMessage, String userId) {
        return PaymentAuditLog.builder()
            .paymentId(paymentId)
            .eventType(EventType.SYSTEM_ERROR)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .description("System error occurred")
            .userId(userId)
            .build();
    }
}
