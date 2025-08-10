package ph.gov.dsr.grievance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the DSR Grievance Service.
 * 
 * This service handles grievance management, complaint resolution,
 * and feedback processing for the DSR system.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@SpringBootApplication(scanBasePackages = {
    "ph.gov.dsr.grievance",
    "ph.gov.dsr.common",
    "ph.gov.dsr.security",
    "ph.gov.dsr.messaging"
})
@EnableCaching
@EnableKafka
@EnableAsync
public class GrievanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrievanceServiceApplication.class, args);
    }
}
