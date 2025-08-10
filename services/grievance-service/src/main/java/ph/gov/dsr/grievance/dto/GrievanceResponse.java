package ph.gov.dsr.grievance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.entity.CaseActivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for Grievance Case operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceResponse {

    private UUID id;
    private String caseNumber;
    private String complainantPsn;
    private String complainantName;
    private String complainantEmail;
    private String complainantPhone;
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String submissionChannel;
    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdated;
    private String assignedTo;
    private LocalDateTime assignedDate;
    private String programCode;
    private String serviceProvider;
    private String region;
    private String province;
    private String municipality;
    private String barangay;
    private Boolean isUrgent;
    private Boolean isAnonymous;
    private Boolean requiresInvestigation;
    private Integer escalationLevel;
    private String escalatedTo;
    private LocalDateTime escalationDate;
    private LocalDateTime resolutionTargetDate;
    private LocalDateTime resolutionDate;
    private String resolutionSummary;
    private String resolutionActions;
    private String complainantSatisfaction;
    private String satisfactionComments;
    private String createdBy;
    private String updatedBy;
    private List<CaseActivityResponse> activities;

    /**
     * Convert GrievanceCase entity to response DTO
     */
    public static GrievanceResponse fromEntity(GrievanceCase grievanceCase) {
        if (grievanceCase == null) {
            return null;
        }

        return GrievanceResponse.builder()
                .id(grievanceCase.getId())
                .caseNumber(grievanceCase.getCaseNumber())
                .complainantPsn(grievanceCase.getComplainantPsn())
                .complainantName(grievanceCase.getComplainantName())
                .complainantEmail(grievanceCase.getComplainantEmail())
                .complainantPhone(grievanceCase.getComplainantPhone())
                .subject(grievanceCase.getSubject())
                .description(grievanceCase.getDescription())
                .category(grievanceCase.getCategory() != null ? grievanceCase.getCategory().name() : null)
                .priority(grievanceCase.getPriority() != null ? grievanceCase.getPriority().name() : null)
                .status(grievanceCase.getStatus() != null ? grievanceCase.getStatus().name() : null)
                .submissionChannel(grievanceCase.getSubmissionChannel())
                .submissionDate(grievanceCase.getSubmissionDate())
                .lastUpdated(grievanceCase.getUpdatedAt())
                .assignedTo(grievanceCase.getAssignedTo())
                .assignedDate(grievanceCase.getAssignedDate())
                .programCode(grievanceCase.getProgramCode())
                .serviceProvider(grievanceCase.getServiceProvider())
                .region(null) // Not available in entity
                .province(null) // Not available in entity
                .municipality(null) // Not available in entity
                .barangay(null) // Not available in entity
                .isUrgent(grievanceCase.getIsUrgent())
                .isAnonymous(grievanceCase.getIsAnonymous())
                .requiresInvestigation(grievanceCase.getRequiresInvestigation())
                .escalationLevel(grievanceCase.getEscalationLevel())
                .escalatedTo(grievanceCase.getEscalatedTo())
                .escalationDate(grievanceCase.getEscalationDate())
                .resolutionTargetDate(grievanceCase.getResolutionTargetDate())
                .resolutionDate(grievanceCase.getResolutionDate())
                .resolutionSummary(grievanceCase.getResolutionSummary())
                .resolutionActions(grievanceCase.getResolutionActions())
                .complainantSatisfaction(grievanceCase.getComplainantSatisfaction())
                .satisfactionComments(grievanceCase.getFeedbackComments())
                .createdBy(grievanceCase.getCreatedBy())
                .updatedBy(grievanceCase.getUpdatedBy())
                .activities(grievanceCase.getActivities() != null ? 
                    grievanceCase.getActivities().stream()
                        .map(CaseActivityResponse::fromEntity)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    /**
     * Nested DTO for case activities
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CaseActivityResponse {
        private UUID id;
        private String activityType;
        private String description;
        private String performedBy;
        private String performedByRole;
        private LocalDateTime performedAt;
        private String assignedBefore;
        private String assignedAfter;
        private String statusBefore;
        private String statusAfter;
        private String channel;
        private String direction;
        private String subject;
        private String content;
        private String recipient;
        private LocalDateTime scheduledTime;
        private Boolean isAutomated;
        private Boolean isInternal;

        public static CaseActivityResponse fromEntity(CaseActivity activity) {
            if (activity == null) {
                return null;
            }

            return CaseActivityResponse.builder()
                    .id(activity.getId())
                    .activityType(activity.getActivityType() != null ? activity.getActivityType().name() : null)
                    .description(activity.getDescription())
                    .performedBy(activity.getPerformedBy())
                    .performedByRole(activity.getPerformedByRole())
                    .performedAt(activity.getActivityDate())
                    .assignedBefore(activity.getAssignedBefore())
                    .assignedAfter(activity.getAssignedAfter())
                    .statusBefore(activity.getStatusBefore())
                    .statusAfter(activity.getStatusAfter())
                    .channel(activity.getCommunicationChannel())
                    .direction(activity.getCommunicationDirection())
                    .subject(activity.getSubject())
                    .content(activity.getContent())
                    .recipient(activity.getRecipient())
                    .scheduledTime(activity.getResponseDueDate())
                    .isAutomated(activity.getIsAutomated())
                    .isInternal(activity.getIsInternal())
                    .build();
        }
    }
}
