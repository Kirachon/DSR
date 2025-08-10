package ph.gov.dsr.security.exception;

/**
 * Exception thrown when security scan operations fail
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public class SecurityScanException extends RuntimeException {

    /**
     * Constructs a new security scan exception with the specified detail message.
     *
     * @param message the detail message
     */
    public SecurityScanException(String message) {
        super(message);
    }

    /**
     * Constructs a new security scan exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public SecurityScanException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new security scan exception with the specified cause.
     *
     * @param cause the cause
     */
    public SecurityScanException(Throwable cause) {
        super(cause);
    }
}
