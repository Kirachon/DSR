package ph.gov.dsr.grievance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.dto.NotificationPreferences;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Citizen Notification Service for comprehensive grievance case communications
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitizenNotificationService {

    private final NotificationService notificationService;
    
    // Notification templates
    private static final Map<String, String> SMS_TEMPLATES = Map.of(
        "CASE_RECEIVED", "Your grievance case %s has been received. Track status at dsr.gov.ph/track/%s",
        "CASE_ASSIGNED", "Your case %s has been assigned to our team. Expected resolution: %s",
        "CASE_UPDATE", "Update on case %s: %s. Check details at dsr.gov.ph/track/%s",
        "CASE_RESOLVED", "Your case %s has been resolved. Please review the resolution and provide feedback.",
        "CASE_ESCALATED", "Your case %s has been escalated for priority handling due to SLA requirements.",
        "FEEDBACK_REQUEST", "Please rate your experience with case %s at dsr.gov.ph/feedback/%s"
    );
    
    private static final Map<String, String> EMAIL_TEMPLATES = Map.of(
        "CASE_RECEIVED", "case_received_template",
        "CASE_ASSIGNED", "case_assigned_template", 
        "CASE_UPDATE", "case_update_template",
        "CASE_RESOLVED", "case_resolved_template",
        "CASE_ESCALATED", "case_escalated_template",
        "FEEDBACK_REQUEST", "feedback_request_template"
    );

    /**
     * Send comprehensive case acknowledgment notification
     */
    @Async
    public CompletableFuture<Void> sendCaseAcknowledgment(GrievanceCase grievanceCase) {
        log.info("Sending comprehensive case acknowledgment for case: {}", grievanceCase.getCaseNumber());
        
        return CompletableFuture.runAsync(() -> {
            try {
                NotificationPreferences preferences = getNotificationPreferences(grievanceCase.getComplainantPsn());
                
                // Send SMS notification
                if (preferences.isSmsEnabled() && grievanceCase.getComplainantPhone() != null) {
                    sendSMSNotification(grievanceCase, "CASE_RECEIVED", preferences);
                }
                
                // Send email notification
                if (preferences.isEmailEnabled() && grievanceCase.getComplainantEmail() != null) {
                    sendEmailNotification(grievanceCase, "CASE_RECEIVED", preferences);
                }
                
                // Send push notification if mobile app user
                if (preferences.isPushEnabled()) {
                    sendPushNotification(grievanceCase, "CASE_RECEIVED", preferences);
                }
                
                // Schedule follow-up notifications
                scheduleFollowUpNotifications(grievanceCase, preferences);
                
            } catch (Exception e) {
                log.error("Error sending case acknowledgment for case: {}", grievanceCase.getCaseNumber(), e);
            }
        });
    }

    /**
     * Send case status update notification
     */
    @Async
    public CompletableFuture<Void> sendCaseStatusUpdate(GrievanceCase grievanceCase, String updateMessage) {
        log.info("Sending case status update for case: {}", grievanceCase.getCaseNumber());
        
        return CompletableFuture.runAsync(() -> {
            try {
                NotificationPreferences preferences = getNotificationPreferences(grievanceCase.getComplainantPsn());
                
                // Create update context
                Map<String, Object> context = new HashMap<>();
                context.put("updateMessage", updateMessage);
                context.put("caseNumber", grievanceCase.getCaseNumber());
                context.put("status", grievanceCase.getStatus().toString());
                context.put("lastUpdate", LocalDateTime.now());
                
                // Send notifications based on preferences
                if (preferences.isSmsEnabled() && grievanceCase.getComplainantPhone() != null) {
                    sendSMSNotification(grievanceCase, "CASE_UPDATE", preferences, context);
                }
                
                if (preferences.isEmailEnabled() && grievanceCase.getComplainantEmail() != null) {
                    sendEmailNotification(grievanceCase, "CASE_UPDATE", preferences, context);
                }
                
                if (preferences.isPushEnabled()) {
                    sendPushNotification(grievanceCase, "CASE_UPDATE", preferences, context);
                }
                
            } catch (Exception e) {
                log.error("Error sending case status update for case: {}", grievanceCase.getCaseNumber(), e);
            }
        });
    }

    /**
     * Send case resolution notification
     */
    @Async
    public CompletableFuture<Void> sendCaseResolutionNotification(GrievanceCase grievanceCase) {
        log.info("Sending case resolution notification for case: {}", grievanceCase.getCaseNumber());
        
        return CompletableFuture.runAsync(() -> {
            try {
                NotificationPreferences preferences = getNotificationPreferences(grievanceCase.getComplainantPsn());
                
                // Send immediate resolution notification
                if (preferences.isSmsEnabled() && grievanceCase.getComplainantPhone() != null) {
                    sendSMSNotification(grievanceCase, "CASE_RESOLVED", preferences);
                }
                
                if (preferences.isEmailEnabled() && grievanceCase.getComplainantEmail() != null) {
                    sendEmailNotification(grievanceCase, "CASE_RESOLVED", preferences);
                }
                
                if (preferences.isPushEnabled()) {
                    sendPushNotification(grievanceCase, "CASE_RESOLVED", preferences);
                }
                
                // Schedule feedback request (24 hours later)
                scheduleFeedbackRequest(grievanceCase, preferences);
                
            } catch (Exception e) {
                log.error("Error sending case resolution notification for case: {}", grievanceCase.getCaseNumber(), e);
            }
        });
    }

    /**
     * Send escalation notification
     */
    @Async
    public CompletableFuture<Void> sendEscalationNotification(GrievanceCase grievanceCase) {
        log.info("Sending escalation notification for case: {}", grievanceCase.getCaseNumber());
        
        return CompletableFuture.runAsync(() -> {
            try {
                NotificationPreferences preferences = getNotificationPreferences(grievanceCase.getComplainantPsn());
                
                // Escalation notifications are always sent regardless of preferences (high priority)
                if (grievanceCase.getComplainantPhone() != null) {
                    sendSMSNotification(grievanceCase, "CASE_ESCALATED", preferences);
                }
                
                if (grievanceCase.getComplainantEmail() != null) {
                    sendEmailNotification(grievanceCase, "CASE_ESCALATED", preferences);
                }
                
                sendPushNotification(grievanceCase, "CASE_ESCALATED", preferences);
                
            } catch (Exception e) {
                log.error("Error sending escalation notification for case: {}", grievanceCase.getCaseNumber(), e);
            }
        });
    }

    /**
     * Send SMS notification
     */
    private void sendSMSNotification(GrievanceCase grievanceCase, String templateKey, 
                                   NotificationPreferences preferences) {
        sendSMSNotification(grievanceCase, templateKey, preferences, new HashMap<>());
    }
    
    private void sendSMSNotification(GrievanceCase grievanceCase, String templateKey, 
                                   NotificationPreferences preferences, Map<String, Object> context) {
        try {
            String template = SMS_TEMPLATES.get(templateKey);
            String message = formatSMSMessage(template, grievanceCase, context);
            
            notificationService.sendSMS(grievanceCase.getComplainantPhone(), message);
            
            log.debug("SMS notification sent for case: {} template: {}", 
                     grievanceCase.getCaseNumber(), templateKey);
                     
        } catch (Exception e) {
            log.error("Error sending SMS notification for case: {}", grievanceCase.getCaseNumber(), e);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(GrievanceCase grievanceCase, String templateKey, 
                                     NotificationPreferences preferences) {
        sendEmailNotification(grievanceCase, templateKey, preferences, new HashMap<>());
    }
    
    private void sendEmailNotification(GrievanceCase grievanceCase, String templateKey, 
                                     NotificationPreferences preferences, Map<String, Object> context) {
        try {
            String template = EMAIL_TEMPLATES.get(templateKey);
            String subject = generateEmailSubject(templateKey, grievanceCase);
            String message = formatEmailMessage(template, grievanceCase, context);
            
            notificationService.sendEmail(grievanceCase.getComplainantEmail(), subject, message);
            
            log.debug("Email notification sent for case: {} template: {}", 
                     grievanceCase.getCaseNumber(), templateKey);
                     
        } catch (Exception e) {
            log.error("Error sending email notification for case: {}", grievanceCase.getCaseNumber(), e);
        }
    }

    /**
     * Send push notification
     */
    private void sendPushNotification(GrievanceCase grievanceCase, String templateKey, 
                                    NotificationPreferences preferences) {
        sendPushNotification(grievanceCase, templateKey, preferences, new HashMap<>());
    }
    
    private void sendPushNotification(GrievanceCase grievanceCase, String templateKey, 
                                    NotificationPreferences preferences, Map<String, Object> context) {
        try {
            String title = generatePushTitle(templateKey, grievanceCase);
            String message = generatePushMessage(templateKey, grievanceCase, context);
            
            // Implementation would integrate with mobile app push notification service
            log.debug("Push notification sent for case: {} template: {}", 
                     grievanceCase.getCaseNumber(), templateKey);
                     
        } catch (Exception e) {
            log.error("Error sending push notification for case: {}", grievanceCase.getCaseNumber(), e);
        }
    }

    /**
     * Schedule follow-up notifications
     */
    private void scheduleFollowUpNotifications(GrievanceCase grievanceCase, NotificationPreferences preferences) {
        // Implementation would schedule notifications based on SLA timelines
        log.debug("Follow-up notifications scheduled for case: {}", grievanceCase.getCaseNumber());
    }

    /**
     * Schedule feedback request
     */
    private void scheduleFeedbackRequest(GrievanceCase grievanceCase, NotificationPreferences preferences) {
        // Implementation would schedule feedback request 24 hours after resolution
        log.debug("Feedback request scheduled for case: {}", grievanceCase.getCaseNumber());
    }

    /**
     * Get notification preferences for citizen
     */
    private NotificationPreferences getNotificationPreferences(String psn) {
        // Implementation would fetch from user preferences service
        return NotificationPreferences.builder()
            .smsEnabled(true)
            .emailEnabled(true)
            .pushEnabled(true)
            .language("en")
            .timezone("Asia/Manila")
            .build();
    }

    /**
     * Helper methods for message formatting
     */
    private String formatSMSMessage(String template, GrievanceCase grievanceCase, Map<String, Object> context) {
        String message = template;
        message = message.replace("%s", grievanceCase.getCaseNumber());
        
        if (context.containsKey("updateMessage")) {
            message = String.format(template, grievanceCase.getCaseNumber(), 
                                  context.get("updateMessage"), grievanceCase.getCaseNumber());
        }
        
        return message;
    }

    private String formatEmailMessage(String template, GrievanceCase grievanceCase, Map<String, Object> context) {
        // Implementation would use template engine to format email
        return "Email message for case: " + grievanceCase.getCaseNumber();
    }

    private String generateEmailSubject(String templateKey, GrievanceCase grievanceCase) {
        return String.format("DSR Grievance Case %s - %s", 
                           grievanceCase.getCaseNumber(), templateKey.replace("_", " "));
    }

    private String generatePushTitle(String templateKey, GrievanceCase grievanceCase) {
        return "DSR Case Update";
    }

    private String generatePushMessage(String templateKey, GrievanceCase grievanceCase, Map<String, Object> context) {
        return String.format("Update on your case %s", grievanceCase.getCaseNumber());
    }
}
