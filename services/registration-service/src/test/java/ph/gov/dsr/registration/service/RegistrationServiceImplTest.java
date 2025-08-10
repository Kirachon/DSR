package ph.gov.dsr.registration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.registration.dto.RegistrationCreateRequest;
import ph.gov.dsr.registration.dto.RegistrationResponse;
import ph.gov.dsr.registration.entity.Registration;
import ph.gov.dsr.registration.entity.RegistrationStatus;
import ph.gov.dsr.registration.entity.RegistrationChannel;
import ph.gov.dsr.registration.entity.Household;
import ph.gov.dsr.registration.repository.RegistrationRepository;
import ph.gov.dsr.registration.repository.HouseholdRepository;
import ph.gov.dsr.registration.repository.UserRepository;
import ph.gov.dsr.registration.service.impl.RegistrationServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegistrationServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private RegistrationCreateRequest validRequest;
    private Registration sampleRegistration;

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationCreateRequest();

        // Create household DTO
        RegistrationCreateRequest.HouseholdCreateDto household = new RegistrationCreateRequest.HouseholdCreateDto();
        household.setMonthlyIncome(new java.math.BigDecimal("15000"));
        household.setIsIndigenous(false);
        household.setIsPwdHousehold(false);
        household.setIsSeniorCitizenHousehold(false);
        validRequest.setHousehold(household);

        // Create household member DTO
        RegistrationCreateRequest.HouseholdMemberCreateDto member = new RegistrationCreateRequest.HouseholdMemberCreateDto();
        member.setPsn("1234-5678-9012");
        member.setFirstName("Juan");
        member.setLastName("Dela Cruz");
        member.setBirthDate(java.time.LocalDate.of(1990, 1, 1));
        member.setGender("MALE");
        member.setCivilStatus("SINGLE");
        member.setRelationshipToHead("HEAD");
        member.setIsHeadOfHousehold(true);
        validRequest.setMembers(java.util.Arrays.asList(member));

        // Create address DTO
        RegistrationCreateRequest.HouseholdAddressCreateDto address = new RegistrationCreateRequest.HouseholdAddressCreateDto();
        address.setBarangay("Sample Barangay");
        address.setMunicipality("Sample Municipality");
        address.setProvince("Sample Province");
        address.setRegion("Sample Region");
        validRequest.setAddress(address);

        // Create contact info DTO
        RegistrationCreateRequest.ContactInformationCreateDto contactInfo = new RegistrationCreateRequest.ContactInformationCreateDto();
        contactInfo.setMobileNumber("09171234567");
        contactInfo.setEmailAddress("juan.delacruz@email.com");
        validRequest.setContactInfo(contactInfo);

        // Set other required fields
        validRequest.setConsentGiven(true);

        sampleRegistration = new Registration();
        sampleRegistration.setId(UUID.randomUUID());
        sampleRegistration.setRegistrationNumber("REG-000001");
        sampleRegistration.setStatus(RegistrationStatus.PENDING_VERIFICATION);
        sampleRegistration.setRegistrationChannel(RegistrationChannel.WEB_PORTAL);
        sampleRegistration.setSubmissionDate(LocalDateTime.now());
        sampleRegistration.setNotes("Test registration");
    }

    @Test
    void testCreateRegistration_Success() {
        // Given
        when(householdRepository.getNextHouseholdNumber()).thenReturn(1);
        when(householdRepository.save(any(Household.class))).thenReturn(new Household());
        when(registrationRepository.save(any(Registration.class))).thenReturn(sampleRegistration);

        // When
        RegistrationResponse response = registrationService.createRegistration(validRequest);

        // Then
        assertNotNull(response);
        assertEquals(sampleRegistration.getId(), response.getId());
        assertEquals(sampleRegistration.getRegistrationNumber(), response.getRegistrationNumber());
        assertEquals(sampleRegistration.getStatus(), response.getStatus());
        verify(registrationRepository).save(any(Registration.class));
        verify(householdRepository).save(any(Household.class));
    }

    @Test
    void testGetRegistrationById_Success() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(sampleRegistration));

        // When
        RegistrationResponse response = registrationService.getRegistrationById(registrationId);

        // Then
        assertNotNull(response);
        assertEquals(sampleRegistration.getId(), response.getId());
        assertEquals(sampleRegistration.getRegistrationNumber(), response.getRegistrationNumber());
        verify(registrationRepository).findById(registrationId);
    }

    @Test
    void testGetRegistrationById_NotFound() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            registrationService.getRegistrationById(registrationId);
        });
        verify(registrationRepository).findById(registrationId);
    }



    @Test
    void testGetRegistrationsByStatus() {
        // Given
        RegistrationStatus status = RegistrationStatus.PENDING_VERIFICATION;
        when(registrationRepository.findByStatus(status))
                .thenReturn(Arrays.asList(sampleRegistration));

        // When
        var registrations = registrationService.getRegistrationsByStatus(status);

        // Then
        assertNotNull(registrations);
        assertEquals(1, registrations.size());
        verify(registrationRepository).findByStatus(status);
    }

    @Test
    void testApproveRegistration_Success() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(sampleRegistration));
        when(registrationRepository.save(any(Registration.class))).thenReturn(sampleRegistration);

        // When
        RegistrationResponse result = registrationService.approveRegistration(registrationId, "Test approval");

        // Then
        assertNotNull(result);
        assertEquals(sampleRegistration.getId(), result.getId());
        verify(registrationRepository).findById(registrationId);
        verify(registrationRepository).save(any(Registration.class));
    }
}
