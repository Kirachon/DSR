package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.service.ValidationRuleEngine;
import ph.gov.dsr.datamanagement.service.impl.DataValidationServiceImpl.ValidationRule;
import ph.gov.dsr.datamanagement.service.impl.DataValidationServiceImpl.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of ValidationRuleEngine for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockValidationRuleEngineImpl implements ValidationRuleEngine {

    @Override
    public List<ValidationRule> getRulesForDataType(String dataType, String validationProfile) {
        log.info("Mock getting rules for data type: {} with profile: {}", dataType, validationProfile);
        
        List<ValidationRule> rules = new ArrayList<>();
        
        // Return mock rules based on data type
        switch (dataType.toUpperCase()) {
            case "HOUSEHOLD":
                rules.add(createMockRule("household_number_required", "REQUIRED", "householdNumber"));
                rules.add(createMockRule("total_members_positive", "POSITIVE_NUMBER", "totalMembers"));
                break;
            case "INDIVIDUAL":
                rules.add(createMockRule("psn_required", "REQUIRED", "psn"));
                rules.add(createMockRule("first_name_required", "REQUIRED", "firstName"));
                rules.add(createMockRule("last_name_required", "REQUIRED", "lastName"));
                break;
            case "ECONOMIC_PROFILE":
                rules.add(createMockRule("household_id_required", "REQUIRED", "householdId"));
                break;
        }
        
        return rules;
    }

    @Override
    public List<String> getRuleNamesForDataType(String dataType) {
        log.info("Mock getting rule names for data type: {}", dataType);
        
        return switch (dataType.toUpperCase()) {
            case "HOUSEHOLD" -> List.of(
                "household_number_required",
                "total_members_positive",
                "monthly_income_non_negative"
            );
            case "INDIVIDUAL" -> List.of(
                "psn_required",
                "first_name_required",
                "last_name_required",
                "date_of_birth_valid",
                "sex_valid"
            );
            case "ECONOMIC_PROFILE" -> List.of(
                "household_id_required",
                "assets_non_negative",
                "expenses_non_negative"
            );
            default -> List.of("basic_validation");
        };
    }

    @Override
    public ValidationResult applyRule(ValidationRule rule, Map<String, Object> data) {
        log.debug("Mock applying rule: {} to field: {}", rule.getName(), rule.getField());
        
        ValidationResult result = new ValidationResult();
        result.setField(rule.getField());
        
        Object fieldValue = data.get(rule.getField());
        
        // Mock validation logic
        switch (rule.getExpression()) {
            case "REQUIRED":
                result.setValid(fieldValue != null && !fieldValue.toString().trim().isEmpty());
                break;
            case "POSITIVE_NUMBER":
                try {
                    double num = Double.parseDouble(fieldValue.toString());
                    result.setValid(num > 0);
                } catch (Exception e) {
                    result.setValid(false);
                }
                break;
            default:
                result.setValid(true); // Mock: assume valid for unknown rules
        }
        
        if (!result.isValid()) {
            result.setErrorCode(rule.getName());
            result.setErrorMessage(rule.getErrorMessage());
            result.setSeverity("ERROR");
            result.setRejectedValue(fieldValue != null ? fieldValue.toString() : null);
        }
        
        return result;
    }

    @Override
    public void addRule(String dataType, String ruleName, String ruleExpression) {
        log.info("Mock adding rule '{}' for data type: {}", ruleName, dataType);
        // Mock implementation - no actual storage
    }

    @Override
    public void removeRule(String dataType, String ruleName) {
        log.info("Mock removing rule '{}' for data type: {}", ruleName, dataType);
        // Mock implementation - no actual storage
    }

    @Override
    public void updateRule(String dataType, String ruleName, String ruleExpression) {
        log.info("Mock updating rule '{}' for data type: {}", ruleName, dataType);
        // Mock implementation - no actual storage
    }

    @Override
    public void setRuleEnabled(String dataType, String ruleName, boolean enabled) {
        log.info("Mock setting rule '{}' enabled={} for data type: {}", ruleName, enabled, dataType);
        // Mock implementation - no actual storage
    }

    @Override
    public ValidationResult testRule(String ruleExpression, Map<String, Object> testData) {
        log.debug("Mock testing rule expression: {}", ruleExpression);
        
        ValidationResult result = new ValidationResult();
        result.setValid(true); // Mock: assume test passes
        result.setField("testField");
        
        return result;
    }

    @Override
    public List<String> getValidationProfiles() {
        log.info("Mock getting validation profiles");
        return List.of("DEFAULT", "STRICT", "LENIENT");
    }

    @Override
    public void createValidationProfile(String profileName, String description) {
        log.info("Mock creating validation profile: {}", profileName);
        // Mock implementation - no actual storage
    }

    @Override
    public void assignRuleToProfile(String dataType, String ruleName, String profileName) {
        log.info("Mock assigning rule '{}' to profile: {}", ruleName, profileName);
        // Mock implementation - no actual storage
    }

    @Override
    public void removeRuleFromProfile(String dataType, String ruleName, String profileName) {
        log.info("Mock removing rule '{}' from profile: {}", ruleName, profileName);
        // Mock implementation - no actual storage
    }
    
    private ValidationRule createMockRule(String name, String expression, String field) {
        ValidationRule rule = new ValidationRule();
        rule.setName(name);
        rule.setExpression(expression);
        rule.setField(field);
        rule.setErrorMessage("Mock validation error for " + field);
        rule.setSeverity("ERROR");
        rule.setEnabled(true);
        return rule;
    }
}
