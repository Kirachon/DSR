package ph.gov.dsr.datamanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import ph.gov.dsr.datamanagement.service.ValidationRuleEngine;
import ph.gov.dsr.datamanagement.service.impl.DataValidationServiceImpl.ValidationRule;
import ph.gov.dsr.datamanagement.service.impl.DataValidationServiceImpl.ValidationResult;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Production implementation of ValidationRuleEngine
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("!no-db")
@RequiredArgsConstructor
@Slf4j
public class ValidationRuleEngineImpl implements ValidationRuleEngine {

    // In-memory rule storage (in production, this would be database-backed)
    private final Map<String, List<ValidationRule>> rulesByDataType = new HashMap<>();
    private final Map<String, Set<String>> profileRules = new HashMap<>();
    
    // Common validation patterns
    private static final Pattern PSN_PATTERN = Pattern.compile("\\d{4}-\\d{4}-\\d{4}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+63|0)\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    @PostConstruct
    public void initializeRules() {
        initializeDefaultRules();
    }

    @Override
    public List<ValidationRule> getRulesForDataType(String dataType, String validationProfile) {
        log.debug("Getting rules for data type: {} with profile: {}", dataType, validationProfile);
        
        List<ValidationRule> allRules = rulesByDataType.getOrDefault(dataType, new ArrayList<>());
        
        if ("DEFAULT".equals(validationProfile)) {
            return allRules.stream()
                    .filter(ValidationRule::isEnabled)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filter rules by profile
        Set<String> profileRuleNames = profileRules.getOrDefault(validationProfile, new HashSet<>());
        return allRules.stream()
                .filter(rule -> rule.isEnabled() && profileRuleNames.contains(rule.getName()))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<String> getRuleNamesForDataType(String dataType) {
        log.debug("Getting rule names for data type: {}", dataType);
        
        return rulesByDataType.getOrDefault(dataType, new ArrayList<>())
                .stream()
                .map(ValidationRule::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public ValidationResult applyRule(ValidationRule rule, Map<String, Object> data) {
        log.debug("Applying rule: {} to field: {}", rule.getName(), rule.getField());
        
        ValidationResult result = new ValidationResult();
        result.setField(rule.getField());
        
        try {
            Object fieldValue = data.get(rule.getField());
            
            // Apply rule based on expression type
            switch (rule.getExpression()) {
                case "REQUIRED":
                    result = validateRequired(fieldValue, rule);
                    break;
                case "PSN_FORMAT":
                    result = validatePSNFormat(fieldValue, rule);
                    break;
                case "PHONE_FORMAT":
                    result = validatePhoneFormat(fieldValue, rule);
                    break;
                case "EMAIL_FORMAT":
                    result = validateEmailFormat(fieldValue, rule);
                    break;
                case "DATE_VALID":
                    result = validateDateFormat(fieldValue, rule);
                    break;
                case "POSITIVE_NUMBER":
                    result = validatePositiveNumber(fieldValue, rule);
                    break;
                case "NON_NEGATIVE":
                    result = validateNonNegative(fieldValue, rule);
                    break;
                case "SEX_VALID":
                    result = validateSex(fieldValue, rule);
                    break;
                case "CIVIL_STATUS_VALID":
                    result = validateCivilStatus(fieldValue, rule);
                    break;
                default:
                    // Custom expression evaluation
                    result = evaluateCustomExpression(rule.getExpression(), fieldValue, data, rule);
            }
            
            result.setField(rule.getField());
            if (!result.isValid()) {
                result.setErrorCode(rule.getName());
                result.setErrorMessage(rule.getErrorMessage());
                result.setSeverity(rule.getSeverity());
                result.setRejectedValue(fieldValue != null ? fieldValue.toString() : null);
            }
            
        } catch (Exception e) {
            log.error("Error applying validation rule: {}", rule.getName(), e);
            result.setValid(false);
            result.setErrorCode("RULE_ERROR");
            result.setErrorMessage("Error applying validation rule: " + e.getMessage());
            result.setSeverity("ERROR");
        }
        
        return result;
    }

    @Override
    public void addRule(String dataType, String ruleName, String ruleExpression) {
        log.info("Adding rule '{}' for data type: {}", ruleName, dataType);
        
        ValidationRule rule = new ValidationRule();
        rule.setName(ruleName);
        rule.setDataType(dataType);
        rule.setExpression(ruleExpression);
        rule.setEnabled(true);
        rule.setSeverity("ERROR");
        
        rulesByDataType.computeIfAbsent(dataType, k -> new ArrayList<>()).add(rule);
    }

    @Override
    public void removeRule(String dataType, String ruleName) {
        log.info("Removing rule '{}' for data type: {}", ruleName, dataType);
        
        List<ValidationRule> rules = rulesByDataType.get(dataType);
        if (rules != null) {
            rules.removeIf(rule -> rule.getName().equals(ruleName));
        }
    }

    @Override
    public void updateRule(String dataType, String ruleName, String ruleExpression) {
        log.info("Updating rule '{}' for data type: {}", ruleName, dataType);
        
        List<ValidationRule> rules = rulesByDataType.get(dataType);
        if (rules != null) {
            rules.stream()
                    .filter(rule -> rule.getName().equals(ruleName))
                    .findFirst()
                    .ifPresent(rule -> rule.setExpression(ruleExpression));
        }
    }

    @Override
    public void setRuleEnabled(String dataType, String ruleName, boolean enabled) {
        log.info("Setting rule '{}' enabled={} for data type: {}", ruleName, enabled, dataType);
        
        List<ValidationRule> rules = rulesByDataType.get(dataType);
        if (rules != null) {
            rules.stream()
                    .filter(rule -> rule.getName().equals(ruleName))
                    .findFirst()
                    .ifPresent(rule -> rule.setEnabled(enabled));
        }
    }

    @Override
    public ValidationResult testRule(String ruleExpression, Map<String, Object> testData) {
        log.debug("Testing rule expression: {}", ruleExpression);
        
        ValidationRule testRule = new ValidationRule();
        testRule.setName("TEST_RULE");
        testRule.setExpression(ruleExpression);
        testRule.setField("testField");
        testRule.setErrorMessage("Test rule failed");
        testRule.setSeverity("ERROR");
        
        return applyRule(testRule, testData);
    }

    @Override
    public List<String> getValidationProfiles() {
        return new ArrayList<>(profileRules.keySet());
    }

    @Override
    public void createValidationProfile(String profileName, String description) {
        log.info("Creating validation profile: {}", profileName);
        profileRules.put(profileName, new HashSet<>());
    }

    @Override
    public void assignRuleToProfile(String dataType, String ruleName, String profileName) {
        log.info("Assigning rule '{}' to profile: {}", ruleName, profileName);
        profileRules.computeIfAbsent(profileName, k -> new HashSet<>()).add(ruleName);
    }

    @Override
    public void removeRuleFromProfile(String dataType, String ruleName, String profileName) {
        log.info("Removing rule '{}' from profile: {}", ruleName, profileName);
        Set<String> rules = profileRules.get(profileName);
        if (rules != null) {
            rules.remove(ruleName);
        }
    }
    
    /**
     * Initialize default validation rules
     */
    private void initializeDefaultRules() {
        // Household validation rules
        addDefaultRule("HOUSEHOLD", "household_number_required", "REQUIRED", "householdNumber", 
                "Household number is required");
        addDefaultRule("HOUSEHOLD", "head_psn_format", "PSN_FORMAT", "headOfHouseholdPSN", 
                "Head of household PSN must be in format XXXX-XXXX-XXXX");
        addDefaultRule("HOUSEHOLD", "total_members_positive", "POSITIVE_NUMBER", "totalMembers", 
                "Total members must be a positive number");
        addDefaultRule("HOUSEHOLD", "monthly_income_non_negative", "NON_NEGATIVE", "monthlyIncome", 
                "Monthly income must be non-negative");
        
        // Individual validation rules
        addDefaultRule("INDIVIDUAL", "psn_required", "REQUIRED", "psn", 
                "PSN is required");
        addDefaultRule("INDIVIDUAL", "psn_format", "PSN_FORMAT", "psn", 
                "PSN must be in format XXXX-XXXX-XXXX");
        addDefaultRule("INDIVIDUAL", "first_name_required", "REQUIRED", "firstName", 
                "First name is required");
        addDefaultRule("INDIVIDUAL", "last_name_required", "REQUIRED", "lastName", 
                "Last name is required");
        addDefaultRule("INDIVIDUAL", "date_of_birth_valid", "DATE_VALID", "dateOfBirth", 
                "Date of birth must be a valid date");
        addDefaultRule("INDIVIDUAL", "sex_valid", "SEX_VALID", "sex", 
                "Sex must be M or F");
        addDefaultRule("INDIVIDUAL", "civil_status_valid", "CIVIL_STATUS_VALID", "civilStatus", 
                "Civil status must be valid");
        
        // Economic Profile validation rules
        addDefaultRule("ECONOMIC_PROFILE", "household_id_required", "REQUIRED", "householdId", 
                "Household ID is required");
        addDefaultRule("ECONOMIC_PROFILE", "assets_non_negative", "NON_NEGATIVE", "totalAssets", 
                "Total assets must be non-negative");
        addDefaultRule("ECONOMIC_PROFILE", "expenses_non_negative", "NON_NEGATIVE", "monthlyExpenses", 
                "Monthly expenses must be non-negative");
    }
    
    private void addDefaultRule(String dataType, String ruleName, String expression, String field, String errorMessage) {
        ValidationRule rule = new ValidationRule();
        rule.setName(ruleName);
        rule.setDataType(dataType);
        rule.setExpression(expression);
        rule.setField(field);
        rule.setErrorMessage(errorMessage);
        rule.setSeverity("ERROR");
        rule.setEnabled(true);
        
        rulesByDataType.computeIfAbsent(dataType, k -> new ArrayList<>()).add(rule);
    }
    
    // Validation methods
    private ValidationResult validateRequired(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        result.setValid(value != null && !value.toString().trim().isEmpty());
        return result;
    }
    
    private ValidationResult validatePSNFormat(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true); // Let REQUIRED rule handle null values
            return result;
        }
        
        String psn = value.toString().trim();
        result.setValid(PSN_PATTERN.matcher(psn).matches());
        return result;
    }
    
    private ValidationResult validatePhoneFormat(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        String phone = value.toString().trim();
        result.setValid(PHONE_PATTERN.matcher(phone).matches());
        return result;
    }
    
    private ValidationResult validateEmailFormat(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        String email = value.toString().trim();
        result.setValid(EMAIL_PATTERN.matcher(email).matches());
        return result;
    }
    
    private ValidationResult validateDateFormat(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        try {
            LocalDate.parse(value.toString());
            result.setValid(true);
        } catch (DateTimeParseException e) {
            result.setValid(false);
        }
        
        return result;
    }
    
    private ValidationResult validatePositiveNumber(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        try {
            double num = Double.parseDouble(value.toString());
            result.setValid(num > 0);
        } catch (NumberFormatException e) {
            result.setValid(false);
        }
        
        return result;
    }
    
    private ValidationResult validateNonNegative(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        try {
            double num = Double.parseDouble(value.toString());
            result.setValid(num >= 0);
        } catch (NumberFormatException e) {
            result.setValid(false);
        }
        
        return result;
    }
    
    private ValidationResult validateSex(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        String sex = value.toString().trim().toUpperCase();
        result.setValid("M".equals(sex) || "F".equals(sex));
        return result;
    }
    
    private ValidationResult validateCivilStatus(Object value, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.setValid(true);
            return result;
        }
        
        String status = value.toString().trim().toUpperCase();
        Set<String> validStatuses = Set.of("SINGLE", "MARRIED", "WIDOWED", "DIVORCED", "SEPARATED");
        result.setValid(validStatuses.contains(status));
        return result;
    }
    
    private ValidationResult evaluateCustomExpression(String expression, Object fieldValue, 
                                                    Map<String, Object> data, ValidationRule rule) {
        ValidationResult result = new ValidationResult();
        
        // TODO: Implement custom expression evaluation
        // This could use a scripting engine like JavaScript or a custom DSL
        // For now, return valid
        result.setValid(true);
        
        return result;
    }
}
