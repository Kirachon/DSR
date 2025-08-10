package ph.gov.dsr.eligibility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the DSR Eligibility Service.
 * 
 * This service handles program eligibility assessment and determination
 * for social protection programs.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@SpringBootApplication(scanBasePackages = {
    "ph.gov.dsr.eligibility",
    "ph.gov.dsr.common",
    "ph.gov.dsr.security",
    "ph.gov.dsr.messaging"
})
@EnableCaching
@EnableKafka
@EnableAsync
public class EligibilityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EligibilityServiceApplication.class, args);
    }
}
