package ph.gov.dsr.grievance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ph.gov.dsr.grievance.entity.GrievanceCase;

import java.util.List;
import java.util.Map;

/**
 * DTO for case submission requests from various channels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseSubmissionRequest {

    @NotBlank(message = "Complainant PSN is required")
    private String complainantPsn;

    @NotBlank(message = "Complainant name is required")
    private String complainantName;

    @Email(message = "Valid email address is required")
    private String complainantEmail;

    private String complainantPhone;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private GrievanceCase.GrievanceCategory category;

    private GrievanceCase.Priority priority;

    private String submissionChannel;

    private List<String> attachments;

    private Map<String, Object> additionalData;

    private String preferredContactMethod;

    private String languagePreference;

    @Builder.Default
    private Boolean isAnonymous = false;

    private String relatedCaseNumber;

    private String organizationAffected;

    private String locationOfIncident;

    private String dateOfIncident;

    private List<String> witnessContacts;

    private String desiredOutcome;

    @Builder.Default
    private Boolean consentToContact = true;

    @Builder.Default
    private Boolean consentToInvestigate = true;

    // Helper methods
    
    /**
     * Check if request has valid contact information
     */
    public boolean hasValidContactInfo() {
        return (complainantEmail != null && !complainantEmail.trim().isEmpty()) ||
               (complainantPhone != null && !complainantPhone.trim().isEmpty());
    }

    /**
     * Get primary contact method
     */
    public String getPrimaryContactMethod() {
        if (preferredContactMethod != null) {
            return preferredContactMethod;
        }
        
        if (complainantEmail != null && !complainantEmail.trim().isEmpty()) {
            return "EMAIL";
        }
        
        if (complainantPhone != null && !complainantPhone.trim().isEmpty()) {
            return "PHONE";
        }
        
        return "POSTAL";
    }

    /**
     * Check if case is high priority based on content
     */
    public boolean isHighPriorityContent() {
        if (priority == GrievanceCase.Priority.CRITICAL || priority == GrievanceCase.Priority.HIGH) {
            return true;
        }
        
        String content = (subject + " " + description).toLowerCase();
        return content.contains("urgent") || content.contains("emergency") || 
               content.contains("critical") || content.contains("safety");
    }

    /**
     * Check if case involves sensitive categories
     */
    public boolean isSensitiveCategory() {
        return category == GrievanceCase.GrievanceCategory.CORRUPTION ||
               category == GrievanceCase.GrievanceCategory.DISCRIMINATION ||
               category == GrievanceCase.GrievanceCategory.DATA_PRIVACY ||
               category == GrievanceCase.GrievanceCategory.STAFF_CONDUCT;
    }
}
