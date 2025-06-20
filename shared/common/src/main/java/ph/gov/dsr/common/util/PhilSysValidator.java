package ph.gov.dsr.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Utility class for validating PhilSys Numbers (PSN) and related identifiers.
 * Implements validation rules according to PSA specifications.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Slf4j
@UtilityClass
public class PhilSysValidator {

    /**
     * Pattern for valid PhilSys Number (16 digits).
     */
    private static final Pattern PSN_PATTERN = Pattern.compile("^[0-9]{16}$");

    /**
     * Pattern for valid household ID.
     */
    private static final Pattern HOUSEHOLD_ID_PATTERN = Pattern.compile("^HH-[0-9]{4}-[0-9]{8}$");

    /**
     * Pattern for valid registration ID.
     */
    private static final Pattern REGISTRATION_ID_PATTERN = Pattern.compile("^REG-[0-9]{4}-[0-9]{8}$");

    /**
     * Pattern for valid service delivery log ID.
     */
    private static final Pattern SERVICE_LOG_ID_PATTERN = Pattern.compile("^SDL-[0-9]{4}-[0-9]{10}$");

    /**
     * Pattern for valid program code.
     */
    private static final Pattern PROGRAM_CODE_PATTERN = Pattern.compile("^[A-Z0-9_]{3,20}$");

    /**
     * Validates a PhilSys Number.
     * 
     * @param psn The PhilSys Number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPsn(String psn) {
        if (StringUtils.isBlank(psn)) {
            log.debug("PSN validation failed: PSN is blank");
            return false;
        }

        if (!PSN_PATTERN.matcher(psn).matches()) {
            log.debug("PSN validation failed: PSN '{}' does not match pattern", maskPsn(psn));
            return false;
        }

        // Additional validation: check digit validation (simplified)
        return isValidCheckDigit(psn);
    }

    /**
     * Validates a household ID.
     * 
     * @param householdId The household ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidHouseholdId(String householdId) {
        if (StringUtils.isBlank(householdId)) {
            return false;
        }

        return HOUSEHOLD_ID_PATTERN.matcher(householdId).matches();
    }

    /**
     * Validates a registration ID.
     * 
     * @param registrationId The registration ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRegistrationId(String registrationId) {
        if (StringUtils.isBlank(registrationId)) {
            return false;
        }

        return REGISTRATION_ID_PATTERN.matcher(registrationId).matches();
    }

    /**
     * Validates a service delivery log ID.
     * 
     * @param serviceLogId The service delivery log ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidServiceLogId(String serviceLogId) {
        if (StringUtils.isBlank(serviceLogId)) {
            return false;
        }

        return SERVICE_LOG_ID_PATTERN.matcher(serviceLogId).matches();
    }

    /**
     * Validates a program code.
     * 
     * @param programCode The program code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidProgramCode(String programCode) {
        if (StringUtils.isBlank(programCode)) {
            return false;
        }

        return PROGRAM_CODE_PATTERN.matcher(programCode).matches();
    }

    /**
     * Masks a PSN for logging purposes.
     * Shows only first 4 and last 4 digits.
     * 
     * @param psn The PSN to mask
     * @return Masked PSN
     */
    public static String maskPsn(String psn) {
        if (StringUtils.isBlank(psn) || psn.length() < 8) {
            return "****";
        }

        return psn.substring(0, 4) + "****" + psn.substring(psn.length() - 4);
    }

    /**
     * Validates the check digit of a PSN using a simplified algorithm.
     * In production, this should implement the actual PSA check digit algorithm.
     * 
     * @param psn The PSN to validate
     * @return true if check digit is valid, false otherwise
     */
    private static boolean isValidCheckDigit(String psn) {
        try {
            // Simplified check digit validation
            // In production, implement the actual PSA algorithm
            int sum = 0;
            for (int i = 0; i < 15; i++) {
                int digit = Character.getNumericValue(psn.charAt(i));
                sum += digit * (i % 2 == 0 ? 2 : 1);
            }

            int checkDigit = Character.getNumericValue(psn.charAt(15));
            int calculatedCheckDigit = (10 - (sum % 10)) % 10;

            boolean isValid = checkDigit == calculatedCheckDigit;
            if (!isValid) {
                log.debug("PSN check digit validation failed for PSN: {}", maskPsn(psn));
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error validating PSN check digit for PSN: {}", maskPsn(psn), e);
            return false;
        }
    }

    /**
     * Generates a household ID based on year and sequence.
     * 
     * @param year The year
     * @param sequence The sequence number
     * @return Generated household ID
     */
    public static String generateHouseholdId(int year, long sequence) {
        return String.format("HH-%04d-%08d", year, sequence);
    }

    /**
     * Generates a registration ID based on year and sequence.
     * 
     * @param year The year
     * @param sequence The sequence number
     * @return Generated registration ID
     */
    public static String generateRegistrationId(int year, long sequence) {
        return String.format("REG-%04d-%08d", year, sequence);
    }

    /**
     * Generates a service delivery log ID based on year and sequence.
     * 
     * @param year The year
     * @param sequence The sequence number
     * @return Generated service delivery log ID
     */
    public static String generateServiceLogId(int year, long sequence) {
        return String.format("SDL-%04d-%010d", year, sequence);
    }

    /**
     * Validates Philippine mobile number format.
     * 
     * @param mobileNumber The mobile number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidMobileNumber(String mobileNumber) {
        if (StringUtils.isBlank(mobileNumber)) {
            return false;
        }

        // Philippine mobile number patterns
        Pattern mobilePattern = Pattern.compile("^(\\+63|0)(9[0-9]{9})$");
        return mobilePattern.matcher(mobileNumber).matches();
    }

    /**
     * Validates Philippine zip code format.
     * 
     * @param zipCode The zip code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidZipCode(String zipCode) {
        if (StringUtils.isBlank(zipCode)) {
            return false;
        }

        Pattern zipPattern = Pattern.compile("^[0-9]{4}$");
        return zipPattern.matcher(zipCode).matches();
    }
}
