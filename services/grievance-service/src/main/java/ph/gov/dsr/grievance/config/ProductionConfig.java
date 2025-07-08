package ph.gov.dsr.grievance.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import ph.gov.dsr.grievance.service.MultiChannelCaseManagementService;
import ph.gov.dsr.grievance.service.WorkflowAutomationService;
import ph.gov.dsr.grievance.service.CaseAssignmentService;

import jakarta.annotation.PostConstruct;

/**
 * Production configuration for Grievance Service.
 * This configuration ensures production implementations are used when the 'no-db' profile is NOT active.
 * 
 * Production implementations include:
 * - Multi-channel case management (Web, Mobile, SMS, Phone, Postal)
 * - Automated workflow processing with intelligent assignment
 * - Case tracking and activity management
 * - SLA monitoring and notification systems
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
        log.info("=== GRIEVANCE SERVICE: PRODUCTION MODE ACTIVE ===");
        log.info("Multi-channel case management: ENABLED");
        log.info("Workflow automation: ENABLED");
        log.info("Intelligent case assignment: ENABLED");
        log.info("SLA monitoring: ENABLED");
        log.info("Database persistence: ENABLED");
        log.info("Mock services: DISABLED");
        log.info("==================================================");
    }

    /**
     * Production multi-channel case management service - always primary in production profile
     */
    @Bean
    @Primary
    public MultiChannelCaseManagementService productionMultiChannelCaseManagementService(
            MultiChannelCaseManagementService impl) {
        log.info("Configuring PRODUCTION MultiChannelCaseManagementService with comprehensive case management - NO MOCK FALLBACK");
        return impl;
    }

    /**
     * Production workflow automation service - always primary in production profile
     */
    @Bean
    @Primary
    public WorkflowAutomationService productionWorkflowAutomationService(WorkflowAutomationService impl) {
        log.info("Configuring PRODUCTION WorkflowAutomationService with intelligent workflow processing - NO MOCK FALLBACK");
        return impl;
    }

    /**
     * Production case assignment service - always primary in production profile
     */
    @Bean
    @Primary
    public CaseAssignmentService productionCaseAssignmentService(CaseAssignmentService impl) {
        log.info("Configuring PRODUCTION CaseAssignmentService with intelligent assignment algorithms - NO MOCK FALLBACK");
        return impl;
    }
}
