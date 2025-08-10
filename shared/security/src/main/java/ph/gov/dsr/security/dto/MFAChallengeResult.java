package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.MFAMethod;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for MFA challenge result
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFAChallengeResult {

    private boolean success;
    
    private String challengeId;
    
    private MFAMethod method;
    
    private String message;
    
    private LocalDateTime expiresAt;
    
    private LocalDateTime issuedAt;

    // Token information
    private String tokenHint;
    
    private int tokenLength;
    
    private int maxAttempts;
    
    private int remainingAttempts;

    // Delivery information
    private String deliveryTarget; // masked phone/email
    
    private String deliveryStatus;
    
    private LocalDateTime deliveredAt;

    // Alternative methods
    private List<MFAMethod> fallbackMethods;
    
    private boolean canUseFallback;

    // Error information
    private String errorCode;
    
    private String errorMessage;
    
    private String failureReason;

    // Security context
    private String sessionId;
    
    private String riskLevel;
    
    private boolean requiresStepUp;

    // Rate limiting information
    private boolean rateLimited;
    
    private LocalDateTime rateLimitResetAt;
    
    private int rateLimitRemaining;

    // Instructions for user
    private String userInstructions;
    
    private String nextStepUrl;
    
    private int estimatedDeliverySeconds;

    // Helper methods
    public boolean isSuccessful() {
        return success;
    }

    public boolean hasExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasRemainingAttempts() {
        return remainingAttempts > 0;
    }

    public boolean isDelivered() {
        return "DELIVERED".equalsIgnoreCase(deliveryStatus) && deliveredAt != null;
    }

    public boolean canRetry() {
        return !rateLimited && hasRemainingAttempts() && !hasExpired();
    }

    public boolean hasFallbackOptions() {
        return canUseFallback && fallbackMethods != null && !fallbackMethods.isEmpty();
    }

    public long getSecondsUntilExpiry() {
        if (expiresAt == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }

    // Static factory methods
    public static MFAChallengeResult success(String challengeId, MFAMethod method, String message, LocalDateTime expiresAt) {
        return MFAChallengeResult.builder()
                .success(true)
                .challengeId(challengeId)
                .method(method)
                .message(message)
                .expiresAt(expiresAt)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    public static MFAChallengeResult failure(String errorCode, String errorMessage) {
        return MFAChallengeResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    public static MFAChallengeResult rateLimited(LocalDateTime resetAt) {
        return MFAChallengeResult.builder()
                .success(false)
                .rateLimited(true)
                .rateLimitResetAt(resetAt)
                .errorCode("RATE_LIMITED")
                .errorMessage("Too many requests. Please try again later.")
                .issuedAt(LocalDateTime.now())
                .build();
    }
}
