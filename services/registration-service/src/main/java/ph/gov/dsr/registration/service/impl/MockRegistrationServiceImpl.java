package ph.gov.dsr.registration.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ph.gov.dsr.registration.dto.*;
import ph.gov.dsr.registration.entity.RegistrationStatus;
import ph.gov.dsr.registration.service.RegistrationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Mock implementation of RegistrationService for no-database mode
 */
@Service
@Profile("no-db")
public class MockRegistrationServiceImpl implements RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(MockRegistrationServiceImpl.class);

    private final Map<UUID, RegistrationResponse> registrations = new ConcurrentHashMap<>();
    private final AtomicLong registrationCounter = new AtomicLong(1);

    @Override
    public RegistrationResponse createRegistration(RegistrationCreateRequest request) {
        logger.info("Mock registration creation for household");

        // Generate mock registration
        UUID registrationId = UUID.randomUUID();
        UUID householdId = UUID.randomUUID();
        String registrationNumber = "REG-" + String.format("%08d", registrationCounter.getAndIncrement());
        String householdNumber = "HH-" + String.format("%08d", registrationCounter.get());

        RegistrationResponse registration = new RegistrationResponse();
        registration.setId(registrationId);
        registration.setRegistrationNumber(registrationNumber);
        registration.setHouseholdId(householdId);
        registration.setHouseholdNumber(householdNumber);
        registration.setStatus(RegistrationStatus.DRAFT);
        registration.setRegistrationChannel(request.getRegistrationChannel());
        registration.setSubmissionDate(LocalDateTime.now());
        registration.setPriorityLevel(calculatePriorityLevel(request));
        registration.setEstimatedCompletionDate(LocalDate.now().plusDays(30));
        registration.setNotes(request.getNotes());
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());
        registration.setCreatedByName("Mock User");
        registration.setUpdatedByName("Mock User");

        registrations.put(registrationId, registration);

        logger.info("Mock registration created successfully with ID: {} and number: {}", 
                   registrationId, registrationNumber);

        return registration;
    }

    @Override
    public RegistrationResponse getRegistrationById(UUID id) {
        RegistrationResponse registration = registrations.get(id);
        if (registration == null) {
            throw new RuntimeException("Registration not found with id: " + id);
        }
        return registration;
    }

    @Override
    public RegistrationResponse getRegistrationByNumber(String registrationNumber) {
        return registrations.values().stream()
                .filter(reg -> reg.getRegistrationNumber().equals(registrationNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Registration not found with number: " + registrationNumber));
    }

    @Override
    public RegistrationResponse updateRegistration(UUID id, RegistrationUpdateRequest request) {
        RegistrationResponse registration = getRegistrationById(id);
        
        // Update fields from request
        registration.setNotes(request.getNotes());
        registration.setUpdatedAt(LocalDateTime.now());
        registration.setUpdatedByName("Mock User");

        logger.info("Mock registration updated: {}", id);
        return registration;
    }

    @Override
    public void deleteRegistration(UUID id) {
        if (registrations.remove(id) != null) {
            logger.info("Mock registration deleted: {}", id);
        } else {
            throw new RuntimeException("Registration not found with id: " + id);
        }
    }

    @Override
    public RegistrationResponse submitRegistration(UUID id) {
        RegistrationResponse registration = getRegistrationById(id);
        
        if (registration.getStatus() != RegistrationStatus.DRAFT) {
            throw new RuntimeException("Only draft registrations can be submitted");
        }

        registration.setStatus(RegistrationStatus.SUBMITTED);
        registration.setSubmissionDate(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        logger.info("Mock registration submitted: {}", id);
        return registration;
    }

    @Override
    public RegistrationResponse approveRegistration(UUID id, String notes) {
        RegistrationResponse registration = getRegistrationById(id);
        
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setApprovalDate(LocalDateTime.now());
        registration.setCompletionDate(LocalDate.now());
        registration.setNotes(notes);
        registration.setUpdatedAt(LocalDateTime.now());

        logger.info("Mock registration approved: {}", id);
        return registration;
    }

    @Override
    public RegistrationResponse rejectRegistration(UUID id, String reason, String notes) {
        RegistrationResponse registration = getRegistrationById(id);
        
        registration.setStatus(RegistrationStatus.REJECTED);
        registration.setRejectionDate(LocalDateTime.now());
        registration.setRejectionReason(reason);
        registration.setNotes(notes);
        registration.setUpdatedAt(LocalDateTime.now());

        logger.info("Mock registration rejected: {}", id);
        return registration;
    }

    @Override
    public RegistrationResponse assignRegistration(UUID id, UUID staffId) {
        RegistrationResponse registration = getRegistrationById(id);
        
        registration.setAssignedToId(staffId);
        registration.setAssignedToName("Mock Staff Member");
        registration.setUpdatedAt(LocalDateTime.now());

        logger.info("Mock registration assigned: {} to staff: {}", id, staffId);
        return registration;
    }

    @Override
    public Page<RegistrationResponse> getAllRegistrations(Pageable pageable) {
        List<RegistrationResponse> allRegistrations = new ArrayList<>(registrations.values());
        allRegistrations.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allRegistrations.size());
        
        List<RegistrationResponse> pageContent = start < allRegistrations.size() 
            ? allRegistrations.subList(start, end) 
            : new ArrayList<>();
            
        return new PageImpl<>(pageContent, pageable, allRegistrations.size());
    }

    @Override
    public List<RegistrationResponse> getRegistrationsByStatus(RegistrationStatus status) {
        return registrations.values().stream()
                .filter(reg -> reg.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationResponse> getRegistrationsAssignedTo(UUID userId) {
        return registrations.values().stream()
                .filter(reg -> Objects.equals(reg.getAssignedToId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public Page<RegistrationResponse> searchRegistrations(RegistrationSearchCriteria criteria, Pageable pageable) {
        // Simple mock search - just return all registrations
        return getAllRegistrations(pageable);
    }

    @Override
    public Object getRegistrationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", registrations.size());
        stats.put("draft", getRegistrationsByStatus(RegistrationStatus.DRAFT).size());
        stats.put("submitted", getRegistrationsByStatus(RegistrationStatus.SUBMITTED).size());
        stats.put("approved", getRegistrationsByStatus(RegistrationStatus.APPROVED).size());
        stats.put("rejected", getRegistrationsByStatus(RegistrationStatus.REJECTED).size());
        return stats;
    }

    @Override
    public List<RegistrationResponse> getOverdueRegistrations() {
        return registrations.values().stream()
                .filter(reg -> reg.getEstimatedCompletionDate() != null && 
                              reg.getEstimatedCompletionDate().isBefore(LocalDate.now()) &&
                              reg.getStatus() != RegistrationStatus.APPROVED &&
                              reg.getStatus() != RegistrationStatus.REJECTED)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegistrationResponse> getRegistrationsByHousehold(UUID householdId) {
        return registrations.values().stream()
                .filter(reg -> Objects.equals(reg.getHouseholdId(), householdId))
                .collect(Collectors.toList());
    }

    @Override
    public RegistrationResponse updateRegistrationPriority(UUID id, Integer priorityLevel) {
        RegistrationResponse registration = getRegistrationById(id);
        registration.setPriorityLevel(priorityLevel);
        registration.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Mock registration priority updated: {} to level: {}", id, priorityLevel);
        return registration;
    }

    @Override
    public RegistrationResponse addNotes(UUID id, String notes) {
        RegistrationResponse registration = getRegistrationById(id);
        registration.setNotes(notes);
        registration.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Mock registration notes added: {}", id);
        return registration;
    }

    /**
     * Calculate priority level based on household vulnerability indicators
     */
    private Integer calculatePriorityLevel(RegistrationCreateRequest request) {
        // Simple mock priority calculation
        int vulnerabilityScore = 0;
        
        // Check for vulnerable members
        if (request.getMembers() != null) {
            for (RegistrationCreateRequest.HouseholdMemberCreateDto member : request.getMembers()) {
                if (Boolean.TRUE.equals(member.getIsPwd())) vulnerabilityScore += 2;
                if (Boolean.TRUE.equals(member.getIsIndigenous())) vulnerabilityScore += 2;
                if (Boolean.TRUE.equals(member.getIsSoloParent())) vulnerabilityScore += 1;
                if (Boolean.TRUE.equals(member.getIsOfw())) vulnerabilityScore += 1;
            }
        }
        
        // Return priority: 1=HIGH, 2=MEDIUM, 3=LOW
        if (vulnerabilityScore >= 4) return 1;
        if (vulnerabilityScore >= 2) return 2;
        return 3;
    }
}
