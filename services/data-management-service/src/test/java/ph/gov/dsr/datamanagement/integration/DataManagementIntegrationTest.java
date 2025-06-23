package ph.gov.dsr.datamanagement.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ph.gov.dsr.datamanagement.dto.*;
import ph.gov.dsr.datamanagement.service.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Data Management Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("no-db")
class DataManagementIntegrationTest {

    @Autowired
    private DataIngestionService dataIngestionService;

    @Autowired
    private DataValidationService dataValidationService;

    @Autowired
    private DeduplicationService deduplicationService;

    @Autowired
    private PhilSysIntegrationService philSysIntegrationService;

    @Autowired
    private HistoricalDataArchivingService archivingService;

    @Test
    void testCompleteDataIngestionWorkflow() {
        // Arrange
        DataIngestionRequest request = createTestHouseholdRequest();

        // Act & Assert - Test validation
        DataIngestionResponse validationResponse = dataIngestionService.validateData(request);
        assertNotNull(validationResponse);
        assertEquals("VALID", validationResponse.getStatus());

        // Act & Assert - Test full ingestion
        DataIngestionResponse ingestionResponse = dataIngestionService.ingestData(request);
        assertNotNull(ingestionResponse);
        assertEquals("SUCCESS", ingestionResponse.getStatus());
        assertEquals(1, ingestionResponse.getSuccessfulRecords());
        assertEquals(0, ingestionResponse.getFailedRecords());
    }

    @Test
    void testDataValidationWorkflow() {
        // Arrange
        ValidationRequest request = new ValidationRequest();
        request.setDataType("HOUSEHOLD");
        request.setSourceSystem("LISTAHANAN");
        
        Map<String, Object> data = new HashMap<>();
        data.put("householdNumber", "HH-001");
        data.put("headOfHouseholdPsn", "1234-5678-9012");
        data.put("totalMembers", 4);
        request.setData(data);

        // Act
        ValidationResponse response = dataValidationService.validateData(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isValid());
        assertNotNull(response.getValidatedAt());
    }

    @Test
    void testDeduplicationWorkflow() {
        // Arrange
        DeduplicationRequest request = new DeduplicationRequest();
        request.setEntityType("HOUSEHOLD");
        request.setMatchingAlgorithm("FUZZY");
        request.setMatchThreshold(0.8);
        
        Map<String, Object> entityData = new HashMap<>();
        entityData.put("householdNumber", "HH-001");
        entityData.put("headOfHouseholdName", "Juan Dela Cruz");
        request.setEntityData(entityData);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getProcessedAt());
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void testPhilSysIntegrationWorkflow() {
        // Arrange
        PhilSysVerificationRequest request = new PhilSysVerificationRequest();
        request.setPsn("1234-5678-9012");
        request.setFirstName("Juan");
        request.setLastName("Dela Cruz");

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(request);

        // Assert
        assertNotNull(response);
        assertEquals("1234-5678-9012", response.getPsn());
        assertNotNull(response.getVerifiedAt());
        assertNotNull(response.getVerificationStatus());
    }

    @Test
    void testHistoricalDataArchivingWorkflow() {
        // Arrange
        String entityType = "HOUSEHOLD";
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(365);

        // Act - Test archiving
        HistoricalDataArchivingService.ArchivingResult archiveResult = 
            archivingService.archiveOldData(entityType, cutoffDate);

        // Assert
        assertNotNull(archiveResult);
        assertTrue(archiveResult.isSuccess());
        assertTrue(archiveResult.getArchivedCount() >= 0);

        // Act - Test statistics
        Map<String, Object> stats = archivingService.getArchivingStatistics();

        // Assert
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalArchived"));
    }

    @Test
    void testServiceAvailability() {
        // Test that all services are properly injected and available
        assertNotNull(dataIngestionService);
        assertNotNull(dataValidationService);
        assertNotNull(deduplicationService);
        assertNotNull(philSysIntegrationService);
        assertNotNull(archivingService);

        // Test PhilSys service status
        String status = philSysIntegrationService.getPhilSysServiceStatus();
        assertNotNull(status);
        assertTrue(status.equals("MOCK_MODE") || status.equals("AVAILABLE") || status.equals("UNAVAILABLE"));
    }

    @Test
    void testDataCleaningWorkflow() {
        // Arrange
        Map<String, Object> dirtyData = new HashMap<>();
        dirtyData.put("firstName", "  JUAN  ");
        dirtyData.put("lastName", "dela cruz");
        dirtyData.put("phoneNumber", "09171234567");
        dirtyData.put("email", "JUAN@EXAMPLE.COM");

        // Act
        Map<String, Object> cleanedData = dataValidationService.cleanData(dirtyData, "INDIVIDUAL");

        // Assert
        assertNotNull(cleanedData);
        // The cleaned data should have normalized values
        assertTrue(cleanedData.containsKey("firstName"));
        assertTrue(cleanedData.containsKey("lastName"));
    }

    private DataIngestionRequest createTestHouseholdRequest() {
        DataIngestionRequest request = new DataIngestionRequest();
        request.setSourceSystem("LISTAHANAN");
        request.setDataType("HOUSEHOLD");
        request.setSubmittedBy("test-user");
        request.setSubmissionDate(LocalDateTime.now());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("householdNumber", "HH-TEST-001");
        payload.put("headOfHouseholdPsn", "1234-5678-9012");
        payload.put("headOfHouseholdName", "Juan Dela Cruz");
        payload.put("totalMembers", 4);
        payload.put("monthlyIncome", 15000.0);
        payload.put("address", "123 Test Street, Test City");
        
        request.setDataPayload(payload);
        return request;
    }
}
