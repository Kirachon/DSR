package ph.gov.dsr.payment.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
 * End-to-end integration tests for payment workflows
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentWorkflowIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentBatchService paymentBatchService;

    @MockBean
    private FSPServiceRegistry fspServiceRegistry;

    private PaymentRequest testPaymentRequest;
    private PaymentBatchRequest testBatchRequest;

    @BeforeEach
    void setUp() {
        testPaymentRequest = PaymentRequest.builder()
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

        List<PaymentRequest> paymentRequests = Arrays.asList(
            testPaymentRequest,
            PaymentRequest.builder()
                .householdId("HH-002")
                .programName("4Ps")
                .amount(new BigDecimal("1400.00"))
                .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                    .accountNumber("0987654321")
                    .bankCode("BPI")
                    .accountName("Maria Santos")
                    .build())
                .build()
        );

        testBatchRequest = PaymentBatchRequest.builder()
            .programId(UUID.randomUUID())
            .programName("4Ps")
            .paymentRequests(paymentRequests)
            .scheduledDate(LocalDateTime.now().plusDays(1))
            .metadata(new HashMap<>())
            .build();

        // Mock FSP service responses
        FSPPaymentResponse successResponse = FSPPaymentResponse.builder()
            .fspReferenceNumber("FSP-REF-001")
            .status("SUCCESS")
            .message("Payment submitted successfully")
            .build();

        when(fspServiceRegistry.getBestFSP(any(), any())).thenReturn("LBP");
        when(fspServiceRegistry.submitPayment(anyString(), any())).thenReturn(successResponse);
        when(fspServiceRegistry.checkPaymentStatus(anyString(), anyString())).thenReturn(successResponse);
    }

    @Test
    void completePaymentWorkflow_SinglePayment_Success() {
        // Step 1: Create payment
        PaymentResponse createdPayment = paymentService.createPayment(testPaymentRequest, "test-user");
        
        assertNotNull(createdPayment);
        assertEquals("HH-001", createdPayment.getHouseholdId());
        assertEquals(Payment.PaymentStatus.PENDING, createdPayment.getStatus());

        // Step 2: Process payment
        PaymentResponse processedPayment = paymentService.processPayment(
            createdPayment.getPaymentId(), "test-user");
        
        assertNotNull(processedPayment);
        assertEquals(createdPayment.getPaymentId(), processedPayment.getPaymentId());

        // Step 3: Update payment status to completed
        PaymentResponse completedPayment = paymentService.updatePaymentStatus(
            createdPayment.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, 
            "Payment completed successfully", 
            "test-user");
        
        assertNotNull(completedPayment);
        assertEquals(Payment.PaymentStatus.COMPLETED, completedPayment.getStatus());

        // Step 4: Verify payment can be retrieved
        PaymentResponse retrievedPayment = paymentService.getPaymentById(createdPayment.getPaymentId());
        assertEquals(Payment.PaymentStatus.COMPLETED, retrievedPayment.getStatus());
    }

    @Test
    void completePaymentBatchWorkflow_Success() {
        // Step 1: Create payment batch
        PaymentBatchResponse createdBatch = paymentBatchService.createPaymentBatch(testBatchRequest, "test-user");
        
        assertNotNull(createdBatch);
        assertEquals("4Ps", createdBatch.getProgramName());
        assertEquals(2, createdBatch.getTotalPayments());
        assertEquals(PaymentBatch.BatchStatus.PENDING, createdBatch.getStatus());

        // Step 2: Start batch processing
        PaymentBatchResponse processingBatch = paymentBatchService.startBatchProcessing(
            createdBatch.getBatchId(), "test-user");
        
        assertNotNull(processingBatch);
        assertEquals(PaymentBatch.BatchStatus.PROCESSING, processingBatch.getStatus());

        // Step 3: Monitor batch progress
        PaymentBatchResponse progressBatch = paymentBatchService.monitorBatchProgress(
            createdBatch.getBatchId());
        
        assertNotNull(progressBatch);
        assertEquals(createdBatch.getBatchId(), progressBatch.getBatchId());

        // Step 4: Complete batch processing
        PaymentBatchResponse completedBatch = paymentBatchService.updateBatchStatus(
            createdBatch.getBatchId(), 
            PaymentBatch.BatchStatus.COMPLETED, 
            "Batch processing completed", 
            "test-user");
        
        assertNotNull(completedBatch);
        assertEquals(PaymentBatch.BatchStatus.COMPLETED, completedBatch.getStatus());

        // Step 5: Generate batch report
        Map<String, Object> report = paymentBatchService.generateBatchReport(createdBatch.getBatchId());
        
        assertNotNull(report);
        assertTrue(report.containsKey("batchInfo"));
        assertTrue(report.containsKey("paymentSummary"));
        assertTrue(report.containsKey("generatedAt"));
    }

    @Test
    void paymentRetryWorkflow_Success() {
        // Step 1: Create payment
        PaymentResponse createdPayment = paymentService.createPayment(testPaymentRequest, "test-user");
        
        // Step 2: Simulate payment failure
        PaymentResponse failedPayment = paymentService.updatePaymentStatus(
            createdPayment.getPaymentId(), 
            Payment.PaymentStatus.FAILED, 
            "FSP service unavailable", 
            "system");
        
        assertEquals(Payment.PaymentStatus.FAILED, failedPayment.getStatus());

        // Step 3: Retry payment
        PaymentResponse retriedPayment = paymentService.retryPayment(
            createdPayment.getPaymentId(), "test-user");
        
        assertNotNull(retriedPayment);
        assertEquals(createdPayment.getPaymentId(), retriedPayment.getPaymentId());

        // Step 4: Complete retried payment
        PaymentResponse completedPayment = paymentService.updatePaymentStatus(
            createdPayment.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, 
            "Payment completed on retry", 
            "test-user");
        
        assertEquals(Payment.PaymentStatus.COMPLETED, completedPayment.getStatus());
    }

    @Test
    void paymentCancellationWorkflow_Success() {
        // Step 1: Create payment
        PaymentResponse createdPayment = paymentService.createPayment(testPaymentRequest, "test-user");
        
        // Step 2: Cancel payment
        PaymentResponse cancelledPayment = paymentService.cancelPayment(
            createdPayment.getPaymentId(), "User requested cancellation", "test-user");
        
        assertNotNull(cancelledPayment);
        assertEquals(Payment.PaymentStatus.CANCELLED, cancelledPayment.getStatus());

        // Step 3: Verify cancelled payment cannot be processed
        assertThrows(RuntimeException.class, () -> 
            paymentService.processPayment(createdPayment.getPaymentId(), "test-user"));
    }

    @Test
    void batchRetryFailedPaymentsWorkflow_Success() {
        // Step 1: Create batch with payments
        PaymentBatchResponse createdBatch = paymentBatchService.createPaymentBatch(testBatchRequest, "test-user");
        
        // Step 2: Start batch processing
        paymentBatchService.startBatchProcessing(createdBatch.getBatchId(), "test-user");

        // Step 3: Simulate some payments failing (this would normally happen during processing)
        // For this test, we'll assume the batch has failed payments

        // Step 4: Retry failed payments in batch
        PaymentBatchResponse retriedBatch = paymentBatchService.retryFailedPaymentsInBatch(
            createdBatch.getBatchId(), "test-user");
        
        assertNotNull(retriedBatch);
        assertEquals(createdBatch.getBatchId(), retriedBatch.getBatchId());
    }

    @Test
    void paymentStatisticsWorkflow_Success() {
        // Step 1: Create multiple payments with different statuses
        PaymentResponse payment1 = paymentService.createPayment(testPaymentRequest, "test-user");
        
        PaymentRequest request2 = PaymentRequest.builder()
            .householdId("HH-003")
            .programName("DSWD-SLP")
            .amount(new BigDecimal("2000.00"))
            .paymentMethod(Payment.PaymentMethod.CASH)
            .build();
        PaymentResponse payment2 = paymentService.createPayment(request2, "test-user");

        // Step 2: Update payment statuses
        paymentService.updatePaymentStatus(payment1.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, "Completed", "test-user");
        paymentService.updatePaymentStatus(payment2.getPaymentId(), 
            Payment.PaymentStatus.FAILED, "Failed", "test-user");

        // Step 3: Get payment statistics
        Map<String, Object> statistics = paymentService.getPaymentStatistics();
        
        assertNotNull(statistics);
        // Statistics should contain data for different payment statuses
        assertTrue(statistics.size() > 0);
    }

    @Test
    void batchStatisticsWorkflow_Success() {
        // Step 1: Create multiple batches
        PaymentBatchResponse batch1 = paymentBatchService.createPaymentBatch(testBatchRequest, "test-user");
        
        // Create another batch request
        PaymentBatchRequest request2 = PaymentBatchRequest.builder()
            .programId(UUID.randomUUID())
            .programName("DSWD-SLP")
            .paymentRequests(Arrays.asList(testPaymentRequest))
            .scheduledDate(LocalDateTime.now().plusDays(2))
            .metadata(new HashMap<>())
            .build();
        PaymentBatchResponse batch2 = paymentBatchService.createPaymentBatch(request2, "test-user");

        // Step 2: Update batch statuses
        paymentBatchService.updateBatchStatus(batch1.getBatchId(), 
            PaymentBatch.BatchStatus.COMPLETED, "Completed", "test-user");
        paymentBatchService.updateBatchStatus(batch2.getBatchId(), 
            PaymentBatch.BatchStatus.PROCESSING, "Processing", "test-user");

        // Step 3: Get batch statistics
        Map<String, Object> batchStats = paymentBatchService.getBatchStatistics();
        Map<String, Object> programStats = paymentBatchService.getBatchStatisticsByProgram();
        
        assertNotNull(batchStats);
        assertNotNull(programStats);
        assertTrue(batchStats.size() > 0);
        assertTrue(programStats.size() > 0);
    }
}
