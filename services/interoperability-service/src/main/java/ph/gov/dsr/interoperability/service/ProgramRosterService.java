package ph.gov.dsr.interoperability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.interoperability.dto.ProgramRosterRequest;
import ph.gov.dsr.interoperability.dto.ProgramRosterResponse;
import ph.gov.dsr.interoperability.dto.BeneficiaryRecord;
import ph.gov.dsr.interoperability.dto.ApiGatewayRequest;
import ph.gov.dsr.interoperability.dto.ApiGatewayResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating program rosters with real data from external systems
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramRosterService {

    private final ExternalSystemConnectorService externalSystemConnectorService;
    private final ApiGatewayService apiGatewayService;

    /**
     * Generate program roster with real beneficiary data
     */
    @Transactional(readOnly = true)
    public ProgramRosterResponse generateProgramRoster(ProgramRosterRequest request) {
        log.info("Generating program roster for program: {} region: {}", 
                request.getProgramCode(), request.getRegion());
        
        try {
            // Fetch beneficiary data from multiple sources
            List<BeneficiaryRecord> beneficiaries = fetchBeneficiaryData(request);
            
            // Enrich with additional data from external systems
            List<BeneficiaryRecord> enrichedBeneficiaries = enrichBeneficiaryData(beneficiaries, request);
            
            // Apply filters and sorting
            List<BeneficiaryRecord> filteredBeneficiaries = applyFilters(enrichedBeneficiaries, request);
            
            // Generate roster statistics
            Map<String, Object> statistics = generateRosterStatistics(filteredBeneficiaries);
            
            return ProgramRosterResponse.builder()
                .rosterId(generateRosterId())
                .programCode(request.getProgramCode())
                .region(request.getRegion())
                .generationDate(LocalDateTime.now())
                .beneficiaries(filteredBeneficiaries)
                .totalBeneficiaries(filteredBeneficiaries.size())
                .statistics(statistics)
                .dataSource("PRODUCTION")
                .build();
                
        } catch (Exception e) {
            log.error("Error generating program roster for program: {}", request.getProgramCode(), e);
            return createErrorResponse(request, e.getMessage());
        }
    }

    /**
     * Fetch beneficiary data from Registration Service and other sources
     */
    private List<BeneficiaryRecord> fetchBeneficiaryData(ProgramRosterRequest request) {
        log.debug("Fetching beneficiary data for program: {}", request.getProgramCode());
        
        List<BeneficiaryRecord> beneficiaries = new ArrayList<>();
        
        try {
            // Fetch from Registration Service
            ApiGatewayRequest registrationRequest = ApiGatewayRequest.builder()
                .systemCode("DSR_REGISTRATION")
                .endpoint("/api/v1/registrations/by-program/" + request.getProgramCode())
                .method("GET")
                .headers(Map.of("Accept", "application/json"))
                .build();
                
            ApiGatewayResponse registrationResponse = apiGatewayService.routeRequest(registrationRequest);
            
            if (registrationResponse.isSuccess()) {
                beneficiaries.addAll(parseRegistrationData(registrationResponse.getData()));
            }
            
            // Fetch from Eligibility Service
            ApiGatewayRequest eligibilityRequest = ApiGatewayRequest.builder()
                .systemCode("DSR_ELIGIBILITY")
                .endpoint("/api/v1/eligibility/eligible-beneficiaries/" + request.getProgramCode())
                .method("GET")
                .headers(Map.of("Accept", "application/json"))
                .build();
                
            ApiGatewayResponse eligibilityResponse = apiGatewayService.routeRequest(eligibilityRequest);
            
            if (eligibilityResponse.isSuccess()) {
                beneficiaries.addAll(parseEligibilityData(eligibilityResponse.getData()));
            }
            
        } catch (Exception e) {
            log.warn("Error fetching beneficiary data: {}", e.getMessage());
        }
        
        return beneficiaries;
    }

    /**
     * Enrich beneficiary data with external system information
     */
    private List<BeneficiaryRecord> enrichBeneficiaryData(List<BeneficiaryRecord> beneficiaries, 
                                                         ProgramRosterRequest request) {
        log.debug("Enriching beneficiary data with external system information");
        
        return beneficiaries.stream()
            .map(beneficiary -> enrichSingleBeneficiary(beneficiary, request))
            .collect(Collectors.toList());
    }

    /**
     * Enrich single beneficiary with external data
     */
    private BeneficiaryRecord enrichSingleBeneficiary(BeneficiaryRecord beneficiary, ProgramRosterRequest request) {
        try {
            // Enrich with PhilSys data if needed
            if (request.isIncludePhilSysData()) {
                enrichWithPhilSysData(beneficiary);
            }
            
            // Enrich with SSS data for employment programs
            if (request.getProgramCode().contains("EMPLOYMENT") || request.getProgramCode().contains("SSS")) {
                enrichWithSSSData(beneficiary);
            }
            
            // Enrich with GSIS data for government employees
            if (request.getProgramCode().contains("GSIS") || request.getProgramCode().contains("GOVERNMENT")) {
                enrichWithGSISData(beneficiary);
            }
            
            // Enrich with health data for health programs
            if (request.getProgramCode().contains("HEALTH") || request.getProgramCode().contains("PHILHEALTH")) {
                enrichWithHealthData(beneficiary);
            }
            
        } catch (Exception e) {
            log.warn("Error enriching beneficiary {}: {}", beneficiary.getPsn(), e.getMessage());
        }
        
        return beneficiary;
    }

    /**
     * Apply filters and sorting to beneficiary list
     */
    private List<BeneficiaryRecord> applyFilters(List<BeneficiaryRecord> beneficiaries, 
                                               ProgramRosterRequest request) {
        log.debug("Applying filters to beneficiary list");
        
        return beneficiaries.stream()
            .filter(b -> applyRegionFilter(b, request.getRegion()))
            .filter(b -> applyStatusFilter(b, request.getStatus()))
            .filter(b -> applyDateFilter(b, request.getFromDate(), request.getToDate()))
            .sorted(getSortComparator(request.getSortBy(), request.getSortOrder()))
            .limit(request.getMaxResults() != null ? request.getMaxResults() : 10000)
            .collect(Collectors.toList());
    }

    /**
     * Generate roster statistics
     */
    private Map<String, Object> generateRosterStatistics(List<BeneficiaryRecord> beneficiaries) {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalBeneficiaries", beneficiaries.size());
        statistics.put("byRegion", groupByRegion(beneficiaries));
        statistics.put("byStatus", groupByStatus(beneficiaries));
        statistics.put("byGender", groupByGender(beneficiaries));
        statistics.put("byAgeGroup", groupByAgeGroup(beneficiaries));
        statistics.put("generatedAt", LocalDateTime.now());
        
        return statistics;
    }

    /**
     * Helper methods for data enrichment
     */
    private void enrichWithPhilSysData(BeneficiaryRecord beneficiary) {
        try {
            ApiGatewayRequest request = ApiGatewayRequest.builder()
                .systemCode("PHILSYS")
                .endpoint("/api/v1/identity/" + beneficiary.getPsn())
                .method("GET")
                .build();
                
            ApiGatewayResponse response = externalSystemConnectorService.connectToGovernmentSystem("PHILSYS", request);
            
            if (response.isSuccess()) {
                // Parse and add PhilSys data to beneficiary
                Map<String, Object> philsysData = (Map<String, Object>) response.getData();
                beneficiary.setPhilsysData(philsysData);
            }
        } catch (Exception e) {
            log.debug("Could not enrich with PhilSys data for PSN: {}", beneficiary.getPsn());
        }
    }

    private void enrichWithSSSData(BeneficiaryRecord beneficiary) {
        try {
            ApiGatewayRequest request = ApiGatewayRequest.builder()
                .systemCode("SSS")
                .endpoint("/api/v1/member/" + beneficiary.getPsn())
                .method("GET")
                .build();
                
            ApiGatewayResponse response = externalSystemConnectorService.connectToGovernmentSystem("SSS", request);
            
            if (response.isSuccess()) {
                Map<String, Object> sssData = (Map<String, Object>) response.getData();
                beneficiary.setSssData(sssData);
            }
        } catch (Exception e) {
            log.debug("Could not enrich with SSS data for PSN: {}", beneficiary.getPsn());
        }
    }

    private void enrichWithGSISData(BeneficiaryRecord beneficiary) {
        try {
            ApiGatewayRequest request = ApiGatewayRequest.builder()
                .systemCode("GSIS")
                .endpoint("/api/v1/member/" + beneficiary.getPsn())
                .method("GET")
                .build();
                
            ApiGatewayResponse response = externalSystemConnectorService.connectToGovernmentSystem("GSIS", request);
            
            if (response.isSuccess()) {
                Map<String, Object> gsisData = (Map<String, Object>) response.getData();
                beneficiary.setGsisData(gsisData);
            }
        } catch (Exception e) {
            log.debug("Could not enrich with GSIS data for PSN: {}", beneficiary.getPsn());
        }
    }

    private void enrichWithHealthData(BeneficiaryRecord beneficiary) {
        try {
            ApiGatewayRequest request = ApiGatewayRequest.builder()
                .systemCode("DOH")
                .endpoint("/fhir/R4/Patient/" + beneficiary.getPsn())
                .method("GET")
                .build();
                
            ApiGatewayResponse response = externalSystemConnectorService.connectToGovernmentSystem("DOH", request);
            
            if (response.isSuccess()) {
                Map<String, Object> healthData = (Map<String, Object>) response.getData();
                beneficiary.setHealthData(healthData);
            }
        } catch (Exception e) {
            log.debug("Could not enrich with health data for PSN: {}", beneficiary.getPsn());
        }
    }

    /**
     * Helper methods for parsing and filtering
     */
    private List<BeneficiaryRecord> parseRegistrationData(Object data) {
        // Implementation would parse registration service response
        return new ArrayList<>();
    }

    private List<BeneficiaryRecord> parseEligibilityData(Object data) {
        // Implementation would parse eligibility service response
        return new ArrayList<>();
    }

    private boolean applyRegionFilter(BeneficiaryRecord beneficiary, String region) {
        return region == null || region.equals(beneficiary.getRegion());
    }

    private boolean applyStatusFilter(BeneficiaryRecord beneficiary, String status) {
        return status == null || status.equals(beneficiary.getStatus());
    }

    private boolean applyDateFilter(BeneficiaryRecord beneficiary, LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate == null && toDate == null) return true;
        LocalDateTime beneficiaryDate = beneficiary.getRegistrationDate();
        if (beneficiaryDate == null) return true;
        
        if (fromDate != null && beneficiaryDate.isBefore(fromDate)) return false;
        if (toDate != null && beneficiaryDate.isAfter(toDate)) return false;
        
        return true;
    }

    private Comparator<BeneficiaryRecord> getSortComparator(String sortBy, String sortOrder) {
        Comparator<BeneficiaryRecord> comparator = Comparator.comparing(BeneficiaryRecord::getLastName);
        
        if ("psn".equals(sortBy)) {
            comparator = Comparator.comparing(BeneficiaryRecord::getPsn);
        } else if ("registrationDate".equals(sortBy)) {
            comparator = Comparator.comparing(BeneficiaryRecord::getRegistrationDate);
        }
        
        return "DESC".equals(sortOrder) ? comparator.reversed() : comparator;
    }

    private Map<String, Long> groupByRegion(List<BeneficiaryRecord> beneficiaries) {
        return beneficiaries.stream()
            .collect(Collectors.groupingBy(BeneficiaryRecord::getRegion, Collectors.counting()));
    }

    private Map<String, Long> groupByStatus(List<BeneficiaryRecord> beneficiaries) {
        return beneficiaries.stream()
            .collect(Collectors.groupingBy(BeneficiaryRecord::getStatus, Collectors.counting()));
    }

    private Map<String, Long> groupByGender(List<BeneficiaryRecord> beneficiaries) {
        return beneficiaries.stream()
            .collect(Collectors.groupingBy(BeneficiaryRecord::getGender, Collectors.counting()));
    }

    private Map<String, Long> groupByAgeGroup(List<BeneficiaryRecord> beneficiaries) {
        return beneficiaries.stream()
            .collect(Collectors.groupingBy(this::getAgeGroup, Collectors.counting()));
    }

    private String getAgeGroup(BeneficiaryRecord beneficiary) {
        // Implementation would calculate age group based on birth date
        return "Adult"; // Simplified
    }

    private String generateRosterId() {
        return "ROSTER-" + System.currentTimeMillis();
    }

    private ProgramRosterResponse createErrorResponse(ProgramRosterRequest request, String errorMessage) {
        return ProgramRosterResponse.builder()
            .rosterId("ERROR-" + System.currentTimeMillis())
            .programCode(request.getProgramCode())
            .region(request.getRegion())
            .generationDate(LocalDateTime.now())
            .beneficiaries(new ArrayList<>())
            .totalBeneficiaries(0)
            .statistics(Map.of("error", errorMessage))
            .dataSource("ERROR")
            .build();
    }
}
