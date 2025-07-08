package ph.gov.dsr.datamanagement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.datamanagement.dto.HouseholdDataRequest;
import ph.gov.dsr.datamanagement.dto.HouseholdDataResponse;
import ph.gov.dsr.datamanagement.entity.Household;
import ph.gov.dsr.datamanagement.repository.HouseholdRepository;
import ph.gov.dsr.datamanagement.service.DataValidationService;
import ph.gov.dsr.datamanagement.service.HouseholdDataService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of HouseholdDataService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HouseholdDataServiceImpl implements HouseholdDataService {

    private final HouseholdRepository householdRepository;
    private final DataValidationService dataValidationService;

    @Override
    public HouseholdDataResponse createHousehold(HouseholdDataRequest request) {
        log.info("Creating household with number: {}", request.getHouseholdNumber());
        
        try {
            // Check if household number already exists
            if (householdRepository.existsByHouseholdNumber(request.getHouseholdNumber())) {
                return HouseholdDataResponse.error("Household number already exists: " + request.getHouseholdNumber());
            }

            // Validate household data
            if (!dataValidationService.validateHouseholdData(request)) {
                return HouseholdDataResponse.error("Household data validation failed");
            }

            // Create household entity
            Household household = mapRequestToEntity(request);
            household.setCreatedAt(LocalDateTime.now());
            household.setUpdatedAt(LocalDateTime.now());

            // Save household
            household = householdRepository.save(household);
            log.info("Successfully created household with ID: {}", household.getId());

            return mapEntityToResponse(household, "SUCCESS", "Household created successfully");

        } catch (Exception e) {
            log.error("Error creating household: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Failed to create household: " + e.getMessage());
        }
    }

    @Override
    public HouseholdDataResponse updateHousehold(UUID householdId, HouseholdDataRequest request) {
        log.info("Updating household with ID: {}", householdId);
        
        try {
            Optional<Household> existingHousehold = householdRepository.findById(householdId);
            if (existingHousehold.isEmpty()) {
                return HouseholdDataResponse.error("Household not found with ID: " + householdId);
            }

            // Validate household data
            if (!dataValidationService.validateHouseholdData(request)) {
                return HouseholdDataResponse.error("Household data validation failed");
            }

            // Update household entity
            Household household = existingHousehold.get();
            updateEntityFromRequest(household, request);
            household.setUpdatedAt(LocalDateTime.now());

            // Save updated household
            household = householdRepository.save(household);
            log.info("Successfully updated household with ID: {}", household.getId());

            return mapEntityToResponse(household, "SUCCESS", "Household updated successfully");

        } catch (Exception e) {
            log.error("Error updating household: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Failed to update household: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HouseholdDataResponse getHousehold(UUID householdId) {
        log.info("Getting household with ID: {}", householdId);
        
        try {
            Optional<Household> household = householdRepository.findById(householdId);
            if (household.isEmpty()) {
                return HouseholdDataResponse.error("Household not found with ID: " + householdId);
            }

            return mapEntityToResponse(household.get(), "SUCCESS", "Household retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting household: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Failed to get household: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HouseholdDataResponse getHouseholdByNumber(String householdNumber) {
        log.info("Getting household with number: {}", householdNumber);

        try {
            Optional<Household> household = householdRepository.findByHouseholdNumber(householdNumber);
            if (household.isEmpty()) {
                return HouseholdDataResponse.error("Household not found with number: " + householdNumber);
            }

            return mapEntityToResponse(household.get(), "SUCCESS", "Household retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting household by number: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Failed to get household: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HouseholdDataResponse getHouseholdByHeadPsn(String headPsn) {
        log.info("Getting household with head PSN: {}", headPsn);

        try {
            Optional<Household> household = householdRepository.findByHeadOfHouseholdPsn(headPsn);
            if (household.isEmpty()) {
                return HouseholdDataResponse.error("Household not found with head PSN: " + headPsn);
            }

            return mapEntityToResponse(household.get(), "SUCCESS", "Household retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting household by head PSN: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Failed to get household: " + e.getMessage());
        }
    }

    @Override
    public HouseholdDataResponse deleteHousehold(UUID householdId) {
        log.info("Deleting household with ID: {}", householdId);

        try {
            Optional<Household> household = householdRepository.findById(householdId);
            if (household.isEmpty()) {
                return HouseholdDataResponse.error("Household not found with ID: " + householdId);
            }

            householdRepository.deleteById(householdId);
            log.info("Successfully deleted household with ID: {}", householdId);

            return HouseholdDataResponse.success("Household deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting household: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Failed to delete household: " + e.getMessage());
        }
    }

    @Override
    public boolean deactivateHousehold(UUID householdId, String reason) {
        log.info("Deactivating household with ID: {} for reason: {}", householdId, reason);

        try {
            Optional<Household> householdOpt = householdRepository.findById(householdId);
            if (householdOpt.isEmpty()) {
                log.warn("Household not found with ID: {}", householdId);
                return false;
            }

            Household household = householdOpt.get();
            household.setStatus("INACTIVE");
            household.setNotes(household.getNotes() != null ?
                household.getNotes() + "; Deactivated: " + reason :
                "Deactivated: " + reason);
            household.setUpdatedAt(LocalDateTime.now());

            householdRepository.save(household);
            log.info("Successfully deactivated household with ID: {}", householdId);

            return true;

        } catch (Exception e) {
            log.error("Error deactivating household: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> getHouseholdsByRegion(String region) {
        log.info("Getting households by region: {}", region);
        
        try {
            List<Household> households = householdRepository.findByRegionOrderByCreatedAtDesc(region);
            return households.stream()
                .map(h -> mapEntityToResponse(h, "SUCCESS", "Household retrieved successfully"))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting households by region: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> getHouseholdsByIncomeRange(BigDecimal minIncome, BigDecimal maxIncome) {
        log.info("Getting households by income range: {} - {}", minIncome, maxIncome);
        
        try {
            List<Household> households = householdRepository.findByMonthlyIncomeBetweenOrderByMonthlyIncomeAsc(minIncome, maxIncome);
            return households.stream()
                .map(h -> mapEntityToResponse(h, "SUCCESS", "Household retrieved successfully"))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting households by income range: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getHouseholdStatistics() {
        log.info("Getting household statistics");
        
        try {
            List<Object[]> stats = householdRepository.getHouseholdStatistics();
            Map<String, Object> result = new HashMap<>();
            
            if (!stats.isEmpty()) {
                Object[] row = stats.get(0);
                result.put("totalHouseholds", row[0]);
                result.put("averageMembers", row[1]);
                result.put("averageIncome", row[2]);
                result.put("vulnerableHouseholds", row[3]);
            }
            
            return result;

        } catch (Exception e) {
            log.error("Error getting household statistics: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    // Helper methods for mapping between entities and DTOs
    private Household mapRequestToEntity(HouseholdDataRequest request) {
        Household household = new Household();
        household.setHouseholdNumber(request.getHouseholdNumber());
        household.setHeadOfHouseholdPsn(request.getHeadOfHouseholdPsn());
        household.setMonthlyIncome(request.getMonthlyIncome());
        household.setTotalMembers(request.getTotalMembers());
        household.setRegion(request.getRegion());
        household.setProvince(request.getProvince());
        household.setMunicipality(request.getMunicipality());
        household.setBarangay(request.getBarangay());
        household.setIsIndigenous(request.getIsIndigenous());
        household.setIsPwdHousehold(request.getIsPwdHousehold());
        household.setIsSeniorCitizenHousehold(request.getIsSeniorCitizenHousehold());
        household.setIsSoloParentHousehold(request.getIsSoloParentHousehold());
        household.setHousingType(request.getHousingType());
        household.setHousingTenure(request.getHousingTenure());
        household.setWaterSource(request.getWaterSource());
        household.setToiletFacility(request.getToiletFacility());
        household.setElectricitySource(request.getElectricitySource());
        household.setCookingFuel(request.getCookingFuel());
        household.setStatus(request.getStatus());
        household.setSourceSystem(request.getSourceSystem());
        household.setPreferredLanguage(request.getPreferredLanguage());
        household.setNotes(request.getNotes());
        household.setRegistrationDate(request.getRegistrationDate() != null ? request.getRegistrationDate() : LocalDateTime.now());
        
        return household;
    }

    private void updateEntityFromRequest(Household household, HouseholdDataRequest request) {
        household.setMonthlyIncome(request.getMonthlyIncome());
        household.setTotalMembers(request.getTotalMembers());
        household.setRegion(request.getRegion());
        household.setProvince(request.getProvince());
        household.setMunicipality(request.getMunicipality());
        household.setBarangay(request.getBarangay());
        household.setIsIndigenous(request.getIsIndigenous());
        household.setIsPwdHousehold(request.getIsPwdHousehold());
        household.setIsSeniorCitizenHousehold(request.getIsSeniorCitizenHousehold());
        household.setIsSoloParentHousehold(request.getIsSoloParentHousehold());
        household.setHousingType(request.getHousingType());
        household.setHousingTenure(request.getHousingTenure());
        household.setWaterSource(request.getWaterSource());
        household.setToiletFacility(request.getToiletFacility());
        household.setElectricitySource(request.getElectricitySource());
        household.setCookingFuel(request.getCookingFuel());
        household.setStatus(request.getStatus());
        household.setPreferredLanguage(request.getPreferredLanguage());
        household.setNotes(request.getNotes());
    }

    private HouseholdDataResponse mapEntityToResponse(Household household, String status, String message) {
        return HouseholdDataResponse.builder()
            .status(status)
            .message(message)
            .householdId(household.getId())
            .householdNumber(household.getHouseholdNumber())
            .headOfHouseholdPsn(household.getHeadOfHouseholdPsn())
            .monthlyIncome(household.getMonthlyIncome())
            .totalMembers(household.getTotalMembers())
            .region(household.getRegion())
            .province(household.getProvince())
            .municipality(household.getMunicipality())
            .barangay(household.getBarangay())
            .isIndigenous(household.getIsIndigenous())
            .isPwdHousehold(household.getIsPwdHousehold())
            .isSeniorCitizenHousehold(household.getIsSeniorCitizenHousehold())
            .isSoloParentHousehold(household.getIsSoloParentHousehold())
            .housingType(household.getHousingType())
            .housingTenure(household.getHousingTenure())
            .waterSource(household.getWaterSource())
            .toiletFacility(household.getToiletFacility())
            .electricitySource(household.getElectricitySource())
            .cookingFuel(household.getCookingFuel())
            .householdStatus(household.getStatus())
            .sourceSystem(household.getSourceSystem())
            .preferredLanguage(household.getPreferredLanguage())
            .notes(household.getNotes())
            .registrationDate(household.getRegistrationDate())
            .createdAt(household.getCreatedAt())
            .updatedAt(household.getUpdatedAt())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> searchHouseholds(Map<String, Object> criteria) {
        log.info("Searching households with criteria: {}", criteria);
        // Implementation would use criteria to build dynamic query
        // For now, return empty list as placeholder
        return Collections.emptyList();
    }

    @Override
    public HouseholdDataResponse validateHousehold(HouseholdDataRequest request) {
        log.info("Validating household data for: {}", request.getHouseholdNumber());

        try {
            boolean isValid = dataValidationService.validateHouseholdData(request);
            if (isValid) {
                return HouseholdDataResponse.success("Household data is valid");
            } else {
                return HouseholdDataResponse.error("Household data validation failed");
            }
        } catch (Exception e) {
            log.error("Error validating household: {}", e.getMessage(), e);
            return HouseholdDataResponse.error("Validation error: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> getHouseholdsByStatus(String status) {
        log.info("Getting households by status: {}", status);

        try {
            List<Household> households = householdRepository.findByStatus(status);
            return households.stream()
                .map(h -> mapEntityToResponse(h, "SUCCESS", "Household retrieved successfully"))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting households by status: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> getHouseholdsBySourceSystem(String sourceSystem) {
        log.info("Getting households by source system: {}", sourceSystem);

        try {
            List<Household> households = householdRepository.findBySourceSystem(sourceSystem);
            return households.stream()
                .map(h -> mapEntityToResponse(h, "SUCCESS", "Household retrieved successfully"))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting households by source system: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> getVulnerableHouseholds() {
        log.info("Getting vulnerable households");

        try {
            List<Household> households = householdRepository.findVulnerableHouseholds();
            return households.stream()
                .map(h -> mapEntityToResponse(h, "SUCCESS", "Household retrieved successfully"))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting vulnerable households: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<HouseholdDataResponse> getHouseholdsWithDataQualityIssues() {
        log.info("Getting households with data quality issues");

        try {
            List<Household> households = householdRepository.findHouseholdsWithDataQualityIssues();
            return households.stream()
                .map(h -> mapEntityToResponse(h, "SUCCESS", "Household retrieved successfully"))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting households with data quality issues: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<HouseholdDataResponse> createHouseholdsBatch(List<HouseholdDataRequest> requests) {
        log.info("Creating batch of {} households", requests.size());

        List<HouseholdDataResponse> responses = new ArrayList<>();
        for (HouseholdDataRequest request : requests) {
            responses.add(createHousehold(request));
        }
        return responses;
    }

    @Override
    public List<HouseholdDataResponse> updateHouseholdsBatch(List<HouseholdDataRequest> requests) {
        log.info("Updating batch of {} households", requests.size());

        List<HouseholdDataResponse> responses = new ArrayList<>();
        for (HouseholdDataRequest request : requests) {
            // Find household by number for update
            Optional<Household> existing = householdRepository.findByHouseholdNumber(request.getHouseholdNumber());
            if (existing.isPresent()) {
                responses.add(updateHousehold(existing.get().getId(), request));
            } else {
                responses.add(HouseholdDataResponse.error("Household not found: " + request.getHouseholdNumber()));
            }
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getHouseholdCountByRegion() {
        log.info("Getting household count by region");

        try {
            List<Object[]> counts = householdRepository.getHouseholdCountByRegion();
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : counts) {
                result.put((String) row[0], (Long) row[1]);
            }
            return result;
        } catch (Exception e) {
            log.error("Error getting household count by region: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getHouseholdStatisticsByRegion() {
        log.info("Getting household statistics by region");

        try {
            List<Object[]> stats = householdRepository.getHouseholdStatisticsByRegion();
            Map<String, Object> result = new HashMap<>();
            for (Object[] row : stats) {
                Map<String, Object> regionStats = new HashMap<>();
                regionStats.put("count", row[1]);
                regionStats.put("averageMembers", row[2]);
                regionStats.put("averageIncome", row[3]);
                result.put((String) row[0], regionStats);
            }
            return result;
        } catch (Exception e) {
            log.error("Error getting household statistics by region: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean householdNumberExists(String householdNumber) {
        return householdRepository.existsByHouseholdNumber(householdNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean psnExistsAsHead(String psn) {
        return householdRepository.findByHeadOfHouseholdPsn(psn).isPresent();
    }
}
