package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Report Visualization DTO for report visualizations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class ReportVisualization {
    private String visualizationId;
    private String name;
    private String type; // CHART, GRAPH, MAP, TABLE, KPI
    private String chartType; // BAR, LINE, PIE, SCATTER, etc.
    private Map<String, Object> data;
    private Map<String, Object> configuration;
    private Map<String, Object> styling;
    private String title;
    private String description;
    private List<String> dataLabels;
    private Map<String, Object> legend;
    private Map<String, Object> axes;
    private String format; // SVG, PNG, PDF, HTML
}
