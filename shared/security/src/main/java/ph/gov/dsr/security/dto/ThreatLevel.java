package ph.gov.dsr.security.dto;

/**
 * Threat Level Enumeration
 * Defines standardized threat levels for security assessments
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum ThreatLevel {
    
    /**
     * Minimal threat level - routine monitoring
     */
    MINIMAL("Minimal", 0, 20, "Routine monitoring sufficient"),
    
    /**
     * Low threat level - standard security measures
     */
    LOW("Low", 21, 40, "Standard security measures apply"),
    
    /**
     * Medium threat level - enhanced monitoring required
     */
    MEDIUM("Medium", 41, 60, "Enhanced monitoring and controls required"),
    
    /**
     * High threat level - immediate attention required
     */
    HIGH("High", 61, 80, "Immediate attention and enhanced security required"),
    
    /**
     * Critical threat level - emergency response required
     */
    CRITICAL("Critical", 81, 100, "Emergency response and immediate action required");

    private final String displayName;
    private final int minScore;
    private final int maxScore;
    private final String description;

    ThreatLevel(String displayName, int minScore, int maxScore, String description) {
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
     * Get threat level from risk score
     */
    public static ThreatLevel fromRiskScore(int riskScore) {
        for (ThreatLevel level : values()) {
            if (riskScore >= level.minScore && riskScore <= level.maxScore) {
                return level;
            }
        }
        return MINIMAL; // Default fallback
    }

    /**
     * Check if this threat level requires immediate action
     */
    public boolean requiresImmediateAction() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * Check if this threat level requires enhanced monitoring
     */
    public boolean requiresEnhancedMonitoring() {
        return this == MEDIUM || this == HIGH || this == CRITICAL;
    }

    /**
     * Check if this threat level requires blocking
     */
    public boolean requiresBlocking() {
        return this == CRITICAL;
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
    public boolean isHigherThan(ThreatLevel other) {
        return this.getPriority() > other.getPriority();
    }

    /**
     * Check if this level is lower than another level
     */
    public boolean isLowerThan(ThreatLevel other) {
        return this.getPriority() < other.getPriority();
    }
}
