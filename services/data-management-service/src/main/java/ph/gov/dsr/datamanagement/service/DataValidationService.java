package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.dto.HouseholdDataRequest;
import ph.gov.dsr.datamanagement.dto.ValidationRequest;
import ph.gov.dsr.datamanagement.dto.ValidationResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for data validation operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface DataValidationService {

    /**
     * Validate single data record
     */
    ValidationResponse validateData(ValidationRequest request);

    /**
     * Validate batch of data records
     */
    List<ValidationResponse> validateBatch(List<ValidationRequest> requests);

    /**
     * Clean and normalize data
     */
    Map<String, Object> cleanData(Map<String, Object> data, String dataType);

    /**
     * Get validation rules for data type
     */
    List<String> getValidationRules(String dataType);

    /**
     * Add custom validation rule
     */
    void addValidationRule(String dataType, String ruleName, String ruleExpression);

    /**
     * Remove validation rule
     */
    void removeValidationRule(String dataType, String ruleName);

    /**
     * Validate household data specifically
     */
    boolean validateHouseholdData(HouseholdDataRequest request);
}
