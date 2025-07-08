package ph.gov.dsr.interoperability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.interoperability.dto.GDPRValidationResult;
import ph.gov.dsr.interoperability.entity.ComplianceRecord;
import ph.gov.dsr.interoperability.repository.ComplianceRecordRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for GDPR (General Data Protection Regulation) compliance validation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GDPRComplianceService {

    private final ComplianceRecordRepository complianceRepository;

    @Value("${dsr.gdpr.validation.enabled:true}")
    private boolean validationEnabled;

    @Value("${dsr.gdpr.strict-mode:true}")
    private boolean strictMode;

    @Value("${dsr.gdpr.data-retention-days:2555}") // 7 years default
    private int dataRetentionDays;

    /**
     * Validate lawful basis for data processing
     */
    @Transactional
    public GDPRValidationResult validateLawfulBasis(String lawfulBasis, String processingPurpose) {
        log.info("Validating GDPR lawful basis: {} for purpose: {}", lawfulBasis, processingPurpose);
        
        try {
            if (!validationEnabled) {
                return createValidationResult(true, "GDPR validation is disabled", "LAWFUL_BASIS");
            }

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate lawful basis is specified
            validateLawfulBasisSpecified(lawfulBasis, errors);

            // Validate lawful basis is valid
            validateLawfulBasisValid(lawfulBasis, errors, warnings);

            // Validate lawful basis matches processing purpose
            validateLawfulBasisPurposeAlignment(lawfulBasis, processingPurpose, warnings);

            // Validate special category data handling
            validateSpecialCategoryData(processingPurpose, lawfulBasis, errors, warnings);

            boolean isValid = errors.isEmpty() || (!strictMode && errors.size() <= 1);

            // Record compliance check
            recordComplianceCheck("GDPR_LAWFUL_BASIS", processingPurpose, isValid,
                                String.format("Lawful basis: %s, Errors: %d, Warnings: %d", 
                                            lawfulBasis, errors.size(), warnings.size()));

            return GDPRValidationResult.builder()
                    .valid(isValid)
                    .validationType("LAWFUL_BASIS")
                    .lawfulBasis(lawfulBasis)
                    .processingPurpose(processingPurpose)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR lawful basis validation failed", e);
            return createValidationResult(false, "Lawful basis validation failed: " + e.getMessage(), "LAWFUL_BASIS");
        }
    }

    /**
     * Validate data subject rights implementation
     */
    @Transactional
    public GDPRValidationResult validateDataSubjectRights(Map<String, Object> rightsImplementation) {
        log.info("Validating GDPR data subject rights implementation");
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate right to access
            validateRightToAccess(rightsImplementation, errors, warnings);

            // Validate right to rectification
            validateRightToRectification(rightsImplementation, errors, warnings);

            // Validate right to erasure
            validateRightToErasure(rightsImplementation, errors, warnings);

            // Validate right to restrict processing
            validateRightToRestrictProcessing(rightsImplementation, errors, warnings);

            // Validate right to data portability
            validateRightToDataPortability(rightsImplementation, errors, warnings);

            // Validate right to object
            validateRightToObject(rightsImplementation, errors, warnings);

            // Validate automated decision-making rights
            validateAutomatedDecisionMakingRights(rightsImplementation, warnings);

            boolean isValid = errors.isEmpty();

            // Record compliance check
            recordComplianceCheck("GDPR_DATA_SUBJECT_RIGHTS", "RIGHTS_IMPLEMENTATION", isValid,
                                String.format("Rights validation - Errors: %d, Warnings: %d", 
                                            errors.size(), warnings.size()));

            return GDPRValidationResult.builder()
                    .valid(isValid)
                    .validationType("DATA_SUBJECT_RIGHTS")
                    .rightsImplementation(rightsImplementation)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR data subject rights validation failed", e);
            return createValidationResult(false, "Data subject rights validation failed: " + e.getMessage(), "DATA_SUBJECT_RIGHTS");
        }
    }

    /**
     * Validate data retention policies
     */
    @Transactional
    public GDPRValidationResult validateDataRetention(Map<String, Object> retentionPolicy) {
        log.info("Validating GDPR data retention policies");
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate retention periods are defined
            validateRetentionPeriodsDefinition(retentionPolicy, errors);

            // Validate retention periods are reasonable
            validateRetentionPeriodsReasonable(retentionPolicy, warnings);

            // Validate automatic deletion mechanisms
            validateAutomaticDeletion(retentionPolicy, errors, warnings);

            // Validate retention justification
            validateRetentionJustification(retentionPolicy, warnings);

            // Validate data minimization
            validateDataMinimization(retentionPolicy, warnings);

            boolean isValid = errors.isEmpty();

            // Record compliance check
            recordComplianceCheck("GDPR_DATA_RETENTION", "RETENTION_POLICY", isValid,
                                String.format("Retention validation - Errors: %d, Warnings: %d", 
                                            errors.size(), warnings.size()));

            return GDPRValidationResult.builder()
                    .valid(isValid)
                    .validationType("DATA_RETENTION")
                    .retentionPolicy(retentionPolicy)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR data retention validation failed", e);
            return createValidationResult(false, "Data retention validation failed: " + e.getMessage(), "DATA_RETENTION");
        }
    }

    /**
     * Validate consent management
     */
    @Transactional
    public GDPRValidationResult validateConsentManagement(Map<String, Object> consentData) {
        log.info("Validating GDPR consent management");
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate consent is freely given
            validateConsentFreelyGiven(consentData, errors, warnings);

            // Validate consent is specific
            validateConsentSpecific(consentData, errors, warnings);

            // Validate consent is informed
            validateConsentInformed(consentData, errors, warnings);

            // Validate consent is unambiguous
            validateConsentUnambiguous(consentData, errors, warnings);

            // Validate consent withdrawal mechanism
            validateConsentWithdrawal(consentData, errors, warnings);

            // Validate consent records
            validateConsentRecords(consentData, warnings);

            boolean isValid = errors.isEmpty();

            // Record compliance check
            recordComplianceCheck("GDPR_CONSENT", "CONSENT_MANAGEMENT", isValid,
                                String.format("Consent validation - Errors: %d, Warnings: %d", 
                                            errors.size(), warnings.size()));

            return GDPRValidationResult.builder()
                    .valid(isValid)
                    .validationType("CONSENT_MANAGEMENT")
                    .consentData(consentData)
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR consent validation failed", e);
            return createValidationResult(false, "Consent validation failed: " + e.getMessage(), "CONSENT_MANAGEMENT");
        }
    }

    /**
     * Validate data protection measures
     */
    @Transactional
    public GDPRValidationResult validateDataProtectionMeasures(Map<String, Object> systemData, Map<String, Object> protectionMeasures) {
        log.info("Validating GDPR data protection measures");

        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate encryption measures
            if (!protectionMeasures.containsKey("encryption") ||
                !Boolean.TRUE.equals(protectionMeasures.get("encryption"))) {
                errors.add("Data encryption is required for GDPR compliance");
            }

            // Validate access controls
            if (!protectionMeasures.containsKey("accessControls")) {
                errors.add("Access controls must be implemented");
            }

            // Validate audit logging
            if (!protectionMeasures.containsKey("auditLogging") ||
                !Boolean.TRUE.equals(protectionMeasures.get("auditLogging"))) {
                warnings.add("Audit logging is recommended for GDPR compliance");
            }

            // Validate data minimization
            if (!protectionMeasures.containsKey("dataMinimization")) {
                warnings.add("Data minimization principles should be implemented");
            }

            return GDPRValidationResult.builder()
                    .valid(errors.isEmpty())
                    .validationType("DATA_PROTECTION")
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR data protection validation failed", e);
            return createValidationResult(false, "Data protection validation failed: " + e.getMessage(), "DATA_PROTECTION");
        }
    }

    /**
     * Validate retention policies
     */
    @Transactional
    public GDPRValidationResult validateRetentionPolicies(Map<String, Object> retentionData) {
        log.info("Validating GDPR retention policies");

        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate retention periods are defined
            if (!retentionData.containsKey("retentionPeriods")) {
                errors.add("Data retention periods must be defined");
            }

            // Validate automatic deletion
            if (!retentionData.containsKey("automaticDeletion")) {
                warnings.add("Automatic deletion mechanisms are recommended");
            }

            // Validate retention justification
            if (!retentionData.containsKey("retentionJustification")) {
                warnings.add("Retention periods should be justified based on legal basis");
            }

            return GDPRValidationResult.builder()
                    .valid(errors.isEmpty())
                    .validationType("RETENTION_POLICIES")
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR retention policies validation failed", e);
            return createValidationResult(false, "Retention policies validation failed: " + e.getMessage(), "RETENTION_POLICIES");
        }
    }

    /**
     * Validate international transfers
     */
    @Transactional
    public GDPRValidationResult validateInternationalTransfers(Map<String, Object> transferData) {
        log.info("Validating GDPR international transfers");

        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            // Validate adequacy decision or safeguards
            if (!transferData.containsKey("adequacyDecision") && !transferData.containsKey("safeguards")) {
                errors.add("International transfers require adequacy decision or appropriate safeguards");
            }

            // Validate transfer documentation
            if (!transferData.containsKey("transferDocumentation")) {
                warnings.add("Transfer documentation should be maintained");
            }

            // Validate data subject rights
            if (!transferData.containsKey("dataSubjectRights")) {
                warnings.add("Data subject rights must be preserved in international transfers");
            }

            return GDPRValidationResult.builder()
                    .valid(errors.isEmpty())
                    .validationType("INTERNATIONAL_TRANSFERS")
                    .errors(errors)
                    .warnings(warnings)
                    .validatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("GDPR international transfers validation failed", e);
            return createValidationResult(false, "International transfers validation failed: " + e.getMessage(), "INTERNATIONAL_TRANSFERS");
        }
    }

    /**
     * Update GDPR compliance settings
     */
    public void updateSettings(Map<String, Object> gdprSettings) {
        log.info("Updating GDPR compliance settings: {}", gdprSettings);

        if (gdprSettings.containsKey("validationEnabled")) {
            this.validationEnabled = (Boolean) gdprSettings.get("validationEnabled");
        }
        
        if (gdprSettings.containsKey("strictMode")) {
            this.strictMode = (Boolean) gdprSettings.get("strictMode");
        }
        
        if (gdprSettings.containsKey("dataRetentionDays")) {
            this.dataRetentionDays = (Integer) gdprSettings.get("dataRetentionDays");
        }
        
        log.info("GDPR settings updated - Validation: {}, Strict: {}, Retention: {} days", 
                validationEnabled, strictMode, dataRetentionDays);
    }

    /**
     * Get GDPR compliance statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getComplianceStatistics() {
        log.info("Getting GDPR compliance statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Get overall GDPR compliance statistics
        Object[] gdprStats = complianceRepository.getComplianceStatistics("GDPR");
        if (gdprStats != null && gdprStats.length >= 4) {
            stats.put("totalChecks", gdprStats[0]);
            stats.put("compliantChecks", gdprStats[1]);
            stats.put("nonCompliantChecks", gdprStats[2]);
            stats.put("averageScore", gdprStats[3]);
        }
        
        // Get lawful basis validation statistics
        Object[] lawfulBasisStats = complianceRepository.getComplianceStatistics("GDPR_LAWFUL_BASIS");
        stats.put("lawfulBasisValidation", lawfulBasisStats);
        
        // Get data subject rights validation statistics
        Object[] rightsStats = complianceRepository.getComplianceStatistics("GDPR_DATA_SUBJECT_RIGHTS");
        stats.put("dataSubjectRightsValidation", rightsStats);
        
        // Get data retention validation statistics
        Object[] retentionStats = complianceRepository.getComplianceStatistics("GDPR_DATA_RETENTION");
        stats.put("dataRetentionValidation", retentionStats);
        
        // Get consent validation statistics
        Object[] consentStats = complianceRepository.getComplianceStatistics("GDPR_CONSENT");
        stats.put("consentValidation", consentStats);
        
        stats.put("lastUpdated", LocalDateTime.now());
        
        return stats;
    }

    // Private helper methods
    
    private void validateLawfulBasisSpecified(String lawfulBasis, List<String> errors) {
        if (lawfulBasis == null || lawfulBasis.trim().isEmpty()) {
            errors.add("Lawful basis for processing must be specified");
        }
    }

    private void validateLawfulBasisValid(String lawfulBasis, List<String> errors, List<String> warnings) {
        if (lawfulBasis == null) return;
        
        Set<String> validLawfulBases = Set.of(
            "CONSENT", "CONTRACT", "LEGAL_OBLIGATION", "VITAL_INTERESTS", 
            "PUBLIC_TASK", "LEGITIMATE_INTERESTS"
        );
        
        if (!validLawfulBases.contains(lawfulBasis.toUpperCase())) {
            errors.add("Invalid lawful basis specified: " + lawfulBasis);
        }
        
        if ("LEGITIMATE_INTERESTS".equals(lawfulBasis.toUpperCase())) {
            warnings.add("Legitimate interests basis requires balancing test documentation");
        }
    }

    private void validateLawfulBasisPurposeAlignment(String lawfulBasis, String processingPurpose, List<String> warnings) {
        if (lawfulBasis == null || processingPurpose == null) return;
        
        // Check alignment between lawful basis and purpose
        if ("CONSENT".equals(lawfulBasis.toUpperCase()) && processingPurpose.toLowerCase().contains("legal")) {
            warnings.add("Consent may not be appropriate for legal obligations");
        }
        
        if ("CONTRACT".equals(lawfulBasis.toUpperCase()) && !processingPurpose.toLowerCase().contains("contract")) {
            warnings.add("Contract basis should align with contractual purposes");
        }
    }

    private void validateSpecialCategoryData(String processingPurpose, String lawfulBasis, 
                                           List<String> errors, List<String> warnings) {
        if (processingPurpose == null) return;
        
        // Check if processing involves special category data
        List<String> specialCategories = List.of("health", "biometric", "genetic", "racial", "ethnic", 
                                                "political", "religious", "sexual", "criminal");
        
        boolean isSpecialCategory = specialCategories.stream()
                .anyMatch(category -> processingPurpose.toLowerCase().contains(category));
        
        if (isSpecialCategory) {
            warnings.add("Special category data processing requires additional lawful basis under Article 9");
            
            if ("CONSENT".equals(lawfulBasis) || "LEGITIMATE_INTERESTS".equals(lawfulBasis)) {
                warnings.add("Special category data requires explicit consent or other Article 9 condition");
            }
        }
    }

    private void validateRightToAccess(Map<String, Object> rightsImplementation, 
                                     List<String> errors, List<String> warnings) {
        if (!rightsImplementation.containsKey("rightToAccess")) {
            errors.add("Right to access implementation not found");
            return;
        }
        
        Map<?, ?> accessImpl = (Map<?, ?>) rightsImplementation.get("rightToAccess");
        
        if (!Boolean.TRUE.equals(accessImpl.get("implemented"))) {
            errors.add("Right to access not implemented");
        }
        
        if (!accessImpl.containsKey("responseTimeLimit")) {
            warnings.add("Response time limit for access requests not specified");
        }
        
        Integer responseTime = (Integer) accessImpl.get("responseTimeLimit");
        if (responseTime != null && responseTime > 30) {
            warnings.add("Response time for access requests exceeds recommended 30 days");
        }
    }

    private void validateRightToRectification(Map<String, Object> rightsImplementation, 
                                            List<String> errors, List<String> warnings) {
        if (!rightsImplementation.containsKey("rightToRectification")) {
            errors.add("Right to rectification implementation not found");
            return;
        }
        
        Map<?, ?> rectificationImpl = (Map<?, ?>) rightsImplementation.get("rightToRectification");
        
        if (!Boolean.TRUE.equals(rectificationImpl.get("implemented"))) {
            errors.add("Right to rectification not implemented");
        }
        
        if (!Boolean.TRUE.equals(rectificationImpl.get("notifiesThirdParties"))) {
            warnings.add("Third party notification for rectification not implemented");
        }
    }

    private void validateRightToErasure(Map<String, Object> rightsImplementation, 
                                      List<String> errors, List<String> warnings) {
        if (!rightsImplementation.containsKey("rightToErasure")) {
            errors.add("Right to erasure implementation not found");
            return;
        }
        
        Map<?, ?> erasureImpl = (Map<?, ?>) rightsImplementation.get("rightToErasure");
        
        if (!Boolean.TRUE.equals(erasureImpl.get("implemented"))) {
            errors.add("Right to erasure not implemented");
        }
        
        if (!erasureImpl.containsKey("erasureExceptions")) {
            warnings.add("Erasure exceptions not documented");
        }
    }

    private void validateRightToRestrictProcessing(Map<String, Object> rightsImplementation, 
                                                 List<String> errors, List<String> warnings) {
        if (!rightsImplementation.containsKey("rightToRestrictProcessing")) {
            errors.add("Right to restrict processing implementation not found");
            return;
        }
        
        Map<?, ?> restrictImpl = (Map<?, ?>) rightsImplementation.get("rightToRestrictProcessing");
        
        if (!Boolean.TRUE.equals(restrictImpl.get("implemented"))) {
            errors.add("Right to restrict processing not implemented");
        }
    }

    private void validateRightToDataPortability(Map<String, Object> rightsImplementation, 
                                              List<String> errors, List<String> warnings) {
        if (!rightsImplementation.containsKey("rightToDataPortability")) {
            warnings.add("Right to data portability implementation not found");
            return;
        }
        
        Map<?, ?> portabilityImpl = (Map<?, ?>) rightsImplementation.get("rightToDataPortability");
        
        if (!Boolean.TRUE.equals(portabilityImpl.get("implemented"))) {
            warnings.add("Right to data portability not implemented");
        }
        
        if (!portabilityImpl.containsKey("supportedFormats")) {
            warnings.add("Supported data formats for portability not specified");
        }
    }

    private void validateRightToObject(Map<String, Object> rightsImplementation, 
                                     List<String> errors, List<String> warnings) {
        if (!rightsImplementation.containsKey("rightToObject")) {
            warnings.add("Right to object implementation not found");
            return;
        }
        
        Map<?, ?> objectImpl = (Map<?, ?>) rightsImplementation.get("rightToObject");
        
        if (!Boolean.TRUE.equals(objectImpl.get("implemented"))) {
            warnings.add("Right to object not implemented");
        }
    }

    private void validateAutomatedDecisionMakingRights(Map<String, Object> rightsImplementation, 
                                                     List<String> warnings) {
        if (!rightsImplementation.containsKey("automatedDecisionMaking")) {
            warnings.add("Automated decision-making rights not addressed");
            return;
        }
        
        Map<?, ?> automatedImpl = (Map<?, ?>) rightsImplementation.get("automatedDecisionMaking");
        
        if (Boolean.TRUE.equals(automatedImpl.get("hasAutomatedDecisionMaking"))) {
            if (!Boolean.TRUE.equals(automatedImpl.get("humanReviewAvailable"))) {
                warnings.add("Human review not available for automated decision-making");
            }
            
            if (!Boolean.TRUE.equals(automatedImpl.get("logicExplained"))) {
                warnings.add("Logic of automated decision-making not explained to data subjects");
            }
        }
    }

    private void validateRetentionPeriodsDefinition(Map<String, Object> retentionPolicy, List<String> errors) {
        if (!retentionPolicy.containsKey("retentionPeriods")) {
            errors.add("Data retention periods not defined");
            return;
        }
        
        Map<?, ?> periods = (Map<?, ?>) retentionPolicy.get("retentionPeriods");
        if (periods.isEmpty()) {
            errors.add("No retention periods specified");
        }
    }

    private void validateRetentionPeriodsReasonable(Map<String, Object> retentionPolicy, List<String> warnings) {
        if (!retentionPolicy.containsKey("retentionPeriods")) return;
        
        Map<?, ?> periods = (Map<?, ?>) retentionPolicy.get("retentionPeriods");
        
        for (Map.Entry<?, ?> entry : periods.entrySet()) {
            String dataType = (String) entry.getKey();
            Integer retentionDays = (Integer) entry.getValue();
            
            if (retentionDays != null && retentionDays > this.dataRetentionDays) {
                warnings.add(String.format("Retention period for %s (%d days) exceeds recommended maximum", 
                                          dataType, retentionDays));
            }
            
            if (retentionDays != null && retentionDays > 3650) { // 10 years
                warnings.add(String.format("Very long retention period for %s (%d days)", dataType, retentionDays));
            }
        }
    }

    private void validateAutomaticDeletion(Map<String, Object> retentionPolicy, 
                                         List<String> errors, List<String> warnings) {
        if (!Boolean.TRUE.equals(retentionPolicy.get("automaticDeletionEnabled"))) {
            errors.add("Automatic deletion mechanism not enabled");
        }
        
        if (!retentionPolicy.containsKey("deletionSchedule")) {
            warnings.add("Deletion schedule not specified");
        }
    }

    private void validateRetentionJustification(Map<String, Object> retentionPolicy, List<String> warnings) {
        if (!retentionPolicy.containsKey("retentionJustification")) {
            warnings.add("Retention period justification not documented");
        }
    }

    private void validateDataMinimization(Map<String, Object> retentionPolicy, List<String> warnings) {
        if (!Boolean.TRUE.equals(retentionPolicy.get("dataMinimizationApplied"))) {
            warnings.add("Data minimization principles not applied to retention");
        }
    }

    private void validateConsentFreelyGiven(Map<String, Object> consentData, 
                                          List<String> errors, List<String> warnings) {
        if (!Boolean.TRUE.equals(consentData.get("freelyGiven"))) {
            errors.add("Consent must be freely given");
        }
        
        if (Boolean.TRUE.equals(consentData.get("bundledWithService"))) {
            warnings.add("Consent bundled with service may not be freely given");
        }
    }

    private void validateConsentSpecific(Map<String, Object> consentData, 
                                       List<String> errors, List<String> warnings) {
        if (!Boolean.TRUE.equals(consentData.get("specific"))) {
            errors.add("Consent must be specific to processing purposes");
        }
        
        if (!consentData.containsKey("specificPurposes")) {
            warnings.add("Specific purposes for consent not documented");
        }
    }

    private void validateConsentInformed(Map<String, Object> consentData, 
                                       List<String> errors, List<String> warnings) {
        if (!Boolean.TRUE.equals(consentData.get("informed"))) {
            errors.add("Consent must be informed");
        }
        
        if (!consentData.containsKey("informationProvided")) {
            warnings.add("Information provided to data subject not documented");
        }
    }

    private void validateConsentUnambiguous(Map<String, Object> consentData, 
                                          List<String> errors, List<String> warnings) {
        if (!Boolean.TRUE.equals(consentData.get("unambiguous"))) {
            errors.add("Consent must be unambiguous");
        }
        
        if (Boolean.TRUE.equals(consentData.get("preTickedBoxes"))) {
            errors.add("Pre-ticked boxes do not constitute valid consent");
        }
    }

    private void validateConsentWithdrawal(Map<String, Object> consentData, 
                                         List<String> errors, List<String> warnings) {
        if (!Boolean.TRUE.equals(consentData.get("withdrawalMechanismAvailable"))) {
            errors.add("Consent withdrawal mechanism must be available");
        }
        
        if (!Boolean.TRUE.equals(consentData.get("withdrawalAsEasyAsGiving"))) {
            errors.add("Consent withdrawal must be as easy as giving consent");
        }
    }

    private void validateConsentRecords(Map<String, Object> consentData, List<String> warnings) {
        if (!Boolean.TRUE.equals(consentData.get("consentRecordsKept"))) {
            warnings.add("Consent records should be maintained");
        }
        
        if (!consentData.containsKey("consentTimestamp")) {
            warnings.add("Consent timestamp not recorded");
        }
        
        if (!consentData.containsKey("consentVersion")) {
            warnings.add("Consent version not tracked");
        }
    }

    private GDPRValidationResult createValidationResult(boolean isValid, String message, String validationType) {
        return GDPRValidationResult.builder()
                .valid(isValid)
                .validationType(validationType)
                .errors(isValid ? new ArrayList<>() : List.of(message))
                .warnings(new ArrayList<>())
                .validatedAt(LocalDateTime.now())
                .build();
    }

    private void recordComplianceCheck(String standard, String entity, boolean compliant, String details) {
        try {
            ComplianceRecord record = new ComplianceRecord(standard, entity, compliant);
            record.setDetails(details);
            record.setCategory("GDPR_VALIDATION");
            record.setSeverity(compliant ? "LOW" : "HIGH");
            record.setCheckedBy("GDPR_COMPLIANCE_SERVICE");
            record.setExpiryFromStandard();
            
            complianceRepository.save(record);
            
        } catch (Exception e) {
            log.error("Failed to record GDPR compliance check", e);
        }
    }
}
