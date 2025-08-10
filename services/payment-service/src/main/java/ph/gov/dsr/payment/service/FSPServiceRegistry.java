package ph.gov.dsr.payment.service;

import ph.gov.dsr.payment.dto.FSPPaymentRequest;
import ph.gov.dsr.payment.dto.FSPPaymentResponse;
import ph.gov.dsr.payment.dto.FSPStatusResponse;
import ph.gov.dsr.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Registry for managing multiple FSP service integrations
 */
public interface FSPServiceRegistry {

    /**
     * Register an FSP service
     */
    void registerFSPService(FSPService fspService);

    /**
     * Get FSP service by code
     */
    FSPService getFSPService(String fspCode);

    /**
     * Get all registered FSP services
     */
    List<FSPService> getAllFSPServices();

    /**
     * Get healthy FSP services
     */
    List<FSPService> getHealthyFSPServices();

    /**
     * Submit payment to FSP
     */
    FSPPaymentResponse submitPayment(String fspCode, FSPPaymentRequest request);

    /**
     * Check payment status with FSP
     */
    FSPStatusResponse checkPaymentStatus(String fspCode, String fspReferenceNumber);

    /**
     * Cancel payment with FSP
     */
    FSPPaymentResponse cancelPayment(String fspCode, String fspReferenceNumber);

    /**
     * Check if FSP supports payment method
     */
    boolean supportsPaymentMethod(String fspCode, Payment.PaymentMethod paymentMethod);

    /**
     * Check if FSP supports amount
     */
    boolean supportsAmount(String fspCode, BigDecimal amount);

    /**
     * Get FSP health status
     */
    Map<String, Boolean> getFSPHealthStatus();

    /**
     * Perform health check on all FSPs
     */
    void performHealthCheck();

    /**
     * Get best FSP for payment method and amount
     */
    String getBestFSP(Payment.PaymentMethod paymentMethod, BigDecimal amount);
}
