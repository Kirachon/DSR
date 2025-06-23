package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.service.impl.DataValidationServiceImpl.ValidationRule;
import ph.gov.dsr.datamanagement.service.impl.DataValidationServiceImpl.ValidationResult;

import java.util.List;
import java.util.Map;

/**
 * Service interface for validation rule engine
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface ValidationRuleEngine {

    /**
     * Get validation rules for data type and profile
     */
    List<ValidationRule> getRulesForDataType(String dataType, String validationProfile);

    /**
     * Get rule names for data type
     */
    List<String> getRuleNamesForDataType(String dataType);

    /**
     * Apply validation rule to data
     */
    ValidationResult applyRule(ValidationRule rule, Map<String, Object> data);

    /**
     * Add new validation rule
     */
    void addRule(String dataType, String ruleName, String ruleExpression);

    /**
     * Remove validation rule
     */
    void removeRule(String dataType, String ruleName);

    /**
     * Update validation rule
     */
    void updateRule(String dataType, String ruleName, String ruleExpression);

    /**
     * Enable/disable validation rule
     */
    void setRuleEnabled(String dataType, String ruleName, boolean enabled);

    /**
     * Test validation rule expression
     */
    ValidationResult testRule(String ruleExpression, Map<String, Object> testData);

    /**
     * Get all available validation profiles
     */
    List<String> getValidationProfiles();

    /**
     * Create new validation profile
     */
    void createValidationProfile(String profileName, String description);

    /**
     * Assign rule to validation profile
     */
    void assignRuleToProfile(String dataType, String ruleName, String profileName);

    /**
     * Remove rule from validation profile
     */
    void removeRuleFromProfile(String dataType, String ruleName, String profileName);
}
