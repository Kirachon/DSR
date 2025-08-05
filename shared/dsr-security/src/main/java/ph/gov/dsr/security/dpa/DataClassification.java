package ph.gov.dsr.security.dpa;

/**
 * Data Classification Levels for Philippine Data Privacy Act (DPA) Compliance
 * 
 * This enum defines the classification levels for personal data processing
 * in accordance with the Data Privacy Act of 2012 (Republic Act No. 10173)
 * and its Implementing Rules and Regulations.
 */
public enum DataClassification {
    
    /**
     * PUBLIC - Information that can be freely shared without privacy concerns
     * Examples: Published reports, public announcements, general program information
     */
    PUBLIC("Public", 0, false, false),
    
    /**
     * INTERNAL - Information for internal government use only
     * Examples: Internal procedures, non-sensitive operational data
     */
    INTERNAL("Internal", 1, false, false),
    
    /**
     * CONFIDENTIAL - Personal data that requires protection
     * Examples: Names, addresses, contact information, employment records
     */
    CONFIDENTIAL("Confidential", 2, true, false),
    
    /**
     * RESTRICTED - Sensitive personal data requiring special protection
     * Examples: Health records, financial information, government IDs
     */
    RESTRICTED("Restricted", 3, true, true),
    
    /**
     * HIGHLY_RESTRICTED - Privileged and sensitive personal data
     * Examples: Criminal records, biometric data, PhilSys data, social case records
     */
    HIGHLY_RESTRICTED("Highly Restricted", 4, true, true);
    
    private final String displayName;
    private final int level;
    private final boolean isPersonalData;
    private final boolean isSensitivePersonalData;
    
    DataClassification(String displayName, int level, boolean isPersonalData, boolean isSensitivePersonalData) {
        this.displayName = displayName;
        this.level = level;
        this.isPersonalData = isPersonalData;
        this.isSensitivePersonalData = isSensitivePersonalData;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isPersonalData() {
        return isPersonalData;
    }
    
    public boolean isSensitivePersonalData() {
        return isSensitivePersonalData;
    }
    
    /**
     * Determines if this classification requires explicit consent
     */
    public boolean requiresExplicitConsent() {
        return isSensitivePersonalData;
    }
    
    /**
     * Determines if this classification requires data encryption at rest
     */
    public boolean requiresEncryptionAtRest() {
        return level >= CONFIDENTIAL.level;
    }
    
    /**
     * Determines if this classification requires data encryption in transit
     */
    public boolean requiresEncryptionInTransit() {
        return level >= CONFIDENTIAL.level;
    }
    
    /**
     * Determines if this classification requires audit logging
     */
    public boolean requiresAuditLogging() {
        return level >= CONFIDENTIAL.level;
    }
    
    /**
     * Determines if this classification requires data masking in non-production
     */
    public boolean requiresDataMasking() {
        return level >= RESTRICTED.level;
    }
    
    /**
     * Gets the minimum retention period in months for this classification
     */
    public int getMinimumRetentionMonths() {
        switch (this) {
            case PUBLIC:
                return 0; // No retention requirement
            case INTERNAL:
                return 12; // 1 year
            case CONFIDENTIAL:
                return 60; // 5 years
            case RESTRICTED:
                return 84; // 7 years
            case HIGHLY_RESTRICTED:
                return 120; // 10 years
            default:
                return 12;
        }
    }
    
    /**
     * Gets the maximum retention period in months for this classification
     */
    public int getMaximumRetentionMonths() {
        switch (this) {
            case PUBLIC:
                return Integer.MAX_VALUE; // No limit
            case INTERNAL:
                return 60; // 5 years
            case CONFIDENTIAL:
                return 120; // 10 years
            case RESTRICTED:
                return 180; // 15 years
            case HIGHLY_RESTRICTED:
                return 300; // 25 years (lifetime records)
            default:
                return 120;
        }
    }
}
