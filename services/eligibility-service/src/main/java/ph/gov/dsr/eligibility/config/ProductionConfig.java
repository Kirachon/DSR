package ph.gov.dsr.eligibility.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ph.gov.dsr.eligibility.service.EligibilityAssessmentService;
import ph.gov.dsr.eligibility.service.ProgramManagementService;
import ph.gov.dsr.eligibility.service.impl.ProductionEligibilityAssessmentServiceImpl;
import ph.gov.dsr.eligibility.service.impl.ProgramManagementServiceImpl;

import jakarta.annotation.PostConstruct;

/**
 * Production configuration for Eligibility Service.
 * This configuration ensures production implementations are used when the 'no-db' profile is NOT active.
 * 
 * Production implementations include:
 * - ProductionEligibilityAssessmentServiceImpl (with database persistence and PMT calculator)
 * - ProgramManagementServiceImpl (with real program management logic)
 * - Full integration with Rules Engine and PMT Calculator
 * - Database-backed assessment history and statistics
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-27
 */
@Configuration
@Profile("!no-db")
@Slf4j
public class ProductionConfig {

    @PostConstruct
    public void logProductionMode() {
        log.info("=== ELIGIBILITY SERVICE: PRODUCTION MODE ACTIVE ===");
        log.info("Using ProductionEligibilityAssessmentServiceImpl with database persistence");
        log.info("PMT Calculator: ENABLED");
        log.info("Rules Engine: ENABLED");
        log.info("Database persistence: ENABLED");
        log.info("Mock services: DISABLED");
        log.info("====================================================");
    }

    /**
     * Production eligibility assessment service - always primary in production profile
     */
    @Bean
    @Primary
    public EligibilityAssessmentService productionEligibilityAssessmentService(
            ProductionEligibilityAssessmentServiceImpl impl) {
        log.info("Configuring PRODUCTION EligibilityAssessmentService with PMT calculator and rules engine - NO MOCK FALLBACK");
        return impl;
    }

    /**
     * Production program management service - always primary in production profile
     */
    @Bean
    @Primary
    public ProgramManagementService productionProgramManagementService(ProgramManagementServiceImpl impl) {
        log.info("Configuring PRODUCTION ProgramManagementService with real program management logic - NO MOCK FALLBACK");
        return impl;
    }
}
