package ph.gov.dsr.eligibility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for eligibility assessment
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Schema(description = "Request for eligibility assessment")
public class EligibilityRequest {

    @NotBlank(message = "PSN is required")
    @Pattern(regexp = "^[0-9]{4}-[0-9]{4}-[0-9]{4}$", message = "PSN must be in format XXXX-XXXX-XXXX")
    @Schema(description = "Philippine Statistical Number", example = "1234-5678-9012")
    private String psn;

    @NotBlank(message = "Program code is required")
    @Pattern(regexp = "^[A-Z0-9_]{3,20}$", message = "Program code must be 3-20 characters, uppercase letters, numbers, and underscores only")
    @Schema(description = "Program code to assess eligibility for", example = "4PS_CONDITIONAL_CASH")
    private String programCode;

    @Schema(description = "Household information for assessment")
    private HouseholdInfo householdInfo;

    @Schema(description = "List of household members")
    private List<HouseholdMemberInfo> members;

    @Schema(description = "Additional assessment parameters")
    private Map<String, Object> additionalParameters;

    @Schema(description = "Force reassessment even if recent assessment exists")
    private Boolean forceReassessment = false;

    @Data
    @Schema(description = "Household information for eligibility assessment")
    public static class HouseholdInfo {
        
        @NotBlank(message = "Household number is required")
        @Schema(description = "Unique household identifier", example = "HH-2024-001234")
        private String householdNumber;

        @NotNull(message = "Monthly income is required")
        @Schema(description = "Total monthly household income", example = "15000.00")
        private BigDecimal monthlyIncome;

        @Schema(description = "Number of household members", example = "5")
        private Integer totalMembers;

        @Schema(description = "Whether household is in indigenous community")
        private Boolean isIndigenous = false;

        @Schema(description = "Whether household has PWD members")
        private Boolean hasPwdMembers = false;

        @Schema(description = "Whether household has senior citizen members")
        private Boolean hasSeniorCitizens = false;

        @Schema(description = "Whether household is headed by solo parent")
        private Boolean isSoloParentHousehold = false;

        @Schema(description = "Geographic location information")
        private LocationInfo location;

        @Schema(description = "Current social protection programs enrolled in")
        private List<String> currentPrograms;
    }

    @Data
    @Schema(description = "Household member information for eligibility assessment")
    public static class HouseholdMemberInfo {
        
        @NotBlank(message = "Member PSN is required")
        @Pattern(regexp = "^[0-9]{4}-[0-9]{4}-[0-9]{4}$", message = "PSN must be in format XXXX-XXXX-XXXX")
        @Schema(description = "Member's Philippine Statistical Number", example = "1234-5678-9013")
        private String psn;

        @NotBlank(message = "First name is required")
        @Schema(description = "Member's first name", example = "Juan")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Schema(description = "Member's last name", example = "Dela Cruz")
        private String lastName;

        @NotNull(message = "Date of birth is required")
        @Schema(description = "Member's date of birth", example = "1990-05-15")
        private LocalDate dateOfBirth;

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(M|F)$", message = "Gender must be M or F")
        @Schema(description = "Member's gender", example = "M")
        private String gender;

        @Schema(description = "Whether member is head of household")
        private Boolean isHeadOfHousehold = false;

        @Schema(description = "Relationship to head of household", example = "SPOUSE")
        private String relationshipToHead;

        @Schema(description = "Member's education level", example = "HIGH_SCHOOL_GRADUATE")
        private String educationLevel;

        @Schema(description = "Member's employment status", example = "EMPLOYED")
        private String employmentStatus;

        @Schema(description = "Member's monthly income", example = "8000.00")
        private BigDecimal monthlyIncome;

        @Schema(description = "Whether member is a person with disability")
        private Boolean isPwd = false;

        @Schema(description = "Type of disability if PWD", example = "VISUAL_IMPAIRMENT")
        private String pwdType;

        @Schema(description = "Whether member is pregnant")
        private Boolean isPregnant = false;

        @Schema(description = "Whether member is lactating")
        private Boolean isLactating = false;

        @Schema(description = "Member's health conditions")
        private List<String> healthConditions;

        @Schema(description = "Member's age in years", example = "34")
        private Integer age;
    }

    @Data
    @Schema(description = "Geographic location information")
    public static class LocationInfo {
        
        @Schema(description = "Region code", example = "NCR")
        private String region;

        @Schema(description = "Province name", example = "Metro Manila")
        private String province;

        @Schema(description = "City/Municipality name", example = "Quezon City")
        private String cityMunicipality;

        @Schema(description = "Barangay name", example = "Barangay Commonwealth")
        private String barangay;

        @Schema(description = "Complete address", example = "123 Main St, Barangay Commonwealth, Quezon City")
        private String completeAddress;

        @Schema(description = "Whether location is urban or rural", example = "URBAN")
        private String locationType;

        @Schema(description = "Whether area is classified as GIDA (Geographically Isolated and Disadvantaged Area)")
        private Boolean isGida = false;
    }
}
