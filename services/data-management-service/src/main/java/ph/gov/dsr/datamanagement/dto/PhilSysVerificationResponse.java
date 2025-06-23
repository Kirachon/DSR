package ph.gov.dsr.datamanagement.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for PhilSys verification operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class PhilSysVerificationResponse {

    private boolean valid; // Changed from isVerified to match implementation

    private String verificationStatus; // VERIFIED, NOT_FOUND, MISMATCH, ERROR

    private String psn;

    private LocalDateTime verifiedAt;

    // Person information fields (flattened for easier access)
    private String firstName;
    private String lastName;
    private String middleName;
    private String dateOfBirth; // Changed to String to match implementation
    private String sex;
    private String placeOfBirth;
    private String civilStatus;
    private String citizenship;

    // Verification details
    private double confidenceScore;
    private double matchScore;

    private String errorMessage;

    private String requestId;

    private long responseTimeMs;

    // Nested person info for backward compatibility
    private VerifiedPersonInfo personInfo;

    @Data
    public static class VerifiedPersonInfo {
        private String firstName;
        private String lastName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String sex;
        private String placeOfBirth;
        private String civilStatus;
        private String citizenship;
        private boolean isActive;
        private LocalDate registrationDate;
    }
}
