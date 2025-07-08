package ph.gov.dsr.security.entity;

/**
 * Enumeration for security event types
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public enum SecurityEventType {
    
    // Authentication Events
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    PASSWORD_CHANGE,
    PASSWORD_RESET,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    MFA_ENABLED,
    MFA_DISABLED,
    MFA_SUCCESS,
    MFA_FAILURE,
    
    // Authorization Events
    ACCESS_GRANTED,
    ACCESS_DENIED,
    PRIVILEGE_ESCALATION,
    ROLE_CHANGE,
    PERMISSION_CHANGE,
    
    // Data Access Events
    DATA_ACCESS,
    DATA_EXPORT,
    DATA_IMPORT,
    DATA_MODIFICATION,
    DATA_DELETION,
    SENSITIVE_DATA_ACCESS,
    
    // Security Threats
    THREAT_DETECTED,
    INTRUSION_ATTEMPT,
    MALWARE_DETECTED,
    SUSPICIOUS_ACTIVITY,
    BRUTE_FORCE_ATTACK,
    SQL_INJECTION_ATTEMPT,
    XSS_ATTEMPT,
    CSRF_ATTEMPT,
    
    // System Events
    SYSTEM_START,
    SYSTEM_SHUTDOWN,
    SERVICE_START,
    SERVICE_STOP,
    CONFIGURATION_CHANGE,
    SOFTWARE_UPDATE,
    
    // Compliance Events
    POLICY_VIOLATION,
    COMPLIANCE_CHECK,
    AUDIT_LOG_ACCESS,
    DATA_RETENTION_ACTION,
    
    // Network Events
    NETWORK_CONNECTION,
    NETWORK_DISCONNECTION,
    FIREWALL_BLOCK,
    VPN_CONNECTION,
    VPN_DISCONNECTION,
    
    // Administrative Events
    ADMIN_LOGIN,
    ADMIN_ACTION,
    USER_CREATION,
    USER_DELETION,
    USER_MODIFICATION,
    BACKUP_CREATED,
    BACKUP_RESTORED,
    
    // Vulnerability Events
    VULNERABILITY_DISCOVERED,
    VULNERABILITY_FIXED,
    VULNERABILITY_SCAN,
    SECURITY_SCAN_STARTED,
    SECURITY_SCAN_COMPLETED,
    
    // Incident Response
    INCIDENT_CREATED,
    INCIDENT_ESCALATED,
    INCIDENT_RESOLVED,
    INCIDENT_CLOSED,
    
    // Other
    UNKNOWN,
    CUSTOM
}
