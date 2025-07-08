package ph.gov.dsr.interoperability.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API Gateway Response DTO for responses from external systems
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Builder
public class ApiGatewayResponse {

    private boolean success;

    private int statusCode;

    private Map<String, String> headers;

    private Object body;

    private String errorCode;

    private String errorMessage;

    private Long responseTime;

    private String systemCode;

    private String requestId;

    private String correlationId;

    private LocalDateTime timestamp;

    private Map<String, Object> metadata;

    // Helper methods
    
    /**
     * Check if response is successful
     */
    public boolean isSuccessful() {
        return success && statusCode >= 200 && statusCode < 300;
    }

    /**
     * Check if response is client error
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Check if response is server error
     */
    public boolean isServerError() {
        return statusCode >= 500;
    }

    /**
     * Get response time in milliseconds
     */
    public Long getResponseTimeMs() {
        return responseTime;
    }

    /**
     * Get response time in seconds
     */
    public Double getResponseTimeSeconds() {
        return responseTime != null ? responseTime / 1000.0 : null;
    }

    /**
     * Check if response has error
     */
    public boolean hasError() {
        return !success || errorCode != null || errorMessage != null;
    }

    /**
     * Get error summary
     */
    public String getErrorSummary() {
        if (!hasError()) {
            return null;
        }
        
        StringBuilder summary = new StringBuilder();
        if (errorCode != null) {
            summary.append(errorCode);
        }
        if (errorMessage != null) {
            if (summary.length() > 0) {
                summary.append(": ");
            }
            summary.append(errorMessage);
        }
        if (summary.length() == 0) {
            summary.append("HTTP ").append(statusCode);
        }
        
        return summary.toString();
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get response data (alias for body)
     */
    public Object getData() {
        return body;
    }
}
