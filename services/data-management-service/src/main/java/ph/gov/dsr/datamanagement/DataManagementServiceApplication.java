package ph.gov.dsr.datamanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the DSR Data Management Service.
 * 
 * This service handles data processing, validation, and management operations
 * for the Dynamic Social Registry system.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@SpringBootApplication(scanBasePackages = {
    "ph.gov.dsr.datamanagement",
    "ph.gov.dsr.common",
    "ph.gov.dsr.security",
    "ph.gov.dsr.messaging"
})
@EnableCaching
@EnableKafka
@EnableAsync
public class DataManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataManagementServiceApplication.class, args);
    }
}
