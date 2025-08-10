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
 * DTO for GDPR compliance results
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "GDPR compliance result")
public class GDPRComplianceResult {

    @Schema(description = "Whether the processing is GDPR compliant", example = "true")
    private boolean compliant;

    @Schema(description = "Processing purpose", example = "Customer service and support")
    private String processingPurpose;

    @Schema(description = "Lawful basis for processing", example = "CONSENT")
    private String lawfulBasis;

    @Schema(description = "List of validation results from different checks")
    private List<GDPRValidationResult> validationResults;

    @Schema(description = "Overall compliance score (0-100)", example = "88.5")
    private Double complianceScore;

    @Schema(description = "Compliance level", example = "MOSTLY_COMPLIANT")
    private String complianceLevel;

    @Schema(description = "Risk assessment level", example = "MEDIUM")
    private String riskLevel;

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

    @Schema(description = "Validation duration in milliseconds", example = "450")
    private Long validationDurationMs;

    @Schema(description = "Validator used", example = "GDPR_COMPLIANCE_SERVICE")
    private String validator;

    @Schema(description = "Validation context or reference", example = "DATA_PROCESSING_ASSESSMENT")
    private String validationContext;

    @Schema(description = "Data categories being processed")
    private List<String> dataCategories;

    @Schema(description = "Special category data indicators")
    private List<String> specialCategoryData;

    @Schema(description = "Data subject rights implementation details")
    private Map<String, Object> rightsImplementation;

    @Schema(description = "Data retention policy details")
    private Map<String, Object> retentionPolicy;

    @Schema(description = "Consent management details")
    private Map<String, Object> consentData;

    @Schema(description = "Third party data sharing details")
    private Map<String, Object> thirdPartySharing;

    @Schema(description = "International data transfers")
    private Map<String, Object> internationalTransfers;

    @Schema(description = "GDPR article references")
    private List<String> gdprArticleReferences;

    @Schema(description = "Data protection impact assessment required", example = "false")
    private Boolean dpiaRequired;

    @Schema(description = "Data protection officer consultation required", example = "false")
    private Boolean dpoConsultationRequired;

    @Schema(description = "Supervisory authority notification required", example = "false")
    private Boolean supervisoryAuthorityNotificationRequired;

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
            return String.format("GDPR compliance passed for %s processing", processingPurpose);
        } else {
            return String.format("GDPR compliance failed for %s processing - %d errors, %d warnings",
                               processingPurpose,
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

        for (GDPRValidationResult result : validationResults) {
            if (result.getValidationScore() != null) {
                totalScore += result.getValidationScore();
                validResults++;
            }
        }

        return validResults > 0 ? totalScore / validResults : (compliant ? 100.0 : 0.0);
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
     * Get validation result by type
     */
    public GDPRValidationResult getValidationResultByType(String validationType) {
        if (validationResults == null) {
            return null;
        }

        return validationResults.stream()
                .filter(result -> validationType.equals(result.getValidationType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if validation passed for specific type
     */
    public boolean isValidForType(String validationType) {
        GDPRValidationResult result = getValidationResultByType(validationType);
        return result != null && result.isValid();
    }

    /**
     * Get risk level based on validation results
     */
    public String getRiskLevel() {
        if (riskLevel != null) {
            return riskLevel;
        }

        if (!compliant || hasSpecialCategoryData() || hasInternationalTransfers()) {
            return "HIGH";
        }

        if (hasWarnings() || hasThirdPartySharing()) {
            return "MEDIUM";
        }

        return "LOW";
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
               (processingPurpose != null && processingPurpose.toLowerCase().contains("automated"));
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

        return !compliant && "HIGH".equals(getRiskLevel());
    }

    /**
     * Get recommended next actions
     */
    public List<String> getRecommendedActions() {
        List<String> actions = new java.util.ArrayList<>();

        if (hasErrors()) {
            actions.add("Address GDPR compliance errors immediately");
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
            actions.add("Review and address GDPR compliance warnings");
        }

        if (hasSpecialCategoryData()) {
            actions.add("Ensure additional safeguards for special category data");
        }

        if (hasInternationalTransfers()) {
            actions.add("Verify adequacy decisions or implement appropriate safeguards");
        }

        if (recommendations != null) {
            actions.addAll(recommendations);
        }

        return actions;
    }

    /**
     * Get data processing summary
     */
    public Map<String, Object> getDataProcessingSummary() {
        Map<String, Object> summary = new java.util.HashMap<>();

        summary.put("processingPurpose", processingPurpose);
        summary.put("lawfulBasis", lawfulBasis);
        summary.put("dataCategories", dataCategories != null ? dataCategories.size() : 0);
        summary.put("specialCategoryData", hasSpecialCategoryData());
        summary.put("internationalTransfers", hasInternationalTransfers());
        summary.put("thirdPartySharing", hasThirdPartySharing());
        summary.put("riskLevel", getRiskLevel());
        summary.put("dpiaRequired", isDpiaRequired());

        return summary;
    }
}