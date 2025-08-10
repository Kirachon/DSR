package ph.gov.dsr.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NetworkPolicy entity for defining network access policies
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "network_policies", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "policy_name", unique = true, nullable = false, length = 100)
    private String policyName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(name = "policy_type", nullable = false, length = 50)
    private String policyType; // FIREWALL, ACCESS_CONTROL, QOS, ROUTING

    @NotBlank
    @Column(name = "action", nullable = false, length = 20)
    private String action; // ALLOW, DENY, LOG, REDIRECT

    @Column(name = "priority")
    private Integer priority; // Higher number = higher priority

    @Column(name = "source_network", length = 100)
    private String sourceNetwork; // CIDR notation or network segment name

    @Column(name = "destination_network", length = 100)
    private String destinationNetwork; // CIDR notation or network segment name

    @Column(name = "source_ports", length = 255)
    private String sourcePorts; // Port ranges, e.g., "80,443,8000-8999"

    @Column(name = "destination_ports", length = 255)
    private String destinationPorts; // Port ranges

    @Column(name = "protocols", length = 255)
    private String protocols; // TCP, UDP, ICMP, etc.

    @Column(name = "applications", length = 500)
    private String applications; // JSON array of application names

    @Column(name = "users", length = 500)
    private String users; // JSON array of user IDs or groups

    @Column(name = "user_groups", length = 500)
    private String userGroups; // JSON array of user group names

    @Column(name = "time_restrictions", length = 500)
    private String timeRestrictions; // JSON object with time-based rules

    @Column(name = "geo_restrictions", length = 500)
    private String geoRestrictions; // JSON array of allowed/blocked countries

    @Column(name = "device_types", length = 500)
    private String deviceTypes; // JSON array of allowed device types

    @Column(name = "encryption_required", nullable = false)
    @Builder.Default
    private Boolean encryptionRequired = false;

    @Column(name = "authentication_required", nullable = false)
    @Builder.Default
    private Boolean authenticationRequired = true;

    @Column(name = "mfa_required", nullable = false)
    @Builder.Default
    private Boolean mfaRequired = false;

    @Column(name = "certificate_required", nullable = false)
    @Builder.Default
    private Boolean certificateRequired = false;

    @Column(name = "bandwidth_limit_mbps")
    private Integer bandwidthLimitMbps;

    @Column(name = "connection_limit")
    private Integer connectionLimit;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "idle_timeout_minutes")
    private Integer idleTimeoutMinutes;

    @Column(name = "logging_enabled", nullable = false)
    @Builder.Default
    private Boolean loggingEnabled = true;

    @Column(name = "monitoring_enabled", nullable = false)
    @Builder.Default
    private Boolean monitoringEnabled = true;

    @Column(name = "alerting_enabled", nullable = false)
    @Builder.Default
    private Boolean alertingEnabled = false;

    @Column(name = "compliance_frameworks", length = 500)
    private String complianceFrameworks; // JSON array of compliance requirements

    @Column(name = "business_justification", columnDefinition = "TEXT")
    private String businessJustification;

    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment;

    @Column(name = "approval_required", nullable = false)
    @Builder.Default
    private Boolean approvalRequired = false;

    @Column(name = "approved_by", columnDefinition = "UUID")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approval_comments", columnDefinition = "TEXT")
    private String approvalComments;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    @Column(name = "review_frequency_days")
    private Integer reviewFrequencyDays;

    @Column(name = "last_reviewed_date")
    private LocalDateTime lastReviewedDate;

    @Column(name = "reviewed_by", columnDefinition = "UUID")
    private UUID reviewedBy;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "enforcement_mode", length = 20)
    private String enforcementMode; // ENFORCE, MONITOR, DISABLED

    @Column(name = "violation_count")
    private Integer violationCount;

    @Column(name = "last_violation_date")
    private LocalDateTime lastViolationDate;

    @Column(name = "usage_count")
    private Long usageCount;

    @Column(name = "last_used_date")
    private LocalDateTime lastUsedDate;

    @Column(name = "tags", length = 500)
    private String tags; // JSON array of tags

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // Additional metadata as JSON

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "UUID")
    private UUID updatedBy;

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    protected void onCreate() {
        if (priority == null) {
            priority = 100; // Default priority
        }
        if (enforcementMode == null) {
            enforcementMode = "ENFORCE";
        }
        if (violationCount == null) {
            violationCount = 0;
        }
        if (usageCount == null) {
            usageCount = 0L;
        }
        if (reviewFrequencyDays == null) {
            reviewFrequencyDays = 90; // Review every 3 months
        }
        if (sessionTimeoutMinutes == null) {
            sessionTimeoutMinutes = 60;
        }
        if (idleTimeoutMinutes == null) {
            idleTimeoutMinutes = 30;
        }
    }
}
