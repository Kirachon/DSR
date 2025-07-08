package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for compliance statistics
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compliance statistics")
public class ComplianceStatistics {

    @Schema(description = "Compliance standard", example = "FHIR")
    private String standard;

    @Schema(description = "Total number of compliance checks", example = "150")
    private Integer totalChecks;

    @Schema(description = "Number of compliant checks", example = "135")
    private Integer compliantChecks;

    @Schema(description = "Number of non-compliant checks", example = "15")
    private Integer nonCompliantChecks;

    @Schema(description = "Overall compliance rate (0-100)", example = "90.0")
    private Double complianceRate;

    @Schema(description = "Number of critical issues", example = "3")
    private Integer criticalIssues;

    @Schema(description = "Number of warnings", example = "12")
    private Integer warnings;

    @Schema(description = "Average compliance score", example = "87.5")
    private Double averageScore;

    @Schema(description = "Highest compliance score", example = "98.5")
    private Double highestScore;

    @Schema(description = "Lowest compliance score", example = "45.2")
    private Double lowestScore;

    @Schema(description = "Compliance trend", example = "IMPROVING")
    private String trend;

    @Schema(description = "Period start date")
    private LocalDateTime periodStart;

    @Schema(description = "Period end date")
    private LocalDateTime periodEnd;

    @Schema(description = "Statistics by entity or system")
    private Map<String, Object> entityStatistics;

    @Schema(description = "Statistics by compliance level")
    private Map<String, Integer> complianceLevelBreakdown;

    @Schema(description = "Statistics by risk level")
    private Map<String, Integer> riskLevelBreakdown;

    @Schema(description = "Monthly compliance trends")
    private Map<String, Double> monthlyTrends;

    @Schema(description = "Additional statistical metadata")
    private Map<String, Object> metadata;

    /**
     * Calculate compliance rate
     */
    public Double calculateComplianceRate() {
        if (totalChecks == null || totalChecks == 0) {
            return 0.0;
        }

        if (compliantChecks == null) {
            return 0.0;
        }

        return (compliantChecks.doubleValue() / totalChecks.doubleValue()) * 100.0;
    }

    /**
     * Get compliance rate percentage
     */
    public Double getComplianceRate() {
        if (complianceRate != null) {
            return complianceRate;
        }

        return calculateComplianceRate();
    }

    /**
     * Check if compliance rate is acceptable (>= 80%)
     */
    public boolean isComplianceAcceptable() {
        return getComplianceRate() >= 80.0;
    }

    /**
     * Check if compliance rate is good (>= 90%)
     */
    public boolean isComplianceGood() {
        return getComplianceRate() >= 90.0;
    }

    /**
     * Check if compliance rate is excellent (>= 95%)
     */
    public boolean isComplianceExcellent() {
        return getComplianceRate() >= 95.0;
    }

    /**
     * Get compliance level based on rate
     */
    public String getComplianceLevel() {
        double rate = getComplianceRate();

        if (rate >= 95.0) {
            return "EXCELLENT";
        } else if (rate >= 90.0) {
            return "GOOD";
        } else if (rate >= 80.0) {
            return "ACCEPTABLE";
        } else if (rate >= 60.0) {
            return "NEEDS_IMPROVEMENT";
        } else {
            return "POOR";
        }
    }

    /**
     * Check if there are critical issues
     */
    public boolean hasCriticalIssues() {
        return criticalIssues != null && criticalIssues > 0;
    }

    /**
     * Check if there are warnings
     */
    public boolean hasWarnings() {
        return warnings != null && warnings > 0;
    }

    /**
     * Get issue summary
     */
    public String getIssueSummary() {
        int critical = criticalIssues != null ? criticalIssues : 0;
        int warn = warnings != null ? warnings : 0;

        if (critical > 0 && warn > 0) {
            return String.format("%d critical issues, %d warnings", critical, warn);
        } else if (critical > 0) {
            return String.format("%d critical issues", critical);
        } else if (warn > 0) {
            return String.format("%d warnings", warn);
        } else {
            return "No issues";
        }
    }

    /**
     * Get statistics summary
     */
    public String getStatisticsSummary() {
        return String.format("%s: %.1f%% compliance (%d/%d checks) - %s",
                           standard, getComplianceRate(),
                           compliantChecks != null ? compliantChecks : 0,
                           totalChecks != null ? totalChecks : 0,
                           getComplianceLevel());
    }

    /**
     * Get trend direction
     */
    public String getTrendDirection() {
        if (trend != null) {
            return trend;
        }

        // Could be calculated based on monthly trends
        if (monthlyTrends != null && monthlyTrends.size() >= 2) {
            // Simple trend calculation based on first and last values
            Double[] values = monthlyTrends.values().toArray(new Double[0]);
            if (values.length >= 2) {
                double first = values[0];
                double last = values[values.length - 1];

                if (last > first + 5.0) {
                    return "IMPROVING";
                } else if (last < first - 5.0) {
                    return "DECLINING";
                } else {
                    return "STABLE";
                }
            }
        }

        return "UNKNOWN";
    }

    /**
     * Check if trend is positive
     */
    public boolean isTrendPositive() {
        return "IMPROVING".equals(getTrendDirection());
    }

    /**
     * Check if trend is negative
     */
    public boolean isTrendNegative() {
        return "DECLINING".equals(getTrendDirection());
    }

    /**
     * Get score range
     */
    public Double getScoreRange() {
        if (highestScore != null && lowestScore != null) {
            return highestScore - lowestScore;
        }
        return null;
    }

    /**
     * Get score variance indicator
     */
    public String getScoreVariance() {
        Double range = getScoreRange();
        if (range == null) {
            return "UNKNOWN";
        }

        if (range <= 10.0) {
            return "LOW";
        } else if (range <= 25.0) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
}