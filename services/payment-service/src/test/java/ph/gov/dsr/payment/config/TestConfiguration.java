package ph.gov.dsr.payment.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import ph.gov.dsr.payment.dto.FSPPaymentRequest;
import ph.gov.dsr.payment.dto.FSPPaymentResponse;
import ph.gov.dsr.payment.dto.FSPStatusResponse;
import ph.gov.dsr.payment.entity.FSPConfiguration;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.service.FSPService;

import java.math.BigDecimal;
import java.util.*;

/**
 * Test configuration for Payment Service tests
 */
@org.springframework.boot.test.context.TestConfiguration
@Profile("test")
public class TestConfiguration {

    @Bean
    @Primary
    public FSPService mockLBPService() {
        return new MockFSPService("LBP", "Land Bank of the Philippines");
    }

    @Bean
    @Primary
    public FSPService mockBPIService() {
        return new MockFSPService("BPI", "Bank of the Philippine Islands");
    }

    /**
     * Mock FSP Service implementation for testing
     */
    public static class MockFSPService implements FSPService {
        
        private final String fspCode;
        private final String fspName;
        private boolean healthy = true;

        public MockFSPService(String fspCode, String fspName) {
            this.fspCode = fspCode;
            this.fspName = fspName;
        }

        @Override
        public String getFspCode() {
            return fspCode;
        }

        @Override
        public String getFspName() {
            return fspName;
        }

        @Override
        public Set<Payment.PaymentMethod> getSupportedPaymentMethods() {
            return Set.of(
                Payment.PaymentMethod.BANK_TRANSFER,
                Payment.PaymentMethod.CASH_PICKUP,
                Payment.PaymentMethod.CHECK
            );
        }

        @Override
        public boolean supportsAmount(BigDecimal amount) {
            // Support amounts up to 100,000
            return amount.compareTo(new BigDecimal("100000")) <= 0;
        }

        @Override
        public boolean isHealthy() {
            return healthy;
        }

        @Override
        public FSPPaymentResponse submitPayment(FSPPaymentRequest request, FSPConfiguration config) {
            if (!healthy) {
                throw new RuntimeException("FSP service is not healthy");
            }

            return FSPPaymentResponse.builder()
                .fspReferenceNumber("FSP-" + fspCode + "-" + System.currentTimeMillis())
                .status("SUCCESS")
                .message("Payment submitted successfully to " + fspName)
                .transactionId("TXN-" + System.currentTimeMillis())
                .build();
        }

        @Override
        public FSPStatusResponse checkPaymentStatus(String fspReferenceNumber, FSPConfiguration config) {
            if (!healthy) {
                throw new RuntimeException("FSP service is not healthy");
            }

            return FSPStatusResponse.builder()
                .fspReferenceNumber(fspReferenceNumber)
                .status(FSPPaymentResponse.FSPPaymentStatus.COMPLETED)
                .statusMessage("Payment completed successfully")
                .success(true)
                .build();
        }

        @Override
        public void performHealthCheck() {
            // Simulate health check - in real implementation this would ping the FSP
            try {
                Thread.sleep(100); // Simulate network call
                healthy = true;
            } catch (InterruptedException e) {
                healthy = false;
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public BigDecimal getTransactionFee(BigDecimal amount, Payment.PaymentMethod method) {
            // Mock transaction fee calculation
            if (method == Payment.PaymentMethod.BANK_TRANSFER) {
                return amount.multiply(new BigDecimal("0.01")); // 1% fee
            } else if (method == Payment.PaymentMethod.CASH) {
                return new BigDecimal("10.00"); // Flat fee for cash
            } else {
                return new BigDecimal("5.00"); // Default fee
            }
        }

        @Override
        public boolean canRetry(String fspReferenceNumber) {
            // Allow retries for mock service
            return true;
        }

        @Override
        public boolean validateConfiguration(FSPConfiguration config) {
            return config != null && fspCode.equals(config.getFspCode());
        }

        @Override
        public boolean testConnection(FSPConfiguration config) {
            return healthy;
        }

        @Override
        public BigDecimal getMinimumAmount() {
            return new BigDecimal("1.00");
        }

        @Override
        public BigDecimal getMaximumAmount() {
            return new BigDecimal("100000.00");
        }

        @Override
        public FSPPaymentResponse cancelPayment(String fspReferenceNumber, FSPConfiguration config) {
            return FSPPaymentResponse.builder()
                .fspReferenceNumber(fspReferenceNumber)
                .status(FSPPaymentResponse.FSPPaymentStatus.CANCELLED)
                .statusMessage("Payment cancelled")
                .success(true)
                .build();
        }

        @Override
        public void processWebhook(String payload, Map<String, String> headers) {
            // Mock webhook processing
        }

        // Test helper methods
        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }
    }
}
