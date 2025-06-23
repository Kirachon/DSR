package ph.gov.dsr.datamanagement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ph.gov.dsr.datamanagement.dto.DeduplicationRequest;
import ph.gov.dsr.datamanagement.dto.DeduplicationResponse;
import ph.gov.dsr.datamanagement.entity.DataIngestionBatch;
import ph.gov.dsr.datamanagement.repository.DataIngestionBatchRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeduplicationServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class DeduplicationServiceImplTest {

    @Mock
    private DataIngestionBatchRepository batchRepository;

    @InjectMocks
    private DeduplicationServiceImpl deduplicationService;

    private DeduplicationRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = new DeduplicationRequest();
        testRequest.setEntityType("HOUSEHOLD");
        testRequest.setMatchingAlgorithm("FUZZY");
        testRequest.setMatchThreshold(0.8);
        
        Map<String, Object> entityData = new HashMap<>();
        entityData.put("householdNumber", "HH-001");
        entityData.put("headOfHouseholdName", "Juan Dela Cruz");
        entityData.put("headOfHouseholdPsn", "1234-5678-9012");
        entityData.put("address", "123 Main Street, Manila");
        testRequest.setEntityData(entityData);
    }

    @Test
    void testFindDuplicates_HouseholdType_Success() {
        // Arrange
        when(batchRepository.findBySourceSystem(anyString())).thenReturn(Optional.empty());

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("HOUSEHOLD", response.getEntityType());
        assertEquals("FUZZY", response.getMatchingAlgorithm());
        assertEquals(0.8, response.getMatchThreshold());
        assertTrue(response.getTotalMatches() >= 0);
        assertNotNull(response.getProcessedAt());
        assertNotNull(response.getMatches());
    }

    @Test
    void testFindDuplicates_IndividualType_Success() {
        // Arrange
        testRequest.setEntityType("INDIVIDUAL");
        Map<String, Object> individualData = new HashMap<>();
        individualData.put("psn", "1234-5678-9012");
        individualData.put("firstName", "Juan");
        individualData.put("lastName", "Dela Cruz");
        individualData.put("birthDate", "1990-01-01");
        testRequest.setEntityData(individualData);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("INDIVIDUAL", response.getEntityType());
        assertTrue(response.getTotalMatches() >= 0);
        assertNotNull(response.getProcessedAt());
    }

    @Test
    void testFindDuplicates_ExactMatchAlgorithm() {
        // Arrange
        testRequest.setMatchingAlgorithm("EXACT");
        testRequest.setMatchThreshold(1.0);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("EXACT", response.getMatchingAlgorithm());
        assertEquals(1.0, response.getMatchThreshold());
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void testFindDuplicates_PhoneticMatchAlgorithm() {
        // Arrange
        testRequest.setMatchingAlgorithm("PHONETIC");
        testRequest.setMatchThreshold(0.9);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("PHONETIC", response.getMatchingAlgorithm());
        assertEquals(0.9, response.getMatchThreshold());
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void testFindDuplicates_LowThreshold() {
        // Arrange
        testRequest.setMatchThreshold(0.3);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0.3, response.getMatchThreshold());
        // Low threshold might find more matches
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void testFindDuplicates_HighThreshold() {
        // Arrange
        testRequest.setMatchThreshold(0.95);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0.95, response.getMatchThreshold());
        // High threshold should find fewer matches
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void testFindDuplicates_EmptyEntityData() {
        // Arrange
        testRequest.setEntityData(new HashMap<>());

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalMatches());
        assertTrue(response.getMatches().isEmpty());
    }

    @Test
    void testFindDuplicates_NullEntityData() {
        // Arrange
        testRequest.setEntityData(null);

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalMatches());
        assertTrue(response.getMatches().isEmpty());
    }

    @Test
    void testFindDuplicates_UnknownEntityType() {
        // Arrange
        testRequest.setEntityType("UNKNOWN_TYPE");

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("UNKNOWN_TYPE", response.getEntityType());
        assertEquals(0, response.getTotalMatches());
        assertTrue(response.getMatches().isEmpty());
    }

    @Test
    void testFindDuplicates_InvalidMatchingAlgorithm() {
        // Arrange
        testRequest.setMatchingAlgorithm("INVALID_ALGORITHM");

        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("INVALID_ALGORITHM", response.getMatchingAlgorithm());
        // Should still process but might return no matches
        assertTrue(response.getTotalMatches() >= 0);
    }

    @Test
    void testFindDuplicates_ResponseStructure() {
        // Act
        DeduplicationResponse response = deduplicationService.findDuplicates(testRequest);

        // Assert - Verify response structure
        assertNotNull(response);
        assertNotNull(response.getEntityType());
        assertNotNull(response.getMatchingAlgorithm());
        assertNotNull(response.getProcessedAt());
        assertNotNull(response.getMatches());
        assertTrue(response.getMatchThreshold() >= 0.0);
        assertTrue(response.getMatchThreshold() <= 1.0);
        assertTrue(response.getTotalMatches() >= 0);
        assertEquals(response.getTotalMatches(), response.getMatches().size());
    }
}
