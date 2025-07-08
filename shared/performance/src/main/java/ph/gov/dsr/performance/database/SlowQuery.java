package ph.gov.dsr.performance.database;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a slow query with performance metrics
 */
@Data
@Builder
public class SlowQuery {
    private String query;
    private Long calls;
    private Double totalTime;
    private Double meanTime;
    private Double maxTime;
    private Double minTime;
    private Double stddevTime;
    private Long rows;
    
    public String getFormattedQuery() {
        return query != null && query.length() > 100 ? 
            query.substring(0, 100) + "..." : query;
    }
    
    public String getPerformanceCategory() {
        if (meanTime == null) return "Unknown";
        
        if (meanTime > 5000) return "Critical";
        if (meanTime > 1000) return "High";
        if (meanTime > 500) return "Medium";
        return "Low";
    }
    
    public Double getRowsPerSecond() {
        return (rows != null && meanTime != null && meanTime > 0) ? 
            (rows * 1000.0) / meanTime : 0.0;
    }
}
