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

/**
 * Enhanced Escalation Workflow Engine
 * Provides intelligent multi-level escalation with configurable rules
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationWorkflowEngine {

    private final GrievanceCaseRepository caseRepository;
    private final CaseAssignmentService caseAssignmentService;
    private final NotificationService notificationService;
    private final IntelligentCaseRoutingService routingService;

    // Escalation hierarchy by category
    private static final Map<GrievanceCase.GrievanceCategory, List<String>> ESCALATION_HIERARCHY = Map.of(
        GrievanceCase.GrievanceCategory.CORRUPTION, List.of(
            "integrity.officer@dswd.gov.ph",
            "senior.integrity.officer@dswd.gov.ph", 
            "regional.director@dswd.gov.ph",
            "national.director@dswd.gov.ph"
        ),
        GrievanceCase.GrievanceCategory.SYSTEM_ERROR, List.of(
            "it.support@dswd.gov.ph",
            "senior.it.manager@dswd.gov.ph",
            "it.director@dswd.gov.ph",
            "cto@dswd.gov.ph"
        ),
        GrievanceCase.GrievanceCategory.PAYMENT_ISSUE, List.of(
            "payment.specialist@dswd.gov.ph",
            "payment.supervisor@dswd.gov.ph",
            "finance.manager@dswd.gov.ph",
            "finance.director@dswd.gov.ph"
        ),
        GrievanceCase.GrievanceCategory.STAFF_CONDUCT, List.of(
            "hr.specialist@dswd.gov.ph",
            "hr.manager@dswd.gov.ph",
            "regional.director@dswd.gov.ph",
            "national.director@dswd.gov.ph"
        ),
        GrievanceCase.GrievanceCategory.ELIGIBILITY_DISPUTE, List.of(
            "eligibility.specialist@dswd.gov.ph",
            "eligibility.supervisor@dswd.gov.ph",
            "program.manager@dswd.gov.ph",
            "regional.director@dswd.gov.ph"
        )
    );

    // Default escalation hierarchy for other categories
    private static final List<String> DEFAULT_ESCALATION_HIERARCHY = List.of(
        "case.manager@dswd.gov.ph",
        "senior.case.manager@dswd.gov.ph",
        "operations.manager@dswd.gov.ph",
        "regional.director@dswd.gov.ph"
    );

    // Escalation triggers
    private static final Map<String, EscalationTrigger> ESCALATION_TRIGGERS = Map.of(
        "SLA_BREACH", new EscalationTrigger("SLA_BREACH", "Case exceeded SLA deadline", 1),
        "CRITICAL_PRIORITY", new EscalationTrigger("CRITICAL_PRIORITY", "Critical priority case", 0),
        "CUSTOMER_COMPLAINT", new EscalationTrigger("CUSTOMER_COMPLAINT", "Customer escalation request", 1),
        "COMPLEXITY", new EscalationTrigger("COMPLEXITY", "Case complexity requires higher expertise", 1),
        "REPEATED_ESCALATION", new EscalationTrigger("REPEATED_ESCALATION", "Multiple escalations", 2),
        "EXTERNAL_PRESSURE", new EscalationTrigger("EXTERNAL_PRESSURE", "External stakeholder pressure", 2)
    );

    /**
     * Process escalation based on trigger and case context
     */
    @Transactional
    public EscalationResult processEscalation(UUID caseId, String triggerType, String reason, String requestedBy) {
        log.info("Processing escalation for case {} with trigger: {}", caseId, triggerType);

        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }

        GrievanceCase grievanceCase = caseOpt.get();
        EscalationTrigger trigger = ESCALATION_TRIGGERS.get(triggerType);
        
        if (trigger == null) {
            throw new RuntimeException("Unknown escalation trigger: " + triggerType);
        }

        try {
            // Determine escalation strategy
            EscalationStrategy strategy = determineEscalationStrategy(grievanceCase, trigger);
            
            // Execute escalation
            EscalationResult result = executeEscalation(grievanceCase, strategy, reason, requestedBy);
            
            // Post-escalation actions
            performPostEscalationActions(grievanceCase, result);
            
            log.info("Escalation completed for case {}: {}", 
                grievanceCase.getCaseNumber(), result.getStatus());
            
            return result;

        } catch (Exception e) {
            log.error("Error processing escalation for case {}: {}", caseId, e.getMessage(), e);
            throw new RuntimeException("Escalation processing failed", e);
        }
    }

    /**
     * Determine optimal escalation strategy
     */
    private EscalationStrategy determineEscalationStrategy(GrievanceCase grievanceCase, EscalationTrigger trigger) {
        EscalationStrategy strategy = new EscalationStrategy();
        
        // Get escalation hierarchy for category
        List<String> hierarchy = ESCALATION_HIERARCHY.getOrDefault(
            grievanceCase.getCategory(), DEFAULT_ESCALATION_HIERARCHY);
        
        // Determine target level
        int currentLevel = grievanceCase.getEscalationLevel();
        int targetLevel = Math.min(currentLevel + trigger.getLevelIncrement(), hierarchy.size() - 1);
        
        // Handle special cases
        if (grievanceCase.getPriority() == GrievanceCase.Priority.CRITICAL && targetLevel < 2) {
            targetLevel = 2; // Critical cases go to at least level 2
        }
        
        if (trigger.getTriggerType().equals("REPEATED_ESCALATION") && targetLevel < 3) {
            targetLevel = 3; // Repeated escalations go to highest level
        }
        
        strategy.setTargetLevel(targetLevel);
        strategy.setTargetAssignee(hierarchy.get(targetLevel));
        strategy.setEscalationType(determineEscalationType(trigger, currentLevel, targetLevel));
        strategy.setUrgency(determineUrgency(grievanceCase, trigger));
        strategy.setNotificationScope(determineNotificationScope(targetLevel, trigger));
        
        return strategy;
    }

    /**
     * Execute the escalation strategy
     */
    private EscalationResult executeEscalation(GrievanceCase grievanceCase, EscalationStrategy strategy, 
                                             String reason, String requestedBy) {
        
        String previousAssignee = grievanceCase.getAssignedTo();
        int previousLevel = grievanceCase.getEscalationLevel();
        
        // Update case assignment and escalation level
        grievanceCase.setAssignedTo(strategy.getTargetAssignee());
        grievanceCase.setEscalationLevel(strategy.getTargetLevel());
        grievanceCase.setStatus(GrievanceCase.CaseStatus.ESCALATED);
        grievanceCase.setAssignedDate(LocalDateTime.now());
        
        // Adjust SLA if needed
        adjustSLAForEscalation(grievanceCase, strategy);
        
        // Add escalation activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.ESCALATION,
            String.format("Case escalated from level %d to level %d. Reason: %s", 
                previousLevel, strategy.getTargetLevel(), reason),
            requestedBy);
        activity.setAssignedBefore(previousAssignee);
        activity.setAssignedAfter(strategy.getTargetAssignee());
        activity.setIsAutomated(requestedBy.equals("SYSTEM"));
        grievanceCase.addActivity(activity);
        
        // Save changes
        caseRepository.save(grievanceCase);
        
        // Create result
        return EscalationResult.builder()
            .caseId(grievanceCase.getId())
            .caseNumber(grievanceCase.getCaseNumber())
            .status(EscalationStatus.SUCCESS)
            .previousAssignee(previousAssignee)
            .newAssignee(strategy.getTargetAssignee())
            .previousLevel(previousLevel)
            .newLevel(strategy.getTargetLevel())
            .escalationType(strategy.getEscalationType())
            .reason(reason)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * Perform post-escalation actions
     */
    private void performPostEscalationActions(GrievanceCase grievanceCase, EscalationResult result) {
        // Send notifications
        sendEscalationNotifications(grievanceCase, result);
        
        // Update workload tracking
        caseAssignmentService.reassignCase(
            result.getPreviousAssignee(), 
            grievanceCase.getCategory(), 
            grievanceCase.getPriority(), 
            "Escalation");
        
        // Schedule follow-up monitoring
        scheduleEscalationFollowUp(grievanceCase);
        
        // Log for analytics
        logEscalationMetrics(grievanceCase, result);
    }

    /**
     * Send escalation notifications to relevant stakeholders
     */
    private void sendEscalationNotifications(GrievanceCase grievanceCase, EscalationResult result) {
        try {
            // Notify new assignee
            notificationService.sendEscalationAssignmentNotification(grievanceCase, result.getNewAssignee());
            
            // Notify previous assignee
            if (result.getPreviousAssignee() != null) {
                notificationService.sendEscalationHandoffNotification(grievanceCase, result.getPreviousAssignee());
            }
            
            // Notify complainant
            notificationService.sendEscalationUpdateNotification(grievanceCase);
            
            // Notify management for high-level escalations
            if (result.getNewLevel() >= 2) {
                notificationService.sendManagementEscalationNotification(grievanceCase, result);
            }
            
        } catch (Exception e) {
            log.error("Error sending escalation notifications for case {}: {}", 
                grievanceCase.getCaseNumber(), e.getMessage(), e);
        }
    }

    /**
     * Adjust SLA deadlines for escalated cases
     */
    private void adjustSLAForEscalation(GrievanceCase grievanceCase, EscalationStrategy strategy) {
        if (strategy.getUrgency() == EscalationUrgency.CRITICAL) {
            // Reduce SLA by 50% for critical escalations
            LocalDateTime currentTarget = grievanceCase.getResolutionTargetDate();
            if (currentTarget != null) {
                LocalDateTime now = LocalDateTime.now();
                long remainingHours = java.time.Duration.between(now, currentTarget).toHours();
                LocalDateTime newTarget = now.plusHours(Math.max(remainingHours / 2, 4)); // Minimum 4 hours
                grievanceCase.setResolutionTargetDate(newTarget);
                
                log.info("Adjusted SLA for escalated case {}: new target {}", 
                    grievanceCase.getCaseNumber(), newTarget);
            }
        }
    }

    /**
     * Schedule follow-up monitoring for escalated cases
     */
    private void scheduleEscalationFollowUp(GrievanceCase grievanceCase) {
        // In a real implementation, this would schedule monitoring tasks
        log.info("Scheduled escalation follow-up monitoring for case: {}", grievanceCase.getCaseNumber());
    }

    /**
     * Log escalation metrics for analytics
     */
    private void logEscalationMetrics(GrievanceCase grievanceCase, EscalationResult result) {
        log.info("Escalation metrics - Case: {}, Level: {} -> {}, Category: {}, Priority: {}", 
            grievanceCase.getCaseNumber(), 
            result.getPreviousLevel(), 
            result.getNewLevel(),
            grievanceCase.getCategory(),
            grievanceCase.getPriority());
    }

    /**
     * Determine escalation type based on context
     */
    private EscalationType determineEscalationType(EscalationTrigger trigger, int currentLevel, int targetLevel) {
        if (trigger.getTriggerType().equals("CRITICAL_PRIORITY")) {
            return EscalationType.EMERGENCY;
        } else if (targetLevel - currentLevel > 1) {
            return EscalationType.SKIP_LEVEL;
        } else if (trigger.getTriggerType().equals("SLA_BREACH")) {
            return EscalationType.AUTOMATIC;
        } else {
            return EscalationType.STANDARD;
        }
    }

    /**
     * Determine escalation urgency
     */
    private EscalationUrgency determineUrgency(GrievanceCase grievanceCase, EscalationTrigger trigger) {
        if (grievanceCase.getPriority() == GrievanceCase.Priority.CRITICAL || 
            trigger.getTriggerType().equals("CRITICAL_PRIORITY")) {
            return EscalationUrgency.CRITICAL;
        } else if (grievanceCase.getPriority() == GrievanceCase.Priority.HIGH ||
                   trigger.getTriggerType().equals("SLA_BREACH")) {
            return EscalationUrgency.HIGH;
        } else {
            return EscalationUrgency.NORMAL;
        }
    }

    /**
     * Determine notification scope based on escalation level
     */
    private NotificationScope determineNotificationScope(int targetLevel, EscalationTrigger trigger) {
        if (targetLevel >= 3 || trigger.getTriggerType().equals("EXTERNAL_PRESSURE")) {
            return NotificationScope.EXECUTIVE;
        } else if (targetLevel >= 2) {
            return NotificationScope.MANAGEMENT;
        } else {
            return NotificationScope.OPERATIONAL;
        }
    }

    /**
     * Get escalation analytics
     */
    public Map<String, Object> getEscalationAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<GrievanceCase> escalatedCases = caseRepository.findEscalatedCasesSince(since);
        
        analytics.put("totalEscalations", escalatedCases.size());
        analytics.put("escalationsByCategory", getEscalationsByCategory(escalatedCases));
        analytics.put("escalationsByLevel", getEscalationsByLevel(escalatedCases));
        analytics.put("averageEscalationTime", calculateAverageEscalationTime(escalatedCases));
        analytics.put("escalationTrends", getEscalationTrends(escalatedCases));
        
        return analytics;
    }

    private Map<String, Long> getEscalationsByCategory(List<GrievanceCase> cases) {
        return cases.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                c -> c.getCategory().name(),
                java.util.stream.Collectors.counting()));
    }

    private Map<Integer, Long> getEscalationsByLevel(List<GrievanceCase> cases) {
        return cases.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                GrievanceCase::getEscalationLevel,
                java.util.stream.Collectors.counting()));
    }

    private double calculateAverageEscalationTime(List<GrievanceCase> cases) {
        return cases.stream()
            .filter(c -> c.getAssignedDate() != null)
            .mapToLong(c -> java.time.Duration.between(c.getSubmissionDate(), c.getAssignedDate()).toHours())
            .average()
            .orElse(0.0);
    }

    private Map<String, Object> getEscalationTrends(List<GrievanceCase> cases) {
        // Simplified trend analysis
        Map<String, Object> trends = new HashMap<>();
        trends.put("weeklyEscalations", cases.size() / 4); // Rough weekly average
        trends.put("criticalEscalations", cases.stream()
            .filter(c -> c.getPriority() == GrievanceCase.Priority.CRITICAL)
            .count());
        return trends;
    }

    // Supporting classes and enums
    @lombok.Data
    public static class EscalationTrigger {
        private final String triggerType;
        private final String description;
        private final int levelIncrement;
    }

    @lombok.Data
    public static class EscalationStrategy {
        private int targetLevel;
        private String targetAssignee;
        private EscalationType escalationType;
        private EscalationUrgency urgency;
        private NotificationScope notificationScope;
    }

    @lombok.Builder
    @lombok.Data
    public static class EscalationResult {
        private UUID caseId;
        private String caseNumber;
        private EscalationStatus status;
        private String previousAssignee;
        private String newAssignee;
        private int previousLevel;
        private int newLevel;
        private EscalationType escalationType;
        private String reason;
        private LocalDateTime timestamp;
    }

    public enum EscalationType {
        STANDARD, AUTOMATIC, EMERGENCY, SKIP_LEVEL
    }

    public enum EscalationUrgency {
        NORMAL, HIGH, CRITICAL
    }

    public enum NotificationScope {
        OPERATIONAL, MANAGEMENT, EXECUTIVE
    }

    public enum EscalationStatus {
        SUCCESS, FAILED, PENDING
    }
}
