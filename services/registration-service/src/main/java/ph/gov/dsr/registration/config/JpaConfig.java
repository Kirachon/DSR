package ph.gov.dsr.registration.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Configuration for the Registration Service.
 * 
 * This configuration conditionally enables JPA auditing based on the
 * features.database-persistence property.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Configuration
@EnableJpaAuditing
@ConditionalOnProperty(name = "features.database-persistence", havingValue = "true", matchIfMissing = true)
public class JpaConfig {
    // JPA auditing configuration
}
