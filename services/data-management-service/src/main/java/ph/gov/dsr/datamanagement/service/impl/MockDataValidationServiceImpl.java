package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.ValidationRequest;
import ph.gov.dsr.datamanagement.dto.ValidationResponse;
import ph.gov.dsr.datamanagement.service.DataValidationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of DataValidationService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Slf4j
public class MockDataValidationServiceImpl implements DataValidationService {

    @Override
    public ValidationResponse validateData(ValidationRequest request) {
        log.info("Mock validating data of type: {}", request.getDataType());
        
        ValidationResponse response = new ValidationResponse();
        response.setValid(true);
        response.setStatus("VALID");
        response.setValidatedAt(LocalDateTime.now());
        response.setErrors(new ArrayList<>());
        response.setWarnings(new ArrayList<>());
        response.setValidationProfile(request.getValidationProfile());
        response.setValidationTimeMs(100L);
        
        // Add some mock warnings for demonstration
        if (request.isIncludeWarnings()) {
            ValidationResponse.ValidationWarning warning = new ValidationResponse.ValidationWarning();
            warning.setField("phoneNumber");
            warning.setCode("FORMAT_SUGGESTION");
            warning.setMessage("Phone number format could be standardized");
            warning.setSuggestion("Consider using +63 format for Philippine numbers");
            response.setWarnings(List.of(warning));
        }
        
        return response;
    }

    @Override
    public List<ValidationResponse> validateBatch(List<ValidationRequest> requests) {
        log.info("Mock validating batch of {} records", requests.size());
        
        List<ValidationResponse> responses = new ArrayList<>();
        for (ValidationRequest request : requests) {
            responses.add(validateData(request));
        }
        
        return responses;
    }

    @Override
    public Map<String, Object> cleanData(Map<String, Object> data, String dataType) {
        log.info("Mock cleaning data of type: {}", dataType);
        
        Map<String, Object> cleanedData = new HashMap<>(data);
        
        // Mock data cleaning operations
        if (cleanedData.containsKey("firstName")) {
            String firstName = (String) cleanedData.get("firstName");
            cleanedData.put("firstName", firstName.trim().toUpperCase());
        }
        
        if (cleanedData.containsKey("lastName")) {
            String lastName = (String) cleanedData.get("lastName");
            cleanedData.put("lastName", lastName.trim().toUpperCase());
        }
        
        if (cleanedData.containsKey("phoneNumber")) {
            String phone = (String) cleanedData.get("phoneNumber");
            // Mock phone number standardization
            cleanedData.put("phoneNumber", phone.replaceAll("[^0-9+]", ""));
        }
        
        return cleanedData;
    }

    @Override
    public List<String> getValidationRules(String dataType) {
        log.info("Mock getting validation rules for data type: {}", dataType);
        
        return switch (dataType.toUpperCase()) {
            case "HOUSEHOLD" -> List.of(
                "household_number_required",
                "head_of_household_psn_valid",
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
                "income_sources_valid",
                "assets_non_negative",
                "expenses_non_negative"
            );
            default -> List.of("basic_validation");
        };
    }

    @Override
    public void addValidationRule(String dataType, String ruleName, String ruleExpression) {
        log.info("Mock adding validation rule '{}' for data type: {}", ruleName, dataType);
        // Mock implementation - would store in database in real implementation
    }

    @Override
    public void removeValidationRule(String dataType, String ruleName) {
        log.info("Mock removing validation rule '{}' for data type: {}", ruleName, dataType);
        // Mock implementation - would remove from database in real implementation
    }
}
