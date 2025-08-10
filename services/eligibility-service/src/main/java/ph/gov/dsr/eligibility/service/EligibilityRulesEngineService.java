package ph.gov.dsr.eligibility.service;

import ph.gov.dsr.eligibility.dto.EligibilityRequest;

import java.util.List;
import java.util.Map;

/**
 * Service interface for eligibility rules engine
 * Handles categorical eligibility criteria evaluation using configurable rules
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface EligibilityRulesEngineService {

    /**
     * Evaluate eligibility rules for a specific program
     * 
     * @param request Eligibility assessment request
     * @param programCode Program to evaluate eligibility for
     * @return Rule evaluation result
     */
    RuleEvaluationResult evaluateEligibilityRules(EligibilityRequest request, String programCode);

    /**
     * Evaluate specific rule set
     * 
     * @param ruleSetName Name of the rule set to evaluate
     * @param context Evaluation context data
     * @return Rule evaluation result
     */
    RuleEvaluationResult evaluateRuleSet(String ruleSetName, Map<String, Object> context);

    /**
     * Get all available rule sets
     * 
     * @return List of rule set names
     */
    List<String> getAvailableRuleSets();

    /**
     * Get rule set configuration
     * 
     * @param ruleSetName Name of the rule set
     * @return Rule set configuration
     */
    RuleSetConfiguration getRuleSetConfiguration(String ruleSetName);

    /**
     * Update rule set configuration (admin function)
     * 
     * @param ruleSetName Name of the rule set
     * @param configuration New rule set configuration
     */
    void updateRuleSetConfiguration(String ruleSetName, RuleSetConfiguration configuration);

    /**
     * Validate rule expression syntax
     * 
     * @param expression Rule expression to validate
     * @return Validation result
     */
    RuleValidationResult validateRuleExpression(String expression);

    /**
     * Rule evaluation result
     */
    class RuleEvaluationResult {
        private boolean passed;
        private String ruleSetName;
        private List<RuleResult> ruleResults;
        private Map<String, Object> evaluationContext;
        private String failureReason;
        private java.time.LocalDateTime evaluatedAt;
        private long evaluationTimeMs;

        // Getters and setters
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        
        public String getRuleSetName() { return ruleSetName; }
        public void setRuleSetName(String ruleSetName) { this.ruleSetName = ruleSetName; }
        
        public List<RuleResult> getRuleResults() { return ruleResults; }
        public void setRuleResults(List<RuleResult> ruleResults) { this.ruleResults = ruleResults; }
        
        public Map<String, Object> getEvaluationContext() { return evaluationContext; }
        public void setEvaluationContext(Map<String, Object> evaluationContext) { this.evaluationContext = evaluationContext; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        public java.time.LocalDateTime getEvaluatedAt() { return evaluatedAt; }
        public void setEvaluatedAt(java.time.LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
        
        public long getEvaluationTimeMs() { return evaluationTimeMs; }
        public void setEvaluationTimeMs(long evaluationTimeMs) { this.evaluationTimeMs = evaluationTimeMs; }
    }

    /**
     * Individual rule result
     */
    class RuleResult {
        private String ruleName;
        private boolean passed;
        private String expression;
        private Object evaluatedValue;
        private String failureMessage;
        private Map<String, Object> ruleContext;

        // Getters and setters
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        
        public Object getEvaluatedValue() { return evaluatedValue; }
        public void setEvaluatedValue(Object evaluatedValue) { this.evaluatedValue = evaluatedValue; }
        
        public String getFailureMessage() { return failureMessage; }
        public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
        
        public Map<String, Object> getRuleContext() { return ruleContext; }
        public void setRuleContext(Map<String, Object> ruleContext) { this.ruleContext = ruleContext; }
    }

    /**
     * Rule set configuration
     */
    class RuleSetConfiguration {
        private String name;
        private String description;
        private String evaluationMode; // ALL_MUST_PASS, ANY_MUST_PASS, WEIGHTED
        private List<Rule> rules;
        private Map<String, Object> globalContext;
        private boolean active;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getEvaluationMode() { return evaluationMode; }
        public void setEvaluationMode(String evaluationMode) { this.evaluationMode = evaluationMode; }
        
        public List<Rule> getRules() { return rules; }
        public void setRules(List<Rule> rules) { this.rules = rules; }
        
        public Map<String, Object> getGlobalContext() { return globalContext; }
        public void setGlobalContext(Map<String, Object> globalContext) { this.globalContext = globalContext; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * Individual rule definition
     */
    class Rule {
        private String name;
        private String description;
        private String expression;
        private String failureMessage;
        private boolean mandatory;
        private double weight;
        private Map<String, Object> ruleContext;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        
        public String getFailureMessage() { return failureMessage; }
        public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
        
        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
        
        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
        
        public Map<String, Object> getRuleContext() { return ruleContext; }
        public void setRuleContext(Map<String, Object> ruleContext) { this.ruleContext = ruleContext; }
    }

    /**
     * Rule validation result
     */
    class RuleValidationResult {
        private boolean valid;
        private String errorMessage;
        private List<String> warnings;

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
}
