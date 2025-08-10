package ph.gov.dsr.payment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import ph.gov.dsr.payment.dto.*;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentBatch;
import ph.gov.dsr.payment.service.PaymentService;
import ph.gov.dsr.payment.service.PaymentBatchService;
import ph.gov.dsr.payment.service.FSPServiceRegistry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for Payment Service with external services
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentBatchService paymentBatchService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private FSPServiceRegistry fspServiceRegistry;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Mock FSP service responses
        FSPPaymentResponse successResponse = FSPPaymentResponse.builder()
            .fspReferenceNumber("FSP-REF-001")
            .status(FSPPaymentResponse.FSPPaymentStatus.SUBMITTED)
            .statusMessage("Payment submitted successfully")
            .success(true)
            .build();

        when(fspServiceRegistry.submitPayment(anyString(), any())).thenReturn(successResponse);
        when(fspServiceRegistry.getBestFSP(any(), any())).thenReturn("MOCK_FSP");
    }

    @Test
    void testPaymentWorkflowWithDataManagementIntegration() {
        // Simulate Data Management service validation
        mockDataManagementHouseholdValidation("HH-001", true);

        // Create payment request
        PaymentRequest request = PaymentRequest.builder()
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("1234567890")
                .bankCode("LBP")
                .accountName("Juan Dela Cruz")
                .build())
            .build();

        // Test payment creation
        PaymentResponse payment = paymentService.createPayment(request, "test-user");
        assertNotNull(payment);
        assertEquals("HH-001", payment.getHouseholdId());
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());

        // Test payment processing
        PaymentResponse processedPayment = paymentService.processPayment(payment.getPaymentId(), "test-user");
        assertNotNull(processedPayment);
    }

    @Test
    void testPaymentBatchWithEligibilityIntegration() {
        // Simulate Eligibility service validation
        mockEligibilityServiceValidation("4Ps", true);

        // Create batch request with multiple payments
        List<PaymentRequest> paymentRequests = Arrays.asList(
            createTestPaymentRequest("HH-001"),
            createTestPaymentRequest("HH-002"),
            createTestPaymentRequest("HH-003")
        );

        PaymentBatchRequest batchRequest = PaymentBatchRequest.builder()
            .programId(UUID.randomUUID())
            .programName("4Ps")
            .paymentRequests(paymentRequests)
            .scheduledDate(LocalDateTime.now().plusDays(1))
            .metadata(new HashMap<>())
            .build();

        // Test batch creation
        PaymentBatchResponse batch = paymentBatchService.createPaymentBatch(batchRequest, "test-user");
        assertNotNull(batch);
        assertEquals(3, batch.getTotalPayments());
        assertEquals(PaymentBatch.BatchStatus.PENDING, batch.getStatus());

        // Test batch processing
        PaymentBatchResponse processingBatch = paymentBatchService.startBatchProcessing(batch.getBatchId(), "test-user");
        assertEquals(PaymentBatch.BatchStatus.PROCESSING, processingBatch.getStatus());
    }

    @Test
    void testCrossServiceErrorHandling() {
        // Simulate Data Management service failure
        mockDataManagementHouseholdValidation("HH-INVALID", false);

        PaymentRequest request = createTestPaymentRequest("HH-INVALID");

        // Should handle external service failures gracefully
        assertDoesNotThrow(() -> {
            PaymentResponse payment = paymentService.createPayment(request, "test-user");
            assertNotNull(payment);
        });
    }

    @Test
    void testFSPServiceIntegration() {
        // Test FSP service registry functionality
        when(fspServiceRegistry.getAllFSPServices()).thenReturn(Arrays.asList());
        when(fspServiceRegistry.getFSPHealthStatus()).thenReturn(Map.of("MOCK_FSP", true));

        PaymentRequest request = createTestPaymentRequest("HH-001");
        PaymentResponse payment = paymentService.createPayment(request, "test-user");

        // Test FSP submission
        PaymentResponse processedPayment = paymentService.processPayment(payment.getPaymentId(), "test-user");
        assertNotNull(processedPayment);

        // Verify FSP service was called
        verify(fspServiceRegistry, atLeastOnce()).submitPayment(anyString(), any());
    }

    @Test
    void testPaymentReconciliationWorkflow() {
        // Create multiple payments for reconciliation
        List<PaymentResponse> payments = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            PaymentRequest request = createTestPaymentRequest("HH-00" + i);
            PaymentResponse payment = paymentService.createPayment(request, "test-user");
            payments.add(payment);
        }

        // Test reconciliation process
        Map<String, Object> reconciliationResult = paymentService.reconcilePayments(
            "MOCK_FSP", 
            LocalDateTime.now().minusDays(1), 
            LocalDateTime.now().plusDays(1)
        );

        assertNotNull(reconciliationResult);
    }

    @Test
    void testPaymentStatisticsIntegration() {
        // Create test data
        createTestPayments(10);

        // Test statistics generation
        Map<String, Object> stats = paymentService.getPaymentStatistics();
        assertNotNull(stats);

        Map<String, Object> fspStats = paymentService.getPaymentStatisticsByFsp();
        assertNotNull(fspStats);

        List<Map<String, Object>> dailyVolume = paymentService.getDailyPaymentVolume(
            LocalDateTime.now().minusDays(7), 
            LocalDateTime.now()
        );
        assertNotNull(dailyVolume);
    }

    @Test
    void testBatchStatisticsIntegration() {
        // Create test batches
        createTestBatches(5);

        // Test batch statistics
        Map<String, Object> batchStats = paymentBatchService.getBatchStatistics();
        assertNotNull(batchStats);

        Map<String, Object> programStats = paymentBatchService.getBatchStatisticsByProgram();
        assertNotNull(programStats);
    }

    @Test
    void testEndToEndPaymentWorkflow() {
        // Simulate complete workflow from creation to completion
        mockDataManagementHouseholdValidation("HH-E2E", true);
        mockEligibilityServiceValidation("4Ps", true);

        // 1. Create payment
        PaymentRequest request = createTestPaymentRequest("HH-E2E");
        PaymentResponse payment = paymentService.createPayment(request, "test-user");

        // 2. Process payment
        PaymentResponse processedPayment = paymentService.processPayment(payment.getPaymentId(), "test-user");

        // 3. Check status
        PaymentResponse statusCheck = paymentService.checkPaymentStatusWithFSP(payment.getPaymentId());

        // 4. Complete payment
        PaymentResponse completedPayment = paymentService.updatePaymentStatus(
            payment.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, 
            "Payment completed successfully", 
            "test-user"
        );

        // Verify workflow
        assertEquals(Payment.PaymentStatus.COMPLETED, completedPayment.getStatus());
        assertNotNull(completedPayment.getCompletedDate());
    }

    // Helper methods
    private void mockDataManagementHouseholdValidation(String householdId, boolean isValid) {
        Map<String, Object> response = new HashMap<>();
        response.put("householdId", householdId);
        response.put("isValid", isValid);
        response.put("status", isValid ? "ACTIVE" : "INACTIVE");

        when(restTemplate.getForEntity(
            contains("/api/v1/households/" + householdId), 
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));
    }

    private void mockEligibilityServiceValidation(String programCode, boolean isEligible) {
        Map<String, Object> response = new HashMap<>();
        response.put("programCode", programCode);
        response.put("isEligible", isEligible);
        response.put("eligibilityScore", isEligible ? 85.5 : 45.0);

        when(restTemplate.postForEntity(
            contains("/api/v1/eligibility/assess"), 
            any(), 
            eq(Map.class)
        )).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));
    }

    private PaymentRequest createTestPaymentRequest(String householdId) {
        return PaymentRequest.builder()
            .householdId(householdId)
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("1234567890")
                .bankCode("LBP")
                .accountName("Test Beneficiary")
                .build())
            .build();
    }

    private void createTestPayments(int count) {
        for (int i = 1; i <= count; i++) {
            PaymentRequest request = createTestPaymentRequest("HH-TEST-" + i);
            paymentService.createPayment(request, "test-user");
        }
    }

    private void createTestBatches(int count) {
        for (int i = 1; i <= count; i++) {
            List<PaymentRequest> requests = Arrays.asList(
                createTestPaymentRequest("HH-BATCH-" + i + "-1"),
                createTestPaymentRequest("HH-BATCH-" + i + "-2")
            );

            PaymentBatchRequest batchRequest = PaymentBatchRequest.builder()
                .programId(UUID.randomUUID())
                .programName("4Ps")
                .paymentRequests(requests)
                .scheduledDate(LocalDateTime.now().plusDays(1))
                .metadata(new HashMap<>())
                .build();

            paymentBatchService.createPaymentBatch(batchRequest, "test-user");
        }
    }
}
