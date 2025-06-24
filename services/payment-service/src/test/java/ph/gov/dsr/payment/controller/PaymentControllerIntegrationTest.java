package ph.gov.dsr.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.dto.PaymentResponse;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequest testPaymentRequest;
    private PaymentResponse testPaymentResponse;
    private UUID testPaymentId;

    @BeforeEach
    void setUp() {
        testPaymentId = UUID.randomUUID();

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
            .paymentMethod(Payment.PaymentMethod.BANK_TRANSFER)
            .status(Payment.PaymentStatus.PENDING)
            .internalReferenceNumber("PAY-2024-001")
            .createdBy("test-user")
            .createdDate(LocalDateTime.now())
            .build();
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_Success() throws Exception {
        // Arrange
        when(paymentService.createPayment(any(PaymentRequest.class), anyString()))
            .thenReturn(testPaymentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.paymentId").value(testPaymentId.toString()))
            .andExpect(jsonPath("$.householdId").value("HH-001"))
            .andExpect(jsonPath("$.amount").value(1400.00))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPayment_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        PaymentRequest invalidRequest = PaymentRequest.builder()
            .householdId("")
            .amount(BigDecimal.ZERO)
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getPaymentById_Success() throws Exception {
        // Arrange
        when(paymentService.getPaymentById(testPaymentId)).thenReturn(testPaymentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/{paymentId}", testPaymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(testPaymentId.toString()))
            .andExpect(jsonPath("$.householdId").value("HH-001"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getPaymentById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(paymentService.getPaymentById(testPaymentId))
            .thenThrow(new RuntimeException("Payment not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/{paymentId}", testPaymentId))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getPaymentsByHouseholdId_Success() throws Exception {
        // Arrange
        UUID householdId = UUID.randomUUID();
        Page<PaymentResponse> paymentPage = new PageImpl<>(Arrays.asList(testPaymentResponse));
        when(paymentService.getPaymentsByHouseholdId(eq(householdId), any()))
            .thenReturn(paymentPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/household/{householdId}", householdId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].householdId").value("HH-001"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void updatePaymentStatus_Success() throws Exception {
        // Arrange
        when(paymentService.updatePaymentStatus(eq(testPaymentId), any(), anyString(), anyString()))
            .thenReturn(testPaymentResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/payments/{paymentId}/status", testPaymentId)
                .with(csrf())
                .param("status", "COMPLETED")
                .param("reason", "Payment completed")
                .param("updatedBy", "test-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(testPaymentId.toString()));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void processPayment_Success() throws Exception {
        // Arrange
        when(paymentService.processPayment(testPaymentId, "test-user"))
            .thenReturn(testPaymentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payments/{paymentId}/process", testPaymentId)
                .with(csrf())
                .param("processedBy", "test-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.paymentId").value(testPaymentId.toString()));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getPaymentStatistics_Success() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("COMPLETED", Map.of("count", 10, "totalAmount", 14000.00));
        stats.put("PENDING", Map.of("count", 5, "totalAmount", 7000.00));
        
        when(paymentService.getPaymentStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payments/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.COMPLETED").exists())
            .andExpect(jsonPath("$.PENDING").exists());
    }

    @Test
    void createPayment_Unauthorized_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"INVALID_ROLE"})
    void createPayment_Forbidden_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isForbidden());
    }
}
