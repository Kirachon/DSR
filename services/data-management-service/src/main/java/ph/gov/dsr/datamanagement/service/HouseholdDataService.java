package ph.gov.dsr.datamanagement.service;

import ph.gov.dsr.datamanagement.dto.HouseholdDataRequest;
import ph.gov.dsr.datamanagement.dto.HouseholdDataResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for household data operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface HouseholdDataService {

    /**
     * Create a new household
     */
    HouseholdDataResponse createHousehold(HouseholdDataRequest request);

    /**
     * Update an existing household
     */
    HouseholdDataResponse updateHousehold(UUID householdId, HouseholdDataRequest request);

    /**
     * Get household by ID
     */
    HouseholdDataResponse getHousehold(UUID householdId);

    /**
     * Get household by household number
     */
    HouseholdDataResponse getHouseholdByNumber(String householdNumber);

    /**
     * Get household by head PSN
     */
    HouseholdDataResponse getHouseholdByHeadPsn(String headPsn);

    /**
     * Delete household
     */
    HouseholdDataResponse deleteHousehold(UUID householdId);

    /**
     * Deactivate household
     */
    boolean deactivateHousehold(UUID householdId, String reason);

    /**
     * Get households by region
     */
    List<HouseholdDataResponse> getHouseholdsByRegion(String region);

    /**
     * Get households by income range
     */
    List<HouseholdDataResponse> getHouseholdsByIncomeRange(BigDecimal minIncome, BigDecimal maxIncome);

    /**
     * Get household statistics
     */
    Map<String, Object> getHouseholdStatistics();

    /**
     * Search households by criteria
     */
    List<HouseholdDataResponse> searchHouseholds(Map<String, Object> criteria);

    /**
     * Validate household data
     */
    HouseholdDataResponse validateHousehold(HouseholdDataRequest request);

    /**
     * Get households by status
     */
    List<HouseholdDataResponse> getHouseholdsByStatus(String status);

    /**
     * Get households by source system
     */
    List<HouseholdDataResponse> getHouseholdsBySourceSystem(String sourceSystem);

    /**
     * Get vulnerable households
     */
    List<HouseholdDataResponse> getVulnerableHouseholds();

    /**
     * Get households with data quality issues
     */
    List<HouseholdDataResponse> getHouseholdsWithDataQualityIssues();

    /**
     * Bulk create households
     */
    List<HouseholdDataResponse> createHouseholdsBatch(List<HouseholdDataRequest> requests);

    /**
     * Bulk update households
     */
    List<HouseholdDataResponse> updateHouseholdsBatch(List<HouseholdDataRequest> requests);

    /**
     * Get household count by region
     */
    Map<String, Long> getHouseholdCountByRegion();

    /**
     * Get household statistics by region
     */
    Map<String, Object> getHouseholdStatisticsByRegion();

    /**
     * Check if household number exists
     */
    boolean householdNumberExists(String householdNumber);

    /**
     * Check if PSN exists as head of household
     */
    boolean psnExistsAsHead(String psn);
}
