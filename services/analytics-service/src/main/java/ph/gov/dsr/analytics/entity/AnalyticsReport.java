package ph.gov.dsr.analytics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Analytics Report entity for storing generated reports and dashboards
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Entity
@Table(name = "analytics_reports", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class AnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "report_code", unique = true, nullable = false, length = 50)
    private String reportCode;

    @NotBlank
    @Column(name = "report_name", nullable = false, length = 200)
    private String reportName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ReportCategory category;

    @Column(name = "data_sources", columnDefinition = "TEXT")
    private String dataSources; // JSON array of data sources

    @Column(name = "parameters", columnDefinition = "JSONB")
    private String parameters;

    @Column(name = "filters", columnDefinition = "JSONB")
    private String filters;

    @Column(name = "aggregations", columnDefinition = "JSONB")
    private String aggregations;

    @Column(name = "visualizations", columnDefinition = "JSONB")
    private String visualizations;

    @Column(name = "report_data", columnDefinition = "JSONB")
    private String reportData;

    @Column(name = "summary_statistics", columnDefinition = "JSONB")
    private String summaryStatistics;

    @NotNull
    @Column(name = "generation_date", nullable = false)
    private LocalDateTime generationDate = LocalDateTime.now();

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.GENERATED;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(name = "generation_time_ms")
    private Long generationTimeMs;

    @Column(name = "record_count")
    private Long recordCount;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "file_format", length = 20)
    private String fileFormat; // PDF, EXCEL, CSV, JSON

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "schedule_expression", length = 100)
    private String scheduleExpression; // Cron expression

    @Column(name = "next_generation_date")
    private LocalDateTime nextGenerationDate;

    @Column(name = "retention_days")
    private Integer retentionDays = 90;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "access_level", length = 30)
    private String accessLevel = "INTERNAL"; // PUBLIC, INTERNAL, RESTRICTED, CONFIDENTIAL

    @Column(name = "allowed_roles", columnDefinition = "TEXT")
    private String allowedRoles; // JSON array of allowed roles

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array of tags

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Enums
    public enum ReportType {
        DASHBOARD,
        SUMMARY_REPORT,
        DETAILED_REPORT,
        TREND_ANALYSIS,
        COMPARATIVE_ANALYSIS,
        KPI_REPORT,
        OPERATIONAL_REPORT,
        COMPLIANCE_REPORT,
        FINANCIAL_REPORT,
        PERFORMANCE_REPORT,
        CUSTOM_REPORT
    }

    public enum ReportCategory {
        REGISTRATION,
        ELIGIBILITY,
        PAYMENT,
        GRIEVANCE,
        INTEROPERABILITY,
        SYSTEM_PERFORMANCE,
        USER_ACTIVITY,
        DATA_QUALITY,
        COMPLIANCE,
        FINANCIAL,
        OPERATIONAL,
        STRATEGIC
    }

    public enum ReportStatus {
        GENERATING,
        GENERATED,
        FAILED,
        EXPIRED,
        ARCHIVED
    }

    // Constructors
    public AnalyticsReport() {}

    public AnalyticsReport(String reportCode, String reportName, ReportType reportType, ReportCategory category) {
        this.reportCode = reportCode;
        this.reportName = reportName;
        this.reportType = reportType;
        this.category = category;
    }

    // Helper methods
    
    /**
     * Check if report is expired
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    /**
     * Check if report is accessible by role
     */
    public boolean isAccessibleByRole(String role) {
        if (allowedRoles == null) {
            return true; // No restrictions
        }
        return allowedRoles.contains(role);
    }

    /**
     * Calculate report age in days
     */
    public long getReportAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(generationDate, LocalDateTime.now());
    }

    /**
     * Check if report needs regeneration
     */
    public boolean needsRegeneration() {
        return isScheduled && nextGenerationDate != null && 
               nextGenerationDate.isBefore(LocalDateTime.now());
    }

    /**
     * Mark as accessed
     */
    public void markAsAccessed() {
        this.lastAccessed = LocalDateTime.now();
        this.downloadCount = (downloadCount != null ? downloadCount : 0) + 1;
    }

    /**
     * Set expiry date based on retention period
     */
    public void setExpiryFromRetention() {
        if (retentionDays != null && retentionDays > 0) {
            this.expiryDate = generationDate.plusDays(retentionDays);
        }
    }

    /**
     * Get file size in MB
     */
    public Double getFileSizeMB() {
        if (fileSizeBytes == null) {
            return null;
        }
        return fileSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * Get generation time in seconds
     */
    public Double getGenerationTimeSeconds() {
        if (generationTimeMs == null) {
            return null;
        }
        return generationTimeMs / 1000.0;
    }

    /**
     * Get report summary
     */
    public String getReportSummary() {
        return String.format("%s (%s) - %s records, generated on %s", 
                reportName, reportType, recordCount, 
                generationDate.toString());
    }
}
