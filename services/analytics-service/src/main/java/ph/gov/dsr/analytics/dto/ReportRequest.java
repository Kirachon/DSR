package ph.gov.dsr.analytics.dto;

import lombok.Data;
import ph.gov.dsr.analytics.entity.AnalyticsReport;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Report Request DTO
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class ReportRequest {
    private String reportCode;
    private String reportName;
    private AnalyticsReport.ReportType reportType;
    private AnalyticsReport.ReportCategory category;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Map<String, Object> parameters;
    private Map<String, Object> filters;
    private String generatedBy;
    private String fileFormat;
}
