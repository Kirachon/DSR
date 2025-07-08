package ph.gov.dsr.eligibility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for program information
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Schema(description = "Information about a social protection program")
public class ProgramInfo {

    @Schema(description = "Unique program code", example = "4PS_CONDITIONAL_CASH")
    private String programCode;

    @Schema(description = "Program display name", example = "Pantawid Pamilyang Pilipino Program")
    private String programName;

    @Schema(description = "Program description")
    private String description;

    @Schema(description = "Implementing agency", example = "DSWD")
    private String implementingAgency;

    @Schema(description = "Program type", example = "CONDITIONAL_CASH_TRANSFER")
    private ProgramType programType;

    @Schema(description = "Program status", example = "ACTIVE")
    private ProgramStatus status;

    @Schema(description = "Program start date")
    private LocalDate startDate;

    @Schema(description = "Program end date (if applicable)")
    private LocalDate endDate;

    @Schema(description = "Target beneficiary count", example = "1000000")
    private Integer targetBeneficiaries;

    @Schema(description = "Current beneficiary count", example = "850000")
    private Integer currentBeneficiaries;

    @Schema(description = "Available slots", example = "150000")
    private Integer availableSlots;

    @Schema(description = "Program budget allocation", example = "50000000000.00")
    private BigDecimal budgetAllocation;

    @Schema(description = "Benefit amount per beneficiary", example = "1400.00")
    private BigDecimal benefitAmount;

    @Schema(description = "Benefit frequency", example = "MONTHLY")
    private BenefitFrequency benefitFrequency;

    @Schema(description = "Eligibility criteria for the program")
    private List<EligibilityCriteria> eligibilityCriteria;

    @Schema(description = "Required documents for application")
    private List<String> requiredDocuments;

    @Schema(description = "Program-specific parameters")
    private Map<String, Object> programParameters;

    @Schema(description = "Geographic coverage areas")
    private List<String> coverageAreas;

    @Schema(description = "Whether program is currently accepting applications")
    private Boolean acceptingApplications;

    @Schema(description = "Target beneficiary categories")
    private List<String> targetBeneficiaryCategories;

    @Schema(description = "Date when the program was created")
    private java.time.LocalDate createdAt;

    @Schema(description = "Date when the program was last updated")
    private java.time.LocalDate updatedAt;

    @Data
    @Schema(description = "Eligibility criteria for a program")
    public static class EligibilityCriteria {
        
        @Schema(description = "Criteria name", example = "INCOME_THRESHOLD")
        private String criteriaName;

        @Schema(description = "Criteria description")
        private String description;

        @Schema(description = "Criteria type", example = "INCOME")
        private CriteriaType criteriaType;

        @Schema(description = "Whether criteria is mandatory")
        private Boolean isMandatory;

        @Schema(description = "Criteria weight in assessment", example = "0.3")
        private BigDecimal weight;

        @Schema(description = "Minimum threshold value")
        private Object minValue;

        @Schema(description = "Maximum threshold value")
        private Object maxValue;

        @Schema(description = "Expected value for criteria")
        private Object expectedValue;

        @Schema(description = "Validation rules for criteria")
        private List<String> validationRules;
    }

    /**
     * Enumeration for program types
     */
    public enum ProgramType {
        CONDITIONAL_CASH_TRANSFER("Conditional Cash Transfer"),
        UNCONDITIONAL_CASH_TRANSFER("Unconditional Cash Transfer"),
        FOOD_ASSISTANCE("Food Assistance Program"),
        HEALTH_INSURANCE("Health Insurance Program"),
        EDUCATION_ASSISTANCE("Education Assistance Program"),
        LIVELIHOOD_PROGRAM("Livelihood Development Program"),
        LIVELIHOOD("Livelihood Support Program"),
        HOUSING_ASSISTANCE("Housing Assistance Program"),
        DISASTER_RELIEF("Disaster Relief Program"),
        SENIOR_CITIZEN_BENEFIT("Senior Citizen Benefit Program"),
        SOCIAL_PENSION("Social Pension Program"),
        PWD_ASSISTANCE("Person with Disability Assistance"),
        INDIGENOUS_SUPPORT("Indigenous Peoples Support Program"),
        SOLO_PARENT_ASSISTANCE("Solo Parent Assistance Program");

        private final String description;

        ProgramType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumeration for program status
     */
    public enum ProgramStatus {
        ACTIVE("Program is currently active and accepting applications"),
        INACTIVE("Program is temporarily inactive"),
        SUSPENDED("Program is suspended"),
        TERMINATED("Program has been terminated"),
        PLANNING("Program is in planning phase"),
        PILOT("Program is in pilot implementation");

        private final String description;

        ProgramStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumeration for benefit frequency
     */
    public enum BenefitFrequency {
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        QUARTERLY("Quarterly"),
        SEMI_ANNUAL("Semi-Annual"),
        ANNUAL("Annual"),
        ONE_TIME("One-time"),
        AS_NEEDED("As Needed");

        private final String description;

        BenefitFrequency(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enumeration for criteria types
     */
    public enum CriteriaType {
        INCOME("Income-based criteria"),
        DEMOGRAPHIC("Demographic criteria"),
        GEOGRAPHIC("Geographic criteria"),
        HEALTH("Health-related criteria"),
        EDUCATION("Education-related criteria"),
        EMPLOYMENT("Employment-related criteria"),
        HOUSING("Housing-related criteria"),
        VULNERABILITY("Vulnerability criteria"),
        BEHAVIORAL("Behavioral compliance criteria"),
        DOCUMENTATION("Documentation requirements");

        private final String description;

        CriteriaType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
