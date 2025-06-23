package ph.gov.dsr.registration.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for the Registration Service.
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
            "registrations",
            "eligibility-checks",
            "documents",
            "user-sessions"
        );
    }
}
