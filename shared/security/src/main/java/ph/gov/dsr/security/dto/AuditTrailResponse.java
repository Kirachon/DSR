package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for audit trail operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailResponse {

    private UUID userId;
    private String dateRange;
    private List<AuditTrailEntry> entries;
    private Long totalCount;
    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
    private LocalDateTime generatedAt;
    private String reportId;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditTrailEntry {
        private UUID id;
        private String eventType;
        private String eventCategory;
        private UUID userId;
        private String username;
        private String userRole;
        private String resourceType;
        private String resourceId;
        private String action;
        private String result;
        private String riskLevel;
        private String ipAddress;
        private String sessionId;
        private String correlationId;
        private LocalDateTime timestamp;
        private String details;
        private String complianceFlags;
    }
}
