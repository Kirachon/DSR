package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for code quality metrics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeQualityMetrics {

    private String projectKey;
    private String projectName;
    private String version;
    private Integer linesOfCode;
    private Integer statements;
    private Integer functions;
    private Integer classes;
    private Integer files;
    private Integer directories;
    private Double coverage;
    private Double lineCoverage;
    private Double branchCoverage;
    private Integer uncoveredLines;
    private Integer uncoveredConditions;
    private Integer duplicatedLines;
    private Double duplicatedLinesDensity;
    private Integer duplicatedBlocks;
    private Integer duplicatedFiles;
    private Integer bugs;
    private Integer vulnerabilities;
    private Integer codeSmells;
    private Integer securityHotspots;
    private String reliabilityRating;
    private String securityRating;
    private String maintainabilityRating;
    private String securityReviewRating;
    private Integer technicalDebt;
    private String technicalDebtRatio;
    private Integer sqaleIndex;
    private String sqaleRating;
    private Integer complexityPerFunction;
    private Integer cognitiveComplexity;
    private Integer cyclomaticComplexity;
    private String qualityGateStatus;
    private String alertStatus;
    private String lastAnalysisDate;
    private String language;
    private String languageDistribution;
    
    /**
     * Check if quality gate passed
     */
    public boolean isQualityGatePassed() {
        return "OK".equals(qualityGateStatus) || "PASSED".equals(qualityGateStatus);
    }
    
    /**
     * Check if there are security issues
     */
    public boolean hasSecurityIssues() {
        return (vulnerabilities != null && vulnerabilities > 0) || 
               (securityHotspots != null && securityHotspots > 0);
    }
    
    /**
     * Check if there are reliability issues
     */
    public boolean hasReliabilityIssues() {
        return bugs != null && bugs > 0;
    }
    
    /**
     * Check if there are maintainability issues
     */
    public boolean hasMaintainabilityIssues() {
        return codeSmells != null && codeSmells > 0;
    }
    
    /**
     * Get overall quality score (0-100)
     */
    public double getOverallQualityScore() {
        double score = 100.0;
        
        // Deduct points for issues
        if (bugs != null) score -= Math.min(bugs * 2, 20);
        if (vulnerabilities != null) score -= Math.min(vulnerabilities * 5, 30);
        if (codeSmells != null) score -= Math.min(codeSmells * 0.5, 20);
        if (securityHotspots != null) score -= Math.min(securityHotspots * 3, 15);
        
        // Deduct points for low coverage
        if (coverage != null && coverage < 80) {
            score -= (80 - coverage) * 0.5;
        }
        
        // Deduct points for high duplication
        if (duplicatedLinesDensity != null && duplicatedLinesDensity > 5) {
            score -= (duplicatedLinesDensity - 5) * 2;
        }
        
        return Math.max(score, 0.0);
    }
}
