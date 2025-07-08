package ph.gov.dsr.security.exception;

/**
 * Exception thrown for Multi-Factor Authentication related errors
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
public class MFAException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;
    private final boolean retryable;

    /**
     * Constructs a new MFA exception with the specified detail message.
     *
     * @param message the detail message
     */
    public MFAException(String message) {
        super(message);
        this.errorCode = "MFA_ERROR";
        this.userMessage = message;
        this.retryable = false;
    }

    /**
     * Constructs a new MFA exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public MFAException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MFA_ERROR";
        this.userMessage = message;
        this.retryable = false;
    }

    /**
     * Constructs a new MFA exception with error code and message.
     *
     * @param errorCode the error code
     * @param message the detail message
     */
    public MFAException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.retryable = false;
    }

    /**
     * Constructs a new MFA exception with error code, message, and cause.
     *
     * @param errorCode the error code
     * @param message the detail message
     * @param cause the cause
     */
    public MFAException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.retryable = false;
    }

    /**
     * Constructs a new MFA exception with full parameters.
     *
     * @param errorCode the error code
     * @param message the detail message
     * @param userMessage the user-friendly message
     * @param retryable whether the operation can be retried
     */
    public MFAException(String errorCode, String message, String userMessage, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.retryable = retryable;
    }

    /**
     * Constructs a new MFA exception with full parameters and cause.
     *
     * @param errorCode the error code
     * @param message the detail message
     * @param userMessage the user-friendly message
     * @param retryable whether the operation can be retried
     * @param cause the cause
     */
    public MFAException(String errorCode, String message, String userMessage, boolean retryable, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.retryable = retryable;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the user-friendly message.
     *
     * @return the user message
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * Checks if the operation can be retried.
     *
     * @return true if retryable, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }

    // Static factory methods for common MFA exceptions

    /**
     * Creates an exception for invalid MFA method.
     */
    public static MFAException invalidMethod(String method) {
        return new MFAException("INVALID_MFA_METHOD", 
                "Invalid MFA method: " + method, 
                "The specified authentication method is not supported.", 
                false);
    }

    /**
     * Creates an exception for MFA not configured.
     */
    public static MFAException notConfigured(String userId) {
        return new MFAException("MFA_NOT_CONFIGURED", 
                "MFA not configured for user: " + userId, 
                "Multi-factor authentication is not set up for this account.", 
                false);
    }

    /**
     * Creates an exception for invalid token.
     */
    public static MFAException invalidToken() {
        return new MFAException("INVALID_TOKEN", 
                "Invalid MFA token", 
                "The authentication code is invalid or has expired.", 
                true);
    }

    /**
     * Creates an exception for expired token.
     */
    public static MFAException expiredToken() {
        return new MFAException("EXPIRED_TOKEN", 
                "MFA token has expired", 
                "The authentication code has expired. Please request a new one.", 
                true);
    }

    /**
     * Creates an exception for too many attempts.
     */
    public static MFAException tooManyAttempts() {
        return new MFAException("TOO_MANY_ATTEMPTS", 
                "Too many failed MFA attempts", 
                "Too many failed attempts. Please try again later.", 
                false);
    }

    /**
     * Creates an exception for account locked.
     */
    public static MFAException accountLocked(String unlockTime) {
        return new MFAException("ACCOUNT_LOCKED", 
                "Account is locked due to failed MFA attempts", 
                "Account is temporarily locked. Try again after " + unlockTime + ".", 
                false);
    }

    /**
     * Creates an exception for service unavailable.
     */
    public static MFAException serviceUnavailable(String service) {
        return new MFAException("SERVICE_UNAVAILABLE", 
                service + " service is currently unavailable", 
                "Authentication service is temporarily unavailable. Please try again later.", 
                true);
    }

    /**
     * Creates an exception for rate limiting.
     */
    public static MFAException rateLimited() {
        return new MFAException("RATE_LIMITED", 
                "Rate limit exceeded for MFA requests", 
                "Too many requests. Please wait before trying again.", 
                true);
    }

    /**
     * Creates an exception for setup failure.
     */
    public static MFAException setupFailed(String reason) {
        return new MFAException("SETUP_FAILED", 
                "MFA setup failed: " + reason, 
                "Failed to set up multi-factor authentication. Please try again.", 
                true);
    }

    /**
     * Creates an exception for verification failure.
     */
    public static MFAException verificationFailed(String reason) {
        return new MFAException("VERIFICATION_FAILED", 
                "MFA verification failed: " + reason, 
                "Authentication verification failed. Please check your code and try again.", 
                true);
    }
}
