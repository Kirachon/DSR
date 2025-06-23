package ph.gov.dsr.registration.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.registration.entity.Registration;
import ph.gov.dsr.registration.entity.User;
import ph.gov.dsr.registration.service.AuditService;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of AuditService for comprehensive audit logging
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-22
 */
@Service
@Profile("!no-db")
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Override
    public void logRegistrationCreated(Registration registration, User createdBy) {
        String message = String.format("Registration created: %s by user %s (%s)", 
            registration.getRegistrationNumber(), 
            createdBy != null ? createdBy.getEmail() : "SYSTEM",
            createdBy != null ? createdBy.getId() : "N/A");
        
        auditLogger.info("REGISTRATION_CREATED: {}", message);
        
        // In a full implementation, this would save to audit_log table
        logAuditEvent("REGISTRATION_CREATED", registration.getId().toString(), 
            createdBy != null ? createdBy.getId() : null, message);
    }

    @Override
    public void logRegistrationSubmitted(Registration registration, User submittedBy) {
        String message = String.format("Registration submitted: %s by user %s (%s)", 
            registration.getRegistrationNumber(), 
            submittedBy != null ? submittedBy.getEmail() : "SYSTEM",
            submittedBy != null ? submittedBy.getId() : "N/A");
        
        auditLogger.info("REGISTRATION_SUBMITTED: {}", message);
        
        logAuditEvent("REGISTRATION_SUBMITTED", registration.getId().toString(), 
            submittedBy != null ? submittedBy.getId() : null, message);
    }

    @Override
    public void logRegistrationApproved(Registration registration, User approvedBy, String notes) {
        String message = String.format("Registration approved: %s by user %s (%s). Notes: %s", 
            registration.getRegistrationNumber(), 
            approvedBy != null ? approvedBy.getEmail() : "SYSTEM",
            approvedBy != null ? approvedBy.getId() : "N/A",
            notes != null ? notes : "None");
        
        auditLogger.info("REGISTRATION_APPROVED: {}", message);
        
        logAuditEvent("REGISTRATION_APPROVED", registration.getId().toString(), 
            approvedBy != null ? approvedBy.getId() : null, message);
    }

    @Override
    public void logRegistrationRejected(Registration registration, User rejectedBy, String reason, String notes) {
        String message = String.format("Registration rejected: %s by user %s (%s). Reason: %s. Notes: %s", 
            registration.getRegistrationNumber(), 
            rejectedBy != null ? rejectedBy.getEmail() : "SYSTEM",
            rejectedBy != null ? rejectedBy.getId() : "N/A",
            reason != null ? reason : "Not specified",
            notes != null ? notes : "None");
        
        auditLogger.info("REGISTRATION_REJECTED: {}", message);
        
        logAuditEvent("REGISTRATION_REJECTED", registration.getId().toString(), 
            rejectedBy != null ? rejectedBy.getId() : null, message);
    }

    @Override
    public void logRegistrationAssigned(Registration registration, User assignedTo, User assignedBy) {
        String message = String.format("Registration assigned: %s assigned to %s (%s) by %s (%s)", 
            registration.getRegistrationNumber(), 
            assignedTo != null ? assignedTo.getEmail() : "UNKNOWN",
            assignedTo != null ? assignedTo.getId() : "N/A",
            assignedBy != null ? assignedBy.getEmail() : "SYSTEM",
            assignedBy != null ? assignedBy.getId() : "N/A");
        
        auditLogger.info("REGISTRATION_ASSIGNED: {}", message);
        
        logAuditEvent("REGISTRATION_ASSIGNED", registration.getId().toString(), 
            assignedBy != null ? assignedBy.getId() : null, message);
    }

    @Override
    public void logRegistrationUpdated(Registration registration, User updatedBy, String changes) {
        String message = String.format("Registration updated: %s by user %s (%s). Changes: %s", 
            registration.getRegistrationNumber(), 
            updatedBy != null ? updatedBy.getEmail() : "SYSTEM",
            updatedBy != null ? updatedBy.getId() : "N/A",
            changes != null ? changes : "Not specified");
        
        auditLogger.info("REGISTRATION_UPDATED: {}", message);
        
        logAuditEvent("REGISTRATION_UPDATED", registration.getId().toString(), 
            updatedBy != null ? updatedBy.getId() : null, message);
    }

    @Override
    public void logRegistrationCancelled(Registration registration, User cancelledBy, String reason) {
        String message = String.format("Registration cancelled: %s by user %s (%s). Reason: %s", 
            registration.getRegistrationNumber(), 
            cancelledBy != null ? cancelledBy.getEmail() : "SYSTEM",
            cancelledBy != null ? cancelledBy.getId() : "N/A",
            reason != null ? reason : "Not specified");
        
        auditLogger.info("REGISTRATION_CANCELLED: {}", message);
        
        logAuditEvent("REGISTRATION_CANCELLED", registration.getId().toString(), 
            cancelledBy != null ? cancelledBy.getId() : null, message);
    }

    @Override
    public void logDocumentUploaded(UUID registrationId, String documentType, String fileName, User uploadedBy) {
        String message = String.format("Document uploaded: %s (%s) for registration %s by user %s (%s)", 
            fileName, documentType, registrationId,
            uploadedBy != null ? uploadedBy.getEmail() : "SYSTEM",
            uploadedBy != null ? uploadedBy.getId() : "N/A");
        
        auditLogger.info("DOCUMENT_UPLOADED: {}", message);
        
        logAuditEvent("DOCUMENT_UPLOADED", registrationId.toString(), 
            uploadedBy != null ? uploadedBy.getId() : null, message);
    }

    @Override
    public void logDocumentVerified(UUID registrationId, String documentType, User verifiedBy, boolean verified) {
        String message = String.format("Document %s: %s for registration %s by user %s (%s)", 
            verified ? "verified" : "rejected", documentType, registrationId,
            verifiedBy != null ? verifiedBy.getEmail() : "SYSTEM",
            verifiedBy != null ? verifiedBy.getId() : "N/A");
        
        auditLogger.info("DOCUMENT_VERIFIED: {}", message);
        
        logAuditEvent("DOCUMENT_VERIFIED", registrationId.toString(), 
            verifiedBy != null ? verifiedBy.getId() : null, message);
    }

    @Override
    public void logSecurityEvent(String eventType, String description, User user, String ipAddress) {
        String message = String.format("Security event: %s - %s. User: %s (%s). IP: %s", 
            eventType, description,
            user != null ? user.getEmail() : "UNKNOWN",
            user != null ? user.getId() : "N/A",
            ipAddress != null ? ipAddress : "UNKNOWN");
        
        auditLogger.warn("SECURITY_EVENT: {}", message);
        
        logAuditEvent("SECURITY_EVENT", eventType, 
            user != null ? user.getId() : null, message);
    }

    @Override
    public void logSystemEvent(String eventType, String description, String additionalData) {
        String message = String.format("System event: %s - %s. Data: %s", 
            eventType, description, additionalData != null ? additionalData : "None");
        
        auditLogger.info("SYSTEM_EVENT: {}", message);
        
        logAuditEvent("SYSTEM_EVENT", eventType, null, message);
    }

    @Override
    public void logUserAction(User user, String action, String target, String details) {
        String message = String.format("User action: %s performed %s on %s. Details: %s", 
            user != null ? user.getEmail() : "UNKNOWN",
            action, target, details != null ? details : "None");
        
        auditLogger.info("USER_ACTION: {}", message);
        
        logAuditEvent("USER_ACTION", target, 
            user != null ? user.getId() : null, message);
    }

    /**
     * Internal method to log audit events
     * In a full implementation, this would save to the audit_log table
     */
    private void logAuditEvent(String eventType, String targetId, UUID userId, String description) {
        try {
            // For now, just log to application logs
            // In production, this would insert into dsr_audit.audit_log table
            logger.debug("Audit Event - Type: {}, Target: {}, User: {}, Description: {}", 
                eventType, targetId, userId, description);
            
            // TODO: Implement database audit logging
            // AuditLog auditLog = new AuditLog();
            // auditLog.setEventType(eventType);
            // auditLog.setTargetId(targetId);
            // auditLog.setUserId(userId);
            // auditLog.setDescription(description);
            // auditLog.setTimestamp(LocalDateTime.now());
            // auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            logger.error("Failed to log audit event: {}", e.getMessage(), e);
        }
    }
}
