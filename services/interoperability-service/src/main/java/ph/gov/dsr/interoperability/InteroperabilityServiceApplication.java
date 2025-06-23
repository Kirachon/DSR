package ph.gov.dsr.interoperability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the DSR Interoperability Service.
 * 
 * This service handles external system integration and data exchange
 * with government agencies and partner organizations.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@SpringBootApplication(scanBasePackages = {
    "ph.gov.dsr.interoperability",
    "ph.gov.dsr.common",
    "ph.gov.dsr.security",
    "ph.gov.dsr.messaging"
})
@EnableCaching
@EnableKafka
@EnableAsync
public class InteroperabilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteroperabilityServiceApplication.class, args);
    }
}
