package ph.gov.dsr.security.entity;

/**
 * Enumeration for security scan status
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum ScanStatus {
    
    /**
     * Scan is scheduled but not yet started
     */
    SCHEDULED("Scheduled", "Scan is scheduled but not yet started"),
    
    /**
     * Scan is queued and waiting to start
     */
    QUEUED("Queued", "Scan is queued and waiting to start"),
    
    /**
     * Scan is currently running
     */
    RUNNING("Running", "Scan is currently in progress"),
    
    /**
     * Scan is paused
     */
    PAUSED("Paused", "Scan execution is temporarily paused"),
    
    /**
     * Scan completed successfully
     */
    COMPLETED("Completed", "Scan completed successfully"),
    
    /**
     * Scan failed due to error
     */
    FAILED("Failed", "Scan failed due to an error"),
    
    /**
     * Scan was cancelled by user
     */
    CANCELLED("Cancelled", "Scan was cancelled by user"),
    
    /**
     * Scan timed out
     */
    TIMEOUT("Timeout", "Scan exceeded maximum duration and timed out"),
    
    /**
     * Scan completed with warnings
     */
    COMPLETED_WITH_WARNINGS("Completed with Warnings", "Scan completed but with warnings"),
    
    /**
     * Scan is being retried after failure
     */
    RETRYING("Retrying", "Scan is being retried after failure"),
    
    /**
     * Scan is being validated
     */
    VALIDATING("Validating", "Scan results are being validated"),
    
    /**
     * Scan results are being processed
     */
    PROCESSING("Processing", "Scan results are being processed");
    
    private final String displayName;
    private final String description;
    
    ScanStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get scan status from string value (case insensitive)
     */
    public static ScanStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return SCHEDULED;
        }
        
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SCHEDULED; // Default to scheduled if invalid value
        }
    }
    
    /**
     * Check if scan is in progress
     */
    public boolean isInProgress() {
        return this == RUNNING || this == QUEUED || this == RETRYING || this == VALIDATING || this == PROCESSING;
    }
    
    /**
     * Check if scan is completed (successfully or with issues)
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == COMPLETED_WITH_WARNINGS;
    }
    
    /**
     * Check if scan has failed
     */
    public boolean isFailed() {
        return this == FAILED || this == TIMEOUT;
    }
    
    /**
     * Check if scan was terminated
     */
    public boolean isTerminated() {
        return this == CANCELLED || this == FAILED || this == TIMEOUT;
    }
    
    /**
     * Check if scan can be resumed
     */
    public boolean canBeResumed() {
        return this == PAUSED;
    }
    
    /**
     * Check if scan can be cancelled
     */
    public boolean canBeCancelled() {
        return this == SCHEDULED || this == QUEUED || this == RUNNING || this == PAUSED || this == RETRYING;
    }
    
    /**
     * Check if scan requires attention
     */
    public boolean requiresAttention() {
        return this == FAILED || this == TIMEOUT || this == COMPLETED_WITH_WARNINGS;
    }
    
    /**
     * Get next possible statuses from current status
     */
    public ScanStatus[] getNextPossibleStatuses() {
        switch (this) {
            case SCHEDULED:
                return new ScanStatus[]{QUEUED, CANCELLED};
            case QUEUED:
                return new ScanStatus[]{RUNNING, CANCELLED};
            case RUNNING:
                return new ScanStatus[]{COMPLETED, FAILED, CANCELLED, PAUSED, TIMEOUT, COMPLETED_WITH_WARNINGS, PROCESSING};
            case PAUSED:
                return new ScanStatus[]{RUNNING, CANCELLED};
            case PROCESSING:
                return new ScanStatus[]{COMPLETED, FAILED, COMPLETED_WITH_WARNINGS, VALIDATING};
            case VALIDATING:
                return new ScanStatus[]{COMPLETED, FAILED, COMPLETED_WITH_WARNINGS};
            case FAILED:
                return new ScanStatus[]{RETRYING};
            case RETRYING:
                return new ScanStatus[]{RUNNING, FAILED, CANCELLED};
            default:
                return new ScanStatus[0]; // Terminal states
        }
    }
}
