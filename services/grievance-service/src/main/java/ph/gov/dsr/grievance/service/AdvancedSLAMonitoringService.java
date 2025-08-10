package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced SLA Monitoring Service
 * Provides real-time SLA tracking, predictive alerts, and performance analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedSLAMonitoringService {

    private final GrievanceCaseRepository caseRepository;
    private final NotificationService notificationService;
    private final WorkflowAutomationService workflowAutomationService;

    // SLA thresholds in hours
    private static final Map<GrievanceCase.Priority, Integer> SLA_THRESHOLDS = Map.of(
        GrievanceCase.Priority.CRITICAL, 24,
        GrievanceCase.Priority.HIGH, 72,
        GrievanceCase.Priority.MEDIUM, 168, // 7 days
        GrievanceCase.Priority.LOW, 336 // 14 days
    );

    // Warning thresholds (percentage of SLA)
    private static final Map<GrievanceCase.Priority, Double> WARNING_THRESHOLDS = Map.of(
        GrievanceCase.Priority.CRITICAL, 0.5, // 50% of SLA
        GrievanceCase.Priority.HIGH, 0.6,     // 60% of SLA
        GrievanceCase.Priority.MEDIUM, 0.7,   // 70% of SLA
        GrievanceCase.Priority.LOW, 0.8       // 80% of SLA
    );

    /**
     * Real-time SLA monitoring - runs every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    @Transactional
    public void performRealTimeSLAMonitoring() {
        log.info("Performing real-time SLA monitoring");

        try {
            // Get all active cases
            List<GrievanceCase> activeCases = getActiveCases();
            
            // Process each case for SLA status
            for (GrievanceCase grievanceCase : activeCases) {
                processCaseSLAStatus(grievanceCase);
            }

            // Generate SLA performance metrics
            generateSLAMetrics(activeCases);

            log.info("Completed SLA monitoring for {} active cases", activeCases.size());

        } catch (Exception e) {
            log.error("Error during SLA monitoring: {}", e.getMessage(), e);
        }
    }

    /**
     * Process individual case SLA status
     */
    private void processCaseSLAStatus(GrievanceCase grievanceCase) {
        try {
            SLAStatus slaStatus = calculateSLAStatus(grievanceCase);
            
            // Handle different SLA statuses
            switch (slaStatus.getStatus()) {
                case APPROACHING_WARNING:
                    handleApproachingWarning(grievanceCase, slaStatus);
                    break;
                case WARNING:
                    handleWarning(grievanceCase, slaStatus);
                    break;
                case APPROACHING_BREACH:
                    handleApproachingBreach(grievanceCase, slaStatus);
                    break;
                case BREACHED:
                    handleSLABreach(grievanceCase, slaStatus);
                    break;
                case CRITICAL_BREACH:
                    handleCriticalBreach(grievanceCase, slaStatus);
                    break;
                default:
                    // Case is within SLA - no action needed
                    break;
            }

            // Update case with SLA metrics
            updateCaseSLAMetrics(grievanceCase, slaStatus);

        } catch (Exception e) {
            log.error("Error processing SLA status for case {}: {}", 
                grievanceCase.getCaseNumber(), e.getMessage(), e);
        }
    }

    /**
     * Calculate comprehensive SLA status for a case
     */
    public SLAStatus calculateSLAStatus(GrievanceCase grievanceCase) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetDate = grievanceCase.getResolutionTargetDate();
        
        if (targetDate == null) {
            // Set target date if missing
            targetDate = calculateTargetDate(grievanceCase);
            grievanceCase.setResolutionTargetDate(targetDate);
        }

        Duration timeElapsed = Duration.between(grievanceCase.getSubmissionDate(), now);
        Duration totalSLATime = Duration.between(grievanceCase.getSubmissionDate(), targetDate);
        Duration timeRemaining = Duration.between(now, targetDate);

        double percentageElapsed = (double) timeElapsed.toMinutes() / totalSLATime.toMinutes();
        
        SLAStatusType statusType = determineSLAStatusType(percentageElapsed, timeRemaining);
        
        return SLAStatus.builder()
            .caseId(grievanceCase.getId())
            .caseNumber(grievanceCase.getCaseNumber())
            .priority(grievanceCase.getPriority())
            .status(statusType)
            .percentageElapsed(percentageElapsed)
            .timeElapsed(timeElapsed)
            .timeRemaining(timeRemaining)
            .targetDate(targetDate)
            .isOverdue(timeRemaining.isNegative())
            .riskLevel(calculateRiskLevel(percentageElapsed, grievanceCase.getPriority()))
            .build();
    }

    /**
     * Determine SLA status type based on elapsed time
     */
    private SLAStatusType determineSLAStatusType(double percentageElapsed, Duration timeRemaining) {
        if (timeRemaining.isNegative()) {
            // Case is overdue
            if (percentageElapsed > 1.5) {
                return SLAStatusType.CRITICAL_BREACH;
            } else {
                return SLAStatusType.BREACHED;
            }
        } else if (percentageElapsed > 0.9) {
            return SLAStatusType.APPROACHING_BREACH;
        } else if (percentageElapsed > 0.7) {
            return SLAStatusType.WARNING;
        } else if (percentageElapsed > 0.5) {
            return SLAStatusType.APPROACHING_WARNING;
        } else {
            return SLAStatusType.ON_TRACK;
        }
    }

    /**
     * Calculate risk level for predictive analytics
     */
    private RiskLevel calculateRiskLevel(double percentageElapsed, GrievanceCase.Priority priority) {
        double riskScore = percentageElapsed;
        
        // Adjust risk based on priority
        switch (priority) {
            case CRITICAL:
                riskScore *= 1.5;
                break;
            case HIGH:
                riskScore *= 1.2;
                break;
            case MEDIUM:
                riskScore *= 1.0;
                break;
            case LOW:
                riskScore *= 0.8;
                break;
        }

        if (riskScore > 1.2) {
            return RiskLevel.CRITICAL;
        } else if (riskScore > 0.9) {
            return RiskLevel.HIGH;
        } else if (riskScore > 0.7) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    /**
     * Handle approaching warning threshold
     */
    private void handleApproachingWarning(GrievanceCase grievanceCase, SLAStatus slaStatus) {
        log.info("Case {} approaching warning threshold: {:.1f}% elapsed", 
            grievanceCase.getCaseNumber(), slaStatus.getPercentageElapsed() * 100);
        
        // Send early warning notification to assignee
        notificationService.sendSLAWarningNotification(grievanceCase, slaStatus);
        
        // Add activity log
        addSLAActivity(grievanceCase, "SLA approaching warning threshold", slaStatus);
    }

    /**
     * Handle warning threshold reached
     */
    private void handleWarning(GrievanceCase grievanceCase, SLAStatus slaStatus) {
        log.warn("Case {} reached warning threshold: {:.1f}% elapsed", 
            grievanceCase.getCaseNumber(), slaStatus.getPercentageElapsed() * 100);
        
        // Send warning notification to assignee and supervisor
        notificationService.sendSLAWarningNotification(grievanceCase, slaStatus);
        
        // Consider priority escalation for critical cases
        if (grievanceCase.getPriority() == GrievanceCase.Priority.CRITICAL) {
            considerPriorityEscalation(grievanceCase);
        }
        
        addSLAActivity(grievanceCase, "SLA warning threshold reached", slaStatus);
    }

    /**
     * Handle approaching breach
     */
    private void handleApproachingBreach(GrievanceCase grievanceCase, SLAStatus slaStatus) {
        log.warn("Case {} approaching SLA breach: {:.1f}% elapsed", 
            grievanceCase.getCaseNumber(), slaStatus.getPercentageElapsed() * 100);
        
        // Send urgent notification
        notificationService.sendSLAUrgentNotification(grievanceCase, slaStatus);
        
        // Trigger automated escalation preparation
        prepareForEscalation(grievanceCase);
        
        addSLAActivity(grievanceCase, "SLA approaching breach - escalation prepared", slaStatus);
    }

    /**
     * Handle SLA breach
     */
    private void handleSLABreach(GrievanceCase grievanceCase, SLAStatus slaStatus) {
        log.error("SLA BREACH: Case {} has exceeded target resolution time", 
            grievanceCase.getCaseNumber());
        
        // Send breach notification to all stakeholders
        notificationService.sendSLABreachNotification(grievanceCase, slaStatus);
        
        // Trigger automated escalation
        workflowAutomationService.processOverdueCases();
        
        // Update case status
        if (grievanceCase.getStatus() != GrievanceCase.CaseStatus.ESCALATED) {
            grievanceCase.setStatus(GrievanceCase.CaseStatus.ESCALATED);
        }
        
        addSLAActivity(grievanceCase, "SLA BREACHED - automated escalation triggered", slaStatus);
    }

    /**
     * Handle critical breach (significantly overdue)
     */
    private void handleCriticalBreach(GrievanceCase grievanceCase, SLAStatus slaStatus) {
        log.error("CRITICAL SLA BREACH: Case {} is significantly overdue", 
            grievanceCase.getCaseNumber());
        
        // Send critical breach notification
        notificationService.sendSLACriticalBreachNotification(grievanceCase, slaStatus);
        
        // Escalate to highest level
        escalateToHighestLevel(grievanceCase);
        
        addSLAActivity(grievanceCase, "CRITICAL SLA BREACH - escalated to highest level", slaStatus);
    }

    /**
     * Get all active cases for monitoring
     */
    private List<GrievanceCase> getActiveCases() {
        return caseRepository.findByStatusIn(Arrays.asList(
            GrievanceCase.CaseStatus.SUBMITTED,
            GrievanceCase.CaseStatus.ACKNOWLEDGED,
            GrievanceCase.CaseStatus.UNDER_REVIEW,
            GrievanceCase.CaseStatus.INVESTIGATING,
            GrievanceCase.CaseStatus.PENDING_RESPONSE,
            GrievanceCase.CaseStatus.ESCALATED
        ));
    }

    /**
     * Calculate target date for case resolution
     */
    private LocalDateTime calculateTargetDate(GrievanceCase grievanceCase) {
        int slaHours = SLA_THRESHOLDS.get(grievanceCase.getPriority());
        return grievanceCase.getSubmissionDate().plusHours(slaHours);
    }

    /**
     * Update case with SLA metrics
     */
    private void updateCaseSLAMetrics(GrievanceCase grievanceCase, SLAStatus slaStatus) {
        // Store SLA metrics in case for reporting
        // In a real implementation, this might be stored in a separate metrics table
        grievanceCase.setResolutionTargetDate(slaStatus.getTargetDate());
        caseRepository.save(grievanceCase);
    }

    /**
     * Add SLA-related activity to case
     */
    private void addSLAActivity(GrievanceCase grievanceCase, String description, SLAStatus slaStatus) {
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.SLA_MONITORING,
            description + String.format(" (%.1f%% elapsed, %s remaining)", 
                slaStatus.getPercentageElapsed() * 100,
                formatDuration(slaStatus.getTimeRemaining())), 
            "SLA_MONITOR");
        activity.setIsAutomated(true);
        grievanceCase.addActivity(activity);
        caseRepository.save(grievanceCase);
    }

    /**
     * Format duration for display
     */
    private String formatDuration(Duration duration) {
        if (duration.isNegative()) {
            return "OVERDUE by " + formatPositiveDuration(duration.abs());
        }
        return formatPositiveDuration(duration);
    }

    private String formatPositiveDuration(Duration duration) {
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " hours";
        } else {
            long days = duration.toDays();
            return days + " days";
        }
    }

    /**
     * Consider priority escalation for critical cases
     */
    private void considerPriorityEscalation(GrievanceCase grievanceCase) {
        // Logic to escalate priority if needed
        log.info("Considering priority escalation for critical case: {}", grievanceCase.getCaseNumber());
    }

    /**
     * Prepare case for escalation
     */
    private void prepareForEscalation(GrievanceCase grievanceCase) {
        log.info("Preparing case {} for escalation", grievanceCase.getCaseNumber());
        // Pre-escalation preparation logic
    }

    /**
     * Escalate to highest level
     */
    private void escalateToHighestLevel(GrievanceCase grievanceCase) {
        log.info("Escalating case {} to highest level", grievanceCase.getCaseNumber());
        grievanceCase.setEscalationLevel(Math.max(grievanceCase.getEscalationLevel(), 3));
        grievanceCase.setAssignedTo("director@dswd.gov.ph");
        caseRepository.save(grievanceCase);
    }

    /**
     * Generate SLA performance metrics
     */
    private void generateSLAMetrics(List<GrievanceCase> activeCases) {
        Map<String, Object> metrics = new HashMap<>();
        
        long totalCases = activeCases.size();
        long onTrackCases = activeCases.stream()
            .mapToLong(c -> calculateSLAStatus(c).getStatus() == SLAStatusType.ON_TRACK ? 1 : 0)
            .sum();
        
        double slaCompliance = totalCases > 0 ? (double) onTrackCases / totalCases : 1.0;
        
        log.info("SLA Metrics - Total: {}, On Track: {}, Compliance: {:.1f}%", 
            totalCases, onTrackCases, slaCompliance * 100);
    }

    // Enums and DTOs
    public enum SLAStatusType {
        ON_TRACK, APPROACHING_WARNING, WARNING, APPROACHING_BREACH, BREACHED, CRITICAL_BREACH
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @lombok.Builder
    @lombok.Data
    public static class SLAStatus {
        private UUID caseId;
        private String caseNumber;
        private GrievanceCase.Priority priority;
        private SLAStatusType status;
        private double percentageElapsed;
        private Duration timeElapsed;
        private Duration timeRemaining;
        private LocalDateTime targetDate;
        private boolean isOverdue;
        private RiskLevel riskLevel;
    }
}
