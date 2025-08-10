package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for GDPR validation requests
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "GDPR validation request")
public class GDPRValidationRequest {

    @NotBlank(message = "Processing purpose is required")
    @Schema(description = "Purpose of data processing", example = "Customer service and support", required = true)
    private String processingPurpose;

    @NotBlank(message = "Lawful basis is required")
    @Schema(description = "Lawful basis for processing", example = "CONSENT", required = true)
    private String lawfulBasis;

    @Schema(description = "Data subject rights implementation details")
    private Map<String, Object> dataSubjectRights;

    @Schema(description = "Technical measures for data protection")
    private Map<String, Object> technicalMeasures;

    @Schema(description = "Organizational measures for data protection")
    private Map<String, Object> organizationalMeasures;

    @Schema(description = "Data retention policies")
    private Map<String, Object> retentionPolicies;

    @Schema(description = "International data transfers details")
    private Map<String, Object> internationalTransfers;

    @Schema(description = "Data categories being processed")
    private List<String> dataCategories;

    @Schema(description = "Special category data indicators")
    private List<String> specialCategoryData;

    @Schema(description = "Third party data sharing details")
    private Map<String, Object> thirdPartySharing;

    @Schema(description = "Consent management details")
    private Map<String, Object> consentData;

    @Schema(description = "Data controller information")
    private Map<String, Object> dataController;

    @Schema(description = "Data processor information")
    private Map<String, Object> dataProcessor;

    @Schema(description = "Validation context or reference", example = "DATA_PROCESSING_ASSESSMENT")
    private String validationContext;

    @Schema(description = "Strict validation mode", example = "true")
    @Builder.Default
    private Boolean strictMode = true;

    @Schema(description = "Include lawful basis validation", example = "true")
    @Builder.Default
    private Boolean includeLawfulBasisValidation = true;

    @Schema(description = "Include data subject rights validation", example = "true")
    @Builder.Default
    private Boolean includeDataSubjectRightsValidation = true;

    @Schema(description = "Include data protection measures validation", example = "true")
    @Builder.Default
    private Boolean includeDataProtectionValidation = true;

    @Schema(description = "Include retention policies validation", example = "true")
    @Builder.Default
    private Boolean includeRetentionValidation = true;

    @Schema(description = "Include international transfers validation", example = "true")
    @Builder.Default
    private Boolean includeTransferValidation = true;

    @Schema(description = "Custom validation rules")
    private Map<String, Object> customValidationRules;

    @Schema(description = "Validation timeout in milliseconds", example = "30000")
    @Builder.Default
    private Long timeoutMs = 30000L;

    @Schema(description = "Additional metadata for validation")
    private Map<String, Object> metadata;

    /**
     * Check if lawful basis validation is enabled
     */
    public boolean isLawfulBasisValidationEnabled() {
        return includeLawfulBasisValidation != null && includeLawfulBasisValidation;
    }

    /**
     * Check if data subject rights validation is enabled
     */
    public boolean isDataSubjectRightsValidationEnabled() {
        return includeDataSubjectRightsValidation != null && includeDataSubjectRightsValidation;
    }

    /**
     * Check if data protection validation is enabled
     */
    public boolean isDataProtectionValidationEnabled() {
        return includeDataProtectionValidation != null && includeDataProtectionValidation;
    }

    /**
     * Check if retention validation is enabled
     */
    public boolean isRetentionValidationEnabled() {
        return includeRetentionValidation != null && includeRetentionValidation;
    }

    /**
     * Check if transfer validation is enabled
     */
    public boolean isTransferValidationEnabled() {
        return includeTransferValidation != null && includeTransferValidation;
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
     * Check if special category data is involved
     */
    public boolean hasSpecialCategoryData() {
        return specialCategoryData != null && !specialCategoryData.isEmpty();
    }

    /**
     * Check if international transfers are involved
     */
    public boolean hasInternationalTransfers() {
        return internationalTransfers != null && !internationalTransfers.isEmpty();
    }

    /**
     * Check if third party sharing is involved
     */
    public boolean hasThirdPartySharing() {
        return thirdPartySharing != null && !thirdPartySharing.isEmpty();
    }

    /**
     * Get validation summary
     */
    public String getValidationSummary() {
        return String.format("GDPR validation for %s processing (basis: %s, strict: %s)",
                           processingPurpose, lawfulBasis, isStrictModeEnabled());
    }

    /**
     * Get enabled validation types
     */
    public List<String> getEnabledValidationTypes() {
        List<String> types = new java.util.ArrayList<>();

        if (isLawfulBasisValidationEnabled()) {
            types.add("LAWFUL_BASIS");
        }

        if (isDataSubjectRightsValidationEnabled()) {
            types.add("DATA_SUBJECT_RIGHTS");
        }

        if (isDataProtectionValidationEnabled()) {
            types.add("DATA_PROTECTION");
        }

        if (isRetentionValidationEnabled()) {
            types.add("RETENTION");
        }

        if (isTransferValidationEnabled()) {
            types.add("TRANSFER");
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
        return processingPurpose != null && !processingPurpose.trim().isEmpty() &&
               lawfulBasis != null && !lawfulBasis.trim().isEmpty();
    }

    /**
     * Get validation configuration summary
     */
    public String getConfigurationSummary() {
        return String.format("GDPR validation: %s (categories: %d, special: %d, strict: %s)",
                           String.join(", ", getEnabledValidationTypes()),
                           dataCategories != null ? dataCategories.size() : 0,
                           hasSpecialCategoryData() ? specialCategoryData.size() : 0,
                           isStrictModeEnabled());
    }

    /**
     * Get risk level based on data characteristics
     */
    public String getRiskLevel() {
        if (hasSpecialCategoryData() || hasInternationalTransfers()) {
            return "HIGH";
        }

        if (hasThirdPartySharing() || (dataCategories != null && dataCategories.size() > 5)) {
            return "MEDIUM";
        }

        return "LOW";
    }

    /**
     * Check if DPIA is likely required
     */
    public boolean isDpiaLikelyRequired() {
        return hasSpecialCategoryData() ||
               hasInternationalTransfers() ||
               "HIGH".equals(getRiskLevel()) ||
               (processingPurpose != null && processingPurpose.toLowerCase().contains("automated"));
    }
}