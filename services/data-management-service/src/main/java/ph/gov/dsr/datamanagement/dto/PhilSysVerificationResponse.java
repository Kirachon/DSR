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

    private boolean isVerified;
    
    private String verificationStatus; // VERIFIED, NOT_FOUND, MISMATCH, ERROR
    
    private String psn;
    
    private LocalDateTime verifiedAt;
    
    private VerifiedPersonInfo personInfo;
    
    private String errorMessage;
    
    private String requestId;
    
    private long responseTimeMs;
    
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
