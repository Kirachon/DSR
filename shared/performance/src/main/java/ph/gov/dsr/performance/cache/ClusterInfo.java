package ph.gov.dsr.performance.cache;

import lombok.Builder;
import lombok.Data;

/**
 * Redis cluster information and status
 */
@Data
@Builder
public class ClusterInfo {
    private int totalNodes;
    private int masterNodes;
    private int slaveNodes;
    private String clusterState;
    private int slotsAssigned;
    private long timestamp;
    
    public static class ClusterInfoBuilder {
        public ClusterInfoBuilder() {
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public boolean isHealthy() {
        return "ok".equals(clusterState) && totalNodes > 0 && slotsAssigned == 16384;
    }
    
    public double getReplicationRatio() {
        return masterNodes > 0 ? (double) slaveNodes / masterNodes : 0.0;
    }
}
