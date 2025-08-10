package ph.gov.dsr.analytics.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the Analytics Service.
 * Provides in-memory cache manager for local development.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * In-memory cache manager for development and testing.
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "analytics-reports",
            "dashboard-data",
            "performance-metrics",
            "aggregated-statistics",
            "trend-analysis",
            "user-sessions"
        );
    }
}
