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
 * SecurityEvent entity for tracking security incidents and events
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "security_events", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // THREAT_DETECTED, INTRUSION_ATTEMPT, POLICY_VIOLATION

    @NotBlank
    @Column(name = "severity", nullable = false, length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @NotBlank
    @Column(name = "status", nullable = false, length = 50)
    private String status; // OPEN, INVESTIGATING, RESOLVED, CLOSED

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "target_resource", length = 255)
    private String targetResource;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "attack_vector", length = 100)
    private String attackVector; // SQL_INJECTION, XSS, BRUTE_FORCE, MALWARE

    @Column(name = "threat_indicators", columnDefinition = "TEXT")
    private String threatIndicators; // JSON array of IOCs

    @Column(name = "impact_assessment", columnDefinition = "TEXT")
    private String impactAssessment;

    @Column(name = "mitigation_actions", columnDefinition = "TEXT")
    private String mitigationActions; // JSON array of actions taken

    @Column(name = "false_positive", nullable = false)
    @Builder.Default
    private Boolean falsePositive = false;

    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.0 to 1.0

    @Column(name = "risk_score")
    private Integer riskScore; // 1 to 100

    @Column(name = "assigned_to", columnDefinition = "UUID")
    private UUID assignedTo;

    @Column(name = "assigned_team", length = 100)
    private String assignedTeam;

    @Column(name = "escalated", nullable = false)
    @Builder.Default
    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "compliance_impact", length = 500)
    private String complianceImpact; // JSON array of affected compliance frameworks

    @Column(name = "notification_sent", nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    @Column(name = "external_reference", length = 255)
    private String externalReference; // Reference to external security tools

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "detection_method", length = 100)
    private String detectionMethod; // AUTOMATED, MANUAL, EXTERNAL_ALERT

    @Column(name = "evidence_collected", columnDefinition = "TEXT")
    private String evidenceCollected; // JSON array of evidence

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
        if (status == null) {
            status = "OPEN";
        }
        if (confidenceScore == null) {
            confidenceScore = 0.5;
        }
        if (riskScore == null) {
            riskScore = 50;
        }
    }
}
