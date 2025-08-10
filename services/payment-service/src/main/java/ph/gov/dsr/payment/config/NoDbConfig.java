package ph.gov.dsr.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for no-database mode.
 * This configuration is active when the 'no-db' profile is used.
 */
@Configuration
@Profile("no-db")
public class NoDbConfig {
    // This configuration will override any JPA-related configurations when no-db profile is active
}
