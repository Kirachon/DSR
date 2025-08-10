package ph.gov.dsr.grievance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for adding comments to cases
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCaseCommentRequest {

    @NotBlank(message = "Comment is required")
    private String comment;
    
    @NotBlank(message = "Author is required")
    private String author;
    
    private String authorRole;

    @Builder.Default
    private Boolean isInternal = false;
    
    private String visibility; // PUBLIC, INTERNAL, STAFF_ONLY
}
