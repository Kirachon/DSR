package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for GDPR validation results
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Data
@Builder
@Schema(description = "GDPR validation result")
public class GDPRValidationResult {

    @Schema(description = "Whether the validation passed", example = "true")
    private boolean valid;

    @Schema(description = "Type of GDPR validation performed", example = "LAWFUL_BASIS")
    private String validationType;

    @Schema(description = "Lawful basis for processing", example = "CONSENT")
    private String lawfulBasis;

    @Schema(description = "Processing purpose", example = "Customer service and support")
    private String processingPurpose;

    @Schema(description = "Data subject rights implementation details")
    private Map<String, Object> rightsImplementation;

    @Schema(description = "Data retention policy details")
    private Map<String, Object> retentionPolicy;

    @Schema(description = "Consent management details")
    private Map<String, Object> consentData;

    @Schema(description = "Data categories being processed")
    private List<String> dataCategories;

    @Schema(description = "Special category data indicators")
    private List<String> specialCategoryData;

    @Schema(description = "Third party data sharing details")
    private Map<String, Object> thirdPartySharing;

    @Schema(description = "International data transfers")
    private Map<String, Object> internationalTransfers;

    @Schema(description = "Validation errors found")
    private List<String> errors;

    @Schema(description = "Validation warnings found")
    private List<String> warnings;

    @Schema(description = "Compliance recommendations")
    private List<String> recommendations;

    @Schema(description = "Validation score (0-100)", example = "85.5")
    private Double validationScore;

    @Schema(description = "Compliance level", example = "MOSTLY_COMPLIANT")
    private String complianceLevel;

    @Schema(description = "Risk assessment level", example = "MEDIUM")
    private String riskLevel;

    @Schema(description = "Additional validation details")
    private String details;

    @Schema(description = "When the validation was performed")
    private LocalDateTime validatedAt;

    @Schema(description = "Validation duration in milliseconds", example = "300")
    private Long validationDurationMs;

    @Schema(description = "Validator used", example = "GDPR_COMPLIANCE_SERVICE")
    private String validator;

    @Schema(description = "Validation context or reference", example = "DATA_PROCESSING_ASSESSMENT")
    private String validationContext;

    @Schema(description = "GDPR article references")
    private List<String> gdprArticleReferences;

    @Schema(description = "Data protection impact assessment required", example = "false")
    private Boolean dpiaRequired;

    @Schema(description = "Data protection officer consultation required", example = "false")
    private Boolean dpoConsultationRequired;

    @Schema(description = "Supervisory authority notification required", example = "false")
    private Boolean supervisoryAuthorityNotificationRequired;

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
            return String.format("GDPR %s validation passed", validationType);
        } else {
            return String.format("GDPR %s validation failed - %d errors, %d warnings", 
                               validationType,
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

    /**
     * Check if this involves special category data
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
     * Get compliance level based on validation results
     */
    public String getComplianceLevel() {
        if (complianceLevel != null) {
            return complianceLevel;
        }
        
        if (!valid) {
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
     * Get risk level based on validation results
     */
    public String getRiskLevel() {
        if (riskLevel != null) {
            return riskLevel;
        }
        
        if (!valid || hasSpecialCategoryData() || hasInternationalTransfers()) {
            return "HIGH";
        }
        
        if (hasWarnings() || hasThirdPartySharing()) {
            return "MEDIUM";
        }
        
        return "LOW";
    }

    /**
     * Check if this is a lawful basis validation
     */
    public boolean isLawfulBasisValidation() {
        return "LAWFUL_BASIS".equals(validationType);
    }

    /**
     * Check if this is a data subject rights validation
     */
    public boolean isDataSubjectRightsValidation() {
        return "DATA_SUBJECT_RIGHTS".equals(validationType);
    }

    /**
     * Check if this is a data retention validation
     */
    public boolean isDataRetentionValidation() {
        return "DATA_RETENTION".equals(validationType);
    }

    /**
     * Check if this is a consent management validation
     */
    public boolean isConsentManagementValidation() {
        return "CONSENT_MANAGEMENT".equals(validationType);
    }

    /**
     * Check if DPIA is required based on validation results
     */
    public boolean isDpiaRequired() {
        if (dpiaRequired != null) {
            return dpiaRequired;
        }
        
        // DPIA required for high-risk processing
        return hasSpecialCategoryData() || 
               hasInternationalTransfers() || 
               "HIGH".equals(getRiskLevel()) ||
               (validationType != null && validationType.contains("AUTOMATED"));
    }

    /**
     * Check if DPO consultation is required
     */
    public boolean isDpoConsultationRequired() {
        if (dpoConsultationRequired != null) {
            return dpoConsultationRequired;
        }
        
        return isDpiaRequired() || hasSpecialCategoryData();
    }

    /**
     * Check if supervisory authority notification is required
     */
    public boolean isSupervisoryAuthorityNotificationRequired() {
        if (supervisoryAuthorityNotificationRequired != null) {
            return supervisoryAuthorityNotificationRequired;
        }
        
        return !valid && "HIGH".equals(getRiskLevel());
    }

    /**
     * Get recommended next actions
     */
    public List<String> getRecommendedActions() {
        List<String> actions = new java.util.ArrayList<>();
        
        if (hasErrors()) {
            actions.add("Address compliance errors immediately");
        }
        
        if (isDpiaRequired()) {
            actions.add("Conduct Data Protection Impact Assessment (DPIA)");
        }
        
        if (isDpoConsultationRequired()) {
            actions.add("Consult with Data Protection Officer");
        }
        
        if (isSupervisoryAuthorityNotificationRequired()) {
            actions.add("Consider notifying supervisory authority");
        }
        
        if (hasWarnings()) {
            actions.add("Review and address compliance warnings");
        }
        
        if (hasSpecialCategoryData()) {
            actions.add("Ensure additional safeguards for special category data");
        }
        
        if (hasInternationalTransfers()) {
            actions.add("Verify adequacy decisions or implement appropriate safeguards");
        }
        
        return actions;
    }
}
