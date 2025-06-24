package ph.gov.dsr.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ph.gov.dsr.payment.dto.PaymentBatchRequest;
import ph.gov.dsr.payment.dto.PaymentBatchResponse;
import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentBatch;
import ph.gov.dsr.payment.entity.PaymentAuditLog;
import ph.gov.dsr.payment.repository.PaymentBatchRepository;
import ph.gov.dsr.payment.repository.PaymentRepository;
import ph.gov.dsr.payment.repository.PaymentAuditLogRepository;
import ph.gov.dsr.payment.service.impl.PaymentBatchServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentBatchService
 */
@ExtendWith(MockitoExtension.class)
class PaymentBatchServiceTest {

    @Mock
    private PaymentBatchRepository batchRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAuditLogRepository auditLogRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentBatchServiceImpl paymentBatchService;

    private PaymentBatch testBatch;
    private PaymentBatchRequest testBatchRequest;
    private UUID testBatchId;
    private UUID testProgramId;

    @BeforeEach
    void setUp() {
        testBatchId = UUID.randomUUID();
        testProgramId = UUID.randomUUID();
        
        testBatch = PaymentBatch.builder()
            .batchId(testBatchId)
            .batchNumber("BATCH-2024-001")
            .programId(testProgramId)
            .programName("4Ps")
            .totalPayments(2)
            .totalAmount(new BigDecimal("2800.00"))
            .status(PaymentBatch.BatchStatus.PENDING)
            .createdBy("test-user")
            .createdDate(LocalDateTime.now())
            .build();

        List<PaymentRequest> paymentRequests = Arrays.asList(
            PaymentRequest.builder()
                .householdId("HH-001")
                .programName("4Ps")
                .amount(new BigDecimal("1400.00"))
                .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                .build(),
            PaymentRequest.builder()
                .householdId("HH-002")
                .programName("4Ps")
                .amount(new BigDecimal("1400.00"))
                .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
                .build()
        );

        testBatchRequest = PaymentBatchRequest.builder()
            .programId(testProgramId)
            .programName("4Ps")
            .paymentRequests(paymentRequests)
            .scheduledDate(LocalDateTime.now().plusDays(1))
            .metadata(new HashMap<>())
            .build();
    }

    @Test
    void createPaymentBatch_Success() {
        // Arrange
        when(batchRepository.save(any(PaymentBatch.class))).thenReturn(testBatch);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentBatchResponse result = paymentBatchService.createPaymentBatch(testBatchRequest, "test-user");

        // Assert
        assertNotNull(result);
        assertEquals(testBatchId, result.getBatchId());
        assertEquals("4Ps", result.getProgramName());
        assertEquals(2, result.getTotalPayments());
        assertEquals(new BigDecimal("2800.00"), result.getTotalAmount());

        verify(batchRepository).save(any(PaymentBatch.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void createPaymentBatch_EmptyPaymentRequests_ThrowsException() {
        // Arrange
        testBatchRequest.setPaymentRequests(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            paymentBatchService.createPaymentBatch(testBatchRequest, "test-user"));
    }

    @Test
    void getPaymentBatchById_Success() {
        // Arrange
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));

        // Act
        PaymentBatchResponse result = paymentBatchService.getPaymentBatchById(testBatchId);

        // Assert
        assertNotNull(result);
        assertEquals(testBatchId, result.getBatchId());
        assertEquals("4Ps", result.getProgramName());
    }

    @Test
    void getPaymentBatchById_NotFound_ThrowsException() {
        // Arrange
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            paymentBatchService.getPaymentBatchById(testBatchId));
    }

    @Test
    void getPaymentBatchesByProgramId_Success() {
        // Arrange
        List<PaymentBatch> batches = Arrays.asList(testBatch);
        Page<PaymentBatch> batchPage = new PageImpl<>(batches);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(batchRepository.findByProgramId(testProgramId, pageable)).thenReturn(batchPage);

        // Act
        Page<PaymentBatchResponse> result = paymentBatchService.getPaymentBatchesByProgramId(testProgramId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBatchId, result.getContent().get(0).getBatchId());
    }

