package ph.gov.dsr.security.audit;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ph.gov.dsr.security.dpa.DataClassification;
import ph.gov.dsr.common.audit.AuditEvent;
import ph.gov.dsr.common.audit.AuditEventType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Security Audit Service for DPA Compliance
 * 
 * Provides comprehensive audit logging for data access, processing,
 * and security events in compliance with Philippine Data Privacy Act.
 */
@Service
public class SecurityAuditService {
    
    /**
     * Logs data access events for DPA compliance
     */
    public void logDataAccess(String dataType, DataClassification classification, 
                             String operation, Object dataId, Map<String, Object> additionalInfo) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "SYSTEM";
        String userRole = auth != null && !auth.getAuthorities().isEmpty() 
            ? auth.getAuthorities().iterator().next().getAuthority() : "UNKNOWN";
        
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("dataType", dataType);
        auditData.put("classification", classification.name());
        auditData.put("classificationLevel", classification.getLevel());
        auditData.put("operation", operation);
        auditData.put("dataId", dataId);
        auditData.put("userId", userId);
        auditData.put("userRole", userRole);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("ipAddress", getCurrentUserIP());
        auditData.put("userAgent", getCurrentUserAgent());
        
        if (additionalInfo != null) {
            auditData.putAll(additionalInfo);
        }
        
        // Create audit event
        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.DATA_ACCESS)
            .userId(userId)
            .resourceType(dataType)
            .resourceId(dataId != null ? dataId.toString() : null)
            .action(operation)
            .timestamp(LocalDateTime.now())
            .metadata(auditData)
            .classification(classification.name())
            .build();
        
        // Log to audit system
        logAuditEvent(event);
        
        // Additional logging for sensitive data
        if (classification.isSensitivePersonalData()) {
            logSensitiveDataAccess(event);
        }
    }
    
    /**
     * Logs authentication events
     */
    public void logAuthenticationEvent(String eventType, String userId, boolean success, 
                                     String reason, Map<String, Object> additionalInfo) {
        
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("eventType", eventType);
        auditData.put("userId", userId);
        auditData.put("success", success);
        auditData.put("reason", reason);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("ipAddress", getCurrentUserIP());
        auditData.put("userAgent", getCurrentUserAgent());
        
        if (additionalInfo != null) {
            auditData.putAll(additionalInfo);
        }
        
        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.AUTHENTICATION)
            .userId(userId)
            .action(eventType)
            .timestamp(LocalDateTime.now())
            .metadata(auditData)
            .classification(DataClassification.CONFIDENTIAL.name())
            .build();
        
        logAuditEvent(event);
    }
    
    /**
     * Logs security violations
     */
    public void logSecurityViolation(String violationType, String description, 
                                   String userId, Map<String, Object> additionalInfo) {
        
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("violationType", violationType);
        auditData.put("description", description);
        auditData.put("userId", userId);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("ipAddress", getCurrentUserIP());
        auditData.put("userAgent", getCurrentUserAgent());
        auditData.put("severity", "HIGH");
        
        if (additionalInfo != null) {
            auditData.putAll(additionalInfo);
        }
        
        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.SECURITY_VIOLATION)
            .userId(userId)
            .action(violationType)
            .timestamp(LocalDateTime.now())
            .metadata(auditData)
            .classification(DataClassification.HIGHLY_RESTRICTED.name())
            .build();
        
        logAuditEvent(event);
        
        // Alert security team for high-severity violations
        alertSecurityTeam(event);
    }
    
    /**
     * Logs consent management events
     */
    public void logConsentEvent(String citizenId, String consentType, String action, 
                               boolean granted, Map<String, Object> additionalInfo) {
        
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("citizenId", citizenId);
        auditData.put("consentType", consentType);
        auditData.put("action", action);
        auditData.put("granted", granted);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("ipAddress", getCurrentUserIP());
        
        if (additionalInfo != null) {
            auditData.putAll(additionalInfo);
        }
        
        AuditEvent event = AuditEvent.builder()
            .eventType(AuditEventType.CONSENT_MANAGEMENT)
            .userId(citizenId)
            .action(action)
            .timestamp(LocalDateTime.now())
            .metadata(auditData)
            .classification(DataClassification.RESTRICTED.name())
            .build();
        
        logAuditEvent(event);
    }
    
    private void logAuditEvent(AuditEvent event) {
        // Implementation would integrate with centralized logging system
        // For now, log to application logs with structured format
        System.out.println("AUDIT: " + event.toString());
    }
    
    private void logSensitiveDataAccess(AuditEvent event) {
        // Additional logging for sensitive personal data access
        // This could integrate with SIEM systems or specialized audit tools
        System.out.println("SENSITIVE_DATA_AUDIT: " + event.toString());
    }
    
    private void alertSecurityTeam(AuditEvent event) {
        // Implementation would send alerts to security team
        // Could integrate with email, Slack, or incident management systems
        System.out.println("SECURITY_ALERT: " + event.toString());
    }
    
    private String getCurrentUserIP() {
        // Implementation would extract IP from request context
        return "127.0.0.1"; // Placeholder
    }
    
    private String getCurrentUserAgent() {
        // Implementation would extract User-Agent from request context
        return "DSR-Client/3.0.0"; // Placeholder
    }
}
