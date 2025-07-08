package ph.gov.dsr.compliance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compliance Framework Registry
 * Manages and provides access to various regulatory compliance frameworks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComplianceFrameworkRegistry {

    // Registry of compliance frameworks
    private final Map<String, ComplianceFramework> frameworks = new ConcurrentHashMap<>();
    private final Map<String, FrameworkMetadata> frameworkMetadata = new ConcurrentHashMap<>();

    /**
     * Initialize built-in compliance frameworks
     */
    @PostConstruct
    public void initializeFrameworks() {
        try {
            log.info("Initializing compliance frameworks");
            
            // Philippine Data Privacy Act (DPA) 2012
            registerDataPrivacyActFramework();
            
            // GDPR Compliance Framework
            registerGDPRFramework();
            
            // ISO 27001 Information Security Management
            registerISO27001Framework();
            
            // NIST Cybersecurity Framework
            registerNISTFramework();
            
            // Philippine Government ICT Standards
            registerPhilippineICTStandards();
            
            // Social Protection Standards
            registerSocialProtectionStandards();
            
            // Financial Services Compliance
            registerFinancialServicesCompliance();
            
            // Accessibility Standards (WCAG 2.1)
            registerAccessibilityStandards();
            
            log.info("Initialized {} compliance frameworks", frameworks.size());
            
        } catch (Exception e) {
            log.error("Error initializing compliance frameworks", e);
        }
    }

    /**
     * Get compliance framework by ID
     */
    public ComplianceFramework getFramework(String frameworkId) {
        return frameworks.get(frameworkId);
    }

    /**
     * Get all available frameworks
     */
    public Collection<ComplianceFramework> getAllFrameworks() {
        return new ArrayList<>(frameworks.values());
    }

    /**
     * Get frameworks by category
     */
    public List<ComplianceFramework> getFrameworksByCategory(FrameworkCategory category) {
        return frameworks.values().stream()
            .filter(framework -> framework.getCategory() == category)
            .toList();
    }

    /**
     * Register custom compliance framework
     */
    public void registerFramework(ComplianceFramework framework) {
        try {
            validateFramework(framework);
            
            frameworks.put(framework.getId(), framework);
            
            FrameworkMetadata metadata = FrameworkMetadata.builder()
                .frameworkId(framework.getId())
                .registeredAt(LocalDateTime.now())
                .version(framework.getVersion())
                .lastUpdated(LocalDateTime.now())
                .build();
            
            frameworkMetadata.put(framework.getId(), metadata);
            
            log.info("Registered compliance framework: {} - {}", framework.getId(), framework.getName());
            
        } catch (Exception e) {
            log.error("Error registering compliance framework: {}", framework.getId(), e);
            throw new ComplianceException("Failed to register framework", e);
        }
    }

    /**
     * Update existing framework
     */
    public void updateFramework(ComplianceFramework framework) {
        try {
            if (!frameworks.containsKey(framework.getId())) {
                throw new IllegalArgumentException("Framework not found: " + framework.getId());
            }
            
            validateFramework(framework);
            
            frameworks.put(framework.getId(), framework);
            
            FrameworkMetadata metadata = frameworkMetadata.get(framework.getId());
            if (metadata != null) {
                metadata.setLastUpdated(LocalDateTime.now());
                metadata.setVersion(framework.getVersion());
            }
            
            log.info("Updated compliance framework: {}", framework.getId());
            
        } catch (Exception e) {
            log.error("Error updating compliance framework: {}", framework.getId(), e);
            throw new ComplianceException("Failed to update framework", e);
        }
    }

    // Private framework initialization methods

    private void registerDataPrivacyActFramework() {
        ComplianceFramework dpaFramework = ComplianceFramework.builder()
            .id("PH_DPA_2012")
            .name("Philippine Data Privacy Act 2012")
            .description("Republic Act No. 10173 - Data Privacy Act of 2012")
            .category(FrameworkCategory.DATA_PRIVACY)
            .version("1.0")
            .enabled(true)
            .rules(createDataPrivacyActRules())
            .build();
        
        frameworks.put(dpaFramework.getId(), dpaFramework);
    }

    private void registerGDPRFramework() {
        ComplianceFramework gdprFramework = ComplianceFramework.builder()
            .id("EU_GDPR")
            .name("General Data Protection Regulation")
            .description("EU General Data Protection Regulation (GDPR)")
            .category(FrameworkCategory.DATA_PRIVACY)
            .version("1.0")
            .enabled(true)
            .rules(createGDPRRules())
            .build();
        
        frameworks.put(gdprFramework.getId(), gdprFramework);
    }

    private void registerISO27001Framework() {
        ComplianceFramework iso27001Framework = ComplianceFramework.builder()
            .id("ISO_27001")
            .name("ISO 27001 Information Security Management")
            .description("ISO/IEC 27001:2013 Information Security Management Systems")
            .category(FrameworkCategory.INFORMATION_SECURITY)
            .version("2013")
            .enabled(true)
            .rules(createISO27001Rules())
            .build();
        
        frameworks.put(iso27001Framework.getId(), iso27001Framework);
    }

    private void registerNISTFramework() {
        ComplianceFramework nistFramework = ComplianceFramework.builder()
            .id("NIST_CSF")
            .name("NIST Cybersecurity Framework")
            .description("NIST Cybersecurity Framework v1.1")
            .category(FrameworkCategory.CYBERSECURITY)
            .version("1.1")
            .enabled(true)
            .rules(createNISTRules())
            .build();
        
        frameworks.put(nistFramework.getId(), nistFramework);
    }

    private void registerPhilippineICTStandards() {
        ComplianceFramework ictFramework = ComplianceFramework.builder()
            .id("PH_ICT_STANDARDS")
            .name("Philippine Government ICT Standards")
            .description("DICT ICT Standards and Guidelines")
            .category(FrameworkCategory.GOVERNMENT_STANDARDS)
            .version("1.0")
            .enabled(true)
            .rules(createPhilippineICTRules())
            .build();
        
        frameworks.put(ictFramework.getId(), ictFramework);
    }

    private void registerSocialProtectionStandards() {
        ComplianceFramework socialProtectionFramework = ComplianceFramework.builder()
            .id("SOCIAL_PROTECTION_STANDARDS")
            .name("Social Protection Standards")
            .description("DSWD Social Protection Program Standards")
            .category(FrameworkCategory.SOCIAL_PROTECTION)
            .version("1.0")
            .enabled(true)
            .rules(createSocialProtectionRules())
            .build();
        
        frameworks.put(socialProtectionFramework.getId(), socialProtectionFramework);
    }

    private void registerFinancialServicesCompliance() {
        ComplianceFramework financialFramework = ComplianceFramework.builder()
            .id("FINANCIAL_SERVICES")
            .name("Financial Services Compliance")
            .description("BSP and SEC Financial Services Regulations")
            .category(FrameworkCategory.FINANCIAL_SERVICES)
            .version("1.0")
            .enabled(true)
            .rules(createFinancialServicesRules())
            .build();
        
        frameworks.put(financialFramework.getId(), financialFramework);
    }

    private void registerAccessibilityStandards() {
        ComplianceFramework accessibilityFramework = ComplianceFramework.builder()
            .id("WCAG_2_1")
            .name("Web Content Accessibility Guidelines 2.1")
            .description("WCAG 2.1 Level AA Accessibility Standards")
            .category(FrameworkCategory.ACCESSIBILITY)
            .version("2.1")
            .enabled(true)
            .rules(createAccessibilityRules())
            .build();
        
        frameworks.put(accessibilityFramework.getId(), accessibilityFramework);
    }

    // Rule creation methods

    private List<ComplianceRule> createDataPrivacyActRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("DPA_CONSENT")
                .name("Data Subject Consent")
                .description("Ensure proper consent is obtained before processing personal data")
                .category("Data Processing")
                .severity(ViolationSeverity.HIGH)
                .condition("personal_data_processing.consent_obtained == true")
                .build(),
            
            ComplianceRule.builder()
                .id("DPA_PURPOSE_LIMITATION")
                .name("Purpose Limitation")
                .description("Personal data must be processed only for declared purposes")
                .category("Data Processing")
                .severity(ViolationSeverity.HIGH)
                .condition("personal_data_processing.purpose_declared == true && personal_data_processing.purpose_exceeded == false")
                .build(),
            
            ComplianceRule.builder()
                .id("DPA_DATA_RETENTION")
                .name("Data Retention Limits")
                .description("Personal data must not be retained longer than necessary")
                .category("Data Retention")
                .severity(ViolationSeverity.MEDIUM)
                .condition("personal_data.retention_period <= declared_retention_period")
                .build(),
            
            ComplianceRule.builder()
                .id("DPA_SECURITY_MEASURES")
                .name("Security Measures")
                .description("Implement appropriate security measures for personal data")
                .category("Data Security")
                .severity(ViolationSeverity.CRITICAL)
                .condition("security_measures.encryption == true && security_measures.access_controls == true")
                .build()
        );
    }

    private List<ComplianceRule> createGDPRRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("GDPR_LAWFUL_BASIS")
                .name("Lawful Basis for Processing")
                .description("Processing must have a lawful basis under GDPR Article 6")
                .category("Legal Basis")
                .severity(ViolationSeverity.CRITICAL)
                .condition("data_processing.lawful_basis != null && data_processing.lawful_basis_documented == true")
                .build(),
            
            ComplianceRule.builder()
                .id("GDPR_DATA_MINIMIZATION")
                .name("Data Minimization")
                .description("Process only data that is necessary for the purpose")
                .category("Data Processing")
                .severity(ViolationSeverity.HIGH)
                .condition("data_processing.data_minimized == true")
                .build(),
            
            ComplianceRule.builder()
                .id("GDPR_BREACH_NOTIFICATION")
                .name("Breach Notification")
                .description("Report data breaches within 72 hours")
                .category("Incident Response")
                .severity(ViolationSeverity.CRITICAL)
                .condition("data_breach.notification_time <= 72_hours")
                .build()
        );
    }

    private List<ComplianceRule> createISO27001Rules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("ISO27001_RISK_ASSESSMENT")
                .name("Information Security Risk Assessment")
                .description("Conduct regular information security risk assessments")
                .category("Risk Management")
                .severity(ViolationSeverity.HIGH)
                .condition("risk_assessment.conducted == true && risk_assessment.last_update <= 12_months")
                .build(),
            
            ComplianceRule.builder()
                .id("ISO27001_ACCESS_CONTROL")
                .name("Access Control Management")
                .description("Implement proper access control measures")
                .category("Access Control")
                .severity(ViolationSeverity.HIGH)
                .condition("access_control.implemented == true && access_control.regularly_reviewed == true")
                .build()
        );
    }

    private List<ComplianceRule> createNISTRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("NIST_IDENTIFY")
                .name("Asset Management")
                .description("Identify and manage organizational assets")
                .category("Identify")
                .severity(ViolationSeverity.MEDIUM)
                .condition("asset_management.inventory_maintained == true")
                .build(),
            
            ComplianceRule.builder()
                .id("NIST_PROTECT")
                .name("Protective Measures")
                .description("Implement appropriate protective measures")
                .category("Protect")
                .severity(ViolationSeverity.HIGH)
                .condition("protective_measures.implemented == true")
                .build()
        );
    }

    private List<ComplianceRule> createPhilippineICTRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("PH_ICT_INTEROPERABILITY")
                .name("System Interoperability")
                .description("Ensure system interoperability with government standards")
                .category("Interoperability")
                .severity(ViolationSeverity.MEDIUM)
                .condition("system.interoperability_standards_compliant == true")
                .build()
        );
    }

    private List<ComplianceRule> createSocialProtectionRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("SP_BENEFICIARY_VERIFICATION")
                .name("Beneficiary Verification")
                .description("Verify beneficiary eligibility before providing assistance")
                .category("Beneficiary Management")
                .severity(ViolationSeverity.HIGH)
                .condition("beneficiary.verification_completed == true && beneficiary.eligibility_confirmed == true")
                .build()
        );
    }

    private List<ComplianceRule> createFinancialServicesRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("FS_AML_COMPLIANCE")
                .name("Anti-Money Laundering Compliance")
                .description("Implement AML compliance measures")
                .category("Financial Compliance")
                .severity(ViolationSeverity.CRITICAL)
                .condition("aml_compliance.implemented == true && aml_compliance.monitoring_active == true")
                .build()
        );
    }

    private List<ComplianceRule> createAccessibilityRules() {
        return Arrays.asList(
            ComplianceRule.builder()
                .id("WCAG_KEYBOARD_ACCESS")
                .name("Keyboard Accessibility")
                .description("All functionality must be accessible via keyboard")
                .category("Accessibility")
                .severity(ViolationSeverity.MEDIUM)
                .condition("accessibility.keyboard_accessible == true")
                .build(),
            
            ComplianceRule.builder()
                .id("WCAG_ALT_TEXT")
                .name("Alternative Text")
                .description("Provide alternative text for images")
                .category("Accessibility")
                .severity(ViolationSeverity.MEDIUM)
                .condition("accessibility.alt_text_provided == true")
                .build()
        );
    }

    private void validateFramework(ComplianceFramework framework) {
        if (framework.getId() == null || framework.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Framework ID cannot be null or empty");
        }
        
        if (framework.getName() == null || framework.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Framework name cannot be null or empty");
        }
        
        if (framework.getRules() == null || framework.getRules().isEmpty()) {
            throw new IllegalArgumentException("Framework must have at least one rule");
        }
        
        // Validate each rule
        for (ComplianceRule rule : framework.getRules()) {
            if (rule.getId() == null || rule.getCondition() == null) {
                throw new IllegalArgumentException("Invalid rule configuration in framework: " + framework.getId());
            }
        }
    }
}
