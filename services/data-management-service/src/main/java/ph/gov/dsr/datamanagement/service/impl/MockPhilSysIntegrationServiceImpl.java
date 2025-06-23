package ph.gov.dsr.datamanagement.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationRequest;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationResponse;
import ph.gov.dsr.datamanagement.service.PhilSysIntegrationService;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mock implementation of PhilSysIntegrationService for no-database mode
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@Profile("no-db")
@Slf4j
public class MockPhilSysIntegrationServiceImpl implements PhilSysIntegrationService {

    @Override
    public PhilSysVerificationResponse verifyPSN(PhilSysVerificationRequest request) {
        log.info("Mock verifying PSN: {}", request.getPsn());
        
        PhilSysVerificationResponse response = new PhilSysVerificationResponse();
        response.setPsn(request.getPsn());
        response.setVerifiedAt(LocalDateTime.now());
        response.setRequestId(request.getRequestId());
        response.setResponseTimeMs(500L);
        
        // Mock verification logic based on PSN pattern
        if (request.getPsn().startsWith("1234")) {
            response.setValid(true);
            response.setVerificationStatus("VERIFIED");
            response.setFirstName("JUAN");
            response.setLastName("DELA CRUZ");
            response.setMiddleName("SANTOS");
            response.setDateOfBirth("1990-05-15");
            response.setSex("M");
            response.setPlaceOfBirth("MANILA, PHILIPPINES");
            response.setCivilStatus("SINGLE");
            response.setCitizenship("FILIPINO");
            response.setConfidenceScore(0.95);
            response.setMatchScore(1.0);

            // Also set nested personInfo for backward compatibility
            PhilSysVerificationResponse.VerifiedPersonInfo personInfo =
                new PhilSysVerificationResponse.VerifiedPersonInfo();
            personInfo.setFirstName("JUAN");
            personInfo.setLastName("DELA CRUZ");
            personInfo.setMiddleName("SANTOS");
            personInfo.setDateOfBirth(LocalDate.of(1990, 5, 15));
            personInfo.setSex("M");
            personInfo.setPlaceOfBirth("MANILA, PHILIPPINES");
            personInfo.setCivilStatus("SINGLE");
            personInfo.setCitizenship("FILIPINO");
            personInfo.setActive(true);
            personInfo.setRegistrationDate(LocalDate.of(2019, 1, 1));
            response.setPersonInfo(personInfo);

        } else if (request.getPsn().startsWith("9999")) {
            response.setValid(false);
            response.setVerificationStatus("NOT_FOUND");
            response.setErrorMessage("PSN not found in PhilSys database");
            response.setConfidenceScore(0.0);
            response.setMatchScore(0.0);
        } else if (request.getPsn().startsWith("0000")) {
            response.setValid(false);
            response.setVerificationStatus("ERROR");
            response.setErrorMessage("PhilSys service temporarily unavailable");
            response.setConfidenceScore(0.0);
            response.setMatchScore(0.0);
        } else {
            response.setValid(true);
            response.setVerificationStatus("VERIFIED");
            response.setFirstName("MARIA");
            response.setLastName("GARCIA");
            response.setMiddleName("REYES");
            response.setDateOfBirth("1985-08-20");
            response.setSex("F");
            response.setPlaceOfBirth("CEBU, PHILIPPINES");
            response.setCivilStatus("MARRIED");
            response.setCitizenship("FILIPINO");
            response.setConfidenceScore(0.90);
            response.setMatchScore(1.0);

            // Also set nested personInfo for backward compatibility
            PhilSysVerificationResponse.VerifiedPersonInfo personInfo =
                new PhilSysVerificationResponse.VerifiedPersonInfo();
            personInfo.setFirstName("MARIA");
            personInfo.setLastName("GARCIA");
            personInfo.setMiddleName("REYES");
            personInfo.setDateOfBirth(LocalDate.of(1985, 8, 20));
            personInfo.setSex("F");
            personInfo.setPlaceOfBirth("CEBU, PHILIPPINES");
            personInfo.setCivilStatus("MARRIED");
            personInfo.setCitizenship("FILIPINO");
            personInfo.setActive(true);
            personInfo.setRegistrationDate(LocalDate.of(2019, 3, 15));
            response.setPersonInfo(personInfo);
        }
        
        return response;
    }

    @Override
    public boolean isPSNValid(String psn) {
        log.info("Mock checking PSN validity: {}", psn);
        
        // Mock validation: PSN should be 16 characters with dashes
        return psn != null && psn.matches("\\d{4}-\\d{4}-\\d{4}");
    }

    @Override
    public PhilSysVerificationResponse getPersonInfo(String psn) {
        log.info("Mock getting person info for PSN: {}", psn);
        
        PhilSysVerificationRequest request = new PhilSysVerificationRequest();
        request.setPsn(psn);
        request.setVerificationLevel("BASIC");
        
        return verifyPSN(request);
    }

    @Override
    public PhilSysVerificationResponse verifyPersonDetails(PhilSysVerificationRequest request) {
        log.info("Mock verifying person details for PSN: {}", request.getPsn());
        
        PhilSysVerificationResponse response = verifyPSN(request);
        
        // Additional verification logic for person details
        if (response.isValid() && response.getPersonInfo() != null) {
            PhilSysVerificationResponse.VerifiedPersonInfo personInfo = response.getPersonInfo();

            // Check if provided details match
            boolean detailsMatch = true;
            if (request.getFirstName() != null &&
                !request.getFirstName().equalsIgnoreCase(personInfo.getFirstName())) {
                detailsMatch = false;
            }
            if (request.getLastName() != null &&
                !request.getLastName().equalsIgnoreCase(personInfo.getLastName())) {
                detailsMatch = false;
            }
            if (request.getDateOfBirth() != null &&
                !request.getDateOfBirth().equals(personInfo.getDateOfBirth())) {
                detailsMatch = false;
            }

            if (!detailsMatch) {
                response.setValid(false);
                response.setVerificationStatus("MISMATCH");
                response.setErrorMessage("Provided details do not match PhilSys records");
                response.setMatchScore(0.3); // Low match score for mismatched details
            }
        }
        
        return response;
    }

    @Override
    public boolean isPhilSysServiceAvailable() {
        log.info("Mock checking PhilSys service availability");
        return true; // Mock: service is always available
    }

    @Override
    public String getPhilSysServiceStatus() {
        log.info("Mock getting PhilSys service status");
        return "ONLINE"; // Mock: service is always online
    }
}
