package ph.gov.dsr.datamanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for PhilSys verification operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class PhilSysVerificationRequest {

    @NotBlank(message = "PSN is required")
    private String psn; // PhilSys Number

    private String firstName;
    
    private String lastName;
    
    private String middleName;
    
    private LocalDate dateOfBirth;
    
    private String sex; // M, F
    
    private String placeOfBirth;
    
    private boolean verifyBiometrics = false; // If true, include biometric verification
    
    private String verificationLevel = "BASIC"; // BASIC, ENHANCED, FULL
    
    private String requestId; // For tracking purposes
}
