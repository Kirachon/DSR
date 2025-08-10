package ph.gov.dsr.security.dto;

/**
 * Risk Level Enumeration
 * Defines standardized risk levels for security assessments
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum RiskLevel {
    
    /**
     * Minimal risk level
     */
    MINIMAL("Minimal", 0, 20, "Minimal risk - routine monitoring"),
    
    /**
     * Low risk level
     */
    LOW("Low", 21, 40, "Low risk - standard security measures"),
    
    /**
     * Medium risk level
     */
    MEDIUM("Medium", 41, 60, "Medium risk - enhanced monitoring required"),
    
    /**
     * High risk level
     */
    HIGH("High", 61, 80, "High risk - immediate attention required"),
    
    /**
     * Critical risk level
     */
    CRITICAL("Critical", 81, 100, "Critical risk - emergency response required");

    private final String displayName;
    private final int minScore;
    private final int maxScore;
    private final String description;

    RiskLevel(String displayName, int minScore, int maxScore, String description) {
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get risk level from risk score
     */
    public static RiskLevel fromRiskScore(int riskScore) {
        for (RiskLevel level : values()) {
            if (riskScore >= level.minScore && riskScore <= level.maxScore) {
                return level;
            }
        }
        return MINIMAL; // Default fallback
    }

    /**
     * Check if this risk level requires immediate action
     */
    public boolean requiresImmediateAction() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * Check if this risk level requires enhanced monitoring
     */
    public boolean requiresEnhancedMonitoring() {
        return this == MEDIUM || this == HIGH || this == CRITICAL;
    }

    /**
     * Get numeric priority (higher number = higher priority)
     */
    public int getPriority() {
        return ordinal() + 1;
    }

    /**
     * Check if this level is higher than another level
     */
    public boolean isHigherThan(RiskLevel other) {
        return this.getPriority() > other.getPriority();
    }

    /**
     * Check if this level is lower than another level
     */
    public boolean isLowerThan(RiskLevel other) {
        return this.getPriority() < other.getPriority();
    }
}
