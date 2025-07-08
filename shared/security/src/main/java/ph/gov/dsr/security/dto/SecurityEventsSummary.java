package ph.gov.dsr.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for security events summary
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventsSummary {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;
    
    private Long totalEvents;
    private Long openEvents;
    private Long resolvedEvents;
    private Long criticalEvents;
    private Long highSeverityEvents;
    private Long falsePositives;
    
    private Map<String, Long> eventsBySeverity;
    private Map<String, Long> eventsByType;
    private Map<String, Long> eventsByStatus;
    private List<TrendData> trends;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private LocalDateTime timestamp;
        private String category;
        private Long count;
        private String period; // HOUR, DAY, WEEK
    }
}
