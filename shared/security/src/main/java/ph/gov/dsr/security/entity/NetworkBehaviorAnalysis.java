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
 * NetworkBehaviorAnalysis entity for tracking network behavior patterns
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "network_behavior_analysis", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkBehaviorAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "analysis_id", unique = true, nullable = false, length = 100)
    private String analysisId;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "destination_ip", length = 45)
    private String destinationIp;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @NotBlank
    @Column(name = "behavior_type", nullable = false, length = 50)
    private String behaviorType; // LOGIN_PATTERN, DATA_ACCESS, NETWORK_USAGE, APPLICATION_USAGE

    @Column(name = "baseline_established", nullable = false)
    @Builder.Default
    private Boolean baselineEstablished = false;

    @Column(name = "baseline_period_days")
    private Integer baselinePeriodDays;

    @Column(name = "baseline_data", columnDefinition = "TEXT")
    private String baselineData; // JSON baseline metrics

    @Column(name = "current_metrics", columnDefinition = "TEXT")
    private String currentMetrics; // JSON current metrics

    @Column(name = "anomaly_score")
    private Double anomalyScore; // 0.0 to 1.0

    @Column(name = "confidence_level")
    private Double confidenceLevel; // 0.0 to 1.0

    @Column(name = "anomaly_detected", nullable = false)
    @Builder.Default
    private Boolean anomalyDetected = false;

    @Column(name = "anomaly_type", length = 100)
    private String anomalyType; // VOLUME, TIMING, LOCATION, PROTOCOL, APPLICATION

    @Column(name = "anomaly_description", columnDefinition = "TEXT")
    private String anomalyDescription;

    @Column(name = "risk_level", length = 20)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "threat_indicators", columnDefinition = "TEXT")
    private String threatIndicators; // JSON array of threat indicators

    @Column(name = "analysis_period_start")
    private LocalDateTime analysisPeriodStart;

    @Column(name = "analysis_period_end")
    private LocalDateTime analysisPeriodEnd;

    @Column(name = "data_points_analyzed")
    private Long dataPointsAnalyzed;

    @Column(name = "patterns_identified", columnDefinition = "TEXT")
    private String patternsIdentified; // JSON array of identified patterns

    @Column(name = "deviations_found", columnDefinition = "TEXT")
    private String deviationsFound; // JSON array of deviations

    @Column(name = "ml_model_used", length = 100)
    private String mlModelUsed;

    @Column(name = "ml_model_version", length = 50)
    private String mlModelVersion;

    @Column(name = "ml_confidence_score")
    private Double mlConfidenceScore;

    @Column(name = "false_positive_probability")
    private Double falsePositiveProbability;

    @Column(name = "recommended_actions", columnDefinition = "TEXT")
    private String recommendedActions; // JSON array of recommended actions

    @Column(name = "automated_response_triggered", nullable = false)
    @Builder.Default
    private Boolean automatedResponseTriggered = false;

    @Column(name = "response_actions_taken", columnDefinition = "TEXT")
    private String responseActionsTaken; // JSON array of actions taken

    @Column(name = "analyst_reviewed", nullable = false)
    @Builder.Default
    private Boolean analystReviewed = false;

    @Column(name = "reviewed_by", columnDefinition = "UUID")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "analyst_notes", columnDefinition = "TEXT")
    private String analystNotes;

    @Column(name = "false_positive", nullable = false)
    @Builder.Default
    private Boolean falsePositive = false;

    @Column(name = "true_positive", nullable = false)
    @Builder.Default
    private Boolean truePositive = false;

    @Column(name = "investigation_required", nullable = false)
    @Builder.Default
    private Boolean investigationRequired = false;

    @Column(name = "investigation_status", length = 50)
    private String investigationStatus; // PENDING, IN_PROGRESS, COMPLETED, CLOSED

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "related_incidents", columnDefinition = "TEXT")
    private String relatedIncidents; // JSON array of related incident IDs

    @Column(name = "geolocation_data", columnDefinition = "TEXT")
    private String geolocationData; // JSON geolocation information

    @Column(name = "network_segment_id", columnDefinition = "UUID")
    private UUID networkSegmentId;

    @Column(name = "compliance_impact", length = 500)
    private String complianceImpact; // JSON array of compliance implications

    @Column(name = "business_impact", columnDefinition = "TEXT")
    private String businessImpact;

    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

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
        if (baselinePeriodDays == null) {
            baselinePeriodDays = 30; // 30 days default baseline
        }
        if (anomalyScore == null) {
            anomalyScore = 0.0;
        }
        if (confidenceLevel == null) {
            confidenceLevel = 0.5;
        }
        if (riskLevel == null) {
            riskLevel = "LOW";
        }
        if (investigationStatus == null) {
            investigationStatus = "PENDING";
        }
        if (dataPointsAnalyzed == null) {
            dataPointsAnalyzed = 0L;
        }
    }
}
