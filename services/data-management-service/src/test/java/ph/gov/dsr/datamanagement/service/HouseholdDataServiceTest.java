package ph.gov.dsr.datamanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.datamanagement.dto.HouseholdDataRequest;
import ph.gov.dsr.datamanagement.dto.HouseholdDataResponse;
import ph.gov.dsr.datamanagement.entity.Household;
import ph.gov.dsr.datamanagement.entity.HouseholdMember;
import ph.gov.dsr.datamanagement.repository.HouseholdRepository;
import ph.gov.dsr.datamanagement.service.impl.HouseholdDataServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HouseholdDataService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class HouseholdDataServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private DataValidationService dataValidationService;

    @Mock
    private PhilSysIntegrationService philSysIntegrationService;

    @InjectMocks
    private HouseholdDataServiceImpl householdDataService;

    private HouseholdDataRequest validRequest;
    private Household sampleHousehold;
    private HouseholdMember sampleMember;

    @BeforeEach
    void setUp() {
        validRequest = new HouseholdDataRequest();
        validRequest.setHouseholdNumber("HH-2024-001234");
        validRequest.setHeadOfHouseholdPsn("1234-5678-9012");
        validRequest.setMonthlyIncome(new BigDecimal("15000"));
        validRequest.setTotalMembers(5);
        validRequest.setRegion("NCR");
        validRequest.setProvince("Metro Manila");
        validRequest.setMunicipality("Quezon City");
        validRequest.setBarangay("Barangay Commonwealth");

        sampleHousehold = new Household();
        sampleHousehold.setId(UUID.randomUUID());
        sampleHousehold.setHouseholdNumber("HH-2024-001234");
        sampleHousehold.setHeadOfHouseholdPsn("1234-5678-9012");
        sampleHousehold.setMonthlyIncome(new BigDecimal("15000"));
        sampleHousehold.setTotalMembers(5);
        sampleHousehold.setRegion("NCR");
        sampleHousehold.setProvince("Metro Manila");
        sampleHousehold.setMunicipality("Quezon City");
        sampleHousehold.setBarangay("Barangay Commonwealth");
        sampleHousehold.setStatus(Household.HouseholdStatus.ACTIVE);
        sampleHousehold.setCreatedAt(LocalDateTime.now());

        sampleMember = new HouseholdMember();
        sampleMember.setId(UUID.randomUUID());
        sampleMember.setHousehold(sampleHousehold);
        sampleMember.setPsn("1234-5678-9012");
        sampleMember.setFirstName("Juan");
        sampleMember.setLastName("Dela Cruz");
        sampleMember.setDateOfBirth(LocalDate.of(1985, 5, 15));
        sampleMember.setGender("M");
        sampleMember.setIsHeadOfHousehold(true);
        sampleMember.setRelationshipToHead("HEAD");
    }

    @Test
    void testCreateHousehold_Success() {
        // Given
        when(householdRepository.existsByHouseholdNumber(anyString())).thenReturn(false);
        when(dataValidationService.validateHouseholdData(any(HouseholdDataRequest.class))).thenReturn(true);
        when(householdRepository.save(any(Household.class))).thenReturn(sampleHousehold);

        // When
        HouseholdDataResponse response = householdDataService.createHousehold(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(sampleHousehold.getHouseholdNumber(), response.getHouseholdNumber());
        assertEquals(sampleHousehold.getHeadOfHouseholdPsn(), response.getHeadOfHouseholdPsn());
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    void testCreateHousehold_DuplicateHouseholdNumber() {
        // Given
        when(householdRepository.existsByHouseholdNumber(anyString())).thenReturn(true);

        // When
        HouseholdDataResponse response = householdDataService.createHousehold(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getErrorMessage().contains("already exists"));
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    void testCreateHousehold_ValidationFailure() {
        // Given
        when(householdRepository.existsByHouseholdNumber(anyString())).thenReturn(false);
        when(dataValidationService.validateHouseholdData(any(HouseholdDataRequest.class))).thenReturn(false);

        // When
        HouseholdDataResponse response = householdDataService.createHousehold(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getErrorMessage().contains("validation failed"));
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    void testGetHouseholdByNumber_Found() {
        // Given
        String householdNumber = "HH-2024-001234";
        when(householdRepository.findByHouseholdNumber(householdNumber))
                .thenReturn(Optional.of(sampleHousehold));

        // When
        Optional<Household> result = householdDataService.getHouseholdByNumber(householdNumber);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sampleHousehold.getHouseholdNumber(), result.get().getHouseholdNumber());
        verify(householdRepository).findByHouseholdNumber(householdNumber);
    }

    @Test
    void testGetHouseholdByNumber_NotFound() {
        // Given
        String householdNumber = "HH-2024-999999";
        when(householdRepository.findByHouseholdNumber(householdNumber))
                .thenReturn(Optional.empty());

        // When
        Optional<Household> result = householdDataService.getHouseholdByNumber(householdNumber);

        // Then
        assertFalse(result.isPresent());
        verify(householdRepository).findByHouseholdNumber(householdNumber);
    }

    @Test
    void testGetHouseholdByHeadPsn_Found() {
        // Given
        String headPsn = "1234-5678-9012";
        when(householdRepository.findByHeadOfHouseholdPsn(headPsn))
                .thenReturn(Optional.of(sampleHousehold));

        // When
        Optional<Household> result = householdDataService.getHouseholdByHeadPsn(headPsn);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sampleHousehold.getHeadOfHouseholdPsn(), result.get().getHeadOfHouseholdPsn());
        verify(householdRepository).findByHeadOfHouseholdPsn(headPsn);
    }

    @Test
    void testUpdateHousehold_Success() {
        // Given
        UUID householdId = UUID.randomUUID();
        when(householdRepository.findById(householdId)).thenReturn(Optional.of(sampleHousehold));
        when(dataValidationService.validateHouseholdData(any(HouseholdDataRequest.class))).thenReturn(true);
        when(householdRepository.save(any(Household.class))).thenReturn(sampleHousehold);

        // When
        HouseholdDataResponse response = householdDataService.updateHousehold(householdId, validRequest);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        verify(householdRepository).findById(householdId);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    void testUpdateHousehold_NotFound() {
        // Given
        UUID householdId = UUID.randomUUID();
        when(householdRepository.findById(householdId)).thenReturn(Optional.empty());

        // When
        HouseholdDataResponse response = householdDataService.updateHousehold(householdId, validRequest);

        // Then
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getErrorMessage().contains("not found"));
        verify(householdRepository).findById(householdId);
        verify(householdRepository, never()).save(any(Household.class));
    }

    @Test
    void testGetHouseholdsByRegion() {
        // Given
        String region = "NCR";
        when(householdRepository.findByRegionOrderByCreatedAtDesc(region))
                .thenReturn(Arrays.asList(sampleHousehold));

        // When
        var households = householdDataService.getHouseholdsByRegion(region);

        // Then
        assertNotNull(households);
        assertEquals(1, households.size());
        assertEquals(region, households.get(0).getRegion());
        verify(householdRepository).findByRegionOrderByCreatedAtDesc(region);
    }

    @Test
    void testGetHouseholdsByIncomeRange() {
        // Given
        BigDecimal minIncome = new BigDecimal("10000");
        BigDecimal maxIncome = new BigDecimal("20000");
        when(householdRepository.findByMonthlyIncomeBetweenOrderByMonthlyIncomeAsc(minIncome, maxIncome))
                .thenReturn(Arrays.asList(sampleHousehold));

        // When
        var households = householdDataService.getHouseholdsByIncomeRange(minIncome, maxIncome);

        // Then
        assertNotNull(households);
        assertEquals(1, households.size());
        assertTrue(households.get(0).getMonthlyIncome().compareTo(minIncome) >= 0);
        assertTrue(households.get(0).getMonthlyIncome().compareTo(maxIncome) <= 0);
        verify(householdRepository).findByMonthlyIncomeBetweenOrderByMonthlyIncomeAsc(minIncome, maxIncome);
    }

    @Test
    void testGetHouseholdStatistics() {
        // Given
        Object[] mockStats = {1000L, new BigDecimal("18500"), 4.2, 850L};
        when(householdRepository.getHouseholdStatistics()).thenReturn(mockStats);

        // When
        Object[] result = householdDataService.getHouseholdStatistics();

        // Then
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals(1000L, result[0]);
        verify(householdRepository).getHouseholdStatistics();
    }

    @Test
    void testValidateHouseholdData_ValidData() {
        // Given
        when(dataValidationService.validateHouseholdData(any(HouseholdDataRequest.class))).thenReturn(true);

        // When
        boolean isValid = householdDataService.validateHouseholdData(validRequest);

        // Then
        assertTrue(isValid);
        verify(dataValidationService).validateHouseholdData(validRequest);
    }

    @Test
    void testValidateHouseholdData_InvalidData() {
        // Given
        when(dataValidationService.validateHouseholdData(any(HouseholdDataRequest.class))).thenReturn(false);

        // When
        boolean isValid = householdDataService.validateHouseholdData(validRequest);

        // Then
        assertFalse(isValid);
        verify(dataValidationService).validateHouseholdData(validRequest);
    }

    @Test
    void testDeactivateHousehold_Success() {
        // Given
        UUID householdId = UUID.randomUUID();
        when(householdRepository.findById(householdId)).thenReturn(Optional.of(sampleHousehold));
        when(householdRepository.save(any(Household.class))).thenReturn(sampleHousehold);

        // When
        boolean result = householdDataService.deactivateHousehold(householdId, "Test deactivation");

        // Then
        assertTrue(result);
        verify(householdRepository).findById(householdId);
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    void testDeactivateHousehold_NotFound() {
        // Given
        UUID householdId = UUID.randomUUID();
        when(householdRepository.findById(householdId)).thenReturn(Optional.empty());

        // When
        boolean result = householdDataService.deactivateHousehold(householdId, "Test deactivation");

        // Then
        assertFalse(result);
        verify(householdRepository).findById(householdId);
        verify(householdRepository, never()).save(any(Household.class));
    }
}
