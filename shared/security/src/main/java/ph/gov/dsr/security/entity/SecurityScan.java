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
 * SecurityScan entity for tracking security scans and assessments
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Entity
@Table(name = "security_scans", schema = "dsr_security")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityScan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "scan_id", unique = true, nullable = false, length = 100)
    private String scanId;

    @NotBlank
    @Column(name = "scan_type", nullable = false, length = 50)
    private String scanType; // VULNERABILITY, PENETRATION, CODE_QUALITY, NETWORK

    @NotBlank
    @Column(name = "scan_tool", nullable = false, length = 100)
    private String scanTool; // OWASP_ZAP, NESSUS, SONARQUBE, CUSTOM

    @Column(name = "target_type", length = 50)
    private String targetType; // WEB_APPLICATION, NETWORK, CODE_REPOSITORY

    @Column(name = "target_identifier", length = 255)
    private String targetIdentifier; // URL, IP range, repository name

    @Column(name = "scan_profile", length = 100)
    private String scanProfile; // QUICK, FULL, CUSTOM

    @NotBlank
    @Column(name = "status", nullable = false, length = 50)
    private String status; // SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Column(name = "initiated_by", columnDefinition = "UUID")
    private UUID initiatedBy;

    @Column(name = "scheduled", nullable = false)
    @Builder.Default
    private Boolean scheduled = false;

    @Column(name = "recurring", nullable = false)
    @Builder.Default
    private Boolean recurring = false;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "next_run_date")
    private LocalDateTime nextRunDate;

    @Column(name = "scan_configuration", columnDefinition = "TEXT")
    private String scanConfiguration; // JSON configuration

    @Column(name = "total_findings")
    private Integer totalFindings;

    @Column(name = "critical_findings")
    private Integer criticalFindings;

    @Column(name = "high_findings")
    private Integer highFindings;

    @Column(name = "medium_findings")
    private Integer mediumFindings;

    @Column(name = "low_findings")
    private Integer lowFindings;

    @Column(name = "info_findings")
    private Integer infoFindings;

    @Column(name = "false_positives")
    private Integer falsePositives;

    @Column(name = "scan_results", columnDefinition = "TEXT")
    private String scanResults; // JSON results

    @Column(name = "raw_output", columnDefinition = "TEXT")
    private String rawOutput;

    @Column(name = "report_generated", nullable = false)
    @Builder.Default
    private Boolean reportGenerated = false;

    @Column(name = "report_path", length = 500)
    private String reportPath;

    @Column(name = "baseline_scan", nullable = false)
    @Builder.Default
    private Boolean baselineScan = false;

    @Column(name = "baseline_scan_id", columnDefinition = "UUID")
    private UUID baselineScanId;

    @Column(name = "comparison_results", columnDefinition = "TEXT")
    private String comparisonResults; // JSON comparison with baseline

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "scan_engine_version", length = 100)
    private String scanEngineVersion;

    @Column(name = "scan_rules_version", length = 100)
    private String scanRulesVersion;

    @Column(name = "compliance_frameworks", length = 500)
    private String complianceFrameworks; // JSON array of frameworks

    @Column(name = "tags", length = 500)
    private String tags; // JSON array of tags

    @Column(name = "priority", length = 20)
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

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
     * Pre-persist hook to set default values
     */
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "SCHEDULED";
        }
        if (progressPercentage == null) {
            progressPercentage = 0;
        }
        if (priority == null) {
            priority = "MEDIUM";
        }
        if (totalFindings == null) {
            totalFindings = 0;
        }
    }
}
