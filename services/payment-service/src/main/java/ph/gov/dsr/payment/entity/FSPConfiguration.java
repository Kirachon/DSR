package ph.gov.dsr.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FSPConfiguration entity for Financial Service Provider configurations
 */
@Entity
@Table(name = "fsp_configurations", indexes = {
    @Index(name = "idx_fsp_code", columnList = "fsp_code", unique = true),
    @Index(name = "idx_fsp_active", columnList = "is_active"),
    @Index(name = "idx_fsp_payment_method", columnList = "payment_method")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FSPConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "config_id")
    private UUID configId;

    @Column(name = "fsp_code", nullable = false, unique = true, length = 20)
    private String fspCode;

    @Column(name = "fsp_name", nullable = false, length = 100)
    private String fspName;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private Payment.PaymentMethod paymentMethod;

    @Column(name = "api_base_url", nullable = false, length = 500)
    private String apiBaseUrl;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "api_secret", length = 500)
    private String apiSecret;

    @Column(name = "client_id", length = 200)
    private String clientId;

    @Column(name = "client_secret", length = 500)
    private String clientSecret;

    @Column(name = "merchant_id", length = 100)
    private String merchantId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_sandbox", nullable = false)
    private Boolean isSandbox = false;

    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "daily_limit", precision = 15, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "transaction_fee", precision = 10, scale = 4)
    private BigDecimal transactionFee;

    @Column(name = "fee_type", length = 20)
    private String feeType = "FIXED"; // FIXED, PERCENTAGE

    @Column(name = "timeout_seconds", nullable = false)
    private Integer timeoutSeconds = 30;

    @Column(name = "retry_attempts", nullable = false)
    private Integer retryAttempts = 3;

    @Column(name = "retry_delay_seconds", nullable = false)
    private Integer retryDelaySeconds = 5;

    @Column(name = "supported_currencies", length = 100)
    private String supportedCurrencies = "PHP";

    @Column(name = "configuration_json", columnDefinition = "jsonb")
    private String configurationJson;

    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    @Column(name = "health_status", length = 20)
    private String healthStatus = "UNKNOWN";

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Business methods
    public boolean isHealthy() {
        return "HEALTHY".equals(healthStatus);
    }

    public boolean supportsAmount(BigDecimal amount) {
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            return false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        return true;
    }

    public boolean supportsCurrency(String currency) {
        return supportedCurrencies != null && 
               supportedCurrencies.contains(currency);
    }

    public BigDecimal calculateFee(BigDecimal amount) {
        if (transactionFee == null) {
            return BigDecimal.ZERO;
        }
        
        if ("PERCENTAGE".equals(feeType)) {
            return amount.multiply(transactionFee).divide(BigDecimal.valueOf(100));
        } else {
            return transactionFee;
        }
    }

    public boolean isOperational() {
        return isActive && isHealthy();
    }
}
