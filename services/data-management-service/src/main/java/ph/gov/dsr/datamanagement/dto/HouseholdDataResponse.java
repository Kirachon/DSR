package ph.gov.dsr.datamanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for household data operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdDataResponse {

    private String status; // SUCCESS, ERROR, WARNING
    private String message;
    private String errorMessage;
    private List<String> validationErrors;
    private List<String> warnings;

    // Household data
    private UUID householdId;
    private String householdNumber;
    private String headOfHouseholdPsn;
    private BigDecimal monthlyIncome;
    private Integer totalMembers;
    private String region;
    private String province;
    private String municipality;
    private String barangay;
    private String streetAddress;
    private String zipCode;

    private Boolean isIndigenous;
    private Boolean isPwdHousehold;
    private Boolean isSeniorCitizenHousehold;
    private Boolean isSoloParentHousehold;

    private String housingType;
    private String housingTenure;
    private String waterSource;
    private String toiletFacility;
    private String electricitySource;
    private String cookingFuel;

    private String householdStatus;
    private String sourceSystem;
    private String preferredLanguage;
    private String notes;

    private LocalDateTime registrationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Household members
    private List<HouseholdMemberResponse> members;

    // Processing metadata
    private ProcessingMetadata processingMetadata;

    // Statistics and analytics
    private HouseholdStatistics statistics;

    // Additional data
    private Map<String, Object> additionalData;

    /**
     * Nested class for household member response data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HouseholdMemberResponse {
        
        private UUID memberId;
        private String firstName;
        private String lastName;
        private String middleName;
        private LocalDate dateOfBirth;
        private Integer age;
        private String gender;
        private String relationshipToHead;
        private String civilStatus;
        private String nationality;
        private String religion;
        private String occupation;
        private BigDecimal monthlyIncome;
        private String educationLevel;
        private String psn;
        private String philsysId;

        private Boolean isPwd;
        private Boolean isSeniorCitizen;
        private Boolean isIndigenous;
        private Boolean isPregnant;
        private Boolean isLactating;

        private String healthConditions;
        private String specialNeeds;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Map<String, Object> additionalData;
    }

    /**
     * Processing metadata for the household data operation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingMetadata {
        
        private String operationType; // CREATE, UPDATE, DELETE, VALIDATE
        private LocalDateTime processedAt;
        private String processedBy;
        private Long processingTimeMs;
        private String batchId;
        private Integer recordNumber;
        
        private Boolean validationPassed;
        private Boolean deduplicationChecked;
        private Boolean philsysVerified;
        
        private String dataQualityScore;
        private List<String> dataQualityIssues;
        
        private Map<String, Object> processingFlags;
    }

    /**
     * Household statistics and derived data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HouseholdStatistics {
        
        private BigDecimal perCapitaIncome;
        private Integer dependentsCount;
        private Integer workingMembersCount;
        private Integer childrenCount;
        private Integer seniorsCount;
        private Integer pwdCount;
        
        private Boolean belowPovertyLine;
        private String povertyClassification;
        private String vulnerabilityScore;
        
        private List<String> eligiblePrograms;
        private List<String> currentBenefits;
        
        private Map<String, Object> derivedMetrics;
    }

    /**
     * Factory methods for common response types
     */
    public static HouseholdDataResponse success(String message) {
        return HouseholdDataResponse.builder()
            .status("SUCCESS")
            .message(message)
            .build();
    }

    public static HouseholdDataResponse error(String errorMessage) {
        return HouseholdDataResponse.builder()
            .status("ERROR")
            .errorMessage(errorMessage)
            .build();
    }

    public static HouseholdDataResponse warning(String message, List<String> warnings) {
        return HouseholdDataResponse.builder()
            .status("WARNING")
            .message(message)
            .warnings(warnings)
            .build();
    }

    public static HouseholdDataResponse validationError(List<String> validationErrors) {
        return HouseholdDataResponse.builder()
            .status("ERROR")
            .errorMessage("Validation failed")
            .validationErrors(validationErrors)
            .build();
    }

    // Utility methods
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean isError() {
        return "ERROR".equals(status);
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }
}
