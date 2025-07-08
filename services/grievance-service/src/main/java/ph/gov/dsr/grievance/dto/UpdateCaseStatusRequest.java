package ph.gov.dsr.grievance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating case status
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCaseStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
    
    private String reason;
    
    private String notes;
    
    @NotBlank(message = "Updated by is required")
    private String updatedBy;
    
    private String updatedByRole;
}
