package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ph.gov.dsr.security.entity.RemediationPriority;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for individual remediation recommendations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemediationRecommendation {

    private String id;
    private String vulnerabilityId;
    private String title;
    private String description;
    private String recommendation;
    private RemediationPriority priority;
    private Integer estimatedEffort; // in hours
    private String complexity; // LOW, MEDIUM, HIGH
    private String category;
    private String impact;
    private String riskReduction;
    private List<String> steps;
    private List<String> resources;
    private List<String> tools;
    private String assignedTo;
    private String assignedTeam;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, DEFERRED
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private String completionNotes;
    private String validationMethod;
    private Boolean validated;
    private String validationNotes;
    private String businessJustification;
    private String technicalDetails;
    private String prerequisites;
    private String rollbackPlan;
    private String testingRequirements;
    private String communicationPlan;
    private Double costEstimate;
    private String costJustification;
    private List<String> dependencies;
    private String tags;
    private String metadata;
    
    /**
     * Check if recommendation is high priority
     */
    public boolean isHighPriority() {
        return priority != null && priority.requiresImmediateAttention();
    }
    
    /**
     * Check if recommendation is critical
     */
    public boolean isCritical() {
        return priority == RemediationPriority.CRITICAL || priority == RemediationPriority.EMERGENCY;
    }
    
    /**
     * Check if recommendation is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * Check if recommendation is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }
    
    /**
     * Check if recommendation is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !isCompleted();
    }
    
    /**
     * Get days until due date
     */
    public long getDaysUntilDue() {
        if (dueDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);
    }
    
    /**
     * Get completion percentage based on status
     */
    public int getCompletionPercentage() {
        switch (status != null ? status : "PENDING") {
            case "COMPLETED": return 100;
            case "IN_PROGRESS": return 50;
            case "DEFERRED": return 0;
            case "PENDING": return 0;
            default: return 0;
        }
    }
    
    /**
     * Get effective complexity (default to MEDIUM if not specified)
     */
    public String getEffectiveComplexity() {
        return complexity != null ? complexity : "MEDIUM";
    }
    
    /**
     * Get effective estimated effort (default to 4 hours if not specified)
     */
    public int getEffectiveEstimatedEffort() {
        return estimatedEffort != null ? estimatedEffort : 4;
    }
    
    /**
     * Check if recommendation has dependencies
     */
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
    
    /**
     * Check if recommendation requires validation
     */
    public boolean requiresValidation() {
        return validationMethod != null && !validationMethod.trim().isEmpty();
    }
    
    /**
     * Check if recommendation is validated
     */
    public boolean isValidated() {
        return Boolean.TRUE.equals(validated);
    }
    
    /**
     * Get risk score based on priority and impact
     */
    public int getRiskScore() {
        int score = 0;
        
        if (priority != null) {
            score += priority.getNumericValue() * 20; // 20-100 based on priority
        }
        
        if ("HIGH".equals(impact)) {
            score += 20;
        } else if ("MEDIUM".equals(impact)) {
            score += 10;
        }
        
        if (isOverdue()) {
            score += 15;
        }
        
        return Math.min(score, 100);
    }
}
