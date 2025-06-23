package ph.gov.dsr.eligibility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for eligibility assessment
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Schema(description = "Response for eligibility assessment")
public class EligibilityResponse {

    @Schema(description = "Philippine Statistical Number", example = "1234-5678-9012")
    private String psn;

    @Schema(description = "Program code assessed", example = "4PS_CONDITIONAL_CASH")
    private String programCode;

    @Schema(description = "Whether the household is eligible for the program")
    private Boolean isEligible;

    @Schema(description = "Calculated eligibility score (0-100)", example = "85.5")
    private BigDecimal eligibilityScore;

    @Schema(description = "Eligibility status", example = "ELIGIBLE")
    private EligibilityStatus status;

    @Schema(description = "Detailed reason for eligibility determination")
    private String reason;

    @Schema(description = "Date and time when eligibility expires")
    private LocalDateTime validUntil;

    @Schema(description = "Conditions that must be met to maintain eligibility")
    private List<String> conditions;

    @Schema(description = "Date and time of this assessment")
    private LocalDateTime lastAssessmentDate;

    @Schema(description = "Assessment details and breakdown")
    private AssessmentDetails assessmentDetails;

    @Schema(description = "Recommended next steps")
    private List<String> recommendations;

    @Schema(description = "Additional metadata about the assessment")
    private Map<String, Object> metadata;

    @Schema(description = "User who last updated the assessment")
    private String lastUpdatedBy;

    @Schema(description = "Date and time when assessment was last updated")
    private LocalDateTime lastUpdatedAt;

    // Convenience methods for backward compatibility with tests
    public BigDecimal getPmtScore() {
        if (assessmentDetails != null && assessmentDetails.incomeAssessment != null) {
            return assessmentDetails.incomeAssessment.monthlyIncome;
        }
        return null;
    }

    public BigDecimal getPovertyThreshold() {
        if (assessmentDetails != null && assessmentDetails.incomeAssessment != null) {
            return assessmentDetails.incomeAssessment.povertyThreshold;
        }
        return null;
    }

    public String getStatusReason() {
        return reason;
    }

    public LocalDateTime getAssessedAt() {
        return lastAssessmentDate;
    }

    @Data
    @Schema(description = "Detailed assessment breakdown")
    public static class AssessmentDetails {
        
        @Schema(description = "Income-based assessment results")
        private IncomeAssessment incomeAssessment;

        @Schema(description = "Demographic-based assessment results")
        private DemographicAssessment demographicAssessment;

        @Schema(description = "Geographic-based assessment results")
        private GeographicAssessment geographicAssessment;

        @Schema(description = "Vulnerability assessment results")
        private VulnerabilityAssessment vulnerabilityAssessment;

        @Schema(description = "Program-specific criteria assessment")
        private List<CriteriaAssessment> programCriteria;

        @Schema(description = "Overall assessment summary")
        private String summary;
    }

    @Data
    @Schema(description = "Income-based assessment")
    public static class IncomeAssessment {
        
        @Schema(description = "Monthly household income", example = "15000.00")
        private BigDecimal monthlyIncome;

        @Schema(description = "Per capita income", example = "3000.00")
        private BigDecimal perCapitaIncome;

        @Schema(description = "Poverty threshold for the area", example = "12000.00")
        private BigDecimal povertyThreshold;

        @Schema(description = "Income ratio to poverty line", example = "1.25")
        private BigDecimal incomeRatio;

        @Schema(description = "Whether income meets program criteria")
        private Boolean meetsIncomeCriteria;

        @Schema(description = "Income assessment score (0-100)", example = "75.0")
        private BigDecimal score;
    }

    @Data
    @Schema(description = "Demographic-based assessment")
    public static class DemographicAssessment {
        
        @Schema(description = "Number of children under 18", example = "3")
        private Integer childrenCount;

        @Schema(description = "Number of school-age children", example = "2")
        private Integer schoolAgeChildren;

        @Schema(description = "Number of pregnant/lactating women", example = "1")
        private Integer pregnantLactatingWomen;

        @Schema(description = "Number of senior citizens", example = "1")
        private Integer seniorCitizens;

        @Schema(description = "Number of PWDs", example = "0")
        private Integer pwdCount;

        @Schema(description = "Whether demographic criteria are met")
        private Boolean meetsDemographicCriteria;

        @Schema(description = "Demographic assessment score (0-100)", example = "90.0")
        private BigDecimal score;
    }

    @Data
    @Schema(description = "Geographic-based assessment")
    public static class GeographicAssessment {
        
        @Schema(description = "Location type (URBAN/RURAL)", example = "RURAL")
        private String locationType;

        @Schema(description = "Whether in GIDA area")
        private Boolean isGida;

        @Schema(description = "Region priority level", example = "HIGH")
        private String regionPriority;

        @Schema(description = "Whether geographic criteria are met")
        private Boolean meetsGeographicCriteria;

        @Schema(description = "Geographic assessment score (0-100)", example = "80.0")
        private BigDecimal score;
    }

    @Data
    @Schema(description = "Vulnerability assessment")
    public static class VulnerabilityAssessment {
        
        @Schema(description = "Vulnerability factors identified")
        private List<String> vulnerabilityFactors;

        @Schema(description = "Overall vulnerability level", example = "HIGH")
        private String vulnerabilityLevel;

        @Schema(description = "Whether vulnerability criteria are met")
        private Boolean meetsVulnerabilityCriteria;

        @Schema(description = "Vulnerability assessment score (0-100)", example = "95.0")
        private BigDecimal score;
    }

    @Data
    @Schema(description = "Individual criteria assessment")
    public static class CriteriaAssessment {
        
        @Schema(description = "Criteria name", example = "CHILD_SCHOOL_ATTENDANCE")
        private String criteriaName;

        @Schema(description = "Criteria description")
        private String description;

        @Schema(description = "Whether criteria is met")
        private Boolean isMet;

        @Schema(description = "Criteria weight in overall assessment", example = "0.2")
        private BigDecimal weight;

        @Schema(description = "Score for this criteria (0-100)", example = "100.0")
        private BigDecimal score;

        @Schema(description = "Additional details about criteria assessment")
        private String details;
    }

    /**
     * Enumeration for eligibility status
     */
    public enum EligibilityStatus {
        ELIGIBLE("Eligible for the program"),
        INELIGIBLE("Not eligible for the program"),
        CONDITIONAL("Conditionally eligible - must meet additional requirements"),
        WAITLISTED("Eligible but placed on waiting list due to program capacity"),
        UNDER_REVIEW("Assessment under review - additional verification needed"),
        SUSPENDED("Eligibility suspended due to non-compliance"),
        EXPIRED("Previous eligibility has expired"),
        ERROR("Assessment failed due to system error");

        private final String description;

        EligibilityStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
