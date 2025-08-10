package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dashboard Update DTO for real-time dashboard updates
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class DashboardUpdate {
    private UUID dashboardId;
    private String updateType; // FULL_REFRESH, WIDGET_UPDATE, DATA_UPDATE, CONFIG_UPDATE
    private LocalDateTime timestamp;
    private String triggeredBy; // USER, SCHEDULE, EVENT, ALERT
    private String userId;
    
    // Update scope
    private Boolean isFullUpdate;
    private Boolean isPartialUpdate;
    private UUID widgetId;
    private String widgetType;
    private String updateScope; // DASHBOARD, WIDGET, DATA, METADATA
    
    // Update data
    private Map<String, Object> updatedData;
    private Map<String, Object> previousData;
    private Map<String, Object> deltaData;
    private List<String> updatedFields;
    
    // Performance metrics
    private Long updateDuration;
    private Long dataFetchTime;
    private Long processingTime;
    private Long renderTime;
    private Integer affectedWidgets;
    private Integer updatedRecords;
    
    // Status information
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    private String statusMessage;
    private Double progressPercentage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime estimatedCompletion;
    
    // Error handling
    private String errorMessage;
    private String errorCode;
    private Boolean hasErrors;
    private List<String> errors;
    private List<String> warnings;
    
    // Cache information
    private Boolean cacheInvalidated;
    private Boolean cacheUpdated;
    private String cacheStatus;
    private LocalDateTime cacheExpiry;
    
    // Notification information
    private Boolean notifyUsers;
    private List<String> notificationRecipients;
    private String notificationMessage;
    private Boolean alertTriggered;
    
    // Metadata
    private Map<String, Object> metadata;
    private String version;
    private String updateSource;
    private Map<String, Object> updateContext;
}
