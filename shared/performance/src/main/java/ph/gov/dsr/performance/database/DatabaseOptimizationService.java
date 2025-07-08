package ph.gov.dsr.performance.database;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database optimization service for PostgreSQL performance tuning
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseOptimizationService {

    private final JdbcTemplate jdbcTemplate;
    private final QueryAnalyzer queryAnalyzer;
    private final IndexOptimizer indexOptimizer;
    private final ConnectionPoolMonitor connectionPoolMonitor;

    @Value("${dsr.database.optimization.enabled:true}")
    private boolean optimizationEnabled;

    @Value("${dsr.database.optimization.auto-vacuum:true}")
    private boolean autoVacuumEnabled;

    @Value("${dsr.database.optimization.auto-analyze:true}")
    private boolean autoAnalyzeEnabled;

    @Value("${dsr.database.optimization.slow-query-threshold:1000}")
    private long slowQueryThresholdMs;

    private final Map<String, QueryPerformanceMetrics> queryMetrics = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        if (optimizationEnabled) {
            log.info("Database optimization service initialized");
            performInitialOptimization();
        }
    }

    /**
     * Perform comprehensive database optimization
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void performScheduledOptimization() {
        if (!optimizationEnabled) {
            return;
        }

        log.info("Starting scheduled database optimization");
        
        CompletableFuture.allOf(
            CompletableFuture.runAsync(this::optimizeIndexes),
            CompletableFuture.runAsync(this::updateTableStatistics),
            CompletableFuture.runAsync(this::cleanupOldData),
            CompletableFuture.runAsync(this::optimizeConnectionPool)
        ).thenRun(() -> {
            log.info("Scheduled database optimization completed");
        }).exceptionally(throwable -> {
            log.error("Database optimization failed", throwable);
            return null;
        });
    }

    /**
     * Optimize database indexes based on query patterns
     */
    public void optimizeIndexes() {
        try {
            log.info("Starting index optimization");
            
            // Analyze slow queries to identify missing indexes
            List<SlowQuery> slowQueries = queryAnalyzer.getSlowQueries(slowQueryThresholdMs);
            
            for (SlowQuery query : slowQueries) {
                List<IndexRecommendation> recommendations = 
                    indexOptimizer.analyzeQuery(query);
                
                for (IndexRecommendation recommendation : recommendations) {
                    if (recommendation.getConfidenceScore() > 0.8) {
                        createRecommendedIndex(recommendation);
                    }
                }
            }
            
            // Remove unused indexes
            removeUnusedIndexes();
            
            // Update index statistics
            updateIndexStatistics();
            
            log.info("Index optimization completed");
            
        } catch (Exception e) {
            log.error("Index optimization failed", e);
        }
    }

    /**
     * Update table statistics for query planner
     */
    public void updateTableStatistics() {
        try {
            log.info("Updating table statistics");
            
            List<String> tables = getTableNames();
            
            for (String table : tables) {
                // Analyze table for updated statistics
                jdbcTemplate.execute("ANALYZE " + table);
                
                // Check if table needs vacuum
                if (needsVacuum(table)) {
                    jdbcTemplate.execute("VACUUM ANALYZE " + table);
                    log.debug("Vacuumed table: {}", table);
                }
            }
            
            log.info("Table statistics update completed");
            
        } catch (Exception e) {
            log.error("Failed to update table statistics", e);
        }
    }

    /**
     * Clean up old data to improve performance
     */
    @Transactional
    public void cleanupOldData() {
        try {
            log.info("Starting data cleanup");
            
            // Clean up old audit logs (older than 1 year)
            int auditLogsDeleted = jdbcTemplate.update(
                "DELETE FROM audit_logs WHERE created_at < ?",
                LocalDateTime.now().minusYears(1)
            );
            
            // Clean up old notification logs (older than 6 months)
            int notificationLogsDeleted = jdbcTemplate.update(
                "DELETE FROM notification_logs WHERE created_at < ?",
                LocalDateTime.now().minusMonths(6)
            );
            
            // Clean up expired sessions
            int expiredSessionsDeleted = jdbcTemplate.update(
                "DELETE FROM user_sessions WHERE expires_at < ?",
                LocalDateTime.now()
            );
            
            // Clean up old temporary files
            int tempFilesDeleted = jdbcTemplate.update(
                "DELETE FROM temporary_files WHERE created_at < ?",
                LocalDateTime.now().minusDays(7)
            );
            
            log.info("Data cleanup completed: {} audit logs, {} notification logs, {} sessions, {} temp files deleted",
                    auditLogsDeleted, notificationLogsDeleted, expiredSessionsDeleted, tempFilesDeleted);
            
        } catch (Exception e) {
            log.error("Data cleanup failed", e);
        }
    }

    /**
     * Optimize connection pool settings
     */
    public void optimizeConnectionPool() {
        try {
            log.info("Optimizing connection pool");
            
            ConnectionPoolMetrics metrics = connectionPoolMonitor.getCurrentMetrics();
            
            // Adjust pool size based on usage patterns
            if (metrics.getAverageActiveConnections() > metrics.getMaxPoolSize() * 0.8) {
                // Pool is heavily used, consider increasing size
                log.warn("Connection pool utilization high: {}%", 
                        (metrics.getAverageActiveConnections() / metrics.getMaxPoolSize()) * 100);
            }
            
            if (metrics.getAverageWaitTime() > 100) {
                // Long wait times, pool might be undersized
                log.warn("High connection wait time: {}ms", metrics.getAverageWaitTime());
            }
            
            // Log connection pool statistics
            log.info("Connection pool metrics: Active={}, Idle={}, Wait={}ms", 
                    metrics.getActiveConnections(),
                    metrics.getIdleConnections(),
                    metrics.getAverageWaitTime());
            
        } catch (Exception e) {
            log.error("Connection pool optimization failed", e);
        }
    }

    /**
     * Monitor query performance and identify bottlenecks
     */
    public void monitorQueryPerformance(String queryId, String sql, long executionTimeMs) {
        if (!optimizationEnabled) {
            return;
        }
        
        QueryPerformanceMetrics metrics = queryMetrics.computeIfAbsent(queryId, 
            k -> new QueryPerformanceMetrics(queryId, sql));
        
        metrics.addExecution(executionTimeMs);
        
        // Log slow queries
        if (executionTimeMs > slowQueryThresholdMs) {
            log.warn("Slow query detected: {} - {}ms - {}", 
                    queryId, executionTimeMs, sql.substring(0, Math.min(sql.length(), 100)));
        }
    }

    /**
     * Get database performance report
     */
    public DatabasePerformanceReport getPerformanceReport() {
        try {
            DatabasePerformanceReport report = new DatabasePerformanceReport();
            
            // Connection pool metrics
            report.setConnectionPoolMetrics(connectionPoolMonitor.getCurrentMetrics());
            
            // Query performance metrics
            report.setSlowQueries(queryAnalyzer.getSlowQueries(slowQueryThresholdMs));
            
            // Index usage statistics
            report.setIndexUsageStats(getIndexUsageStatistics());
            
            // Table size information
            report.setTableSizes(getTableSizes());
            
            // Database size and growth
            report.setDatabaseSize(getDatabaseSize());
            
            return report;
            
        } catch (Exception e) {
            log.error("Failed to generate performance report", e);
            return new DatabasePerformanceReport();
        }
    }

    private void performInitialOptimization() {
        CompletableFuture.runAsync(() -> {
            try {
                // Set optimal PostgreSQL configuration
                optimizePostgreSQLSettings();
                
                // Create essential indexes if missing
                createEssentialIndexes();
                
                // Update all table statistics
                updateTableStatistics();
                
                log.info("Initial database optimization completed");
            } catch (Exception e) {
                log.error("Initial database optimization failed", e);
            }
        });
    }

    private void optimizePostgreSQLSettings() {
        try {
            // Set work_mem for complex queries
            jdbcTemplate.execute("SET work_mem = '256MB'");
            
            // Set maintenance_work_mem for maintenance operations
            jdbcTemplate.execute("SET maintenance_work_mem = '1GB'");
            
            // Enable parallel query execution
            jdbcTemplate.execute("SET max_parallel_workers_per_gather = 4");
            
            // Optimize random page cost for SSD
            jdbcTemplate.execute("SET random_page_cost = 1.1");
            
            log.info("PostgreSQL settings optimized");
            
        } catch (Exception e) {
            log.error("Failed to optimize PostgreSQL settings", e);
        }
    }

    private void createEssentialIndexes() {
        try {
            // Create indexes for frequently queried columns
            String[] essentialIndexes = {
                // Household-related indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_households_head_psn ON households(head_psn)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_households_status ON households(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_households_created_at ON households(created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_households_updated_at ON households(updated_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_household_members_psn ON household_members(psn)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_household_members_household_id ON household_members(household_id)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_household_members_head ON household_members(is_head_of_household)",

                // User and authentication indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_username ON users(username)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email ON users(email)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_role ON users(role)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_status ON users(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_last_login ON users(last_login_at)",

                // Audit and logging indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_action ON audit_logs(action)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_entity_type ON audit_logs(entity_type)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_user_id ON notification_logs(user_id)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_status ON notification_logs(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_notification_logs_created_at ON notification_logs(created_at)",

                // Payment and disbursement indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_disbursements_status ON payment_disbursements(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_disbursements_household_id ON payment_disbursements(household_id)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_disbursements_created_at ON payment_disbursements(created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_disbursements_amount ON payment_disbursements(amount)",

                // Eligibility and assessment indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_eligibility_assessments_household_id ON eligibility_assessments(household_id)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_eligibility_assessments_status ON eligibility_assessments(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_eligibility_assessments_score ON eligibility_assessments(pmt_score)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_eligibility_assessments_created_at ON eligibility_assessments(created_at)",

                // Grievance and case management indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_status ON grievance_cases(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_priority ON grievance_cases(priority)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_assigned_to ON grievance_cases(assigned_to)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_created_at ON grievance_cases(created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_household_id ON grievance_cases(household_id)",

                // Data management indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_data_ingestion_batches_status ON data_ingestion_batches(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_data_ingestion_batches_created_at ON data_ingestion_batches(created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_data_validation_results_status ON data_validation_results(status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_deduplication_results_status ON deduplication_results(status)",

                // Analytics and reporting indexes
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_analytics_reports_created_at ON analytics_reports(created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_analytics_reports_type ON analytics_reports(report_type)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_performance_metrics_timestamp ON performance_metrics(timestamp)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_performance_metrics_service ON performance_metrics(service_name)",

                // Composite indexes for common query patterns
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_households_status_created ON households(status, created_at)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_role_status ON users(role, status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_user_action ON audit_logs(user_id, action)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_payment_disbursements_household_status ON payment_disbursements(household_id, status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_status_priority ON grievance_cases(status, priority)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_eligibility_assessments_household_status ON eligibility_assessments(household_id, status)",
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_grievance_cases_status ON grievance_cases(status)"
            };
            
            for (String indexSql : essentialIndexes) {
                try {
                    jdbcTemplate.execute(indexSql);
                    log.debug("Created index: {}", indexSql);
                } catch (Exception e) {
                    log.debug("Index already exists or creation failed: {}", indexSql);
                }
            }
            
            log.info("Essential indexes created");
            
        } catch (Exception e) {
            log.error("Failed to create essential indexes", e);
        }
    }

    private void createRecommendedIndex(IndexRecommendation recommendation) {
        try {
            String indexSql = recommendation.getCreateIndexSql();
            jdbcTemplate.execute(indexSql);
            log.info("Created recommended index: {}", recommendation.getIndexName());
        } catch (Exception e) {
            log.error("Failed to create recommended index: {}", recommendation.getIndexName(), e);
        }
    }

    private void removeUnusedIndexes() {
        try {
            List<String> unusedIndexes = indexOptimizer.getUnusedIndexes();
            
            for (String indexName : unusedIndexes) {
                // Only remove non-essential indexes
                if (!isEssentialIndex(indexName)) {
                    jdbcTemplate.execute("DROP INDEX CONCURRENTLY " + indexName);
                    log.info("Removed unused index: {}", indexName);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to remove unused indexes", e);
        }
    }

    private void updateIndexStatistics() {
        try {
            jdbcTemplate.execute("REINDEX DATABASE CONCURRENTLY");
            log.info("Index statistics updated");
        } catch (Exception e) {
            log.error("Failed to update index statistics", e);
        }
    }

    private boolean needsVacuum(String tableName) {
        try {
            Integer deadTuples = jdbcTemplate.queryForObject(
                "SELECT n_dead_tup FROM pg_stat_user_tables WHERE relname = ?",
                Integer.class, tableName);
            
            Integer liveTuples = jdbcTemplate.queryForObject(
                "SELECT n_tup_ins + n_tup_upd FROM pg_stat_user_tables WHERE relname = ?",
                Integer.class, tableName);
            
            if (deadTuples != null && liveTuples != null && liveTuples > 0) {
                double deadRatio = (double) deadTuples / liveTuples;
                return deadRatio > 0.1; // Vacuum if more than 10% dead tuples
            }
            
            return false;
        } catch (Exception e) {
            log.error("Failed to check vacuum need for table: {}", tableName, e);
            return false;
        }
    }

    private List<String> getTableNames() {
        return jdbcTemplate.queryForList(
            "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
            String.class);
    }

    private boolean isEssentialIndex(String indexName) {
        return indexName.contains("_pkey") || 
               indexName.contains("_unique") ||
               indexName.startsWith("idx_households_head_psn") ||
               indexName.startsWith("idx_household_members_psn") ||
               indexName.startsWith("idx_users_username");
    }

    private List<IndexUsageStatistic> getIndexUsageStatistics() {
        return jdbcTemplate.query(
            "SELECT schemaname, tablename, indexname, idx_tup_read, idx_tup_fetch " +
            "FROM pg_stat_user_indexes ORDER BY idx_tup_read DESC",
            (rs, rowNum) -> IndexUsageStatistic.builder()
                .schemaName(rs.getString("schemaname"))
                .tableName(rs.getString("tablename"))
                .indexName(rs.getString("indexname"))
                .tuplesRead(rs.getLong("idx_tup_read"))
                .tuplesFetched(rs.getLong("idx_tup_fetch"))
                .build());
    }

    private List<TableSizeInfo> getTableSizes() {
        return jdbcTemplate.query(
            "SELECT schemaname, tablename, " +
            "pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size " +
            "FROM pg_tables WHERE schemaname = 'public' " +
            "ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC",
            (rs, rowNum) -> TableSizeInfo.builder()
                .schemaName(rs.getString("schemaname"))
                .tableName(rs.getString("tablename"))
                .size(rs.getString("size"))
                .build());
    }

    private String getDatabaseSize() {
        return jdbcTemplate.queryForObject(
            "SELECT pg_size_pretty(pg_database_size(current_database()))",
            String.class);
    }
}
