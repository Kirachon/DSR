package ph.gov.dsr.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the DSR Registration Service.
 *
 * This service handles citizen engagement and registration processes,
 * including PhilSys integration, multi-channel access, and life event reporting.
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@SpringBootApplication(scanBasePackages = {
    "ph.gov.dsr.registration",
    "ph.gov.dsr.common",
    "ph.gov.dsr.security",
    "ph.gov.dsr.messaging"
})

@EnableCaching
@EnableKafka
@EnableAsync
public class RegistrationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistrationServiceApplication.class, args);
    }
}
