package ph.gov.dsr.payment.service;

import ph.gov.dsr.payment.dto.FSPPaymentRequest;
import ph.gov.dsr.payment.dto.FSPPaymentResponse;
import ph.gov.dsr.payment.dto.FSPStatusResponse;
import ph.gov.dsr.payment.entity.FSPConfiguration;

/**
 * Interface for Financial Service Provider integrations
 */
public interface FSPService {

    /**
     * Get the FSP code this service handles
     */
    String getFspCode();

    /**
     * Check if the FSP service is available and healthy
     */
    boolean isHealthy();

    /**
     * Submit a payment to the FSP
     */
    FSPPaymentResponse submitPayment(FSPPaymentRequest request, FSPConfiguration config);

    /**
     * Check the status of a payment with the FSP
     */
    FSPStatusResponse checkPaymentStatus(String fspReferenceNumber, FSPConfiguration config);

    /**
     * Cancel a payment with the FSP (if supported)
     */
    FSPPaymentResponse cancelPayment(String fspReferenceNumber, FSPConfiguration config);

    /**
     * Validate FSP configuration
     */
    boolean validateConfiguration(FSPConfiguration config);

    /**
     * Test FSP connectivity
     */
    boolean testConnection(FSPConfiguration config);

    /**
     * Get supported payment methods for this FSP
     */
    java.util.Set<ph.gov.dsr.payment.entity.Payment.PaymentMethod> getSupportedPaymentMethods();

    /**
     * Get minimum amount supported by this FSP
     */
    java.math.BigDecimal getMinimumAmount();

    /**
     * Get maximum amount supported by this FSP
     */
    java.math.BigDecimal getMaximumAmount();

    /**
     * Check if amount is supported by this FSP
     */
    default boolean supportsAmount(java.math.BigDecimal amount) {
        java.math.BigDecimal min = getMinimumAmount();
        java.math.BigDecimal max = getMaximumAmount();
        
        if (min != null && amount.compareTo(min) < 0) {
            return false;
        }
        
        if (max != null && amount.compareTo(max) > 0) {
            return false;
        }
        
        return true;
    }

    /**
     * Process webhook notification from FSP
     */
    void processWebhook(String payload, java.util.Map<String, String> headers);
}
