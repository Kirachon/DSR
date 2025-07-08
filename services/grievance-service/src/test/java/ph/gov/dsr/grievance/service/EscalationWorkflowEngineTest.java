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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EscalationWorkflowEngine
 */
@ExtendWith(MockitoExtension.class)
class EscalationWorkflowEngineTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @Mock
    private CaseAssignmentService caseAssignmentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private IntelligentCaseRoutingService routingService;

    @InjectMocks
    private EscalationWorkflowEngine escalationEngine;

    private GrievanceCase testCase;
    private UUID caseId;

    @BeforeEach
    void setUp() {
        caseId = UUID.randomUUID();
        testCase = new GrievanceCase();
        testCase.setId(caseId);
        testCase.setCaseNumber("GRV-2025-001");
        testCase.setComplainantPsn("PSN123456789");
        testCase.setSubject("Test Case");
        testCase.setDescription("Test case description");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        testCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(50));
        testCase.setAssignedTo("payment.specialist@dswd.gov.ph");
        testCase.setEscalationLevel(0);
    }

    @Test
    void testProcessEscalation_SLABreach_Success() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "SLA_BREACH", "Case exceeded SLA deadline", "SYSTEM");

        // Assert
        assertNotNull(result);
        assertEquals(EscalationWorkflowEngine.EscalationStatus.SUCCESS, result.getStatus());
        assertEquals(caseId, result.getCaseId());
        assertEquals("GRV-2025-001", result.getCaseNumber());
        assertEquals("payment.specialist@dswd.gov.ph", result.getPreviousAssignee());
        assertEquals("payment.supervisor@dswd.gov.ph", result.getNewAssignee());
        assertEquals(0, result.getPreviousLevel());
        assertEquals(1, result.getNewLevel());
        assertEquals(GrievanceCase.CaseStatus.ESCALATED, testCase.getStatus());
        
        verify(caseRepository).save(testCase);
        verify(notificationService).sendEscalationAssignmentNotification(testCase, result.getNewAssignee());
    }

    @Test
    void testProcessEscalation_CriticalPriority_SkipsLevels() {
        // Arrange
        testCase.setPriority(GrievanceCase.Priority.CRITICAL);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "CRITICAL_PRIORITY", "Critical priority case", "SYSTEM");

        // Assert
        assertNotNull(result);
        assertEquals(EscalationWorkflowEngine.EscalationStatus.SUCCESS, result.getStatus());
        assertTrue(result.getNewLevel() >= 2); // Critical cases go to at least level 2
        assertEquals(EscalationWorkflowEngine.EscalationType.EMERGENCY, result.getEscalationType());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_CorruptionCase_AssignsToIntegrityHierarchy() {
        // Arrange
        testCase.setCategory(GrievanceCase.GrievanceCategory.CORRUPTION);
        testCase.setAssignedTo("integrity.officer@dswd.gov.ph");
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "SLA_BREACH", "SLA breach for corruption case", "SYSTEM");

        // Assert
        assertNotNull(result);
        assertEquals("senior.integrity.officer@dswd.gov.ph", result.getNewAssignee());
        assertEquals(1, result.getNewLevel());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_SystemError_AssignsToITHierarchy() {
        // Arrange
        testCase.setCategory(GrievanceCase.GrievanceCategory.SYSTEM_ERROR);
        testCase.setAssignedTo("it.support@dswd.gov.ph");
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "COMPLEXITY", "Complex technical issue", "MANUAL");

        // Assert
        assertNotNull(result);
        assertEquals("senior.it.manager@dswd.gov.ph", result.getNewAssignee());
        assertEquals(1, result.getNewLevel());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_RepeatedEscalation_GoesToHighestLevel() {
        // Arrange
        testCase.setEscalationLevel(1);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "REPEATED_ESCALATION", "Multiple escalations", "MANUAL");

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getNewLevel()); // Goes to highest level
        assertEquals("finance.director@dswd.gov.ph", result.getNewAssignee());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_CaseNotFound_ThrowsException() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            escalationEngine.processEscalation(caseId, "SLA_BREACH", "Test reason", "SYSTEM"));
        
        assertTrue(exception.getMessage().contains("Case not found"));
    }

    @Test
    void testProcessEscalation_UnknownTrigger_ThrowsException() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            escalationEngine.processEscalation(caseId, "UNKNOWN_TRIGGER", "Test reason", "SYSTEM"));
        
        assertTrue(exception.getMessage().contains("Unknown escalation trigger"));
    }

    @Test
    void testProcessEscalation_ExternalPressure_HighLevelEscalation() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "EXTERNAL_PRESSURE", "External stakeholder pressure", "MANUAL");

        // Assert
        assertNotNull(result);
        assertTrue(result.getNewLevel() >= 2); // External pressure escalates high
        
        verify(caseRepository).save(testCase);
        verify(notificationService).sendManagementEscalationNotification(testCase, result);
    }

    @Test
    void testProcessEscalation_CustomerComplaint_StandardEscalation() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "CUSTOMER_COMPLAINT", "Customer escalation request", "MANUAL");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNewLevel());
        assertEquals(EscalationWorkflowEngine.EscalationType.STANDARD, result.getEscalationType());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_CriticalCaseWithSLAAdjustment_ReducesSLA() {
        // Arrange
        testCase.setPriority(GrievanceCase.Priority.CRITICAL);
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(20));
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "CRITICAL_PRIORITY", "Critical escalation", "SYSTEM");

        // Assert
        assertNotNull(result);
        // SLA should be reduced for critical escalations
        assertTrue(testCase.getResolutionTargetDate().isBefore(LocalDateTime.now().plusHours(15)));
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testGetEscalationAnalytics_Success() {
        // Arrange
        List<GrievanceCase> escalatedCases = List.of(testCase);
        when(caseRepository.findEscalatedCasesSince(any(LocalDateTime.class))).thenReturn(escalatedCases);

        // Act
        Map<String, Object> analytics = escalationEngine.getEscalationAnalytics();

        // Assert
        assertNotNull(analytics);
        assertTrue(analytics.containsKey("totalEscalations"));
        assertTrue(analytics.containsKey("escalationsByCategory"));
        assertTrue(analytics.containsKey("escalationsByLevel"));
        assertTrue(analytics.containsKey("averageEscalationTime"));
        assertTrue(analytics.containsKey("escalationTrends"));
        
        assertEquals(1, analytics.get("totalEscalations"));
        verify(caseRepository).findEscalatedCasesSince(any(LocalDateTime.class));
    }

    @Test
    void testProcessEscalation_StaffConductCase_AssignsToHRHierarchy() {
        // Arrange
        testCase.setCategory(GrievanceCase.GrievanceCategory.STAFF_CONDUCT);
        testCase.setAssignedTo("hr.specialist@dswd.gov.ph");
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "SLA_BREACH", "Staff conduct SLA breach", "SYSTEM");

        // Assert
        assertNotNull(result);
        assertEquals("hr.manager@dswd.gov.ph", result.getNewAssignee());
        assertEquals(1, result.getNewLevel());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_EligibilityDispute_AssignsToEligibilityHierarchy() {
        // Arrange
        testCase.setCategory(GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE);
        testCase.setAssignedTo("eligibility.specialist@dswd.gov.ph");
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "COMPLEXITY", "Complex eligibility issue", "MANUAL");

        // Assert
        assertNotNull(result);
        assertEquals("eligibility.supervisor@dswd.gov.ph", result.getNewAssignee());
        assertEquals(1, result.getNewLevel());
        
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_ManagementLevelEscalation_SendsManagementNotification() {
        // Arrange
        testCase.setEscalationLevel(1);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "SLA_BREACH", "Second level escalation", "SYSTEM");

        // Assert
        assertNotNull(result);
        assertTrue(result.getNewLevel() >= 2);
        
        verify(notificationService).sendManagementEscalationNotification(testCase, result);
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessEscalation_UpdatesWorkloadTracking_Success() {
        // Arrange
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        EscalationWorkflowEngine.EscalationResult result = escalationEngine.processEscalation(
            caseId, "SLA_BREACH", "Test escalation", "SYSTEM");

        // Assert
        assertNotNull(result);
        verify(caseAssignmentService).reassignCase(
            result.getPreviousAssignee(),
            testCase.getCategory(),
            testCase.getPriority(),
            "Escalation"
        );
    }
}
