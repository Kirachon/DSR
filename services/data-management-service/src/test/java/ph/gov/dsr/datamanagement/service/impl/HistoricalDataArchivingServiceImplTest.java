package ph.gov.dsr.datamanagement.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import ph.gov.dsr.datamanagement.entity.ArchivedData;
import ph.gov.dsr.datamanagement.entity.Household;
import ph.gov.dsr.datamanagement.entity.HouseholdMember;
import ph.gov.dsr.datamanagement.entity.RetentionPolicy;
import ph.gov.dsr.datamanagement.repository.ArchivedDataRepository;
import ph.gov.dsr.datamanagement.repository.HouseholdMemberRepository;
import ph.gov.dsr.datamanagement.repository.HouseholdRepository;
import ph.gov.dsr.datamanagement.repository.RetentionPolicyRepository;
import ph.gov.dsr.datamanagement.service.HistoricalDataArchivingService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoricalDataArchivingServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@ExtendWith(MockitoExtension.class)
class HistoricalDataArchivingServiceImplTest {

    @Mock
    private ArchivedDataRepository archivedDataRepository;

    @Mock
    private RetentionPolicyRepository retentionPolicyRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private HouseholdMemberRepository householdMemberRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HistoricalDataArchivingServiceImpl archivingService;

    private RetentionPolicy testRetentionPolicy;
    private Household testHousehold;
    private HouseholdMember testHouseholdMember;
    private ArchivedData testArchivedData;

    @BeforeEach
    void setUp() {
        // Set up test configuration values
        ReflectionTestUtils.setField(archivingService, "defaultRetentionDays", 2555);
        ReflectionTestUtils.setField(archivingService, "autoArchiveEnabled", true);
        ReflectionTestUtils.setField(archivingService, "archiveBatchSize", 1000);

        // Create test retention policy
        testRetentionPolicy = new RetentionPolicy();
        testRetentionPolicy.setPolicyId(UUID.randomUUID());
        testRetentionPolicy.setEntityType("HOUSEHOLD");
        testRetentionPolicy.setRetentionDays(2555);
        testRetentionPolicy.setAutoArchiveEnabled(true);
        testRetentionPolicy.setArchiveAfterDays(1825);
        testRetentionPolicy.setIsActive(true);

        // Create test household
        testHousehold = new Household();
        testHousehold.setId(UUID.randomUUID());
        testHousehold.setHouseholdNumber("HH-2024-001234");
        testHousehold.setHeadOfHouseholdPsn("1234-5678-9012");
        testHousehold.setTotalMembers(4);
        testHousehold.setCreatedAt(LocalDateTime.now().minusDays(2000));
        testHousehold.setUpdatedAt(LocalDateTime.now().minusDays(100));

        // Create test household member
        testHouseholdMember = new HouseholdMember();
        testHouseholdMember.setId(UUID.randomUUID());
        testHouseholdMember.setHousehold(testHousehold);
        testHouseholdMember.setPsn("1234-5678-9012");
        testHouseholdMember.setFirstName("Juan");
        testHouseholdMember.setLastName("Dela Cruz");
        testHouseholdMember.setCreatedAt(LocalDateTime.now().minusDays(2000));
        testHouseholdMember.setUpdatedAt(LocalDateTime.now().minusDays(100));

        // Create test archived data
        testArchivedData = new ArchivedData();
        testArchivedData.setArchiveId(UUID.randomUUID());
        testArchivedData.setOriginalEntityId(testHousehold.getId());
        testArchivedData.setEntityType("HOUSEHOLD");
        testArchivedData.setArchivedData("{\"id\":\"" + testHousehold.getId() + "\"}");
        testArchivedData.setArchiveReason("Test archival");
        testArchivedData.setArchivedBy("SYSTEM");
        testArchivedData.setArchiveStatus(ArchivedData.ArchiveStatus.ACTIVE);
        testArchivedData.setArchivedAt(LocalDateTime.now());
    }

