package ph.gov.dsr.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ph.gov.dsr.payment.dto.PaymentBatchRequest;
import ph.gov.dsr.payment.dto.PaymentBatchResponse;
import ph.gov.dsr.payment.dto.PaymentRequest;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.entity.PaymentBatch;
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
 * Integration tests for PaymentBatchController
 */
@WebMvcTest(PaymentBatchController.class)
class PaymentBatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentBatchService paymentBatchService;

    private PaymentBatchRequest testBatchRequest;
    private PaymentBatchResponse testBatchResponse;
    private UUID testBatchId;
    private UUID testProgramId;

    @BeforeEach
    void setUp() {
        testBatchId = UUID.randomUUID();
        testProgramId = UUID.randomUUID();

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

        testBatchResponse = PaymentBatchResponse.builder()
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
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPaymentBatch_Success() throws Exception {
        // Arrange
        when(paymentBatchService.createPaymentBatch(any(PaymentBatchRequest.class), anyString()))
            .thenReturn(testBatchResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.batchId").value(testBatchId.toString()))
            .andExpect(jsonPath("$.programName").value("4Ps"))
            .andExpect(jsonPath("$.totalPayments").value(2))
            .andExpect(jsonPath("$.totalAmount").value(2800.00))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void createPaymentBatch_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        PaymentBatchRequest invalidRequest = PaymentBatchRequest.builder()
            .programName("")
            .paymentRequests(Collections.emptyList())
            .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getPaymentBatchById_Success() throws Exception {
        // Arrange
        when(paymentBatchService.getPaymentBatchById(testBatchId)).thenReturn(testBatchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payment-batches/{batchId}", testBatchId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(testBatchId.toString()))
            .andExpect(jsonPath("$.programName").value("4Ps"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getPaymentBatchesByProgramId_Success() throws Exception {
        // Arrange
        Page<PaymentBatchResponse> batchPage = new PageImpl<>(Arrays.asList(testBatchResponse));
        when(paymentBatchService.getPaymentBatchesByProgramId(eq(testProgramId), any()))
            .thenReturn(batchPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payment-batches/program/{programId}", testProgramId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].programId").value(testProgramId.toString()));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void startBatchProcessing_Success() throws Exception {
        // Arrange
        testBatchResponse.setStatus(PaymentBatch.BatchStatus.PROCESSING);
        when(paymentBatchService.startBatchProcessing(testBatchId, "test-user"))
            .thenReturn(testBatchResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-batches/{batchId}/start", testBatchId)
                .with(csrf())
                .param("startedBy", "test-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(testBatchId.toString()))
            .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void updateBatchStatus_Success() throws Exception {
        // Arrange
        testBatchResponse.setStatus(PaymentBatch.BatchStatus.COMPLETED);
        when(paymentBatchService.updateBatchStatus(eq(testBatchId), any(), anyString(), anyString()))
            .thenReturn(testBatchResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/payment-batches/{batchId}/status", testBatchId)
                .with(csrf())
                .param("status", "COMPLETED")
                .param("reason", "Batch completed")
                .param("updatedBy", "test-user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(testBatchId.toString()))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void monitorBatchProgress_Success() throws Exception {
        // Arrange
        when(paymentBatchService.monitorBatchProgress(testBatchId)).thenReturn(testBatchResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payment-batches/{batchId}/progress", testBatchId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchId").value(testBatchId.toString()));
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getBatchStatistics_Success() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("COMPLETED", Map.of("count", 5, "totalAmount", 70000.00));
        stats.put("PENDING", Map.of("count", 3, "totalAmount", 42000.00));
        
        when(paymentBatchService.getBatchStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payment-batches/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.COMPLETED").exists())
            .andExpect(jsonPath("$.PENDING").exists());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void getBatchStatisticsByProgram_Success() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put(testProgramId.toString(), Map.of("programName", "4Ps", "count", 5, "totalAmount", 70000.00));
        
        when(paymentBatchService.getBatchStatisticsByProgram()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payment-batches/statistics/program"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + testProgramId.toString()).exists());
    }

    @Test
    @WithMockUser(roles = {"DSWD_STAFF"})
    void generateBatchReport_Success() throws Exception {
        // Arrange
        Map<String, Object> report = new HashMap<>();
        report.put("batchInfo", testBatchResponse);
        report.put("paymentSummary", Map.of("totalPayments", 2));
        report.put("generatedAt", LocalDateTime.now());
        
        when(paymentBatchService.generateBatchReport(testBatchId)).thenReturn(report);

        // Act & Assert
        mockMvc.perform(get("/api/v1/payment-batches/{batchId}/report", testBatchId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.batchInfo").exists())
            .andExpect(jsonPath("$.paymentSummary").exists())
            .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    @WithMockUser(roles = {"SYSTEM_ADMIN"})
    void processScheduledBatches_Success() throws Exception {
        // Arrange
        List<PaymentBatchResponse> processedBatches = Arrays.asList(testBatchResponse);
        when(paymentBatchService.processScheduledBatches()).thenReturn(processedBatches);

        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-batches/process-scheduled")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].batchId").value(testBatchId.toString()));
    }

    @Test
    void createPaymentBatch_Unauthorized_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"INVALID_ROLE"})
    void createPaymentBatch_Forbidden_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/payment-batches")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBatchRequest))
                .param("createdBy", "test-user"))
            .andExpect(status().isForbidden());
    }
}
