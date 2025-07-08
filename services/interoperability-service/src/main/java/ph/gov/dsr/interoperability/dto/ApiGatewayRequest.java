package ph.gov.dsr.interoperability.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * API Gateway Request DTO for routing requests to external systems
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiGatewayRequest {

    @NotBlank(message = "System code is required")
    private String systemCode;

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    @NotBlank(message = "HTTP method is required")
    @Builder.Default
    private String method = "GET";

    private Map<String, String> headers;

    private Object body;

    private Map<String, String> queryParameters;

    private Integer timeoutSeconds;

    @Builder.Default
    private Boolean retryOnFailure = false;

    private String requestId;

    private String userId;

    private String correlationId;

    // Constructors (removed to avoid conflict with @Builder)

    // Helper methods
    
    /**
     * Add header to request
     */
    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new java.util.HashMap<>();
        }
        headers.put(key, value);
    }

    /**
     * Add query parameter
     */
    public void addQueryParameter(String key, String value) {
        if (queryParameters == null) {
            queryParameters = new java.util.HashMap<>();
        }
        queryParameters.put(key, value);
    }

    /**
     * Check if request has body
     */
    public boolean hasBody() {
        return body != null;
    }

    /**
     * Check if request has headers
     */
    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    /**
     * Check if request has query parameters
     */
    public boolean hasQueryParameters() {
        return queryParameters != null && !queryParameters.isEmpty();
    }
}
