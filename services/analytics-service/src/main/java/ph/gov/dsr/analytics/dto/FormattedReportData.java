package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Formatted Report Data DTO for formatted report data
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class FormattedReportData {
    private String dataId;
    private String reportId;
    private List<Map<String, Object>> formattedRows;
    private List<String> formattedHeaders;
    private Map<String, Object> formatting;
    private Map<String, Object> styling;
    private Map<String, Object> layout;
    private LocalDateTime formattedAt;
    private String format; // HTML, PDF, EXCEL, CSV
    private Map<String, Object> metadata;
    private List<String> appliedFormats;
}
