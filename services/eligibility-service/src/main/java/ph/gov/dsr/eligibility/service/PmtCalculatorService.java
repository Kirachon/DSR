package ph.gov.dsr.eligibility.service;

import ph.gov.dsr.eligibility.dto.EligibilityRequest;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service interface for Proxy Means Test (PMT) calculations
 * Implements official DSWD PMT formulas for poverty assessment
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
public interface PmtCalculatorService {

    /**
     * Calculate PMT score for a household using DSWD formulas
     * 
     * @param request Eligibility request containing household information
     * @return PMT calculation result with score and breakdown
     */
    PmtCalculationResult calculatePmtScore(EligibilityRequest request);

    /**
     * Calculate PMT score with custom parameters
     * 
     * @param householdData Household demographic and economic data
     * @param region Geographic region for regional adjustments
     * @return PMT calculation result
     */
    PmtCalculationResult calculatePmtScore(Map<String, Object> householdData, String region);

    /**
     * Get poverty threshold for a specific region
     * 
     * @param region Geographic region code
     * @param householdSize Number of household members
     * @return Poverty threshold amount
     */
    BigDecimal getPovertyThreshold(String region, Integer householdSize);

    /**
     * Validate if household meets poverty criteria based on PMT score
     * 
     * @param pmtScore Calculated PMT score
     * @param region Geographic region
     * @param householdSize Number of household members
     * @return True if household is below poverty threshold
     */
    boolean isPoorHousehold(BigDecimal pmtScore, String region, Integer householdSize);

    /**
     * Get PMT score breakdown for transparency
     * 
     * @param request Eligibility request
     * @return Detailed breakdown of PMT calculation components
     */
    Map<String, Object> getPmtScoreBreakdown(EligibilityRequest request);

    /**
     * Update regional poverty thresholds (admin function)
     * 
     * @param region Geographic region code
     * @param thresholds Map of household size to threshold amounts
     */
    void updatePovertyThresholds(String region, Map<Integer, BigDecimal> thresholds);

    /**
     * PMT calculation result
     */
    class PmtCalculationResult {
        private BigDecimal pmtScore;
        private BigDecimal povertyThreshold;
        private String region;
        private Integer householdSize;
        private boolean isPoor;
        private Map<String, BigDecimal> componentScores;
        private Map<String, Object> calculationDetails;
        private String calculationMethod;
        private java.time.LocalDateTime calculatedAt;

        // Getters and setters
        public BigDecimal getPmtScore() { return pmtScore; }
        public void setPmtScore(BigDecimal pmtScore) { this.pmtScore = pmtScore; }
        
        public BigDecimal getPovertyThreshold() { return povertyThreshold; }
        public void setPovertyThreshold(BigDecimal povertyThreshold) { this.povertyThreshold = povertyThreshold; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public Integer getHouseholdSize() { return householdSize; }
        public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }
        
        public boolean isPoor() { return isPoor; }
        public void setPoor(boolean poor) { isPoor = poor; }
        
        public Map<String, BigDecimal> getComponentScores() { return componentScores; }
        public void setComponentScores(Map<String, BigDecimal> componentScores) { this.componentScores = componentScores; }
        
        public Map<String, Object> getCalculationDetails() { return calculationDetails; }
        public void setCalculationDetails(Map<String, Object> calculationDetails) { this.calculationDetails = calculationDetails; }
        
        public String getCalculationMethod() { return calculationMethod; }
        public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
        
        public java.time.LocalDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(java.time.LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    }
}
