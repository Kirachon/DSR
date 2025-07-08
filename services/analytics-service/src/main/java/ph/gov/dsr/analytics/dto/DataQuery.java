package ph.gov.dsr.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Data Query DTO for report data queries
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Data
@Builder
public class DataQuery {
    private String queryId;
    private String queryType; // SQL, AGGREGATION, FILTER
    private List<String> dataSources;
    private Map<String, Object> filters;
    private List<String> selectedFields;
    private List<String> groupByFields;
    private String sortBy;
    private String sortOrder;
    private Map<String, Object> aggregations;
    private Integer limit;
    private Integer offset;
    private Map<String, Object> parameters;
    private String rawQuery;
}
