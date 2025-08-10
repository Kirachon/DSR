package ph.gov.dsr.interoperability.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the Interoperability Service.
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
            "external-api-responses",
            "agency-configurations",
            "data-mappings",
            "sync-status",
            "integration-logs",
            "user-sessions"
        );
    }
}
