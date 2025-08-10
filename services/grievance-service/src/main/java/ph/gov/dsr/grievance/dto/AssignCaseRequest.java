package ph.gov.dsr.grievance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for assigning cases to staff
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignCaseRequest {

    @NotBlank(message = "Assigned to is required")
    private String assignedTo;
    
    private String assignedByRole;
    
    @NotBlank(message = "Assigned by is required")
    private String assignedBy;
    
    private String reason;
    
    private String notes;
}
