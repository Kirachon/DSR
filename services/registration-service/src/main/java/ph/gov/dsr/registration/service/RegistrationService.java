package ph.gov.dsr.registration.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.entity.RegistrationStatus;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Registration operations
 */
public interface RegistrationService {

    /**
     * Create a new household registration
     */
    RegistrationResponse createRegistration(RegistrationCreateRequest request);

    /**
     * Get registration by ID
     */
    RegistrationResponse getRegistrationById(UUID id);

    /**
     * Get registration by registration number
     */
    RegistrationResponse getRegistrationByNumber(String registrationNumber);

    /**
     * Update an existing registration
     */
    RegistrationResponse updateRegistration(UUID id, RegistrationUpdateRequest request);

    /**
     * Delete (cancel) a registration
     */
    void deleteRegistration(UUID id);

    /**
     * Submit registration for verification
     */
    RegistrationResponse submitRegistration(UUID id);

    /**
     * Approve a registration
     */
    RegistrationResponse approveRegistration(UUID id, String notes);

    /**
     * Reject a registration
     */
    RegistrationResponse rejectRegistration(UUID id, String reason, String notes);

    /**
     * Assign registration to staff member
     */
    RegistrationResponse assignRegistration(UUID id, UUID staffId);

    /**
     * Get all registrations with pagination
     */
    Page<RegistrationResponse> getAllRegistrations(Pageable pageable);

    /**
     * Get registrations by status
     */
    List<RegistrationResponse> getRegistrationsByStatus(RegistrationStatus status);

    /**
     * Get registrations assigned to specific user
     */
    List<RegistrationResponse> getRegistrationsAssignedTo(UUID userId);

    /**
     * Search registrations with criteria
     */
    Page<RegistrationResponse> searchRegistrations(RegistrationSearchCriteria criteria, Pageable pageable);

    /**
     * Get registration statistics
     */
    Object getRegistrationStatistics();

    /**
     * Get overdue registrations
     */
    List<RegistrationResponse> getOverdueRegistrations();

    /**
     * Get registrations by household
     */
    List<RegistrationResponse> getRegistrationsByHousehold(UUID householdId);

    /**
     * Update registration priority
     */
    RegistrationResponse updateRegistrationPriority(UUID id, Integer priorityLevel);

    /**
     * Add notes to registration
     */
    RegistrationResponse addNotes(UUID id, String notes);
}
