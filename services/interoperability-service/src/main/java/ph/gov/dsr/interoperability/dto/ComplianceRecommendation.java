package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for compliance recommendations
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compliance recommendation")
public class ComplianceRecommendation {

    @Schema(description = "Recommendation identifier", example = "REC-001")
    private String recommendationId;

    @Schema(description = "Compliance standard", example = "FHIR")
    private String standard;

    @Schema(description = "Recommendation category", example = "STRUCTURE_IMPROVEMENT")
    private String category;

    @Schema(description = "Recommendation priority", example = "HIGH")
    private String priority;

    @Schema(description = "Recommendation title", example = "Implement FHIR validation")
    private String title;

    @Schema(description = "Recommendation description")
    private String description;

    @Schema(description = "Rationale for the recommendation")
    private String rationale;

    @Schema(description = "Expected benefits")
    private List<String> expectedBenefits;

    @Schema(description = "Implementation steps")
    private List<String> implementationSteps;

    @Schema(description = "Estimated effort", example = "MEDIUM")
    private String estimatedEffort;

    @Schema(description = "Estimated cost", example = "LOW")
    private String estimatedCost;

    @Schema(description = "Implementation timeline", example = "2-4 weeks")
    private String timeline;

    @Schema(description = "Required resources")
    private List<String> requiredResources;

    @Schema(description = "Risk mitigation aspects")
    private List<String> riskMitigation;

    @Schema(description = "Success criteria")
    private List<String> successCriteria;

    @Schema(description = "Related compliance gaps")
    private List<String> relatedGaps;

    @Schema(description = "Recommendation status", example = "PENDING")
    private String status;

    @Schema(description = "Created date")
    private LocalDateTime createdAt;

    @Schema(description = "Target implementation date")
    private LocalDateTime targetDate;

    @Schema(description = "Assigned to")
    private String assignedTo;

    @Schema(description = "Additional recommendation metadata")
    private Map<String, Object> metadata;

    /**
     * Check if recommendation is high priority
     */
    public boolean isHighPriority() {
        return "HIGH".equalsIgnoreCase(priority) || "CRITICAL".equalsIgnoreCase(priority);
    }

    /**
     * Check if recommendation is pending
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status) || "NEW".equalsIgnoreCase(status);
    }

    /**
     * Check if recommendation is overdue
     */
    public boolean isOverdue() {
        return targetDate != null && LocalDateTime.now().isAfter(targetDate);
    }

    /**
     * Get recommendation summary
     */
    public String getRecommendationSummary() {
        return String.format("%s: %s (%s priority)",
                           recommendationId, title, priority);
    }

    /**
     * Get implementation complexity score
     */
    public int getComplexityScore() {
        int score = 0;

        // Effort scoring
        if ("HIGH".equalsIgnoreCase(estimatedEffort)) {
            score += 30;
        } else if ("MEDIUM".equalsIgnoreCase(estimatedEffort)) {
            score += 15;
        } else if ("LOW".equalsIgnoreCase(estimatedEffort)) {
            score += 5;
        }

        // Cost scoring
        if ("HIGH".equalsIgnoreCase(estimatedCost)) {
            score += 25;
        } else if ("MEDIUM".equalsIgnoreCase(estimatedCost)) {
            score += 10;
        }

        // Resource requirements
        if (requiredResources != null) {
            score += Math.min(requiredResources.size() * 3, 15);
        }

        return score;
    }

    /**
     * Get benefit score
     */
    public int getBenefitScore() {
        int score = 0;

        // Priority scoring
        if ("CRITICAL".equalsIgnoreCase(priority)) {
            score += 50;
        } else if ("HIGH".equalsIgnoreCase(priority)) {
            score += 30;
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            score += 15;
        }

        // Benefits count
        if (expectedBenefits != null) {
            score += Math.min(expectedBenefits.size() * 5, 25);
        }

        // Risk mitigation value
        if (riskMitigation != null) {
            score += Math.min(riskMitigation.size() * 3, 15);
        }

        return score;
    }

    /**
     * Get ROI indicator (benefit vs complexity)
     */
    public String getRoiIndicator() {
        int benefit = getBenefitScore();
        int complexity = getComplexityScore();

        if (complexity == 0) {
            return "HIGH";
        }

        double ratio = (double) benefit / complexity;

        if (ratio >= 2.0) {
            return "HIGH";
        } else if (ratio >= 1.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}