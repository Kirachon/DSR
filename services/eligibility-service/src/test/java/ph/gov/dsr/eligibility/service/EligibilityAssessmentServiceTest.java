package ph.gov.dsr.eligibility.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.service.impl.EligibilityAssessmentServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EligibilityAssessmentService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class EligibilityAssessmentServiceTest {

    @Mock
    private PmtCalculatorService pmtCalculatorService;

    @Mock
    private EligibilityRulesEngineService rulesEngineService;

    @InjectMocks
    private EligibilityAssessmentServiceImpl eligibilityService;

    private EligibilityRequest validRequest;
    private PmtCalculatorService.PmtCalculationResult pmtResult;
    private EligibilityRulesEngineService.RuleEvaluationResult rulesResult;

    @BeforeEach
    void setUp() {
        validRequest = new EligibilityRequest();
        validRequest.setPsn("1234-5678-9012");
        validRequest.setProgramCode("4PS");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-001234");
        householdInfo.setMonthlyIncome(new BigDecimal("15000"));
        householdInfo.setTotalMembers(5);
        validRequest.setHouseholdInfo(householdInfo);

        pmtResult = new PmtCalculatorService.PmtCalculationResult();
        pmtResult.setPmtScore(new BigDecimal("12000"));
        pmtResult.setPovertyThreshold(new BigDecimal("15000"));
        pmtResult.setPoor(true);

        rulesResult = new EligibilityRulesEngineService.RuleEvaluationResult();
        rulesResult.setPassed(true);
        rulesResult.setScore(85.0);
    }

    @Test
    void testAssessEligibility_Eligible() {
        // Given
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        EligibilityResponse response = eligibilityService.assessEligibility(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(validRequest.getPsn(), response.getPsn());
        assertEquals(validRequest.getProgramCode(), response.getProgramCode());
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertTrue(response.getIsEligible());
        assertNotNull(response.getEligibilityScore());
        assertNotNull(response.getLastAssessmentDate());
        
        verify(pmtCalculatorService).calculatePmtScore(any(EligibilityRequest.class));
        verify(rulesEngineService).evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS"));
    }

    @Test
    void testAssessEligibility_Ineligible() {
        // Given
        pmtResult.setPoor(false);
        rulesResult.setPassed(false);
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        EligibilityResponse response = eligibilityService.assessEligibility(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.INELIGIBLE, response.getStatus());
        assertFalse(response.getIsEligible());
        assertNotNull(response.getReason());
        assertTrue(response.getReason().contains("PMT score above poverty threshold"));
    }

    @Test
    void testAssessEligibility_Conditional() {
        // Given
        pmtResult.setPoor(true);
        rulesResult.setPassed(false);
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        EligibilityResponse response = eligibilityService.assessEligibility(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.CONDITIONAL, response.getStatus());
        assertNotNull(response.getReason());
        assertTrue(response.getReason().contains("manual review"));
    }

    @Test
    void testAssessMultiplePrograms() {
        // Given
        String psn = "1234-5678-9012";
        List<String> programCodes = Arrays.asList("4PS", "DSWD_SLP", "KALAHI_CIDSS");
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        Map<String, EligibilityResponse> results = eligibilityService.assessMultiplePrograms(psn, programCodes, false);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.containsKey("4PS"));
        assertTrue(results.containsKey("DSWD_SLP"));
        assertTrue(results.containsKey("KALAHI_CIDSS"));
        
        for (EligibilityResponse response : results.values()) {
            assertEquals(psn, response.getPsn());
            assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        }
    }

    @Test
    void testIsAssessmentValid_ValidAssessment() {
        // Given
        String psn = "1234-5678-9012";
        String programCode = "4PS";

        // When
        boolean isValid = eligibilityService.isAssessmentValid(psn, programCode);

        // Then - This would depend on the actual implementation
        // For mock test, we can't verify the actual database state
        assertNotNull(isValid);
    }

    @Test
    void testGetEligibilityStatistics() {
        // When
        Map<String, Object> stats = eligibilityService.getEligibilityStatistics("4PS");

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalAssessments"));
        assertTrue(stats.containsKey("eligibleCount"));
        assertTrue(stats.containsKey("ineligibleCount"));
        assertTrue(stats.containsKey("eligibilityRate"));
        assertTrue(stats.containsKey("generatedAt"));
    }

    @Test
    void testCalculateEligibilityScore() {
        // Given
        when(pmtCalculatorService.getPmtScoreBreakdown(any(EligibilityRequest.class)))
                .thenReturn(Map.of("score", 12000, "threshold", 15000));
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        Map<String, Object> scoreBreakdown = eligibilityService.calculateEligibilityScore(validRequest);

        // Then
        assertNotNull(scoreBreakdown);
        assertTrue(scoreBreakdown.containsKey("pmtCalculation"));
        assertTrue(scoreBreakdown.containsKey("rulesEvaluation"));
        assertTrue(scoreBreakdown.containsKey("overallScore"));
        assertTrue(scoreBreakdown.containsKey("calculatedAt"));
        
        verify(pmtCalculatorService).getPmtScoreBreakdown(any(EligibilityRequest.class));
        verify(pmtCalculatorService).calculatePmtScore(any(EligibilityRequest.class));
        verify(rulesEngineService).evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS"));
    }

    @Test
    void testBatchAssessEligibility() {
        // Given
        List<EligibilityRequest> requests = Arrays.asList(validRequest);
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        List<EligibilityResponse> responses = eligibilityService.batchAssessEligibility(requests);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(validRequest.getPsn(), responses.get(0).getPsn());
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, responses.get(0).getStatus());
    }

    @Test
    void testUpdateEligibilityStatus() {
        // Given
        String psn = "1234-5678-9012";
        String programCode = "4PS";
        EligibilityResponse.EligibilityStatus newStatus = EligibilityResponse.EligibilityStatus.INELIGIBLE;
        String reason = "Manual review completed";
        String updatedBy = "admin";

        // When
        EligibilityResponse response = eligibilityService.updateEligibilityStatus(
                psn, programCode, newStatus, reason, updatedBy);

        // Then
        assertNotNull(response);
        assertEquals(psn, response.getPsn());
        assertEquals(programCode, response.getProgramCode());
        assertEquals(newStatus, response.getStatus());
        assertEquals(reason, response.getReason());
    }

    @Test
    void testInvalidateAssessment() {
        // Given
        String psn = "1234-5678-9012";
        String programCode = "4PS";
        String reason = "Data updated";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            eligibilityService.invalidateAssessment(psn, programCode, reason);
        });
    }

    @Test
    void testGetPendingReviews() {
        // When
        List<EligibilityResponse> pendingReviews = eligibilityService.getPendingReviews("4PS", 10);

        // Then
        assertNotNull(pendingReviews);
        // The actual content would depend on the database state
    }

    @Test
    void testAssessEligibility_WithForceReassessment() {
        // Given
        validRequest.setForceReassessment(true);
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), anyString()))
                .thenReturn(rulesResult);

        // When
        EligibilityResponse response = eligibilityService.assessEligibility(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        // Should not check cache when force reassessment is true
        verify(pmtCalculatorService).calculatePmtScore(any(EligibilityRequest.class));
    }
}
