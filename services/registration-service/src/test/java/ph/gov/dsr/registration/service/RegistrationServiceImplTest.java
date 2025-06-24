package ph.gov.dsr.registration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ph.gov.dsr.registration.dto.RegistrationCreateRequest;
import ph.gov.dsr.registration.dto.RegistrationResponse;
import ph.gov.dsr.registration.entity.Registration;
import ph.gov.dsr.registration.repository.RegistrationRepository;
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

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    private RegistrationCreateRequest validRequest;
    private Registration sampleRegistration;

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationRequest();
        validRequest.setPsn("1234-5678-9012");
        validRequest.setFirstName("Juan");
        validRequest.setLastName("Dela Cruz");
        validRequest.setEmail("juan.delacruz@email.com");
        validRequest.setPhoneNumber("09171234567");
        validRequest.setProgramCode("4PS");

        sampleRegistration = new Registration();
        sampleRegistration.setId(UUID.randomUUID());
        sampleRegistration.setPsn("1234-5678-9012");
        sampleRegistration.setFirstName("Juan");
        sampleRegistration.setLastName("Dela Cruz");
        sampleRegistration.setEmail("juan.delacruz@email.com");
        sampleRegistration.setPhoneNumber("09171234567");
        sampleRegistration.setProgramCode("4PS");
        sampleRegistration.setStatus(Registration.RegistrationStatus.PENDING);
        sampleRegistration.setSubmissionDate(LocalDateTime.now());
    }

    @Test
    void testCreateRegistration_Success() {
        // Given
        when(registrationRepository.existsByPsnAndProgramCode(anyString(), anyString())).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenReturn(sampleRegistration);

        // When
        RegistrationResponse response = registrationService.createRegistration(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(sampleRegistration.getPsn(), response.getPsn());
        assertEquals(sampleRegistration.getProgramCode(), response.getProgramCode());
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    void testCreateRegistration_DuplicateRegistration() {
        // Given
        when(registrationRepository.existsByPsnAndProgramCode(anyString(), anyString())).thenReturn(true);

        // When
        RegistrationResponse response = registrationService.createRegistration(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("ERROR", response.getStatus());
        assertTrue(response.getErrorMessage().contains("already registered"));
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    void testGetRegistrationById_Found() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(sampleRegistration));

        // When
        Optional<Registration> result = registrationService.getRegistrationById(registrationId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sampleRegistration.getPsn(), result.get().getPsn());
        verify(registrationRepository).findById(registrationId);
    }

    @Test
    void testGetRegistrationById_NotFound() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        // When
        Optional<Registration> result = registrationService.getRegistrationById(registrationId);

        // Then
        assertFalse(result.isPresent());
        verify(registrationRepository).findById(registrationId);
    }

    @Test
    void testGetRegistrationsByPsn() {
        // Given
        String psn = "1234-5678-9012";
        when(registrationRepository.findByPsnOrderBySubmissionDateDesc(psn))
                .thenReturn(Arrays.asList(sampleRegistration));

        // When
        var registrations = registrationService.getRegistrationsByPsn(psn);

        // Then
        assertNotNull(registrations);
        assertEquals(1, registrations.size());
        assertEquals(sampleRegistration.getPsn(), registrations.get(0).getPsn());
        verify(registrationRepository).findByPsnOrderBySubmissionDateDesc(psn);
    }

    @Test
    void testUpdateRegistrationStatus_Success() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(sampleRegistration));
        when(registrationRepository.save(any(Registration.class))).thenReturn(sampleRegistration);

        // When
        Registration result = registrationService.updateRegistrationStatus(
                registrationId, Registration.RegistrationStatus.APPROVED, "Test approval", "admin");

        // Then
        assertNotNull(result);
        assertEquals(Registration.RegistrationStatus.APPROVED, result.getStatus());
        verify(registrationRepository).findById(registrationId);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    void testUpdateRegistrationStatus_NotFound() {
        // Given
        UUID registrationId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            registrationService.updateRegistrationStatus(
                    registrationId, Registration.RegistrationStatus.APPROVED, "Test approval", "admin");
        });
        verify(registrationRepository).findById(registrationId);
        verify(registrationRepository, never()).save(any(Registration.class));
    }

    @Test
    void testSearchRegistrations() {
        // Given
        String searchText = "Juan";
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Registration> mockPage = new PageImpl<>(Arrays.asList(sampleRegistration));
        when(registrationRepository.searchRegistrations(eq(searchText), eq(pageRequest)))
                .thenReturn(mockPage);

        // When
        Page<Registration> result = registrationService.searchRegistrations(searchText, pageRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(sampleRegistration.getPsn(), result.getContent().get(0).getPsn());
        verify(registrationRepository).searchRegistrations(searchText, pageRequest);
    }

    @Test
    void testGetRegistrationStatistics() {
        // Given
        Object[] mockStats = {100L, 80L, 15L, 5L, 85.5};
        when(registrationRepository.getRegistrationStatistics()).thenReturn(mockStats);

        // When
        Object[] result = registrationService.getRegistrationStatistics();

        // Then
        assertNotNull(result);
        assertEquals(5, result.length);
        assertEquals(100L, result[0]);
        verify(registrationRepository).getRegistrationStatistics();
    }

    @Test
    void testValidateRegistrationRequest_ValidRequest() {
        // When
        boolean isValid = registrationService.validateRegistrationRequest(validRequest);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateRegistrationRequest_InvalidPsn() {
        // Given
        validRequest.setPsn("invalid-psn");

        // When
        boolean isValid = registrationService.validateRegistrationRequest(validRequest);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateRegistrationRequest_MissingRequiredFields() {
        // Given
        validRequest.setFirstName(null);

        // When
        boolean isValid = registrationService.validateRegistrationRequest(validRequest);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetPendingRegistrations() {
        // Given
        when(registrationRepository.findByStatusOrderBySubmissionDateAsc(Registration.RegistrationStatus.PENDING))
                .thenReturn(Arrays.asList(sampleRegistration));

        // When
        var result = registrationService.getPendingRegistrations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Registration.RegistrationStatus.PENDING, result.get(0).getStatus());
        verify(registrationRepository).findByStatusOrderBySubmissionDateAsc(Registration.RegistrationStatus.PENDING);
    }

    @Test
    void testGetRegistrationsByProgram() {
        // Given
        String programCode = "4PS";
        when(registrationRepository.findByProgramCodeOrderBySubmissionDateDesc(programCode))
                .thenReturn(Arrays.asList(sampleRegistration));

        // When
        var result = registrationService.getRegistrationsByProgram(programCode);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(programCode, result.get(0).getProgramCode());
        verify(registrationRepository).findByProgramCodeOrderBySubmissionDateDesc(programCode);
    }
}
