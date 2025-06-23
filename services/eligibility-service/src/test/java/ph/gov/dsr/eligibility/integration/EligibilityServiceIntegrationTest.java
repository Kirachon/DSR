package ph.gov.dsr.eligibility.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Eligibility Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("no-db")
class EligibilityServiceIntegrationTest {

    @Autowired
    private EligibilityAssessmentService eligibilityAssessmentService;

    @Autowired
    private PmtCalculatorService pmtCalculatorService;

    @Autowired
    private EligibilityRulesEngineService rulesEngineService;

    @Test
    void testCompleteEligibilityAssessmentWorkflow_4PS() {
        // Arrange
        EligibilityRequest request = create4PSEligibleHousehold();

        // Act & Assert - Test PMT calculation
        PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);
        assertNotNull(pmtResult);
        assertTrue(pmtResult.isPoor());
        assertEquals("NCR", pmtResult.getRegion());

        // Act & Assert - Test rules evaluation
        EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
            rulesEngineService.evaluateEligibilityRules(request, "4PS_CONDITIONAL_CASH");
        assertNotNull(rulesResult);
        assertTrue(rulesResult.isPassed());

        // Act & Assert - Test complete assessment
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertEquals("1234-5678-9012", response.getPsn());
        assertEquals("4PS_CONDITIONAL_CASH", response.getProgramCode());
    }

    @Test
    void testCompleteEligibilityAssessmentWorkflow_SeniorCitizen() {
        // Arrange
        EligibilityRequest request = createSeniorCitizenEligibleHousehold();

        // Act & Assert - Test complete assessment
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertEquals("SENIOR_CITIZEN_PENSION", response.getProgramCode());
    }

    @Test
    void testIneligibleHousehold_HighIncome() {
        // Arrange
        EligibilityRequest request = createHighIncomeHousehold();

        // Act
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);

        // Assert
        assertNotNull(response);
        assertNotEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertTrue(response.getStatusReason().contains("PMT") || 
                  response.getStatusReason().contains("criteria"));
    }

    @Test
    void testPmtCalculatorWithDifferentRegions() {
        // Arrange
        Map<String, Object> householdData = new HashMap<>();
        householdData.put("totalMembers", 4);
        householdData.put("monthlyIncome", new BigDecimal("20000"));

        // Act & Assert - NCR
        PmtCalculatorService.PmtCalculationResult ncrResult = 
            pmtCalculatorService.calculatePmtScore(householdData, "NCR");
        assertNotNull(ncrResult);
        assertEquals("NCR", ncrResult.getRegion());

        // Act & Assert - Default region
        PmtCalculatorService.PmtCalculationResult defaultResult = 
            pmtCalculatorService.calculatePmtScore(householdData, "REGION_IV");
        assertNotNull(defaultResult);
        assertEquals("REGION_IV", defaultResult.getRegion());

        // Different regions should have different thresholds
        assertNotEquals(ncrResult.getPovertyThreshold(), defaultResult.getPovertyThreshold());
    }

    @Test
    void testRulesEngineWithCustomRules() {
        // Arrange
        Map<String, Object> context = new HashMap<>();
        context.put("childrenCount", 3L);
        context.put("elderlyCount", 1L);
        context.put("receivingOtherBenefits", false);

        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateRuleSet("4PS_ELIGIBILITY_RULES", context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPassed());
        assertNotNull(result.getRuleResults());
        assertFalse(result.getRuleResults().isEmpty());
    }

    @Test
    void testBatchEligibilityAssessment() {
        // Arrange
        List<EligibilityRequest> requests = new ArrayList<>();
        requests.add(create4PSEligibleHousehold());
        requests.add(createSeniorCitizenEligibleHousehold());
        requests.add(createHighIncomeHousehold());

        // Act
        List<EligibilityResponse> responses = eligibilityAssessmentService.batchAssessEligibility(requests);

        // Assert
        assertNotNull(responses);
        assertEquals(3, responses.size());
        
        // Verify each response
        for (EligibilityResponse response : responses) {
            assertNotNull(response);
            assertNotNull(response.getPsn());
            assertNotNull(response.getStatus());
            assertNotNull(response.getAssessedAt());
        }
    }

    @Test
    void testEligibilityScoreCalculation() {
        // Arrange
        EligibilityRequest request = create4PSEligibleHousehold();

        // Act
        Map<String, Object> scoreBreakdown = eligibilityAssessmentService.calculateEligibilityScore(request);

        // Assert
        assertNotNull(scoreBreakdown);
        assertTrue(scoreBreakdown.containsKey("pmtCalculation"));
        assertTrue(scoreBreakdown.containsKey("rulesEvaluation"));
        assertTrue(scoreBreakdown.containsKey("overallScore"));
        assertTrue(scoreBreakdown.containsKey("weights"));

        // Verify score is within valid range
        Double overallScore = (Double) scoreBreakdown.get("overallScore");
        assertTrue(overallScore >= 0.0 && overallScore <= 100.0);
    }

    @Test
    void testServiceAvailability() {
        // Test that all services are properly injected and available
        assertNotNull(eligibilityAssessmentService);
        assertNotNull(pmtCalculatorService);
        assertNotNull(rulesEngineService);

        // Test basic service functionality
        List<String> ruleSets = rulesEngineService.getAvailableRuleSets();
        assertNotNull(ruleSets);
        assertFalse(ruleSets.isEmpty());
    }

    @Test
    void testAssessmentCaching() {
        // Arrange
        EligibilityRequest request = create4PSEligibleHousehold();

        // Act - First assessment
        long startTime1 = System.currentTimeMillis();
        EligibilityResponse response1 = eligibilityAssessmentService.assessEligibility(request);
        long duration1 = System.currentTimeMillis() - startTime1;

        // Act - Second assessment (should use cache)
        long startTime2 = System.currentTimeMillis();
        EligibilityResponse response2 = eligibilityAssessmentService.assessEligibility(request);
        long duration2 = System.currentTimeMillis() - startTime2;

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getStatus(), response2.getStatus());
        assertEquals(response1.getPmtScore(), response2.getPmtScore());
        
        // Second call should be faster (cached)
        assertTrue(duration2 <= duration1);
    }

    private EligibilityRequest create4PSEligibleHousehold() {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn("1234-5678-9012");
        request.setProgramCode("4PS_CONDITIONAL_CASH");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-001");
        householdInfo.setMonthlyIncome(new BigDecimal("15000"));
        householdInfo.setTotalMembers(5);
        householdInfo.setIsIndigenous(false);
        householdInfo.setHasPwdMembers(false);
        
        EligibilityRequest.LocationInfo location = new EligibilityRequest.LocationInfo();
        location.setRegion("NCR");
        location.setProvince("Metro Manila");
        location.setCityMunicipality("Manila");
        householdInfo.setLocation(location);
        
        request.setHouseholdInfo(householdInfo);
        
        // Add household members with children
        List<EligibilityRequest.HouseholdMemberInfo> members = new ArrayList<>();
        
        EligibilityRequest.HouseholdMemberInfo child1 = new EligibilityRequest.HouseholdMemberInfo();
        child1.setAge(8);
        child1.setRelationshipToHead("CHILD");
        members.add(child1);
        
        EligibilityRequest.HouseholdMemberInfo child2 = new EligibilityRequest.HouseholdMemberInfo();
        child2.setAge(12);
        child2.setRelationshipToHead("CHILD");
        members.add(child2);
        
        request.setMembers(members);
        
        return request;
    }

    private EligibilityRequest createSeniorCitizenEligibleHousehold() {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn("2345-6789-0123");
        request.setProgramCode("SENIOR_CITIZEN_PENSION");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-002");
        householdInfo.setMonthlyIncome(new BigDecimal("8000"));
        householdInfo.setTotalMembers(2);
        
        EligibilityRequest.LocationInfo location = new EligibilityRequest.LocationInfo();
        location.setRegion("NCR");
        householdInfo.setLocation(location);
        
        request.setHouseholdInfo(householdInfo);
        
        // Add elderly member
        List<EligibilityRequest.HouseholdMemberInfo> members = new ArrayList<>();
        
        EligibilityRequest.HouseholdMemberInfo elderly = new EligibilityRequest.HouseholdMemberInfo();
        elderly.setAge(65);
        elderly.setRelationshipToHead("HEAD");
        members.add(elderly);
        
        request.setMembers(members);
        
        return request;
    }

    private EligibilityRequest createHighIncomeHousehold() {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn("3456-7890-1234");
        request.setProgramCode("4PS_CONDITIONAL_CASH");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-003");
        householdInfo.setMonthlyIncome(new BigDecimal("80000")); // High income
        householdInfo.setTotalMembers(4);
        
        EligibilityRequest.LocationInfo location = new EligibilityRequest.LocationInfo();
        location.setRegion("NCR");
        householdInfo.setLocation(location);
        
        request.setHouseholdInfo(householdInfo);
        
        return request;
    }
}
