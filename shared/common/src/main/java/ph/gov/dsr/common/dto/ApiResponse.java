package ph.gov.dsr.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard API response wrapper for all DSR services.
 * Provides consistent response format across all endpoints.
 * 
 * @param <T> The type of data being returned
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates whether the request was successful.
     */
    private boolean success;

    /**
     * Human-readable message describing the result.
     */
    private String message;

    /**
     * The actual data payload.
     */
    private T data;

    /**
     * Error information if the request failed.
     */
    private ErrorInfo error;

    /**
     * Metadata about the response (pagination, etc.).
     */
    private Map<String, Object> metadata;

    /**
     * Timestamp when the response was generated.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant timestamp;

    /**
     * Unique request identifier for tracking.
     */
    private String requestId;

    /**
     * API version that generated this response.
     */
    private String version;

    /**
     * Creates a successful response with data.
     * 
     * @param data The response data
     * @param <T> The type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .version("3.0.0")
                .build();
    }

    /**
     * Creates a successful response with data and message.
     * 
     * @param data The response data
     * @param message Success message
     * @param <T> The type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .version("3.0.0")
                .build();
    }

    /**
     * Creates a successful response with data, message, and metadata.
     * 
     * @param data The response data
     * @param message Success message
     * @param metadata Additional metadata
     * @param <T> The type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(Instant.now())
                .version("3.0.0")
                .build();
    }

    /**
     * Creates an error response.
     * 
     * @param code Error code
     * @param message Error message
     * @param <T> The type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .timestamp(Instant.now())
                        .build())
                .timestamp(Instant.now())
                .version("3.0.0")
                .build();
    }

    /**
     * Creates an error response with details.
     * 
     * @param code Error code
     * @param message Error message
     * @param details Additional error details
     * @param <T> The type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String code, String message, Map<String, Object> details) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .timestamp(Instant.now())
                        .build())
                .timestamp(Instant.now())
                .version("3.0.0")
                .build();
    }

    /**
     * Creates a validation error response.
     * 
     * @param validationErrors List of validation errors
     * @param <T> The type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> validationError(List<ValidationError> validationErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Validation failed")
                .error(ErrorInfo.builder()
                        .code("VALIDATION_FAILED")
                        .message("Request validation failed")
                        .validationErrors(validationErrors)
                        .timestamp(Instant.now())
                        .build())
                .timestamp(Instant.now())
                .version("3.0.0")
                .build();
    }

    /**
     * Error information structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        /**
         * Machine-readable error code.
         */
        private String code;

        /**
         * Human-readable error message.
         */
        private String message;

        /**
         * Additional error details.
         */
        private Map<String, Object> details;

        /**
         * List of validation errors.
         */
        private List<ValidationError> validationErrors;

        /**
         * Timestamp when the error occurred.
         */
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        private Instant timestamp;

        /**
         * Link to documentation about this error.
         */
        private String documentation;
    }

    /**
     * Validation error structure.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * Field that failed validation.
         */
        private String field;

        /**
         * Validation error code.
         */
        private String code;

        /**
         * Human-readable error message.
         */
        private String message;

        /**
         * The rejected value.
         */
        private Object rejectedValue;
    }
}
