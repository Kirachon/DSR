package ph.gov.dsr.performance.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Advanced query analyzer for PostgreSQL performance monitoring
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QueryAnalyzer {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, QueryPerformanceMetrics> queryMetrics = new ConcurrentHashMap<>();

    /**
     * Analyze slow queries from PostgreSQL logs
     */
    public List<SlowQuery> getSlowQueries(long thresholdMs) {
        try {
            String sql = """
                SELECT 
                    query,
                    calls,
                    total_time,
                    mean_time,
                    max_time,
                    min_time,
                    stddev_time,
                    rows
                FROM pg_stat_statements 
                WHERE mean_time > ? 
                ORDER BY mean_time DESC 
                LIMIT 50
                """;

            return jdbcTemplate.query(sql, new Object[]{thresholdMs}, (rs, rowNum) -> 
                SlowQuery.builder()
                    .query(rs.getString("query"))
                    .calls(rs.getLong("calls"))
                    .totalTime(rs.getDouble("total_time"))
                    .meanTime(rs.getDouble("mean_time"))
                    .maxTime(rs.getDouble("max_time"))
                    .minTime(rs.getDouble("min_time"))
                    .stddevTime(rs.getDouble("stddev_time"))
                    .rows(rs.getLong("rows"))
                    .build()
            );

        } catch (Exception e) {
            log.warn("Failed to get slow queries from pg_stat_statements: {}", e.getMessage());
            // Fallback to application-level metrics
            return queryMetrics.values().stream()
                .filter(metrics -> metrics.getAverageExecutionTime() > thresholdMs)
                .map(this::convertToSlowQuery)
                .sorted((a, b) -> Double.compare(b.getMeanTime(), a.getMeanTime()))
                .limit(50)
                .collect(Collectors.toList());
        }
    }

    /**
     * Analyze query execution plans
     */
    public QueryExecutionPlan analyzeQueryPlan(String query) {
        try {
            String explainQuery = "EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) " + query;
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(explainQuery);
            
            if (!result.isEmpty()) {
                Map<String, Object> plan = result.get(0);
                return parseExecutionPlan(plan);
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to analyze query plan for: {}", query.substring(0, Math.min(query.length(), 100)), e);
            return null;
        }
    }

    /**
     * Get index usage statistics
     */
    public List<IndexUsageStats> getIndexUsageStatistics() {
        try {
            String sql = """
                SELECT 
                    schemaname,
                    tablename,
                    indexname,
                    idx_tup_read,
                    idx_tup_fetch,
                    idx_scan
                FROM pg_stat_user_indexes 
                ORDER BY idx_scan DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> 
                IndexUsageStats.builder()
                    .schemaName(rs.getString("schemaname"))
                    .tableName(rs.getString("tablename"))
                    .indexName(rs.getString("indexname"))
                    .tuplesRead(rs.getLong("idx_tup_read"))
                    .tuplesFetched(rs.getLong("idx_tup_fetch"))
                    .scans(rs.getLong("idx_scan"))
                    .build()
            );

        } catch (Exception e) {
            log.error("Failed to get index usage statistics", e);
            return Collections.emptyList();
        }
    }

    /**
     * Identify unused indexes
     */
    public List<UnusedIndex> getUnusedIndexes() {
        try {
            String sql = """
                SELECT 
                    schemaname,
                    tablename,
                    indexname,
                    pg_size_pretty(pg_relation_size(indexrelid)) as size
                FROM pg_stat_user_indexes 
                WHERE idx_scan = 0 
                AND schemaname NOT IN ('information_schema', 'pg_catalog')
                ORDER BY pg_relation_size(indexrelid) DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> 
                UnusedIndex.builder()
                    .schemaName(rs.getString("schemaname"))
                    .tableName(rs.getString("tablename"))
                    .indexName(rs.getString("indexname"))
                    .size(rs.getString("size"))
                    .build()
            );

        } catch (Exception e) {
            log.error("Failed to get unused indexes", e);
            return Collections.emptyList();
        }
    }

    /**
     * Suggest missing indexes based on query patterns
     */
    public List<IndexSuggestion> suggestMissingIndexes() {
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        try {
            // Analyze queries that perform sequential scans
            String sql = """
                SELECT 
                    query,
                    calls,
                    mean_time
                FROM pg_stat_statements 
                WHERE query LIKE '%WHERE%' 
                AND calls > 10 
                AND mean_time > 100
                ORDER BY calls * mean_time DESC 
                LIMIT 20
                """;

            List<Map<String, Object>> slowQueries = jdbcTemplate.queryForList(sql);
            
            for (Map<String, Object> queryData : slowQueries) {
                String query = (String) queryData.get("query");
                suggestions.addAll(analyzeQueryForIndexSuggestions(query));
            }
            
        } catch (Exception e) {
            log.warn("Failed to analyze queries for index suggestions: {}", e.getMessage());
        }
        
        return suggestions.stream()
            .distinct()
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * Get table bloat information
     */
    public List<TableBloat> getTableBloatInfo() {
        try {
            String sql = """
                SELECT 
                    schemaname,
                    tablename,
                    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
                    n_tup_ins,
                    n_tup_upd,
                    n_tup_del,
                    n_dead_tup,
                    CASE 
                        WHEN n_live_tup > 0 
                        THEN round(100.0 * n_dead_tup / (n_live_tup + n_dead_tup), 2)
                        ELSE 0 
                    END as bloat_percentage
                FROM pg_stat_user_tables 
                WHERE n_dead_tup > 1000
                ORDER BY n_dead_tup DESC
                """;

            return jdbcTemplate.query(sql, (rs, rowNum) -> 
                TableBloat.builder()
                    .schemaName(rs.getString("schemaname"))
                    .tableName(rs.getString("tablename"))
                    .size(rs.getString("size"))
                    .insertedTuples(rs.getLong("n_tup_ins"))
                    .updatedTuples(rs.getLong("n_tup_upd"))
                    .deletedTuples(rs.getLong("n_tup_del"))
                    .deadTuples(rs.getLong("n_dead_tup"))
                    .bloatPercentage(rs.getDouble("bloat_percentage"))
                    .build()
            );

        } catch (Exception e) {
            log.error("Failed to get table bloat information", e);
            return Collections.emptyList();
        }
    }

    /**
     * Record query execution for analysis
     */
    public void recordQueryExecution(String queryId, String sql, long executionTimeMs, int rowCount) {
        QueryPerformanceMetrics metrics = queryMetrics.computeIfAbsent(queryId, 
            k -> new QueryPerformanceMetrics(queryId, sql));
        
        metrics.addExecution(executionTimeMs, rowCount);
    }

    /**
     * Get query performance metrics
     */
    public Map<String, QueryPerformanceMetrics> getQueryMetrics() {
        return new HashMap<>(queryMetrics);
    }

    /**
     * Clear query metrics (for testing or reset)
     */
    public void clearMetrics() {
        queryMetrics.clear();
    }

    private SlowQuery convertToSlowQuery(QueryPerformanceMetrics metrics) {
        return SlowQuery.builder()
            .query(metrics.getSql())
            .calls((long) metrics.getExecutionCount())
            .totalTime(metrics.getTotalExecutionTime())
            .meanTime(metrics.getAverageExecutionTime())
            .maxTime(metrics.getMaxExecutionTime())
            .minTime(metrics.getMinExecutionTime())
            .stddevTime(0.0) // Not calculated in application metrics
            .rows((long) metrics.getAverageRowCount())
            .build();
    }

    private QueryExecutionPlan parseExecutionPlan(Map<String, Object> planData) {
        // Parse PostgreSQL JSON execution plan
        // This is a simplified implementation
        return QueryExecutionPlan.builder()
            .nodeType("Unknown")
            .totalCost(0.0)
            .actualTime(0.0)
            .rows(0L)
            .build();
    }

    private List<IndexSuggestion> analyzeQueryForIndexSuggestions(String query) {
        List<IndexSuggestion> suggestions = new ArrayList<>();
        
        // Simple pattern matching for common WHERE clauses
        // In a real implementation, this would use a proper SQL parser
        
        if (query.toLowerCase().contains("where") && query.toLowerCase().contains("=")) {
            // Extract potential column names from WHERE clauses
            // This is a simplified heuristic approach
            String[] parts = query.toLowerCase().split("where")[1].split("and|or");
            
            for (String part : parts) {
                if (part.contains("=") && !part.contains("'")) {
                    String column = part.split("=")[0].trim();
                    if (column.matches("[a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*")) {
                        String[] tableDotColumn = column.split("\\.");
                        suggestions.add(IndexSuggestion.builder()
                            .tableName(tableDotColumn[0])
                            .columnName(tableDotColumn[1])
                            .indexType("btree")
                            .reason("Frequent WHERE clause usage")
                            .estimatedBenefit("High")
                            .build());
                    }
                }
            }
        }
        
        return suggestions;
    }
}
