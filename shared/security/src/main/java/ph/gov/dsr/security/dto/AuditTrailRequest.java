package ph.gov.dsr.security.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for audit trail operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailRequest {

    private UUID userId;
    private String username;
    private String eventType;
    private String eventCategory;
    private String resourceType;
    private String resourceId;
    private String action;
    private String result;
    private String riskLevel;
    private String ipAddress;
    private String sessionId;
    private String correlationId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Integer pageNumber;
    private Integer pageSize;
    private String sortBy;
    private String sortDirection; // ASC, DESC
    
    private Boolean includeDetails;
    private Boolean includeComplianceData;
    private String complianceFramework;
    
    private String exportFormat; // JSON, CSV, PDF
    private Boolean generateReport;
    
    /**
     * Validation method
     */
    public boolean isValid() {
        return startDate != null && endDate != null && 
               startDate.isBefore(endDate);
    }
}
