package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for FHIR validation results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Data
@Builder
@Schema(description = "FHIR validation result")
public class FHIRValidationResult {

    @Schema(description = "Whether the validation passed", example = "true")
    private boolean valid;

    @Schema(description = "FHIR resource type being validated", example = "Patient")
    private String resourceType;

    @Schema(description = "FHIR version used for validation", example = "R4")
    private String fhirVersion;

    @Schema(description = "List of FHIR profiles validated against")
    private List<String> profiles;

    @Schema(description = "Validation errors found")
    private List<String> errors;

    @Schema(description = "Validation warnings found")
    private List<String> warnings;

    @Schema(description = "Validation score (0-100)", example = "95.5")
    private Double validationScore;

    @Schema(description = "Validation category", example = "STRUCTURE")
    private String category;

    @Schema(description = "Validation severity", example = "MEDIUM")
    private String severity;

    @Schema(description = "Additional validation details")
    private String details;

    @Schema(description = "When the validation was performed")
    private LocalDateTime validatedAt;

    @Schema(description = "Validation duration in milliseconds", example = "150")
    private Long validationDurationMs;

    @Schema(description = "Validator used", example = "FHIR_COMPLIANCE_SERVICE")
    private String validator;

    @Schema(description = "Validation context or reference", example = "PATIENT_REGISTRATION")
    private String validationContext;

    /**
     * Check if validation has errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Check if validation has warnings
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
     * Get validation summary
     */
    public String getValidationSummary() {
        if (valid) {
            return String.format("Validation passed for %s (FHIR %s)", resourceType, fhirVersion);
        } else {
            return String.format("Validation failed for %s (FHIR %s) - %d errors, %d warnings", 
                               resourceType, fhirVersion, 
                               errors != null ? errors.size() : 0,
                               warnings != null ? warnings.size() : 0);
        }
    }

    /**
     * Check if validation is critical (has errors)
     */
    public boolean isCritical() {
        return hasErrors();
    }

    /**
     * Get validation quality level
     */
    public String getQualityLevel() {
        if (!valid) {
            return "POOR";
        } else if (hasWarnings()) {
            return "GOOD";
        } else {
            return "EXCELLENT";
        }
    }
}
