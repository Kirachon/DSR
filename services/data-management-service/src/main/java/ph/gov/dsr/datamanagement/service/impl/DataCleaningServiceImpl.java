package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.service.DataCleaningService;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Production implementation of DataCleaningService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("!no-db")
@Slf4j
public class DataCleaningServiceImpl implements DataCleaningService {

    // Common patterns for cleaning
    private static final Pattern PHONE_CLEANUP_PATTERN = Pattern.compile("[^0-9+]");
    private static final Pattern PSN_CLEANUP_PATTERN = Pattern.compile("[^0-9-]");
    private static final Pattern NAME_CLEANUP_PATTERN = Pattern.compile("[^a-zA-Z\\s.-]");
    private static final Pattern EXTRA_SPACES_PATTERN = Pattern.compile("\\s+");
    
    // Date formatters for parsing various date formats
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy")
    };

    @Override
    public Map<String, Object> cleanData(Map<String, Object> data, String dataType) {
        log.debug("Cleaning data of type: {}", dataType);
        
        Map<String, Object> cleanedData = new HashMap<>(data);
        
        // Apply general cleaning to all fields
        for (Map.Entry<String, Object> entry : cleanedData.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String cleanedValue = cleanTextField((String) value, fieldName);
                cleanedData.put(fieldName, cleanedValue);
            }
        }
        
        // Apply data type specific cleaning
        cleanedData = applyDataTypeRules(cleanedData, dataType);
        
        return cleanedData;
    }

    @Override
    public String cleanTextField(String value, String fieldType) {
        if (value == null) {
            return null;
        }
        
        String cleaned = value.trim();
        
        // Remove control characters and normalize Unicode
        cleaned = Normalizer.normalize(cleaned, Normalizer.Form.NFC);
        cleaned = cleaned.replaceAll("\\p{Cntrl}", "");
        
        // Field-specific cleaning
        switch (fieldType.toLowerCase()) {
            case "firstname":
            case "lastname":
            case "middlename":
            case "name":
                cleaned = normalizeName(cleaned);
                break;
            case "phone":
            case "phonenumber":
            case "mobilenumber":
                cleaned = cleanPhoneNumber(cleaned);
                break;
            case "email":
            case "emailaddress":
                cleaned = cleanEmail(cleaned);
                break;
            case "psn":
            case "philsysnumber":
                cleaned = cleanPSN(cleaned);
                break;
            case "householdnumber":
                cleaned = cleanHouseholdNumber(cleaned);
                break;
            default:
                // General text cleaning
                cleaned = sanitizeInput(cleaned, fieldType);
        }
        
        return cleaned;
    }

    @Override
    public Number cleanNumericField(Object value, String fieldType) {
        if (value == null) {
            return null;
        }
        
        try {
            if (value instanceof Number) {
                return (Number) value;
            }
            
            String stringValue = value.toString().trim();
            
            // Remove common formatting characters
            stringValue = stringValue.replaceAll("[,\\s]", "");
            
            // Handle currency symbols
            stringValue = stringValue.replaceAll("[₱$]", "");
            
            // Parse as double first, then convert to appropriate type
            double doubleValue = Double.parseDouble(stringValue);
            
            // Return as integer if it's a whole number and field suggests integer
            if (doubleValue == Math.floor(doubleValue) && 
                (fieldType.toLowerCase().contains("count") || 
                 fieldType.toLowerCase().contains("members") ||
                 fieldType.toLowerCase().contains("age"))) {
                return (int) doubleValue;
            }
            
            return doubleValue;
            
        } catch (NumberFormatException e) {
            log.warn("Could not parse numeric value: {} for field: {}", value, fieldType);
            return null;
        }
    }

    @Override
    public String cleanDateField(Object value, String fieldType) {
        if (value == null) {
            return null;
        }
        
        String stringValue = value.toString().trim();
        
        // Try to parse with various formats
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(stringValue, formatter);
                // Return in standard ISO format
                return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        
        log.warn("Could not parse date value: {} for field: {}", value, fieldType);
        return stringValue; // Return original if parsing fails
    }

    @Override
    public String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Remove all non-numeric characters except +
        String cleaned = PHONE_CLEANUP_PATTERN.matcher(phoneNumber).replaceAll("");
        
        // Standardize Philippine numbers
        if (cleaned.startsWith("0")) {
            cleaned = "+63" + cleaned.substring(1);
        } else if (cleaned.startsWith("63") && !cleaned.startsWith("+63")) {
            cleaned = "+" + cleaned;
        } else if (!cleaned.startsWith("+") && cleaned.length() == 10) {
            cleaned = "+63" + cleaned;
        }
        
        return cleaned;
    }

    @Override
    public Map<String, Object> cleanAddress(Map<String, Object> addressData) {
        if (addressData == null) {
            return null;
        }
        
        Map<String, Object> cleanedAddress = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : addressData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String cleanedValue = ((String) value).trim();
                
                // Normalize address components
                cleanedValue = cleanedValue.toUpperCase();
                cleanedValue = EXTRA_SPACES_PATTERN.matcher(cleanedValue).replaceAll(" ");
                
                // Remove common abbreviations and standardize
                cleanedValue = cleanedValue
                    .replaceAll("\\bST\\.?\\b", "STREET")
                    .replaceAll("\\bAVE\\.?\\b", "AVENUE")
                    .replaceAll("\\bBLVD\\.?\\b", "BOULEVARD")
                    .replaceAll("\\bRD\\.?\\b", "ROAD")
                    .replaceAll("\\bBRGY\\.?\\b", "BARANGAY");
                
                cleanedAddress.put(key, cleanedValue);
            } else {
                cleanedAddress.put(key, value);
            }
        }
        
        return cleanedAddress;
    }

    @Override
    public String cleanPSN(String psn) {
        if (psn == null) {
            return null;
        }
        
        // Remove all non-numeric and non-dash characters
        String cleaned = PSN_CLEANUP_PATTERN.matcher(psn).replaceAll("");
        
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
        if (householdNumber == null) {
            return null;
        }
        
        String cleaned = householdNumber.trim().toUpperCase();
        
        // Remove extra spaces
        cleaned = EXTRA_SPACES_PATTERN.matcher(cleaned).replaceAll(" ");
        
        // Standardize format if it looks like a household number
        if (cleaned.matches(".*\\d{6,}.*")) {
            // Keep alphanumeric and dashes
            cleaned = cleaned.replaceAll("[^A-Z0-9-]", "");
        }
        
        return cleaned;
    }

    @Override
    public String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        
        // Remove invalid characters for names
        String cleaned = NAME_CLEANUP_PATTERN.matcher(name).replaceAll("");
        
        // Normalize spaces
        cleaned = EXTRA_SPACES_PATTERN.matcher(cleaned.trim()).replaceAll(" ");
        
        // Convert to proper case
        String[] words = cleaned.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                
                // Handle special cases like "de", "del", "van", etc.
                if (word.toLowerCase().matches("^(de|del|van|von|la|le|da|dos|das)$")) {
                    result.append(word.toLowerCase());
                } else {
                    result.append(word.substring(0, 1).toUpperCase())
                          .append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }

    @Override
    public String cleanEmail(String email) {
        if (email == null) {
            return null;
        }
        
        String cleaned = email.trim().toLowerCase();
        
        // Remove spaces
        cleaned = cleaned.replaceAll("\\s", "");
        
        return cleaned;
    }

    @Override
    public String sanitizeInput(String input, String fieldType) {
        if (input == null) {
            return null;
        }
        
        String sanitized = input.trim();
        
        // Remove potentially dangerous characters
        sanitized = sanitized.replaceAll("[<>\"'&]", "");
        
        // Normalize spaces
        sanitized = EXTRA_SPACES_PATTERN.matcher(sanitized).replaceAll(" ");
        
        return sanitized;
    }

    @Override
    public Map<String, Object> applyDataTypeRules(Map<String, Object> data, String dataType) {
        Map<String, Object> cleaned = new HashMap<>(data);
        
        switch (dataType.toUpperCase()) {
            case "HOUSEHOLD":
                cleaned = cleanHouseholdData(cleaned);
                break;
            case "INDIVIDUAL":
                cleaned = cleanIndividualData(cleaned);
                break;
            case "ECONOMIC_PROFILE":
                cleaned = cleanEconomicProfileData(cleaned);
                break;
        }
        
        return cleaned;
    }
    
    private Map<String, Object> cleanHouseholdData(Map<String, Object> data) {
        Map<String, Object> cleaned = new HashMap<>(data);
        
        // Clean household-specific fields
        if (cleaned.containsKey("householdNumber")) {
            cleaned.put("householdNumber", cleanHouseholdNumber((String) cleaned.get("householdNumber")));
        }
        
        if (cleaned.containsKey("headOfHouseholdName")) {
            cleaned.put("headOfHouseholdName", normalizeName((String) cleaned.get("headOfHouseholdName")));
        }
        
        if (cleaned.containsKey("totalMembers")) {
            cleaned.put("totalMembers", cleanNumericField(cleaned.get("totalMembers"), "totalMembers"));
        }
        
        if (cleaned.containsKey("monthlyIncome")) {
            cleaned.put("monthlyIncome", cleanNumericField(cleaned.get("monthlyIncome"), "monthlyIncome"));
        }
        
        return cleaned;
    }
    
    private Map<String, Object> cleanIndividualData(Map<String, Object> data) {
        Map<String, Object> cleaned = new HashMap<>(data);
        
        // Clean individual-specific fields
        if (cleaned.containsKey("psn")) {
            cleaned.put("psn", cleanPSN((String) cleaned.get("psn")));
        }
        
        if (cleaned.containsKey("firstName")) {
            cleaned.put("firstName", normalizeName((String) cleaned.get("firstName")));
        }
        
        if (cleaned.containsKey("lastName")) {
            cleaned.put("lastName", normalizeName((String) cleaned.get("lastName")));
        }
        
        if (cleaned.containsKey("middleName")) {
            cleaned.put("middleName", normalizeName((String) cleaned.get("middleName")));
        }
        
        if (cleaned.containsKey("dateOfBirth")) {
            cleaned.put("dateOfBirth", cleanDateField(cleaned.get("dateOfBirth"), "dateOfBirth"));
        }
        
        if (cleaned.containsKey("sex")) {
            String sex = (String) cleaned.get("sex");
            if (sex != null) {
                cleaned.put("sex", sex.trim().toUpperCase());
            }
        }
        
        if (cleaned.containsKey("civilStatus")) {
            String status = (String) cleaned.get("civilStatus");
            if (status != null) {
                cleaned.put("civilStatus", status.trim().toUpperCase());
            }
        }
        
        return cleaned;
    }
    
    private Map<String, Object> cleanEconomicProfileData(Map<String, Object> data) {
        Map<String, Object> cleaned = new HashMap<>(data);
        
        // Clean economic profile-specific fields
        if (cleaned.containsKey("totalAssets")) {
            cleaned.put("totalAssets", cleanNumericField(cleaned.get("totalAssets"), "totalAssets"));
        }
        
        if (cleaned.containsKey("monthlyExpenses")) {
            cleaned.put("monthlyExpenses", cleanNumericField(cleaned.get("monthlyExpenses"), "monthlyExpenses"));
        }
        
        if (cleaned.containsKey("monthlyIncome")) {
            cleaned.put("monthlyIncome", cleanNumericField(cleaned.get("monthlyIncome"), "monthlyIncome"));
        }
        
        return cleaned;
    }
}
