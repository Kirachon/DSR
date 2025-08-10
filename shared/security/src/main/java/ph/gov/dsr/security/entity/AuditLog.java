package ph.gov.dsr.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLog entity for tracking security and system events
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "audit_logs", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_subtype", length = 100)
    private String eventSubtype; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, etc.

    @NotBlank
    @Column(name = "event_category", nullable = false, length = 50)
    private String eventCategory; // AUTHENTICATION, AUTHORIZATION, DATA_ACCESS, ADMIN_ACTION

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "resource_id", length = 255)
    private String resourceId;

    @Column(name = "action", length = 100)
    private String action; // CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT

    @Column(name = "result", length = 50)
    private String result; // SUCCESS, FAILURE, PARTIAL

    @Column(name = "success")
    private Boolean success; // Whether the operation was successful

    @Column(name = "target_user_id", columnDefinition = "UUID")
    private UUID targetUserId; // For admin actions affecting other users

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Column(name = "risk_level", length = 20)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "data_before", columnDefinition = "TEXT")
    private String dataBefore;

    @Column(name = "data_after", columnDefinition = "TEXT")
    private String dataAfter;

    @Column(name = "compliance_flags", length = 500)
    private String complianceFlags; // JSON array of compliance requirements

    @Column(name = "retention_period_days")
    private Integer retentionPeriodDays;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "geolocation", length = 255)
    private String geolocation;

    @Column(name = "device_fingerprint", length = 500)
    private String deviceFingerprint;

    @Column(name = "threat_indicators", columnDefinition = "TEXT")
    private String threatIndicators; // JSON array of threat indicators

    @Column(name = "remediation_actions", columnDefinition = "TEXT")
    private String remediationActions; // JSON array of actions taken

    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    protected void onCreate() {
        if (retentionPeriodDays == null) {
            retentionPeriodDays = 2555; // 7 years default retention
        }
        if (riskLevel == null) {
            riskLevel = "LOW";
        }
    }
}
