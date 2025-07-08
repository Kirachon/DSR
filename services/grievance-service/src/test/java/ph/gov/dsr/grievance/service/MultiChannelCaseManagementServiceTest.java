package ph.gov.dsr.grievance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.grievance.dto.CaseSubmissionRequest;
import ph.gov.dsr.grievance.dto.CommunicationRequest;
import ph.gov.dsr.grievance.entity.CaseActivity;
import ph.gov.dsr.grievance.entity.GrievanceCase;
import ph.gov.dsr.grievance.repository.GrievanceCaseRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MultiChannelCaseManagementService
 */
@ExtendWith(MockitoExtension.class)
class MultiChannelCaseManagementServiceTest {

    @Mock
    private GrievanceCaseRepository caseRepository;

    @Mock
    private WorkflowAutomationService workflowService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MultiChannelCaseManagementService caseManagementService;

    private CaseSubmissionRequest testRequest;
    private GrievanceCase testCase;
    private CommunicationRequest testCommunication;

    @BeforeEach
    void setUp() {
        testRequest = new CaseSubmissionRequest();
        testRequest.setComplainantPsn("123456789012");
        testRequest.setComplainantName("John Doe");
        testRequest.setComplainantEmail("john.doe@email.com");
        testRequest.setComplainantPhone("+639123456789");
        testRequest.setSubject("Payment Issue");
        testRequest.setDescription("Payment not received for December 2024");
        testRequest.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testRequest.setPriority(GrievanceCase.Priority.MEDIUM);

        testCase = new GrievanceCase();
        testCase.setId(UUID.randomUUID());
        testCase.setCaseNumber("GRV-2024-001");
        testCase.setComplainantPsn("123456789012");
        testCase.setComplainantName("John Doe");
        testCase.setComplainantEmail("john.doe@email.com");
        testCase.setComplainantPhone("+639123456789");
        testCase.setSubject("Payment Issue");
        testCase.setDescription("Payment not received for December 2024");
        testCase.setCategory(GrievanceCase.GrievanceCategory.PAYMENT_ISSUE);
        testCase.setStatus(GrievanceCase.CaseStatus.SUBMITTED);
        testCase.setActivities(new ArrayList<>());

        testCommunication = new CommunicationRequest();
        testCommunication.setChannel("EMAIL");
        testCommunication.setDirection("INBOUND");
        testCommunication.setSubject("Follow-up on case");
        testCommunication.setContent("I would like to know the status of my case");
        testCommunication.setPerformedBy("john.doe@email.com");
    }