    @Test
    void testArchiveOldData_HouseholdType_Success() throws Exception {
        // Arrange
        String entityType = "HOUSEHOLD";
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1000);
        
        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.of(testRetentionPolicy));
        
        Page<Household> householdPage = new PageImpl<>(Arrays.asList(testHousehold));
        when(householdRepository.findAll(any(PageRequest.class))).thenReturn(householdPage);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        when(archivedDataRepository.save(any(ArchivedData.class))).thenReturn(testArchivedData);

        // Act
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveOldData(entityType, cutoffDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
        assertNotNull(result.getArchivedAt());
        assertTrue(result.getErrors().isEmpty());
        verify(archivedDataRepository, atLeastOnce()).save(any(ArchivedData.class));
        verify(householdRepository, atLeastOnce()).delete(any(Household.class));
    }

    @Test
    void testArchiveOldData_HouseholdMemberType_Success() throws Exception {
        // Arrange
        String entityType = "HOUSEHOLD_MEMBER";
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1000);
        
        RetentionPolicy memberPolicy = new RetentionPolicy();
        memberPolicy.setEntityType("HOUSEHOLD_MEMBER");
        memberPolicy.setRetentionDays(2555);
        memberPolicy.setAutoArchiveEnabled(true);
        memberPolicy.setIsActive(true);
        
        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.of(memberPolicy));
        
        Page<HouseholdMember> memberPage = new PageImpl<>(Arrays.asList(testHouseholdMember));
        when(householdMemberRepository.findAll(any(PageRequest.class))).thenReturn(memberPage);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        when(archivedDataRepository.save(any(ArchivedData.class))).thenReturn(testArchivedData);

        // Act
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveOldData(entityType, cutoffDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
        verify(archivedDataRepository, atLeastOnce()).save(any(ArchivedData.class));
        verify(householdMemberRepository, atLeastOnce()).delete(any(HouseholdMember.class));
    }

    @Test
    void testArchiveOldData_UnsupportedEntityType_Failure() {
        // Arrange
        String entityType = "UNSUPPORTED_TYPE";
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1000);
        
        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.of(testRetentionPolicy));

        // Act
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveOldData(entityType, cutoffDate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getArchivedCount());
        assertTrue(result.getMessage().contains("Unsupported entity type"));
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void testArchiveOldData_NoRetentionPolicy_UsesDefault() throws Exception {
        // Arrange
        String entityType = "HOUSEHOLD";
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1000);
        
        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.empty());
        
        Page<Household> householdPage = new PageImpl<>(Arrays.asList(testHousehold));
        when(householdRepository.findAll(any(PageRequest.class))).thenReturn(householdPage);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        when(archivedDataRepository.save(any(ArchivedData.class))).thenReturn(testArchivedData);

        // Act
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveOldData(entityType, cutoffDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
    }

    @Test
    void testArchiveEntity_HouseholdFound_Success() throws Exception {
        // Arrange
        UUID entityId = testHousehold.getId();
        String entityType = "HOUSEHOLD";
        String reason = "Manual archival";
        
        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.of(testRetentionPolicy));
        when(householdRepository.findById(entityId)).thenReturn(Optional.of(testHousehold));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        when(archivedDataRepository.save(any(ArchivedData.class))).thenReturn(testArchivedData);

        // Act
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveEntity(entityId, entityType, reason);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
        assertEquals("Entity archived successfully", result.getMessage());
        verify(householdRepository).findById(entityId);
        verify(archivedDataRepository).save(any(ArchivedData.class));
        verify(householdRepository).delete(testHousehold);
    }

    @Test
    void testArchiveEntity_EntityNotFound_Failure() {
        // Arrange
        UUID entityId = UUID.randomUUID();
        String entityType = "HOUSEHOLD";
        String reason = "Manual archival";
        
        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.of(testRetentionPolicy));
        when(householdRepository.findById(entityId)).thenReturn(Optional.empty());

        // Act
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveEntity(entityId, entityType, reason);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getArchivedCount());
        assertTrue(result.getMessage().contains("not found"));
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void testRestoreArchivedData_Success() throws Exception {
        // Arrange
        UUID archiveId = testArchivedData.getArchiveId();
        
        when(archivedDataRepository.findById(archiveId)).thenReturn(Optional.of(testArchivedData));
        when(objectMapper.readValue(anyString(), eq(Household.class))).thenReturn(testHousehold);
        when(householdRepository.save(any(Household.class))).thenReturn(testHousehold);
        when(archivedDataRepository.save(any(ArchivedData.class))).thenReturn(testArchivedData);

        // Act
        HistoricalDataArchivingService.RestoreResult result = 
                archivingService.restoreArchivedData(archiveId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRestoredCount());
        assertEquals("Data restored successfully", result.getMessage());
        verify(householdRepository).save(any(Household.class));
        verify(archivedDataRepository).save(any(ArchivedData.class));
    }

    @Test
    void testRestoreArchivedData_NotFound_Failure() {
        // Arrange
        UUID archiveId = UUID.randomUUID();
        
        when(archivedDataRepository.findById(archiveId)).thenReturn(Optional.empty());

        // Act
        HistoricalDataArchivingService.RestoreResult result = 
                archivingService.restoreArchivedData(archiveId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getRestoredCount());
        assertTrue(result.getMessage().contains("not found"));
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void testRestoreArchivedData_AlreadyRestored_Failure() {
        // Arrange
        UUID archiveId = testArchivedData.getArchiveId();
        testArchivedData.setArchiveStatus(ArchivedData.ArchiveStatus.RESTORED);
        
        when(archivedDataRepository.findById(archiveId)).thenReturn(Optional.of(testArchivedData));

        // Act
        HistoricalDataArchivingService.RestoreResult result = 
                archivingService.restoreArchivedData(archiveId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getRestoredCount());
        assertTrue(result.getMessage().contains("already been restored"));
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void testGetArchivingStatistics_Success() {
        // Arrange
        Object[] stats = {10L, 8L, 2L, 0L, 1024L, LocalDateTime.now()};
        List<Object[]> countByType = Arrays.asList(
                new Object[]{"HOUSEHOLD", 5L},
                new Object[]{"HOUSEHOLD_MEMBER", 3L}
        );
        List<Object[]> countByStatus = Arrays.asList(
                new Object[]{"ACTIVE", 8L},
                new Object[]{"RESTORED", 2L}
        );

        when(archivedDataRepository.getArchivingStatistics()).thenReturn(stats);
        when(archivedDataRepository.countArchivedDataByEntityType()).thenReturn(countByType);
        when(archivedDataRepository.countArchivedDataByStatus()).thenReturn(countByStatus);
        when(retentionPolicyRepository.count()).thenReturn(5L);

        // Act
        Map<String, Object> result = archivingService.getArchivingStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.get("totalArchived"));
        assertEquals(8L, result.get("activeArchives"));
        assertEquals(2L, result.get("restoredArchives"));
        assertEquals(0L, result.get("expiredArchives"));
        assertEquals(1024L, result.get("totalSizeBytes"));
        assertEquals(5L, result.get("retentionPoliciesCount"));

        @SuppressWarnings("unchecked")
        Map<String, Long> countByTypeMap = (Map<String, Long>) result.get("countByEntityType");
        assertEquals(5L, countByTypeMap.get("HOUSEHOLD"));
        assertEquals(3L, countByTypeMap.get("HOUSEHOLD_MEMBER"));

        @SuppressWarnings("unchecked")
        Map<String, Long> countByStatusMap = (Map<String, Long>) result.get("countByStatus");
        assertEquals(8L, countByStatusMap.get("ACTIVE"));
        assertEquals(2L, countByStatusMap.get("RESTORED"));
    }

    @Test
    void testGetArchivingStatistics_EmptyStats_ReturnsDefaults() {
        // Arrange
        when(archivedDataRepository.getArchivingStatistics()).thenReturn(null);
        when(archivedDataRepository.countArchivedDataByEntityType()).thenReturn(Collections.emptyList());
        when(archivedDataRepository.countArchivedDataByStatus()).thenReturn(Collections.emptyList());
        when(retentionPolicyRepository.count()).thenReturn(0L);

        // Act
        Map<String, Object> result = archivingService.getArchivingStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.get("totalArchived"));
        assertEquals(0L, result.get("activeArchives"));
        assertEquals(0L, result.get("restoredArchives"));
        assertEquals(0L, result.get("expiredArchives"));
        assertEquals(0L, result.get("totalSizeBytes"));
        assertNull(result.get("lastArchiveDate"));
        assertEquals(0L, result.get("retentionPoliciesCount"));
    }

    @Test
    void testGetArchivingStatistics_Exception_ReturnsErrorResult() {
        // Arrange
        when(archivedDataRepository.getArchivingStatistics()).thenThrow(new RuntimeException("Database error"));

        // Act
        Map<String, Object> result = archivingService.getArchivingStatistics();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertTrue(result.get("error").toString().contains("Failed to retrieve statistics"));
    }

    @Test
    void testConfigureRetentionPolicy_NewPolicy_Success() {
        // Arrange
        String entityType = "NEW_ENTITY";
        int retentionDays = 1000;
        boolean autoArchive = true;

        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.empty());
        when(retentionPolicyRepository.save(any(RetentionPolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> archivingService.configureRetentionPolicy(entityType, retentionDays, autoArchive));

        // Assert
        verify(retentionPolicyRepository).save(argThat(policy ->
                policy.getEntityType().equals(entityType) &&
                policy.getRetentionDays().equals(retentionDays) &&
                policy.getAutoArchiveEnabled().equals(autoArchive) &&
                policy.getIsActive().equals(true)
        ));
    }

    @Test
    void testConfigureRetentionPolicy_UpdateExisting_Success() {
        // Arrange
        String entityType = "HOUSEHOLD";
        int retentionDays = 3000;
        boolean autoArchive = false;

        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenReturn(Optional.of(testRetentionPolicy));
        when(retentionPolicyRepository.save(any(RetentionPolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> archivingService.configureRetentionPolicy(entityType, retentionDays, autoArchive));

        // Assert
        verify(retentionPolicyRepository).save(argThat(policy ->
                policy.getEntityType().equals(entityType) &&
                policy.getRetentionDays().equals(retentionDays) &&
                policy.getAutoArchiveEnabled().equals(autoArchive) &&
                policy.getUpdatedBy().equals("SYSTEM")
        ));
    }

    @Test
    void testConfigureRetentionPolicy_Exception_ThrowsRuntimeException() {
        // Arrange
        String entityType = "HOUSEHOLD";
        int retentionDays = 1000;
        boolean autoArchive = true;

        when(retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> archivingService.configureRetentionPolicy(entityType, retentionDays, autoArchive));

        assertTrue(exception.getMessage().contains("Failed to configure retention policy"));
    }

    @Test
    void testGetArchivedData_WithAllFilters_Success() {
        // Arrange
        String entityType = "HOUSEHOLD";
        LocalDateTime fromDate = LocalDateTime.now().minusDays(30);
        LocalDateTime toDate = LocalDateTime.now();

        List<ArchivedData> archivedDataList = Arrays.asList(testArchivedData);
        when(archivedDataRepository.findByEntityTypeAndArchivedAtBetweenAndArchiveStatusOrderByArchivedAtDesc(
                eq(entityType), eq(fromDate), eq(toDate), eq(ArchivedData.ArchiveStatus.ACTIVE)))
                .thenReturn(archivedDataList);

        // Act
        List<HistoricalDataArchivingService.ArchivedDataRecord> result =
                archivingService.getArchivedData(entityType, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testArchivedData.getArchiveId(), result.get(0).getArchiveId());
        assertEquals(testArchivedData.getEntityType(), result.get(0).getEntityType());
    }

    @Test
    void testGetArchivedData_EntityTypeOnly_Success() {
        // Arrange
        String entityType = "HOUSEHOLD";

        List<ArchivedData> archivedDataList = Arrays.asList(testArchivedData);
        when(archivedDataRepository.findByEntityTypeAndArchiveStatusOrderByArchivedAtDesc(
                eq(entityType), eq(ArchivedData.ArchiveStatus.ACTIVE)))
                .thenReturn(archivedDataList);

        // Act
        List<HistoricalDataArchivingService.ArchivedDataRecord> result =
                archivingService.getArchivedData(entityType, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetArchivedData_DateRangeOnly_Success() {
        // Arrange
        LocalDateTime fromDate = LocalDateTime.now().minusDays(30);
        LocalDateTime toDate = LocalDateTime.now();

        List<ArchivedData> archivedDataList = Arrays.asList(testArchivedData);
        when(archivedDataRepository.findByArchivedAtBetweenAndArchiveStatusOrderByArchivedAtDesc(
                eq(fromDate), eq(toDate), eq(ArchivedData.ArchiveStatus.ACTIVE)))
                .thenReturn(archivedDataList);

        // Act
        List<HistoricalDataArchivingService.ArchivedDataRecord> result =
                archivingService.getArchivedData(null, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetArchivedData_NoFilters_Success() {
        // Arrange
        Page<ArchivedData> archivedDataPage = new PageImpl<>(Arrays.asList(testArchivedData));
        when(archivedDataRepository.findByArchiveStatusOrderByArchivedAtDesc(
                eq(ArchivedData.ArchiveStatus.ACTIVE), any(PageRequest.class)))
                .thenReturn(archivedDataPage);

        // Act
        List<HistoricalDataArchivingService.ArchivedDataRecord> result =
                archivingService.getArchivedData(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetArchivedData_Exception_ReturnsEmptyList() {
        // Arrange
        when(archivedDataRepository.findByArchiveStatusOrderByArchivedAtDesc(
                any(), any(PageRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        List<HistoricalDataArchivingService.ArchivedDataRecord> result =
                archivingService.getArchivedData(null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsEntityArchived_EntityExists_ReturnsTrue() {
        // Arrange
        UUID entityId = testHousehold.getId();
        String entityType = "HOUSEHOLD";

        when(archivedDataRepository.existsByOriginalEntityIdAndEntityTypeAndArchiveStatus(
                entityId, entityType, ArchivedData.ArchiveStatus.ACTIVE))
                .thenReturn(true);

        // Act
        boolean result = archivingService.isEntityArchived(entityId, entityType);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsEntityArchived_EntityNotExists_ReturnsFalse() {
        // Arrange
        UUID entityId = UUID.randomUUID();
        String entityType = "HOUSEHOLD";

        when(archivedDataRepository.existsByOriginalEntityIdAndEntityTypeAndArchiveStatus(
                entityId, entityType, ArchivedData.ArchiveStatus.ACTIVE))
                .thenReturn(false);

        // Act
        boolean result = archivingService.isEntityArchived(entityId, entityType);

        // Assert
        assertFalse(result);
    }
}
