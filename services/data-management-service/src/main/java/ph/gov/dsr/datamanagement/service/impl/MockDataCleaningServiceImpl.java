package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.service.DataCleaningService;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of DataCleaningService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockDataCleaningServiceImpl implements DataCleaningService {

    @Override
    public Map<String, Object> cleanData(Map<String, Object> data, String dataType) {
        log.info("Mock cleaning data of type: {}", dataType);
        
        Map<String, Object> cleanedData = new HashMap<>(data);
        
        // Mock cleaning operations
        for (Map.Entry<String, Object> entry : cleanedData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String stringValue = (String) value;
                // Basic mock cleaning: trim and normalize case for names
                if (key.toLowerCase().contains("name")) {
                    cleanedData.put(key, stringValue.trim().toUpperCase());
                } else {
                    cleanedData.put(key, stringValue.trim());
                }
            }
        }
        
        return cleanedData;
    }

    @Override
    public String cleanTextField(String value, String fieldType) {
        log.debug("Mock cleaning text field: {} of type: {}", value, fieldType);
        
        if (value == null) {
            return null;
        }
        
        String cleaned = value.trim();
        
        // Mock field-specific cleaning
        switch (fieldType.toLowerCase()) {
            case "firstname":
            case "lastname":
            case "middlename":
            case "name":
                cleaned = cleaned.toUpperCase();
                break;
            case "phone":
            case "phonenumber":
                cleaned = cleaned.replaceAll("[^0-9+]", "");
                break;
            case "email":
                cleaned = cleaned.toLowerCase();
                break;
        }
        
        return cleaned;
    }

    @Override
    public Number cleanNumericField(Object value, String fieldType) {
        log.debug("Mock cleaning numeric field: {} of type: {}", value, fieldType);
        
        if (value == null) {
            return null;
        }
        
        try {
            if (value instanceof Number) {
                return (Number) value;
            }
            
            String stringValue = value.toString().trim();
            stringValue = stringValue.replaceAll("[,\\s₱$]", "");
            
            return Double.parseDouble(stringValue);
            
        } catch (NumberFormatException e) {
            log.warn("Mock: Could not parse numeric value: {}", value);
            return null;
        }
    }

    @Override
    public String cleanDateField(Object value, String fieldType) {
        log.debug("Mock cleaning date field: {} of type: {}", value, fieldType);
        
        if (value == null) {
            return null;
        }
        
        // Mock date cleaning - just return as string
        return value.toString().trim();
    }

    @Override
    public String cleanPhoneNumber(String phoneNumber) {
        log.debug("Mock cleaning phone number: {}", phoneNumber);
        
        if (phoneNumber == null) {
            return null;
        }
        
        // Mock phone cleaning
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        if (cleaned.startsWith("0")) {
            cleaned = "+63" + cleaned.substring(1);
        }
        
        return cleaned;
    }

    @Override
    public Map<String, Object> cleanAddress(Map<String, Object> addressData) {
        log.debug("Mock cleaning address data");
        
        if (addressData == null) {
            return null;
        }
        
        Map<String, Object> cleanedAddress = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : addressData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                cleanedAddress.put(key, ((String) value).trim().toUpperCase());
            } else {
                cleanedAddress.put(key, value);
            }
        }
        
        return cleanedAddress;
    }

    @Override
    public String cleanPSN(String psn) {
        log.debug("Mock cleaning PSN: {}", psn);
        
        if (psn == null) {
            return null;
        }
        
        // Mock PSN cleaning
        String cleaned = psn.replaceAll("[^0-9-]", "");
        
        // Ensure proper format XXXX-XXXX-XXXX
        if (cleaned.length() == 12 && !cleaned.contains("-")) {
            cleaned = cleaned.substring(0, 4) + "-" + 
                     cleaned.substring(4, 8) + "-" + 
                     cleaned.substring(8, 12);
        }
        
        return cleaned;
    }

    @Override
    public String cleanHouseholdNumber(String householdNumber) {
        log.debug("Mock cleaning household number: {}", householdNumber);
        
        if (householdNumber == null) {
            return null;
        }
        
        return householdNumber.trim().toUpperCase();
    }

    @Override
    public String normalizeName(String name) {
        log.debug("Mock normalizing name: {}", name);
        
        if (name == null) {
            return null;
        }
        
        // Mock name normalization - convert to proper case
        String[] words = name.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word.substring(0, 1).toUpperCase())
                      .append(word.substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }

    @Override
    public String cleanEmail(String email) {
        log.debug("Mock cleaning email: {}", email);
        
        if (email == null) {
            return null;
        }
        
        return email.trim().toLowerCase();
    }

    @Override
    public String sanitizeInput(String input, String fieldType) {
        log.debug("Mock sanitizing input: {} of type: {}", input, fieldType);
        
        if (input == null) {
            return null;
        }
        
        // Mock sanitization - remove potentially dangerous characters
        return input.trim().replaceAll("[<>\"'&]", "");
    }

    @Override
    public Map<String, Object> applyDataTypeRules(Map<String, Object> data, String dataType) {
        log.debug("Mock applying data type rules for: {}", dataType);
        
        Map<String, Object> cleaned = new HashMap<>(data);
        
        // Mock data type specific cleaning
        switch (dataType.toUpperCase()) {
            case "HOUSEHOLD":
                if (cleaned.containsKey("headOfHouseholdName")) {
                    cleaned.put("headOfHouseholdName", 
                        normalizeName((String) cleaned.get("headOfHouseholdName")));
                }
                break;
            case "INDIVIDUAL":
                if (cleaned.containsKey("firstName")) {
                    cleaned.put("firstName", normalizeName((String) cleaned.get("firstName")));
                }
                if (cleaned.containsKey("lastName")) {
                    cleaned.put("lastName", normalizeName((String) cleaned.get("lastName")));
                }
                if (cleaned.containsKey("psn")) {
                    cleaned.put("psn", cleanPSN((String) cleaned.get("psn")));
                }
                break;
        }
        
        return cleaned;
    }
}
