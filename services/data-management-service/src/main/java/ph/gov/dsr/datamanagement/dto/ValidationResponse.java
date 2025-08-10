package ph.gov.dsr.datamanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for data validation operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class ValidationResponse {

    private boolean isValid;
    
    private String status; // VALID, INVALID, WARNING
    
    private LocalDateTime validatedAt;
    
    private List<ValidationError> errors;
    
    private List<ValidationWarning> warnings;
    
    private String validationProfile;
    
    private long validationTimeMs;
    
    @Data
    public static class ValidationError {
        private String field;
        private String code;
        private String message;
        private String rejectedValue;
        private String severity; // ERROR, WARNING, INFO
    }
    
    @Data
    public static class ValidationWarning {
        private String field;
        private String code;
        private String message;
        private String suggestion;
    }
}
