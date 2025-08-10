package ph.gov.dsr.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.entity.RegistrationStatus;
import ph.gov.dsr.registration.service.AuthService;
import ph.gov.dsr.registration.service.RegistrationService;
import ph.gov.dsr.registration.service.impl.MockAuthServiceImpl;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Configuration for no-database mode.
 * This configuration provides mock implementations when the 'no-db' profile is active.
 * Uses real JWT authentication with mock user data.
 */
@Configuration
@Profile("no-db")
public class NoDbConfig {

    @Bean
    @Primary
    public AuthService mockAuthService(MockAuthServiceImpl mockAuthServiceImpl) {
        return mockAuthServiceImpl;
    }

    @Bean
    @Primary
    public RegistrationService mockRegistrationService() {
        return new RegistrationService() {
            @Override
            public RegistrationResponse createRegistration(RegistrationCreateRequest request) {
                RegistrationResponse response = new RegistrationResponse();
                response.setId(UUID.randomUUID());
                response.setRegistrationNumber("REG-" + System.currentTimeMillis());
                response.setStatus(RegistrationStatus.DRAFT);
                response.setCreatedAt(LocalDateTime.now());
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse getRegistrationById(UUID id) {
                RegistrationResponse response = new RegistrationResponse();
                response.setId(id);
                response.setRegistrationNumber("REG-" + id.toString().substring(0, 8));
                response.setStatus(RegistrationStatus.DRAFT);
                response.setCreatedAt(LocalDateTime.now().minusDays(1));
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse getRegistrationByNumber(String registrationNumber) {
                RegistrationResponse response = new RegistrationResponse();
                response.setId(UUID.randomUUID());
                response.setRegistrationNumber(registrationNumber);
                response.setStatus(RegistrationStatus.DRAFT);
                response.setCreatedAt(LocalDateTime.now().minusDays(1));
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse updateRegistration(UUID id, RegistrationUpdateRequest request) {
                RegistrationResponse response = getRegistrationById(id);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public void deleteRegistration(UUID id) {
                // Mock deletion - no-op
            }

            @Override
            public RegistrationResponse submitRegistration(UUID id) {
                RegistrationResponse response = getRegistrationById(id);
                response.setStatus(RegistrationStatus.PENDING_VERIFICATION);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse approveRegistration(UUID id, String notes) {
                RegistrationResponse response = getRegistrationById(id);
                response.setStatus(RegistrationStatus.APPROVED);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse rejectRegistration(UUID id, String reason, String notes) {
                RegistrationResponse response = getRegistrationById(id);
                response.setStatus(RegistrationStatus.REJECTED);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse assignRegistration(UUID id, UUID staffId) {
                RegistrationResponse response = getRegistrationById(id);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public Page<RegistrationResponse> getAllRegistrations(Pageable pageable) {
                List<RegistrationResponse> registrations = Arrays.asList(
                    createMockRegistration("REG-001", RegistrationStatus.DRAFT),
                    createMockRegistration("REG-002", RegistrationStatus.PENDING_VERIFICATION),
                    createMockRegistration("REG-003", RegistrationStatus.APPROVED)
                );
                return new PageImpl<>(registrations, pageable, registrations.size());
            }

            @Override
            public List<RegistrationResponse> getRegistrationsByStatus(RegistrationStatus status) {
                return Arrays.asList(createMockRegistration("REG-" + status.name(), status));
            }

            @Override
            public List<RegistrationResponse> getRegistrationsAssignedTo(UUID userId) {
                return Arrays.asList(createMockRegistration("REG-ASSIGNED", RegistrationStatus.PENDING_VERIFICATION));
            }

            @Override
            public Page<RegistrationResponse> searchRegistrations(RegistrationSearchCriteria criteria, Pageable pageable) {
                List<RegistrationResponse> registrations = Arrays.asList(
                    createMockRegistration("REG-SEARCH-001", RegistrationStatus.DRAFT)
                );
                return new PageImpl<>(registrations, pageable, registrations.size());
            }

            @Override
            public Object getRegistrationStatistics() {
                Map<String, Object> stats = new HashMap<>();
                stats.put("total", 100);
                stats.put("draft", 20);
                stats.put("pending", 30);
                stats.put("approved", 40);
                stats.put("rejected", 10);
                return stats;
            }

            @Override
            public List<RegistrationResponse> getOverdueRegistrations() {
                return Arrays.asList(createMockRegistration("REG-OVERDUE", RegistrationStatus.PENDING_VERIFICATION));
            }

            @Override
            public List<RegistrationResponse> getRegistrationsByHousehold(UUID householdId) {
                return Arrays.asList(createMockRegistration("REG-HOUSEHOLD", RegistrationStatus.APPROVED));
            }

            @Override
            public RegistrationResponse updateRegistrationPriority(UUID id, Integer priorityLevel) {
                RegistrationResponse response = getRegistrationById(id);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            @Override
            public RegistrationResponse addNotes(UUID id, String notes) {
                RegistrationResponse response = getRegistrationById(id);
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }

            private RegistrationResponse createMockRegistration(String regNumber, RegistrationStatus status) {
                RegistrationResponse response = new RegistrationResponse();
                response.setId(UUID.randomUUID());
                response.setRegistrationNumber(regNumber);
                response.setStatus(status);
                response.setCreatedAt(LocalDateTime.now().minusDays(1));
                response.setUpdatedAt(LocalDateTime.now());
                return response;
            }
        };
    }
}
