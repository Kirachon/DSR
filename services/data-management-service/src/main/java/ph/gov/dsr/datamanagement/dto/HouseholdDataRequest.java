package ph.gov.dsr.datamanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for household data operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdDataRequest {

    @NotBlank(message = "Household number is required")
    @Size(max = 50, message = "Household number cannot exceed 50 characters")
    private String householdNumber;

    @NotBlank(message = "Head of household PSN is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}$", message = "PSN must be in format XXXX-XXXX-XXXX")
    private String headOfHouseholdPsn;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Total members is required")
    @Min(value = 1, message = "Total members must be at least 1")
    @Max(value = 50, message = "Total members cannot exceed 50")
    private Integer totalMembers;

    @NotBlank(message = "Region is required")
    @Size(max = 100, message = "Region cannot exceed 100 characters")
    private String region;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province cannot exceed 100 characters")
    private String province;

    @NotBlank(message = "Municipality is required")
    @Size(max = 100, message = "Municipality cannot exceed 100 characters")
    private String municipality;

    @NotBlank(message = "Barangay is required")
    @Size(max = 100, message = "Barangay cannot exceed 100 characters")
    private String barangay;

    @Size(max = 200, message = "Street address cannot exceed 200 characters")
    private String streetAddress;

    @Pattern(regexp = "^\\d{4}$", message = "Zip code must be 4 digits")
    private String zipCode;

    @Builder.Default
    private Boolean isIndigenous = false;
    @Builder.Default
    private Boolean isPwdHousehold = false;
    @Builder.Default
    private Boolean isSeniorCitizenHousehold = false;
    @Builder.Default
    private Boolean isSoloParentHousehold = false;

    @Size(max = 50, message = "Housing type cannot exceed 50 characters")
    private String housingType;

    @Size(max = 50, message = "Housing tenure cannot exceed 50 characters")
    private String housingTenure;

    @Size(max = 50, message = "Water source cannot exceed 50 characters")
    private String waterSource;

    @Size(max = 50, message = "Toilet facility cannot exceed 50 characters")
    private String toiletFacility;

    @Size(max = 50, message = "Electricity source cannot exceed 50 characters")
    private String electricitySource;

    @Size(max = 50, message = "Cooking fuel cannot exceed 50 characters")
    private String cookingFuel;

    @Size(max = 20, message = "Status cannot exceed 20 characters")
    @Builder.Default
    private String status = "ACTIVE";

    @NotBlank(message = "Source system is required")
    @Size(max = 50, message = "Source system cannot exceed 50 characters")
    private String sourceSystem;

    @Size(max = 10, message = "Preferred language cannot exceed 10 characters")
    @Builder.Default
    private String preferredLanguage = "en";

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private LocalDateTime registrationDate;

    // Household members data
    private List<HouseholdMemberData> members;

    // Additional metadata
    private Map<String, Object> additionalData;

    // Validation flags
    @Builder.Default
    private Boolean validateReferences = true;
    @Builder.Default
    private Boolean strictValidation = false;

    /**
     * Nested class for household member data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HouseholdMemberData {
        
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        private String lastName;

        @Size(max = 100, message = "Middle name cannot exceed 100 characters")
        private String middleName;

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        private LocalDate dateOfBirth;

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
        private String gender;

        @NotBlank(message = "Relationship to head is required")
        @Size(max = 50, message = "Relationship cannot exceed 50 characters")
        private String relationshipToHead;

        @Size(max = 50, message = "Civil status cannot exceed 50 characters")
        private String civilStatus;

        @Size(max = 50, message = "Nationality cannot exceed 50 characters")
        @Builder.Default
        private String nationality = "Filipino";

        @Size(max = 50, message = "Religion cannot exceed 50 characters")
        private String religion;

        @Size(max = 100, message = "Occupation cannot exceed 100 characters")
        private String occupation;

        @DecimalMin(value = "0.0", message = "Monthly income cannot be negative")
        private BigDecimal monthlyIncome;

        @Size(max = 50, message = "Education level cannot exceed 50 characters")
        private String educationLevel;

        @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}$", message = "PSN must be in format XXXX-XXXX-XXXX")
        private String psn;

        @Size(max = 20, message = "PhilSys ID cannot exceed 20 characters")
        private String philsysId;

        @Builder.Default
        private Boolean isPwd = false;
        @Builder.Default
        private Boolean isSeniorCitizen = false;
        @Builder.Default
        private Boolean isIndigenous = false;
        @Builder.Default
        private Boolean isPregnant = false;
        @Builder.Default
        private Boolean isLactating = false;

        @Size(max = 500, message = "Health conditions cannot exceed 500 characters")
        private String healthConditions;

        @Size(max = 500, message = "Special needs cannot exceed 500 characters")
        private String specialNeeds;

        private Map<String, Object> additionalData;
    }
}
