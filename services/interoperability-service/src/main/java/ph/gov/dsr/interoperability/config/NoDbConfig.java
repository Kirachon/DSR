package ph.gov.dsr.interoperability.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for no-database mode.
 * This configuration is active when the 'no-db' profile is used.
 * Disables JPA and database auto-configuration.
 */
@Configuration
@Profile("no-db")
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
public class NoDbConfig {

    /**
     * RestTemplate bean for HTTP client operations
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // This configuration will override any JPA-related configurations when no-db profile is active
}