    @Test
    void testSubmitCaseFromWebPortal_Success() {
        // Arrange
        when(caseRepository.save(any(GrievanceCase.class))).thenAnswer(invocation -> {
            GrievanceCase case_ = invocation.getArgument(0);
            case_.setId(UUID.randomUUID());
            case_.setCaseNumber("GRV-2024-001");
            return case_;
        });

        // Act
        GrievanceCase result = caseManagementService.submitCaseFromWebPortal(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("WEB_PORTAL", result.getSubmissionChannel());
        assertEquals(GrievanceCase.CaseStatus.SUBMITTED, result.getStatus());
        verify(caseRepository, times(2)).save(any(GrievanceCase.class));
        verify(workflowService).processNewCaseWorkflow(any(UUID.class));
    }

    @Test
    void testSubmitCaseFromMobileApp_Success() {
        // Arrange
        when(caseRepository.save(any(GrievanceCase.class))).thenAnswer(invocation -> {
            GrievanceCase case_ = invocation.getArgument(0);
            case_.setId(UUID.randomUUID());
            case_.setCaseNumber("GRV-2024-001");
            return case_;
        });

        // Act
        GrievanceCase result = caseManagementService.submitCaseFromMobileApp(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("MOBILE_APP", result.getSubmissionChannel());
        assertEquals(GrievanceCase.CaseStatus.SUBMITTED, result.getStatus());
        verify(caseRepository, times(2)).save(any(GrievanceCase.class));
        verify(workflowService).processNewCaseWorkflow(any(UUID.class));
    }

    @Test
    void testSubmitCaseFromPhoneCall_Success() {
        // Arrange
        String callOperator = "operator@dswd.gov.ph";
        when(caseRepository.save(any(GrievanceCase.class))).thenAnswer(invocation -> {
            GrievanceCase case_ = invocation.getArgument(0);
            case_.setId(UUID.randomUUID());
            case_.setCaseNumber("GRV-2024-001");
            return case_;
        });

        // Act
        GrievanceCase result = caseManagementService.submitCaseFromPhoneCall(testRequest, callOperator);

        // Assert
        assertNotNull(result);
        assertEquals("PHONE_CALL", result.getSubmissionChannel());
        assertEquals(GrievanceCase.CaseStatus.SUBMITTED, result.getStatus());
        verify(caseRepository, times(2)).save(any(GrievanceCase.class));
        verify(workflowService).processNewCaseWorkflow(any(UUID.class));
    }

    @Test
    void testSubmitCaseFromEmail_Success() {
        // Arrange
        String emailSubject = "Complaint about service";
        String emailBody = "I am writing to complain about the poor service I received...";
        when(caseRepository.save(any(GrievanceCase.class))).thenAnswer(invocation -> {
            GrievanceCase case_ = invocation.getArgument(0);
            case_.setId(UUID.randomUUID());
            case_.setCaseNumber("GRV-2024-001");
            return case_;
        });

        // Act
        GrievanceCase result = caseManagementService.submitCaseFromEmail(testRequest, emailSubject, emailBody);

        // Assert
        assertNotNull(result);
        assertEquals("EMAIL", result.getSubmissionChannel());
        assertEquals(GrievanceCase.CaseStatus.SUBMITTED, result.getStatus());
        assertTrue(result.getDescription().contains("--- Original Email ---"));
        assertTrue(result.getDescription().contains(emailSubject));
        assertTrue(result.getDescription().contains(emailBody));
        verify(caseRepository, times(2)).save(any(GrievanceCase.class));
        verify(workflowService).processNewCaseWorkflow(any(UUID.class));
    }

    @Test
    void testSubmitCaseFromWalkIn_Success() {
        // Arrange
        String receivingOfficer = "officer@dswd.gov.ph";
        String officeLocation = "DSWD Regional Office NCR";
        when(caseRepository.save(any(GrievanceCase.class))).thenAnswer(invocation -> {
            GrievanceCase case_ = invocation.getArgument(0);
            case_.setId(UUID.randomUUID());
            case_.setCaseNumber("GRV-2024-001");
            return case_;
        });

        // Act
        GrievanceCase result = caseManagementService.submitCaseFromWalkIn(testRequest, receivingOfficer, officeLocation);

        // Assert
        assertNotNull(result);
        assertEquals("WALK_IN", result.getSubmissionChannel());
        assertEquals(GrievanceCase.CaseStatus.SUBMITTED, result.getStatus());
        verify(caseRepository, times(2)).save(any(GrievanceCase.class));
        verify(workflowService).processNewCaseWorkflow(any(UUID.class));
    }

    @Test
    void testHandleCommunication_Success() {
        // Arrange
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = caseManagementService.handleCommunication(testCase.getId(), testCommunication);

        // Assert
        assertNotNull(result);
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
        verify(notificationService).sendCommunicationReceived(eq(testCase), any(CaseActivity.class));
    }

    @Test
    void testHandleCommunication_CaseNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(caseRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            caseManagementService.handleCommunication(nonExistentId, testCommunication);
        });
        verify(caseRepository).findById(nonExistentId);
        verify(caseRepository, never()).save(any());
        verify(notificationService, never()).sendCommunicationReceived(any(), any());
    }

    @Test
    void testHandleCommunication_RequiresResponse() {
        // Arrange
        testCommunication.setContent("When will my case be resolved? Please respond urgently.");
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        GrievanceCase result = caseManagementService.handleCommunication(testCase.getId(), testCommunication);

        // Assert
        assertNotNull(result);
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
        verify(notificationService).sendCommunicationReceived(eq(testCase), any(CaseActivity.class));
    }

