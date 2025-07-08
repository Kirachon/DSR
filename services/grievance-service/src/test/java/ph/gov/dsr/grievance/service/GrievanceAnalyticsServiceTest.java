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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GrievanceAnalyticsService
 */
@ExtendWith(MockitoExtension.class)
class GrievanceAnalyticsServiceTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @Mock
    private AdvancedSLAMonitoringService slaMonitoringService;

    @Mock
    private EscalationWorkflowEngine escalationEngine;

    @InjectMocks
    private GrievanceAnalyticsService analyticsService;

    private GrievanceCase resolvedCase;
    private GrievanceCase pendingCase;
    private GrievanceCase escalatedCase;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        resolvedCase = new GrievanceCase();
        resolvedCase.setId(UUID.randomUUID());
        resolvedCase.setCaseNumber("GRV-2025-001");
        resolvedCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        resolvedCase.setPriority(GrievanceCase.Priority.MEDIUM);
        resolvedCase.setStatus(GrievanceCase.CaseStatus.RESOLVED);
        resolvedCase.setSubmissionDate(now.minusDays(5));
        resolvedCase.setResolutionDate(now.minusDays(2));
        resolvedCase.setResolutionTargetDate(now.minusDays(1));
        resolvedCase.setAssignedTo("payment.specialist@dswd.gov.ph");
        resolvedCase.setComplainantSatisfaction("SATISFIED");

        pendingCase = new GrievanceCase();
        pendingCase.setId(UUID.randomUUID());
        pendingCase.setCaseNumber("GRV-2025-002");
        pendingCase.setCategory(GrievanceCase.GrievanceCategory.SYSTEM_ERROR);
        pendingCase.setPriority(GrievanceCase.Priority.HIGH);
        pendingCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);
        pendingCase.setSubmissionDate(now.minusDays(3));
        pendingCase.setAssignedTo("it.support@dswd.gov.ph");

        escalatedCase = new GrievanceCase();
        escalatedCase.setId(UUID.randomUUID());
        escalatedCase.setCaseNumber("GRV-2025-003");
        escalatedCase.setCategory(GrievanceCase.GrievanceCategory.CORRUPTION);
        escalatedCase.setPriority(GrievanceCase.Priority.CRITICAL);
        escalatedCase.setStatus(GrievanceCase.CaseStatus.ESCALATED);
        escalatedCase.setSubmissionDate(now.minusDays(7));
        escalatedCase.setEscalationLevel(2);
        escalatedCase.setAssignedTo("regional.director@dswd.gov.ph");
    }

    @Test
    void testGeneratePerformanceDashboard_30Days_Success() {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(resolvedCase, pendingCase, escalatedCase);
        when(caseRepository.findBySubmissionDateBetween(any(), any())).thenReturn(cases);
        when(escalationEngine.getEscalationAnalytics()).thenReturn(Map.of("totalEscalations", 1));

        // Act
        Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard("30d");

        // Assert
        assertNotNull(dashboard);
        assertTrue(dashboard.containsKey("coreMetrics"));
        assertTrue(dashboard.containsKey("slaPerformance"));
        assertTrue(dashboard.containsKey("categoryAnalysis"));
        assertTrue(dashboard.containsKey("resolutionTrends"));
        assertTrue(dashboard.containsKey("staffPerformance"));
        assertTrue(dashboard.containsKey("escalationAnalytics"));
        assertTrue(dashboard.containsKey("satisfactionMetrics"));
        assertTrue(dashboard.containsKey("predictiveInsights"));
        assertTrue(dashboard.containsKey("generatedAt"));
        assertTrue(dashboard.containsKey("timeRange"));

        assertEquals("30d", dashboard.get("timeRange"));
        verify(caseRepository).findBySubmissionDateBetween(any(), any());
    }

    @Test
    void testGeneratePerformanceDashboard_CoreMetrics_Accurate() {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(resolvedCase, pendingCase, escalatedCase);
        when(caseRepository.findBySubmissionDateBetween(any(), any())).thenReturn(cases);
        when(escalationEngine.getEscalationAnalytics()).thenReturn(Map.of());

        // Act
        Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard("7d");

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> coreMetrics = (Map<String, Object>) dashboard.get("coreMetrics");
        
        assertNotNull(coreMetrics);
        assertEquals(3, coreMetrics.get("totalCases"));
        assertEquals(1L, coreMetrics.get("resolvedCases"));
        assertEquals(2L, coreMetrics.get("pendingCases"));
        assertEquals(1L, coreMetrics.get("escalatedCases"));
        
        Double resolutionRate = (Double) coreMetrics.get("resolutionRate");
        assertEquals(1.0/3.0, resolutionRate, 0.01);
    }

    @Test
    void testGeneratePerformanceDashboard_SLAPerformance_Calculated() {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(resolvedCase, pendingCase, escalatedCase);
        when(caseRepository.findBySubmissionDateBetween(any(), any())).thenReturn(cases);
        when(escalationEngine.getEscalationAnalytics()).thenReturn(Map.of());

        // Act
        Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard("30d");

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> slaMetrics = (Map<String, Object>) dashboard.get("slaPerformance");
        
        assertNotNull(slaMetrics);
        assertTrue(slaMetrics.containsKey("complianceByPriority"));
        assertTrue(slaMetrics.containsKey("overallCompliance"));
        assertTrue(slaMetrics.containsKey("breachedCases"));
        assertTrue(slaMetrics.containsKey("breachRate"));
    }

    @Test
    void testGeneratePerformanceDashboard_CategoryAnalysis_Grouped() {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(resolvedCase, pendingCase, escalatedCase);
        when(caseRepository.findBySubmissionDateBetween(any(), any())).thenReturn(cases);
        when(escalationEngine.getEscalationAnalytics()).thenReturn(Map.of());

        // Act
        Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard("30d");

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> categoryAnalysis = (Map<String, Object>) dashboard.get("categoryAnalysis");
        
        assertNotNull(categoryAnalysis);
        assertTrue(categoryAnalysis.containsKey("casesByCategory"));
        assertTrue(categoryAnalysis.containsKey("resolutionTimeByCategory"));
        assertTrue(categoryAnalysis.containsKey("mostProblematicCategories"));

        @SuppressWarnings("unchecked")
        Map<String, Long> casesByCategory = (Map<String, Long>) categoryAnalysis.get("casesByCategory");
        assertEquals(1L, casesByCategory.get("PAYMENT_ISSUE"));
        assertEquals(1L, casesByCategory.get("SYSTEM_ERROR"));
        assertEquals(1L, casesByCategory.get("CORRUPTION"));
    }

    @Test
    void testGeneratePerformanceDashboard_StaffPerformance_Analyzed() {
        // Arrange
        List<GrievanceCase> cases = Arrays.asList(resolvedCase, pendingCase, escalatedCase);
        when(caseRepository.findBySubmissionDateBetween(any(), any())).thenReturn(cases);
        when(escalationEngine.getEscalationAnalytics()).thenReturn(Map.of());

        // Act
        Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard("30d");

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> staffMetrics = (Map<String, Object>) dashboard.get("staffPerformance");
        
        assertNotNull(staffMetrics);
        assertTrue(staffMetrics.containsKey("casesByAssignee"));
        assertTrue(staffMetrics.containsKey("resolutionRateByAssignee"));
        assertTrue(staffMetrics.containsKey("avgResolutionTimeByAssignee"));

        @SuppressWarnings("unchecked")
        Map<String, Long> casesByAssignee = (Map<String, Long>) staffMetrics.get("casesByAssignee");
        assertEquals(1L, casesByAssignee.get("payment.specialist@dswd.gov.ph"));
        assertEquals(1L, casesByAssignee.get("it.support@dswd.gov.ph"));
        assertEquals(1L, casesByAssignee.get("regional.director@dswd.gov.ph"));
    }

    @Test
    void testGetRealTimeAnalytics_Success() {
        // Arrange
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<GrievanceCase> todayCases = Arrays.asList(pendingCase);
        List<GrievanceCase> urgentCases = Arrays.asList(escalatedCase);
        List<GrievanceCase> activeCases = Arrays.asList(pendingCase, escalatedCase);
        
        when(caseRepository.findBySubmissionDateAfter(today)).thenReturn(todayCases);
        when(caseRepository.findByPriorityAndStatusIn(any(), any())).thenReturn(urgentCases);
        when(caseRepository.findByStatusIn(any())).thenReturn(activeCases);
        
        AdvancedSLAMonitoringService.SLAStatus slaStatus = AdvancedSLAMonitoringService.SLAStatus.builder()
            .percentageElapsed(0.8)
            .build();
        when(slaMonitoringService.calculateSLAStatus(any())).thenReturn(slaStatus);

        // Act
        Map<String, Object> realTime = analyticsService.getRealTimeAnalytics();

        // Assert
        assertNotNull(realTime);
        assertTrue(realTime.containsKey("todaySubmissions"));
        assertTrue(realTime.containsKey("todayResolutions"));
        assertTrue(realTime.containsKey("urgentCasesCount"));
        assertTrue(realTime.containsKey("slaWarnings"));
        assertTrue(realTime.containsKey("lastUpdated"));

        assertEquals(1, realTime.get("todaySubmissions"));
        assertEquals(1, realTime.get("urgentCasesCount"));
        assertEquals(2L, realTime.get("slaWarnings")); // Both active cases have high SLA percentage
    }

    @Test
    void testGeneratePerformanceDashboard_Exception_HandlesGracefully() {
        // Arrange
        when(caseRepository.findBySubmissionDateBetween(any(), any()))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        Map<String, Object> dashboard = analyticsService.generatePerformanceDashboard("30d");

        // Assert
        assertNotNull(dashboard);
        assertTrue(dashboard.containsKey("error"));
        assertTrue(dashboard.get("error").toString().contains("Failed to generate dashboard"));
    }

    @Test
    void testGeneratePerformanceDashboard_DifferentTimeRanges_ParsedCorrectly() {
        // Arrange
        when(caseRepository.findBySubmissionDateBetween(any(), any())).thenReturn(Arrays.asList());
        when(escalationEngine.getEscalationAnalytics()).thenReturn(Map.of());

        // Act & Assert
        Map<String, Object> weekDashboard = analyticsService.generatePerformanceDashboard("7d");
        Map<String, Object> monthDashboard = analyticsService.generatePerformanceDashboard("month");
        Map<String, Object> quarterDashboard = analyticsService.generatePerformanceDashboard("quarter");
        Map<String, Object> yearDashboard = analyticsService.generatePerformanceDashboard("year");

        assertNotNull(weekDashboard);
        assertNotNull(monthDashboard);
        assertNotNull(quarterDashboard);
        assertNotNull(yearDashboard);

        assertEquals("7d", weekDashboard.get("timeRange"));
        assertEquals("month", monthDashboard.get("timeRange"));
        assertEquals("quarter", quarterDashboard.get("timeRange"));
        assertEquals("year", yearDashboard.get("timeRange"));
    }
}
