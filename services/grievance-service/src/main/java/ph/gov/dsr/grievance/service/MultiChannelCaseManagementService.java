package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.grievance.dto.CaseSubmissionRequest;
import ph.gov.dsr.grievance.dto.CommunicationRequest;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Multi-Channel Case Management Service for handling grievances from various channels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiChannelCaseManagementService {

    private final GrievanceCaseRepository caseRepository;
    private final GrievanceCaseService caseService;
    private final WorkflowAutomationService workflowService;
    private final NotificationService notificationService;
    
    /**
     * Submit grievance case from web portal
     */
    @Transactional
    public GrievanceCase submitCaseFromWebPortal(CaseSubmissionRequest request) {
        log.info("Submitting grievance case from web portal for PSN: {}", request.getComplainantPsn());
        
        GrievanceCase grievanceCase = createCaseFromRequest(request, "WEB_PORTAL");
        grievanceCase = caseRepository.save(grievanceCase);
        
        // Add channel-specific activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.CASE_CREATED,
            "Case submitted via web portal", "WEB_PORTAL");
        activity.setCommunicationChannel("WEB_PORTAL");
        activity.setIsAutomated(false);
        grievanceCase.addActivity(activity);
        
        // Trigger automated workflow
        workflowService.processNewCaseWorkflow(grievanceCase.getId());
        
        log.info("Web portal case created: {}", grievanceCase.getCaseNumber());
        return caseRepository.save(grievanceCase);
    }
    
    /**
     * Submit grievance case from mobile app
     */
    @Transactional
    public GrievanceCase submitCaseFromMobileApp(CaseSubmissionRequest request) {
        log.info("Submitting grievance case from mobile app for PSN: {}", request.getComplainantPsn());
        
        GrievanceCase grievanceCase = createCaseFromRequest(request, "MOBILE_APP");
        grievanceCase = caseRepository.save(grievanceCase);
        
        // Add channel-specific activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.CASE_CREATED,
            "Case submitted via mobile application", "MOBILE_APP");
        activity.setCommunicationChannel("MOBILE_APP");
        activity.setIsAutomated(false);
        grievanceCase.addActivity(activity);
        
        // Trigger automated workflow
        workflowService.processNewCaseWorkflow(grievanceCase.getId());
        
        log.info("Mobile app case created: {}", grievanceCase.getCaseNumber());
        return caseRepository.save(grievanceCase);
    }
    
    /**
     * Submit grievance case from phone call
     */
    @Transactional
    public GrievanceCase submitCaseFromPhoneCall(CaseSubmissionRequest request, String callOperator) {
        log.info("Submitting grievance case from phone call for PSN: {}", request.getComplainantPsn());
        
        GrievanceCase grievanceCase = createCaseFromRequest(request, "PHONE_CALL");
        grievanceCase = caseRepository.save(grievanceCase);
        
        // Add channel-specific activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.CASE_CREATED,
            "Case submitted via phone call, recorded by operator: " + callOperator, callOperator);
        activity.setCommunicationChannel("PHONE");
        activity.setCommunicationDirection("INBOUND");
        activity.setIsAutomated(false);
        grievanceCase.addActivity(activity);
        
        // Trigger automated workflow
        workflowService.processNewCaseWorkflow(grievanceCase.getId());
        
        log.info("Phone call case created: {} by operator: {}", grievanceCase.getCaseNumber(), callOperator);
        return caseRepository.save(grievanceCase);
    }
    
    /**
     * Submit grievance case from email
     */
    @Transactional
    public GrievanceCase submitCaseFromEmail(CaseSubmissionRequest request, String emailSubject, String emailBody) {
        log.info("Submitting grievance case from email for PSN: {}", request.getComplainantPsn());
        
        GrievanceCase grievanceCase = createCaseFromRequest(request, "EMAIL");
        
        // Enhance description with email content
        String enhancedDescription = grievanceCase.getDescription() + 
            "\n\n--- Original Email ---\nSubject: " + emailSubject + 
            "\nBody: " + emailBody;
        grievanceCase.setDescription(enhancedDescription);
        
        grievanceCase = caseRepository.save(grievanceCase);
        
        // Add channel-specific activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.CASE_CREATED,
            "Case submitted via email", "EMAIL_SYSTEM");
        activity.setCommunicationChannel("EMAIL");
        activity.setCommunicationDirection("INBOUND");
        activity.setSubject(emailSubject);
        activity.setContent(emailBody);
        activity.setIsAutomated(true);
        grievanceCase.addActivity(activity);
        
        // Trigger automated workflow
        workflowService.processNewCaseWorkflow(grievanceCase.getId());
        
        log.info("Email case created: {}", grievanceCase.getCaseNumber());
        return caseRepository.save(grievanceCase);
    }
    
    /**
     * Submit grievance case from walk-in/in-person
     */
    @Transactional
    public GrievanceCase submitCaseFromWalkIn(CaseSubmissionRequest request, String receivingOfficer, String officeLocation) {
        log.info("Submitting grievance case from walk-in for PSN: {}", request.getComplainantPsn());
        
        GrievanceCase grievanceCase = createCaseFromRequest(request, "WALK_IN");
        grievanceCase = caseRepository.save(grievanceCase);
        
        // Add channel-specific activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.CASE_CREATED,
            "Case submitted in-person at " + officeLocation + ", received by: " + receivingOfficer, 
            receivingOfficer);
        activity.setCommunicationChannel("IN_PERSON");
        activity.setIsAutomated(false);
        grievanceCase.addActivity(activity);
        
        // Trigger automated workflow
        workflowService.processNewCaseWorkflow(grievanceCase.getId());
        
        log.info("Walk-in case created: {} at {} by {}", 
            grievanceCase.getCaseNumber(), officeLocation, receivingOfficer);
        return caseRepository.save(grievanceCase);
    }
    
    /**
     * Handle communication from any channel
     */
    @Transactional
    public GrievanceCase handleCommunication(UUID caseId, CommunicationRequest request) {
        log.info("Handling communication for case {} via {}", caseId, request.getChannel());
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        
        // Create communication activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.COMMUNICATION_RECEIVED,
            "Communication received via " + request.getChannel(), request.getPerformedBy());
        
        activity.setCommunicationChannel(request.getChannel());
        activity.setCommunicationDirection("INBOUND");
        activity.setSubject(request.getSubject());
        activity.setContent(request.getContent());
        activity.setRecipient(grievanceCase.getAssignedTo());
        
        // Set response requirement based on content
        if (requiresResponse(request.getContent())) {
            activity.setRequiresResponse(true);
            activity.setResponseDueDate(calculateResponseDueDate(grievanceCase.getPriority()));
        }
        
        grievanceCase.addActivity(activity);
        
        // Update case status if needed
        updateCaseStatusBasedOnCommunication(grievanceCase, request);
        
        // Send notifications
        notificationService.sendCommunicationReceived(grievanceCase, activity);
        
        log.info("Communication handled for case {}", grievanceCase.getCaseNumber());
        return caseRepository.save(grievanceCase);
    }
    
    /**
     * Send communication through appropriate channel
     */
    @Transactional
    public void sendCommunication(UUID caseId, CommunicationRequest request) {
        log.info("Sending communication for case {} via {}", caseId, request.getChannel());
        
        Optional<GrievanceCase> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseId);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        
        // Send through appropriate channel
        boolean sent = false;
        switch (request.getChannel().toUpperCase()) {
            case "EMAIL":
                sent = notificationService.sendEmail(grievanceCase.getComplainantEmail(), 
                    request.getSubject(), request.getContent());
                break;
            case "SMS":
                sent = notificationService.sendSMS(grievanceCase.getComplainantPhone(), 
                    request.getContent());
                break;
            case "PHONE":
                // Log phone call attempt
                sent = true; // Assume successful for logging
                break;
            case "POSTAL":
                sent = notificationService.sendPostalMail(grievanceCase, 
                    request.getSubject(), request.getContent());
                break;
            default:
                log.warn("Unsupported communication channel: {}", request.getChannel());
        }
        
        // Create communication activity
        CaseActivity activity = new CaseActivity(grievanceCase,
            CaseActivity.ActivityType.COMMUNICATION_SENT,
            "Communication sent via " + request.getChannel(), request.getPerformedBy());
        
        activity.setCommunicationChannel(request.getChannel());
        activity.setCommunicationDirection("OUTBOUND");
        activity.setSubject(request.getSubject());
        activity.setContent(request.getContent());
        activity.setRecipient(grievanceCase.getComplainantName());
        activity.setOutcome(sent ? "SENT" : "FAILED");
        
        grievanceCase.addActivity(activity);
        caseRepository.save(grievanceCase);
        
        log.info("Communication {} for case {} via {}", 
            sent ? "sent" : "failed", grievanceCase.getCaseNumber(), request.getChannel());
    }
    
    /**
     * Get unified case tracking across all channels
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUnifiedCaseTracking(String caseNumber) {
        Optional<GrievanceCase> caseOpt = caseRepository.findByCaseNumber(caseNumber);
        if (caseOpt.isEmpty()) {
            throw new RuntimeException("Case not found: " + caseNumber);
        }
        
        GrievanceCase grievanceCase = caseOpt.get();
        
        Map<String, Object> tracking = new HashMap<>();
        tracking.put("caseNumber", grievanceCase.getCaseNumber());
        tracking.put("status", grievanceCase.getStatus());
        tracking.put("priority", grievanceCase.getPriority());
        tracking.put("submissionDate", grievanceCase.getSubmissionDate());
        tracking.put("lastUpdate", grievanceCase.getUpdatedAt());
        
        // Channel breakdown
        Map<String, Integer> channelBreakdown = new HashMap<>();
        List<String> communicationTimeline = new ArrayList<>();
        
        for (CaseActivity activity : grievanceCase.getActivities()) {
            if (activity.getCommunicationChannel() != null) {
                channelBreakdown.merge(activity.getCommunicationChannel(), 1, Integer::sum);
                
                communicationTimeline.add(String.format("%s - %s via %s: %s",
                    activity.getActivityDate(),
                    activity.getCommunicationDirection(),
                    activity.getCommunicationChannel(),
                    activity.getDescription()));
            }
        }
        
        tracking.put("channelBreakdown", channelBreakdown);
        tracking.put("communicationTimeline", communicationTimeline);
        tracking.put("totalCommunications", communicationTimeline.size());
        
        return tracking;
    }
    
    // Helper methods
    
    private GrievanceCase createCaseFromRequest(CaseSubmissionRequest request, String channel) {
        GrievanceCase grievanceCase = new GrievanceCase();
        grievanceCase.setComplainantPsn(request.getComplainantPsn());
        grievanceCase.setComplainantName(request.getComplainantName());
        grievanceCase.setComplainantEmail(request.getComplainantEmail());
        grievanceCase.setComplainantPhone(request.getComplainantPhone());
        grievanceCase.setSubject(request.getSubject());
        grievanceCase.setDescription(request.getDescription());
        grievanceCase.setCategory(request.getCategory());
        grievanceCase.setPriority(request.getPriority() != null ? request.getPriority() : GrievanceCase.Priority.MEDIUM);
        grievanceCase.setSubmissionChannel(channel);
        grievanceCase.setSubmissionDate(LocalDateTime.now());
        grievanceCase.setStatus(GrievanceCase.CaseStatus.SUBMITTED);
        
        return grievanceCase;
    }
    
    private boolean requiresResponse(String content) {
        String lowerContent = content.toLowerCase();
        List<String> responseKeywords = Arrays.asList(
            "question", "when", "how", "why", "what", "please respond", 
            "need answer", "clarification", "explain", "status update"
        );
        
        return responseKeywords.stream().anyMatch(lowerContent::contains);
    }
    
    private LocalDateTime calculateResponseDueDate(GrievanceCase.Priority priority) {
        LocalDateTime now = LocalDateTime.now();
        switch (priority) {
            case CRITICAL:
                return now.plusHours(2);
            case HIGH:
                return now.plusHours(8);
            case MEDIUM:
                return now.plusDays(1);
            case LOW:
            default:
                return now.plusDays(2);
        }
    }
    
    private void updateCaseStatusBasedOnCommunication(GrievanceCase grievanceCase, CommunicationRequest request) {
        // Update status based on communication content
        String content = request.getContent().toLowerCase();
        
        if (content.contains("additional information") || content.contains("more details")) {
            if (grievanceCase.getStatus() == GrievanceCase.CaseStatus.UNDER_REVIEW) {
                grievanceCase.setStatus(GrievanceCase.CaseStatus.PENDING_RESPONSE);
            }
        } else if (content.contains("satisfied") || content.contains("resolved")) {
            // Don't auto-close, but flag for review
            grievanceCase.setNotes(grievanceCase.getNotes() + "\n[AUTO-FLAG] Complainant indicated satisfaction in communication");
        }
    }
}
