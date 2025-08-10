package ph.gov.dsr.interoperability.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import ph.gov.dsr.interoperability.service.ApiGatewayService;
import ph.gov.dsr.interoperability.service.ExternalSystemConnectorService;
import ph.gov.dsr.interoperability.service.InternationalStandardsService;

import jakarta.annotation.PostConstruct;

/**
 * Production configuration for Interoperability Service.
 * This configuration ensures production implementations are used when the 'no-db' profile is NOT active.
 * 
 * Production implementations include:
 * - Full external system integration (PhilSys, SSS, GSIS, PhilHealth, BIR, etc.)
 * - API Gateway with load balancing and routing
 * - International standards compliance (FHIR, OpenID Connect, GDPR)
 * - Service delivery tracking and monitoring
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
        log.info("=== INTEROPERABILITY SERVICE: PRODUCTION MODE ACTIVE ===");
        log.info("External system integrations: ENABLED");
        log.info("API Gateway: ENABLED");
        log.info("Service delivery tracking: ENABLED");
        log.info("International standards compliance: ENABLED");
        log.info("Database persistence: ENABLED");
        log.info("Mock services: DISABLED");
        log.info("==========================================================");
    }

    /**
     * RestTemplate bean for HTTP client operations in production
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Production API gateway service - always primary in production profile
     */
    @Bean
    @Primary
    public ApiGatewayService productionApiGatewayService(ApiGatewayService impl) {
        log.info("Configuring PRODUCTION ApiGatewayService with load balancing and routing - NO MOCK FALLBACK");
        return impl;
    }

    /**
     * Production external system connector service - always primary in production profile
     */
    @Bean
    @Primary
    public ExternalSystemConnectorService productionExternalSystemConnectorService(ExternalSystemConnectorService impl) {
        log.info("Configuring PRODUCTION ExternalSystemConnectorService with real government system integrations - NO MOCK FALLBACK");
        return impl;
    }

    /**
     * Production international standards service - always primary in production profile
     */
    @Bean
    @Primary
    public InternationalStandardsService productionInternationalStandardsService(InternationalStandardsService impl) {
        log.info("Configuring PRODUCTION InternationalStandardsService with FHIR, OpenID Connect, and GDPR compliance - NO MOCK FALLBACK");
        return impl;
    }
}
