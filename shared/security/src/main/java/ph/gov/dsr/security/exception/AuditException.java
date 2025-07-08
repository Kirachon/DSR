package ph.gov.dsr.security.exception;

/**
 * Exception thrown when audit operations fail
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public class AuditException extends RuntimeException {

    /**
     * Constructs a new audit exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AuditException(String message) {
        super(message);
    }

    /**
     * Constructs a new audit exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new audit exception with the specified cause.
     *
     * @param cause the cause
     */
    public AuditException(Throwable cause) {
        super(cause);
    }
}
