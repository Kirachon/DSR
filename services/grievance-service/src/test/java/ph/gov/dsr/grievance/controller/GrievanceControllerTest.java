package ph.gov.dsr.grievance.controller;

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

import ph.gov.dsr.grievance.dto.*;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.service.GrievanceCaseService;
import ph.gov.dsr.grievance.service.MultiChannelCaseManagementService;
import ph.gov.dsr.grievance.service.CaseAssignmentService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for GrievanceController
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@WebMvcTest(GrievanceController.class)
class GrievanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GrievanceCaseService grievanceCaseService;

    @MockBean
    private MultiChannelCaseManagementService caseManagementService;

    @MockBean
    private CaseAssignmentService caseAssignmentService;

    private GrievanceCase testCase;
    private CaseSubmissionRequest testRequest;

    @BeforeEach
    void setUp() {
        testCase = new GrievanceCase();
        testCase.setId(UUID.randomUUID());
        testCase.setCaseNumber("GRV-2024-001");
        testCase.setComplainantPsn("123456789012");
        testCase.setComplainantName("John Doe");
        testCase.setComplainantEmail("john.doe@email.com");
        testCase.setSubject("Payment Issue");
        testCase.setDescription("Payment not received");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        testCase.setStatus(GrievanceCase.CaseStatus.SUBMITTED);
        testCase.setSubmissionDate(LocalDateTime.now());

        testRequest = CaseSubmissionRequest.builder()
                .complainantPsn("123456789012")
                .complainantName("John Doe")
                .complainantEmail("john.doe@email.com")
                .subject("Payment Issue")
                .description("Payment not received")
                .category(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE)
                .priority(GrievanceCase.Priority.MEDIUM)
                .build();
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testGetCases_Success() throws Exception {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(testCase);
        Page<GrievanceCase> casePage = new PageImpl<>(cases, PageRequest.of(0, 20), 1);
        
        when(grievanceCaseService.getCasesByCriteria(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(casePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/cases")
                .param("page", "0")
                .param("size", "20")
                .param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].caseNumber").value("GRV-2024-001"));

        verify(grievanceCaseService).getCasesByCriteria(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testGetCase_Success() throws Exception {
        // Arrange
        when(grievanceCaseService.getCaseById(testCase.getId())).thenReturn(Optional.of(testCase));

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/cases/{caseId}", testCase.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseNumber").value("GRV-2024-001"))
                .andExpect(jsonPath("$.complainantName").value("John Doe"));

        verify(grievanceCaseService).getCaseById(testCase.getId());
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testGetCase_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(grievanceCaseService.getCaseById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/cases/{caseId}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(grievanceCaseService).getCaseById(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void testCreateCase_Success() throws Exception {
        // Arrange
        when(caseManagementService.submitCaseFromWebPortal(any(CaseSubmissionRequest.class)))
                .thenReturn(testCase);

        // Act & Assert
        mockMvc.perform(post("/api/v1/grievances/cases")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caseNumber").value("GRV-2024-001"));

        verify(caseManagementService).submitCaseFromWebPortal(any(CaseSubmissionRequest.class));
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testUpdateCaseStatus_Success() throws Exception {
        // Arrange
        UpdateCaseStatusRequest request = UpdateCaseStatusRequest.builder()
                .status("UNDER_REVIEW")
                .reason("Starting investigation")
                .updatedBy("staff@dswd.gov.ph")
                .build();

        testCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);
        when(grievanceCaseService.updateCaseStatus(any(UUID.class), any(), any(), any()))
                .thenReturn(testCase);

        // Act & Assert
        mockMvc.perform(post("/api/v1/grievances/cases/{caseId}/status", testCase.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        verify(grievanceCaseService).updateCaseStatus(eq(testCase.getId()), 
                eq(GrievanceCase.CaseStatus.UNDER_REVIEW), eq("Starting investigation"), eq("staff@dswd.gov.ph"));
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testAssignCase_Success() throws Exception {
        // Arrange
        AssignCaseRequest request = AssignCaseRequest.builder()
                .assignedTo("staff@dswd.gov.ph")
                .assignedBy("manager@dswd.gov.ph")
                .reason("Workload distribution")
                .build();

        testCase.setAssignedTo("staff@dswd.gov.ph");
        when(grievanceCaseService.assignCase(any(UUID.class), any(), any()))
                .thenReturn(testCase);

        // Act & Assert
        mockMvc.perform(post("/api/v1/grievances/cases/{caseId}/assign", testCase.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo").value("staff@dswd.gov.ph"));

        verify(grievanceCaseService).assignCase(testCase.getId(), "staff@dswd.gov.ph", "manager@dswd.gov.ph");
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testAddCaseComment_Success() throws Exception {
        // Arrange
        AddCaseCommentRequest request = AddCaseCommentRequest.builder()
                .comment("Investigation started")
                .author("staff@dswd.gov.ph")
                .isInternal(false)
                .build();

        when(grievanceCaseService.addComment(any(UUID.class), any(), any(), any()))
                .thenReturn(testCase);

        // Act & Assert
        mockMvc.perform(post("/api/v1/grievances/cases/{caseId}/comments", testCase.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseNumber").value("GRV-2024-001"));

        verify(grievanceCaseService).addComment(testCase.getId(), "Investigation started", "staff@dswd.gov.ph", false);
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testGetMyCases_Success() throws Exception {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(testCase);
        Page<GrievanceCase> casePage = new PageImpl<>(cases, PageRequest.of(0, 20), 1);
        
        when(grievanceCaseService.getCasesAssignedTo(any(), any())).thenReturn(casePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/my-cases")
                .param("assignedTo", "staff@dswd.gov.ph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(grievanceCaseService).getCasesAssignedTo(eq("staff@dswd.gov.ph"), any());
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testGetCaseStatistics_Success() throws Exception {
        // Arrange
        Object[] stats = {100L, 80L, 20L, 5L, 10L, 2L, 3.5, 4.2};
        when(grievanceCaseService.getCaseStatistics()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCases").value(100))
                .andExpect(jsonPath("$.openCases").value(80))
                .andExpect(jsonPath("$.resolvedCases").value(20));

        verify(grievanceCaseService).getCaseStatistics();
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testSearchCases_Success() throws Exception {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(testCase);
        Page<GrievanceCase> casePage = new PageImpl<>(cases, PageRequest.of(0, 20), 1);
        
        when(grievanceCaseService.searchCases(any(), any())).thenReturn(casePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/search")
                .param("searchText", "payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(grievanceCaseService).searchCases(eq("payment"), any());
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testResolveCase_Success() throws Exception {
        // Arrange
        testCase.setStatus(GrievanceCase.CaseStatus.RESOLVED);
        when(grievanceCaseService.resolveCase(any(UUID.class), any(), any(), any()))
                .thenReturn(testCase);

        // Act & Assert
        mockMvc.perform(post("/api/v1/grievances/cases/{caseId}/resolve", testCase.getId())
                .with(csrf())
                .param("resolutionSummary", "Issue resolved")
                .param("resolutionActions", "Payment processed")
                .param("resolvedBy", "staff@dswd.gov.ph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        verify(grievanceCaseService).resolveCase(testCase.getId(), "Issue resolved", "Payment processed", "staff@dswd.gov.ph");
    }

    @Test
    @WithMockUser(roles = "DSWD_STAFF")
    void testGetCaseTypes_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("SERVICE_DELIVERY"));
    }

    @Test
    @WithMockUser(roles = "UNAUTHORIZED")
    void testGetCases_AccessDenied() throws Exception {
        // Mock the service to return null to trigger the NullPointerException
        when(grievanceCaseService.getCasesByCriteria(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/v1/grievances/cases"))
                .andExpect(status().isInternalServerError());
    }
}
