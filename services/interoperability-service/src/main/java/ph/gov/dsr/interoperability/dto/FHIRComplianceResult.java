package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for FHIR compliance results
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FHIR compliance result")
public class FHIRComplianceResult {

    @Schema(description = "Whether the resource is FHIR compliant", example = "true")
    private boolean compliant;

    @Schema(description = "FHIR resource type validated", example = "Patient")
    private String resourceType;

    @Schema(description = "FHIR version used for validation", example = "R4")
    private String fhirVersion;

    @Schema(description = "List of validation results from different checks")
    private List<FHIRValidationResult> validationResults;

    @Schema(description = "Overall compliance score (0-100)", example = "95.5")
    private Double complianceScore;

    @Schema(description = "Compliance level", example = "FULLY_COMPLIANT")
    private String complianceLevel;

    @Schema(description = "List of compliance errors found")
    private List<String> errors;

    @Schema(description = "List of compliance warnings found")
    private List<String> warnings;

    @Schema(description = "List of compliance recommendations")
    private List<String> recommendations;

    @Schema(description = "Error message if validation failed")
    private String errorMessage;

    @Schema(description = "Additional compliance details")
    private String details;

    @Schema(description = "When the compliance check was performed")
    private LocalDateTime validatedAt;

    @Schema(description = "Validation duration in milliseconds", example = "250")
    private Long validationDurationMs;

    @Schema(description = "Validator used", example = "FHIR_COMPLIANCE_SERVICE")
    private String validator;

    @Schema(description = "Validation context or reference", example = "PATIENT_REGISTRATION")
    private String validationContext;

    @Schema(description = "FHIR profiles validated against")
    private List<String> profilesValidated;

    @Schema(description = "Compliance metadata")
    private Map<String, Object> metadata;

    /**
     * Check if compliance check has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if compliance check has warnings
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Get total issue count (errors + warnings)
     */
    public int getTotalIssueCount() {
        int errorCount = errors != null ? errors.size() : 0;
        int warningCount = warnings != null ? warnings.size() : 0;
        return errorCount + warningCount;
    }

    /**
     * Get compliance summary
     */
    public String getComplianceSummary() {
        if (compliant) {
            return String.format("FHIR %s compliance passed for %s", fhirVersion, resourceType);
        } else {
            return String.format("FHIR %s compliance failed for %s - %d errors, %d warnings",
                               fhirVersion, resourceType,
                               errors != null ? errors.size() : 0,
                               warnings != null ? warnings.size() : 0);
        }
    }

    /**
     * Check if compliance is critical (has errors)
     */
    public boolean isCritical() {
        return hasErrors();
    }

    /**
     * Get compliance quality level
     */
    public String getQualityLevel() {
        if (!compliant) {
            return "POOR";
        } else if (hasWarnings()) {
            return "GOOD";
        } else {
            return "EXCELLENT";
        }
    }

    /**
     * Get compliance level based on results
     */
    public String getComplianceLevel() {
        if (complianceLevel != null) {
            return complianceLevel;
        }

        if (!compliant) {
            return "NON_COMPLIANT";
        }

        if (hasErrors()) {
            return "PARTIALLY_COMPLIANT";
        }

        if (hasWarnings()) {
            return "MOSTLY_COMPLIANT";
        }

        return "FULLY_COMPLIANT";
    }

    /**
     * Calculate compliance score based on validation results
     */
    public Double calculateComplianceScore() {
        if (complianceScore != null) {
            return complianceScore;
        }

        if (validationResults == null || validationResults.isEmpty()) {
            return compliant ? 100.0 : 0.0;
        }

        double totalScore = 0.0;
        int validResults = 0;

        for (FHIRValidationResult result : validationResults) {
            if (result.getValidationScore() != null) {
                totalScore += result.getValidationScore();
                validResults++;
            }
        }

        return validResults > 0 ? totalScore / validResults : (compliant ? 100.0 : 0.0);
    }

    /**
     * Check if profiles were validated
     */
    public boolean hasProfilesValidated() {
        return profilesValidated != null && !profilesValidated.isEmpty();
    }

    /**
     * Get validation result by category
     */
    public FHIRValidationResult getValidationResultByCategory(String category) {
        if (validationResults == null) {
            return null;
        }

        return validationResults.stream()
                .filter(result -> category.equals(result.getCategory()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if validation passed for specific category
     */
    public boolean isValidForCategory(String category) {
        FHIRValidationResult result = getValidationResultByCategory(category);
        return result != null && result.isValid();
    }

    /**
     * Get recommended next actions
     */
    public List<String> getRecommendedActions() {
        List<String> actions = new java.util.ArrayList<>();

        if (hasErrors()) {
            actions.add("Address FHIR compliance errors immediately");
        }

        if (hasWarnings()) {
            actions.add("Review and address FHIR compliance warnings");
        }

        if (!compliant) {
            actions.add("Ensure FHIR resource structure meets specification requirements");
        }

        if (recommendations != null) {
            actions.addAll(recommendations);
        }

        return actions;
    }
}