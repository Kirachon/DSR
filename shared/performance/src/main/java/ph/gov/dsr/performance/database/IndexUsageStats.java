package ph.gov.dsr.performance.database;

import lombok.Builder;
import lombok.Data;

/**
 * Index usage statistics from PostgreSQL
 */
@Data
@Builder
public class IndexUsageStats {
    private String schemaName;
    private String tableName;
    private String indexName;
    private Long tuplesRead;
    private Long tuplesFetched;
    private Long scans;
    
    public String getFullIndexName() {
        return schemaName + "." + tableName + "." + indexName;
    }
    
    public Double getEfficiencyRatio() {
        return (tuplesRead != null && tuplesRead > 0 && tuplesFetched != null) ? 
            (double) tuplesFetched / tuplesRead : 0.0;
    }
    
    public String getUsageLevel() {
        if (scans == null || scans == 0) return "Unused";
        if (scans < 10) return "Low";
        if (scans < 100) return "Medium";
        if (scans < 1000) return "High";
        return "Very High";
    }
    
    public boolean isUnused() {
        return scans == null || scans == 0;
    }
    
    public boolean isLowUsage() {
        return scans != null && scans > 0 && scans < 10;
    }
}
