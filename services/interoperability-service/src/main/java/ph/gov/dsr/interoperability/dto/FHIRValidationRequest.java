package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for FHIR validation requests
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FHIR validation request")
public class FHIRValidationRequest {

    @NotBlank(message = "Resource type is required")
    @Schema(description = "FHIR resource type to validate", example = "Patient", required = true)
    private String resourceType;

    @NotNull(message = "Resource data is required")
    @Schema(description = "FHIR resource data to validate", required = true)
    private Map<String, Object> resourceData;

    @Schema(description = "FHIR version to validate against", example = "R4")
    @Builder.Default
    private String fhirVersion = "R4";

    @Schema(description = "FHIR profiles to validate against")
    private List<String> profiles;

    @Schema(description = "Validation context or reference", example = "PATIENT_REGISTRATION")
    private String validationContext;

    @Schema(description = "Strict validation mode", example = "true")
    @Builder.Default
    private Boolean strictMode = true;

    @Schema(description = "Include terminology validation", example = "true")
    @Builder.Default
    private Boolean includeTerminologyValidation = true;

    @Schema(description = "Include profile validation", example = "true")
    @Builder.Default
    private Boolean includeProfileValidation = true;

    @Schema(description = "Include structure validation", example = "true")
    @Builder.Default
    private Boolean includeStructureValidation = true;

    @Schema(description = "Custom validation rules")
    private Map<String, Object> customValidationRules;

    @Schema(description = "Validation timeout in milliseconds", example = "30000")
    @Builder.Default
    private Long timeoutMs = 30000L;

    @Schema(description = "Additional metadata for validation")
    private Map<String, Object> metadata;

    /**
     * Check if terminology validation is enabled
     */
    public boolean isTerminologyValidationEnabled() {
        return includeTerminologyValidation != null && includeTerminologyValidation;
    }

    /**
     * Check if profile validation is enabled
     */
    public boolean isProfileValidationEnabled() {
        return includeProfileValidation != null && includeProfileValidation;
    }

    /**
     * Check if structure validation is enabled
     */
    public boolean isStructureValidationEnabled() {
        return includeStructureValidation != null && includeStructureValidation;
    }

    /**
     * Check if strict mode is enabled
     */
    public boolean isStrictModeEnabled() {
        return strictMode != null && strictMode;
    }

    /**
     * Check if custom validation rules are provided
     */
    public boolean hasCustomValidationRules() {
        return customValidationRules != null && !customValidationRules.isEmpty();
    }

    /**
     * Check if profiles are specified
     */
    public boolean hasProfiles() {
        return profiles != null && !profiles.isEmpty();
    }

    /**
     * Get validation summary
     */
    public String getValidationSummary() {
        return String.format("FHIR %s validation for %s resource (strict: %s)",
                           fhirVersion, resourceType, isStrictModeEnabled());
    }

    /**
     * Get enabled validation types
     */
    public List<String> getEnabledValidationTypes() {
        List<String> types = new java.util.ArrayList<>();

        if (isStructureValidationEnabled()) {
            types.add("STRUCTURE");
        }

        if (isTerminologyValidationEnabled()) {
            types.add("TERMINOLOGY");
        }

        if (isProfileValidationEnabled()) {
            types.add("PROFILE");
        }

        if (hasCustomValidationRules()) {
            types.add("CUSTOM");
        }

        return types;
    }

    /**
     * Validate request parameters
     */
    public boolean isValid() {
        return resourceType != null && !resourceType.trim().isEmpty() &&
               resourceData != null && !resourceData.isEmpty() &&
               fhirVersion != null && !fhirVersion.trim().isEmpty();
    }

    /**
     * Get validation configuration summary
     */
    public String getConfigurationSummary() {
        return String.format("FHIR %s validation: %s (profiles: %d, strict: %s)",
                           fhirVersion,
                           String.join(", ", getEnabledValidationTypes()),
                           hasProfiles() ? profiles.size() : 0,
                           isStrictModeEnabled());
    }
}