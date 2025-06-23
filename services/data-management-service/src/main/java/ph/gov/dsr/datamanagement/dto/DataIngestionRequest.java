package ph.gov.dsr.datamanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Request DTO for data ingestion operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class DataIngestionRequest {

    @NotBlank(message = "Source system is required")
    private String sourceSystem; // LISTAHANAN, I_REGISTRO, MANUAL_ENTRY

    @NotBlank(message = "Data type is required")
    private String dataType; // HOUSEHOLD, INDIVIDUAL, ECONOMIC_PROFILE

    @NotNull(message = "Data payload is required")
    private Map<String, Object> dataPayload;

    private String batchId;
    
    private LocalDateTime submissionDate;
    
    private String submittedBy;
    
    private Map<String, String> metadata;
    
    private boolean validateOnly = false; // If true, only validate without persisting
    
    private boolean skipDuplicateCheck = false; // If true, skip deduplication
    
    private String processingPriority = "NORMAL"; // HIGH, NORMAL, LOW
}
