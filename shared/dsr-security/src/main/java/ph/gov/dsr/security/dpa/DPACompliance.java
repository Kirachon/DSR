package ph.gov.dsr.security.dpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields, methods, or classes for DPA compliance tracking
 * 
 * This annotation is used to automatically enforce Data Privacy Act compliance
 * requirements including audit logging, data masking, and retention policies.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DPACompliance {
    
    /**
     * Data classification level
     */
    DataClassification classification() default DataClassification.CONFIDENTIAL;
    
    /**
     * Purpose of data processing
     */
    String purpose() default "";
    
    /**
     * Legal basis for processing under DPA
     */
    LegalBasis legalBasis() default LegalBasis.LEGITIMATE_INTEREST;
    
    /**
     * Whether explicit consent is required
     */
    boolean requiresConsent() default false;
    
    /**
     * Data retention period in months (0 = use classification default)
     */
    int retentionMonths() default 0;
    
    /**
     * Whether to mask this data in non-production environments
     */
    boolean maskInNonProd() default true;
    
    /**
     * Whether to log access to this data
     */
    boolean auditAccess() default true;
    
    /**
     * Data subject categories that this data relates to
     */
    String[] dataSubjects() default {"citizen"};
    
    /**
     * Additional security measures required
     */
    String[] securityMeasures() default {};
    
    /**
     * Legal basis for data processing under Philippine DPA
     */
    enum LegalBasis {
        CONSENT("Consent of the data subject"),
        CONTRACT("Performance of a contract"),
        LEGAL_OBLIGATION("Compliance with legal obligation"),
        VITAL_INTERESTS("Protection of vital interests"),
        PUBLIC_TASK("Performance of public task"),
        LEGITIMATE_INTEREST("Legitimate interests");
        
        private final String description;
        
        LegalBasis(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
