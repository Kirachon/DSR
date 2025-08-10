package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for security events summary
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventsSummaryRequest {

    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String groupBy; // SEVERITY, EVENT_TYPE, STATUS, HOUR, DAY, WEEK
    private String filterBySeverity;
    private String filterByEventType;
    private String filterByStatus;
    private Boolean includeResolved;
    private Boolean includeFalsePositives;
    
    /**
     * Validation method
     */
    public boolean isValid() {
        return startDate != null && endDate != null && 
               startDate.isBefore(endDate);
    }
}
