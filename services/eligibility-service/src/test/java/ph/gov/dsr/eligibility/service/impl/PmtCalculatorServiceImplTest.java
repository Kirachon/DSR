package ph.gov.dsr.eligibility.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.eligibility.dto.EligibilityRequest;
import ph.gov.dsr.eligibility.service.PmtCalculatorService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PmtCalculatorServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class PmtCalculatorServiceImplTest {

    @InjectMocks
    private PmtCalculatorServiceImpl pmtCalculatorService;

    private EligibilityRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new EligibilityRequest();
        testRequest.setPsn("1234-5678-9012");
        testRequest.setProgramCode("4PS_CONDITIONAL_CASH");
        
        EligibilityRequest.HouseholdInfo householdInfo = new EligibilityRequest.HouseholdInfo();
        householdInfo.setHouseholdNumber("HH-2024-001");
        householdInfo.setMonthlyIncome(new BigDecimal("15000"));
        householdInfo.setTotalMembers(5);
        householdInfo.setIsIndigenous(false);
        householdInfo.setHasPwdMembers(false);
        
        EligibilityRequest.LocationInfo location = new EligibilityRequest.LocationInfo();
        location.setRegion("NCR");
        location.setProvince("Metro Manila");
        location.setCityMunicipality("Manila");
        householdInfo.setLocation(location);
        
        testRequest.setHouseholdInfo(householdInfo);
    }

    @Test
    void testCalculatePmtScore_Success() {
        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPmtScore());
        assertNotNull(result.getPovertyThreshold());
        assertEquals("NCR", result.getRegion());
        assertEquals(5, result.getHouseholdSize());
        assertNotNull(result.getCalculatedAt());
        assertEquals("DSWD_PMT_2024", result.getCalculationMethod());
        assertNotNull(result.getComponentScores());
        assertTrue(result.getComponentScores().containsKey("demographic"));
        assertTrue(result.getComponentScores().containsKey("education"));
        assertTrue(result.getComponentScores().containsKey("housing"));
        assertTrue(result.getComponentScores().containsKey("assets"));
        assertTrue(result.getComponentScores().containsKey("income"));
    }

    @Test
    void testCalculatePmtScore_WithCustomData() {
        // Arrange
        Map<String, Object> householdData = new HashMap<>();
        householdData.put("totalMembers", 4);
        householdData.put("monthlyIncome", new BigDecimal("12000"));
        householdData.put("childrenCount", 2);
        householdData.put("elderlyCount", 1);
        householdData.put("hasPwdMembers", true);
        householdData.put("isIndigenous", true);

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(householdData, "NCR");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPmtScore());
        assertEquals("NCR", result.getRegion());
        assertEquals(4, result.getHouseholdSize());
        assertTrue(result.getComponentScores().size() == 5);
    }

    @Test
    void testGetPovertyThreshold_NCR() {
        // Act
        BigDecimal threshold = pmtCalculatorService.getPovertyThreshold("NCR", 5);

        // Assert
        assertNotNull(threshold);
        assertEquals(new BigDecimal("36000"), threshold);
    }

    @Test
    void testGetPovertyThreshold_DefaultRegion() {
        // Act
        BigDecimal threshold = pmtCalculatorService.getPovertyThreshold("UNKNOWN_REGION", 5);

        // Assert
        assertNotNull(threshold);
        assertEquals(new BigDecimal("27000"), threshold);
    }

    @Test
    void testGetPovertyThreshold_ClosestHouseholdSize() {
        // Test with household size not in the map (7 members)
        // Should return threshold for closest size (6 members)
        
        // Act
        BigDecimal threshold = pmtCalculatorService.getPovertyThreshold("NCR", 7);

        // Assert
        assertNotNull(threshold);
        assertEquals(new BigDecimal("42000"), threshold); // 6-member threshold
    }

    @Test
    void testIsPoorHousehold_Poor() {
        // Arrange
        BigDecimal pmtScore = new BigDecimal("25000");
        
        // Act
        boolean isPoor = pmtCalculatorService.isPoorHousehold(pmtScore, "NCR", 5);

        // Assert
        assertTrue(isPoor);
    }

    @Test
    void testIsPoorHousehold_NotPoor() {
        // Arrange
        BigDecimal pmtScore = new BigDecimal("40000");
        
        // Act
        boolean isPoor = pmtCalculatorService.isPoorHousehold(pmtScore, "NCR", 5);

        // Assert
        assertFalse(isPoor);
    }

    @Test
    void testGetPmtScoreBreakdown() {
        // Act
        Map<String, Object> breakdown = pmtCalculatorService.getPmtScoreBreakdown(testRequest);

        // Assert
        assertNotNull(breakdown);
        assertTrue(breakdown.containsKey("pmtScore"));
        assertTrue(breakdown.containsKey("povertyThreshold"));
        assertTrue(breakdown.containsKey("isPoor"));
        assertTrue(breakdown.containsKey("componentScores"));
        assertTrue(breakdown.containsKey("calculationDetails"));
        assertTrue(breakdown.containsKey("region"));
        assertTrue(breakdown.containsKey("householdSize"));
        assertTrue(breakdown.containsKey("calculatedAt"));
    }

    @Test
    void testUpdatePovertyThresholds() {
        // Arrange
        Map<Integer, BigDecimal> newThresholds = new HashMap<>();
        newThresholds.put(1, new BigDecimal("15000"));
        newThresholds.put(2, new BigDecimal("22500"));
        newThresholds.put(3, new BigDecimal("30000"));

        // Act
        pmtCalculatorService.updatePovertyThresholds("TEST_REGION", newThresholds);
        BigDecimal threshold = pmtCalculatorService.getPovertyThreshold("TEST_REGION", 2);

        // Assert
        assertEquals(new BigDecimal("22500"), threshold);
    }

    @Test
    void testCalculatePmtScore_WithIndigenousHousehold() {
        // Arrange
        testRequest.getHouseholdInfo().setIsIndigenous(true);

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPmtScore());
        // Indigenous households should get additional points in demographic score
        assertTrue(result.getComponentScores().get("demographic").compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculatePmtScore_WithPwdMembers() {
        // Arrange
        testRequest.getHouseholdInfo().setHasPwdMembers(true);

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPmtScore());
        // PWD households should get additional points in demographic score
        assertTrue(result.getComponentScores().get("demographic").compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculatePmtScore_WithLowIncome() {
        // Arrange
        testRequest.getHouseholdInfo().setMonthlyIncome(new BigDecimal("8000"));

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isPoor());
        assertTrue(result.getPmtScore().compareTo(result.getPovertyThreshold()) <= 0);
    }

    @Test
    void testCalculatePmtScore_WithHighIncome() {
        // Arrange
        testRequest.getHouseholdInfo().setMonthlyIncome(new BigDecimal("50000"));

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isPoor());
        assertTrue(result.getPmtScore().compareTo(result.getPovertyThreshold()) > 0);
    }

    @Test
    void testCalculatePmtScore_ComponentWeights() {
        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result.getCalculationDetails());
        Map<String, Object> details = result.getCalculationDetails();
        assertTrue(details.containsKey("weights"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> weights = (Map<String, String>) details.get("weights");
        assertEquals("30%", weights.get("demographic"));
        assertEquals("25%", weights.get("education"));
        assertEquals("20%", weights.get("housing"));
        assertEquals("15%", weights.get("assets"));
        assertEquals("10%", weights.get("income"));
    }

    @Test
    void testCalculatePmtScore_NullHouseholdInfo() {
        // Arrange
        testRequest.setHouseholdInfo(null);

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("DEFAULT", result.getRegion());
        assertEquals(1, result.getHouseholdSize()); // Default value
    }

    @Test
    void testCalculatePmtScore_EmptyAdditionalParameters() {
        // Arrange
        testRequest.setAdditionalParameters(new HashMap<>());

        // Act
        PmtCalculatorService.PmtCalculationResult result = pmtCalculatorService.calculatePmtScore(testRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPmtScore());
    }
}
