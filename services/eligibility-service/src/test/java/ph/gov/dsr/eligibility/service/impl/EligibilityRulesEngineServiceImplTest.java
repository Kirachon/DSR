package ph.gov.dsr.eligibility.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EligibilityRulesEngineServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class EligibilityRulesEngineServiceImplTest {

    @InjectMocks
    private EligibilityRulesEngineServiceImpl rulesEngineService;

    private EligibilityRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new EligibilityRequest();
        testRequest.setPsn("1234-5678-9012");
        testRequest.setProgramCode("4PS_CONDITIONAL_CASH");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-001");
        householdInfo.setMonthlyIncome(new BigDecimal("15000"));
        householdInfo.setTotalMembers(5);
        householdInfo.setIsIndigenous(false);
        householdInfo.setHasPwdMembers(false);
        testRequest.setHouseholdInfo(householdInfo);
        
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
        
        EligibilityRequest.HouseholdMemberInfo adult = new EligibilityRequest.HouseholdMemberInfo();
        adult.setAge(35);
        adult.setRelationshipToHead("HEAD");
        members.add(adult);
        
        testRequest.setMembers(members);
    }

    @Test
    void testEvaluateEligibilityRules_4PS_Success() {
        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "4PS_CONDITIONAL_CASH");

        // Assert
        assertNotNull(result);
        assertEquals("4PS_ELIGIBILITY_RULES", result.getRuleSetName());
        assertTrue(result.isPassed()); // Should pass because household has children
        assertNotNull(result.getRuleResults());
        assertFalse(result.getRuleResults().isEmpty());
        assertNotNull(result.getEvaluatedAt());
        assertTrue(result.getEvaluationTimeMs() >= 0);
    }

    @Test
    void testEvaluateEligibilityRules_4PS_NoChildren() {
        // Arrange - remove children from household
        testRequest.setMembers(new ArrayList<>());

        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "4PS_CONDITIONAL_CASH");

        // Assert
        assertNotNull(result);
        assertFalse(result.isPassed()); // Should fail because no children
        assertNotNull(result.getFailureReason());
        assertTrue(result.getFailureReason().contains("HAS_SCHOOL_AGE_CHILDREN"));
    }

    @Test
    void testEvaluateEligibilityRules_SeniorCitizen_Success() {
        // Arrange - add elderly member
        EligibilityRequest.HouseholdMemberInfo elderly = new EligibilityRequest.HouseholdMemberInfo();
        elderly.setAge(65);
        elderly.setRelationshipToHead("PARENT");
        testRequest.getMembers().add(elderly);

        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "SENIOR_CITIZEN_PENSION");

        // Assert
        assertNotNull(result);
        assertEquals("SENIOR_CITIZEN_RULES", result.getRuleSetName());
        assertTrue(result.isPassed()); // Should pass because household has elderly member
    }

    @Test
    void testEvaluateEligibilityRules_SeniorCitizen_NoElderly() {
        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "SENIOR_CITIZEN_PENSION");

        // Assert
        assertNotNull(result);
        assertFalse(result.isPassed()); // Should fail because no elderly members
        assertNotNull(result.getFailureReason());
        assertTrue(result.getFailureReason().contains("HAS_ELDERLY_MEMBERS"));
    }

    @Test
    void testEvaluateRuleSet_CustomContext() {
        // Arrange
        Map<String, Object> context = new HashMap<>();
        context.put("childrenCount", 2L);
        context.put("elderlyCount", 0L);
        context.put("receivingOtherBenefits", false);

        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateRuleSet("4PS_ELIGIBILITY_RULES", context);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPassed());
        assertEquals("4PS_ELIGIBILITY_RULES", result.getRuleSetName());
    }

    @Test
    void testEvaluateRuleSet_NonExistentRuleSet() {
        // Arrange
        Map<String, Object> context = new HashMap<>();

        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateRuleSet("NON_EXISTENT_RULES", context);

        // Assert
        assertNotNull(result);
        assertFalse(result.isPassed());
        assertEquals("Rule set not found: NON_EXISTENT_RULES", result.getFailureReason());
    }

    @Test
    void testGetAvailableRuleSets() {
        // Act
        List<String> ruleSets = rulesEngineService.getAvailableRuleSets();

        // Assert
        assertNotNull(ruleSets);
        assertFalse(ruleSets.isEmpty());
        assertTrue(ruleSets.contains("4PS_ELIGIBILITY_RULES"));
        assertTrue(ruleSets.contains("SENIOR_CITIZEN_RULES"));
        assertTrue(ruleSets.contains("DEFAULT_ELIGIBILITY_RULES"));
    }

    @Test
    void testGetRuleSetConfiguration() {
        // Act
        EligibilityRulesEngineService.RuleSetConfiguration config = 
            rulesEngineService.getRuleSetConfiguration("4PS_ELIGIBILITY_RULES");

        // Assert
        assertNotNull(config);
        assertEquals("4PS_ELIGIBILITY_RULES", config.getName());
        assertEquals("ALL_MUST_PASS", config.getEvaluationMode());
        assertTrue(config.isActive());
        assertNotNull(config.getRules());
        assertFalse(config.getRules().isEmpty());
    }

    @Test
    void testGetRuleSetConfiguration_NonExistent() {
        // Act
        EligibilityRulesEngineService.RuleSetConfiguration config = 
            rulesEngineService.getRuleSetConfiguration("NON_EXISTENT");

        // Assert
        assertNull(config);
    }

    @Test
    void testUpdateRuleSetConfiguration() {
        // Arrange
        EligibilityRulesEngineService.RuleSetConfiguration newConfig = 
            new EligibilityRulesEngineService.RuleSetConfiguration();
        newConfig.setName("TEST_RULES");
        newConfig.setDescription("Test rule set");
        newConfig.setEvaluationMode("ANY_MUST_PASS");
        newConfig.setActive(true);
        newConfig.setRules(new ArrayList<>());

        // Act
        rulesEngineService.updateRuleSetConfiguration("TEST_RULES", newConfig);
        EligibilityRulesEngineService.RuleSetConfiguration retrieved = 
            rulesEngineService.getRuleSetConfiguration("TEST_RULES");

        // Assert
        assertNotNull(retrieved);
        assertEquals("TEST_RULES", retrieved.getName());
        assertEquals("ANY_MUST_PASS", retrieved.getEvaluationMode());
    }

    @Test
    void testValidateRuleExpression_Valid() {
        // Act
        EligibilityRulesEngineService.RuleValidationResult result = 
            rulesEngineService.validateRuleExpression("#childrenCount > 0");

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    void testValidateRuleExpression_Invalid() {
        // Act
        EligibilityRulesEngineService.RuleValidationResult result = 
            rulesEngineService.validateRuleExpression("invalid expression $$");

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void testValidateRuleExpression_WithWarnings() {
        // Act
        EligibilityRulesEngineService.RuleValidationResult result = 
            rulesEngineService.validateRuleExpression("#value != null and #value > 0");

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNotNull(result.getWarnings());
        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().get(0).contains("null"));
    }

    @Test
    void testEvaluateEligibilityRules_WithAdditionalParameters() {
        // Arrange
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("receivingOtherBenefits", false);
        additionalParams.put("customCriteria", true);
        testRequest.setAdditionalParameters(additionalParams);

        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "4PS_CONDITIONAL_CASH");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEvaluationContext());
        assertTrue(result.getEvaluationContext().containsKey("receivingOtherBenefits"));
        assertTrue(result.getEvaluationContext().containsKey("customCriteria"));
    }

    @Test
    void testEvaluateEligibilityRules_DefaultProgram() {
        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "UNKNOWN_PROGRAM");

        // Assert
        assertNotNull(result);
        assertEquals("DEFAULT_ELIGIBILITY_RULES", result.getRuleSetName());
        assertTrue(result.isPassed()); // Default rules should pass (empty rule set)
    }

    @Test
    void testRuleEvaluationContext_DerivedValues() {
        // Act
        EligibilityRulesEngineService.RuleEvaluationResult result = 
            rulesEngineService.evaluateEligibilityRules(testRequest, "4PS_CONDITIONAL_CASH");

        // Assert
        assertNotNull(result.getEvaluationContext());
        assertTrue(result.getEvaluationContext().containsKey("childrenCount"));
        assertTrue(result.getEvaluationContext().containsKey("elderlyCount"));
        assertTrue(result.getEvaluationContext().containsKey("workingAgeCount"));
        assertTrue(result.getEvaluationContext().containsKey("dependencyRatio"));
        
        // Verify calculated values
        assertEquals(2L, result.getEvaluationContext().get("childrenCount"));
        assertEquals(0L, result.getEvaluationContext().get("elderlyCount"));
        assertEquals(1L, result.getEvaluationContext().get("workingAgeCount"));
    }
}
