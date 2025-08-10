package ph.gov.dsr.eligibility.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Eligibility Assessment entity for persisting assessment results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "eligibility_assessments", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class EligibilityAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "psn", nullable = false, length = 16)
    private String psn;

    @Column(name = "household_id", columnDefinition = "UUID")
    private UUID householdId;

    @NotBlank
    @Column(name = "program_code", nullable = false, length = 50)
    private String programCode;

    @NotNull
    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EligibilityStatus status;

    @Column(name = "pmt_score", precision = 10, scale = 4)
    private BigDecimal pmtScore;

    @Column(name = "poverty_threshold", precision = 10, scale = 2)
    private BigDecimal povertyThreshold;

    @Column(name = "is_poor")
    private Boolean isPoor;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "income_score", precision = 5, scale = 2)
    private BigDecimal incomeScore;

    @Column(name = "demographic_score", precision = 5, scale = 2)
    private BigDecimal demographicScore;

    @Column(name = "geographic_score", precision = 5, scale = 2)
    private BigDecimal geographicScore;

    @Column(name = "vulnerability_score", precision = 5, scale = 2)
    private BigDecimal vulnerabilityScore;

    @Column(name = "program_criteria_score", precision = 5, scale = 2)
    private BigDecimal programCriteriaScore;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "assessment_method", length = 50)
    private String assessmentMethod = "AUTOMATED";

    @Column(name = "assessor_id", length = 100)
    private String assessorId;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "calculation_details", columnDefinition = "JSONB")
    private String calculationDetails;

    @Column(name = "rules_evaluation_result", columnDefinition = "JSONB")
    private String rulesEvaluationResult;

    @Column(name = "force_reassessment")
    private Boolean forceReassessment = false;

    @Column(name = "previous_assessment_id", columnDefinition = "UUID")
    private UUID previousAssessmentId;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum EligibilityStatus {
        ELIGIBLE,
        NOT_ELIGIBLE,
        CONDITIONAL,
        PENDING_REVIEW,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        EXPIRED,
        SUSPENDED
    }

    // Constructors
    public EligibilityAssessment() {}

    public EligibilityAssessment(String psn, String programCode) {
        this.psn = psn;
        this.programCode = programCode;
        this.assessmentDate = LocalDateTime.now();
        this.status = EligibilityStatus.PENDING_REVIEW;
    }

    // Helper methods
    
    /**
     * Check if assessment is still valid
     */
    public boolean isValid() {
        return validUntil != null && validUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if assessment is eligible status
     */
    public boolean isEligible() {
        return status == EligibilityStatus.ELIGIBLE || status == EligibilityStatus.CONDITIONAL;
    }

    /**
     * Check if assessment needs review
     */
    public boolean needsReview() {
        return status == EligibilityStatus.PENDING_REVIEW || status == EligibilityStatus.UNDER_REVIEW;
    }

    /**
     * Calculate days until expiration
     */
    public long getDaysUntilExpiration() {
        if (validUntil == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), validUntil);
    }

    /**
     * Check if assessment is expired
     */
    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDateTime.now());
    }

    /**
     * Get assessment age in days
     */
    public long getAssessmentAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(assessmentDate, LocalDateTime.now());
    }

    /**
     * Check if assessment meets poverty criteria
     */
    public boolean meetsPovertyThreshold() {
        return Boolean.TRUE.equals(isPoor) || 
               (pmtScore != null && povertyThreshold != null && pmtScore.compareTo(povertyThreshold) <= 0);
    }

    /**
     * Get overall eligibility percentage
     */
    public BigDecimal getEligibilityPercentage() {
        if (overallScore == null) {
            return BigDecimal.ZERO;
        }
        return overallScore.multiply(BigDecimal.valueOf(100));
    }
}
