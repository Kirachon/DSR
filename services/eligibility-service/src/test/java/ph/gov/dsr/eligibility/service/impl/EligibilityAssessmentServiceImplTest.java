package ph.gov.dsr.eligibility.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EligibilityAssessmentServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class EligibilityAssessmentServiceImplTest {

    @Mock
    private PmtCalculatorService pmtCalculatorService;

    @Mock
    private EligibilityRulesEngineService rulesEngineService;

    @InjectMocks
    private EligibilityAssessmentServiceImpl eligibilityAssessmentService;

    private EligibilityRequest testRequest;
    private PmtCalculatorService.PmtCalculationResult pmtResult;
    private EligibilityRulesEngineService.RuleEvaluationResult rulesResult;

    @BeforeEach
    void setUp() {
        testRequest = new EligibilityRequest();
        testRequest.setPsn("1234-5678-9012");
        testRequest.setProgramCode("4PS_CONDITIONAL_CASH");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-001");
        householdInfo.setMonthlyIncome(new BigDecimal("15000"));
        householdInfo.setTotalMembers(5);
        testRequest.setHouseholdInfo(householdInfo);

        // Setup PMT result
        pmtResult = new PmtCalculatorService.PmtCalculationResult();
        pmtResult.setPmtScore(new BigDecimal("25000"));
        pmtResult.setPovertyThreshold(new BigDecimal("36000"));
        pmtResult.setPoor(true);
        pmtResult.setRegion("NCR");
        pmtResult.setHouseholdSize(5);
        pmtResult.setCalculatedAt(LocalDateTime.now());

        // Setup rules result
        rulesResult = new EligibilityRulesEngineService.RuleEvaluationResult();
        rulesResult.setPassed(true);
        rulesResult.setRuleSetName("4PS_ELIGIBILITY_RULES");
        rulesResult.setEvaluatedAt(LocalDateTime.now());
        rulesResult.setRuleResults(new ArrayList<>());
    }

    @Test
    void testAssessEligibility_Eligible() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("1234-5678-9012", response.getPsn());
        assertEquals("4PS_CONDITIONAL_CASH", response.getProgramCode());
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertEquals(new BigDecimal("25000"), response.getPmtScore());
        assertEquals(new BigDecimal("36000"), response.getPovertyThreshold());
        assertNotNull(response.getAssessedAt());
        assertNotNull(response.getAssessmentDetails());
        assertTrue(response.getStatusReason().contains("meets both PMT and categorical eligibility criteria"));
    }

    @Test
    void testAssessEligibility_Ineligible_PMT() {
        // Arrange - PMT fails
        pmtResult.setPoor(false);
        pmtResult.setPmtScore(new BigDecimal("40000"));
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.CONDITIONAL, response.getStatus());
        assertTrue(response.getStatusReason().contains("some but not all eligibility criteria"));
    }

    @Test
    void testAssessEligibility_Ineligible_Rules() {
        // Arrange - Rules fail
        rulesResult.setPassed(false);
        rulesResult.setFailureReason("No school-age children");
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.CONDITIONAL, response.getStatus());
        assertTrue(response.getStatusReason().contains("some but not all eligibility criteria"));
    }

    @Test
    void testAssessEligibility_Ineligible_Both() {
        // Arrange - Both PMT and rules fail
        pmtResult.setPoor(false);
        rulesResult.setPassed(false);
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.INELIGIBLE, response.getStatus());
        assertTrue(response.getStatusReason().contains("PMT score above poverty threshold"));
        assertTrue(response.getStatusReason().contains("Does not meet categorical eligibility criteria"));
    }

    @Test
    void testAssessEligibility_WithCache() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act - First assessment
        EligibilityResponse response1 = eligibilityAssessmentService.assessEligibility(testRequest);
        
        // Act - Second assessment (should use cache)
        EligibilityResponse response2 = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getStatus(), response2.getStatus());
        assertEquals(response1.getPmtScore(), response2.getPmtScore());
    }

    @Test
    void testAssessEligibility_ForceReassessment() {
        // Arrange
        testRequest.setForceReassessment(true);
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
    }

    @Test
    void testAssessEligibility_Error() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class)))
                .thenThrow(new RuntimeException("PMT calculation failed"));

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.ERROR, response.getStatus());
        assertTrue(response.getStatusReason().contains("Assessment failed"));
    }

    @Test
    void testGetEligibilityHistory() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);
        
        // Create an assessment first
        eligibilityAssessmentService.assessEligibility(testRequest);

        // Act
        List<EligibilityResponse> history = eligibilityAssessmentService.getEligibilityHistory(
                "1234-5678-9012", "4PS_CONDITIONAL_CASH");

        // Assert
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(1, history.size());
        assertEquals("1234-5678-9012", history.get(0).getPsn());
    }

    @Test
    void testCalculateEligibilityScore() {
        // Arrange
        Map<String, Object> pmtBreakdown = new HashMap<>();
        pmtBreakdown.put("pmtScore", new BigDecimal("25000"));
        pmtBreakdown.put("povertyThreshold", new BigDecimal("36000"));
        
        when(pmtCalculatorService.getPmtScoreBreakdown(any(EligibilityRequest.class))).thenReturn(pmtBreakdown);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        Map<String, Object> scoreBreakdown = eligibilityAssessmentService.calculateEligibilityScore(testRequest);

        // Assert
        assertNotNull(scoreBreakdown);
        assertTrue(scoreBreakdown.containsKey("pmtCalculation"));
        assertTrue(scoreBreakdown.containsKey("rulesEvaluation"));
        assertTrue(scoreBreakdown.containsKey("overallScore"));
        assertTrue(scoreBreakdown.containsKey("weights"));
        assertTrue(scoreBreakdown.containsKey("calculatedAt"));
    }

    @Test
    void testIsAssessmentValid_Valid() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);
        
        // Create an assessment
        eligibilityAssessmentService.assessEligibility(testRequest);

        // Act
        boolean isValid = eligibilityAssessmentService.isAssessmentValid("1234-5678-9012", "4PS_CONDITIONAL_CASH");

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsAssessmentValid_NotFound() {
        // Act
        boolean isValid = eligibilityAssessmentService.isAssessmentValid("9999-9999-9999", "UNKNOWN_PROGRAM");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testInvalidateAssessment() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);
        
        // Create an assessment
        eligibilityAssessmentService.assessEligibility(testRequest);
        assertTrue(eligibilityAssessmentService.isAssessmentValid("1234-5678-9012", "4PS_CONDITIONAL_CASH"));

        // Act
        eligibilityAssessmentService.invalidateAssessment("1234-5678-9012", "4PS_CONDITIONAL_CASH", "Test invalidation");

        // Assert
        assertFalse(eligibilityAssessmentService.isAssessmentValid("1234-5678-9012", "4PS_CONDITIONAL_CASH"));
    }

    @Test
    void testGetEligibilityStatistics() {
        // Arrange
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);
        
        // Create some assessments
        eligibilityAssessmentService.assessEligibility(testRequest);

        // Act
        Map<String, Object> stats = eligibilityAssessmentService.getEligibilityStatistics("4PS_CONDITIONAL_CASH");

        // Assert
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalAssessments"));
        assertTrue(stats.containsKey("eligibleCount"));
        assertTrue(stats.containsKey("ineligibleCount"));
        assertTrue(stats.containsKey("eligibilityRate"));
        assertTrue(stats.containsKey("generatedAt"));
        assertEquals(1L, stats.get("totalAssessments"));
        assertEquals(1L, stats.get("eligibleCount"));
    }

    @Test
    void testBatchAssessEligibility() {
        // Arrange
        List<EligibilityRequest> requests = new ArrayList<>();
        requests.add(testRequest);
        
        EligibilityRequest request2 = new EligibilityRequest();
        request2.setPsn("2345-6789-0123");
        request2.setProgramCode("4PS_CONDITIONAL_CASH");
        request2.setHouseholdInfo(testRequest.getHouseholdInfo());
        requests.add(request2);
        
        when(pmtCalculatorService.calculatePmtScore(any(EligibilityRequest.class))).thenReturn(pmtResult);
        when(rulesEngineService.evaluateEligibilityRules(any(EligibilityRequest.class), eq("4PS_CONDITIONAL_CASH")))
                .thenReturn(rulesResult);

        // Act
        List<EligibilityResponse> responses = eligibilityAssessmentService.batchAssessEligibility(requests);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("1234-5678-9012", responses.get(0).getPsn());
        assertEquals("2345-6789-0123", responses.get(1).getPsn());
    }

    @Test
    void testUpdateEligibilityStatus() {
        // Act
        EligibilityResponse response = eligibilityAssessmentService.updateEligibilityStatus(
                "1234-5678-9012", "4PS_CONDITIONAL_CASH", 
                EligibilityResponse.EligibilityStatus.ELIGIBLE, 
                "Manual override", "admin-user");

        // Assert
        assertNotNull(response);
        assertEquals("1234-5678-9012", response.getPsn());
        assertEquals("4PS_CONDITIONAL_CASH", response.getProgramCode());
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertEquals("Manual override", response.getStatusReason());
        assertEquals("admin-user", response.getLastUpdatedBy());
        assertNotNull(response.getLastUpdatedAt());
    }
}
