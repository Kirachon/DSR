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
import java.util.Set;

/**
 * Test configuration for Payment Service tests
 */
@TestConfiguration
@Profile("test")
public class PaymentServiceTestConfiguration {

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
        public boolean isHealthy() {
            return healthy;
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
        public FSPPaymentResponse submitPayment(FSPPaymentRequest request, FSPConfiguration config) {
            if (!healthy) {
                throw new RuntimeException("FSP service is not healthy");
            }

            return FSPPaymentResponse.builder()
                .success(true)
                .fspReferenceNumber("FSP-" + fspCode + "-" + System.currentTimeMillis())
                .status(FSPPaymentResponse.FSPPaymentStatus.SUBMITTED)
                .statusMessage("Payment submitted successfully to " + fspName)
                .transactionId("TXN-" + System.currentTimeMillis())
                .build();
        }

        @Override
        public FSPStatusResponse checkPaymentStatus(String fspReferenceNumber, FSPConfiguration config) {
            if (!healthy) {
                throw new RuntimeException("FSP service is not healthy");
            }

            return FSPStatusResponse.builder()
                .success(true)
                .fspReferenceNumber(fspReferenceNumber)
                .status(FSPPaymentResponse.FSPPaymentStatus.COMPLETED)
                .statusMessage("Payment completed successfully")
                .build();
        }

        @Override
        public FSPPaymentResponse cancelPayment(String fspReferenceNumber, FSPConfiguration config) {
            return FSPPaymentResponse.builder()
                .success(true)
                .fspReferenceNumber(fspReferenceNumber)
                .status(FSPPaymentResponse.FSPPaymentStatus.CANCELLED)
                .statusMessage("Payment cancelled successfully")
                .build();
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
        public void processWebhook(String payload, java.util.Map<String, String> headers) {
            // Mock webhook processing - in real implementation would parse payload
        }

        // Test helper methods
        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }
    }
}
