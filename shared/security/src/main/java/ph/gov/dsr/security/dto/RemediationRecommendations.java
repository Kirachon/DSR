package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for collection of remediation recommendations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemediationRecommendations {

    private String scanId;
    private String reportId;
    private List<RemediationRecommendation> recommendations;
    private List<String> prioritizedActions;
    private String estimatedEffort;
    private String totalCostEstimate;
    private String timelineEstimate;
    private String riskReductionSummary;
    private String executiveSummary;
    private String technicalSummary;
    private String implementationPlan;
    private String resourceRequirements;
    private String budgetRequirements;
    private String skillsRequirements;
    private String toolsRequirements;
    private String complianceImpact;
    private String businessImpact;
    private String riskAssessment;
    private String successCriteria;
    private String monitoringPlan;
    private String communicationPlan;
    private String rollbackStrategy;
    private String lessonsLearned;
    private LocalDateTime generatedAt;
    private LocalDateTime validUntil;
    private String generatedBy;
    private String approvedBy;
    private String status; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, IMPLEMENTED
    private String version;
    private String tags;
    private String metadata;
    
    /**
     * Get total number of recommendations
     */
    public int getTotalRecommendations() {
        return recommendations != null ? recommendations.size() : 0;
    }
    
    /**
     * Get number of critical recommendations
     */
    public long getCriticalRecommendations() {
        return recommendations != null ? 
            recommendations.stream().filter(RemediationRecommendation::isCritical).count() : 0;
    }
    
    /**
     * Get number of high priority recommendations
     */
    public long getHighPriorityRecommendations() {
        return recommendations != null ? 
            recommendations.stream().filter(RemediationRecommendation::isHighPriority).count() : 0;
    }
    
    /**
     * Get number of completed recommendations
     */
    public long getCompletedRecommendations() {
        return recommendations != null ? 
            recommendations.stream().filter(RemediationRecommendation::isCompleted).count() : 0;
    }
    
    /**
     * Get number of in-progress recommendations
     */
    public long getInProgressRecommendations() {
        return recommendations != null ? 
            recommendations.stream().filter(RemediationRecommendation::isInProgress).count() : 0;
    }
    
    /**
     * Get number of overdue recommendations
     */
    public long getOverdueRecommendations() {
        return recommendations != null ? 
            recommendations.stream().filter(RemediationRecommendation::isOverdue).count() : 0;
    }
    
    /**
     * Calculate completion percentage
     */
    public double getCompletionPercentage() {
        if (getTotalRecommendations() == 0) {
            return 0.0;
        }
        return (double) getCompletedRecommendations() / getTotalRecommendations() * 100.0;
    }
    
    /**
     * Get total estimated effort in hours
     */
    public int getTotalEstimatedEffortHours() {
        return recommendations != null ? 
            recommendations.stream()
                .mapToInt(RemediationRecommendation::getEffectiveEstimatedEffort)
                .sum() : 0;
    }
    
    /**
     * Get average risk score
     */
    public double getAverageRiskScore() {
        return recommendations != null && !recommendations.isEmpty() ? 
            recommendations.stream()
                .mapToInt(RemediationRecommendation::getRiskScore)
                .average()
                .orElse(0.0) : 0.0;
    }
    
    /**
     * Check if recommendations are approved
     */
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
    
    /**
     * Check if recommendations are implemented
     */
    public boolean isImplemented() {
        return "IMPLEMENTED".equals(status);
    }
    
    /**
     * Check if recommendations are expired
     */
    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }
    
    /**
     * Get recommendations by priority
     */
    public List<RemediationRecommendation> getRecommendationsByPriority(String priority) {
        return recommendations != null ? 
            recommendations.stream()
                .filter(r -> r.getPriority() != null && 
                           r.getPriority().name().equals(priority))
                .toList() : List.of();
    }
    
    /**
     * Get recommendations by status
     */
    public List<RemediationRecommendation> getRecommendationsByStatus(String status) {
        return recommendations != null ? 
            recommendations.stream()
                .filter(r -> status.equals(r.getStatus()))
                .toList() : List.of();
    }
    
    /**
     * Get next actions to take
     */
    public List<String> getNextActions() {
        return prioritizedActions != null && !prioritizedActions.isEmpty() ? 
            prioritizedActions.subList(0, Math.min(5, prioritizedActions.size())) : List.of();
    }
}
