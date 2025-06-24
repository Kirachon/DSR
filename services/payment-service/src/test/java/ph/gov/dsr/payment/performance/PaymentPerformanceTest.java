package ph.gov.dsr.payment.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Performance tests for Payment Service
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentPerformanceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentBatchService paymentBatchService;

    @MockBean
    private FSPServiceRegistry fspServiceRegistry;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);

        // Mock FSP service responses for performance testing
        FSPPaymentResponse successResponse = FSPPaymentResponse.builder()
            .fspReferenceNumber("FSP-REF-" + System.currentTimeMillis())
            .status(FSPPaymentResponse.FSPPaymentStatus.SUBMITTED)
            .statusMessage("Payment submitted successfully")
            .success(true)
            .build();

        when(fspServiceRegistry.submitPayment(anyString(), any())).thenReturn(successResponse);
        when(fspServiceRegistry.getBestFSP(any(), any())).thenReturn("MOCK_FSP");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSinglePaymentCreationPerformance() {
        // Test creating 100 payments sequentially
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            PaymentRequest request = createTestPaymentRequest("HH-PERF-" + i);
            PaymentResponse payment = paymentService.createPayment(request, "perf-test-user");
            assertNotNull(payment);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within 30 seconds (300ms per payment max)
        assertTrue(duration < 30000, "Payment creation took too long: " + duration + "ms");
        
        // Log performance metrics
        double avgTimePerPayment = (double) duration / 100;
        System.out.println("Average time per payment creation: " + avgTimePerPayment + "ms");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testConcurrentPaymentCreation() throws InterruptedException {
        int numberOfThreads = 10;
        int paymentsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<Future<List<PaymentResponse>>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Submit concurrent payment creation tasks
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            Future<List<PaymentResponse>> future = executorService.submit(() -> {
                List<PaymentResponse> payments = new ArrayList<>();
                try {
                    for (int j = 0; j < paymentsPerThread; j++) {
                        PaymentRequest request = createTestPaymentRequest("HH-CONC-" + threadId + "-" + j);
                        PaymentResponse payment = paymentService.createPayment(request, "perf-test-user");
                        payments.add(payment);
                    }
                } finally {
                    latch.countDown();
                }
                return payments;
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Verify all payments were created
        int totalPayments = 0;
        for (Future<List<PaymentResponse>> future : futures) {
            try {
                List<PaymentResponse> payments = future.get(5, TimeUnit.SECONDS);
                totalPayments += payments.size();
            } catch (Exception e) {
                fail("Concurrent payment creation failed: " + e.getMessage());
            }
        }

        assertEquals(numberOfThreads * paymentsPerThread, totalPayments);
        
        long duration = endTime - startTime;
        double avgTimePerPayment = (double) duration / totalPayments;
        System.out.println("Concurrent payment creation - Total time: " + duration + "ms, Avg per payment: " + avgTimePerPayment + "ms");
    }

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void testBatchPaymentCreationPerformance() {
        // Test creating batches with different sizes
        int[] batchSizes = {10, 50, 100, 200};
        
        for (int batchSize : batchSizes) {
            long startTime = System.currentTimeMillis();
            
            List<PaymentRequest> paymentRequests = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                paymentRequests.add(createTestPaymentRequest("HH-BATCH-" + batchSize + "-" + i));
            }

            PaymentBatchRequest batchRequest = PaymentBatchRequest.builder()
                .programId(UUID.randomUUID())
                .programName("4Ps")
                .paymentRequests(paymentRequests)
                .scheduledDate(LocalDateTime.now().plusDays(1))
                .metadata(new HashMap<>())
                .build();

            PaymentBatchResponse batch = paymentBatchService.createPaymentBatch(batchRequest, "perf-test-user");
            assertNotNull(batch);
            assertEquals(batchSize, batch.getTotalPayments());
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double avgTimePerPayment = (double) duration / batchSize;
            
            System.out.println("Batch size " + batchSize + " - Total time: " + duration + "ms, Avg per payment: " + avgTimePerPayment + "ms");
            
            // Performance threshold: should not exceed 50ms per payment in batch
            assertTrue(avgTimePerPayment < 50, "Batch creation too slow for size " + batchSize + ": " + avgTimePerPayment + "ms per payment");
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testPaymentProcessingPerformance() {
        // Create payments first
        List<PaymentResponse> payments = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PaymentRequest request = createTestPaymentRequest("HH-PROC-" + i);
            PaymentResponse payment = paymentService.createPayment(request, "perf-test-user");
            payments.add(payment);
        }

        // Test processing performance
        long startTime = System.currentTimeMillis();
        
        for (PaymentResponse payment : payments) {
            PaymentResponse processedPayment = paymentService.processPayment(payment.getPaymentId(), "perf-test-user");
            assertNotNull(processedPayment);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double avgTimePerProcessing = (double) duration / payments.size();
        
        System.out.println("Payment processing - Total time: " + duration + "ms, Avg per payment: " + avgTimePerProcessing + "ms");
        
        // Should process payments efficiently
        assertTrue(avgTimePerProcessing < 200, "Payment processing too slow: " + avgTimePerProcessing + "ms per payment");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testConcurrentBatchProcessing() throws InterruptedException {
        int numberOfBatches = 5;
        int paymentsPerBatch = 20;
        CountDownLatch latch = new CountDownLatch(numberOfBatches);
        List<Future<PaymentBatchResponse>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Create and process batches concurrently
        for (int i = 0; i < numberOfBatches; i++) {
            final int batchId = i;
            Future<PaymentBatchResponse> future = executorService.submit(() -> {
                try {
                    // Create batch
                    List<PaymentRequest> paymentRequests = new ArrayList<>();
                    for (int j = 0; j < paymentsPerBatch; j++) {
                        paymentRequests.add(createTestPaymentRequest("HH-CONC-BATCH-" + batchId + "-" + j));
                    }

                    PaymentBatchRequest batchRequest = PaymentBatchRequest.builder()
                        .programId(UUID.randomUUID())
                        .programName("4Ps")
                        .paymentRequests(paymentRequests)
                        .scheduledDate(LocalDateTime.now().plusDays(1))
                        .metadata(new HashMap<>())
                        .build();

                    PaymentBatchResponse batch = paymentBatchService.createPaymentBatch(batchRequest, "perf-test-user");
                    
                    // Start processing
                    return paymentBatchService.startBatchProcessing(batch.getBatchId(), "perf-test-user");
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // Wait for completion
        latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        // Verify all batches were processed
        for (Future<PaymentBatchResponse> future : futures) {
            try {
                PaymentBatchResponse batch = future.get(5, TimeUnit.SECONDS);
                assertNotNull(batch);
                assertEquals(PaymentBatch.BatchStatus.PROCESSING, batch.getStatus());
            } catch (Exception e) {
                fail("Concurrent batch processing failed: " + e.getMessage());
            }
        }

        long duration = endTime - startTime;
        System.out.println("Concurrent batch processing - Total time: " + duration + "ms for " + numberOfBatches + " batches");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testPaymentStatisticsPerformance() {
        // Create test data
        createTestPayments(100);

        // Test statistics generation performance
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> stats = paymentService.getPaymentStatistics();
        Map<String, Object> fspStats = paymentService.getPaymentStatisticsByFsp();
        List<Map<String, Object>> dailyVolume = paymentService.getDailyPaymentVolume(
            LocalDateTime.now().minusDays(7), 
            LocalDateTime.now()
        );
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull(stats);
        assertNotNull(fspStats);
        assertNotNull(dailyVolume);
        
        // Statistics should be generated quickly
        assertTrue(duration < 5000, "Statistics generation too slow: " + duration + "ms");
        System.out.println("Statistics generation time: " + duration + "ms");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testDatabaseQueryPerformance() {
        // Create test data
        createTestPayments(200);

        // Test various query performance
        long startTime = System.currentTimeMillis();
        
        // Test pagination performance
        for (int page = 0; page < 10; page++) {
            paymentService.searchPayments(
                null, null, null, null, 
                LocalDateTime.now().minusDays(1), 
                LocalDateTime.now(),
                org.springframework.data.domain.PageRequest.of(page, 20)
            );
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Pagination queries should be efficient
        assertTrue(duration < 10000, "Database queries too slow: " + duration + "ms");
        System.out.println("Database query performance: " + duration + "ms for 10 paginated queries");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testMemoryUsageUnderLoad() {
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create a large number of payments to test memory usage
        List<PaymentResponse> payments = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            PaymentRequest request = createTestPaymentRequest("HH-MEM-" + i);
            PaymentResponse payment = paymentService.createPayment(request, "perf-test-user");
            payments.add(payment);
            
            // Force garbage collection every 100 payments
            if (i % 100 == 0) {
                System.gc();
            }
        }
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.println("Memory usage increase: " + (memoryIncrease / 1024 / 1024) + " MB for 500 payments");
        
        // Memory increase should be reasonable (less than 100MB for 500 payments)
        assertTrue(memoryIncrease < 100 * 1024 * 1024, "Memory usage too high: " + (memoryIncrease / 1024 / 1024) + " MB");
    }

    // Helper methods
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
        for (int i = 0; i < count; i++) {
            PaymentRequest request = createTestPaymentRequest("HH-TEST-" + i);
            paymentService.createPayment(request, "perf-test-user");
        }
    }
}
