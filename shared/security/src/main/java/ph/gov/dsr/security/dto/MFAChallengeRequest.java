package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

/**
 * DTO for MFA challenge request
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAChallengeRequest {

    private String userId;

    private MFAMethod preferredMethod;
    
    private String sessionId;
    
    private String deviceId;
    
    private String ipAddress;
    
    private String userAgent;

    // Context information
    private String authenticationContext;
    
    private String riskLevel;
    
    private boolean highRiskTransaction = false;

    // Challenge specific options
    private boolean allowFallbackMethods = true;
    
    private boolean sendImmediately = true;
    
    private int customTokenLength;
    
    private int customExpiryMinutes;

    // Previous challenge information
    private String previousChallengeId;
    
    private String previousFailureReason;
    
    private int previousAttempts;

    // Additional security context
    private String transactionId;
    
    private String operationType;
    
    private String operationDescription;

    // Helper methods
    public boolean isHighRisk() {
        return highRiskTransaction || "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }

    public boolean hasCustomTokenSettings() {
        return customTokenLength > 0 || customExpiryMinutes > 0;
    }

    public boolean isRetryAttempt() {
        return previousChallengeId != null && !previousChallengeId.trim().isEmpty();
    }

    public boolean hasTransactionContext() {
        return transactionId != null && !transactionId.trim().isEmpty();
    }

    public int getEffectiveTokenLength(int defaultLength) {
        return customTokenLength > 0 ? customTokenLength : defaultLength;
    }

    public int getEffectiveExpiryMinutes(int defaultExpiry) {
        return customExpiryMinutes > 0 ? customExpiryMinutes : defaultExpiry;
    }
}
