package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Workflow Automation Service for Grievance Management
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowAutomationService {

    private final GrievanceCaseRepository caseRepository;
    private final NotificationService notificationService;
    private final CaseAssignmentService caseAssignmentService;
    private final IntelligentCaseRoutingService intelligentRoutingService;
    
    // SLA Configuration (in hours)
    private static final Map<GrievanceCase.Priority, Integer> SLA_ACKNOWLEDGMENT = Map.of(
        GrievanceCase.Priority.CRITICAL, 1,
        GrievanceCase.Priority.HIGH, 4,
        GrievanceCase.Priority.MEDIUM, 24,
        GrievanceCase.Priority.LOW, 48
    );
    
    private static final Map<GrievanceCase.Priority, Integer> SLA_RESOLUTION = Map.of(
        GrievanceCase.Priority.CRITICAL, 24,
        GrievanceCase.Priority.HIGH, 72,
        GrievanceCase.Priority.MEDIUM, 168, // 7 days
        GrievanceCase.Priority.LOW, 336 // 14 days
    );

    /**
     * Automated case routing and assignment based on category and workload
     */
    @Async
    @Transactional
    public void processNewCaseWorkflow(UUID caseId) {
        log.info("Processing automated workflow for new case: {}", caseId);
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            log.error("Case not found for workflow processing: {}", caseId);
            return;
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        
        try {
            // Step 1: Auto-acknowledge case
            acknowledgeCase(grievanceCase);
            
            // Step 2: Determine priority based on category and content analysis
            determinePriority(grievanceCase);
            
            // Step 3: Auto-assign to appropriate staff
            autoAssignCase(grievanceCase);
            
            // Step 4: Set SLA deadlines
            setSLADeadlines(grievanceCase);
            
            // Step 5: Send initial notifications
            sendInitialNotifications(grievanceCase);
            
            // Step 6: Schedule follow-up reminders
            scheduleFollowUpReminders(grievanceCase);
            
            caseRepository.save(grievanceCase);
            log.info("Automated workflow completed for case: {}", grievanceCase.getCaseNumber());
            
        } catch (Exception e) {
            log.error("Error processing automated workflow for case {}: {}", caseId, e.getMessage(), e);
        }
    }
    
    /**
     * Automated case acknowledgment
     */
    private void acknowledgeCase(GrievanceCase grievanceCase) {
        if (grievanceCase.getStatus() == GrievanceCase.CaseStatus.SUBMITTED) {
            grievanceCase.setStatus(GrievanceCase.CaseStatus.ACKNOWLEDGED);
            // Note: acknowledgment date tracking would be added to entity if needed
            
            // Add acknowledgment activity
            CaseActivity activity = new CaseActivity(grievanceCase, 
                CaseActivity.ActivityType.STATUS_CHANGED,
                "Case automatically acknowledged by system", "SYSTEM");
            activity.setStatusBefore("SUBMITTED");
            activity.setStatusAfter("ACKNOWLEDGED");
            activity.setIsAutomated(true);
            grievanceCase.addActivity(activity);
            
            log.debug("Case {} automatically acknowledged", grievanceCase.getCaseNumber());
        }
    }
    
    /**
     * Intelligent priority determination based on content analysis
     */
    private void determinePriority(GrievanceCase grievanceCase) {
        GrievanceCase.Priority originalPriority = grievanceCase.getPriority();
        GrievanceCase.Priority determinedPriority = analyzePriority(grievanceCase);
        
        if (determinedPriority != originalPriority) {
            grievanceCase.setPriority(determinedPriority);
            
            // Add priority change activity
            CaseActivity activity = new CaseActivity(grievanceCase,
                CaseActivity.ActivityType.PRIORITY_CHANGED,
                "Priority automatically adjusted based on content analysis", "SYSTEM");
            activity.setPriorityBefore(originalPriority.name());
            activity.setPriorityAfter(determinedPriority.name());
            activity.setIsAutomated(true);
            grievanceCase.addActivity(activity);
            
            log.debug("Case {} priority changed from {} to {}", 
                grievanceCase.getCaseNumber(), originalPriority, determinedPriority);
        }
    }
    
    /**
     * Analyze case content to determine appropriate priority
     */
    private GrievanceCase.Priority analyzePriority(GrievanceCase grievanceCase) {
        String content = (grievanceCase.getSubject() + " " + grievanceCase.getDescription()).toLowerCase();
        
        // Critical keywords
        List<String> criticalKeywords = Arrays.asList(
            "emergency", "urgent", "critical", "life threatening", "safety", "security breach",
            "corruption", "fraud", "discrimination", "harassment", "abuse"
        );
        
        // High priority keywords
        List<String> highKeywords = Arrays.asList(
            "payment not received", "benefit stopped", "system error", "data breach",
            "incorrect information", "service denied", "deadline missed"
        );
        
        // Check for critical issues
        if (criticalKeywords.stream().anyMatch(content::contains) ||
            grievanceCase.getCategory() == GrievanceCase.GrievanceCategory.CORRUPTION ||
            grievanceCase.getCategory() == GrievanceCase.GrievanceCategory.DISCRIMINATION) {
            return GrievanceCase.Priority.CRITICAL;
        }
        
        // Check for high priority issues
        if (highKeywords.stream().anyMatch(content::contains) ||
            grievanceCase.getCategory() == GrievanceCase.GrievanceCategory.PAYMENT_ISSUE ||
            grievanceCase.getCategory() == GrievanceCase.GrievanceCategory.SYSTEM_ERROR) {
            return GrievanceCase.Priority.HIGH;
        }
        
        // Default to medium priority
        return GrievanceCase.Priority.MEDIUM;
    }
    
    /**
     * Automated case assignment using intelligent routing
     */
    private void autoAssignCase(GrievanceCase grievanceCase) {
        try {
            // Use intelligent routing for optimal assignment
            String assignedStaff = intelligentRoutingService.routeCaseIntelligently(grievanceCase);

            if (assignedStaff != null) {
                String previousAssignee = grievanceCase.getAssignedTo();
                grievanceCase.setAssignedTo(assignedStaff);
                grievanceCase.setAssignedDate(LocalDateTime.now());
                grievanceCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);

                // Add assignment activity
                CaseActivity activity = new CaseActivity(grievanceCase,
                    CaseActivity.ActivityType.CASE_ASSIGNED,
                    "Case intelligently assigned based on content analysis, expertise, and workload", "SYSTEM");
                activity.setAssignedBefore(previousAssignee);
                activity.setAssignedAfter(assignedStaff);
                activity.setIsAutomated(true);
                grievanceCase.addActivity(activity);

                log.debug("Case {} intelligently assigned to {}",
                    grievanceCase.getCaseNumber(), assignedStaff);
            }
        } catch (Exception e) {
            log.warn("Failed to auto-assign case {}: {}",
                grievanceCase.getCaseNumber(), e.getMessage());

            // Fallback to standard assignment
            try {
                String fallbackStaff = caseAssignmentService.findBestAssignee(
                    grievanceCase.getCategory(), grievanceCase.getPriority());
                if (fallbackStaff != null) {
                    grievanceCase.setAssignedTo(fallbackStaff);
                    grievanceCase.setAssignedDate(LocalDateTime.now());
                    grievanceCase.setStatus(GrievanceCase.CaseStatus.UNDER_REVIEW);
                    log.info("Case {} assigned using fallback method to {}",
                        grievanceCase.getCaseNumber(), fallbackStaff);
                }
            } catch (Exception fallbackException) {
                log.error("Both intelligent and fallback assignment failed for case {}: {}",
                    grievanceCase.getCaseNumber(), fallbackException.getMessage());
            }
        }
    }
    
    /**
     * Set SLA deadlines based on priority
     */
    private void setSLADeadlines(GrievanceCase grievanceCase) {
        LocalDateTime now = LocalDateTime.now();
        GrievanceCase.Priority priority = grievanceCase.getPriority();
        
        // Set resolution deadline
        int resolutionHours = SLA_RESOLUTION.get(priority);
        grievanceCase.setResolutionTargetDate(now.plusHours(resolutionHours));

        log.debug("SLA deadlines set for case {}: resolution due {}",
            grievanceCase.getCaseNumber(), grievanceCase.getResolutionTargetDate());
    }
    
    /**
     * Send initial notifications to complainant and assigned staff
     */
    private void sendInitialNotifications(GrievanceCase grievanceCase) {
        try {
            // Notify complainant of acknowledgment
            notificationService.sendCaseAcknowledgment(grievanceCase);
            
            // Notify assigned staff of new case
            if (grievanceCase.getAssignedTo() != null) {
                notificationService.sendCaseAssignment(grievanceCase);
            }
            
        } catch (Exception e) {
            log.warn("Failed to send initial notifications for case {}: {}", 
                grievanceCase.getCaseNumber(), e.getMessage());
        }
    }
    
    /**
     * Schedule automated follow-up reminders
     */
    private void scheduleFollowUpReminders(GrievanceCase grievanceCase) {
        // Implementation would integrate with scheduling system
        // For now, we'll log the scheduling
        log.debug("Follow-up reminders scheduled for case {}", grievanceCase.getCaseNumber());
    }
    
    /**
     * Automated escalation for overdue cases
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void processOverdueCases() {
        log.info("Processing overdue cases for automated escalation");
        
        LocalDateTime now = LocalDateTime.now();
        List<GrievanceCase> overdueCases = caseRepository.findOverdueCases(now);
        
        for (GrievanceCase grievanceCase : overdueCases) {
            try {
                processOverdueCase(grievanceCase);
            } catch (Exception e) {
                log.error("Error processing overdue case {}: {}", 
                    grievanceCase.getCaseNumber(), e.getMessage(), e);
            }
        }
        
        log.info("Processed {} overdue cases", overdueCases.size());
    }
    
    /**
     * Process individual overdue case
     */
    private void processOverdueCase(GrievanceCase grievanceCase) {
        // Escalate if not already escalated recently
        if (shouldEscalate(grievanceCase)) {
            escalateCase(grievanceCase);
        }
        
        // Send overdue notifications
        notificationService.sendOverdueNotification(grievanceCase);
        
        // Adjust priority if significantly overdue
        if (isSignificantlyOverdue(grievanceCase)) {
            adjustPriorityForOverdue(grievanceCase);
        }
    }
    
    /**
     * Determine if case should be escalated
     */
    private boolean shouldEscalate(GrievanceCase grievanceCase) {
        // Don't escalate if already escalated recently (within 24 hours)
        if (grievanceCase.getEscalationDate() != null &&
            grievanceCase.getEscalationDate().isAfter(LocalDateTime.now().minusHours(24))) {
            return false;
        }
        
        // Escalate critical cases overdue by 2+ hours
        if (grievanceCase.getPriority() == GrievanceCase.Priority.CRITICAL &&
            isOverdueByHours(grievanceCase, 2)) {
            return true;
        }
        
        // Escalate high priority cases overdue by 12+ hours
        if (grievanceCase.getPriority() == GrievanceCase.Priority.HIGH &&
            isOverdueByHours(grievanceCase, 12)) {
            return true;
        }
        
        // Escalate other cases overdue by 48+ hours
        return isOverdueByHours(grievanceCase, 48);
    }
    
    /**
     * Check if case is overdue by specified hours
     */
    private boolean isOverdueByHours(GrievanceCase grievanceCase, int hours) {
        LocalDateTime dueDate = grievanceCase.getResolutionTargetDate();
        return dueDate != null && dueDate.isBefore(LocalDateTime.now().minusHours(hours));
    }
    
    /**
     * Check if case is significantly overdue
     */
    private boolean isSignificantlyOverdue(GrievanceCase grievanceCase) {
        return isOverdueByHours(grievanceCase, 72); // 3 days
    }
    
    /**
     * Escalate overdue case
     */
    private void escalateCase(GrievanceCase grievanceCase) {
        String escalationTarget = caseAssignmentService.findEscalationTarget(
            grievanceCase.getAssignedTo(), grievanceCase.getCategory());
        
        grievanceCase.escalate(escalationTarget, "Automated escalation due to SLA breach");
        
        // Add escalation activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.ESCALATION,
            "Case automatically escalated due to SLA breach", "SYSTEM");
        activity.setIsAutomated(true);
        grievanceCase.addActivity(activity);
        
        caseRepository.save(grievanceCase);
        
        log.info("Case {} automatically escalated to {}", 
            grievanceCase.getCaseNumber(), escalationTarget);
    }
    
    /**
     * Adjust priority for significantly overdue cases
     */
    private void adjustPriorityForOverdue(GrievanceCase grievanceCase) {
        GrievanceCase.Priority currentPriority = grievanceCase.getPriority();
        GrievanceCase.Priority newPriority = null;
        
        if (currentPriority == GrievanceCase.Priority.LOW) {
            newPriority = GrievanceCase.Priority.MEDIUM;
        } else if (currentPriority == GrievanceCase.Priority.MEDIUM) {
            newPriority = GrievanceCase.Priority.HIGH;
        }
        
        if (newPriority != null) {
            grievanceCase.setPriority(newPriority);
            
            // Add priority change activity
            CaseActivity activity = new CaseActivity(grievanceCase,
                CaseActivity.ActivityType.PRIORITY_CHANGED,
                "Priority automatically increased due to significant delay", "SYSTEM");
            activity.setPriorityBefore(currentPriority.name());
            activity.setPriorityAfter(newPriority.name());
            activity.setIsAutomated(true);
            grievanceCase.addActivity(activity);
            
            caseRepository.save(grievanceCase);
            
            log.info("Case {} priority automatically increased from {} to {} due to delay", 
                grievanceCase.getCaseNumber(), currentPriority, newPriority);
        }
    }
}
