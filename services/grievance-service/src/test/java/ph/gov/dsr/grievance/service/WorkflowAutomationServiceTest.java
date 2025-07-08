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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkflowAutomationService
 */
@ExtendWith(MockitoExtension.class)
class WorkflowAutomationServiceTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseAssignmentService caseAssignmentService;

    @InjectMocks
    private WorkflowAutomationService workflowService;

    private GrievanceCase testCase;

    @BeforeEach
    void setUp() {
        testCase = new GrievanceCase();
        testCase.setId(UUID.randomUUID());
        testCase.setCaseNumber("GRV-2024-001");
        testCase.setComplainantPsn("123456789012");
        testCase.setComplainantName("John Doe");
        testCase.setComplainantEmail("john.doe@email.com");
        testCase.setComplainantPhone("+639123456789");
        testCase.setSubject("Payment Issue");
        testCase.setDescription("Payment not received for December 2024");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        testCase.setStatus(GrievanceCase.CaseStatus.SUBMITTED);
        testCase.setSubmissionDate(LocalDateTime.now());
        testCase.setActivities(new ArrayList<>());
    }

    @Test
    void testProcessNewCaseWorkflow_Success() {
        // Arrange
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseAssignmentService.findBestAssignee(any(), any())).thenReturn("staff@dswd.gov.ph");
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processNewCaseWorkflow(testCase.getId());

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(caseAssignmentService).findBestAssignee(
            GrievanceCase.GrievanceCategory.PAYMENT_ISSUE,
            GrievanceCase.Priority.HIGH
        );
        verify(notificationService).sendCaseAcknowledgment(testCase);
        verify(notificationService).sendCaseAssignment(testCase);
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessNewCaseWorkflow_CaseNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        workflowService.processNewCaseWorkflow(nonExistentId);

        // Assert
        verify(caseRepository).findById(nonExistentId);
        verify(caseAssignmentService, never()).findBestAssignee(any(), any());
        verify(notificationService, never()).sendCaseAcknowledgment(any());
        verify(caseRepository, never()).save(any());
    }

    @Test
    void testProcessNewCaseWorkflow_CriticalPriority() {
        // Arrange
        testCase.setSubject("Emergency - Safety Issue");
        testCase.setDescription("Urgent safety concern requiring immediate attention");
        testCase.setCategory(GrievanceCase.GrievanceCategory.STAFF_CONDUCT);
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseAssignmentService.findBestAssignee(any(), any())).thenReturn("manager@dswd.gov.ph");
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processNewCaseWorkflow(testCase.getId());

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(caseAssignmentService).findBestAssignee(
            GrievanceCase.GrievanceCategory.STAFF_CONDUCT, 
            GrievanceCase.Priority.CRITICAL
        );
        verify(caseRepository).save(testCase);
        
        // Verify priority was upgraded to CRITICAL
        assertEquals(GrievanceCase.Priority.CRITICAL, testCase.getPriority());
    }

    @Test
    void testProcessNewCaseWorkflow_CorruptionCase() {
        // Arrange
        testCase.setSubject("Corruption Report");
        testCase.setDescription("Report of corruption in benefit distribution");
        testCase.setCategory(GrievanceCase.GrievanceCategory.CORRUPTION);
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseAssignmentService.findBestAssignee(any(), any())).thenReturn("integrity.officer@dswd.gov.ph");
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processNewCaseWorkflow(testCase.getId());

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(caseAssignmentService).findBestAssignee(
            GrievanceCase.GrievanceCategory.CORRUPTION, 
            GrievanceCase.Priority.CRITICAL
        );
        verify(caseRepository).save(testCase);
        
        // Verify priority was upgraded to CRITICAL for corruption
        assertEquals(GrievanceCase.Priority.CRITICAL, testCase.getPriority());
    }

    @Test
    void testProcessNewCaseWorkflow_SystemErrorCase() {
        // Arrange
        testCase.setSubject("System Error");
        testCase.setDescription("System error preventing benefit access");
        testCase.setCategory(GrievanceCase.GrievanceCategory.SYSTEM_ERROR);
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseAssignmentService.findBestAssignee(any(), any())).thenReturn("it.support@dswd.gov.ph");
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processNewCaseWorkflow(testCase.getId());

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(caseAssignmentService).findBestAssignee(
            GrievanceCase.GrievanceCategory.SYSTEM_ERROR, 
            GrievanceCase.Priority.HIGH
        );
        verify(caseRepository).save(testCase);
        
        // Verify priority was upgraded to HIGH for system errors
        assertEquals(GrievanceCase.Priority.HIGH, testCase.getPriority());
    }

    @Test
    void testProcessOverdueCases_Success() {
        // Arrange
        testCase.setResolutionTargetDate(LocalDateTime.now().minusDays(2)); // Overdue
        testCase.setAssignedTo("staff@dswd.gov.ph");
        
        List<GrievanceCase> overdueCases = Arrays.asList(testCase);
        when(caseRepository.findOverdueCases(any(LocalDateTime.class))).thenReturn(overdueCases);
        when(caseAssignmentService.findEscalationTarget(anyString(), any())).thenReturn("manager@dswd.gov.ph");
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processOverdueCases();

        // Assert
        verify(caseRepository).findOverdueCases(any(LocalDateTime.class));
        verify(notificationService).sendOverdueNotification(testCase);
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessOverdueCases_NoOverdueCases() {
        // Arrange
        when(caseRepository.findOverdueCases(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // Act
        workflowService.processOverdueCases();

        // Assert
        verify(caseRepository).findOverdueCases(any(LocalDateTime.class));
        verify(notificationService, never()).sendOverdueNotification(any());
        verify(caseRepository, never()).save(any());
    }

    @Test
    void testProcessOverdueCases_CriticalCaseEscalation() {
        // Arrange
        testCase.setPriority(GrievanceCase.Priority.CRITICAL);
        testCase.setResolutionTargetDate(LocalDateTime.now().minusHours(3)); // 3 hours overdue
        testCase.setAssignedTo("staff@dswd.gov.ph");
        
        List<GrievanceCase> overdueCases = Arrays.asList(testCase);
        when(caseRepository.findOverdueCases(any(LocalDateTime.class))).thenReturn(overdueCases);
        when(caseAssignmentService.findEscalationTarget("staff@dswd.gov.ph", testCase.getCategory()))
            .thenReturn("director@dswd.gov.ph");
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processOverdueCases();

        // Assert
        verify(caseRepository).findOverdueCases(any(LocalDateTime.class));
        verify(caseAssignmentService).findEscalationTarget("staff@dswd.gov.ph", testCase.getCategory());
        verify(notificationService).sendOverdueNotification(testCase);
        verify(caseRepository).save(testCase);
        
        // Verify escalation occurred
        assertEquals("director@dswd.gov.ph", testCase.getEscalatedTo());
        assertEquals(GrievanceCase.CaseStatus.ESCALATED, testCase.getStatus());
    }

    @Test
    void testProcessOverdueCases_SignificantlyOverdue_PriorityIncrease() {
        // Arrange - Create a separate case that doesn't auto-upgrade to HIGH
        GrievanceCase lowPriorityCase = new GrievanceCase();
        lowPriorityCase.setId(UUID.randomUUID());
        lowPriorityCase.setCaseNumber("GRV-2024-002");
        lowPriorityCase.setComplainantPsn("123456789012");
        lowPriorityCase.setComplainantName("Jane Doe");
        lowPriorityCase.setComplainantEmail("jane.doe@email.com");
        lowPriorityCase.setComplainantPhone("+639123456789");
        lowPriorityCase.setSubject("General inquiry");
        lowPriorityCase.setDescription("General question about services");
        lowPriorityCase.setCategory(GrievanceCase.GrievanceCategory.OTHER);
        lowPriorityCase.setPriority(GrievanceCase.Priority.LOW);
        lowPriorityCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);
        lowPriorityCase.setResolutionTargetDate(LocalDateTime.now().minusDays(4)); // 4 days overdue
        lowPriorityCase.setAssignedTo("staff@dswd.gov.ph");
        lowPriorityCase.setActivities(new ArrayList<>());

        List<GrievanceCase> overdueCases = Arrays.asList(lowPriorityCase);
        when(caseRepository.findOverdueCases(any(LocalDateTime.class))).thenReturn(overdueCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        workflowService.processOverdueCases();

        // Assert
        verify(caseRepository).findOverdueCases(any(LocalDateTime.class));
        verify(notificationService).sendOverdueNotification(lowPriorityCase);
        verify(caseRepository, times(2)).save(lowPriorityCase); // Once for escalation, once for priority change

        // Verify priority was increased (LOW -> MEDIUM via escalation -> HIGH via overdue adjustment)
        assertEquals(GrievanceCase.Priority.HIGH, lowPriorityCase.getPriority());
    }

    @Test
    void testProcessOverdueCases_RecentlyEscalated_NoNewEscalation() {
        // Arrange
        testCase.setResolutionTargetDate(LocalDateTime.now().minusDays(2)); // Overdue
        testCase.setEscalationDate(LocalDateTime.now().minusHours(12)); // Recently escalated
        testCase.setAssignedTo("staff@dswd.gov.ph");
        
        List<GrievanceCase> overdueCases = Arrays.asList(testCase);
        when(caseRepository.findOverdueCases(any(LocalDateTime.class))).thenReturn(overdueCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        workflowService.processOverdueCases();

        // Assert
        verify(caseRepository).findOverdueCases(any(LocalDateTime.class));
        verify(caseAssignmentService, never()).findEscalationTarget(anyString(), any());
        verify(notificationService).sendOverdueNotification(testCase);
        verify(caseRepository).save(testCase);
    }

    @Test
    void testProcessOverdueCases_ExceptionHandling() {
        // Arrange
        testCase.setResolutionTargetDate(LocalDateTime.now().minusDays(2));
        
        List<GrievanceCase> overdueCases = Arrays.asList(testCase);
        when(caseRepository.findOverdueCases(any(LocalDateTime.class))).thenReturn(overdueCases);
        doThrow(new RuntimeException("Notification service error"))
            .when(notificationService).sendOverdueNotification(testCase);

        // Act & Assert
        assertDoesNotThrow(() -> workflowService.processOverdueCases());
        verify(caseRepository).findOverdueCases(any(LocalDateTime.class));
        verify(notificationService).sendOverdueNotification(testCase);
    }

    // Note: Tests for private methods (analyzePriority, setSLADeadlines) are covered
    // through integration testing in processNewCaseWorkflow tests above
}
