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
 * DTO for compliance gaps
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compliance gap")
public class ComplianceGap {

    @Schema(description = "Gap identifier", example = "FHIR-001")
    private String gapId;

    @Schema(description = "Compliance standard", example = "FHIR")
    private String standard;

    @Schema(description = "Gap category", example = "STRUCTURE_VALIDATION")
    private String category;

    @Schema(description = "Gap severity", example = "HIGH")
    private String severity;

    @Schema(description = "Gap title", example = "Missing required FHIR elements")
    private String title;

    @Schema(description = "Gap description")
    private String description;

    @Schema(description = "Affected entities or systems")
    private List<String> affectedEntities;

    @Schema(description = "Gap impact assessment")
    private String impact;

    @Schema(description = "Risk level", example = "MEDIUM")
    private String riskLevel;

    @Schema(description = "Frequency of occurrence", example = "15")
    private Integer frequency;

    @Schema(description = "First detected date")
    private LocalDateTime firstDetected;

    @Schema(description = "Last detected date")
    private LocalDateTime lastDetected;

    @Schema(description = "Gap status", example = "OPEN")
    private String status;

    @Schema(description = "Remediation effort estimate", example = "MEDIUM")
    private String effortEstimate;

    @Schema(description = "Target resolution date")
    private LocalDateTime targetResolution;

    @Schema(description = "Assigned to")
    private String assignedTo;

    @Schema(description = "Related compliance requirements")
    private List<String> relatedRequirements;

    @Schema(description = "Additional gap metadata")
    private Map<String, Object> metadata;

    /**
     * Check if gap is critical
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(severity) || "HIGH".equalsIgnoreCase(severity);
    }

    /**
     * Check if gap is overdue
     */
    public boolean isOverdue() {
        return targetResolution != null && LocalDateTime.now().isAfter(targetResolution);
    }

    /**
     * Check if gap is open
     */
    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status);
    }

    /**
     * Get gap age in days
     */
    public long getAgeInDays() {
        if (firstDetected == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(firstDetected, LocalDateTime.now());
    }

    /**
     * Get gap summary
     */
    public String getGapSummary() {
        return String.format("%s: %s (%s severity, %s risk)",
                           gapId, title, severity, riskLevel);
    }

    /**
     * Get priority score (higher = more urgent)
     */
    public int getPriorityScore() {
        int score = 0;

        // Severity scoring
        if ("CRITICAL".equalsIgnoreCase(severity)) {
            score += 50;
        } else if ("HIGH".equalsIgnoreCase(severity)) {
            score += 30;
        } else if ("MEDIUM".equalsIgnoreCase(severity)) {
            score += 15;
        }

        // Risk level scoring
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            score += 25;
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            score += 10;
        }

        // Frequency scoring
        if (frequency != null) {
            score += Math.min(frequency, 20); // Cap at 20 points
        }

        // Overdue penalty
        if (isOverdue()) {
            score += 30;
        }

        return score;
    }
}