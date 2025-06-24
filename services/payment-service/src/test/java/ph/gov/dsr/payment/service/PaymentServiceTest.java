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

import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.dto.PaymentResponse;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentAuditLog;
import ph.gov.dsr.payment.repository.PaymentRepository;
import ph.gov.dsr.payment.repository.PaymentAuditLogRepository;
import ph.gov.dsr.payment.service.impl.PaymentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAuditLogRepository auditLogRepository;

    @Mock
    private FSPServiceRegistry fspServiceRegistry;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private PaymentRequest testPaymentRequest;
    private UUID testPaymentId;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        
        testPayment = Payment.builder()
            .paymentId(testPaymentId)
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .status(Payment.PaymentStatus.PENDING)
            .internalReferenceNumber("PAY-2024-001")
            .createdBy("test-user")
            .createdDate(LocalDateTime.now())
            .build();

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
    }

    @Test
    void createPayment_Success() {
        // Arrange
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentResponse result = paymentService.createPayment(testPaymentRequest, "test-user");

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentId, result.getPaymentId());
        assertEquals("HH-001", result.getHouseholdId());
        assertEquals(new BigDecimal("1400.00"), result.getAmount());
        assertEquals(Payment.PaymentStatus.PENDING, result.getStatus());

        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void createPayment_InvalidRequest_ThrowsException() {
        // Arrange
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .householdId("")
            .amount(BigDecimal.ZERO)
            .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.createPayment(invalidRequest, "test-user"));
    }

    @Test
    void getPaymentById_Success() {
        // Arrange
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        // Act
        PaymentResponse result = paymentService.getPaymentById(testPaymentId);

        // Assert
        assertNotNull(result);
        assertEquals(testPaymentId, result.getPaymentId());
        assertEquals("HH-001", result.getHouseholdId());
    }

    @Test
    void getPaymentById_NotFound_ThrowsException() {
        // Arrange
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            paymentService.getPaymentById(testPaymentId));
    }

    @Test
    void getPaymentsByHouseholdId_Success() {
        // Arrange
        UUID householdId = UUID.randomUUID();
        List<Payment> payments = Arrays.asList(testPayment);
        Page<Payment> paymentPage = new PageImpl<>(payments);
        Pageable pageable = PageRequest.of(0, 10);

        when(paymentRepository.findByHouseholdId(householdId, pageable)).thenReturn(paymentPage);

        // Act
        Page<PaymentResponse> result = paymentService.getPaymentsByHouseholdId(householdId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPaymentId, result.getContent().get(0).getPaymentId());
    }

    @Test
    void updatePaymentStatus_Success() {
        // Arrange
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentResponse result = paymentService.updatePaymentStatus(
            testPaymentId, Payment.PaymentStatus.COMPLETED, "Payment completed", "test-user");

        // Assert
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void cancelPayment_Success() {
        // Arrange
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentResponse result = paymentService.cancelPayment(testPaymentId, "User cancelled", "test-user");

        // Assert
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void validatePaymentRequest_ValidRequest_NoException() {
        // Act & Assert
        assertDoesNotThrow(() -> paymentService.validatePaymentRequest(testPaymentRequest));
    }

    @Test
    void validatePaymentRequest_InvalidAmount_ThrowsException() {
        // Arrange
        testPaymentRequest.setAmount(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            paymentService.validatePaymentRequest(testPaymentRequest));
    }

    @Test
    void getPaymentStatistics_Success() {
        // Arrange
        List<Object[]> mockStats = Arrays.asList(
            new Object[]{Payment.PaymentStatus.COMPLETED, 10L, new BigDecimal("14000.00")},
            new Object[]{Payment.PaymentStatus.PENDING, 5L, new BigDecimal("7000.00")}
        );
        when(paymentRepository.getPaymentStatistics()).thenReturn(mockStats);

        // Act
        Map<String, Object> result = paymentService.getPaymentStatistics();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("COMPLETED"));
        assertTrue(result.containsKey("PENDING"));
    }

    @Test
    void retryPayment_Success() {
        // Arrange
        testPayment.setStatus(Payment.PaymentStatus.FAILED);
        testPayment.setRetryCount(1);
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(auditLogRepository.save(any(PaymentAuditLog.class))).thenReturn(new PaymentAuditLog());

        // Act
        PaymentResponse result = paymentService.retryPayment(testPaymentId, "test-user");

        // Assert
        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
        verify(auditLogRepository).save(any(PaymentAuditLog.class));
    }

    @Test
    void retryPayment_ExceededMaxRetries_ThrowsException() {
        // Arrange
        testPayment.setStatus(Payment.PaymentStatus.FAILED);
        testPayment.setRetryCount(5); // Assuming max retries is 3
        when(paymentRepository.findById(testPaymentId)).thenReturn(Optional.of(testPayment));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            paymentService.retryPayment(testPaymentId, "test-user"));
    }
}
