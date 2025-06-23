package ph.gov.dsr.datamanagement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.datamanagement.dto.*;
import ph.gov.dsr.datamanagement.entity.DataIngestionBatch;
import ph.gov.dsr.datamanagement.repository.DataIngestionBatchRepository;
import ph.gov.dsr.datamanagement.service.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataIngestionServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class DataIngestionServiceImplTest {

    @Mock
    private DataValidationService dataValidationService;

    @Mock
    private DeduplicationService deduplicationService;

    @Mock
    private LegacyDataParserService legacyDataParserService;

    @Mock
    private DataIngestionBatchRepository batchRepository;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private DataIngestionServiceImpl dataIngestionService;

    private DataIngestionRequest testRequest;
    private ValidationResponse validationResponse;
    private DeduplicationResponse deduplicationResponse;

    @BeforeEach
    void setUp() {
        testRequest = new DataIngestionRequest();
        testRequest.setSourceSystem("LISTAHANAN");
        testRequest.setDataType("HOUSEHOLD");
        testRequest.setSubmittedBy("test-user");
        testRequest.setSubmissionDate(LocalDateTime.now());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("householdNumber", "HH-001");
        payload.put("headOfHouseholdName", "Juan Dela Cruz");
        payload.put("totalMembers", 5);
        testRequest.setDataPayload(payload);

        validationResponse = new ValidationResponse();
        validationResponse.setValid(true);
        validationResponse.setErrors(new ArrayList<>());
        validationResponse.setWarnings(new ArrayList<>());

        deduplicationResponse = new DeduplicationResponse();
        deduplicationResponse.setHasDuplicates(false);
        deduplicationResponse.setRecommendation("ACCEPT");
    }

    @Test
    void testIngestData_Success() {
        // Arrange
        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);
        when(deduplicationService.findDuplicates(any())).thenReturn(deduplicationResponse);
        when(dataValidationService.cleanData(any(), any())).thenReturn(testRequest.getDataPayload());

        // Act
        DataIngestionResponse response = dataIngestionService.ingestData(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getSuccessfulRecords());
        assertEquals(0, response.getFailedRecords());
        assertNotNull(response.getIngestionId());
        assertNotNull(response.getProcessedAt());

        verify(dataValidationService).validateData(any(ValidationRequest.class));
        verify(deduplicationService).findDuplicates(any());
        verify(dataValidationService).cleanData(any(), any());
    }

    @Test
    void testIngestData_ValidationFailure() {
        // Arrange
        validationResponse.setValid(false);
        List<ValidationResponse.ValidationError> errors = new ArrayList<>();
        ValidationResponse.ValidationError error = new ValidationResponse.ValidationError();
        error.setField("householdNumber");
        error.setMessage("Invalid format");
        errors.add(error);
        validationResponse.setErrors(errors);

        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);

        // Act
        DataIngestionResponse response = dataIngestionService.ingestData(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals(0, response.getSuccessfulRecords());
        assertEquals(1, response.getFailedRecords());
        assertFalse(response.getValidationErrors().isEmpty());

        verify(dataValidationService).validateData(any(ValidationRequest.class));
        verify(deduplicationService, never()).findDuplicates(any());
    }

    @Test
    void testIngestData_DuplicateFound() {
        // Arrange
        deduplicationResponse.setHasDuplicates(true);
        deduplicationResponse.setRecommendation("REJECT");

        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);
        when(deduplicationService.findDuplicates(any())).thenReturn(deduplicationResponse);

        // Act
        DataIngestionResponse response = dataIngestionService.ingestData(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals(0, response.getSuccessfulRecords());
        assertEquals(1, response.getFailedRecords());
        assertEquals(1, response.getDuplicateRecords());

        verify(dataValidationService).validateData(any(ValidationRequest.class));
        verify(deduplicationService).findDuplicates(any());
        verify(dataValidationService, never()).cleanData(any(), any());
    }

    @Test
    void testIngestData_ValidationOnly() {
        // Arrange
        testRequest.setValidateOnly(true);
        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);
        when(deduplicationService.findDuplicates(any())).thenReturn(deduplicationResponse);

        // Act
        DataIngestionResponse response = dataIngestionService.ingestData(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("VALID", response.getStatus());
        assertEquals(0, response.getSuccessfulRecords());
        assertEquals(0, response.getFailedRecords());

        verify(dataValidationService).validateData(any(ValidationRequest.class));
        verify(deduplicationService).findDuplicates(any());
        verify(dataValidationService, never()).cleanData(any(), any());
    }

    @Test
    void testIngestData_SkipDuplicateCheck() {
        // Arrange
        testRequest.setSkipDuplicateCheck(true);
        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);
        when(dataValidationService.cleanData(any(), any())).thenReturn(testRequest.getDataPayload());

        // Act
        DataIngestionResponse response = dataIngestionService.ingestData(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(1, response.getSuccessfulRecords());
        assertEquals(0, response.getFailedRecords());

        verify(dataValidationService).validateData(any(ValidationRequest.class));
        verify(deduplicationService, never()).findDuplicates(any());
        verify(dataValidationService).cleanData(any(), any());
    }

    @Test
    void testGetIngestionStatus_Found() {
        // Arrange
        UUID ingestionId = UUID.randomUUID();
        DataIngestionBatch batch = new DataIngestionBatch();
        batch.setBatchId("BATCH-001");
        batch.setStatus("SUCCESS");
        batch.setTotalRecords(10);
        batch.setSuccessfulRecords(9);
        batch.setFailedRecords(1);
        batch.setDuplicateRecords(0);
        batch.setProcessingTimeMs(5000L);
        batch.setCompletedAt(LocalDateTime.now());

        when(batchRepository.findById(ingestionId)).thenReturn(Optional.of(batch));

        // Act
        DataIngestionResponse response = dataIngestionService.getIngestionStatus(ingestionId);

        // Assert
        assertNotNull(response);
        assertEquals(ingestionId, response.getIngestionId());
        assertEquals("BATCH-001", response.getBatchId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(10, response.getTotalRecords());
        assertEquals(9, response.getSuccessfulRecords());
        assertEquals(1, response.getFailedRecords());
        assertEquals(0, response.getDuplicateRecords());
        assertEquals("5000", response.getProcessingTimeMs());

        verify(batchRepository).findById(ingestionId);
    }

    @Test
    void testGetIngestionStatus_NotFound() {
        // Arrange
        UUID ingestionId = UUID.randomUUID();
        when(batchRepository.findById(ingestionId)).thenReturn(Optional.empty());

        // Act
        DataIngestionResponse response = dataIngestionService.getIngestionStatus(ingestionId);

        // Assert
        assertNotNull(response);
        assertEquals(ingestionId, response.getIngestionId());
        assertEquals("NOT_FOUND", response.getStatus());
        assertEquals("Ingestion record not found", response.getMessage());

        verify(batchRepository).findById(ingestionId);
    }

    @Test
    void testProcessLegacyDataFile_Success() {
        // Arrange
        String sourceSystem = "LISTAHANAN";
        String filePath = "/test/data.csv";
        String dataType = "HOUSEHOLD";

        LegacyDataParserService.FileMetadata metadata = new LegacyDataParserService.FileMetadata();
        metadata.setValid(true);
        metadata.setFileSizeBytes(1024L);

        List<DataIngestionRequest> requests = Arrays.asList(testRequest);

        when(legacyDataParserService.validateFileFormat(sourceSystem, filePath)).thenReturn(true);
        when(legacyDataParserService.getFileMetadata(filePath)).thenReturn(metadata);
        when(legacyDataParserService.parseFile(sourceSystem, filePath, dataType)).thenReturn(requests);
        when(batchRepository.save(any(DataIngestionBatch.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the ingestBatch method by calling the real implementation
        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);
        when(deduplicationService.findDuplicates(any())).thenReturn(deduplicationResponse);
        when(dataValidationService.cleanData(any(), any())).thenReturn(testRequest.getDataPayload());

        // Act
        DataIngestionResponse response = dataIngestionService.processLegacyDataFile(sourceSystem, filePath, dataType);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getIngestionId());

        verify(legacyDataParserService).validateFileFormat(sourceSystem, filePath);
        verify(legacyDataParserService).getFileMetadata(filePath);
        verify(legacyDataParserService).parseFile(sourceSystem, filePath, dataType);
        verify(batchRepository).save(any(DataIngestionBatch.class));
    }

    @Test
    void testProcessLegacyDataFile_InvalidFormat() {
        // Arrange
        String sourceSystem = "LISTAHANAN";
        String filePath = "/test/invalid.txt";
        String dataType = "HOUSEHOLD";

        when(legacyDataParserService.validateFileFormat(sourceSystem, filePath)).thenReturn(false);

        // Act
        DataIngestionResponse response = dataIngestionService.processLegacyDataFile(sourceSystem, filePath, dataType);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Invalid file format"));

        verify(legacyDataParserService).validateFileFormat(sourceSystem, filePath);
        verify(legacyDataParserService, never()).getFileMetadata(any());
        verify(legacyDataParserService, never()).parseFile(any(), any(), any());
    }

    @Test
    void testValidateData() {
        // Arrange
        when(dataValidationService.validateData(any(ValidationRequest.class))).thenReturn(validationResponse);
        when(deduplicationService.findDuplicates(any())).thenReturn(deduplicationResponse);

        // Act
        DataIngestionResponse response = dataIngestionService.validateData(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("VALID", response.getStatus());
        assertEquals(0, response.getSuccessfulRecords());
        assertEquals(0, response.getFailedRecords());

        verify(dataValidationService).validateData(any(ValidationRequest.class));
        verify(deduplicationService).findDuplicates(any());
        verify(dataValidationService, never()).cleanData(any(), any());
    }

    @Test
    void testGetIngestionStatistics_SpecificBatch() {
        // Arrange
        String batchId = "BATCH-001";
        DataIngestionBatch batch = new DataIngestionBatch();
        batch.setBatchId(batchId);
        batch.setStatus("SUCCESS");
        batch.setTotalRecords(100);
        batch.setSuccessfulRecords(95);
        batch.setFailedRecords(5);
        batch.setDuplicateRecords(2);
        batch.setProcessingTimeMs(10000L);
        batch.setCompletedAt(LocalDateTime.now());

        when(batchRepository.findByBatchId(batchId)).thenReturn(Optional.of(batch));

        // Act
        DataIngestionResponse response = dataIngestionService.getIngestionStatistics(batchId);

        // Assert
        assertNotNull(response);
        assertEquals(batchId, response.getBatchId());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(100, response.getTotalRecords());
        assertEquals(95, response.getSuccessfulRecords());
        assertEquals(5, response.getFailedRecords());
        assertEquals(2, response.getDuplicateRecords());
        assertEquals("10000", response.getProcessingTimeMs());

        verify(batchRepository).findByBatchId(batchId);
    }

    @Test
    void testGetIngestionStatistics_BatchNotFound() {
        // Arrange
        String batchId = "NONEXISTENT";
        when(batchRepository.findByBatchId(batchId)).thenReturn(Optional.empty());

        // Act
        DataIngestionResponse response = dataIngestionService.getIngestionStatistics(batchId);

        // Assert
        assertNotNull(response);
        assertEquals(batchId, response.getBatchId());
        assertEquals("NOT_FOUND", response.getStatus());
        assertTrue(response.getMessage().contains("Batch not found"));

        verify(batchRepository).findByBatchId(batchId);
    }

    @Test
    void testGetIngestionStatistics_OverallStats() {
        // Arrange - null batchId for overall stats
        
        // Act
        DataIngestionResponse response = dataIngestionService.getIngestionStatistics(null);

        // Assert
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertTrue(response.getMessage().contains("Overall ingestion statistics"));
        // Note: Current implementation returns hardcoded values for overall stats
        assertEquals(1000, response.getTotalRecords());
        assertEquals(950, response.getSuccessfulRecords());
        assertEquals(30, response.getFailedRecords());
        assertEquals(20, response.getDuplicateRecords());

        verify(batchRepository, never()).findByBatchId(any());
    }
}
