package ph.gov.dsr.registration.service;

import ph.gov.dsr.registration.entity.Registration;
import ph.gov.dsr.registration.entity.User;

import java.util.UUID;

/**
 * Service interface for audit operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-22
 */
public interface AuditService {

    /**
     * Log registration creation
     */
    void logRegistrationCreated(Registration registration, User createdBy);

    /**
     * Log registration submission
     */
    void logRegistrationSubmitted(Registration registration, User submittedBy);

    /**
     * Log registration approval
     */
    void logRegistrationApproved(Registration registration, User approvedBy, String notes);

    /**
     * Log registration rejection
     */
    void logRegistrationRejected(Registration registration, User rejectedBy, String reason, String notes);

    /**
     * Log registration assignment
     */
    void logRegistrationAssigned(Registration registration, User assignedTo, User assignedBy);

    /**
     * Log registration update
     */
    void logRegistrationUpdated(Registration registration, User updatedBy, String changes);

    /**
     * Log registration cancellation
     */
    void logRegistrationCancelled(Registration registration, User cancelledBy, String reason);

    /**
     * Log document upload
     */
    void logDocumentUploaded(UUID registrationId, String documentType, String fileName, User uploadedBy);

    /**
     * Log document verification
     */
    void logDocumentVerified(UUID registrationId, String documentType, User verifiedBy, boolean verified);

    /**
     * Log security event
     */
    void logSecurityEvent(String eventType, String description, User user, String ipAddress);

    /**
     * Log system event
     */
    void logSystemEvent(String eventType, String description, String additionalData);

    /**
     * Log user action
     */
    void logUserAction(User user, String action, String target, String details);
}
