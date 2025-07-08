package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core service for managing grievance cases
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GrievanceCaseService {

    private final GrievanceCaseRepository caseRepository;

    /**
     * Create new grievance case
     */
    @Transactional
    public GrievanceCase createCase(GrievanceCase grievanceCase) {
        log.info("Creating new grievance case for complainant: {}", grievanceCase.getComplainantPsn());
        
        // Generate case number if not provided
        if (grievanceCase.getCaseNumber() == null) {
            grievanceCase.setCaseNumber(generateCaseNumber());
        }
        
        // Set initial status and dates
        grievanceCase.setStatus(GrievanceCase.CaseStatus.SUBMITTED);
        grievanceCase.setSubmissionDate(LocalDateTime.now());
        
        // Save case
        GrievanceCase savedCase = caseRepository.save(grievanceCase);
        
        // Add initial activity
        CaseActivity initialActivity = new CaseActivity(savedCase, CaseActivity.ActivityType.CASE_CREATED,
                "Case created and submitted", "SYSTEM");
        savedCase.addActivity(initialActivity);
        
        log.info("Created grievance case: {}", savedCase.getCaseNumber());
        return caseRepository.save(savedCase);
    }

    /**
     * Get case by ID
     */
    @Transactional(readOnly = true)
    public Optional<GrievanceCase> getCaseById(UUID id) {
        return caseRepository.findById(id);
    }

    /**
     * Get case by case number
     */
    @Transactional(readOnly = true)
    public Optional<GrievanceCase> getCaseByCaseNumber(String caseNumber) {
        return caseRepository.findByCaseNumber(caseNumber);
    }

    /**
     * Get cases by complainant PSN
     */
    @Transactional(readOnly = true)
    public List<GrievanceCase> getCasesByComplainant(String complainantPsn) {
        return caseRepository.findByComplainantPsnOrderBySubmissionDateDesc(complainantPsn);
    }

    /**
     * Get cases by status
     */
    @Transactional(readOnly = true)
    public List<GrievanceCase> getCasesByStatus(GrievanceCase.CaseStatus status) {
        return caseRepository.findByStatus(status);
    }

    /**
     * Get assigned cases
     */
    @Transactional(readOnly = true)
    public List<GrievanceCase> getAssignedCases(String assignedTo) {
        return caseRepository.findByAssignedToOrderByPriorityDescSubmissionDateAsc(assignedTo);
    }

    /**
     * Get overdue cases
     */
    @Transactional(readOnly = true)
    public List<GrievanceCase> getOverdueCases() {
        return caseRepository.findOverdueCases(LocalDateTime.now());
    }

    /**
     * Assign case to staff member
     */
    @Transactional
    public GrievanceCase assignCase(UUID caseId, String assignedTo, String assignedBy) {
        log.info("Assigning case {} to {}", caseId, assignedTo);
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        String previousAssignee = grievanceCase.getAssignedTo();
        
        grievanceCase.assignTo(assignedTo);
        grievanceCase.setUpdatedBy(assignedBy);
        
        // Add activity
        CaseActivity activity = new CaseActivity(grievanceCase, CaseActivity.ActivityType.CASE_ASSIGNED,
                "Case assigned to " + assignedTo, assignedBy);
        activity.setAssignedBefore(previousAssignee);
        activity.setAssignedAfter(assignedTo);
        grievanceCase.addActivity(activity);
        
        return caseRepository.save(grievanceCase);
    }

    /**
     * Update case status
     */
    @Transactional
    public GrievanceCase updateStatus(UUID caseId, GrievanceCase.CaseStatus newStatus, 
                                    String reason, String updatedBy) {
        log.info("Updating case {} status to {}", caseId, newStatus);
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        GrievanceCase.CaseStatus oldStatus = grievanceCase.getStatus();
        
        grievanceCase.setStatus(newStatus);
        grievanceCase.setUpdatedBy(updatedBy);
        
        // Add activity
        CaseActivity activity = CaseActivity.createStatusChange(grievanceCase, 
                oldStatus.toString(), newStatus.toString(), updatedBy, reason);
        grievanceCase.addActivity(activity);
        
        return caseRepository.save(grievanceCase);
    }

    /**
     * Escalate case
     */
    @Transactional
    public GrievanceCase escalateCase(UUID caseId, String escalatedTo, String reason, String escalatedBy) {
        log.info("Escalating case {} to {}", caseId, escalatedTo);
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        grievanceCase.escalate(escalatedTo, reason);
        grievanceCase.setUpdatedBy(escalatedBy);
        
        // Add activity
        CaseActivity activity = new CaseActivity(grievanceCase, CaseActivity.ActivityType.ESCALATION,
                "Case escalated to " + escalatedTo + ": " + reason, escalatedBy);
        grievanceCase.addActivity(activity);
        
        return caseRepository.save(grievanceCase);
    }

    /**
     * Resolve case
     */
    @Transactional
    public GrievanceCase resolveCase(UUID caseId, String resolutionSummary, 
                                   String resolutionActions, String resolvedBy) {
        log.info("Resolving case {}", caseId);
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        grievanceCase.resolve(resolutionSummary, resolutionActions);
        grievanceCase.setUpdatedBy(resolvedBy);
        
        // Add activity
        CaseActivity activity = new CaseActivity(grievanceCase, CaseActivity.ActivityType.CASE_RESOLVED,
                "Case resolved: " + resolutionSummary, resolvedBy);
        grievanceCase.addActivity(activity);
        
        return caseRepository.save(grievanceCase);
    }

    /**
     * Add communication to case
     */
    @Transactional
    public GrievanceCase addCommunication(UUID caseId, String channel, String direction,
                                        String subject, String content, String performedBy) {
        log.info("Adding communication to case {}", caseId);
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        
        // Add communication activity
        CaseActivity activity = CaseActivity.createCommunication(grievanceCase, channel, 
                direction, subject, content, performedBy);
        grievanceCase.addActivity(activity);
        
        return caseRepository.save(grievanceCase);
    }

    /**
     * Search cases
     */
    @Transactional(readOnly = true)
    public Page<GrievanceCase> searchCases(String searchText, Pageable pageable) {
        return caseRepository.searchCases(searchText, pageable);
    }

    /**
     * Get case statistics
     */
    @Transactional(readOnly = true)
    public Object[] getCaseStatistics() {
        return caseRepository.getCaseStatistics();
    }

    /**
     * Get cases by criteria
     */
    @Transactional(readOnly = true)
    public Page<GrievanceCase> getCasesByCriteria(GrievanceCase.CaseStatus status,
                                                GrievanceCase.Priority priority,
                                                GrievanceCase.GrievanceCategory category,
                                                String assignedTo,
                                                LocalDateTime startDate,
                                                LocalDateTime endDate,
                                                Pageable pageable) {
        return caseRepository.findByCriteria(status, priority, category, assignedTo,
                                           startDate, endDate, pageable);
    }

    /**
     * Get cases assigned to specific staff member
     */
    @Transactional(readOnly = true)
    public Page<GrievanceCase> getCasesAssignedTo(String assignedTo, Pageable pageable) {
        return caseRepository.findByAssignedToOrderBySubmissionDateDesc(assignedTo, pageable);
    }

    /**
     * Update case status
     */
    @Transactional
    public GrievanceCase updateCaseStatus(UUID caseId, GrievanceCase.CaseStatus newStatus,
                                        String reason, String updatedBy) {
        log.info("Updating status for case {} to {}", caseId, newStatus);

        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }

        GrievanceCase grievanceCase = caseOpt.get();
        GrievanceCase.CaseStatus oldStatus = grievanceCase.getStatus();

        grievanceCase.setStatus(newStatus);
        grievanceCase.setUpdatedBy(updatedBy);

        // Add status change activity
        CaseActivity activity = new CaseActivity(grievanceCase, CaseActivity.ActivityType.STATUS_CHANGED,
                "Status changed from " + oldStatus + " to " + newStatus +
                (reason != null ? ". Reason: " + reason : ""), updatedBy);
        activity.setStatusBefore(oldStatus.name());
        activity.setStatusAfter(newStatus.name());
        grievanceCase.addActivity(activity);

        return caseRepository.save(grievanceCase);
    }

    /**
     * Add comment to case
     */
    @Transactional
    public GrievanceCase addComment(UUID caseId, String comment, String author, Boolean isInternal) {
        log.info("Adding comment to case {}", caseId);

        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }

        GrievanceCase grievanceCase = caseOpt.get();

        // Add comment as activity
        CaseActivity activity = new CaseActivity(grievanceCase, CaseActivity.ActivityType.NOTE_ADDED,
                comment, author);
        activity.setIsInternal(isInternal != null ? isInternal : false);
        grievanceCase.addActivity(activity);

        return caseRepository.save(grievanceCase);
    }



    // Helper methods

    private String generateCaseNumber() {
        return "GRV-" + System.currentTimeMillis();
    }
}
