package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Report Response DTO
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
@Builder
public class ReportResponse {
    private UUID reportId;
    private String reportCode;
    private String reportName;
    private String status;
    private String errorMessage;
    private Map<String, Object> data;
    private Map<String, Object> summaryStatistics;
    private Long recordCount;
    private Long generationTime;
    private LocalDateTime generatedAt;
}
