package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for security event operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventRequest {

    private String eventType; // THREAT_DETECTED, INTRUSION_ATTEMPT, POLICY_VIOLATION

    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String status; // OPEN, INVESTIGATING, RESOLVED, CLOSED

    private String title;

    private String description;

    private String sourceIp;

    private String targetResource;

    private UUID userId;

    private String username;

    private String attackVector; // SQL_INJECTION, XSS, BRUTE_FORCE, MALWARE

    private String threatIndicators; // JSON array of IOCs

    private String impactAssessment;

    private String mitigationActions; // JSON array of actions taken

    private Boolean falsePositive;

    private Double confidenceScore; // 0.0 to 1.0

    private Integer riskScore; // 1 to 100

    private UUID assignedTo;

    private String assignedTeam;

    private Boolean escalated;

    private LocalDateTime escalatedAt;

    private LocalDateTime resolvedAt;

    private String resolutionNotes;

    private String complianceImpact; // JSON array of affected compliance frameworks

    private Boolean notificationSent;

    private String externalReference; // Reference to external security tools

    private String correlationId;

    private String detectionMethod; // AUTOMATED, MANUAL, EXTERNAL_ALERT

    private String evidenceCollected; // JSON array of evidence

    private LocalDateTime timestamp;

    private UUID createdBy;

    private UUID updatedBy;

    private String sourceSystem;

    private String geolocation;

    private String deviceId;

    private String networkSegment;

    private String userAgent;

    private String sessionId;

    private String requestId;

    private String businessContext;

    private String technicalDetails;

    private String remediationRecommendations; // JSON array of recommended actions

    private Boolean automatedResponse; // Whether automated response was triggered

    private String responseActions; // JSON array of automated actions taken

    private String threatIntelligence; // Related threat intelligence data

    private String vulnerabilityReferences; // Related vulnerability IDs

    private String attackTimeline; // Timeline of the attack

    private String affectedSystems; // JSON array of affected systems

    private String dataCompromised; // Information about compromised data

    private String recoveryActions; // Actions taken for recovery

    private String lessonsLearned;

    private String tags; // JSON array of tags for categorization

    private String metadata; // Additional metadata as JSON

    /**
     * Validation method to check if the request is valid
     */
    public boolean isValid() {
        return eventType != null && !eventType.trim().isEmpty() &&
               severity != null && !severity.trim().isEmpty() &&
               timestamp != null;
    }

    /**
     * Check if this is a high-severity event
     */
    public boolean isHighSeverity() {
        return "HIGH".equals(severity) || "CRITICAL".equals(severity);
    }

    /**
     * Check if this is a critical event
     */
    public boolean isCritical() {
        return "CRITICAL".equals(severity);
    }

    /**
     * Check if this event requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return isCritical() ||
               Boolean.TRUE.equals(escalated) ||
               (riskScore != null && riskScore >= 80);
    }

    /**
     * Check if this event involves a security threat
     */
    public boolean involvesSecurityThreat() {
        return "THREAT_DETECTED".equals(eventType) ||
               "INTRUSION_ATTEMPT".equals(eventType) ||
               "MALWARE_DETECTED".equals(eventType);
    }

    /**
     * Check if automated response should be triggered
     */
    public boolean shouldTriggerAutomatedResponse() {
        return isCritical() ||
               involvesSecurityThreat() ||
               (confidenceScore != null && confidenceScore >= 0.8);
    }

    /**
     * Check if notifications should be sent
     */
    public boolean shouldSendNotifications() {
        return isHighSeverity() ||
               requiresImmediateAttention() ||
               Boolean.TRUE.equals(escalated);
    }

    /**
     * Get estimated response time in minutes based on severity
     */
    public int getEstimatedResponseTimeMinutes() {
        switch (severity.toUpperCase()) {
            case "CRITICAL": return 5;    // 5 minutes
            case "HIGH": return 30;       // 30 minutes
            case "MEDIUM": return 120;    // 2 hours
            case "LOW": return 480;       // 8 hours
            default: return 480;
        }
    }

    /**
     * Get risk level for audit logging
     */
    public String getRiskLevel() {
        return severity; // Use severity as risk level
    }

    /**
     * Get IP address for audit logging
     */
    public String getIpAddress() {
        return sourceIp;
    }

    /**
     * Get additional details for audit logging
     */
    public String getDetails() {
        return technicalDetails;
    }
}
