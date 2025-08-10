package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Report Data DTO for report data representation
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ReportData {
    private String dataId;
    private String reportId;
    private List<Map<String, Object>> rows;
    private List<String> headers;
    private Map<String, Object> metadata;
    private Integer totalRows;
    private Integer pageSize;
    private Integer currentPage;
    private LocalDateTime generatedAt;
    private String dataSource;
    private Map<String, Object> statistics;
    private List<String> warnings;
}
