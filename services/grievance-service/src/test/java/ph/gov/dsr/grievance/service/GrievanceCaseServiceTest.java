package ph.gov.dsr.grievance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for GrievanceCaseService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@ExtendWith(MockitoExtension.class)
class GrievanceCaseServiceTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @InjectMocks
    private GrievanceCaseService grievanceCaseService;

    private GrievanceCase testCase;

    @BeforeEach
    void setUp() {
        testCase = new GrievanceCase();
        testCase.setId(UUID.randomUUID());
        testCase.setCaseNumber("GRV-2024-001");
        testCase.setComplainantPsn("123456789012");
        testCase.setComplainantName("John Doe");
        testCase.setComplainantEmail("john.doe@email.com");
        testCase.setSubject("Payment Issue");
        testCase.setDescription("Payment not received");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setPriority(GrievanceCase.Priority.MEDIUM);
        testCase.setStatus(GrievanceCase.CaseStatus.SUBMITTED);
        testCase.setSubmissionDate(LocalDateTime.now());
    }

    @Test
    void testCreateCase_Success() {
        // Arrange
        GrievanceCase newCase = new GrievanceCase();
        newCase.setComplainantPsn("123456789012");
        newCase.setSubject("Test Issue");
        newCase.setDescription("Test Description");
        newCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);

        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = grievanceCaseService.createCase(newCase);

        // Assert
        assertNotNull(result);
        assertEquals(GrievanceCase.CaseStatus.SUBMITTED, result.getStatus());
        assertNotNull(result.getSubmissionDate());
        verify(caseRepository, times(2)).save(any(GrievanceCase.class));
    }

    @Test
    void testGetCaseById_Success() {
        // Arrange
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));

        // Act
        Optional<GrievanceCase> result = grievanceCaseService.getCaseById(testCase.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCase.getCaseNumber(), result.get().getCaseNumber());
        verify(caseRepository).findById(testCase.getId());
    }

    @Test
    void testGetCaseById_NotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        Optional<GrievanceCase> result = grievanceCaseService.getCaseById(nonExistentId);

        // Assert
        assertFalse(result.isPresent());
        verify(caseRepository).findById(nonExistentId);
    }

    @Test
    void testGetCaseByCaseNumber_Success() {
        // Arrange
        when(caseRepository.findByCaseNumber(testCase.getCaseNumber())).thenReturn(Optional.of(testCase));

        // Act
        Optional<GrievanceCase> result = grievanceCaseService.getCaseByCaseNumber(testCase.getCaseNumber());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCase.getId(), result.get().getId());
        verify(caseRepository).findByCaseNumber(testCase.getCaseNumber());
    }

    @Test
    void testAssignCase_Success() {
        // Arrange
        String assignedTo = "staff@dswd.gov.ph";
        String assignedBy = "manager@dswd.gov.ph";
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = grievanceCaseService.assignCase(testCase.getId(), assignedTo, assignedBy);

        // Assert
        assertNotNull(result);
        assertEquals(assignedTo, result.getAssignedTo());
        assertNotNull(result.getAssignedDate());
        assertEquals(GrievanceCase.CaseStatus.UNDER_REVIEW, result.getStatus());
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testAssignCase_CaseNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            grievanceCaseService.assignCase(nonExistentId, "staff@dswd.gov.ph", "manager@dswd.gov.ph");
        });
        verify(caseRepository).findById(nonExistentId);
        verify(caseRepository, never()).save(any());
    }

    @Test
    void testUpdateCaseStatus_Success() {
        // Arrange
        GrievanceCase.CaseStatus newStatus = GrievanceCase.CaseStatus.UNDER_REVIEW;
        String reason = "Starting investigation";
        String updatedBy = "staff@dswd.gov.ph";
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = grievanceCaseService.updateCaseStatus(testCase.getId(), newStatus, reason, updatedBy);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(updatedBy, result.getUpdatedBy());
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testAddComment_Success() {
        // Arrange
        String comment = "Investigation started";
        String author = "staff@dswd.gov.ph";
        Boolean isInternal = false;
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = grievanceCaseService.addComment(testCase.getId(), comment, author, isInternal);

        // Assert
        assertNotNull(result);
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testGetCasesAssignedTo_Success() {
        // Arrange
        String assignedTo = "staff@dswd.gov.ph";
        Pageable pageable = PageRequest.of(0, 20);
        List<GrievanceCase> cases = Arrays.asList(testCase);
        Page<GrievanceCase> casePage = new PageImpl<>(cases, pageable, 1);
        
        when(caseRepository.findByAssignedToOrderBySubmissionDateDesc(assignedTo, pageable))
                .thenReturn(casePage);

        // Act
        Page<GrievanceCase> result = grievanceCaseService.getCasesAssignedTo(assignedTo, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCase.getCaseNumber(), result.getContent().get(0).getCaseNumber());
        verify(caseRepository).findByAssignedToOrderBySubmissionDateDesc(assignedTo, pageable);
    }

    @Test
    void testGetCasesByCriteria_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<GrievanceCase> cases = Arrays.asList(testCase);
        Page<GrievanceCase> casePage = new PageImpl<>(cases, pageable, 1);
        
        when(caseRepository.findByCriteria(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(casePage);

        // Act
        Page<GrievanceCase> result = grievanceCaseService.getCasesByCriteria(
                GrievanceCase.CaseStatus.SUBMITTED,
                GrievanceCase.Priority.MEDIUM,
                GrievanceCase.GrievanceCategory.PAYMENT_ISSUE,
                "staff@dswd.gov.ph",
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now(),
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(caseRepository).findByCriteria(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSearchCases_Success() {
        // Arrange
        String searchText = "payment";
        Pageable pageable = PageRequest.of(0, 20);
        List<GrievanceCase> cases = Arrays.asList(testCase);
        Page<GrievanceCase> casePage = new PageImpl<>(cases, pageable, 1);
        
        when(caseRepository.searchCases(searchText, pageable)).thenReturn(casePage);

        // Act
        Page<GrievanceCase> result = grievanceCaseService.searchCases(searchText, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(caseRepository).searchCases(searchText, pageable);
    }

    @Test
    void testGetCaseStatistics_Success() {
        // Arrange
        Object[] stats = {100L, 80L, 20L, 5L, 10L, 2L, 3.5, 4.2};
        when(caseRepository.getCaseStatistics()).thenReturn(stats);

        // Act
        Object[] result = grievanceCaseService.getCaseStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(8, result.length);
        assertEquals(100L, result[0]);
        verify(caseRepository).getCaseStatistics();
    }

    @Test
    void testResolveCase_Success() {
        // Arrange
        String resolutionSummary = "Issue resolved";
        String resolutionActions = "Payment processed";
        String resolvedBy = "staff@dswd.gov.ph";
        
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = grievanceCaseService.resolveCase(testCase.getId(), 
                resolutionSummary, resolutionActions, resolvedBy);

        // Assert
        assertNotNull(result);
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testResolveCase_CaseNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            grievanceCaseService.resolveCase(nonExistentId, "Summary", "Actions", "staff@dswd.gov.ph");
        });
        verify(caseRepository).findById(nonExistentId);
        verify(caseRepository, never()).save(any());
    }
}
