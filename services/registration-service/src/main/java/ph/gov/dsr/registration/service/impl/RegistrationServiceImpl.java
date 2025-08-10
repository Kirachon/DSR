package ph.gov.dsr.registration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.entity.*;
import ph.gov.dsr.registration.repository.HouseholdRepository;
import ph.gov.dsr.registration.repository.RegistrationRepository;
import ph.gov.dsr.registration.repository.UserRepository;
import ph.gov.dsr.registration.service.RegistrationService;
import ph.gov.dsr.registration.service.ValidationService;
import ph.gov.dsr.registration.service.AuditService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RegistrationService
 */
@Service
@Transactional
@Profile("!no-db")
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final AuditService auditService;

    @Autowired
    public RegistrationServiceImpl(
            RegistrationRepository registrationRepository,
            HouseholdRepository householdRepository,
            UserRepository userRepository,
            ValidationService validationService,
            AuditService auditService) {
        this.registrationRepository = registrationRepository;
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.auditService = auditService;
    }

    @Override
    public RegistrationResponse createRegistration(RegistrationCreateRequest request) {
        // Validate the registration request
        ValidationResult validationResult = validationService.validateRegistrationRequest(request);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Validation failed: " + validationResult.getMessage() +
                ". Errors: " + validationResult.getErrors().size());
        }

        // Generate household number
        String householdNumber = generateHouseholdNumber();

        // Create household with comprehensive data mapping
        Household household = new Household();
        household.setHouseholdNumber(householdNumber);

        // Map household data from request
        if (request.getHousehold() != null) {
            household.setMonthlyIncome(request.getHousehold().getMonthlyIncome());
            household.setIsIndigenous(request.getHousehold().getIsIndigenous());
            household.setIsPwdHousehold(request.getHousehold().getIsPwdHousehold());
            household.setIsSeniorCitizenHousehold(request.getHousehold().getIsSeniorCitizenHousehold());
            // Additional fields will be added when DTO is enhanced
        }

        household.setConsentGiven(request.getConsentGiven());
        household.setPreferredLanguage(request.getPreferredLanguage());
        household.setNotes(request.getNotes());
        household.setRegistrationChannel(request.getRegistrationChannel());

        // Set total members count
        if (request.getMembers() != null) {
            household.setTotalMembers(request.getMembers().size());
        }

        // Save household first
        household = householdRepository.save(household);

        // Create registration
        String registrationNumber = generateRegistrationNumber();
        Registration registration = new Registration();
        registration.setRegistrationNumber(registrationNumber);
        registration.setHousehold(household);
        registration.setRegistrationChannel(request.getRegistrationChannel());
        registration.setStatus(RegistrationStatus.DRAFT);
        registration.setNotes(request.getNotes());

        // Set priority level based on vulnerability
        registration.setPriorityLevel(calculatePriorityLevel(request));

        // Save registration
        registration = registrationRepository.save(registration);

        // Log audit event
        auditService.logRegistrationCreated(registration, null); // TODO: Get current user from security context

        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationById(UUID id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponse getRegistrationByNumber(String registrationNumber) {
        Registration registration = registrationRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new RuntimeException("Registration not found with number: " + registrationNumber));
        return mapToResponse(registration);
    }

    @Override
    public RegistrationResponse updateRegistration(UUID id, RegistrationUpdateRequest request) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        if (!registration.getStatus().canBeModified()) {
            throw new RuntimeException("Registration cannot be modified in current status: " + registration.getStatus());
        }
        
        // Update registration fields
        if (request.getNotes() != null) {
            registration.setNotes(request.getNotes());
        }
        if (request.getPriorityLevel() != null) {
            registration.setPriorityLevel(request.getPriorityLevel());
        }
        
        registration = registrationRepository.save(registration);
        return mapToResponse(registration);
    }

    @Override
    public void deleteRegistration(UUID id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
    }

    @Override
    public RegistrationResponse submitRegistration(UUID id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));

        if (registration.getStatus() != RegistrationStatus.DRAFT) {
            throw new RuntimeException("Only draft registrations can be submitted");
        }

        // Additional validation before submission
        // Note: In a real implementation, we would reconstruct the request from the registration
        // For now, we'll perform basic validation
        if (registration.getHousehold() == null) {
            throw new RuntimeException("Cannot submit registration without household information");
        }

        registration.submit();
        registration = registrationRepository.save(registration);

        // Log audit event
        auditService.logRegistrationSubmitted(registration, null); // TODO: Get current user from security context

        return mapToResponse(registration);
    }

    @Override
    public RegistrationResponse approveRegistration(UUID id, String notes) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        // For now, we'll use a mock user for approval
        User mockApprover = createMockUser("System Approver", UserRole.LGU_STAFF);
        
        registration.approve(mockApprover);
        if (notes != null) {
            registration.setNotes(notes);
        }
        
        registration = registrationRepository.save(registration);

        // Log audit event
        auditService.logRegistrationApproved(registration, mockApprover, notes);

        return mapToResponse(registration);
    }

    @Override
    public RegistrationResponse rejectRegistration(UUID id, String reason, String notes) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        // For now, we'll use a mock user for rejection
        User mockRejector = createMockUser("System Rejector", UserRole.LGU_STAFF);
        
        registration.reject(reason, mockRejector);
        if (notes != null) {
            registration.setNotes(notes);
        }
        
        registration = registrationRepository.save(registration);

        // Log audit event
        auditService.logRegistrationRejected(registration, mockRejector, reason, notes);

        return mapToResponse(registration);
    }

    @Override
    public RegistrationResponse assignRegistration(UUID id, UUID staffId) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));
        
        registration.assignTo(staff);
        registration = registrationRepository.save(registration);
        return mapToResponse(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponse> getAllRegistrations(Pageable pageable) {
        Page<Registration> registrations = registrationRepository.findAll(pageable);
        return registrations.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByStatus(RegistrationStatus status) {
        List<Registration> registrations = registrationRepository.findByStatus(status);
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsAssignedTo(UUID userId) {
        List<Registration> registrations = registrationRepository.findByAssignedToId(userId);
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationResponse> searchRegistrations(RegistrationSearchCriteria criteria, Pageable pageable) {
        // For now, return all registrations - implement search logic later
        return getAllRegistrations(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getRegistrationStatistics() {
        // Return basic statistics
        return new Object() {
            public final long totalRegistrations = registrationRepository.count();
            public final long draftRegistrations = registrationRepository.countByStatus(RegistrationStatus.DRAFT);
            public final long pendingRegistrations = registrationRepository.countByStatus(RegistrationStatus.PENDING_VERIFICATION);
            public final long approvedRegistrations = registrationRepository.countByStatus(RegistrationStatus.APPROVED);
            public final long rejectedRegistrations = registrationRepository.countByStatus(RegistrationStatus.REJECTED);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getOverdueRegistrations() {
        List<Registration> registrations = registrationRepository.findOverdueRegistrations();
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponse> getRegistrationsByHousehold(UUID householdId) {
        List<Registration> registrations = registrationRepository.findByHouseholdId(householdId);
        return registrations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RegistrationResponse updateRegistrationPriority(UUID id, Integer priorityLevel) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        registration.setPriorityLevel(priorityLevel);
        registration = registrationRepository.save(registration);
        return mapToResponse(registration);
    }

    @Override
    public RegistrationResponse addNotes(UUID id, String notes) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found with id: " + id));
        
        String existingNotes = registration.getNotes();
        String newNotes = existingNotes != null ? 
            existingNotes + "\n[" + LocalDateTime.now() + "] " + notes : 
            "[" + LocalDateTime.now() + "] " + notes;
        
        registration.setNotes(newNotes);
        registration = registrationRepository.save(registration);
        return mapToResponse(registration);
    }

    // Helper methods
    private String generateHouseholdNumber() {
        Integer nextNumber = householdRepository.getNextHouseholdNumber();
        return String.format("HH-%06d", nextNumber != null ? nextNumber : 1);
    }

    private String generateRegistrationNumber() {
        Integer nextNumber = registrationRepository.getNextRegistrationNumber();
        return String.format("REG-%06d", nextNumber != null ? nextNumber : 1);
    }

    private User createMockUser(String name, UserRole role) {
        User user = new User();
        user.setEmail(name.toLowerCase().replace(" ", ".") + "@dsr.gov.ph");
        user.setFirstName(name.split(" ")[0]);
        user.setLastName(name.split(" ")[1]);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash("mock_password_hash");
        return user;
    }

    private Integer calculatePriorityLevel(RegistrationCreateRequest request) {
        int priority = 5; // Default priority (1 = highest, 10 = lowest)

        // Higher priority for vulnerable households
        if (request.getHousehold() != null) {
            if (Boolean.TRUE.equals(request.getHousehold().getIsIndigenous())) {
                priority -= 1;
            }
            if (Boolean.TRUE.equals(request.getHousehold().getIsPwdHousehold())) {
                priority -= 1;
            }
            if (Boolean.TRUE.equals(request.getHousehold().getIsSeniorCitizenHousehold())) {
                priority -= 1;
            }
        }

        // Higher priority for households with vulnerable members
        if (request.getMembers() != null) {
            boolean hasVulnerableMembers = request.getMembers().stream().anyMatch(member ->
                Boolean.TRUE.equals(member.getIsPwd()));

            if (hasVulnerableMembers) {
                priority -= 1;
            }
        }

        // Ensure priority is within valid range
        return Math.max(1, Math.min(10, priority));
    }

    private RegistrationResponse mapToResponse(Registration registration) {
        RegistrationResponse response = new RegistrationResponse();
        response.setId(registration.getId());
        response.setRegistrationNumber(registration.getRegistrationNumber());
        response.setStatus(registration.getStatus());
        response.setRegistrationChannel(registration.getRegistrationChannel());
        response.setSubmissionDate(registration.getSubmissionDate());
        response.setVerificationDate(registration.getVerificationDate());
        response.setApprovalDate(registration.getApprovalDate());
        response.setRejectionDate(registration.getRejectionDate());
        response.setRejectionReason(registration.getRejectionReason());
        response.setPriorityLevel(registration.getPriorityLevel());
        response.setEstimatedCompletionDate(registration.getEstimatedCompletionDate());
        response.setCompletionDate(registration.getCompletionDate());
        response.setNotes(registration.getNotes());
        response.setCreatedAt(registration.getCreatedAt());
        response.setUpdatedAt(registration.getUpdatedAt());
        
        // Household information
        if (registration.getHousehold() != null) {
            response.setHouseholdId(registration.getHousehold().getId());
            response.setHouseholdNumber(registration.getHousehold().getHouseholdNumber());
            response.setTotalMembers(registration.getHousehold().getTotalMembers());
        }
        
        // Assigned user information
        if (registration.getAssignedTo() != null) {
            response.setAssignedToId(registration.getAssignedTo().getId());
            response.setAssignedToName(registration.getAssignedTo().getFullName());
        }
        
        return response;
    }
}
