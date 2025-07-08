package ph.gov.dsr.grievance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IntelligentCaseRoutingService
 */
@ExtendWith(MockitoExtension.class)
class IntelligentCaseRoutingServiceTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @Mock
    private CaseAssignmentService caseAssignmentService;

    @InjectMocks
    private IntelligentCaseRoutingService routingService;

    private GrievanceCase testCase;

    @BeforeEach
    void setUp() {
        testCase = new GrievanceCase();
        testCase.setId(UUID.randomUUID());
        testCase.setCaseNumber("GRV-2025-001");
        testCase.setComplainantPsn("PSN123456789");
        testCase.setSubject("Payment Issue");
        testCase.setDescription("I have not received my payment for this month");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        testCase.setSubmissionDate(LocalDateTime.now());
    }

    @Test
    void testRouteCaseIntelligently_PaymentIssue_Success() {
        // Arrange
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("payment.specialist@dswd.gov.ph", 2);
        workloadDistribution.put("case.manager@dswd.gov.ph", 5);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("payment.specialist@dswd.gov.ph", assignedStaff);
        verify(caseAssignmentService).getWorkloadDistribution();
    }

    @Test
    void testRouteCaseIntelligently_CriticalPriority_UpgradesPriority() {
        // Arrange
        testCase.setSubject("URGENT: Critical system error");
        testCase.setDescription("Emergency situation requiring immediate attention");
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("it.support@dswd.gov.ph", 1);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals(GrievanceCase.Priority.CRITICAL, testCase.getPriority());
    }

    @Test
    void testRouteCaseIntelligently_CorruptionCase_AssignsToIntegrityOfficer() {
        // Arrange
        testCase.setSubject("Corruption Report");
        testCase.setDescription("Report of corruption in benefit distribution");
        testCase.setCategory(GrievanceCase.GrievanceCategory.CORRUPTION);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("integrity.officer@dswd.gov.ph", 1);
        workloadDistribution.put("case.manager@dswd.gov.ph", 3);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("integrity.officer@dswd.gov.ph", assignedStaff);
    }

    @Test
    void testRouteCaseIntelligently_SystemError_AssignsToITSupport() {
        // Arrange
        testCase.setSubject("System Error");
        testCase.setDescription("System error preventing benefit access");
        testCase.setCategory(GrievanceCase.GrievanceCategory.SYSTEM_ERROR);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("it.support@dswd.gov.ph", 2);
        workloadDistribution.put("case.manager@dswd.gov.ph", 1);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("it.support@dswd.gov.ph", assignedStaff);
    }

    @Test
    void testRouteCaseIntelligently_HighWorkload_AdjustsForWorkload() {
        // Arrange
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("payment.specialist@dswd.gov.ph", 10); // High workload
        workloadDistribution.put("case.manager@dswd.gov.ph", 2); // Low workload
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        // Should prefer case manager due to lower workload despite lower expertise
        assertEquals("case.manager@dswd.gov.ph", assignedStaff);
    }

    @Test
    void testRouteCaseIntelligently_Exception_FallsBackToStandardAssignment() {
        // Arrange
        when(caseAssignmentService.getWorkloadDistribution()).thenThrow(new RuntimeException("Service unavailable"));
        when(caseAssignmentService.findBestAssignee(any(), any())).thenReturn("fallback.staff@dswd.gov.ph");

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("fallback.staff@dswd.gov.ph", assignedStaff);
        verify(caseAssignmentService).findBestAssignee(
            GrievanceCase.GrievanceCategory.PAYMENT_ISSUE,
            GrievanceCase.Priority.MEDIUM
        );
    }

    @Test
    void testGetRoutingAnalytics_Success() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<GrievanceCase> recentCases = List.of(testCase);
        
        when(caseRepository.findBySubmissionDateAfter(any(LocalDateTime.class))).thenReturn(recentCases);
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(new HashMap<>());

        // Act
        Map<String, Object> analytics = routingService.getRoutingAnalytics();

        // Assert
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("totalCasesRouted"));
        assertTrue(analytics.containsKey("averageResolutionTime"));
        assertTrue(analytics.containsKey("routingAccuracy"));
        assertTrue(analytics.containsKey("workloadDistribution"));
        
        assertEquals(1, analytics.get("totalCasesRouted"));
        verify(caseRepository).findBySubmissionDateAfter(any(LocalDateTime.class));
    }

    @Test
    void testRouteCaseIntelligently_EligibilityDispute_AssignsToSpecialist() {
        // Arrange
        testCase.setSubject("Eligibility Question");
        testCase.setDescription("Question about eligibility criteria and requirements");
        testCase.setCategory(GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("eligibility.specialist@dswd.gov.ph", 1);
        workloadDistribution.put("case.manager@dswd.gov.ph", 1);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("eligibility.specialist@dswd.gov.ph", assignedStaff);
    }

    @Test
    void testRouteCaseIntelligently_ServiceDelivery_AssignsToCaseManager() {
        // Arrange
        testCase.setSubject("Service Delivery Issue");
        testCase.setDescription("Delay in service delivery and quality concerns");
        testCase.setCategory(GrievanceCase.GrievanceCategory.SERVICE_DELIVERY);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("case.manager@dswd.gov.ph", 2);
        workloadDistribution.put("payment.specialist@dswd.gov.ph", 1);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("case.manager@dswd.gov.ph", assignedStaff);
    }

    @Test
    void testRouteCaseIntelligently_ContentAnalysis_DetectsKeywords() {
        // Arrange
        testCase.setSubject("High priority payment issue");
        testCase.setDescription("This is an important matter that needs immediate attention");
        testCase.setPriority(GrievanceCase.Priority.LOW);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("payment.specialist@dswd.gov.ph", 1);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        // Priority should be upgraded based on keywords
        assertEquals(GrievanceCase.Priority.HIGH, testCase.getPriority());
    }

    @Test
    void testRouteCaseIntelligently_EmptyWorkloadDistribution_HandlesGracefully() {
        // Arrange
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(new HashMap<>());
        when(caseAssignmentService.findBestAssignee(any(), any())).thenReturn("default.staff@dswd.gov.ph");

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals("default.staff@dswd.gov.ph", assignedStaff);
    }

    @Test
    void testRouteCaseIntelligently_MultipleKeywords_SelectsHighestPriority() {
        // Arrange
        testCase.setSubject("Critical emergency system error");
        testCase.setDescription("Urgent technical issue requiring immediate attention");
        testCase.setPriority(GrievanceCase.Priority.LOW);
        
        Map<String, Integer> workloadDistribution = new HashMap<>();
        workloadDistribution.put("it.support@dswd.gov.ph", 1);
        
        when(caseAssignmentService.getWorkloadDistribution()).thenReturn(workloadDistribution);

        // Act
        String assignedStaff = routingService.routeCaseIntelligently(testCase);

        // Assert
        assertNotNull(assignedStaff);
        assertEquals(GrievanceCase.Priority.CRITICAL, testCase.getPriority());
        assertEquals("it.support@dswd.gov.ph", assignedStaff);
    }
}
