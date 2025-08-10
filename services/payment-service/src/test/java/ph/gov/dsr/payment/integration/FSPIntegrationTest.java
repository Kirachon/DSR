package ph.gov.dsr.payment.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ph.gov.dsr.payment.dto.*;
import ph.gov.dsr.payment.entity.FSPConfiguration;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.repository.FSPConfigurationRepository;
import ph.gov.dsr.payment.service.FSPService;
import ph.gov.dsr.payment.service.FSPServiceRegistry;
import ph.gov.dsr.payment.service.impl.MockFSPService;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FSP (Financial Service Provider) integrations
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FSPIntegrationTest {

    @Autowired
    private FSPServiceRegistry fspServiceRegistry;

    @Autowired
    private FSPConfigurationRepository fspConfigurationRepository;

    private FSPConfiguration testFspConfig;
    private FSPPaymentRequest testPaymentRequest;

    @BeforeEach
    void setUp() {
        // Create test FSP configuration
        testFspConfig = FSPConfiguration.builder()
            .fspCode("TEST_FSP")
            .fspName("Test Financial Service Provider")
            .apiBaseUrl("https://api.test-fsp.com")
            .apiKey("test-api-key")
            .isActive(true)
            .connectionTimeout(30000)
            .readTimeout(60000)
            .maxRetryAttempts(3)
            .retryDelay(1000)
            .build();

        fspConfigurationRepository.save(testFspConfig);

        // Create test payment request
        testPaymentRequest = FSPPaymentRequest.builder()
            .paymentId(UUID.randomUUID())
            .internalReferenceNumber("PAY-TEST-001")
            .amount(new BigDecimal("1400.00"))
            .currency("PHP")
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .recipientAccountNumber("1234567890")
            .recipientAccountName("Test Beneficiary")
            .description("Test payment")
            .correlationId(UUID.randomUUID().toString())
            .build();
    }

    @Test
    void testFSPServiceRegistration() {
        // Create and register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Verify registration
        FSPService retrievedService = fspServiceRegistry.getFSPService("MOCK");
        assertNotNull(retrievedService);
        assertEquals("MOCK", retrievedService.getFspCode());
    }

    @Test
    void testFSPHealthChecks() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test health status
        Map<String, Boolean> healthStatus = fspServiceRegistry.getFSPHealthStatus();
        assertNotNull(healthStatus);
        assertTrue(healthStatus.containsKey("MOCK"));

        // Test individual FSP health
        List<FSPService> healthyServices = fspServiceRegistry.getHealthyFSPServices();
        assertNotNull(healthyServices);
        assertTrue(healthyServices.size() > 0);
    }

    @Test
    void testFSPPaymentSubmission() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test payment submission
        FSPPaymentResponse response = fspServiceRegistry.submitPayment("MOCK", testPaymentRequest);
        
        assertNotNull(response);
        assertNotNull(response.getFspReferenceNumber());
        assertTrue(response.isSuccess());
        assertEquals(FSPPaymentResponse.FSPPaymentStatus.SUBMITTED, response.getStatus());
    }

    @Test
    void testFSPPaymentStatusCheck() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Submit payment first
        FSPPaymentResponse submitResponse = fspServiceRegistry.submitPayment("MOCK", testPaymentRequest);
        String fspReferenceNumber = submitResponse.getFspReferenceNumber();

        // Check payment status
        FSPStatusResponse statusResponse = fspServiceRegistry.checkPaymentStatus("MOCK", fspReferenceNumber);
        
        assertNotNull(statusResponse);
        assertEquals(fspReferenceNumber, statusResponse.getFspReferenceNumber());
        assertTrue(statusResponse.isSuccess());
    }

    @Test
    void testFSPPaymentCancellation() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Submit payment first
        FSPPaymentResponse submitResponse = fspServiceRegistry.submitPayment("MOCK", testPaymentRequest);
        String fspReferenceNumber = submitResponse.getFspReferenceNumber();

        // Cancel payment
        FSPPaymentResponse cancelResponse = fspServiceRegistry.cancelPayment("MOCK", fspReferenceNumber);
        
        assertNotNull(cancelResponse);
        assertEquals(fspReferenceNumber, cancelResponse.getFspReferenceNumber());
        assertEquals(FSPPaymentResponse.FSPPaymentStatus.CANCELLED, cancelResponse.getStatus());
    }

    @Test
    void testFSPBestServiceSelection() {
        // Register multiple mock FSP services
        MockFSPService mockFsp1 = new MockFSPService();
        MockFSPService mockFsp2 = new MockFSPService();
        
        fspServiceRegistry.registerFSPService(mockFsp1);
        fspServiceRegistry.registerFSPService(mockFsp2);

        // Test best FSP selection
        String bestFsp = fspServiceRegistry.getBestFSP(
            Payment.PaymentMethod.BANK_TRANSFER, 
            new BigDecimal("1400.00")
        );
        
        assertNotNull(bestFsp);
        assertTrue(Arrays.asList("MOCK").contains(bestFsp));
    }

    @Test
    void testFSPConfigurationValidation() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test configuration validation
        boolean isValid = mockFspService.validateConfiguration(testFspConfig);
        assertFalse(isValid); // Should be false because FSP codes don't match

        // Test with matching configuration
        FSPConfiguration mockConfig = FSPConfiguration.builder()
            .fspCode("MOCK")
            .fspName("Mock FSP")
            .apiBaseUrl("https://mock.fsp.com")
            .apiKey("mock-key")
            .isActive(true)
            .build();

        boolean isValidMock = mockFspService.validateConfiguration(mockConfig);
        assertTrue(isValidMock);
    }

    @Test
    void testFSPConnectionTesting() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test connection
        boolean connectionTest = mockFspService.testConnection(testFspConfig);
        assertTrue(connectionTest); // Mock service should always return true
    }

    @Test
    void testFSPAmountLimits() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test amount limits
        BigDecimal minAmount = mockFspService.getMinimumAmount();
        BigDecimal maxAmount = mockFspService.getMaximumAmount();
        
        assertNotNull(minAmount);
        assertNotNull(maxAmount);
        assertTrue(minAmount.compareTo(maxAmount) < 0);

        // Test amount support
        assertTrue(mockFspService.supportsAmount(new BigDecimal("1400.00")));
        assertFalse(mockFspService.supportsAmount(new BigDecimal("0.50"))); // Below minimum
        assertFalse(mockFspService.supportsAmount(new BigDecimal("200000.00"))); // Above maximum
    }

    @Test
    void testFSPPaymentMethodSupport() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test supported payment methods
        Set<Payment.PaymentMethod> supportedMethods = mockFspService.getSupportedPaymentMethods();
        assertNotNull(supportedMethods);
        assertTrue(supportedMethods.contains(Payment.PaymentMethod.BANK_TRANSFER));

        // Test payment method support through registry
        boolean supportsMethod = fspServiceRegistry.supportsPaymentMethod(
            "MOCK", Payment.PaymentMethod.BANK_TRANSFER);
        assertTrue(supportsMethod);
    }

    @Test
    void testFSPTransactionFees() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test transaction fee calculation
        BigDecimal fee = mockFspService.getTransactionFee(
            new BigDecimal("1400.00"), 
            Payment.PaymentMethod.BANK_TRANSFER
        );
        
        assertNotNull(fee);
        assertTrue(fee.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testFSPRetryCapability() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Submit payment first
        FSPPaymentResponse submitResponse = fspServiceRegistry.submitPayment("MOCK", testPaymentRequest);
        String fspReferenceNumber = submitResponse.getFspReferenceNumber();

        // Test retry capability
        boolean canRetry = mockFspService.canRetry(fspReferenceNumber);
        assertTrue(canRetry); // Mock service should allow retries
    }

    @Test
    void testFSPWebhookProcessing() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test webhook processing
        String webhookPayload = "{\"paymentId\":\"PAY-001\",\"status\":\"COMPLETED\"}";
        Map<String, String> headers = Map.of("Content-Type", "application/json");

        // Should not throw exception
        assertDoesNotThrow(() -> {
            mockFspService.processWebhook(webhookPayload, headers);
        });
    }

    @Test
    void testFSPErrorHandling() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Test with invalid FSP code
        assertThrows(RuntimeException.class, () -> {
            fspServiceRegistry.submitPayment("INVALID_FSP", testPaymentRequest);
        });

        // Test with null payment request
        assertThrows(Exception.class, () -> {
            fspServiceRegistry.submitPayment("MOCK", null);
        });
    }

    @Test
    void testFSPPerformanceHealthCheck() {
        // Register mock FSP service
        MockFSPService mockFspService = new MockFSPService();
        fspServiceRegistry.registerFSPService(mockFspService);

        // Perform health check
        fspServiceRegistry.performHealthCheck();

        // Verify health status is updated
        Map<String, Boolean> healthStatus = fspServiceRegistry.getFSPHealthStatus();
        assertTrue(healthStatus.containsKey("MOCK"));
        assertTrue(healthStatus.get("MOCK"));
    }

    @Test
    void testMultipleFSPIntegration() {
        // Register multiple FSP services
        MockFSPService mockFsp1 = new MockFSPService();
        MockFSPService mockFsp2 = new MockFSPService();
        
        fspServiceRegistry.registerFSPService(mockFsp1);
        fspServiceRegistry.registerFSPService(mockFsp2);

        // Test getting all services
        List<FSPService> allServices = fspServiceRegistry.getAllFSPServices();
        assertTrue(allServices.size() >= 2);

        // Test getting healthy services
        List<FSPService> healthyServices = fspServiceRegistry.getHealthyFSPServices();
        assertTrue(healthyServices.size() >= 2);

        // Test health status for all
        Map<String, Boolean> healthStatus = fspServiceRegistry.getFSPHealthStatus();
        assertTrue(healthStatus.size() >= 2);
    }
}
