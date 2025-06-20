package ph.gov.dsr.common.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Base exception class for all DSR-specific exceptions.
 * Provides consistent error handling across all services.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Getter
public class DsrException extends RuntimeException {

    /**
     * Machine-readable error code.
     */
    private final String errorCode;

    /**
     * Additional error details.
     */
    private final Map<String, Object> details;

    /**
     * HTTP status code to return.
     */
    private final int httpStatus;

    /**
     * Creates a new DSR exception.
     * 
     * @param errorCode Machine-readable error code
     * @param message Human-readable error message
     * @param httpStatus HTTP status code
     */
    public DsrException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    /**
     * Creates a new DSR exception with details.
     * 
     * @param errorCode Machine-readable error code
     * @param message Human-readable error message
     * @param httpStatus HTTP status code
     * @param details Additional error details
     */
    public DsrException(String errorCode, String message, int httpStatus, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }

    /**
     * Creates a new DSR exception with cause.
     * 
     * @param errorCode Machine-readable error code
     * @param message Human-readable error message
     * @param httpStatus HTTP status code
     * @param cause The underlying cause
     */
    public DsrException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    /**
     * Creates a new DSR exception with details and cause.
     * 
     * @param errorCode Machine-readable error code
     * @param message Human-readable error message
     * @param httpStatus HTTP status code
     * @param details Additional error details
     * @param cause The underlying cause
     */
    public DsrException(String errorCode, String message, int httpStatus, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }
}

/**
 * Exception thrown when a requested resource is not found.
 */
class ResourceNotFoundException extends DsrException {
    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s with identifier '%s' not found", resource, identifier), 
              404);
    }
}

/**
 * Exception thrown when a resource already exists.
 */
class ResourceAlreadyExistsException extends DsrException {
    public ResourceAlreadyExistsException(String resource, String identifier) {
        super("RESOURCE_ALREADY_EXISTS", 
              String.format("%s with identifier '%s' already exists", resource, identifier), 
              409);
    }
}

/**
 * Exception thrown when validation fails.
 */
class ValidationException extends DsrException {
    public ValidationException(String message) {
        super("VALIDATION_FAILED", message, 400);
    }

    public ValidationException(String message, Map<String, Object> details) {
        super("VALIDATION_FAILED", message, 400, details);
    }
}

/**
 * Exception thrown when authentication fails.
 */
class AuthenticationException extends DsrException {
    public AuthenticationException(String message) {
        super("AUTHENTICATION_FAILED", message, 401);
    }
}

/**
 * Exception thrown when authorization fails.
 */
class AuthorizationException extends DsrException {
    public AuthorizationException(String message) {
        super("AUTHORIZATION_FAILED", message, 403);
    }
}

/**
 * Exception thrown when external service integration fails.
 */
class ExternalServiceException extends DsrException {
    public ExternalServiceException(String service, String message) {
        super("EXTERNAL_SERVICE_ERROR", 
              String.format("External service '%s' error: %s", service, message), 
              502);
    }

    public ExternalServiceException(String service, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", 
              String.format("External service '%s' error: %s", service, message), 
              502, cause);
    }
}

/**
 * Exception thrown when business rules are violated.
 */
class BusinessRuleException extends DsrException {
    public BusinessRuleException(String rule, String message) {
        super("BUSINESS_RULE_VIOLATION", 
              String.format("Business rule '%s' violated: %s", rule, message), 
              422);
    }
}

/**
 * Exception thrown when rate limits are exceeded.
 */
class RateLimitExceededException extends DsrException {
    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message, 429);
    }
}

/**
 * Exception thrown for internal server errors.
 */
class InternalServerException extends DsrException {
    public InternalServerException(String message) {
        super("INTERNAL_SERVER_ERROR", message, 500);
    }

    public InternalServerException(String message, Throwable cause) {
        super("INTERNAL_SERVER_ERROR", message, 500, cause);
    }
}
