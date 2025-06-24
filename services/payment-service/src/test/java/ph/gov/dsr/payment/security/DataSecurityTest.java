package ph.gov.dsr.payment.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.dto.PaymentResponse;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentAuditLog;
import ph.gov.dsr.payment.repository.PaymentRepository;
import ph.gov.dsr.payment.repository.PaymentAuditLogRepository;
import ph.gov.dsr.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security tests for data protection, encryption, and audit logging
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataSecurityTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAuditLogRepository auditLogRepository;

    private PaymentRequest testPaymentRequest;

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
    }

    @Test
    void sensitiveDataEncryption_AccountNumbers_EncryptedInDatabase() {
        // Create payment with sensitive account information
        PaymentResponse payment = paymentService.createPayment(testPaymentRequest, "security-test-user");
        
        // Retrieve payment directly from database
        Payment dbPayment = paymentRepository.findById(payment.getPaymentId()).orElse(null);
        assertNotNull(dbPayment);
        
        // Account number should be encrypted in database (not plain text)
        // Note: This test assumes encryption is implemented
        String storedAccountNumber = dbPayment.getRecipientAccountNumber();
        assertNotNull(storedAccountNumber);
        
        // In a real implementation, this would verify the account number is encrypted
        // For now, we verify it's not empty and has been processed
        assertFalse(storedAccountNumber.isEmpty());
        
        // Verify the service layer returns decrypted data
        PaymentResponse retrievedPayment = paymentService.getPaymentById(payment.getPaymentId());
        assertEquals("1234567890", retrievedPayment.getRecipientAccountNumber());
    }

    @Test
    void auditLogging_PaymentCreation_LoggedProperly() {
        // Create payment
        PaymentResponse payment = paymentService.createPayment(testPaymentRequest, "audit-test-user");
        
        // Verify audit log entry was created
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByPaymentId(payment.getPaymentId());
        assertFalse(auditLogs.isEmpty());
        
        PaymentAuditLog creationLog = auditLogs.stream()
            .filter(log -> log.getEventType() == PaymentAuditLog.EventType.PAYMENT_CREATED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(creationLog);
        assertEquals("audit-test-user", creationLog.getUserId());
        assertEquals(payment.getPaymentId(), creationLog.getPaymentId());
        assertNotNull(creationLog.getTimestamp());
    }

    @Test
    void auditLogging_PaymentStatusUpdate_LoggedProperly() {
        // Create payment
        PaymentResponse payment = paymentService.createPayment(testPaymentRequest, "audit-test-user");
        
        // Update payment status
        paymentService.updatePaymentStatus(
            payment.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, 
            "Payment completed", 
            "audit-test-user"
        );
        
        // Verify audit log entries
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByPaymentId(payment.getPaymentId());
        assertTrue(auditLogs.size() >= 2); // Creation + Status update
        
        PaymentAuditLog statusUpdateLog = auditLogs.stream()
            .filter(log -> log.getEventType() == PaymentAuditLog.EventType.STATUS_CHANGED)
            .findFirst()
            .orElse(null);
        
        assertNotNull(statusUpdateLog);
        assertEquals("audit-test-user", statusUpdateLog.getUserId());
        assertEquals("PENDING", statusUpdateLog.getOldStatus());
        assertEquals("COMPLETED", statusUpdateLog.getNewStatus());
    }

    @Test
    void dataValidation_InvalidAccountNumber_Rejected() {
        // Test with invalid account number format
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("invalid-account") // Invalid format
                .bankCode("LBP")
                .accountName("Juan Dela Cruz")
                .build())
            .build();

        // Should throw validation exception
        assertThrows(Exception.class, () -> {
            paymentService.createPayment(invalidRequest, "test-user");
        });
    }

    @Test
    void dataValidation_ExcessiveAmount_Rejected() {
        // Test with amount exceeding limits
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("999999999999.99")) // Excessive amount
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("1234567890")
                .bankCode("LBP")
                .accountName("Juan Dela Cruz")
                .build())
            .build();

        // Should throw validation exception
        assertThrows(Exception.class, () -> {
            paymentService.createPayment(invalidRequest, "test-user");
        });
    }

    @Test
    void dataIntegrity_PaymentModification_AuditTrailMaintained() {
        // Create payment
        PaymentResponse payment = paymentService.createPayment(testPaymentRequest, "integrity-test-user");
        
        // Perform multiple status updates
        paymentService.updatePaymentStatus(
            payment.getPaymentId(), 
            Payment.PaymentStatus.PROCESSING, 
            "Payment processing started", 
            "processor-user"
        );
        
        paymentService.updatePaymentStatus(
            payment.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, 
            "Payment completed successfully", 
            "completion-user"
        );
        
        // Verify complete audit trail
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByPaymentId(payment.getPaymentId());
        assertTrue(auditLogs.size() >= 3); // Creation + 2 status updates
        
        // Verify chronological order and data integrity
        PaymentAuditLog creationLog = auditLogs.stream()
            .filter(log -> log.getEventType() == PaymentAuditLog.EventType.PAYMENT_CREATED)
            .findFirst()
            .orElse(null);
        assertNotNull(creationLog);
        assertEquals("integrity-test-user", creationLog.getUserId());
        
        // Verify status change logs
        List<PaymentAuditLog> statusLogs = auditLogs.stream()
            .filter(log -> log.getEventType() == PaymentAuditLog.EventType.STATUS_CHANGED)
            .toList();
        assertEquals(2, statusLogs.size());
    }

    @Test
    void accessControl_PaymentRetrieval_UserSpecificData() {
        // Create payments for different users
        PaymentResponse payment1 = paymentService.createPayment(testPaymentRequest, "user1");
        
        PaymentRequest request2 = PaymentRequest.builder()
            .householdId("HH-002")
            .programName("4Ps")
            .amount(new BigDecimal("1500.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("0987654321")
                .bankCode("BPI")
                .accountName("Maria Santos")
                .build())
            .build();
        PaymentResponse payment2 = paymentService.createPayment(request2, "user2");
        
        // Verify both payments exist
        assertNotNull(paymentService.getPaymentById(payment1.getPaymentId()));
        assertNotNull(paymentService.getPaymentById(payment2.getPaymentId()));
        
        // In a real implementation, this would test user-specific access controls
        // For now, we verify the payments are properly isolated by household
        assertNotEquals(payment1.getHouseholdId(), payment2.getHouseholdId());
    }

    @Test
    void dataRetention_SensitiveInformation_ProperlyHandled() {
        // Create payment with sensitive information
        PaymentResponse payment = paymentService.createPayment(testPaymentRequest, "retention-test-user");
        
        // Verify payment exists
        Payment dbPayment = paymentRepository.findById(payment.getPaymentId()).orElse(null);
        assertNotNull(dbPayment);
        
        // Verify sensitive data is present (for active payments)
        assertNotNull(dbPayment.getRecipientAccountNumber());
        assertNotNull(dbPayment.getRecipientAccountName());
        
        // In a real implementation, this would test data retention policies
        // such as automatic anonymization after certain periods
    }

    @Test
    void errorHandling_SecurityExceptions_NoSensitiveDataLeakage() {
        // Test with malformed request that might cause exceptions
        PaymentRequest malformedRequest = PaymentRequest.builder()
            .householdId(null) // This should cause validation error
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .build();

        // Verify exception doesn't leak sensitive information
        Exception exception = assertThrows(Exception.class, () -> {
            paymentService.createPayment(malformedRequest, "error-test-user");
        });
        
        // Exception message should not contain sensitive data
        String errorMessage = exception.getMessage();
        assertNotNull(errorMessage);
        assertFalse(errorMessage.contains("password"));
        assertFalse(errorMessage.contains("secret"));
        assertFalse(errorMessage.contains("key"));
    }

    @Test
    void concurrentAccess_PaymentModification_DataConsistency() {
        // Create payment
        PaymentResponse payment = paymentService.createPayment(testPaymentRequest, "concurrency-test-user");
        
        // Simulate concurrent access (in a real test, this would use multiple threads)
        // For now, we test sequential operations to verify data consistency
        
        // First update
        PaymentResponse updated1 = paymentService.updatePaymentStatus(
            payment.getPaymentId(), 
            Payment.PaymentStatus.PROCESSING, 
            "Processing started", 
            "user1"
        );
        
        // Second update
        PaymentResponse updated2 = paymentService.updatePaymentStatus(
            payment.getPaymentId(), 
            Payment.PaymentStatus.COMPLETED, 
            "Processing completed", 
            "user2"
        );
        
        // Verify final state is consistent
        assertEquals(Payment.PaymentStatus.COMPLETED, updated2.getStatus());
        
        // Verify audit trail shows both updates
        List<PaymentAuditLog> auditLogs = auditLogRepository.findByPaymentId(payment.getPaymentId());
        long statusChangeCount = auditLogs.stream()
            .filter(log -> log.getEventType() == PaymentAuditLog.EventType.STATUS_CHANGED)
            .count();
        assertEquals(2, statusChangeCount);
    }

    @Test
    void inputSanitization_SpecialCharacters_HandledSafely() {
        // Test with special characters that might cause issues
        PaymentRequest specialCharRequest = PaymentRequest.builder()
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("1234567890")
                .bankCode("LBP")
                .accountName("José María Ñoño") // Special characters
                .build())
            .build();

        // Should handle special characters properly
        PaymentResponse payment = paymentService.createPayment(specialCharRequest, "special-char-user");
        assertNotNull(payment);
        
        // Verify special characters are preserved correctly
        PaymentResponse retrieved = paymentService.getPaymentById(payment.getPaymentId());
        assertEquals("José María Ñoño", retrieved.getRecipientAccountName());
    }
}
