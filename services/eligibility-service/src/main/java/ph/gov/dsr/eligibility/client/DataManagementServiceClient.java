package ph.gov.dsr.eligibility.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Client for integrating with Data Management Service
 * Fetches household and member data for eligibility assessments
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Component
@Slf4j
public class DataManagementServiceClient {

    private final RestTemplate restTemplate;
    private final String dataManagementServiceUrl;

    @Autowired
    public DataManagementServiceClient(RestTemplate restTemplate,
                                     @Value("${dsr.services.data-management.url:http://localhost:8082}") 
                                     String dataManagementServiceUrl) {
        this.restTemplate = restTemplate;
        this.dataManagementServiceUrl = dataManagementServiceUrl;
    }

    /**
     * Fetch household data by PSN
     */
    public Optional<HouseholdData> getHouseholdByPsn(String psn, String authToken) {
        log.debug("Fetching household data for PSN: {}", psn);
        
        try {
            String url = dataManagementServiceUrl + "/api/v1/households/by-psn/" + psn;
            
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<HouseholdData> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, HouseholdData.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Successfully fetched household data for PSN: {}", psn);
                return Optional.of(response.getBody());
            }
            
            log.warn("No household data found for PSN: {}", psn);
            return Optional.empty();
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Household not found for PSN: {}", psn);
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            log.error("HTTP error fetching household data for PSN {}: {} - {}", 
                     psn, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.error("Connection error fetching household data for PSN {}: {}", psn, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error fetching household data for PSN {}: {}", psn, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Fetch household data by household number
     */
    public Optional<HouseholdData> getHouseholdByNumber(String householdNumber, String authToken) {
        log.debug("Fetching household data for household number: {}", householdNumber);
        
        try {
            String url = dataManagementServiceUrl + "/api/v1/households/by-number/" + householdNumber;
            
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<HouseholdData> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, HouseholdData.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Successfully fetched household data for household number: {}", householdNumber);
                return Optional.of(response.getBody());
            }
            
            log.warn("No household data found for household number: {}", householdNumber);
            return Optional.empty();
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Household not found for household number: {}", householdNumber);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching household data for household number {}: {}", householdNumber, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Fetch economic profile for household
     */
    public Optional<EconomicProfileData> getEconomicProfile(UUID householdId, String authToken) {
        log.debug("Fetching economic profile for household ID: {}", householdId);
        
        try {
            String url = dataManagementServiceUrl + "/api/v1/economic-profiles/household/" + householdId;
            
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<EconomicProfileData> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, EconomicProfileData.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Successfully fetched economic profile for household ID: {}", householdId);
                return Optional.of(response.getBody());
            }
            
            log.warn("No economic profile found for household ID: {}", householdId);
            return Optional.empty();
            
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Economic profile not found for household ID: {}", householdId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching economic profile for household ID {}: {}", householdId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Check if Data Management Service is available
     */
    public boolean isServiceAvailable() {
        try {
            String url = dataManagementServiceUrl + "/actuator/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Data Management Service is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create authentication headers
     */
    private HttpHeaders createAuthHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null && !authToken.trim().isEmpty()) {
            headers.setBearerAuth(authToken.replace("Bearer ", ""));
        }
        return headers;
    }

    // DTOs for service integration
    
    public static class HouseholdData {
        private UUID id;
        private String householdNumber;
        private String headOfHouseholdPsn;
        private Integer totalMembers;
        private BigDecimal monthlyIncome;
        private String region;
        private String province;
        private String municipality;
        private String barangay;
        private Boolean isIndigenous;
        private Boolean isPwdHousehold;
        private Boolean isSeniorCitizenHousehold;
        private Boolean isSoloParentHousehold;
        private String housingType;
        private String housingTenure;
        private String waterSource;
        private String toiletFacility;
        private String electricitySource;
        private String cookingFuel;
        private String status;
        private String sourceSystem;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getHouseholdNumber() { return householdNumber; }
        public void setHouseholdNumber(String householdNumber) { this.householdNumber = householdNumber; }
        public String getHeadOfHouseholdPsn() { return headOfHouseholdPsn; }
        public void setHeadOfHouseholdPsn(String headOfHouseholdPsn) { this.headOfHouseholdPsn = headOfHouseholdPsn; }
        public Integer getTotalMembers() { return totalMembers; }
        public void setTotalMembers(Integer totalMembers) { this.totalMembers = totalMembers; }
        public BigDecimal getMonthlyIncome() { return monthlyIncome; }
        public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getMunicipality() { return municipality; }
        public void setMunicipality(String municipality) { this.municipality = municipality; }
        public String getBarangay() { return barangay; }
        public void setBarangay(String barangay) { this.barangay = barangay; }
        public Boolean getIsIndigenous() { return isIndigenous; }
        public void setIsIndigenous(Boolean isIndigenous) { this.isIndigenous = isIndigenous; }
        public Boolean getIsPwdHousehold() { return isPwdHousehold; }
        public void setIsPwdHousehold(Boolean isPwdHousehold) { this.isPwdHousehold = isPwdHousehold; }
        public Boolean getIsSeniorCitizenHousehold() { return isSeniorCitizenHousehold; }
        public void setIsSeniorCitizenHousehold(Boolean isSeniorCitizenHousehold) { this.isSeniorCitizenHousehold = isSeniorCitizenHousehold; }
        public Boolean getIsSoloParentHousehold() { return isSoloParentHousehold; }
        public void setIsSoloParentHousehold(Boolean isSoloParentHousehold) { this.isSoloParentHousehold = isSoloParentHousehold; }
        public String getHousingType() { return housingType; }
        public void setHousingType(String housingType) { this.housingType = housingType; }
        public String getHousingTenure() { return housingTenure; }
        public void setHousingTenure(String housingTenure) { this.housingTenure = housingTenure; }
        public String getWaterSource() { return waterSource; }
        public void setWaterSource(String waterSource) { this.waterSource = waterSource; }
        public String getToiletFacility() { return toiletFacility; }
        public void setToiletFacility(String toiletFacility) { this.toiletFacility = toiletFacility; }
        public String getElectricitySource() { return electricitySource; }
        public void setElectricitySource(String electricitySource) { this.electricitySource = electricitySource; }
        public String getCookingFuel() { return cookingFuel; }
        public void setCookingFuel(String cookingFuel) { this.cookingFuel = cookingFuel; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSourceSystem() { return sourceSystem; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    }

    public static class EconomicProfileData {
        private UUID id;
        private UUID householdId;
        private BigDecimal totalHouseholdIncome;
        private BigDecimal perCapitaIncome;
        private BigDecimal totalAssetsValue;
        private BigDecimal pmtScore;
        private BigDecimal povertyThreshold;
        private Boolean isPoor;
        private Boolean hasSalaryIncome;
        private Boolean hasBusinessIncome;
        private Boolean hasAgriculturalIncome;
        private Boolean hasRemittanceIncome;
        private Boolean ownsHouse;
        private Boolean ownsLand;
        private Boolean ownsVehicle;
        private Boolean hasSavings;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getHouseholdId() { return householdId; }
        public void setHouseholdId(UUID householdId) { this.householdId = householdId; }
        public BigDecimal getTotalHouseholdIncome() { return totalHouseholdIncome; }
        public void setTotalHouseholdIncome(BigDecimal totalHouseholdIncome) { this.totalHouseholdIncome = totalHouseholdIncome; }
        public BigDecimal getPerCapitaIncome() { return perCapitaIncome; }
        public void setPerCapitaIncome(BigDecimal perCapitaIncome) { this.perCapitaIncome = perCapitaIncome; }
        public BigDecimal getTotalAssetsValue() { return totalAssetsValue; }
        public void setTotalAssetsValue(BigDecimal totalAssetsValue) { this.totalAssetsValue = totalAssetsValue; }
        public BigDecimal getPmtScore() { return pmtScore; }
        public void setPmtScore(BigDecimal pmtScore) { this.pmtScore = pmtScore; }
        public BigDecimal getPovertyThreshold() { return povertyThreshold; }
        public void setPovertyThreshold(BigDecimal povertyThreshold) { this.povertyThreshold = povertyThreshold; }
        public Boolean getIsPoor() { return isPoor; }
        public void setIsPoor(Boolean isPoor) { this.isPoor = isPoor; }
        public Boolean getHasSalaryIncome() { return hasSalaryIncome; }
        public void setHasSalaryIncome(Boolean hasSalaryIncome) { this.hasSalaryIncome = hasSalaryIncome; }
        public Boolean getHasBusinessIncome() { return hasBusinessIncome; }
        public void setHasBusinessIncome(Boolean hasBusinessIncome) { this.hasBusinessIncome = hasBusinessIncome; }
        public Boolean getHasAgriculturalIncome() { return hasAgriculturalIncome; }
        public void setHasAgriculturalIncome(Boolean hasAgriculturalIncome) { this.hasAgriculturalIncome = hasAgriculturalIncome; }
        public Boolean getHasRemittanceIncome() { return hasRemittanceIncome; }
        public void setHasRemittanceIncome(Boolean hasRemittanceIncome) { this.hasRemittanceIncome = hasRemittanceIncome; }
        public Boolean getOwnsHouse() { return ownsHouse; }
        public void setOwnsHouse(Boolean ownsHouse) { this.ownsHouse = ownsHouse; }
        public Boolean getOwnsLand() { return ownsLand; }
        public void setOwnsLand(Boolean ownsLand) { this.ownsLand = ownsLand; }
        public Boolean getOwnsVehicle() { return ownsVehicle; }
        public void setOwnsVehicle(Boolean ownsVehicle) { this.ownsVehicle = ownsVehicle; }
        public Boolean getHasSavings() { return hasSavings; }
        public void setHasSavings(Boolean hasSavings) { this.hasSavings = hasSavings; }
    }
}
