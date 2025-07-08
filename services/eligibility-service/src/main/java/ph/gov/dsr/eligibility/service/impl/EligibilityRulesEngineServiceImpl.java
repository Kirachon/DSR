package ph.gov.dsr.eligibility.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.service.EligibilityRulesEngineService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of EligibilityRulesEngineService using Spring Expression Language (SpEL)
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityRulesEngineServiceImpl implements EligibilityRulesEngineService {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    
    // In-memory rule sets (in production, these would be stored in database)
    private final Map<String, RuleSetConfiguration> ruleSets = initializeRuleSets();

    @Override
    public RuleEvaluationResult evaluateEligibilityRules(EligibilityRequest request, String programCode) {
        log.info("Evaluating eligibility rules for program: {}", programCode);
        
        // Map program code to rule set name
        String ruleSetName = mapProgramToRuleSet(programCode);
        
        // Build evaluation context from request
        Map<String, Object> context = buildEvaluationContext(request);
        
        return evaluateRuleSet(ruleSetName, context);
    }

    @Override
    public RuleEvaluationResult evaluateRuleSet(String ruleSetName, Map<String, Object> context) {
        log.debug("Evaluating rule set: {}", ruleSetName);
        
        long startTime = System.currentTimeMillis();
        
        RuleEvaluationResult result = new RuleEvaluationResult();
        result.setRuleSetName(ruleSetName);
        result.setEvaluatedAt(LocalDateTime.now());
        result.setEvaluationContext(new HashMap<>(context));
        
        RuleSetConfiguration ruleSet = ruleSets.get(ruleSetName);
        if (ruleSet == null) {
            result.setPassed(false);
            result.setFailureReason("Rule set not found: " + ruleSetName);
            result.setRuleResults(new ArrayList<>());
            return result;
        }
        
        if (!ruleSet.isActive()) {
            result.setPassed(false);
            result.setFailureReason("Rule set is inactive: " + ruleSetName);
            result.setRuleResults(new ArrayList<>());
            return result;
        }
        
        List<RuleResult> ruleResults = new ArrayList<>();
        StandardEvaluationContext evalContext = createEvaluationContext(context, ruleSet.getGlobalContext());
        
        try {
            // Evaluate each rule
            for (Rule rule : ruleSet.getRules()) {
                RuleResult ruleResult = evaluateRule(rule, evalContext);
                ruleResults.add(ruleResult);
            }
            
            result.setRuleResults(ruleResults);
            
            // Determine overall result based on evaluation mode
            boolean overallPassed = determineOverallResult(ruleResults, ruleSet.getEvaluationMode());
            result.setPassed(overallPassed);
            
            if (!overallPassed) {
                result.setFailureReason(generateFailureReason(ruleResults, ruleSet.getEvaluationMode()));
            }
            
        } catch (Exception e) {
            log.error("Error evaluating rule set: {}", ruleSetName, e);
            result.setPassed(false);
            result.setFailureReason("Rule evaluation error: " + e.getMessage());
        }
        
        long evaluationTime = System.currentTimeMillis() - startTime;
        result.setEvaluationTimeMs(evaluationTime);
        
        log.debug("Rule set evaluation completed: {} in {}ms. Passed: {}", 
                ruleSetName, evaluationTime, result.isPassed());
        
        return result;
    }

    @Override
    public List<String> getAvailableRuleSets() {
        return new ArrayList<>(ruleSets.keySet());
    }

    @Override
    public RuleSetConfiguration getRuleSetConfiguration(String ruleSetName) {
        return ruleSets.get(ruleSetName);
    }

    @Override
    public void updateRuleSetConfiguration(String ruleSetName, RuleSetConfiguration configuration) {
        log.info("Updating rule set configuration: {}", ruleSetName);
        ruleSets.put(ruleSetName, configuration);
    }

    @Override
    public RuleValidationResult validateRuleExpression(String expression) {
        RuleValidationResult result = new RuleValidationResult();
        result.setWarnings(new ArrayList<>());
        
        try {
            Expression expr = expressionParser.parseExpression(expression);
            result.setValid(true);
            
            // Add warnings for potentially problematic expressions
            if (expression.contains("null")) {
                result.getWarnings().add("Expression contains null checks - ensure proper null handling");
            }
            if (expression.length() > 500) {
                result.getWarnings().add("Expression is very long - consider breaking into smaller rules");
            }
            
        } catch (Exception e) {
            result.setValid(false);
            result.setErrorMessage("Invalid expression: " + e.getMessage());
        }
        
        return result;
    }

    private RuleResult evaluateRule(Rule rule, StandardEvaluationContext context) {
        RuleResult result = new RuleResult();
        result.setRuleName(rule.getName());
        result.setExpression(rule.getExpression());
        result.setRuleContext(rule.getRuleContext());
        
        try {
            Expression expression = expressionParser.parseExpression(rule.getExpression());
            Object value = expression.getValue(context);
            result.setEvaluatedValue(value);
            
            // Convert result to boolean
            boolean passed = convertToBoolean(value);
            result.setPassed(passed);
            
            if (!passed && rule.getFailureMessage() != null) {
                result.setFailureMessage(rule.getFailureMessage());
            }
            
        } catch (Exception e) {
            log.error("Error evaluating rule: {}", rule.getName(), e);
            result.setPassed(false);
            result.setFailureMessage("Rule evaluation error: " + e.getMessage());
        }
        
        return result;
    }

    private boolean determineOverallResult(List<RuleResult> ruleResults, String evaluationMode) {
        switch (evaluationMode.toUpperCase()) {
            case "ALL_MUST_PASS":
                return ruleResults.stream().allMatch(RuleResult::isPassed);
            
            case "ANY_MUST_PASS":
                return ruleResults.stream().anyMatch(RuleResult::isPassed);
            
            case "WEIGHTED":
                // For weighted mode, calculate weighted score
                double totalWeight = ruleResults.stream().mapToDouble(r -> getWeight(r.getRuleName())).sum();
                double passedWeight = ruleResults.stream()
                        .filter(RuleResult::isPassed)
                        .mapToDouble(r -> getWeight(r.getRuleName()))
                        .sum();
                return totalWeight > 0 && (passedWeight / totalWeight) >= 0.7; // 70% threshold
            
            default:
                return ruleResults.stream().allMatch(RuleResult::isPassed);
        }
    }

    private String generateFailureReason(List<RuleResult> ruleResults, String evaluationMode) {
        List<String> failedRules = ruleResults.stream()
                .filter(r -> !r.isPassed())
                .map(r -> r.getRuleName() + (r.getFailureMessage() != null ? ": " + r.getFailureMessage() : ""))
                .collect(Collectors.toList());
        
        if (failedRules.isEmpty()) {
            return "Unknown failure reason";
        }
        
        switch (evaluationMode.toUpperCase()) {
            case "ALL_MUST_PASS":
                return "Failed rules: " + String.join(", ", failedRules);
            case "ANY_MUST_PASS":
                return "No rules passed. Failed rules: " + String.join(", ", failedRules);
            case "WEIGHTED":
                return "Insufficient weighted score. Failed rules: " + String.join(", ", failedRules);
            default:
                return "Failed rules: " + String.join(", ", failedRules);
        }
    }

    private StandardEvaluationContext createEvaluationContext(Map<String, Object> context, Map<String, Object> globalContext) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        
        // Add context variables
        if (context != null) {
            context.forEach(evalContext::setVariable);
        }
        
        // Add global context variables
        if (globalContext != null) {
            globalContext.forEach(evalContext::setVariable);
        }
        
        // Add utility functions
        evalContext.setVariable("Math", Math.class);
        evalContext.setVariable("String", String.class);
        
        return evalContext;
    }

    private Map<String, Object> buildEvaluationContext(EligibilityRequest request) {
        Map<String, Object> context = new HashMap<>();
        
        // Add household information
        if (request.getHouseholdInfo() != null) {
            context.put("household", request.getHouseholdInfo());
            context.put("monthlyIncome", request.getHouseholdInfo().getMonthlyIncome());
            context.put("totalMembers", request.getHouseholdInfo().getTotalMembers());
            context.put("isIndigenous", request.getHouseholdInfo().getIsIndigenous());
            context.put("hasPwdMembers", request.getHouseholdInfo().getHasPwdMembers());
        }
        
        // Add member information
        if (request.getMembers() != null) {
            context.put("members", request.getMembers());
            context.put("memberCount", request.getMembers().size());
            
            // Calculate derived values
            long children = request.getMembers().stream().filter(m -> m.getAge() != null && m.getAge() < 18).count();
            long elderly = request.getMembers().stream().filter(m -> m.getAge() != null && m.getAge() >= 60).count();
            long workingAge = request.getMembers().size() - children - elderly;
            
            context.put("childrenCount", children);
            context.put("elderlyCount", elderly);
            context.put("workingAgeCount", workingAge);
            context.put("dependencyRatio", workingAge > 0 ? (double)(children + elderly) / workingAge : 0.0);
        }
        
        // Add additional parameters
        if (request.getAdditionalParameters() != null) {
            context.putAll(request.getAdditionalParameters());
        }
        
        // Add request metadata
        context.put("psn", request.getPsn());
        context.put("programCode", request.getProgramCode());
        
        return context;
    }

    private boolean convertToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() > 0;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else {
            return value != null;
        }
    }

    private double getWeight(String ruleName) {
        // In production, this would be stored with the rule configuration
        return 1.0; // Default weight
    }

    private String mapProgramToRuleSet(String programCode) {
        // Map program codes to rule set names
        switch (programCode) {
            case "4PS_CONDITIONAL_CASH":
                return "4PS_ELIGIBILITY_RULES";
            case "SENIOR_CITIZEN_PENSION":
                return "SENIOR_CITIZEN_RULES";
            case "PWD_ASSISTANCE":
                return "PWD_ASSISTANCE_RULES";
            default:
                return "DEFAULT_ELIGIBILITY_RULES";
        }
    }

    private static Map<String, RuleSetConfiguration> initializeRuleSets() {
        Map<String, RuleSetConfiguration> ruleSets = new HashMap<>();
        
        // 4Ps Eligibility Rules
        RuleSetConfiguration fourPsRules = new RuleSetConfiguration();
        fourPsRules.setName("4PS_ELIGIBILITY_RULES");
        fourPsRules.setDescription("Eligibility rules for Pantawid Pamilyang Pilipino Program");
        fourPsRules.setEvaluationMode("ALL_MUST_PASS");
        fourPsRules.setActive(true);
        
        List<Rule> fourPsRuleList = new ArrayList<>();
        
        Rule hasChildren = new Rule();
        hasChildren.setName("HAS_SCHOOL_AGE_CHILDREN");
        hasChildren.setDescription("Must have children aged 0-18");
        hasChildren.setExpression("#childrenCount > 0");
        hasChildren.setFailureMessage("No school-age children found");
        hasChildren.setMandatory(true);
        fourPsRuleList.add(hasChildren);
        
        Rule notReceivingBenefits = new Rule();
        notReceivingBenefits.setName("NOT_RECEIVING_OTHER_BENEFITS");
        notReceivingBenefits.setDescription("Must not be receiving other cash transfer benefits");
        notReceivingBenefits.setExpression("#receivingOtherBenefits == null or #receivingOtherBenefits == false");
        notReceivingBenefits.setFailureMessage("Already receiving other cash transfer benefits");
        notReceivingBenefits.setMandatory(true);
        fourPsRuleList.add(notReceivingBenefits);
        
        fourPsRules.setRules(fourPsRuleList);
        ruleSets.put("4PS_ELIGIBILITY_RULES", fourPsRules);
        
        // Senior Citizen Rules
        RuleSetConfiguration seniorRules = new RuleSetConfiguration();
        seniorRules.setName("SENIOR_CITIZEN_RULES");
        seniorRules.setDescription("Eligibility rules for Senior Citizen Pension");
        seniorRules.setEvaluationMode("ALL_MUST_PASS");
        seniorRules.setActive(true);
        
        List<Rule> seniorRuleList = new ArrayList<>();
        
        Rule hasElderly = new Rule();
        hasElderly.setName("HAS_ELDERLY_MEMBERS");
        hasElderly.setDescription("Must have members aged 60 and above");
        hasElderly.setExpression("#elderlyCount > 0");
        hasElderly.setFailureMessage("No elderly members found");
        hasElderly.setMandatory(true);
        seniorRuleList.add(hasElderly);
        
        seniorRules.setRules(seniorRuleList);
        ruleSets.put("SENIOR_CITIZEN_RULES", seniorRules);
        
        // Default rules
        RuleSetConfiguration defaultRules = new RuleSetConfiguration();
        defaultRules.setName("DEFAULT_ELIGIBILITY_RULES");
        defaultRules.setDescription("Default eligibility rules for general programs");
        defaultRules.setEvaluationMode("ALL_MUST_PASS");
        defaultRules.setActive(true);
        defaultRules.setRules(new ArrayList<>());
        ruleSets.put("DEFAULT_ELIGIBILITY_RULES", defaultRules);
        
        return ruleSets;
    }

    /**
     * Enhanced rule evaluation with program-specific logic
     */
    public RuleEvaluationResult evaluateAdvancedRules(EligibilityRequest request, String programCode) {
        log.info("Evaluating advanced eligibility rules for program: {}", programCode);

        // Get base rule evaluation
        RuleEvaluationResult baseResult = evaluateEligibilityRules(request, programCode);

        // Apply program-specific enhancements
        enhanceRuleEvaluationForProgram(baseResult, request, programCode);

        return baseResult;
    }

    private void enhanceRuleEvaluationForProgram(RuleEvaluationResult result, EligibilityRequest request, String programCode) {
        switch (programCode) {
            case "4PS_CONDITIONAL_CASH":
                enhance4PSRules(result, request);
                break;
            case "SOCIAL_PENSION":
                enhanceSocialPensionRules(result, request);
                break;
            case "DSWD_ASSISTANCE":
                enhanceDSWDAssistanceRules(result, request);
                break;
            default:
                log.debug("No specific enhancements for program: {}", programCode);
        }
    }

    private void enhance4PSRules(RuleEvaluationResult result, EligibilityRequest request) {
        // Add 4Ps-specific rule enhancements
        List<RuleResult> additionalRules = new ArrayList<>();

        // Check for school-age children
        if (request.getMembers() != null) {
            boolean hasSchoolAgeChildren = request.getMembers().stream()
                .anyMatch(member -> {
                    int age = calculateAge(member.getDateOfBirth());
                    return age >= 3 && age <= 18;
                });

            RuleResult schoolAgeRule = new RuleResult();
            schoolAgeRule.setRuleName("HAS_SCHOOL_AGE_CHILDREN");
            schoolAgeRule.setExpression("hasSchoolAgeChildren == true");
            schoolAgeRule.setPassed(hasSchoolAgeChildren);
            schoolAgeRule.setEvaluatedValue(hasSchoolAgeChildren);
            if (!hasSchoolAgeChildren) {
                schoolAgeRule.setFailureMessage("Household must have children aged 3-18 for 4Ps eligibility");
            }
            additionalRules.add(schoolAgeRule);
        }

        // Check for pregnant/lactating women
        if (request.getMembers() != null) {
            boolean hasPregnantLactating = request.getMembers().stream()
                .anyMatch(member -> Boolean.TRUE.equals(member.getIsPregnant()) ||
                                  Boolean.TRUE.equals(member.getIsLactating()));

            RuleResult pregnantLactatingRule = new RuleResult();
            pregnantLactatingRule.setRuleName("HAS_PREGNANT_LACTATING_WOMEN");
            pregnantLactatingRule.setExpression("hasPregnantLactating == true");
            pregnantLactatingRule.setPassed(hasPregnantLactating);
            pregnantLactatingRule.setEvaluatedValue(hasPregnantLactating);
            additionalRules.add(pregnantLactatingRule);
        }

        // Add additional rules to result
        result.getRuleResults().addAll(additionalRules);

        // Recalculate overall result
        boolean overallPassed = result.getRuleResults().stream().allMatch(RuleResult::isPassed);
        result.setPassed(overallPassed);
    }

    private void enhanceSocialPensionRules(RuleEvaluationResult result, EligibilityRequest request) {
        // Add Social Pension-specific rule enhancements
        List<RuleResult> additionalRules = new ArrayList<>();

        // Check for senior citizens
        if (request.getMembers() != null) {
            boolean hasSeniorCitizens = request.getMembers().stream()
                .anyMatch(member -> {
                    int age = calculateAge(member.getDateOfBirth());
                    return age >= 60;
                });

            RuleResult seniorCitizenRule = new RuleResult();
            seniorCitizenRule.setRuleName("HAS_SENIOR_CITIZENS");
            seniorCitizenRule.setExpression("hasSeniorCitizens == true");
            seniorCitizenRule.setPassed(hasSeniorCitizens);
            seniorCitizenRule.setEvaluatedValue(hasSeniorCitizens);
            if (!hasSeniorCitizens) {
                seniorCitizenRule.setFailureMessage("Household must have members aged 60 or above for Social Pension");
            }
            additionalRules.add(seniorCitizenRule);
        }

        result.getRuleResults().addAll(additionalRules);

        // Recalculate overall result
        boolean overallPassed = result.getRuleResults().stream().allMatch(RuleResult::isPassed);
        result.setPassed(overallPassed);
    }

    private void enhanceDSWDAssistanceRules(RuleEvaluationResult result, EligibilityRequest request) {
        // Add DSWD Assistance-specific rule enhancements
        List<RuleResult> additionalRules = new ArrayList<>();

        // Check for vulnerability indicators
        boolean hasVulnerabilities = false;
        if (request.getHouseholdInfo() != null) {
            hasVulnerabilities = Boolean.TRUE.equals(request.getHouseholdInfo().getHasPwdMembers()) ||
                               Boolean.TRUE.equals(request.getHouseholdInfo().getIsSoloParentHousehold()) ||
                               Boolean.TRUE.equals(request.getHouseholdInfo().getIsIndigenous());
        }

        RuleResult vulnerabilityRule = new RuleResult();
        vulnerabilityRule.setRuleName("HAS_VULNERABILITY_INDICATORS");
        vulnerabilityRule.setExpression("hasVulnerabilities == true");
        vulnerabilityRule.setPassed(hasVulnerabilities);
        vulnerabilityRule.setEvaluatedValue(hasVulnerabilities);
        if (!hasVulnerabilities) {
            vulnerabilityRule.setFailureMessage("Household should have vulnerability indicators for DSWD assistance");
        }
        additionalRules.add(vulnerabilityRule);

        result.getRuleResults().addAll(additionalRules);

        // For DSWD assistance, vulnerability can make conditional eligible
        if (hasVulnerabilities && !result.isPassed()) {
            result.setPassed(true);
            result.setFailureReason("Eligible due to vulnerability indicators");
        }
    }

    private int calculateAge(java.time.LocalDate dateOfBirth) {
        if (dateOfBirth == null) return 0;
        return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }
}
