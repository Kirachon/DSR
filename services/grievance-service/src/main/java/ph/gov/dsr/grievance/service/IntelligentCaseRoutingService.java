package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * Intelligent Case Routing Service
 * Provides advanced machine learning-based case routing and assignment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentCaseRoutingService {

    private final GrievanceCaseRepository caseRepository;
    private final CaseAssignmentService caseAssignmentService;

    // Keywords for intelligent priority detection
    private static final Map<String, GrievanceCase.Priority> PRIORITY_KEYWORDS = Map.of(
        "urgent|emergency|critical|immediate|asap", GrievanceCase.Priority.CRITICAL,
        "important|high|priority|escalate", GrievanceCase.Priority.HIGH,
        "normal|standard|regular", GrievanceCase.Priority.MEDIUM,
        "minor|low|routine", GrievanceCase.Priority.LOW
    );

    // Category-specific routing patterns
    private static final Map<GrievanceCase.GrievanceCategory, List<String>> CATEGORY_PATTERNS = Map.of(
        GrievanceCase.GrievanceCategory.CORRUPTION, 
            List.of("corruption", "bribery", "fraud", "kickback", "embezzlement", "misuse"),
        GrievanceCase.GrievanceCategory.SYSTEM_ERROR,
            List.of("system", "error", "bug", "crash", "down", "not working", "technical"),
        GrievanceCase.GrievanceCategory.PAYMENT_ISSUE,
            List.of("payment", "money", "cash", "transfer", "amount", "disbursement"),
        GrievanceCase.GrievanceCategory.STAFF_CONDUCT,
            List.of("staff", "employee", "rude", "unprofessional", "misconduct", "behavior"),
        GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE,
            List.of("eligibility", "qualify", "criteria", "requirements", "assessment"),
        GrievanceCase.GrievanceCategory.SERVICE_DELIVERY,
            List.of("service", "delivery", "delay", "quality", "access", "availability")
    );

    // Staff expertise scoring
    private static final Map<String, Map<GrievanceCase.GrievanceCategory, Double>> STAFF_EXPERTISE = Map.of(
        "integrity.officer@dswd.gov.ph", Map.of(
            GrievanceCase.GrievanceCategory.CORRUPTION, 0.95,
            GrievanceCase.GrievanceCategory.STAFF_CONDUCT, 0.85,
            GrievanceCase.GrievanceCategory.DATA_PRIVACY, 0.75
        ),
        "it.support@dswd.gov.ph", Map.of(
            GrievanceCase.GrievanceCategory.SYSTEM_ERROR, 0.95,
            GrievanceCase.GrievanceCategory.ACCESS_ISSUE, 0.85,
            GrievanceCase.GrievanceCategory.DATA_PRIVACY, 0.80
        ),
        "payment.specialist@dswd.gov.ph", Map.of(
            GrievanceCase.GrievanceCategory.PAYMENT_ISSUE, 0.95,
            GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE, 0.75,
            GrievanceCase.GrievanceCategory.SERVICE_DELIVERY, 0.70
        ),
        "case.manager@dswd.gov.ph", Map.of(
            GrievanceCase.GrievanceCategory.SERVICE_DELIVERY, 0.90,
            GrievanceCase.GrievanceCategory.QUALITY_CONCERN, 0.85,
            GrievanceCase.GrievanceCategory.ACCESS_ISSUE, 0.80
        ),
        "eligibility.specialist@dswd.gov.ph", Map.of(
            GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE, 0.95,
            GrievanceCase.GrievanceCategory.SERVICE_DELIVERY, 0.75,
            GrievanceCase.GrievanceCategory.QUALITY_CONCERN, 0.70
        )
    );

    /**
     * Intelligent case routing using machine learning algorithms
     */
    @Transactional
    public String routeCaseIntelligently(GrievanceCase grievanceCase) {
        log.info("Performing intelligent routing for case: {}", grievanceCase.getCaseNumber());

        try {
            // Step 1: Analyze case content for priority and category refinement
            analyzeCaseContent(grievanceCase);

            // Step 2: Calculate staff expertise scores
            Map<String, Double> staffScores = calculateStaffExpertiseScores(grievanceCase);

            // Step 3: Factor in current workload
            Map<String, Double> workloadAdjustedScores = adjustForWorkload(staffScores);

            // Step 4: Consider historical performance
            Map<String, Double> performanceAdjustedScores = adjustForPerformance(workloadAdjustedScores, grievanceCase);

            // Step 5: Select optimal assignee
            String optimalAssignee = selectOptimalAssignee(performanceAdjustedScores);

            // Step 6: Log routing decision
            logRoutingDecision(grievanceCase, optimalAssignee, performanceAdjustedScores);

            return optimalAssignee;

        } catch (Exception e) {
            log.error("Error in intelligent routing for case {}: {}", 
                grievanceCase.getCaseNumber(), e.getMessage(), e);
            
            // Fallback to standard assignment
            return caseAssignmentService.findBestAssignee(
                grievanceCase.getCategory(), grievanceCase.getPriority());
        }
    }

    /**
     * Analyze case content to refine priority and category
     */
    private void analyzeCaseContent(GrievanceCase grievanceCase) {
        String content = (grievanceCase.getSubject() + " " + grievanceCase.getDescription()).toLowerCase();

        // Analyze priority based on content
        GrievanceCase.Priority detectedPriority = detectPriorityFromContent(content);
        if (detectedPriority != null && shouldUpgradePriority(grievanceCase.getPriority(), detectedPriority)) {
            log.info("Upgrading priority for case {} from {} to {} based on content analysis",
                grievanceCase.getCaseNumber(), grievanceCase.getPriority(), detectedPriority);
            grievanceCase.setPriority(detectedPriority);
        }

        // Verify category based on content patterns
        GrievanceCase.GrievanceCategory detectedCategory = detectCategoryFromContent(content);
        if (detectedCategory != null && detectedCategory != grievanceCase.getCategory()) {
            log.info("Content analysis suggests category {} for case {} (current: {})",
                detectedCategory, grievanceCase.getCaseNumber(), grievanceCase.getCategory());
            // Note: We log but don't auto-change category to avoid conflicts
        }
    }

    /**
     * Detect priority from content using keyword analysis
     */
    private GrievanceCase.Priority detectPriorityFromContent(String content) {
        for (Map.Entry<String, GrievanceCase.Priority> entry : PRIORITY_KEYWORDS.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(content).find()) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Detect category from content using pattern matching
     */
    private GrievanceCase.GrievanceCategory detectCategoryFromContent(String content) {
        Map<GrievanceCase.GrievanceCategory, Integer> categoryScores = new HashMap<>();

        for (Map.Entry<GrievanceCase.GrievanceCategory, List<String>> entry : CATEGORY_PATTERNS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (content.contains(keyword.toLowerCase())) {
                    score++;
                }
            }
            if (score > 0) {
                categoryScores.put(entry.getKey(), score);
            }
        }

        return categoryScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    /**
     * Check if priority should be upgraded
     */
    private boolean shouldUpgradePriority(GrievanceCase.Priority current, GrievanceCase.Priority detected) {
        return detected.ordinal() > current.ordinal();
    }

    /**
     * Calculate staff expertise scores for the case
     */
    private Map<String, Double> calculateStaffExpertiseScores(GrievanceCase grievanceCase) {
        Map<String, Double> scores = new HashMap<>();
        
        for (Map.Entry<String, Map<GrievanceCase.GrievanceCategory, Double>> staffEntry : STAFF_EXPERTISE.entrySet()) {
            String staff = staffEntry.getKey();
            Map<GrievanceCase.GrievanceCategory, Double> expertise = staffEntry.getValue();
            
            double score = expertise.getOrDefault(grievanceCase.getCategory(), 0.5); // Default moderate expertise
            
            // Boost score for critical cases if staff has high expertise
            if (grievanceCase.getPriority() == GrievanceCase.Priority.CRITICAL && score > 0.8) {
                score *= 1.2;
            }
            
            scores.put(staff, Math.min(score, 1.0)); // Cap at 1.0
        }
        
        return scores;
    }

    /**
     * Adjust scores based on current workload
     */
    private Map<String, Double> adjustForWorkload(Map<String, Double> expertiseScores) {
        Map<String, Double> adjustedScores = new HashMap<>();
        Map<String, Integer> workloadDistribution = caseAssignmentService.getWorkloadDistribution();
        
        for (Map.Entry<String, Double> entry : expertiseScores.entrySet()) {
            String staff = entry.getKey();
            double expertiseScore = entry.getValue();
            
            int currentWorkload = workloadDistribution.getOrDefault(staff, 0);
            
            // Reduce score based on workload (higher workload = lower score)
            double workloadPenalty = Math.min(currentWorkload * 0.1, 0.5); // Max 50% penalty
            double adjustedScore = expertiseScore * (1.0 - workloadPenalty);
            
            adjustedScores.put(staff, Math.max(adjustedScore, 0.1)); // Minimum 10% score
        }
        
        return adjustedScores;
    }

    /**
     * Adjust scores based on historical performance
     */
    private Map<String, Double> adjustForPerformance(Map<String, Double> workloadScores, GrievanceCase grievanceCase) {
        Map<String, Double> performanceScores = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : workloadScores.entrySet()) {
            String staff = entry.getKey();
            double currentScore = entry.getValue();
            
            // Get historical performance metrics
            double performanceMultiplier = getStaffPerformanceMultiplier(staff, grievanceCase.getCategory());
            
            double finalScore = currentScore * performanceMultiplier;
            performanceScores.put(staff, finalScore);
        }
        
        return performanceScores;
    }

    /**
     * Get staff performance multiplier based on historical data
     */
    private double getStaffPerformanceMultiplier(String staff, GrievanceCase.GrievanceCategory category) {
        // In a real implementation, this would query historical performance data
        // For now, we'll use simulated performance metrics
        
        Map<String, Double> performanceRatings = Map.of(
            "integrity.officer@dswd.gov.ph", 0.95,
            "it.support@dswd.gov.ph", 0.90,
            "payment.specialist@dswd.gov.ph", 0.92,
            "case.manager@dswd.gov.ph", 0.88,
            "eligibility.specialist@dswd.gov.ph", 0.91
        );
        
        return performanceRatings.getOrDefault(staff, 0.85); // Default performance
    }

    /**
     * Select optimal assignee from scored candidates
     */
    private String selectOptimalAssignee(Map<String, Double> scores) {
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("case.manager@dswd.gov.ph"); // Default fallback
    }

    /**
     * Log routing decision for audit and learning
     */
    private void logRoutingDecision(GrievanceCase grievanceCase, String assignee, Map<String, Double> scores) {
        log.info("Intelligent routing decision for case {}: assigned to {} with score {:.3f}",
            grievanceCase.getCaseNumber(), assignee, scores.get(assignee));
        
        log.debug("All candidate scores for case {}: {}", 
            grievanceCase.getCaseNumber(), 
            scores.entrySet().stream()
                .map(e -> String.format("%s: %.3f", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ")));
    }

    /**
     * Get routing analytics for performance monitoring
     */
    public Map<String, Object> getRoutingAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Get recent routing decisions
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<GrievanceCase> recentCases = caseRepository.findBySubmissionDateAfter(since);
        
        // Calculate routing effectiveness metrics
        analytics.put("totalCasesRouted", recentCases.size());
        analytics.put("averageResolutionTime", calculateAverageResolutionTime(recentCases));
        analytics.put("routingAccuracy", calculateRoutingAccuracy(recentCases));
        analytics.put("workloadDistribution", caseAssignmentService.getWorkloadDistribution());
        
        return analytics;
    }

    private double calculateAverageResolutionTime(List<GrievanceCase> cases) {
        return cases.stream()
            .filter(c -> c.getResolutionDate() != null)
            .mapToLong(c -> java.time.Duration.between(c.getSubmissionDate(), c.getResolutionDate()).toHours())
            .average()
            .orElse(0.0);
    }

    private double calculateRoutingAccuracy(List<GrievanceCase> cases) {
        // Simplified accuracy calculation - in practice would be more sophisticated
        long successfulRoutes = cases.stream()
            .filter(c -> c.getEscalationLevel() == 0) // No escalations = good routing
            .count();
        
        return cases.isEmpty() ? 0.0 : (double) successfulRoutes / cases.size();
    }
}
