package ph.gov.dsr.datamanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ph.gov.dsr.datamanagement.service.DataIngestionService;
import ph.gov.dsr.datamanagement.service.DataValidationService;
import ph.gov.dsr.datamanagement.service.DeduplicationService;
import ph.gov.dsr.datamanagement.service.PhilSysIntegrationService;
import ph.gov.dsr.datamanagement.service.impl.MockDataIngestionServiceImpl;
import ph.gov.dsr.datamanagement.service.impl.MockDataValidationServiceImpl;
import ph.gov.dsr.datamanagement.service.impl.MockDeduplicationServiceImpl;
import ph.gov.dsr.datamanagement.service.impl.MockPhilSysIntegrationServiceImpl;
import ph.gov.dsr.datamanagement.service.impl.MockLegacyDataParserServiceImpl;
import ph.gov.dsr.datamanagement.service.impl.MockValidationRuleEngineImpl;
import ph.gov.dsr.datamanagement.service.impl.MockDataCleaningServiceImpl;
import ph.gov.dsr.datamanagement.service.LegacyDataParserService;
import ph.gov.dsr.datamanagement.service.ValidationRuleEngine;
import ph.gov.dsr.datamanagement.service.DataCleaningService;

/**
 * Configuration for no-database mode.
 * This configuration provides mock implementations when the 'no-db' profile is active.
 */
@Configuration
@Profile("no-db")
public class NoDbConfig {

    @Bean
    @Primary
    public DataIngestionService mockDataIngestionService() {
        return new MockDataIngestionServiceImpl();
    }

    @Bean
    @Primary
    public DataValidationService mockDataValidationService() {
        return new MockDataValidationServiceImpl();
    }

    @Bean
    @Primary
    public DeduplicationService mockDeduplicationService() {
        return new MockDeduplicationServiceImpl();
    }

    @Bean
    @Primary
    public PhilSysIntegrationService mockPhilSysIntegrationService() {
        return new MockPhilSysIntegrationServiceImpl();
    }

    @Bean
    @Primary
    public LegacyDataParserService mockLegacyDataParserService() {
        return new MockLegacyDataParserServiceImpl();
    }

    @Bean
    @Primary
    public ValidationRuleEngine mockValidationRuleEngine() {
        return new MockValidationRuleEngineImpl();
    }

    @Bean
    @Primary
    public DataCleaningService mockDataCleaningService() {
        return new MockDataCleaningServiceImpl();
    }
}
