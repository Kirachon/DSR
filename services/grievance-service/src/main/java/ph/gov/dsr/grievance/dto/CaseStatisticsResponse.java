package ph.gov.dsr.grievance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for Case Statistics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseStatisticsResponse {

    private Long totalCases;
    private Long openCases;
    private Long resolvedCases;
    private Long overdueCase;
    private Long urgentCases;
    private Long escalatedCases;
    private Double averageResolutionTime;
    private Double satisfactionScore;
    private LocalDateTime lastUpdated;
    
    // Statistics by category
    private Map<String, Long> casesByCategory;
    
    // Statistics by status
    private Map<String, Long> casesByStatus;
    
    // Statistics by priority
    private Map<String, Long> casesByPriority;
    
    // Statistics by channel
    private Map<String, Long> casesByChannel;
    
    // Monthly trends
    private Map<String, Long> monthlyTrends;
    
    // Resolution time by category
    private Map<String, Double> resolutionTimeByCategory;
    
    // Staff workload
    private Map<String, Long> staffWorkload;
    
    // Regional distribution
    private Map<String, Long> regionalDistribution;
}
