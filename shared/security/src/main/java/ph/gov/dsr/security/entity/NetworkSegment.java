package ph.gov.dsr.security.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NetworkSegment entity for network segmentation and zero-trust architecture
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "network_segments", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "segment_name", unique = true, nullable = false, length = 100)
    private String segmentName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Column(name = "segment_type", nullable = false, length = 50)
    private String segmentType; // DMZ, INTERNAL, EXTERNAL, MANAGEMENT, GUEST

    @Column(name = "network_cidr", length = 50)
    private String networkCidr; // e.g., 192.168.1.0/24

    @Column(name = "vlan_id")
    private Integer vlanId;

    @Column(name = "subnet_mask", length = 15)
    private String subnetMask;

    @Column(name = "gateway_ip", length = 45)
    private String gatewayIp;

    @Column(name = "dns_servers", length = 500)
    private String dnsServers; // JSON array of DNS server IPs

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "trust_level", nullable = false)
    private TrustLevel trustLevel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "security_zone", nullable = false)
    private SecurityZone securityZone;

    @Column(name = "isolation_level", length = 20)
    private String isolationLevel; // NONE, PARTIAL, FULL

    @Column(name = "monitoring_enabled", nullable = false)
    @Builder.Default
    private Boolean monitoringEnabled = true;

    @Column(name = "logging_enabled", nullable = false)
    @Builder.Default
    private Boolean loggingEnabled = true;

    @Column(name = "intrusion_detection_enabled", nullable = false)
    @Builder.Default
    private Boolean intrusionDetectionEnabled = true;

    @Column(name = "firewall_rules", columnDefinition = "TEXT")
    private String firewallRules; // JSON array of firewall rules

    @Column(name = "access_control_list", columnDefinition = "TEXT")
    private String accessControlList; // JSON array of ACL rules

    @Column(name = "allowed_protocols", length = 500)
    private String allowedProtocols; // JSON array of allowed protocols

    @Column(name = "blocked_protocols", length = 500)
    private String blockedProtocols; // JSON array of blocked protocols

    @Column(name = "bandwidth_limit_mbps")
    private Integer bandwidthLimitMbps;

    @Column(name = "connection_limit")
    private Integer connectionLimit;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "encryption_required", nullable = false)
    @Builder.Default
    private Boolean encryptionRequired = false;

    @Column(name = "vpn_required", nullable = false)
    @Builder.Default
    private Boolean vpnRequired = false;

    @Column(name = "mfa_required", nullable = false)
    @Builder.Default
    private Boolean mfaRequired = false;

    @Column(name = "certificate_required", nullable = false)
    @Builder.Default
    private Boolean certificateRequired = false;

    @Column(name = "compliance_frameworks", length = 500)
    private String complianceFrameworks; // JSON array of compliance requirements

    @Column(name = "data_classification", length = 50)
    private String dataClassification; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED

    @Column(name = "business_criticality", length = 20)
    private String businessCriticality; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "maintenance_window", length = 100)
    private String maintenanceWindow; // e.g., "Sunday 02:00-04:00"

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_scan_date")
    private LocalDateTime lastScanDate;

    @Column(name = "last_vulnerability_assessment")
    private LocalDateTime lastVulnerabilityAssessment;

    @Column(name = "risk_score")
    private Integer riskScore; // 1-100

    @Column(name = "threat_indicators", columnDefinition = "TEXT")
    private String threatIndicators; // JSON array of current threats

    @Column(name = "incident_count")
    private Integer incidentCount;

    @Column(name = "last_incident_date")
    private LocalDateTime lastIncidentDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "UUID")
    private UUID updatedBy;

    /**
     * Security zones enumeration
     */
    public enum SecurityZone {
        PUBLIC,
        DMZ,
        INTERNAL,
        RESTRICTED,
        MANAGEMENT,
        GUEST
    }

    /**
     * Pre-persist hook to set default values
     */
    @PrePersist
    protected void onCreate() {
        if (trustLevel == null) {
            trustLevel = TrustLevel.LOW;
        }
        if (securityZone == null) {
            securityZone = SecurityZone.INTERNAL;
        }
        if (isolationLevel == null) {
            isolationLevel = "PARTIAL";
        }
        if (sessionTimeoutMinutes == null) {
            sessionTimeoutMinutes = 60;
        }
        if (riskScore == null) {
            riskScore = 50;
        }
        if (incidentCount == null) {
            incidentCount = 0;
        }
    }
}
