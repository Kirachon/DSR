package ph.gov.dsr.payment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ph.gov.dsr.payment.controller.PaymentController;
import ph.gov.dsr.payment.controller.PaymentBatchController;
import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.dto.PaymentResponse;
import ph.gov.dsr.payment.dto.PaymentBatchRequest;
import ph.gov.dsr.payment.dto.PaymentBatchResponse;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentBatch;
import ph.gov.dsr.payment.service.PaymentService;
import ph.gov.dsr.payment.service.PaymentBatchService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for Payment Service endpoints
 */
@WebMvcTest({PaymentController.class, PaymentBatchController.class})
class PaymentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private PaymentBatchService paymentBatchService;

    private PaymentRequest testPaymentRequest;
    private PaymentResponse testPaymentResponse;
    private PaymentBatchRequest testBatchRequest;
    private PaymentBatchResponse testBatchResponse;
    private UUID testPaymentId;
    private UUID testBatchId;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();
        testBatchId = UUID.randomUUID();

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

        testPaymentResponse = PaymentResponse.builder()
            .paymentId(testPaymentId)
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .status(Payment.PaymentStatus.PENDING)
            .build();

        testBatchRequest = PaymentBatchRequest.builder()
            .programId(UUID.randomUUID())
            .programName("4Ps")
            .paymentRequests(Arrays.asList(testPaymentRequest))
            .scheduledDate(LocalDateTime.now().plusDays(1))
            .metadata(new HashMap<>())
            .build();

        testBatchResponse = PaymentBatchResponse.builder()
            .batchId(testBatchId)
            .programName("4Ps")
            .totalPayments(1)
            .status(PaymentBatch.BatchStatus.PENDING)
            .build();
    }

    // Authentication Tests
    @Test
    void createPayment_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaymentById_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/payments/{paymentId}", testPaymentId))
            .andExpected(status().isUnauthorized());
    }

    @Test
    void createPaymentBatch_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isUnauthorized());
    }

    // Authorization Tests - DSWD_STAFF Role
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_WithDSWDStaffRole_ReturnsSuccess() throws Exception {
        when(paymentService.createPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(testPaymentResponse);

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void processPayment_WithDSWDStaffRole_ReturnsSuccess() throws Exception {
        when(paymentService.processPayment(any(UUID.class), anyString()))
            .thenReturn(testPaymentResponse);

        mockMvc.perform(post("/api/v1/payments/{paymentId}/process", testPaymentId)
                .with(csrf())
                .param("processedBy", "test-user"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPaymentBatch_WithDSWDStaffRole_ReturnsSuccess() throws Exception {
        when(paymentBatchService.createPaymentBatch(any(PaymentBatchRequest.class), anyString()))
            .thenReturn(testBatchResponse);

        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated());
    }

    // Authorization Tests - LGU_STAFF Role
    @Test
    @WithMockUser(roles = {"LGU_STAFF"})
    void createPayment_WithLGUStaffRole_ReturnsSuccess() throws Exception {
        when(paymentService.createPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(testPaymentResponse);

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"LGU_STAFF"})
    void processPayment_WithLGUStaffRole_ReturnsForbidden() throws Exception {
        // LGU_STAFF should not be able to process payments
        mockMvc.perform(post("/api/v1/payments/{paymentId}/process", testPaymentId)
                .with(csrf())
                .param("processedBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LGU_STAFF"})
    void createPaymentBatch_WithLGUStaffRole_ReturnsForbidden() throws Exception {
        // LGU_STAFF should not be able to create batches
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    // Authorization Tests - SYSTEM_ADMIN Role
    @Test
    @WithMockUser(roles = {"SYSTEM_ADMIN"})
    void allEndpoints_WithSystemAdminRole_ReturnsSuccess() throws Exception {
        when(paymentService.createPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(testPaymentResponse);
        when(paymentService.processPayment(any(UUID.class), anyString()))
            .thenReturn(testPaymentResponse);
        when(paymentBatchService.createPaymentBatch(any(PaymentBatchRequest.class), anyString()))
            .thenReturn(testBatchResponse);

        // Test payment creation
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated());

        // Test payment processing
        mockMvc.perform(post("/api/v1/payments/{paymentId}/process", testPaymentId)
                .with(csrf())
                .param("processedBy", "test-user"))
            .andExpect(status().isOk());

        // Test batch creation
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated());
    }

    // Authorization Tests - BENEFICIARY Role
    @Test
    @WithMockUser(roles = {"BENEFICIARY"})
    void getPaymentById_WithBeneficiaryRole_ReturnsSuccess() throws Exception {
        when(paymentService.getPaymentById(any(UUID.class))).thenReturn(testPaymentResponse);

        mockMvc.perform(get("/api/v1/payments/{paymentId}", testPaymentId))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"BENEFICIARY"})
    void createPayment_WithBeneficiaryRole_ReturnsForbidden() throws Exception {
        // Beneficiaries should not be able to create payments
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"BENEFICIARY"})
    void processPayment_WithBeneficiaryRole_ReturnsForbidden() throws Exception {
        // Beneficiaries should not be able to process payments
        mockMvc.perform(post("/api/v1/payments/{paymentId}/process", testPaymentId)
                .with(csrf())
                .param("processedBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    // Authorization Tests - Invalid Role
    @Test
    @WithMockUser(roles = {"INVALID_ROLE"})
    void createPayment_WithInvalidRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"INVALID_ROLE"})
    void getPaymentById_WithInvalidRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/payments/{paymentId}", testPaymentId))
            .andExpect(status().isForbidden());
    }

    // CSRF Protection Tests
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_WithoutCSRF_ReturnsForbidden() throws Exception {
        // Test without CSRF token
        mockMvc.perform(post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void processPayment_WithoutCSRF_ReturnsForbidden() throws Exception {
        // Test without CSRF token
        mockMvc.perform(post("/api/v1/payments/{paymentId}/process", testPaymentId)
                .param("processedBy", "test-user"))
            .andExpect(status().isForbidden());
    }

    // Input Validation Security Tests
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_WithInvalidAmount_ReturnsBadRequest() throws Exception {
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .householdId("HH-001")
            .programName("4Ps")
            .amount(new BigDecimal("-100.00")) // Negative amount
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .build();

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_WithMissingRequiredFields_ReturnsBadRequest() throws Exception {
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .amount(new BigDecimal("1400.00"))
            // Missing required fields
            .build();

        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isBadRequest());
    }

    // SQL Injection Protection Tests
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void searchPayments_WithSQLInjectionAttempt_HandledSafely() throws Exception {
        // Test with potential SQL injection in parameters
        mockMvc.perform(get("/api/v1/payments/search")
                .param("householdId", "'; DROP TABLE payments; --")
                .param("status", "PENDING"))
            .andExpect(status().isBadRequest()); // Should be handled as invalid UUID
    }

    // XSS Protection Tests
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_WithXSSAttempt_SanitizedProperly() throws Exception {
        PaymentRequest xssRequest = PaymentRequest.builder()
            .householdId("<script>alert('xss')</script>")
            .programName("4Ps")
            .amount(new BigDecimal("1400.00"))
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .beneficiaryAccount(PaymentRequest.BeneficiaryAccount.builder()
                .accountNumber("1234567890")
                .bankCode("LBP")
                .accountName("<script>alert('xss')</script>")
                .build())
            .build();

        // Should be handled by input validation
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(xssRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isBadRequest());
    }

    // Rate Limiting Tests (if implemented)
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void multipleRequests_WithinRateLimit_AllowedThrough() throws Exception {
        when(paymentService.createPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(testPaymentResponse);

        // Make multiple requests (should all succeed if no rate limiting)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/payments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testPaymentRequest))
                    .param("createdBy", "test-user"))
                .andExpect(status().isCreated());
        }
    }

    // Security Headers Tests
    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void securityHeaders_PresentInResponse() throws Exception {
        when(paymentService.getPaymentById(any(UUID.class))).thenReturn(testPaymentResponse);

        mockMvc.perform(get("/api/v1/payments/{paymentId}", testPaymentId))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().exists("X-Frame-Options"))
            .andExpect(header().exists("X-XSS-Protection"));
    }
}
