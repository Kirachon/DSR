package ph.gov.dsr.interoperability.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * External System Integration entity for managing external system connections
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "external_system_integrations", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class ExternalSystemIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "system_code", unique = true, nullable = false, length = 50)
    private String systemCode;

    @NotBlank
    @Column(name = "system_name", nullable = false, length = 200)
    private String systemName;

    @Column(name = "system_description", columnDefinition = "TEXT")
    private String systemDescription;

    @NotBlank
    @Column(name = "organization", nullable = false, length = 200)
    private String organization; // DSWD, DOH, DepEd, BSP, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "system_type", nullable = false, length = 30)
    private SystemType systemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false, length = 30)
    private IntegrationType integrationType;

    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @Column(name = "api_version", length = 20)
    private String apiVersion;

    @Column(name = "authentication_type", length = 30)
    private String authenticationType; // API_KEY, OAUTH2, JWT, BASIC_AUTH, etc.

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "client_id", length = 200)
    private String clientId;

    @Column(name = "client_secret", length = 500)
    private String clientSecret;

    @Column(name = "token_endpoint", length = 500)
    private String tokenEndpoint;

    @Column(name = "scope", length = 200)
    private String scope;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 30;

    @Column(name = "retry_attempts")
    private Integer retryAttempts = 3;

    @Column(name = "retry_delay_seconds")
    private Integer retryDelaySeconds = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SystemStatus status;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_production")
    private Boolean isProduction = false;

    @Column(name = "environment", length = 20)
    private String environment = "DEVELOPMENT"; // DEVELOPMENT, STAGING, PRODUCTION

    @Column(name = "supported_operations", columnDefinition = "TEXT")
    private String supportedOperations; // JSON array of supported operations

    @Column(name = "data_formats", length = 100)
    private String dataFormats = "JSON"; // JSON, XML, CSV, etc.

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "rate_limit_per_hour")
    private Integer rateLimitPerHour;

    @Column(name = "rate_limit_per_day")
    private Integer rateLimitPerDay;

    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    @Column(name = "health_check_interval_minutes")
    private Integer healthCheckIntervalMinutes = 15;

    @Column(name = "last_successful_call")
    private LocalDateTime lastSuccessfulCall;

    @Column(name = "last_failed_call")
    private LocalDateTime lastFailedCall;

    @Column(name = "total_successful_calls")
    private Long totalSuccessfulCalls = 0L;

    @Column(name = "total_failed_calls")
    private Long totalFailedCalls = 0L;

    @Column(name = "average_response_time_ms")
    private Double averageResponseTimeMs;

    @Column(name = "uptime_percentage")
    private Double uptimePercentage;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "documentation_url", length = 500)
    private String documentationUrl;

    @Column(name = "sla_response_time_ms")
    private Integer slaResponseTimeMs;

    @Column(name = "sla_availability_percentage")
    private Double slaAvailabilityPercentage;

    @Column(name = "maintenance_window", length = 100)
    private String maintenanceWindow;

    @Column(name = "configuration", columnDefinition = "JSONB")
    private String configuration;

    @Column(name = "security_settings", columnDefinition = "JSONB")
    private String securitySettings;

    @Column(name = "monitoring_settings", columnDefinition = "JSONB")
    private String monitoringSettings;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum SystemType {
        GOVERNMENT_AGENCY,
        FINANCIAL_SERVICE_PROVIDER,
        IDENTITY_PROVIDER,
        NOTIFICATION_SERVICE,
        PAYMENT_GATEWAY,
        DATA_PROVIDER,
        MONITORING_SYSTEM,
        THIRD_PARTY_SERVICE
    }

    public enum IntegrationType {
        REST_API,
        SOAP_WEB_SERVICE,
        FILE_TRANSFER,
        DATABASE_SYNC,
        MESSAGE_QUEUE,
        WEBHOOK,
        BATCH_PROCESSING
    }

    public enum SystemStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        ERROR,
        DEPRECATED
    }

    // Constructors
    public ExternalSystemIntegration() {}

    public ExternalSystemIntegration(String systemCode, String systemName, String organization) {
        this.systemCode = systemCode;
        this.systemName = systemName;
        this.organization = organization;
        this.status = SystemStatus.INACTIVE;
    }

    // Helper methods
    
    /**
     * Check if system is healthy
     */
    public boolean isHealthy() {
        return status == SystemStatus.ACTIVE && 
               lastHealthCheck != null && 
               lastHealthCheck.isAfter(LocalDateTime.now().minusMinutes(healthCheckIntervalMinutes * 2));
    }

    /**
     * Check if system is available for calls
     */
    public boolean isAvailable() {
        return isActive && status == SystemStatus.ACTIVE && !isInMaintenanceWindow();
    }

    /**
     * Check if system is in maintenance window
     */
    public boolean isInMaintenanceWindow() {
        // Simple implementation - can be enhanced with actual maintenance window logic
        return status == SystemStatus.MAINTENANCE;
    }

    /**
     * Calculate success rate
     */
    public double getSuccessRate() {
        long totalCalls = totalSuccessfulCalls + totalFailedCalls;
        if (totalCalls == 0) {
            return 0.0;
        }
        return (double) totalSuccessfulCalls / totalCalls * 100.0;
    }

    /**
     * Update call statistics
     */
    public void recordSuccessfulCall(long responseTimeMs) {
        this.totalSuccessfulCalls++;
        this.lastSuccessfulCall = LocalDateTime.now();
        updateAverageResponseTime(responseTimeMs);
    }

    /**
     * Record failed call
     */
    public void recordFailedCall() {
        this.totalFailedCalls++;
        this.lastFailedCall = LocalDateTime.now();
    }

    /**
     * Update average response time
     */
    private void updateAverageResponseTime(long responseTimeMs) {
        if (averageResponseTimeMs == null) {
            averageResponseTimeMs = (double) responseTimeMs;
        } else {
            // Simple moving average
            averageResponseTimeMs = (averageResponseTimeMs * 0.9) + (responseTimeMs * 0.1);
        }
    }

    /**
     * Check if rate limit is exceeded
     */
    public boolean isRateLimitExceeded(int callsInLastMinute, int callsInLastHour, int callsInLastDay) {
        if (rateLimitPerMinute != null && callsInLastMinute >= rateLimitPerMinute) {
            return true;
        }
        if (rateLimitPerHour != null && callsInLastHour >= rateLimitPerHour) {
            return true;
        }
        if (rateLimitPerDay != null && callsInLastDay >= rateLimitPerDay) {
            return true;
        }
        return false;
    }

    /**
     * Get system health summary
     */
    public String getHealthSummary() {
        return String.format("System: %s, Status: %s, Success Rate: %.2f%%, Avg Response: %.0fms", 
                systemName, status, getSuccessRate(), 
                averageResponseTimeMs != null ? averageResponseTimeMs : 0.0);
    }
}