    @Test
    void testSendCommunication_Email_Success() {
        // Arrange
        testCommunication.setDirection("OUTBOUND");
        testCommunication.setChannel("EMAIL");
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(notificationService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        caseManagementService.sendCommunication(testCase.getId(), testCommunication);

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(notificationService).sendEmail(testCase.getComplainantEmail(), 
            testCommunication.getSubject(), testCommunication.getContent());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testSendCommunication_SMS_Success() {
        // Arrange
        testCommunication.setDirection("OUTBOUND");
        testCommunication.setChannel("SMS");
        testCommunication.setSubject(null); // SMS doesn't have subject
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(notificationService.sendSMS(anyString(), anyString())).thenReturn(true);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        caseManagementService.sendCommunication(testCase.getId(), testCommunication);

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(notificationService).sendSMS(testCase.getComplainantPhone(), testCommunication.getContent());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testSendCommunication_Phone_Success() {
        // Arrange
        testCommunication.setDirection("OUTBOUND");
        testCommunication.setChannel("PHONE");
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        caseManagementService.sendCommunication(testCase.getId(), testCommunication);

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
        // Phone calls are just logged, no actual sending
        verify(notificationService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendSMS(anyString(), anyString());
    }

    @Test
    void testSendCommunication_PostalMail_Success() {
        // Arrange
        testCommunication.setDirection("OUTBOUND");
        testCommunication.setChannel("POSTAL");
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(notificationService.sendPostalMail(any(GrievanceCase.class), anyString(), anyString())).thenReturn(true);
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        caseManagementService.sendCommunication(testCase.getId(), testCommunication);

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(notificationService).sendPostalMail(testCase, 
            testCommunication.getSubject(), testCommunication.getContent());
        verify(caseRepository).save(testCase);
    }

    @Test
    void testSendCommunication_UnsupportedChannel() {
        // Arrange
        testCommunication.setDirection("OUTBOUND");
        testCommunication.setChannel("UNSUPPORTED_CHANNEL");
        when(caseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(GrievanceCase.class))).thenReturn(testCase);

        // Act
        caseManagementService.sendCommunication(testCase.getId(), testCommunication);

        // Assert
        verify(caseRepository).findById(testCase.getId());
        verify(caseRepository).save(testCase);
        // No notification service calls for unsupported channels
        verify(notificationService, never()).sendEmail(anyString(), anyString(), anyString());
        verify(notificationService, never()).sendSMS(anyString(), anyString());
    }

    @Test
    void testGetUnifiedCaseTracking_Success() {
        // Arrange
        testCase.getActivities().add(new CaseActivity(testCase, CaseActivity.ActivityType.CASE_CREATED, 
            "Case created", "SYSTEM"));
        testCase.getActivities().add(new CaseActivity(testCase, CaseActivity.ActivityType.COMMUNICATION_RECEIVED, 
            "Email received", "john.doe@email.com"));
        
        when(caseRepository.findByCaseNumber(testCase.getCaseNumber())).thenReturn(Optional.of(testCase));

        // Act
        Map<String, Object> result = caseManagementService.getUnifiedCaseTracking(testCase.getCaseNumber());

        // Assert
        assertNotNull(result);
        assertEquals(testCase.getCaseNumber(), result.get("caseNumber"));
        assertEquals(testCase.getStatus(), result.get("status"));
        assertEquals(testCase.getPriority(), result.get("priority"));
        assertNotNull(result.get("channelBreakdown"));
        assertNotNull(result.get("communicationTimeline"));
        verify(caseRepository).findByCaseNumber(testCase.getCaseNumber());
    }

    @Test
    void testGetUnifiedCaseTracking_CaseNotFound() {
        // Arrange
        String nonExistentCaseNumber = "GRV-2024-999";
        when(caseRepository.findByCaseNumber(nonExistentCaseNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            caseManagementService.getUnifiedCaseTracking(nonExistentCaseNumber);
        });
        verify(caseRepository).findByCaseNumber(nonExistentCaseNumber);
    }

    // Note: Tests for private helper methods (createCaseFromRequest, requiresResponse)
    // are covered through integration testing in the public method tests above
}
