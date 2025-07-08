package ph.gov.dsr.eligibility.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.dto.EligibilityResponse;
import ph.gov.dsr.eligibility.dto.ProgramInfo;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;
import ph.gov.dsr.eligibility.service.ProgramManagementService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Production Integration tests for Eligibility Service
 * Tests production implementations with database persistence
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("local") // Use local profile to test production implementations
@Transactional
class ProductionEligibilityServiceIntegrationTest {

    @Autowired
    private EligibilityAssessmentService eligibilityAssessmentService;

    @Autowired
    private PmtCalculatorService pmtCalculatorService;

    @Autowired
    private EligibilityRulesEngineService rulesEngineService;

    @Autowired
    private ProgramManagementService programManagementService;

    @Test
    void testProductionEligibilityAssessmentWorkflow_4PS() {
        // Arrange
        EligibilityRequest request = create4PSEligibleHousehold();

        // Act & Assert - Test PMT calculation
        PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);
        assertNotNull(pmtResult);
        assertTrue(pmtResult.isPoor());
        assertTrue(pmtResult.getPmtScore().compareTo(BigDecimal.ZERO) >= 0);

        // Act & Assert - Test rules engine
        EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
            rulesEngineService.evaluateEligibilityRules(request, "4PS");
        assertNotNull(rulesResult);
        assertTrue(rulesResult.isPassed());

        // Act & Assert - Test complete assessment
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);
        assertNotNull(response);
        assertEquals(EligibilityResponse.EligibilityStatus.ELIGIBLE, response.getStatus());
        assertTrue(response.getIsEligible());
        assertNotNull(response.getAssessedAt());
        assertNotNull(response.getValidUntil());
    }

    @Test
    void testProductionProgramManagementService() {
        // Test getting all programs
        List<ProgramInfo> allPrograms = programManagementService.getAllPrograms(true);
        assertNotNull(allPrograms);
        assertFalse(allPrograms.isEmpty());
        
        // Verify 4PS program exists
        ProgramInfo fourPs = programManagementService.getProgramByCode("4PS");
        assertNotNull(fourPs);
        assertEquals("4PS", fourPs.getProgramCode());
        assertEquals("Pantawid Pamilyang Pilipino Program", fourPs.getProgramName());
        assertEquals(ProgramInfo.ProgramStatus.ACTIVE, fourPs.getStatus());
        
        // Test program search
        List<ProgramInfo> searchResults = programManagementService.searchPrograms("Pantawid", true);
        assertNotNull(searchResults);
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(p -> "4PS".equals(p.getProgramCode())));
        
        // Test program statistics
        Map<String, Object> stats = programManagementService.getProgramStatistics("4PS");
        assertNotNull(stats);
        assertFalse(stats.isEmpty());
        assertTrue(stats.containsKey("totalBeneficiaries"));
    }

    @Test
    void testProductionServiceIntegration() {
        // Test integration between PMT calculator and rules engine
        EligibilityRequest request = createSeniorCitizenEligibleHousehold();
        
        // PMT calculation
        PmtCalculatorService.PmtCalculationResult pmtResult = pmtCalculatorService.calculatePmtScore(request);
        assertNotNull(pmtResult);
        
        // Rules evaluation using PMT result
        EligibilityRulesEngineService.RuleEvaluationResult rulesResult = 
            rulesEngineService.evaluateEligibilityRules(request, "SCP");
        assertNotNull(rulesResult);
        
        // Complete assessment combining both
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);
        assertNotNull(response);
        assertNotNull(response.getStatus());
        assertNotNull(response.getAssessmentDetails());
    }

    @Test
    void testProductionBatchAssessment() {
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
    void testProductionErrorHandling() {
        // Test null request
        assertThrows(IllegalArgumentException.class, () -> {
            eligibilityAssessmentService.assessEligibility(null);
        });
        
        // Test invalid PSN
        EligibilityRequest invalidRequest = new EligibilityRequest();
        invalidRequest.setPsn("invalid");
        invalidRequest.setProgramCode("4PS");
        
        assertThrows(IllegalArgumentException.class, () -> {
            eligibilityAssessmentService.assessEligibility(invalidRequest);
        });
        
        // Test invalid program code
        assertThrows(IllegalArgumentException.class, () -> {
            programManagementService.getProgramByCode("");
        });
    }

    @Test
    void testProductionPerformance() {
        // Test that assessment completes within reasonable time
        EligibilityRequest request = create4PSEligibleHousehold();
        
        long startTime = System.currentTimeMillis();
        EligibilityResponse response = eligibilityAssessmentService.assessEligibility(request);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(response);
        assertTrue((endTime - startTime) < 5000, "Assessment should complete within 5 seconds");
    }

    // Helper methods to create test data
    private EligibilityRequest create4PSEligibleHousehold() {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn("1234567890123456");
        request.setProgramCode("4PS");
        request.setHouseholdSize(5);
        request.setMonthlyIncome(new BigDecimal("8000"));
        request.setHasChildren(true);
        request.setChildrenCount(3);
        request.setElderlyCount(0);
        request.setPwdCount(0);
        request.setPregnantCount(1);
        request.setRegion("NCR");
        request.setProvince("Metro Manila");
        request.setCity("Quezon City");
        request.setBarangay("Barangay 123");
        
        // Add household members
        List<Map<String, Object>> members = new ArrayList<>();
        Map<String, Object> head = new HashMap<>();
        head.put("relationship", "HEAD");
        head.put("age", 35);
        head.put("gender", "FEMALE");
        head.put("civilStatus", "MARRIED");
        members.add(head);
        
        Map<String, Object> spouse = new HashMap<>();
        spouse.put("relationship", "SPOUSE");
        spouse.put("age", 38);
        spouse.put("gender", "MALE");
        spouse.put("civilStatus", "MARRIED");
        members.add(spouse);
        
        request.setHouseholdMembers(members);
        
        return request;
    }

    private EligibilityRequest createSeniorCitizenEligibleHousehold() {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn("9876543210987654");
        request.setProgramCode("SCP");
        request.setHouseholdSize(2);
        request.setMonthlyIncome(new BigDecimal("5000"));
        request.setHasChildren(false);
        request.setChildrenCount(0);
        request.setElderlyCount(1);
        request.setPwdCount(0);
        request.setPregnantCount(0);
        request.setRegion("Region IV-A");
        request.setProvince("Laguna");
        request.setCity("Los Baños");
        request.setBarangay("Barangay Poblacion");
        
        // Add household members
        List<Map<String, Object>> members = new ArrayList<>();
        Map<String, Object> senior = new HashMap<>();
        senior.put("relationship", "HEAD");
        senior.put("age", 68);
        senior.put("gender", "MALE");
        senior.put("civilStatus", "WIDOWED");
        members.add(senior);
        
        request.setHouseholdMembers(members);
        
        return request;
    }

    private EligibilityRequest createHighIncomeHousehold() {
        EligibilityRequest request = new EligibilityRequest();
        request.setPsn("5555666677778888");
        request.setProgramCode("4PS");
        request.setHouseholdSize(4);
        request.setMonthlyIncome(new BigDecimal("50000")); // High income
        request.setHasChildren(true);
        request.setChildrenCount(2);
        request.setElderlyCount(0);
        request.setPwdCount(0);
        request.setPregnantCount(0);
        request.setRegion("NCR");
        request.setProvince("Metro Manila");
        request.setCity("Makati");
        request.setBarangay("Poblacion");
        
        return request;
    }
}
