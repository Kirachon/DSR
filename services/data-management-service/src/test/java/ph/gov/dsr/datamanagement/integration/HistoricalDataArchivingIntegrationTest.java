package ph.gov.dsr.datamanagement.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.datamanagement.entity.ArchivedData;
import ph.gov.dsr.datamanagement.entity.Household;
import ph.gov.dsr.datamanagement.entity.HouseholdMember;
import ph.gov.dsr.datamanagement.entity.RetentionPolicy;
import ph.gov.dsr.datamanagement.repository.ArchivedDataRepository;
import ph.gov.dsr.datamanagement.repository.HouseholdMemberRepository;
import ph.gov.dsr.datamanagement.repository.HouseholdRepository;
import ph.gov.dsr.datamanagement.repository.RetentionPolicyRepository;
import ph.gov.dsr.datamanagement.service.HistoricalDataArchivingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for HistoricalDataArchivingService
 * Tests the service with actual database connectivity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HistoricalDataArchivingIntegrationTest {

    @Autowired
    private HistoricalDataArchivingService archivingService;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private HouseholdMemberRepository householdMemberRepository;

    @Autowired
    private ArchivedDataRepository archivedDataRepository;

    @Autowired
    private RetentionPolicyRepository retentionPolicyRepository;

    private Household testHousehold;
    private HouseholdMember testHouseholdMember;
    private RetentionPolicy testRetentionPolicy;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        archivedDataRepository.deleteAll();
        householdMemberRepository.deleteAll();
        householdRepository.deleteAll();
        retentionPolicyRepository.deleteAll();

        // Create test retention policy
        testRetentionPolicy = new RetentionPolicy();
        testRetentionPolicy.setEntityType("HOUSEHOLD");
        testRetentionPolicy.setRetentionDays(2555);
        testRetentionPolicy.setAutoArchiveEnabled(true);
        testRetentionPolicy.setArchiveAfterDays(1825);
        testRetentionPolicy.setPolicyDescription("Test retention policy");
        testRetentionPolicy.setCreatedBy("TEST_USER");
        testRetentionPolicy.setIsActive(true);
        testRetentionPolicy = retentionPolicyRepository.save(testRetentionPolicy);

        // Create test household
        testHousehold = new Household();
        testHousehold.setHouseholdNumber("HH-TEST-001234");
        testHousehold.setHeadOfHouseholdPsn("1234-5678-9012");
        testHousehold.setTotalMembers(2);
        testHousehold.setMonthlyIncome(new BigDecimal("25000.00"));
        testHousehold.setIsIndigenous(false);
        testHousehold.setIsPwdHousehold(false);
        testHousehold.setCreatedAt(LocalDateTime.now().minusDays(2000)); // Old enough to archive
        testHousehold.setUpdatedAt(LocalDateTime.now().minusDays(100));
        testHousehold = householdRepository.save(testHousehold);

        // Create test household member
        testHouseholdMember = new HouseholdMember();
        testHouseholdMember.setHousehold(testHousehold);
        testHouseholdMember.setPsn("1234-5678-9012");
        testHouseholdMember.setFirstName("Juan");
        testHouseholdMember.setLastName("Dela Cruz");
        testHouseholdMember.setMiddleName("Santos");
        testHouseholdMember.setBirthDate(LocalDateTime.now().minusYears(35).toLocalDate());
        testHouseholdMember.setCreatedAt(LocalDateTime.now().minusDays(2000));
        testHouseholdMember.setUpdatedAt(LocalDateTime.now().minusDays(100));
        testHouseholdMember = householdMemberRepository.save(testHouseholdMember);
    }

    @Test
    void testCompleteArchivingWorkflow_HouseholdArchival() {
        // Verify initial state
        assertEquals(1, householdRepository.count());
        assertEquals(1, householdMemberRepository.count());
        assertEquals(0, archivedDataRepository.count());

        // Act - Archive old household data
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1000);
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveOldData("HOUSEHOLD", cutoffDate);

        // Assert archiving result
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
        assertNotNull(result.getArchivedAt());
        assertTrue(result.getErrors().isEmpty());

        // Verify database state after archiving
        assertEquals(0, householdRepository.count()); // Original household should be deleted
        assertEquals(1, archivedDataRepository.count()); // Should have archived data

        // Verify archived data content
        List<ArchivedData> archivedDataList = archivedDataRepository.findAll();
        ArchivedData archivedData = archivedDataList.get(0);
        assertEquals(testHousehold.getId(), archivedData.getOriginalEntityId());
        assertEquals("HOUSEHOLD", archivedData.getEntityType());
        assertEquals(ArchivedData.ArchiveStatus.ACTIVE, archivedData.getArchiveStatus());
        assertNotNull(archivedData.getArchivedData());
        assertNotNull(archivedData.getChecksum());
        assertTrue(archivedData.getFileSizeBytes() > 0);
    }

    @Test
    void testCompleteArchivingWorkflow_EntityArchival() {
        // Verify initial state
        assertEquals(1, householdRepository.count());
        assertEquals(0, archivedDataRepository.count());

        // Act - Archive specific household entity
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveEntity(testHousehold.getId(), "HOUSEHOLD", "Manual test archival");

        // Assert archiving result
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
        assertEquals("Entity archived successfully", result.getMessage());

        // Verify database state
        assertEquals(0, householdRepository.count());
        assertEquals(1, archivedDataRepository.count());

        // Verify archived data
        ArchivedData archivedData = archivedDataRepository.findAll().get(0);
        assertEquals("Manual test archival", archivedData.getArchiveReason());
        assertEquals("SYSTEM", archivedData.getArchivedBy());
    }

    @Test
    void testCompleteRestoreWorkflow() {
        // First archive the household
        HistoricalDataArchivingService.ArchivingResult archiveResult = 
                archivingService.archiveEntity(testHousehold.getId(), "HOUSEHOLD", "Test archival for restore");

        assertTrue(archiveResult.isSuccess());
        assertEquals(0, householdRepository.count());
        assertEquals(1, archivedDataRepository.count());

        // Get the archived data ID
        ArchivedData archivedData = archivedDataRepository.findAll().get(0);
        UUID archiveId = archivedData.getArchiveId();

        // Act - Restore the archived data
        HistoricalDataArchivingService.RestoreResult restoreResult = 
                archivingService.restoreArchivedData(archiveId);

        // Assert restore result
        assertNotNull(restoreResult);
        assertTrue(restoreResult.isSuccess());
        assertEquals(1, restoreResult.getRestoredCount());
        assertEquals("Data restored successfully", restoreResult.getMessage());

        // Verify database state after restore
        assertEquals(1, householdRepository.count()); // Household should be restored
        assertEquals(1, archivedDataRepository.count()); // Archived data should still exist

        // Verify restored household
        Optional<Household> restoredHousehold = householdRepository.findByHouseholdNumber("HH-TEST-001234");
        assertTrue(restoredHousehold.isPresent());
        assertEquals(testHousehold.getHeadOfHouseholdPsn(), restoredHousehold.get().getHeadOfHouseholdPsn());

        // Verify archived data is marked as restored
        ArchivedData updatedArchivedData = archivedDataRepository.findById(archiveId).orElse(null);
        assertNotNull(updatedArchivedData);
        assertEquals(ArchivedData.ArchiveStatus.RESTORED, updatedArchivedData.getArchiveStatus());
        assertNotNull(updatedArchivedData.getRestoredAt());
        assertEquals("SYSTEM", updatedArchivedData.getRestoredBy());
    }

    @Test
    void testRetentionPolicyConfiguration() {
        // Act - Configure new retention policy
        String entityType = "TEST_ENTITY";
        int retentionDays = 1000;
        boolean autoArchive = true;

        assertDoesNotThrow(() -> 
                archivingService.configureRetentionPolicy(entityType, retentionDays, autoArchive));

        // Verify policy was created
        Optional<RetentionPolicy> policy = retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType);
        assertTrue(policy.isPresent());
        assertEquals(retentionDays, policy.get().getRetentionDays());
        assertEquals(autoArchive, policy.get().getAutoArchiveEnabled());
        assertEquals("SYSTEM", policy.get().getCreatedBy());

        // Act - Update existing policy
        int newRetentionDays = 1500;
        boolean newAutoArchive = false;

        assertDoesNotThrow(() -> 
                archivingService.configureRetentionPolicy(entityType, newRetentionDays, newAutoArchive));

        // Verify policy was updated
        Optional<RetentionPolicy> updatedPolicy = retentionPolicyRepository.findByEntityTypeAndIsActiveTrue(entityType);
        assertTrue(updatedPolicy.isPresent());
        assertEquals(newRetentionDays, updatedPolicy.get().getRetentionDays());
        assertEquals(newAutoArchive, updatedPolicy.get().getAutoArchiveEnabled());
        assertEquals("SYSTEM", updatedPolicy.get().getUpdatedBy());
    }

    @Test
    void testArchivingStatistics() {
        // Create some archived data first
        archivingService.archiveEntity(testHousehold.getId(), "HOUSEHOLD", "Test statistics");

        // Act - Get archiving statistics
        Map<String, Object> stats = archivingService.getArchivingStatistics();

        // Assert statistics
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalArchived"));
        assertTrue(stats.containsKey("activeArchives"));
        assertTrue(stats.containsKey("restoredArchives"));
        assertTrue(stats.containsKey("expiredArchives"));
        assertTrue(stats.containsKey("totalSizeBytes"));
        assertTrue(stats.containsKey("retentionPoliciesCount"));
        assertTrue(stats.containsKey("countByEntityType"));
        assertTrue(stats.containsKey("countByStatus"));

        // Verify specific values
        assertEquals(1L, stats.get("totalArchived"));
        assertEquals(1L, stats.get("activeArchives"));
        assertEquals(0L, stats.get("restoredArchives"));
        assertTrue((Long) stats.get("totalSizeBytes") > 0);

        @SuppressWarnings("unchecked")
        Map<String, Long> countByType = (Map<String, Long>) stats.get("countByEntityType");
        assertEquals(1L, countByType.get("HOUSEHOLD"));

        @SuppressWarnings("unchecked")
        Map<String, Long> countByStatus = (Map<String, Long>) stats.get("countByStatus");
        assertEquals(1L, countByStatus.get("ACTIVE"));
    }

    @Test
    void testGetArchivedDataWithFilters() {
        // Create archived data
        archivingService.archiveEntity(testHousehold.getId(), "HOUSEHOLD", "Test filter");

        // Act - Get archived data with entity type filter
        List<HistoricalDataArchivingService.ArchivedDataRecord> records = 
                archivingService.getArchivedData("HOUSEHOLD", null, null);

        // Assert
        assertNotNull(records);
        assertEquals(1, records.size());
        assertEquals("HOUSEHOLD", records.get(0).getEntityType());
        assertEquals(testHousehold.getId(), records.get(0).getOriginalEntityId());

        // Act - Get archived data with date range filter
        LocalDateTime fromDate = LocalDateTime.now().minusHours(1);
        LocalDateTime toDate = LocalDateTime.now().plusHours(1);
        List<HistoricalDataArchivingService.ArchivedDataRecord> dateFilteredRecords = 
                archivingService.getArchivedData(null, fromDate, toDate);

        // Assert
        assertNotNull(dateFilteredRecords);
        assertEquals(1, dateFilteredRecords.size());
    }

    @Test
    void testIsEntityArchived() {
        // Initially not archived
        assertFalse(archivingService.isEntityArchived(testHousehold.getId(), "HOUSEHOLD"));

        // Archive the entity
        archivingService.archiveEntity(testHousehold.getId(), "HOUSEHOLD", "Test entity check");

        // Now should be archived
        assertTrue(archivingService.isEntityArchived(testHousehold.getId(), "HOUSEHOLD"));

        // Different entity type should not be archived
        assertFalse(archivingService.isEntityArchived(testHousehold.getId(), "DIFFERENT_TYPE"));
    }

    @Test
    void testArchivingWithoutRetentionPolicy() {
        // Delete the retention policy
        retentionPolicyRepository.deleteAll();

        // Act - Archive should still work with default policy
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveOldData("HOUSEHOLD", LocalDateTime.now().minusDays(1000));

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getArchivedCount());
    }

    @Test
    void testArchiveNonExistentEntity() {
        // Act - Try to archive non-existent entity
        UUID nonExistentId = UUID.randomUUID();
        HistoricalDataArchivingService.ArchivingResult result = 
                archivingService.archiveEntity(nonExistentId, "HOUSEHOLD", "Test non-existent");

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getArchivedCount());
        assertTrue(result.getMessage().contains("not found"));
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void testRestoreNonExistentArchive() {
        // Act - Try to restore non-existent archive
        UUID nonExistentArchiveId = UUID.randomUUID();
        HistoricalDataArchivingService.RestoreResult result = 
                archivingService.restoreArchivedData(nonExistentArchiveId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getRestoredCount());
        assertTrue(result.getMessage().contains("not found"));
        assertFalse(result.getErrors().isEmpty());
    }
}
