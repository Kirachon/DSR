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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdvancedSLAMonitoringService
 */
@ExtendWith(MockitoExtension.class)
class AdvancedSLAMonitoringServiceTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private WorkflowAutomationService workflowAutomationService;

    @InjectMocks
    private AdvancedSLAMonitoringService slaMonitoringService;

    private GrievanceCase testCase;

    @BeforeEach
    void setUp() {
        testCase = new GrievanceCase();
        testCase.setId(UUID.randomUUID());
        testCase.setCaseNumber("GRV-2025-001");
        testCase.setComplainantPsn("PSN123456789");
        testCase.setSubject("Test Case");
        testCase.setDescription("Test case description");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        testCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(100)); // 100 hours ago
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(68)); // 68 hours from now (168 total for MEDIUM)
        testCase.setAssignedTo("staff@dswd.gov.ph");
    }

    @Test
    void testCalculateSLAStatus_OnTrack_Success() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(50));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(118)); // 168 total for MEDIUM

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.ON_TRACK, status.getStatus());
        assertEquals(testCase.getId(), status.getCaseId());
        assertEquals(testCase.getCaseNumber(), status.getCaseNumber());
        assertEquals(testCase.getPriority(), status.getPriority());
        assertFalse(status.isOverdue());
        assertTrue(status.getPercentageElapsed() < 0.5);
    }

    @Test
    void testCalculateSLAStatus_ApproachingWarning_Success() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(90));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(78)); // 168 total, ~53% elapsed

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.APPROACHING_WARNING, status.getStatus());
        assertTrue(status.getPercentageElapsed() > 0.5);
        assertTrue(status.getPercentageElapsed() < 0.7);
        assertFalse(status.isOverdue());
    }

    @Test
    void testCalculateSLAStatus_Warning_Success() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(120));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(48)); // 168 total, ~71% elapsed

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.WARNING, status.getStatus());
        assertTrue(status.getPercentageElapsed() > 0.7);
        assertTrue(status.getPercentageElapsed() < 0.9);
        assertFalse(status.isOverdue());
    }

    @Test
    void testCalculateSLAStatus_ApproachingBreach_Success() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(155));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(13)); // 168 total, ~92% elapsed

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.APPROACHING_BREACH, status.getStatus());
        assertTrue(status.getPercentageElapsed() > 0.9);
        assertFalse(status.isOverdue());
    }

    @Test
    void testCalculateSLAStatus_Breached_Success() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(180));
        testCase.setResolutionTargetDate(LocalDateTime.now().minusHours(12)); // Overdue by 12 hours

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.BREACHED, status.getStatus());
        assertTrue(status.isOverdue());
        assertTrue(status.getTimeRemaining().isNegative());
    }

    @Test
    void testCalculateSLAStatus_CriticalBreach_Success() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(300));
        testCase.setResolutionTargetDate(LocalDateTime.now().minusHours(132)); // Significantly overdue

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.CRITICAL_BREACH, status.getStatus());
        assertTrue(status.isOverdue());
        assertTrue(status.getPercentageElapsed() > 1.5);
    }

    @Test
    void testCalculateSLAStatus_CriticalPriority_HighRisk() {
        // Arrange
        testCase.setPriority(GrievanceCase.Priority.CRITICAL);
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(15));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(9)); // 24 total for CRITICAL, ~62% elapsed

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.RiskLevel.HIGH, status.getRiskLevel());
        assertEquals(GrievanceCase.Priority.CRITICAL, status.getPriority());
    }

    @Test
    void testCalculateSLAStatus_MissingTargetDate_SetsTargetDate() {
        // Arrange
        testCase.setResolutionTargetDate(null);
        testCase.setPriority(GrievanceCase.Priority.HIGH);

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getTargetDate());
        assertNotNull(testCase.getResolutionTargetDate());
        // HIGH priority should have 72 hours SLA
        assertEquals(testCase.getSubmissionDate().plusHours(72), testCase.getResolutionTargetDate());
    }

    @Test
    void testPerformRealTimeSLAMonitoring_Success() {
        // Arrange
        List<GrievanceCase> activeCases = Arrays.asList(testCase);
        when(caseRepository.findByStatusIn(any())).thenReturn(activeCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        slaMonitoringService.performRealTimeSLAMonitoring();

        // Assert
        verify(caseRepository).findByStatusIn(any());
        verify(caseRepository, atLeastOnce()).save(testCase);
    }

    @Test
    void testPerformRealTimeSLAMonitoring_WarningCase_SendsNotification() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(120));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(48)); // Warning threshold
        
        List<GrievanceCase> activeCases = Arrays.asList(testCase);
        when(caseRepository.findByStatusIn(any())).thenReturn(activeCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        slaMonitoringService.performRealTimeSLAMonitoring();

        // Assert
        verify(notificationService).sendSLAWarningNotification(eq(testCase), any());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testPerformRealTimeSLAMonitoring_BreachedCase_TriggersEscalation() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(180));
        testCase.setResolutionTargetDate(LocalDateTime.now().minusHours(12)); // Breached
        
        List<GrievanceCase> activeCases = Arrays.asList(testCase);
        when(caseRepository.findByStatusIn(any())).thenReturn(activeCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        slaMonitoringService.performRealTimeSLAMonitoring();

        // Assert
        verify(notificationService).sendSLABreachNotification(eq(testCase), any());
        verify(workflowAutomationService).processOverdueCases();
        assertEquals(GrievanceCase.CaseStatus.ESCALATED, testCase.getStatus());
    }

    @Test
    void testPerformRealTimeSLAMonitoring_CriticalBreach_EscalatesToHighestLevel() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(300));
        testCase.setResolutionTargetDate(LocalDateTime.now().minusHours(132)); // Critical breach
        
        List<GrievanceCase> activeCases = Arrays.asList(testCase);
        when(caseRepository.findByStatusIn(any())).thenReturn(activeCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        slaMonitoringService.performRealTimeSLAMonitoring();

        // Assert
        verify(notificationService).sendSLACriticalBreachNotification(eq(testCase), any());
        assertTrue(testCase.getEscalationLevel() >= 3);
        assertEquals("director@dswd.gov.ph", testCase.getAssignedTo());
    }

    @Test
    void testPerformRealTimeSLAMonitoring_Exception_HandlesGracefully() {
        // Arrange
        when(caseRepository.findByStatusIn(any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertDoesNotThrow(() -> slaMonitoringService.performRealTimeSLAMonitoring());
    }

    @Test
    void testCalculateSLAStatus_LowPriority_LowerRisk() {
        // Arrange
        testCase.setPriority(GrievanceCase.Priority.LOW);
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(200));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(136)); // 336 total for LOW, ~59% elapsed

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertEquals(AdvancedSLAMonitoringService.RiskLevel.LOW, status.getRiskLevel());
        assertEquals(GrievanceCase.Priority.LOW, status.getPriority());
    }

    @Test
    void testPerformRealTimeSLAMonitoring_ApproachingBreach_PreparesEscalation() {
        // Arrange
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(155));
        testCase.setResolutionTargetDate(LocalDateTime.now().plusHours(13)); // Approaching breach
        
        List<GrievanceCase> activeCases = Arrays.asList(testCase);
        when(caseRepository.findByStatusIn(any())).thenReturn(activeCases);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        slaMonitoringService.performRealTimeSLAMonitoring();

        // Assert
        verify(notificationService).sendSLAUrgentNotification(eq(testCase), any());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testCalculateSLAStatus_HighPriority_CorrectSLA() {
        // Arrange
        testCase.setPriority(GrievanceCase.Priority.HIGH);
        testCase.setSubmissionDate(LocalDateTime.now().minusHours(36));
        testCase.setResolutionTargetDate(null); // Will be calculated

        // Act
        AdvancedSLAMonitoringService.SLAStatus status = slaMonitoringService.calculateSLAStatus(testCase);

        // Assert
        assertNotNull(status);
        assertNotNull(testCase.getResolutionTargetDate());
        // HIGH priority should have 72 hours SLA
        assertEquals(testCase.getSubmissionDate().plusHours(72), testCase.getResolutionTargetDate());
        assertEquals(AdvancedSLAMonitoringService.SLAStatusType.WARNING, status.getStatus());
    }
}
