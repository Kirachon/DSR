package ph.gov.dsr.interoperability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for compliance report requests
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compliance report request")
public class ComplianceReportRequest {

    @NotNull(message = "Start date is required")
    @Schema(description = "Report period start date", required = true)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Schema(description = "Report period end date", required = true)
    private LocalDate endDate;

    @Schema(description = "Compliance standards to include in report")
    private List<String> standards;

    @Schema(description = "Report format", example = "DETAILED")
    @Builder.Default
    private String reportFormat = "DETAILED";

    @Schema(description = "Include compliance statistics", example = "true")
    @Builder.Default
    private Boolean includeStatistics = true;

    @Schema(description = "Include compliance gaps analysis", example = "true")
    @Builder.Default
    private Boolean includeGapsAnalysis = true;

    @Schema(description = "Include recommendations", example = "true")
    @Builder.Default
    private Boolean includeRecommendations = true;

    @Schema(description = "Include trend analysis", example = "false")
    @Builder.Default
    private Boolean includeTrendAnalysis = false;

    @Schema(description = "Filter by compliance level")
    private String complianceLevel;

    @Schema(description = "Filter by risk level")
    private String riskLevel;

    @Schema(description = "Filter by entity or system")
    private String entityFilter;

    @Schema(description = "Group results by field", example = "STANDARD")
    private String groupBy;

    @Schema(description = "Sort results by field", example = "DATE")
    @Builder.Default
    private String sortBy = "DATE";

    @Schema(description = "Sort order", example = "DESC")
    @Builder.Default
    private String sortOrder = "DESC";

    @Schema(description = "Maximum number of records to include", example = "1000")
    @Builder.Default
    private Integer maxRecords = 1000;

    @Schema(description = "Additional report parameters")
    private Map<String, Object> parameters;

    @Schema(description = "Report requester information")
    private String requestedBy;

    @Schema(description = "Report purpose or context")
    private String reportPurpose;

    /**
     * Check if statistics are included
     */
    public boolean isStatisticsIncluded() {
        return includeStatistics != null && includeStatistics;
    }

    /**
     * Check if gaps analysis is included
     */
    public boolean isGapsAnalysisIncluded() {
        return includeGapsAnalysis != null && includeGapsAnalysis;
    }

    /**
     * Check if recommendations are included
     */
    public boolean isRecommendationsIncluded() {
        return includeRecommendations != null && includeRecommendations;
    }

    /**
     * Check if trend analysis is included
     */
    public boolean isTrendAnalysisIncluded() {
        return includeTrendAnalysis != null && includeTrendAnalysis;
    }

    /**
     * Check if specific standards are requested
     */
    public boolean hasStandardsFilter() {
        return standards != null && !standards.isEmpty();
    }

    /**
     * Check if compliance level filter is applied
     */
    public boolean hasComplianceLevelFilter() {
        return complianceLevel != null && !complianceLevel.trim().isEmpty();
    }

    /**
     * Check if risk level filter is applied
     */
    public boolean hasRiskLevelFilter() {
        return riskLevel != null && !riskLevel.trim().isEmpty();
    }

    /**
     * Check if entity filter is applied
     */
    public boolean hasEntityFilter() {
        return entityFilter != null && !entityFilter.trim().isEmpty();
    }

    /**
     * Get report period in days
     */
    public long getReportPeriodDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Validate request parameters
     */
    public boolean isValid() {
        return startDate != null && endDate != null &&
               !endDate.isBefore(startDate) &&
               maxRecords != null && maxRecords > 0;
    }

    /**
     * Get request summary
     */
    public String getRequestSummary() {
        return String.format("Compliance report for %s to %s (%d days, format: %s)",
                           startDate, endDate, getReportPeriodDays(), reportFormat);
    }

    /**
     * Get included sections
     */
    public List<String> getIncludedSections() {
        List<String> sections = new java.util.ArrayList<>();

        if (isStatisticsIncluded()) {
            sections.add("STATISTICS");
        }

        if (isGapsAnalysisIncluded()) {
            sections.add("GAPS_ANALYSIS");
        }

        if (isRecommendationsIncluded()) {
            sections.add("RECOMMENDATIONS");
        }

        if (isTrendAnalysisIncluded()) {
            sections.add("TREND_ANALYSIS");
        }

        return sections;
    }

    /**
     * Get applied filters
     */
    public Map<String, String> getAppliedFilters() {
        Map<String, String> filters = new java.util.HashMap<>();

        if (hasComplianceLevelFilter()) {
            filters.put("complianceLevel", complianceLevel);
        }

        if (hasRiskLevelFilter()) {
            filters.put("riskLevel", riskLevel);
        }

        if (hasEntityFilter()) {
            filters.put("entity", entityFilter);
        }

        if (hasStandardsFilter()) {
            filters.put("standards", String.join(", ", standards));
        }

        return filters;
    }
}