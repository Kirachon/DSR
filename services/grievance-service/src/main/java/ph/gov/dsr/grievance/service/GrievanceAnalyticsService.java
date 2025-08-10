package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Grievance Analytics Service
 * Provides comprehensive performance analytics and reporting for grievance management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrievanceAnalyticsService {

    private final GrievanceCaseRepository caseRepository;
    private final AdvancedSLAMonitoringService slaMonitoringService;
    private final EscalationWorkflowEngine escalationEngine;

    /**
     * Generate comprehensive performance dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generatePerformanceDashboard(String timeRange) {
        log.info("Generating performance dashboard for timeRange: {}", timeRange);

        LocalDateTime[] dateRange = parseDateRange(timeRange);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        Map<String, Object> dashboard = new HashMap<>();

        try {
            // Core metrics
            dashboard.put("coreMetrics", generateCoreMetrics(startDate, endDate));
            
            // SLA performance
            dashboard.put("slaPerformance", generateSLAPerformance(startDate, endDate));
            
            // Category analysis
            dashboard.put("categoryAnalysis", generateCategoryAnalysis(startDate, endDate));
            
            // Resolution trends
            dashboard.put("resolutionTrends", generateResolutionTrends(startDate, endDate));
            
            // Staff performance
            dashboard.put("staffPerformance", generateStaffPerformance(startDate, endDate));
            
            // Escalation analytics
            dashboard.put("escalationAnalytics", generateEscalationAnalytics(startDate, endDate));
            
            // Satisfaction metrics
            dashboard.put("satisfactionMetrics", generateSatisfactionMetrics(startDate, endDate));
            
            // Predictive insights
            dashboard.put("predictiveInsights", generatePredictiveInsights(startDate, endDate));

            dashboard.put("generatedAt", LocalDateTime.now());
            dashboard.put("timeRange", timeRange);

        } catch (Exception e) {
            log.error("Error generating performance dashboard: {}", e.getMessage(), e);
            dashboard.put("error", "Failed to generate dashboard: " + e.getMessage());
        }

        return dashboard;
    }

    /**
     * Generate core performance metrics
     */
    private Map<String, Object> generateCoreMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("totalCases", cases.size());
        metrics.put("resolvedCases", cases.stream().filter(c -> c.getResolutionDate() != null).count());
        metrics.put("pendingCases", cases.stream().filter(c -> c.getResolutionDate() == null).count());
        metrics.put("escalatedCases", cases.stream().filter(c -> c.getEscalationLevel() > 0).count());
        
        // Resolution rate
        double resolutionRate = cases.isEmpty() ? 0.0 : 
            (double) cases.stream().filter(c -> c.getResolutionDate() != null).count() / cases.size();
        metrics.put("resolutionRate", resolutionRate);
        
        // Average resolution time
        double avgResolutionTime = cases.stream()
            .filter(c -> c.getResolutionDate() != null)
            .mapToLong(c -> ChronoUnit.HOURS.between(c.getSubmissionDate(), c.getResolutionDate()))
            .average()
            .orElse(0.0);
        metrics.put("averageResolutionTimeHours", avgResolutionTime);
        
        return metrics;
    }

    /**
     * Generate SLA performance metrics
     */
    private Map<String, Object> generateSLAPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> slaMetrics = new HashMap<>();
        
        // SLA compliance by priority
        Map<GrievanceCase.Priority, Double> complianceByPriority = new HashMap<>();
        for (GrievanceCase.Priority priority : GrievanceCase.Priority.values()) {
            List<GrievanceCase> priorityCases = cases.stream()
                .filter(c -> c.getPriority() == priority)
                .collect(Collectors.toList());
            
            if (!priorityCases.isEmpty()) {
                long compliantCases = priorityCases.stream()
                    .filter(this::isSLACompliant)
                    .count();
                double compliance = (double) compliantCases / priorityCases.size();
                complianceByPriority.put(priority, compliance);
            }
        }
        slaMetrics.put("complianceByPriority", complianceByPriority);
        
        // Overall SLA compliance
        long totalCompliant = cases.stream().filter(this::isSLACompliant).count();
        double overallCompliance = cases.isEmpty() ? 0.0 : (double) totalCompliant / cases.size();
        slaMetrics.put("overallCompliance", overallCompliance);
        
        // SLA breach analysis
        List<GrievanceCase> breachedCases = cases.stream()
            .filter(c -> !isSLACompliant(c))
            .collect(Collectors.toList());
        slaMetrics.put("breachedCases", breachedCases.size());
        slaMetrics.put("breachRate", cases.isEmpty() ? 0.0 : (double) breachedCases.size() / cases.size());
        
        return slaMetrics;
    }

    /**
     * Generate category analysis
     */
    private Map<String, Object> generateCategoryAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> categoryAnalysis = new HashMap<>();
        
        // Cases by category
        Map<String, Long> casesByCategory = cases.stream()
            .collect(Collectors.groupingBy(
                c -> c.getCategory().name(),
                Collectors.counting()));
        categoryAnalysis.put("casesByCategory", casesByCategory);
        
        // Resolution time by category
        Map<String, Double> resolutionTimeByCategory = new HashMap<>();
        for (GrievanceCase.GrievanceCategory category : GrievanceCase.GrievanceCategory.values()) {
            double avgTime = cases.stream()
                .filter(c -> c.getCategory() == category && c.getResolutionDate() != null)
                .mapToLong(c -> ChronoUnit.HOURS.between(c.getSubmissionDate(), c.getResolutionDate()))
                .average()
                .orElse(0.0);
            resolutionTimeByCategory.put(category.name(), avgTime);
        }
        categoryAnalysis.put("resolutionTimeByCategory", resolutionTimeByCategory);
        
        // Most problematic categories
        List<String> problematicCategories = casesByCategory.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        categoryAnalysis.put("mostProblematicCategories", problematicCategories);
        
        return categoryAnalysis;
    }

    /**
     * Generate resolution trends
     */
    private Map<String, Object> generateResolutionTrends(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> trends = new HashMap<>();
        
        // Daily resolution counts
        Map<String, Long> dailyResolutions = cases.stream()
            .filter(c -> c.getResolutionDate() != null)
            .collect(Collectors.groupingBy(
                c -> c.getResolutionDate().toLocalDate().toString(),
                Collectors.counting()));
        trends.put("dailyResolutions", dailyResolutions);
        
        // Weekly trends
        Map<String, Long> weeklySubmissions = cases.stream()
            .collect(Collectors.groupingBy(
                c -> getWeekOfYear(c.getSubmissionDate()),
                Collectors.counting()));
        trends.put("weeklySubmissions", weeklySubmissions);
        
        // Resolution velocity (cases resolved per day)
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long resolvedCases = cases.stream().filter(c -> c.getResolutionDate() != null).count();
        double velocity = totalDays > 0 ? (double) resolvedCases / totalDays : 0.0;
        trends.put("resolutionVelocity", velocity);
        
        return trends;
    }

    /**
     * Generate staff performance metrics
     */
    private Map<String, Object> generateStaffPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> staffMetrics = new HashMap<>();
        
        // Cases by assignee
        Map<String, Long> casesByAssignee = cases.stream()
            .filter(c -> c.getAssignedTo() != null)
            .collect(Collectors.groupingBy(
                GrievanceCase::getAssignedTo,
                Collectors.counting()));
        staffMetrics.put("casesByAssignee", casesByAssignee);
        
        // Resolution rate by assignee
        Map<String, Double> resolutionRateByAssignee = new HashMap<>();
        for (String assignee : casesByAssignee.keySet()) {
            List<GrievanceCase> assigneeCases = cases.stream()
                .filter(c -> assignee.equals(c.getAssignedTo()))
                .collect(Collectors.toList());
            
            long resolved = assigneeCases.stream().filter(c -> c.getResolutionDate() != null).count();
            double rate = assigneeCases.isEmpty() ? 0.0 : (double) resolved / assigneeCases.size();
            resolutionRateByAssignee.put(assignee, rate);
        }
        staffMetrics.put("resolutionRateByAssignee", resolutionRateByAssignee);
        
        // Average resolution time by assignee
        Map<String, Double> avgResolutionTimeByAssignee = new HashMap<>();
        for (String assignee : casesByAssignee.keySet()) {
            double avgTime = cases.stream()
                .filter(c -> assignee.equals(c.getAssignedTo()) && c.getResolutionDate() != null)
                .mapToLong(c -> ChronoUnit.HOURS.between(c.getSubmissionDate(), c.getResolutionDate()))
                .average()
                .orElse(0.0);
            avgResolutionTimeByAssignee.put(assignee, avgTime);
        }
        staffMetrics.put("avgResolutionTimeByAssignee", avgResolutionTimeByAssignee);
        
        return staffMetrics;
    }

    /**
     * Generate escalation analytics
     */
    private Map<String, Object> generateEscalationAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        return escalationEngine.getEscalationAnalytics();
    }

    /**
     * Generate satisfaction metrics
     */
    private Map<String, Object> generateSatisfactionMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> satisfactionMetrics = new HashMap<>();
        
        // Cases with satisfaction feedback
        List<GrievanceCase> casesWithFeedback = cases.stream()
            .filter(c -> c.getComplainantSatisfaction() != null)
            .collect(Collectors.toList());
        
        if (!casesWithFeedback.isEmpty()) {
            // Satisfaction distribution
            Map<String, Long> satisfactionDistribution = casesWithFeedback.stream()
                .collect(Collectors.groupingBy(
                    GrievanceCase::getComplainantSatisfaction,
                    Collectors.counting()));
            satisfactionMetrics.put("satisfactionDistribution", satisfactionDistribution);
            
            // Average satisfaction score
            double avgSatisfaction = calculateAverageSatisfactionScore(casesWithFeedback);
            satisfactionMetrics.put("averageSatisfactionScore", avgSatisfaction);
            
            // Satisfaction rate (satisfied + very satisfied)
            long satisfiedCount = casesWithFeedback.stream()
                .filter(c -> c.getComplainantSatisfaction().contains("SATISFIED"))
                .count();
            double satisfactionRate = (double) satisfiedCount / casesWithFeedback.size();
            satisfactionMetrics.put("satisfactionRate", satisfactionRate);
        }
        
        satisfactionMetrics.put("feedbackResponseRate", 
            cases.isEmpty() ? 0.0 : (double) casesWithFeedback.size() / cases.size());
        
        return satisfactionMetrics;
    }

    /**
     * Generate predictive insights
     */
    private Map<String, Object> generatePredictiveInsights(LocalDateTime startDate, LocalDateTime endDate) {
        List<GrievanceCase> cases = caseRepository.findBySubmissionDateBetween(startDate, endDate);
        
        Map<String, Object> insights = new HashMap<>();
        
        // Predict next week's case volume
        long avgWeeklyVolume = cases.size() / Math.max(1, ChronoUnit.WEEKS.between(startDate, endDate));
        insights.put("predictedNextWeekVolume", avgWeeklyVolume);
        
        // Identify categories likely to increase
        Map<String, Double> categoryTrends = analyzeCategoryTrends(cases);
        insights.put("categoryTrends", categoryTrends);
        
        // SLA risk assessment
        List<GrievanceCase> activeCases = caseRepository.findByStatusIn(Arrays.asList(
            GrievanceCase.CaseStatus.SUBMITTED,
            GrievanceCase.CaseStatus.UNDER_REVIEW,
            GrievanceCase.CaseStatus.INVESTIGATING
        ));
        
        long casesAtRisk = activeCases.stream()
            .filter(c -> {
                var slaStatus = slaMonitoringService.calculateSLAStatus(c);
                return slaStatus.getPercentageElapsed() > 0.7;
            })
            .count();
        insights.put("casesAtSLARisk", casesAtRisk);
        
        return insights;
    }

    /**
     * Parse date range string into start and end dates
     */
    private LocalDateTime[] parseDateRange(String timeRange) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (timeRange.toLowerCase()) {
            case "7d":
            case "week":
                startDate = endDate.minusDays(7);
                break;
            case "30d":
            case "month":
                startDate = endDate.minusDays(30);
                break;
            case "90d":
            case "quarter":
                startDate = endDate.minusDays(90);
                break;
            case "365d":
            case "year":
                startDate = endDate.minusDays(365);
                break;
            default:
                startDate = endDate.minusDays(30); // Default to 30 days
        }
        
        return new LocalDateTime[]{startDate, endDate};
    }

    /**
     * Check if case is SLA compliant
     */
    private boolean isSLACompliant(GrievanceCase grievanceCase) {
        if (grievanceCase.getResolutionDate() == null || grievanceCase.getResolutionTargetDate() == null) {
            return false; // Unresolved or no target date
        }
        return !grievanceCase.getResolutionDate().isAfter(grievanceCase.getResolutionTargetDate());
    }

    /**
     * Get week of year for grouping
     */
    private String getWeekOfYear(LocalDateTime date) {
        return String.format("%d-W%02d", date.getYear(), date.getDayOfYear() / 7 + 1);
    }

    /**
     * Calculate average satisfaction score
     */
    private double calculateAverageSatisfactionScore(List<GrievanceCase> cases) {
        Map<String, Integer> scoreMap = Map.of(
            "VERY_SATISFIED", 5,
            "SATISFIED", 4,
            "NEUTRAL", 3,
            "DISSATISFIED", 2,
            "VERY_DISSATISFIED", 1
        );
        
        return cases.stream()
            .mapToInt(c -> scoreMap.getOrDefault(c.getComplainantSatisfaction(), 3))
            .average()
            .orElse(3.0);
    }

    /**
     * Analyze category trends for predictive insights
     */
    private Map<String, Double> analyzeCategoryTrends(List<GrievanceCase> cases) {
        // Simplified trend analysis - compare first half vs second half of period
        int midPoint = cases.size() / 2;
        List<GrievanceCase> firstHalf = cases.subList(0, midPoint);
        List<GrievanceCase> secondHalf = cases.subList(midPoint, cases.size());
        
        Map<String, Double> trends = new HashMap<>();
        
        for (GrievanceCase.GrievanceCategory category : GrievanceCase.GrievanceCategory.values()) {
            long firstHalfCount = firstHalf.stream().filter(c -> c.getCategory() == category).count();
            long secondHalfCount = secondHalf.stream().filter(c -> c.getCategory() == category).count();
            
            double trend = firstHalfCount == 0 ? 0.0 : 
                ((double) secondHalfCount - firstHalfCount) / firstHalfCount;
            trends.put(category.name(), trend);
        }
        
        return trends;
    }

    /**
     * Generate real-time analytics summary
     */
    public Map<String, Object> getRealTimeAnalytics() {
        Map<String, Object> realTime = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.toLocalDate().atStartOfDay();
        
        // Today's metrics
        List<GrievanceCase> todayCases = caseRepository.findBySubmissionDateAfter(today);
        realTime.put("todaySubmissions", todayCases.size());
        realTime.put("todayResolutions", todayCases.stream()
            .filter(c -> c.getResolutionDate() != null && c.getResolutionDate().isAfter(today))
            .count());
        
        // Active cases requiring attention
        List<GrievanceCase> urgentCases = caseRepository.findByPriorityAndStatusIn(
            GrievanceCase.Priority.CRITICAL,
            Arrays.asList(GrievanceCase.CaseStatus.SUBMITTED, GrievanceCase.CaseStatus.UNDER_REVIEW)
        );
        realTime.put("urgentCasesCount", urgentCases.size());
        
        // SLA alerts
        List<GrievanceCase> activeCases = caseRepository.findByStatusIn(Arrays.asList(
            GrievanceCase.CaseStatus.SUBMITTED,
            GrievanceCase.CaseStatus.UNDER_REVIEW,
            GrievanceCase.CaseStatus.INVESTIGATING
        ));
        
        long slaWarnings = activeCases.stream()
            .filter(c -> {
                var slaStatus = slaMonitoringService.calculateSLAStatus(c);
                return slaStatus.getPercentageElapsed() > 0.7;
            })
            .count();
        realTime.put("slaWarnings", slaWarnings);
        
        realTime.put("lastUpdated", now);
        
        return realTime;
    }
}