    @Test
    void startBatchProcessing_Success() {
        // Arrange
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any(PaymentBatch.class))).thenReturn(testBatch);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentBatchResponse result = paymentBatchService.startBatchProcessing(testBatchId, "test-user");

        // Assert
        assertNotNull(result);
        verify(batchRepository).save(any(PaymentBatch.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void startBatchProcessing_BatchNotPending_ThrowsException() {
        // Arrange
        testBatch.setStatus(PaymentBatch.BatchStatus.PROCESSING);
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            paymentBatchService.startBatchProcessing(testBatchId, "test-user"));
    }

    @Test
    void updateBatchStatus_Success() {
        // Arrange
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any(PaymentBatch.class))).thenReturn(testBatch);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentBatchResponse result = paymentBatchService.updateBatchStatus(
            testBatchId, PaymentBatch.BatchStatus.COMPLETED, "Batch completed", "test-user");

        // Assert
        assertNotNull(result);
        verify(batchRepository).save(any(PaymentBatch.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void monitorBatchProgress_Success() {
        // Arrange
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(any(PaymentBatch.class))).thenReturn(testBatch);

        // Act
        PaymentBatchResponse result = paymentBatchService.monitorBatchProgress(testBatchId);

        // Assert
        assertNotNull(result);
        assertEquals(testBatchId, result.getBatchId());
    }

    @Test
    void getBatchStatistics_Success() {
        // Arrange
        List<Object[]> mockStats = Arrays.asList(
            new Object[]{PaymentBatch.BatchStatus.COMPLETED, 5L, new BigDecimal("70000.00")},
            new Object[]{PaymentBatch.BatchStatus.PENDING, 3L, new BigDecimal("42000.00")}
        );
        when(batchRepository.getBatchStatistics()).thenReturn(mockStats);

        // Act
        Map<String, Object> result = paymentBatchService.getBatchStatistics();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("COMPLETED"));
        assertTrue(result.containsKey("PENDING"));
    }

    @Test
    void getBatchStatisticsByProgram_Success() {
        // Arrange
        List<Object[]> mockStats = Arrays.asList(
            new Object[]{testProgramId, "4Ps", 5L, new BigDecimal("70000.00")},
            new Object[]{UUID.randomUUID(), "DSWD-SLP", 3L, new BigDecimal("42000.00")}
        );
        when(batchRepository.getBatchStatisticsByProgram()).thenReturn(mockStats);

        // Act
        Map<String, Object> result = paymentBatchService.getBatchStatisticsByProgram();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void canStartBatch_PendingBatch_ReturnsTrue() {
        // Arrange
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));

        // Act
        boolean result = paymentBatchService.canStartBatch(testBatchId);

        // Assert
        assertTrue(result);
    }

    @Test
    void canStartBatch_ProcessingBatch_ReturnsFalse() {
        // Arrange
        testBatch.setStatus(PaymentBatch.BatchStatus.PROCESSING);
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));

        // Act
        boolean result = paymentBatchService.canStartBatch(testBatchId);

        // Assert
        assertFalse(result);
    }

    @Test
    void generateBatchReport_Success() {
        // Arrange
        List<Payment> payments = Arrays.asList(
            Payment.builder()
                .paymentId(UUID.randomUUID())
                .amount(new BigDecimal("1400.00"))
                .status(Payment.PaymentStatus.COMPLETED)
                .build()
        );
        
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));
        when(paymentRepository.findByBatch_BatchId(testBatchId)).thenReturn(payments);

        // Act
        Map<String, Object> result = paymentBatchService.generateBatchReport(testBatchId);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("batchInfo"));
        assertTrue(result.containsKey("paymentSummary"));
        assertTrue(result.containsKey("fspBreakdown"));
        assertTrue(result.containsKey("generatedAt"));
    }
}
