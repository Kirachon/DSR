package ph.gov.dsr.datamanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for data ingestion operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class DataIngestionResponse {

    private UUID ingestionId;
    
    private String status; // SUCCESS, FAILED, PARTIAL, PROCESSING
    
    private String message;
    
    private LocalDateTime processedAt;
    
    private String batchId;
    
    private int totalRecords;
    
    private int successfulRecords;
    
    private int failedRecords;
    
    private int duplicateRecords;
    
    private List<ValidationError> validationErrors;
    
    private List<String> warnings;
    
    private String processingTimeMs;
    
    @Data
    public static class ValidationError {
        private String field;
        private String message;
        private String rejectedValue;
        private int recordIndex;
    }
}
