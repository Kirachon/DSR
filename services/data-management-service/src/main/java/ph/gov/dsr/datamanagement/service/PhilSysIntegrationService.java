package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.dto.PhilSysVerificationRequest;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationResponse;

/**
 * Service interface for PhilSys integration operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface PhilSysIntegrationService {

    /**
     * Verify PSN with PhilSys
     */
    PhilSysVerificationResponse verifyPSN(PhilSysVerificationRequest request);

    /**
     * Check if PSN exists in PhilSys
     */
    boolean isPSNValid(String psn);

    /**
     * Get person information from PhilSys
     */
    PhilSysVerificationResponse getPersonInfo(String psn);

    /**
     * Verify person details against PhilSys
     */
    PhilSysVerificationResponse verifyPersonDetails(PhilSysVerificationRequest request);

    /**
     * Check PhilSys service health
     */
    boolean isPhilSysServiceAvailable();

    /**
     * Get PhilSys service status
     */
    String getPhilSysServiceStatus();
}
