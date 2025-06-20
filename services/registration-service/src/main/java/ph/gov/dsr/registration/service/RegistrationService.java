package ph.gov.dsr.registration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.common.exception.ResourceNotFoundException;
import ph.gov.dsr.common.exception.ValidationException;
import ph.gov.dsr.common.util.PhilSysValidator;
import ph.gov.dsr.registration.domain.LifeEvent;
import ph.gov.dsr.registration.domain.Registration;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.mapper.RegistrationMapper;
import ph.gov.dsr.registration.repository.RegistrationRepository;
import ph.gov.dsr.registration.service.external.PhilSysClient;
import ph.gov.dsr.registration.service.messaging.RegistrationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for handling registration operations.
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-20
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;
    private final PhilSysClient philSysClient;
    private final RegistrationEventPublisher eventPublisher;
    private final RegistrationValidationService validationService;
    private final RegistrationIdGenerator idGenerator;

    /**
     * Creates a new household registration.
     * 
     * @param request Registration request data
     * @return Registration response
     */
    public RegistrationResponseDto createRegistration(RegistrationRequestDto request) {
        log.info("Processing registration for PSN: {}", maskPsn(request.getHeadOfHouseholdPsn()));
        
        // Step 1: Validate PhilSys identity
        validatePhilSysIdentity(request.getHeadOfHouseholdPsn());
        
        // Step 2: Check for duplicate registrations
        checkForDuplicateRegistration(request.getHeadOfHouseholdPsn());
        
        // Step 3: Validate household data
        validationService.validateRegistrationRequest(request);
        
        // Step 4: Create registration entity
        Registration registration = createRegistrationEntity(request);
        
        // Step 5: Save registration
        Registration savedRegistration = registrationRepository.save(registration);
        
        // Step 6: Publish registration event
        publishRegistrationCreatedEvent(savedRegistration);
        
        log.info("Registration created successfully with ID: {}", savedRegistration.getId());
        
        return registrationMapper.toResponseDto(savedRegistration);
    }

    /**
     * Searches for registrations based on criteria.
     * 
     * @param criteria Search criteria
     * @param pageable Pagination information
     * @return Page of registration summaries
     */
    @Transactional(readOnly = true)
    public Page<RegistrationSummaryDto> searchRegistrations(
            RegistrationSearchCriteria criteria, Pageable pageable) {
        
        log.info("Searching registrations with criteria: {}", criteria);
        
        Page<Registration> registrations = registrationRepository.findByCriteria(criteria, pageable);
        
        return registrations.map(registrationMapper::toSummaryDto);
    }

    /**
     * Gets detailed registration information.
     * 
     * @param registrationId Registration ID
     * @param include Additional data to include
     * @return Registration details
     */
    @Transactional(readOnly = true)
    public RegistrationDetailDto getRegistrationDetail(UUID registrationId, String include) {
        log.info("Retrieving registration details for ID: {}", registrationId);
        
        Registration registration = findRegistrationById(registrationId);
        
        return registrationMapper.toDetailDto(registration, include);
    }

    /**
     * Updates an existing registration.
     * 
     * @param registrationId Registration ID
     * @param request Update request
     * @return Updated registration response
     */
    public RegistrationResponseDto updateRegistration(
            UUID registrationId, RegistrationUpdateRequestDto request) {
        
        log.info("Updating registration: {}", registrationId);
        
        Registration registration = findRegistrationById(registrationId);
        
        // Check if registration can be modified
        if (!registration.canBeModified()) {
            throw new ValidationException(
                "Registration cannot be modified in current state: " + registration.getStatus());
        }
        
        // Validate update request
        validationService.validateUpdateRequest(request, registration);
        
        // Apply updates
        registrationMapper.updateEntity(registration, request);
        
        // Save updated registration
        Registration updatedRegistration = registrationRepository.save(registration);
        
        // Publish update event
        publishRegistrationUpdatedEvent(updatedRegistration);
        
        log.info("Registration updated successfully: {}", registrationId);
        
        return registrationMapper.toResponseDto(updatedRegistration);
    }

    /**
     * Reports a life event for a registration.
     * 
     * @param registrationId Registration ID
     * @param request Life event request
     * @return Life event response
     */
    public LifeEventResponseDto reportLifeEvent(
            UUID registrationId, LifeEventRequestDto request) {
        
        log.info("Reporting life event for registration: {}, event type: {}", 
                 registrationId, request.getEventType());
        
        Registration registration = findRegistrationById(registrationId);
        
        // Validate life event request
        validationService.validateLifeEventRequest(request, registration);
        
        // Create life event entity
        LifeEvent lifeEvent = registrationMapper.toLifeEventEntity(request);
        lifeEvent.setRegistration(registration);
        
        // Add to registration
        registration.addLifeEvent(lifeEvent);
        
        // Save registration with life event
        Registration updatedRegistration = registrationRepository.save(registration);
        
        // Publish life event
        publishLifeEventReportedEvent(lifeEvent);
        
        log.info("Life event reported successfully for registration: {}", registrationId);
        
        return registrationMapper.toLifeEventResponseDto(lifeEvent);
    }

    /**
     * Gets life events for a registration.
     * 
     * @param registrationId Registration ID
     * @param pageable Pagination information
     * @return Page of life events
     */
    @Transactional(readOnly = true)
    public Page<LifeEventDto> getLifeEvents(UUID registrationId, Pageable pageable) {
        log.info("Retrieving life events for registration: {}", registrationId);
        
        // Verify registration exists
        findRegistrationById(registrationId);
        
        Page<LifeEvent> lifeEvents = registrationRepository.findLifeEventsByRegistrationId(
                registrationId, pageable);
        
        return lifeEvents.map(registrationMapper::toLifeEventDto);
    }

    /**
     * Gets registration status information.
     * 
     * @param registrationId Registration ID
     * @return Registration status
     */
    @Transactional(readOnly = true)
    public RegistrationStatusDto getRegistrationStatus(UUID registrationId) {
        log.info("Retrieving status for registration: {}", registrationId);
        
        Registration registration = findRegistrationById(registrationId);
        
        return registrationMapper.toStatusDto(registration);
    }

    /**
     * Validates PhilSys identity.
     */
    private void validatePhilSysIdentity(String psn) {
        if (!PhilSysValidator.isValidPsn(psn)) {
            throw new ValidationException("Invalid PhilSys Number format");
        }
        
        // Call PhilSys API for verification
        if (!philSysClient.verifyIdentity(psn)) {
            throw new ValidationException("PhilSys identity verification failed");
        }
    }

    /**
     * Checks for duplicate registrations.
     */
    private void checkForDuplicateRegistration(String psn) {
        Optional<Registration> existing = registrationRepository.findByHeadOfHouseholdPsn(psn);
        if (existing.isPresent()) {
            throw new ValidationException(
                "Registration already exists for PSN: " + maskPsn(psn));
        }
    }

    /**
     * Creates a registration entity from request.
     */
    private Registration createRegistrationEntity(RegistrationRequestDto request) {
        Registration registration = registrationMapper.toEntity(request);
        
        // Generate IDs
        registration.setRegistrationId(idGenerator.generateRegistrationId());
        registration.setConfirmationNumber(idGenerator.generateConfirmationNumber());
        
        // Set default values
        registration.setSubmissionDate(LocalDate.now());
        registration.setEstimatedCompletionDate(LocalDate.now().plusDays(5));
        
        return registration;
    }

    /**
     * Finds registration by ID or throws exception.
     */
    private Registration findRegistrationById(UUID registrationId) {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Registration", registrationId.toString()));
    }

    /**
     * Publishes registration created event.
     */
    private void publishRegistrationCreatedEvent(Registration registration) {
        eventPublisher.publishRegistrationCreated(registration);
    }

    /**
     * Publishes registration updated event.
     */
    private void publishRegistrationUpdatedEvent(Registration registration) {
        eventPublisher.publishRegistrationUpdated(registration);
    }

    /**
     * Publishes life event reported event.
     */
    private void publishLifeEventReportedEvent(LifeEvent lifeEvent) {
        eventPublisher.publishLifeEventReported(lifeEvent);
    }

    /**
     * Masks PSN for logging.
     */
    private String maskPsn(String psn) {
        return PhilSysValidator.maskPsn(psn);
    }
}
