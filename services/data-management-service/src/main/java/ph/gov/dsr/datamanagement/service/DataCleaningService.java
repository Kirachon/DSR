package ph.gov.dsr.datamanagement.service;

import java.util.Map;

/**
 * Service interface for data cleaning operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface DataCleaningService {

    /**
     * Clean and normalize data
     */
    Map<String, Object> cleanData(Map<String, Object> data, String dataType);

    /**
     * Clean text fields (trim, normalize case, remove special characters)
     */
    String cleanTextField(String value, String fieldType);

    /**
     * Clean numeric fields (remove formatting, validate ranges)
     */
    Number cleanNumericField(Object value, String fieldType);

    /**
     * Clean date fields (parse and normalize format)
     */
    String cleanDateField(Object value, String fieldType);

    /**
     * Clean phone number fields (standardize format)
     */
    String cleanPhoneNumber(String phoneNumber);

    /**
     * Clean address fields (standardize format, geocode if possible)
     */
    Map<String, Object> cleanAddress(Map<String, Object> addressData);

    /**
     * Clean PSN (PhilSys Number) format
     */
    String cleanPSN(String psn);

    /**
     * Clean household number format
     */
    String cleanHouseholdNumber(String householdNumber);

    /**
     * Normalize name fields (proper case, remove extra spaces)
     */
    String normalizeName(String name);

    /**
     * Clean and validate email addresses
     */
    String cleanEmail(String email);

    /**
     * Remove or replace invalid characters
     */
    String sanitizeInput(String input, String fieldType);

    /**
     * Apply data type specific cleaning rules
     */
    Map<String, Object> applyDataTypeRules(Map<String, Object> data, String dataType);
}
