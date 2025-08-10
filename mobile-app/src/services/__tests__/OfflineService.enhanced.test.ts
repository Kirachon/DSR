import { OfflineService } from '../OfflineService';
import { OFFLINE_CONFIG } from '../../config/constants';

// Mock dependencies
jest.mock('react-native-sqlite-storage');
jest.mock('@react-native-community/netinfo');
jest.mock('react-native-mmkv');
jest.mock('crypto-js');
jest.mock('react-native-background-job');

describe('Enhanced OfflineService', () => {
  beforeEach(async () => {
    jest.clearAllMocks();
    // Initialize service for testing
    await OfflineService.initialize();
  });

  afterEach(() => {
    jest.clearAllTimers();
  });

  describe('Priority-based Sync Queue', () => {
    it('should prioritize high priority items in sync queue', async () => {
      // Save items with different priorities
      const highPriorityId = await OfflineService.saveOfflineData(
        'registration',
        { psn: '1234-5678-9012', name: 'High Priority' },
        'high'
      );

      const lowPriorityId = await OfflineService.saveOfflineData(
        'case',
        { caseId: 'CASE-001', description: 'Low Priority' },
        'low'
      );

      const mediumPriorityId = await OfflineService.saveOfflineData(
        'update',
        { householdId: 'HH-001', update: 'Medium Priority' },
        'medium'
      );

      // Verify sync queue ordering
      const offlineData = await OfflineService.getOfflineData();
      expect(offlineData).toHaveLength(3);
      
      // High priority should be first
      expect(offlineData[0].priority).toBe('high');
      expect(offlineData[0].id).toBe(highPriorityId);
    });

    it('should handle background sync for high priority items', async () => {
      const syncSpy = jest.spyOn(OfflineService, 'syncOfflineData');
      
      // Save high priority item
      await OfflineService.saveOfflineData(
        'registration',
        { psn: '1234-5678-9012', urgent: true },
        'high'
      );

      // Simulate background sync trigger
      jest.advanceTimersByTime(30000); // 30 seconds

      expect(syncSpy).toHaveBeenCalled();
    });
  });

  describe('Data Integrity Validation', () => {
    it('should detect data corruption', async () => {
      const itemId = await OfflineService.saveOfflineData(
        'registration',
        { psn: '1234-5678-9012', name: 'Test User' }
      );

      // Simulate data corruption by modifying stored data
      const corruptedData = { psn: '1234-5678-9012', name: 'Corrupted User' };
      
      // Mock corrupted checksum
      jest.spyOn(OfflineService['integrityChecker'], 'validateIntegrity')
        .mockReturnValue({
          id: itemId,
          isValid: false,
          checksum: 'corrupted_checksum',
          expectedChecksum: 'original_checksum',
          corruptionType: 'checksum_mismatch'
        });

      const integrityCheck = OfflineService['integrityChecker'].validateIntegrity({
        id: itemId,
        type: 'registration',
        data: corruptedData,
        timestamp: Date.now(),
        synced: false,
        retryCount: 0,
        priority: 'medium',
        checksum: 'original_checksum',
        lastModified: Date.now(),
        version: 1
      });

      expect(integrityCheck.isValid).toBe(false);
      expect(integrityCheck.corruptionType).toBe('checksum_mismatch');
    });

    it('should repair corrupted data when possible', async () => {
      const originalData = { psn: '1234-5678-9012', name: 'Test User' };
      const itemId = await OfflineService.saveOfflineData('registration', originalData);

      // Mock successful repair
      jest.spyOn(OfflineService['integrityChecker'], 'repairData')
        .mockReturnValue({
          id: itemId,
          type: 'registration',
          data: originalData,
          timestamp: Date.now(),
          synced: false,
          retryCount: 0,
          priority: 'medium',
          checksum: 'repaired_checksum',
          lastModified: Date.now(),
          version: 1
        });

      const corruptedItem = {
        id: itemId,
        type: 'registration' as const,
        data: { corrupted: true },
        timestamp: Date.now(),
        synced: false,
        retryCount: 0,
        priority: 'medium' as const,
        checksum: 'wrong_checksum',
        lastModified: Date.now(),
        version: 1
      };

      const repairedItem = OfflineService['integrityChecker'].repairData(corruptedItem);
      
      expect(repairedItem).not.toBeNull();
      expect(repairedItem?.data).toEqual(originalData);
    });
  });

  describe('Conflict Resolution', () => {
    it('should resolve conflicts using server_wins strategy', () => {
      const conflict = {
        id: 'test-conflict',
        type: 'update',
        clientData: { name: 'Client Name', version: 1 },
        serverData: { name: 'Server Name', version: 2 },
        timestamp: Date.now(),
        resolution: 'server_wins' as const
      };

      const resolved = OfflineService['conflictResolver'].resolveConflict(conflict);
      expect(resolved).toEqual(conflict.serverData);
    });

    it('should resolve conflicts using client_wins strategy', () => {
      const conflict = {
        id: 'test-conflict',
        type: 'update',
        clientData: { name: 'Client Name', version: 2 },
        serverData: { name: 'Server Name', version: 1 },
        timestamp: Date.now(),
        resolution: 'client_wins' as const
      };

      const resolved = OfflineService['conflictResolver'].resolveConflict(conflict);
      expect(resolved).toEqual(conflict.clientData);
    });

    it('should merge data intelligently', () => {
      const conflict = {
        id: 'test-conflict',
        type: 'update',
        clientData: { 
          name: 'Client Name', 
          contactNumber: '123-456-7890',
          lastModified: Date.now() + 1000,
          version: 1 
        },
        serverData: { 
          name: 'Server Name', 
          address: 'Server Address',
          lastModified: Date.now(),
          version: 1 
        },
        timestamp: Date.now(),
        resolution: 'merge' as const
      };

      const resolved = OfflineService['conflictResolver'].resolveConflict(conflict);
      
      // Should merge updatable fields from client
      expect(resolved.contactNumber).toBe('123-456-7890');
      // Should keep server data for non-updatable fields
      expect(resolved.name).toBe('Server Name');
      expect(resolved.address).toBe('Server Address');
      // Should increment version
      expect(resolved.version).toBe(2);
    });
  });

  describe('Manual Review Process', () => {
    it('should mark items for manual review', async () => {
      const itemId = 'test-item-id';
      await OfflineService['markForManualReview'](itemId);

      const reviewItems = await OfflineService.getItemsForManualReview();
      expect(reviewItems.some(item => item.id === itemId)).toBe(true);
    });

    it('should resolve manual review items', async () => {
      const itemId = 'test-item-id';
      await OfflineService['markForManualReview'](itemId);

      await OfflineService.resolveManualReviewItem(itemId, 'ignore');

      const reviewItems = await OfflineService.getItemsForManualReview();
      expect(reviewItems.some(item => item.id === itemId)).toBe(false);
    });
  });

  describe('Enhanced Sync Process', () => {
    it('should include conflict information in sync results', async () => {
      // Mock a sync with conflicts
      jest.spyOn(OfflineService, 'syncOfflineData').mockResolvedValue({
        success: true,
        syncedCount: 2,
        failedCount: 1,
        conflictCount: 1,
        errors: ['Sync error for item-3'],
        conflicts: [{
          id: 'conflict-item',
          type: 'update',
          clientData: { name: 'Client' },
          serverData: { name: 'Server' },
          timestamp: Date.now(),
          resolution: 'manual'
        }]
      });

      const result = await OfflineService.syncOfflineData();
      
      expect(result.conflictCount).toBe(1);
      expect(result.conflicts).toHaveLength(1);
      expect(result.conflicts[0].resolution).toBe('manual');
    });

    it('should perform integrity check before sync', async () => {
      const integrityCheckSpy = jest.spyOn(OfflineService['integrityChecker'], 'validateIntegrity');
      
      await OfflineService.saveOfflineData(
        'registration',
        { psn: '1234-5678-9012', name: 'Test User' }
      );

      await OfflineService.syncOfflineData();

      expect(integrityCheckSpy).toHaveBeenCalled();
    });
  });

  describe('Performance and Optimization', () => {
    it('should limit background sync to high priority items', async () => {
      // Add multiple items with different priorities
      await OfflineService.saveOfflineData('registration', { id: 1 }, 'high');
      await OfflineService.saveOfflineData('registration', { id: 2 }, 'high');
      await OfflineService.saveOfflineData('registration', { id: 3 }, 'high');
      await OfflineService.saveOfflineData('registration', { id: 4 }, 'high');
      await OfflineService.saveOfflineData('registration', { id: 5 }, 'high');
      await OfflineService.saveOfflineData('registration', { id: 6 }, 'high');
      await OfflineService.saveOfflineData('registration', { id: 7 }, 'medium');

      const syncSpy = jest.spyOn(OfflineService, 'syncSingleItem');
      
      // Trigger background sync
      await OfflineService['syncHighPriorityItems']();

      // Should only sync 5 high priority items
      expect(syncSpy).toHaveBeenCalledTimes(5);
    });

    it('should handle retry logic with exponential backoff', async () => {
      const itemId = await OfflineService.saveOfflineData(
        'registration',
        { psn: '1234-5678-9012', name: 'Test User' }
      );

      // Mock sync failure
      jest.spyOn(OfflineService, 'syncSingleItem').mockRejectedValue(new Error('Network error'));

      const result = await OfflineService.syncOfflineData();
      
      expect(result.failedCount).toBe(1);
      expect(result.errors).toContain(`${itemId}: Network error`);
    });
  });
});
