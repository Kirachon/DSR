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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Program Enrollment entity for tracking beneficiary enrollment in social programs
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "program_enrollments", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class ProgramEnrollment {

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

    @Column(name = "program_name", length = 200)
    private String programName;

    @Column(name = "eligibility_assessment_id", columnDefinition = "UUID")
    private UUID eligibilityAssessmentId;

    @NotNull
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EnrollmentStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "benefit_amount", precision = 12, scale = 2)
    private BigDecimal benefitAmount;

    @Column(name = "benefit_frequency", length = 20)
    private String benefitFrequency; // MONTHLY, QUARTERLY, ANNUALLY, ONE_TIME

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // CASH, BANK_TRANSFER, MOBILE_MONEY, CHECK

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "mobile_wallet_number", length = 20)
    private String mobileWalletNumber;

    @Column(name = "priority_level")
    private Integer priorityLevel = 1; // 1=Highest, 5=Lowest

    @Column(name = "enrollment_channel", length = 50)
    private String enrollmentChannel; // ONLINE, OFFICE, MOBILE, OUTREACH

    @Column(name = "enrollment_officer", length = 100)
    private String enrollmentOfficer;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "suspension_date")
    private LocalDate suspensionDate;

    @Column(name = "suspension_reason", columnDefinition = "TEXT")
    private String suspensionReason;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;

    @Column(name = "last_benefit_date")
    private LocalDate lastBenefitDate;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "total_benefits_received", precision = 12, scale = 2)
    private BigDecimal totalBenefitsReceived = BigDecimal.ZERO;

    @Column(name = "compliance_status", length = 30)
    private String complianceStatus = "COMPLIANT"; // COMPLIANT, NON_COMPLIANT, UNDER_REVIEW

    @Column(name = "compliance_notes", columnDefinition = "TEXT")
    private String complianceNotes;

    @Column(name = "conditions_met")
    private Boolean conditionsMet = true;

    @Column(name = "conditions_details", columnDefinition = "JSONB")
    private String conditionsDetails;

    @Column(name = "special_circumstances", columnDefinition = "TEXT")
    private String specialCircumstances;

    @Column(name = "contact_preference", length = 20)
    private String contactPreference = "SMS"; // SMS, EMAIL, PHONE, MAIL

    @Column(name = "language_preference", length = 10)
    private String languagePreference = "en";

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
    public enum EnrollmentStatus {
        PENDING,
        ACTIVE,
        SUSPENDED,
        TERMINATED,
        COMPLETED,
        CANCELLED,
        UNDER_REVIEW,
        APPROVED,
        REJECTED
    }

    // Constructors
    public ProgramEnrollment() {}

    public ProgramEnrollment(String psn, String programCode) {
        this.psn = psn;
        this.programCode = programCode;
        this.enrollmentDate = LocalDate.now();
        this.status = EnrollmentStatus.PENDING;
    }

    // Helper methods
    
    /**
     * Check if enrollment is active
     */
    public boolean isActive() {
        return status == EnrollmentStatus.ACTIVE && 
               (endDate == null || endDate.isAfter(LocalDate.now()));
    }

    /**
     * Check if enrollment is expired
     */
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /**
     * Check if enrollment needs review
     */
    public boolean needsReview() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDate.now());
    }

    /**
     * Calculate enrollment duration in months
     */
    public long getEnrollmentDurationInMonths() {
        LocalDate start = startDate != null ? startDate : enrollmentDate;
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return java.time.temporal.ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calculate days until next review
     */
    public long getDaysUntilNextReview() {
        if (nextReviewDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextReviewDate);
    }

    /**
     * Check if enrollment is eligible for benefits
     */
    public boolean isEligibleForBenefits() {
        return isActive() && Boolean.TRUE.equals(conditionsMet) && 
               "COMPLIANT".equals(complianceStatus);
    }

    /**
     * Calculate monthly benefit amount
     */
    public BigDecimal getMonthlyBenefitAmount() {
        if (benefitAmount == null || benefitFrequency == null) {
            return BigDecimal.ZERO;
        }
        
        switch (benefitFrequency) {
            case "MONTHLY":
                return benefitAmount;
            case "QUARTERLY":
                return benefitAmount.divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP);
            case "ANNUALLY":
                return benefitAmount.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
            case "ONE_TIME":
                return BigDecimal.ZERO;
            default:
                return benefitAmount;
        }
    }

    /**
     * Update total benefits received
     */
    public void addBenefitPayment(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalBenefitsReceived = this.totalBenefitsReceived.add(amount);
            this.lastBenefitDate = LocalDate.now();
        }
    }

    /**
     * Check if enrollment is in good standing
     */
    public boolean isInGoodStanding() {
        return isActive() && "COMPLIANT".equals(complianceStatus) && 
               Boolean.TRUE.equals(conditionsMet);
    }
}
