package ph.gov.dsr.datamanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.HouseholdDataRequest;
import ph.gov.dsr.datamanagement.dto.ValidationRequest;
import ph.gov.dsr.datamanagement.dto.ValidationResponse;
import ph.gov.dsr.datamanagement.service.DataValidationService;
import ph.gov.dsr.datamanagement.service.ValidationRuleEngine;
import ph.gov.dsr.datamanagement.service.DataCleaningService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Production implementation of DataValidationService
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataValidationServiceImpl implements DataValidationService {

    private final ValidationRuleEngine validationRuleEngine;
    private final DataCleaningService dataCleaningService;

    @Override
    public ValidationResponse validateData(ValidationRequest request) {
        log.info("Validating data of type: {} with profile: {}", request.getDataType(), request.getValidationProfile());
        long startTime = System.currentTimeMillis();
        
        ValidationResponse response = new ValidationResponse();
        response.setValidatedAt(LocalDateTime.now());
        response.setValidationProfile(request.getValidationProfile());
        response.setErrors(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        
        try {
            // Get validation rules for the data type and profile
            List<ValidationRule> rules = validationRuleEngine.getRulesForDataType(
                request.getDataType(), request.getValidationProfile());
            
            boolean isValid = true;
            
            // Apply each validation rule
            for (ValidationRule rule : rules) {
                ValidationResult result = validationRuleEngine.applyRule(rule, request.getData());
                
                if (!result.isValid()) {
                    isValid = false;
                    
                    ValidationResponse.ValidationError error = new ValidationResponse.ValidationError();
                    error.setField(result.getField());
                    error.setCode(result.getErrorCode());
                    error.setMessage(result.getErrorMessage());
                    error.setRejectedValue(result.getRejectedValue());
                    error.setSeverity(result.getSeverity());
                    
                    response.getErrors().add(error);
                }
                
                // Add warnings if any
                if (request.isIncludeWarnings() && result.hasWarnings()) {
                    for (ValidationWarning warning : result.getWarnings()) {
                        ValidationResponse.ValidationWarning responseWarning = 
                            new ValidationResponse.ValidationWarning();
                        responseWarning.setField(warning.getField());
                        responseWarning.setCode(warning.getCode());
                        responseWarning.setMessage(warning.getMessage());
                        responseWarning.setSuggestion(warning.getSuggestion());
                        
                        response.getWarnings().add(responseWarning);
                    }
                }
            }
            
            // Validate references if requested
            if (request.isValidateReferences()) {
                ValidationResult referenceResult = validateReferences(request.getDataType(), request.getData());
                if (!referenceResult.isValid()) {
                    isValid = false;
                    
                    ValidationResponse.ValidationError error = new ValidationResponse.ValidationError();
                    error.setField(referenceResult.getField());
                    error.setCode(referenceResult.getErrorCode());
                    error.setMessage(referenceResult.getErrorMessage());
                    error.setRejectedValue(referenceResult.getRejectedValue());
                    error.setSeverity("ERROR");
                    
                    response.getErrors().add(error);
                }
            }
            
            response.setValid(isValid);
            response.setStatus(isValid ? "VALID" : "INVALID");
            
            // Add overall status based on errors and warnings
            if (!isValid) {
                response.setStatus("INVALID");
            } else if (!response.getWarnings().isEmpty()) {
                response.setStatus("WARNING");
            } else {
                response.setStatus("VALID");
            }
            
        } catch (Exception e) {
            log.error("Error during data validation", e);
            response.setValid(false);
            response.setStatus("ERROR");
            
            ValidationResponse.ValidationError error = new ValidationResponse.ValidationError();
            error.setField("SYSTEM");
            error.setCode("VALIDATION_ERROR");
            error.setMessage("Internal validation error: " + e.getMessage());
            error.setSeverity("ERROR");
            
            response.getErrors().add(error);
        }
        
        response.setValidationTimeMs(System.currentTimeMillis() - startTime);
        
        log.info("Data validation completed. Status: {}, Errors: {}, Warnings: {}, Time: {}ms", 
                response.getStatus(), response.getErrors().size(), response.getWarnings().size(), 
                response.getValidationTimeMs());
        
        return response;
    }

    @Override
    public List<ValidationResponse> validateBatch(List<ValidationRequest> requests) {
        log.info("Validating batch of {} records", requests.size());
        
        List<ValidationResponse> responses = new ArrayList<>();
        
        for (ValidationRequest request : requests) {
            responses.add(validateData(request));
        }
        
        return responses;
    }

    @Override
    public Map<String, Object> cleanData(Map<String, Object> data, String dataType) {
        log.info("Cleaning data of type: {}", dataType);
        
        try {
            return dataCleaningService.cleanData(data, dataType);
        } catch (Exception e) {
            log.error("Error during data cleaning", e);
            // Return original data if cleaning fails
            return new HashMap<>(data);
        }
    }

    @Override
    public List<String> getValidationRules(String dataType) {
        log.info("Getting validation rules for data type: {}", dataType);
        
        try {
            return validationRuleEngine.getRuleNamesForDataType(dataType);
        } catch (Exception e) {
            log.error("Error getting validation rules", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void addValidationRule(String dataType, String ruleName, String ruleExpression) {
        log.info("Adding validation rule '{}' for data type: {}", ruleName, dataType);
        
        try {
            validationRuleEngine.addRule(dataType, ruleName, ruleExpression);
        } catch (Exception e) {
            log.error("Error adding validation rule", e);
            throw new RuntimeException("Failed to add validation rule: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeValidationRule(String dataType, String ruleName) {
        log.info("Removing validation rule '{}' for data type: {}", ruleName, dataType);
        
        try {
            validationRuleEngine.removeRule(dataType, ruleName);
        } catch (Exception e) {
            log.error("Error removing validation rule", e);
            throw new RuntimeException("Failed to remove validation rule: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate foreign key references
     */
    private ValidationResult validateReferences(String dataType, Map<String, Object> data) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        
        // TODO: Implement reference validation logic
        // This would check if referenced entities exist in the database
        // For example:
        // - Household references should validate PSN exists
        // - Individual references should validate household exists
        // - Economic profile references should validate household exists
        
        return result;
    }

    @Override
    public boolean validateHouseholdData(HouseholdDataRequest request) {
        log.info("Validating household data for: {}", request.getHouseholdNumber());

        try {
            // Create validation request
            ValidationRequest validationRequest = new ValidationRequest();
            validationRequest.setDataType("HOUSEHOLD");

            // Convert HouseholdDataRequest to Map for validation
            Map<String, Object> data = new HashMap<>();
            data.put("householdNumber", request.getHouseholdNumber());
            data.put("headOfHouseholdPsn", request.getHeadOfHouseholdPsn());
            data.put("monthlyIncome", request.getMonthlyIncome());
            data.put("totalMembers", request.getTotalMembers());
            data.put("region", request.getRegion());
            data.put("province", request.getProvince());
            data.put("municipality", request.getMunicipality());
            data.put("barangay", request.getBarangay());

            validationRequest.setData(data);
            validationRequest.setValidateReferences(request.getValidateReferences());

            // Perform validation
            ValidationResponse response = validateData(validationRequest);

            return response.isValid();

        } catch (Exception e) {
            log.error("Error validating household data: {}", e.getMessage(), e);
            return false;
        }
    }

    // Inner classes for validation results
    public static class ValidationRule {
        private String name;
        private String dataType;
        private String field;
        private String expression;
        private String errorMessage;
        private String severity;
        private boolean enabled;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDataType() { return dataType; }
        public void setDataType(String dataType) { this.dataType = dataType; }
        
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    public static class ValidationResult {
        private boolean valid = true;
        private String field;
        private String errorCode;
        private String errorMessage;
        private String rejectedValue;
        private String severity;
        private List<ValidationWarning> warnings = new ArrayList<>();
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(String rejectedValue) { this.rejectedValue = rejectedValue; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public List<ValidationWarning> getWarnings() { return warnings; }
        public void setWarnings(List<ValidationWarning> warnings) { this.warnings = warnings; }
        
        public boolean hasWarnings() { return warnings != null && !warnings.isEmpty(); }
    }
    
    public static class ValidationWarning {
        private String field;
        private String code;
        private String message;
        private String suggestion;
        
        // Getters and setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
