package ph.gov.dsr.analytics.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Report entity for storing report configurations and generated reports
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Entity
@Table(name = "reports", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class Report {

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
    private ReportType reportType = ReportType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ReportCategory category = ReportCategory.GENERAL;

    @Column(name = "target_role", length = 50)
    private String targetRole; // ADMIN, DSWD_STAFF, LGU_STAFF, etc.

    @Column(name = "data_source", length = 100)
    private String dataSource; // Database, API, File, etc.

    @Column(name = "query_config", columnDefinition = "JSONB")
    private String queryConfig; // JSON configuration for data queries

    @Column(name = "filter_config", columnDefinition = "JSONB")
    private String filterConfig; // JSON configuration for filters

    @Column(name = "format_config", columnDefinition = "JSONB")
    private String formatConfig; // JSON configuration for formatting

    @Column(name = "schedule_config", columnDefinition = "JSONB")
    private String scheduleConfig; // JSON configuration for scheduling

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 20)
    private ReportFrequency frequency = ReportFrequency.ON_DEMAND;

    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;

    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.ACTIVE;

    @Column(name = "is_template")
    private Boolean isTemplate = false;

    @Column(name = "template_id", columnDefinition = "UUID")
    private UUID templateId;

    @Column(name = "output_format", length = 20)
    private String outputFormat = "PDF"; // PDF, EXCEL, CSV, JSON, etc.

    @Column(name = "file_path", length = 500)
    private String filePath; // Path to generated report file

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "generation_time_ms")
    private Long generationTimeMs;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "column_count")
    private Integer columnCount;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "access_level", length = 30)
    private String accessLevel = "INTERNAL"; // PUBLIC, INTERNAL, RESTRICTED, CONFIDENTIAL

    @Column(name = "allowed_roles", columnDefinition = "TEXT")
    private String allowedRoles; // JSON array of allowed roles

    @Column(name = "allowed_users", columnDefinition = "TEXT")
    private String allowedUsers; // JSON array of allowed user IDs

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "last_downloaded")
    private LocalDateTime lastDownloaded;

    @Column(name = "email_enabled")
    private Boolean emailEnabled = false;

    @Column(name = "email_recipients", columnDefinition = "TEXT")
    private String emailRecipients; // JSON array of email addresses

    @Column(name = "email_subject", length = 200)
    private String emailSubject;

    @Column(name = "email_body", columnDefinition = "TEXT")
    private String emailBody;

    @Column(name = "retention_days")
    private Integer retentionDays = 90; // Default 90 days

    @Column(name = "auto_delete_enabled")
    private Boolean autoDeleteEnabled = true;

    @Column(name = "compression_enabled")
    private Boolean compressionEnabled = false;

    @Column(name = "encryption_enabled")
    private Boolean encryptionEnabled = false;

    @Column(name = "watermark_enabled")
    private Boolean watermarkEnabled = false;

    @Column(name = "watermark_text", length = 100)
    private String watermarkText;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "parent_report_id", columnDefinition = "UUID")
    private UUID parentReportId;

    @Column(name = "parameters", columnDefinition = "JSONB")
    private String parameters; // JSON object for report parameters

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array of tags

    @Column(name = "metadata", columnDefinition = "JSONB")
    private String metadata;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "warning_count")
    private Integer warningCount = 0;

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

    // Additional fields for BusinessIntelligenceService compatibility
    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "configuration", columnDefinition = "JSONB")
    private String configuration;

    @Column(name = "report_data", columnDefinition = "JSONB")
    private String data;

    @Column(name = "visualizations", columnDefinition = "JSONB")
    private String visualizations;

    // Convenience methods for BusinessIntelligenceService compatibility
    public void setName(String name) {
        this.reportName = name;
    }

    public String getName() {
        return this.reportName;
    }

    public void setConfiguration(Object configuration) {
        // Convert object to JSON string if needed
        if (configuration instanceof String) {
            this.configuration = (String) configuration;
        } else {
            // In a real implementation, you'd use Jackson or similar to serialize
            this.configuration = configuration != null ? configuration.toString() : null;
        }
    }

    public void setData(Object data) {
        // Convert object to JSON string if needed
        if (data instanceof String) {
            this.data = (String) data;
        } else {
            // In a real implementation, you'd use Jackson or similar to serialize
            this.data = data != null ? data.toString() : null;
        }
    }

    public void setVisualizations(Object visualizations) {
        // Convert object to JSON string if needed
        if (visualizations instanceof String) {
            this.visualizations = (String) visualizations;
        } else {
            // In a real implementation, you'd use Jackson or similar to serialize
            this.visualizations = visualizations != null ? visualizations.toString() : null;
        }
    }

    // Enums
    public enum ReportType {
        STANDARD("Standard Report"),
        CUSTOM("Custom Report"),
        DASHBOARD("Dashboard Report"),
        SCHEDULED("Scheduled Report"),
        AD_HOC("Ad-hoc Report"),
        DRILL_DOWN("Drill-down Report"),
        SUMMARY("Summary Report"),
        DETAILED("Detailed Report"),
        COMPARATIVE("Comparative Report"),
        TREND("Trend Analysis Report");

        private final String description;

        ReportType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ReportCategory {
        GENERAL("General Purpose"),
        REGISTRATION("Registration Reports"),
        ELIGIBILITY("Eligibility Reports"),
        PAYMENT("Payment Reports"),
        GRIEVANCE("Grievance Reports"),
        INTEROPERABILITY("Interoperability Reports"),
        SYSTEM_PERFORMANCE("System Performance Reports"),
        USER_ACTIVITY("User Activity Reports"),
        DATA_QUALITY("Data Quality Reports"),
        COMPLIANCE("Compliance Reports"),
        FINANCIAL("Financial Reports"),
        OPERATIONAL("Operational Reports"),
        STATISTICAL("Statistical Reports"),
        AUDIT("Audit Reports");

        private final String description;

        ReportCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ReportFrequency {
        ON_DEMAND("On Demand"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        QUARTERLY("Quarterly"),
        YEARLY("Yearly"),
        CUSTOM("Custom Schedule");

        private final String description;

        ReportFrequency(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ReportStatus {
        ACTIVE("Report is active"),
        INACTIVE("Report is inactive"),
        DRAFT("Report is in draft"),
        GENERATING("Report is being generated"),
        COMPLETED("Report generation completed"),
        FAILED("Report generation failed"),
        SCHEDULED("Report is scheduled"),
        ARCHIVED("Report is archived");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public Report() {}

    public Report(String reportCode, String reportName, ReportType reportType, ReportCategory category) {
        this.reportCode = reportCode;
        this.reportName = reportName;
        this.reportType = reportType;
        this.category = category;
    }

    // Helper methods
    
    /**
     * Check if report is active
     */
    public boolean isActive() {
        return ReportStatus.ACTIVE.equals(status);
    }

    /**
     * Check if report is scheduled
     */
    public boolean isScheduled() {
        return !ReportFrequency.ON_DEMAND.equals(frequency);
    }

    /**
     * Check if report is due for generation
     */
    public boolean isDueForGeneration() {
        if (!isScheduled() || nextRunTime == null) {
            return false;
        }
        return nextRunTime.isBefore(LocalDateTime.now());
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
     * Check if report is accessible by user
     */
    public boolean isAccessibleByUser(String userId) {
        if (allowedUsers == null) {
            return true; // No user restrictions
        }
        return allowedUsers.contains(userId);
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        this.downloadCount = (downloadCount != null ? downloadCount : 0) + 1;
        this.lastDownloaded = LocalDateTime.now();
    }

    /**
     * Mark as completed
     */
    public void markAsCompleted(String filePath, Long fileSizeBytes, Long generationTimeMs) {
        this.status = ReportStatus.COMPLETED;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.generationTimeMs = generationTimeMs;
        this.lastRunTime = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Mark as failed
     */
    public void markAsFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.lastRunTime = LocalDateTime.now();
    }

    /**
     * Check if report file exists
     */
    public boolean hasFile() {
        return filePath != null && !filePath.trim().isEmpty();
    }

    /**
     * Check if report is expired (based on retention policy)
     */
    public boolean isExpired() {
        if (retentionDays == null || lastRunTime == null) {
            return false;
        }
        return lastRunTime.plusDays(retentionDays).isBefore(LocalDateTime.now());
    }

    /**
     * Check if report should be auto-deleted
     */
    public boolean shouldAutoDelete() {
        return Boolean.TRUE.equals(autoDeleteEnabled) && isExpired();
    }

    /**
     * Check if report supports email delivery
     */
    public boolean supportsEmailDelivery() {
        return Boolean.TRUE.equals(emailEnabled) && emailRecipients != null;
    }

    /**
     * Check if report is encrypted
     */
    public boolean isEncrypted() {
        return Boolean.TRUE.equals(encryptionEnabled);
    }

    /**
     * Check if report is compressed
     */
    public boolean isCompressed() {
        return Boolean.TRUE.equals(compressionEnabled);
    }

    /**
     * Check if report has watermark
     */
    public boolean hasWatermark() {
        return Boolean.TRUE.equals(watermarkEnabled);
    }

    /**
     * Get report age in days
     */
    public long getReportAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Check if report is recently created (within 7 days)
     */
    public boolean isRecentlyCreated() {
        return getReportAgeInDays() <= 7;
    }

    /**
     * Check if report is frequently downloaded (more than 50 times)
     */
    public boolean isFrequentlyDownloaded() {
        return downloadCount != null && downloadCount > 50;
    }

    /**
     * Get download frequency level
     */
    public String getDownloadFrequency() {
        if (downloadCount == null || downloadCount == 0) {
            return "UNUSED";
        } else if (downloadCount < 5) {
            return "LOW";
        } else if (downloadCount < 20) {
            return "MEDIUM";
        } else if (downloadCount < 50) {
            return "HIGH";
        } else {
            return "VERY_HIGH";
        }
    }

    /**
     * Get file size in human readable format
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes == null) {
            return "Unknown";
        }
        
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else if (fileSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get generation time in human readable format
     */
    public String getFormattedGenerationTime() {
        if (generationTimeMs == null) {
            return "Unknown";
        }
        
        if (generationTimeMs < 1000) {
            return generationTimeMs + " ms";
        } else if (generationTimeMs < 60000) {
            return String.format("%.1f s", generationTimeMs / 1000.0);
        } else {
            return String.format("%.1f min", generationTimeMs / 60000.0);
        }
    }
}
