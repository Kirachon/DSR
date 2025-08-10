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
 * Dashboard entity for storing dashboard configurations and layouts
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Entity
@Table(name = "dashboards", schema = "dsr_core")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = false)
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @NotBlank
    @Column(name = "dashboard_code", unique = true, nullable = false, length = 50)
    private String dashboardCode;

    @NotBlank
    @Column(name = "dashboard_name", nullable = false, length = 200)
    private String dashboardName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dashboard_type", nullable = false, length = 30)
    private DashboardType dashboardType = DashboardType.OPERATIONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private DashboardCategory category = DashboardCategory.GENERAL;

    @Column(name = "target_role", length = 50)
    private String targetRole; // ADMIN, DSWD_STAFF, LGU_STAFF, etc.

    @Column(name = "layout_config", columnDefinition = "JSONB")
    private String layoutConfig; // JSON configuration for dashboard layout

    @Column(name = "widget_config", columnDefinition = "JSONB")
    private String widgetConfig; // JSON configuration for widgets

    @Column(name = "filter_config", columnDefinition = "JSONB")
    private String filterConfig; // JSON configuration for filters

    @Column(name = "refresh_interval_seconds")
    private Integer refreshIntervalSeconds = 300; // 5 minutes default

    @Column(name = "auto_refresh_enabled")
    private Boolean autoRefreshEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DashboardStatus status = DashboardStatus.ACTIVE;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "access_level", length = 30)
    private String accessLevel = "INTERNAL"; // PUBLIC, INTERNAL, RESTRICTED, CONFIDENTIAL

    @Column(name = "allowed_roles", columnDefinition = "TEXT")
    private String allowedRoles; // JSON array of allowed roles

    @Column(name = "allowed_users", columnDefinition = "TEXT")
    private String allowedUsers; // JSON array of allowed user IDs

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "last_viewed")
    private LocalDateTime lastViewed;

    @Column(name = "last_refreshed")
    private LocalDateTime lastRefreshed;

    @Column(name = "cache_enabled")
    private Boolean cacheEnabled = true;

    @Column(name = "cache_duration_seconds")
    private Integer cacheDurationSeconds = 300;

    @Column(name = "export_enabled")
    private Boolean exportEnabled = true;

    @Column(name = "export_formats", columnDefinition = "TEXT")
    private String exportFormats; // JSON array of supported export formats

    @Column(name = "drill_down_enabled")
    private Boolean drillDownEnabled = false;

    @Column(name = "real_time_enabled")
    private Boolean realTimeEnabled = false;

    @Column(name = "alert_enabled")
    private Boolean alertEnabled = false;

    @Column(name = "alert_config", columnDefinition = "JSONB")
    private String alertConfig; // JSON configuration for alerts

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "parent_dashboard_id", columnDefinition = "UUID")
    private UUID parentDashboardId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

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
    public enum DashboardType {
        OPERATIONAL("Operational Dashboard"),
        STRATEGIC("Strategic Dashboard"),
        ANALYTICAL("Analytical Dashboard"),
        TACTICAL("Tactical Dashboard"),
        EXECUTIVE("Executive Dashboard"),
        KPI("KPI Dashboard"),
        REAL_TIME("Real-time Dashboard"),
        CUSTOM("Custom Dashboard");

        private final String description;

        DashboardType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DashboardCategory {
        GENERAL("General Purpose"),
        REGISTRATION("Registration Analytics"),
        ELIGIBILITY("Eligibility Analytics"),
        PAYMENT("Payment Analytics"),
        GRIEVANCE("Grievance Analytics"),
        INTEROPERABILITY("Interoperability Analytics"),
        SYSTEM_PERFORMANCE("System Performance"),
        USER_ACTIVITY("User Activity"),
        DATA_QUALITY("Data Quality"),
        COMPLIANCE("Compliance Monitoring"),
        FINANCIAL("Financial Analytics"),
        OPERATIONAL("Operational Metrics");

        private final String description;

        DashboardCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DashboardStatus {
        ACTIVE("Dashboard is active and available"),
        INACTIVE("Dashboard is temporarily inactive"),
        DRAFT("Dashboard is in draft mode"),
        ARCHIVED("Dashboard is archived"),
        MAINTENANCE("Dashboard is under maintenance");

        private final String description;

        DashboardStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public Dashboard() {}

    public Dashboard(String dashboardCode, String dashboardName, DashboardType dashboardType, DashboardCategory category) {
        this.dashboardCode = dashboardCode;
        this.dashboardName = dashboardName;
        this.dashboardType = dashboardType;
        this.category = category;
    }

    // Helper methods
    
    /**
     * Check if dashboard is active
     */
    public boolean isActive() {
        return DashboardStatus.ACTIVE.equals(status);
    }

    /**
     * Check if dashboard is accessible by role
     */
    public boolean isAccessibleByRole(String role) {
        if (allowedRoles == null) {
            return true; // No restrictions
        }
        return allowedRoles.contains(role);
    }

    /**
     * Check if dashboard is accessible by user
     */
    public boolean isAccessibleByUser(String userId) {
        if (allowedUsers == null) {
            return true; // No user restrictions
        }
        return allowedUsers.contains(userId);
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount = (viewCount != null ? viewCount : 0) + 1;
        this.lastViewed = LocalDateTime.now();
    }

    /**
     * Mark as refreshed
     */
    public void markAsRefreshed() {
        this.lastRefreshed = LocalDateTime.now();
    }

    /**
     * Check if dashboard needs refresh
     */
    public boolean needsRefresh() {
        if (!autoRefreshEnabled || refreshIntervalSeconds == null) {
            return false;
        }
        
        if (lastRefreshed == null) {
            return true;
        }
        
        return lastRefreshed.plusSeconds(refreshIntervalSeconds).isBefore(LocalDateTime.now());
    }

    /**
     * Check if dashboard is cached
     */
    public boolean isCached() {
        return Boolean.TRUE.equals(cacheEnabled);
    }

    /**
     * Check if cache is valid
     */
    public boolean isCacheValid() {
        if (!isCached() || cacheDurationSeconds == null || lastRefreshed == null) {
            return false;
        }
        
        return lastRefreshed.plusSeconds(cacheDurationSeconds).isAfter(LocalDateTime.now());
    }

    /**
     * Check if dashboard supports export
     */
    public boolean supportsExport() {
        return Boolean.TRUE.equals(exportEnabled);
    }

    /**
     * Check if dashboard supports drill down
     */
    public boolean supportsDrillDown() {
        return Boolean.TRUE.equals(drillDownEnabled);
    }

    /**
     * Check if dashboard is real-time
     */
    public boolean isRealTime() {
        return Boolean.TRUE.equals(realTimeEnabled);
    }

    /**
     * Check if dashboard has alerts enabled
     */
    public boolean hasAlertsEnabled() {
        return Boolean.TRUE.equals(alertEnabled);
    }

    /**
     * Get dashboard age in days
     */
    public long getDashboardAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Check if dashboard is recently created (within 7 days)
     */
    public boolean isRecentlyCreated() {
        return getDashboardAgeInDays() <= 7;
    }

    /**
     * Check if dashboard is frequently used (viewed more than 100 times)
     */
    public boolean isFrequentlyUsed() {
        return viewCount != null && viewCount > 100;
    }

    /**
     * Get usage frequency level
     */
    public String getUsageFrequency() {
        if (viewCount == null || viewCount == 0) {
            return "UNUSED";
        } else if (viewCount < 10) {
            return "LOW";
        } else if (viewCount < 50) {
            return "MEDIUM";
        } else if (viewCount < 100) {
            return "HIGH";
        } else {
            return "VERY_HIGH";
        }
    }
}
