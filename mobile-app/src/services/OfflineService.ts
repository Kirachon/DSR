import SQLite from 'react-native-sqlite-storage';
import NetInfo from '@react-native-community/netinfo';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { MMKV } from 'react-native-mmkv';
import CryptoJS from 'crypto-js';
import BackgroundJob from 'react-native-background-job';

// Enable SQLite debugging
SQLite.DEBUG(true);
SQLite.enablePromise(true);

export interface OfflineData {
  id: string;
  type: 'registration' | 'case' | 'update';
  data: any;
  timestamp: number;
  synced: boolean;
  retryCount: number;
  priority: 'high' | 'medium' | 'low';
  checksum: string;
  conflictResolution?: 'server_wins' | 'client_wins' | 'merge' | 'manual';
  lastModified: number;
  version: number;
}

export interface SyncResult {
  success: boolean;
  syncedCount: number;
  failedCount: number;
  conflictCount: number;
  errors: string[];
  conflicts: ConflictData[];
}

export interface ConflictData {
  id: string;
  type: string;
  clientData: any;
  serverData: any;
  timestamp: number;
  resolution?: 'server_wins' | 'client_wins' | 'merge' | 'manual';
}

export interface DataIntegrityCheck {
  id: string;
  isValid: boolean;
  checksum: string;
  expectedChecksum: string;
  corruptionType?: 'checksum_mismatch' | 'missing_data' | 'invalid_format';
}

class OfflineServiceClass {
  private db: SQLite.SQLiteDatabase | null = null;
  private storage: MMKV;
  private isOnline: boolean = true;
  private syncInProgress: boolean = false;
  private syncQueue: OfflineData[] = [];
  private conflictResolver: ConflictResolver;
  private integrityChecker: DataIntegrityChecker;
  private backgroundSyncTimer: NodeJS.Timeout | null = null;

  constructor() {
    this.storage = new MMKV({
      id: 'dsr-offline-storage',
      encryptionKey: 'dsr-offline-encryption-key',
    });
    this.conflictResolver = new ConflictResolver();
    this.integrityChecker = new DataIntegrityChecker();
  }

  async initialize(): Promise<void> {
    try {
      await this.initializeDatabase();
      await this.setupNetworkListener();
      await this.loadOfflineData();
      await this.startBackgroundSync();
      await this.performIntegrityCheck();
    } catch (error) {
      console.error('Offline service initialization failed:', error);
      throw error;
    }
  }

  private async initializeDatabase(): Promise<void> {
    try {
      this.db = await SQLite.openDatabase({
        name: 'DSROffline.db',
        location: 'default',
        createFromLocation: '~DSROffline.db',
      });

      await this.createTables();
      console.log('Offline database initialized successfully');
    } catch (error) {
      console.error('Database initialization failed:', error);
      throw error;
    }
  }

  private async createTables(): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const createOfflineDataTable = `
      CREATE TABLE IF NOT EXISTS offline_data (
        id TEXT PRIMARY KEY,
        type TEXT NOT NULL,
        data TEXT NOT NULL,
        timestamp INTEGER NOT NULL,
        synced INTEGER DEFAULT 0,
        retry_count INTEGER DEFAULT 0,
        priority TEXT DEFAULT 'medium',
        checksum TEXT NOT NULL DEFAULT '',
        last_modified INTEGER NOT NULL DEFAULT 0,
        version INTEGER DEFAULT 1,
        conflict_resolution TEXT DEFAULT NULL
      );
    `;

    const createHouseholdsTable = `
      CREATE TABLE IF NOT EXISTS households (
        id TEXT PRIMARY KEY,
        head_psn TEXT NOT NULL,
        address TEXT NOT NULL,
        members TEXT NOT NULL,
        economic_profile TEXT,
        created_at INTEGER NOT NULL,
        updated_at INTEGER NOT NULL,
        synced INTEGER DEFAULT 0
      );
    `;

    const createCasesTable = `
      CREATE TABLE IF NOT EXISTS cases (
        id TEXT PRIMARY KEY,
        complainant_psn TEXT NOT NULL,
        case_type TEXT NOT NULL,
        description TEXT NOT NULL,
        status TEXT DEFAULT 'PENDING',
        created_at INTEGER NOT NULL,
        updated_at INTEGER NOT NULL,
        synced INTEGER DEFAULT 0
      );
    `;

    await this.db.executeSql(createOfflineDataTable);
    await this.db.executeSql(createHouseholdsTable);
    await this.db.executeSql(createCasesTable);
  }

  private async setupNetworkListener(): Promise<void> {
    NetInfo.addEventListener(state => {
      const wasOffline = !this.isOnline;
      this.isOnline = state.isConnected ?? false;

      console.log(`Network status changed: ${this.isOnline ? 'Online' : 'Offline'}`);

      // Auto-sync when coming back online
      if (wasOffline && this.isOnline) {
        this.syncOfflineData();
      }
    });

    // Get initial network state
    const state = await NetInfo.fetch();
    this.isOnline = state.isConnected ?? false;
  }

  async saveOfflineData(
    type: OfflineData['type'],
    data: any,
    priority: 'high' | 'medium' | 'low' = 'medium'
  ): Promise<string> {
    if (!this.db) throw new Error('Database not initialized');

    const id = `${type}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const timestamp = Date.now();
    const checksum = this.integrityChecker.generateChecksum(data);
    const version = data.version || 1;

    const offlineData: OfflineData = {
      id,
      type,
      data,
      timestamp,
      synced: false,
      retryCount: 0,
      priority,
      checksum,
      lastModified: timestamp,
      version
    };

    const insertQuery = `
      INSERT INTO offline_data (
        id, type, data, timestamp, synced, retry_count,
        priority, checksum, last_modified, version
      ) VALUES (?, ?, ?, ?, 0, 0, ?, ?, ?, ?);
    `;

    await this.db.executeSql(insertQuery, [
      id, type, JSON.stringify(data), timestamp,
      priority, checksum, timestamp, version
    ]);

    // Also store in MMKV for quick access
    this.storage.set(`offline_${id}`, JSON.stringify(offlineData));

    // Add to sync queue with priority ordering
    this.addToSyncQueue(offlineData);

    console.log(`Offline data saved: ${id} with priority: ${priority}`);
    return id;
  }

  async getOfflineData(): Promise<OfflineData[]> {
    if (!this.db) throw new Error('Database not initialized');

    const query = `
      SELECT * FROM offline_data 
      WHERE synced = 0 
      ORDER BY timestamp ASC;
    `;

    const [results] = await this.db.executeSql(query);
    const offlineData: OfflineData[] = [];

    for (let i = 0; i < results.rows.length; i++) {
      const row = results.rows.item(i);
      offlineData.push({
        id: row.id,
        type: row.type,
        data: JSON.parse(row.data),
        timestamp: row.timestamp,
        synced: row.synced === 1,
        retryCount: row.retry_count,
      });
    }

    return offlineData;
  }

  async syncOfflineData(): Promise<SyncResult> {
    if (!this.isOnline || this.syncInProgress) {
      return {
        success: false,
        syncedCount: 0,
        failedCount: 0,
        errors: ['Sync already in progress or device is offline'],
      };
    }

    this.syncInProgress = true;
    const result: SyncResult = {
      success: true,
      syncedCount: 0,
      failedCount: 0,
      conflictCount: 0,
      errors: [],
      conflicts: [],
    };

    try {
      const offlineData = await this.getOfflineData();
      console.log(`Starting sync of ${offlineData.length} items`);

      for (const item of offlineData) {
        try {
          await this.syncSingleItem(item);
          await this.markAsSynced(item.id);
          result.syncedCount++;
        } catch (error) {
          console.error(`Failed to sync item ${item.id}:`, error);
          await this.incrementRetryCount(item.id);
          result.failedCount++;
          result.errors.push(`${item.id}: ${error.message}`);
        }
      }

      console.log(`Sync completed: ${result.syncedCount} synced, ${result.failedCount} failed`);
    } catch (error) {
      console.error('Sync process failed:', error);
      result.success = false;
      result.errors.push(`Sync process error: ${error.message}`);
    } finally {
      this.syncInProgress = false;
    }

    return result;
  }

  private async syncSingleItem(item: OfflineData): Promise<void> {
    // Perform integrity check before sync
    const integrityCheck = this.integrityChecker.validateIntegrity(item);
    if (!integrityCheck.isValid) {
      throw new Error(`Data integrity check failed: ${integrityCheck.corruptionType}`);
    }

    try {
      switch (item.type) {
        case 'registration':
          await this.syncRegistration(item);
          break;
        case 'case':
          await this.syncCase(item);
          break;
        case 'update':
          await this.syncUpdate(item);
          break;
        default:
          throw new Error(`Unknown sync type: ${item.type}`);
      }
    } catch (error) {
      // Check if it's a conflict error
      if (error.status === 409 || error.message.includes('conflict')) {
        await this.handleSyncConflict(item, error);
        throw error; // Re-throw to be handled by sync process
      }
      throw error;
    }
  }

  private async syncRegistration(item: OfflineData): Promise<void> {
    console.log('Syncing registration:', item.id);
    // TODO: Implement actual API call
    // const response = await AuthService.getApiClient().post('/registration', item.data);
    // Handle response and potential conflicts

    // Simulate API call for now
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  private async syncCase(item: OfflineData): Promise<void> {
    console.log('Syncing case:', item.id);
    // TODO: Implement actual API call
    // const response = await AuthService.getApiClient().post('/grievance/cases', item.data);

    // Simulate API call for now
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  private async syncUpdate(item: OfflineData): Promise<void> {
    console.log('Syncing update:', item.id);
    // TODO: Implement actual API call with conflict detection
    // const response = await AuthService.getApiClient().put(`/households/${item.data.id}`, {
    //   ...item.data,
    //   version: item.version,
    //   lastModified: item.lastModified
    // });

    // Simulate API call for now
    await new Promise(resolve => setTimeout(resolve, 100));
  }

  /**
   * Handle sync conflicts
   */
  private async handleSyncConflict(item: OfflineData, error: any): Promise<void> {
    console.log(`Handling sync conflict for item ${item.id}`);

    // Extract server data from error response
    const serverData = error.response?.data?.currentData || {};

    const conflict: ConflictData = {
      id: item.id,
      type: item.type,
      clientData: item.data,
      serverData: serverData,
      timestamp: Date.now(),
      resolution: item.conflictResolution || 'server_wins'
    };

    // Attempt automatic resolution
    const resolvedData = this.conflictResolver.resolveConflict(conflict);

    if (resolvedData) {
      // Update local data with resolved version
      const updatedItem: OfflineData = {
        ...item,
        data: resolvedData,
        checksum: this.integrityChecker.generateChecksum(resolvedData),
        lastModified: Date.now(),
        version: (resolvedData.version || item.version) + 1
      };

      await this.updateOfflineData(updatedItem);
      console.log(`Conflict resolved automatically for item ${item.id}`);
    } else {
      // Store for manual resolution
      console.log(`Conflict requires manual resolution for item ${item.id}`);
      await this.storeConflictForManualResolution(conflict);
    }
  }

  /**
   * Store conflict for manual resolution
   */
  private async storeConflictForManualResolution(conflict: ConflictData): Promise<void> {
    const conflicts = JSON.parse(this.storage.getString('manual_conflicts') || '[]');
    conflicts.push(conflict);
    this.storage.set('manual_conflicts', JSON.stringify(conflicts));
  }

  private async markAsSynced(id: string): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const updateQuery = `
      UPDATE offline_data 
      SET synced = 1 
      WHERE id = ?;
    `;

    await this.db.executeSql(updateQuery, [id]);
    this.storage.delete(`offline_${id}`);
  }

  private async incrementRetryCount(id: string): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const updateQuery = `
      UPDATE offline_data 
      SET retry_count = retry_count + 1 
      WHERE id = ?;
    `;

    await this.db.executeSql(updateQuery, [id]);
  }

  async clearSyncedData(): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const deleteQuery = `
      DELETE FROM offline_data 
      WHERE synced = 1;
    `;

    await this.db.executeSql(deleteQuery);
    console.log('Cleared synced offline data');
  }

  async getStorageInfo(): Promise<{
    totalItems: number;
    pendingSync: number;
    storageSize: string;
  }> {
    if (!this.db) throw new Error('Database not initialized');

    const totalQuery = 'SELECT COUNT(*) as count FROM offline_data;';
    const pendingQuery = 'SELECT COUNT(*) as count FROM offline_data WHERE synced = 0;';

    const [totalResults] = await this.db.executeSql(totalQuery);
    const [pendingResults] = await this.db.executeSql(pendingQuery);

    const totalItems = totalResults.rows.item(0).count;
    const pendingSync = pendingResults.rows.item(0).count;

    return {
      totalItems,
      pendingSync,
      storageSize: `${(this.storage.size / 1024).toFixed(2)} KB`,
    };
  }

  isDeviceOnline(): boolean {
    return this.isOnline;
  }

  isSyncInProgress(): boolean {
    return this.syncInProgress;
  }

  private async loadOfflineData(): Promise<void> {
    // Load any cached data from MMKV on startup
    const keys = this.storage.getAllKeys().filter(key => key.startsWith('offline_'));
    console.log(`Loaded ${keys.length} offline items from cache`);
  }
  /**
   * Add item to priority-based sync queue
   */
  private addToSyncQueue(item: OfflineData): void {
    // Remove existing item if present
    this.syncQueue = this.syncQueue.filter(existing => existing.id !== item.id);

    // Add new item and sort by priority
    this.syncQueue.push(item);
    this.syncQueue.sort((a, b) => {
      const priorityOrder = { high: 3, medium: 2, low: 1 };
      return priorityOrder[b.priority] - priorityOrder[a.priority];
    });
  }

  /**
   * Start background synchronization
   */
  private async startBackgroundSync(): Promise<void> {
    if (this.backgroundSyncTimer) {
      clearInterval(this.backgroundSyncTimer);
    }

    this.backgroundSyncTimer = setInterval(async () => {
      if (this.isOnline && !this.syncInProgress && this.syncQueue.length > 0) {
        console.log('Background sync triggered');
        await this.syncOfflineData();
      }
    }, 30000); // Every 30 seconds

    // Also setup background job for when app is backgrounded
    BackgroundJob.register({
      jobKey: 'dsr-background-sync',
      period: 60000, // Every minute
    });

    BackgroundJob.on('dsr-background-sync', async () => {
      if (this.isOnline && !this.syncInProgress) {
        await this.syncHighPriorityItems();
      }
    });
  }

  /**
   * Sync only high priority items in background
   */
  private async syncHighPriorityItems(): Promise<void> {
    const highPriorityItems = this.syncQueue.filter(item => item.priority === 'high');

    for (const item of highPriorityItems.slice(0, 5)) { // Limit to 5 items
      try {
        await this.syncSingleItem(item);
        await this.markAsSynced(item.id);
        this.syncQueue = this.syncQueue.filter(existing => existing.id !== item.id);
      } catch (error) {
        console.error(`Background sync failed for item ${item.id}:`, error);
        await this.incrementRetryCount(item.id);
      }
    }
  }

  /**
   * Perform data integrity check on all offline data
   */
  private async performIntegrityCheck(): Promise<void> {
    console.log('Performing data integrity check...');

    const offlineData = await this.getOfflineData();
    let corruptedCount = 0;
    let repairedCount = 0;

    for (const item of offlineData) {
      const integrityCheck = this.integrityChecker.validateIntegrity(item);

      if (!integrityCheck.isValid) {
        corruptedCount++;
        console.warn(`Data corruption detected for item ${item.id}: ${integrityCheck.corruptionType}`);

        // Attempt repair
        const repairedItem = this.integrityChecker.repairData(item);
        if (repairedItem) {
          await this.updateOfflineData(repairedItem);
          repairedCount++;
          console.log(`Successfully repaired item ${item.id}`);
        } else {
          console.error(`Cannot repair item ${item.id}, marking for manual review`);
          await this.markForManualReview(item.id);
        }
      }
    }

    console.log(`Integrity check completed: ${corruptedCount} corrupted, ${repairedCount} repaired`);
  }

  /**
   * Update offline data item
   */
  private async updateOfflineData(item: OfflineData): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const updateQuery = `
      UPDATE offline_data
      SET data = ?, checksum = ?, last_modified = ?, version = ?
      WHERE id = ?;
    `;

    await this.db.executeSql(updateQuery, [
      JSON.stringify(item.data),
      item.checksum,
      item.lastModified,
      item.version,
      item.id
    ]);

    this.storage.set(`offline_${item.id}`, JSON.stringify(item));
  }

  /**
   * Mark item for manual review
   */
  private async markForManualReview(itemId: string): Promise<void> {
    const reviewItems = JSON.parse(this.storage.getString('manual_review_items') || '[]');
    if (!reviewItems.includes(itemId)) {
      reviewItems.push(itemId);
      this.storage.set('manual_review_items', JSON.stringify(reviewItems));
    }
  }

  /**
   * Get items requiring manual review
   */
  async getItemsForManualReview(): Promise<OfflineData[]> {
    const reviewItemIds = JSON.parse(this.storage.getString('manual_review_items') || '[]');
    const allItems = await this.getOfflineData();
    return allItems.filter(item => reviewItemIds.includes(item.id));
  }

  /**
   * Resolve manual review item
   */
  async resolveManualReviewItem(itemId: string, action: 'repair' | 'delete' | 'ignore'): Promise<void> {
    const reviewItems = JSON.parse(this.storage.getString('manual_review_items') || '[]');
    const updatedItems = reviewItems.filter((id: string) => id !== itemId);
    this.storage.set('manual_review_items', JSON.stringify(updatedItems));

    switch (action) {
      case 'delete':
        await this.deleteOfflineData(itemId);
        break;
      case 'repair':
        // Attempt repair again or mark as resolved
        console.log(`Manual repair attempted for item ${itemId}`);
        break;
      case 'ignore':
        // Just remove from review list
        console.log(`Manual review ignored for item ${itemId}`);
        break;
    }
  }

  /**
   * Delete offline data item
   */
  private async deleteOfflineData(itemId: string): Promise<void> {
    if (!this.db) throw new Error('Database not initialized');

    const deleteQuery = 'DELETE FROM offline_data WHERE id = ?;';
    await this.db.executeSql(deleteQuery, [itemId]);
    this.storage.delete(`offline_${itemId}`);

    // Remove from sync queue
    this.syncQueue = this.syncQueue.filter(item => item.id !== itemId);
  }
}

/**
 * Advanced conflict resolution for offline data synchronization
 */
class ConflictResolver {

  resolveConflict(conflict: ConflictData): any {
    switch (conflict.resolution) {
      case 'server_wins':
        return conflict.serverData;
      case 'client_wins':
        return conflict.clientData;
      case 'merge':
        return this.mergeData(conflict.clientData, conflict.serverData);
      case 'manual':
        // Store for manual resolution
        this.storeForManualResolution(conflict);
        return null;
      default:
        // Default to server wins for safety
        return conflict.serverData;
    }
  }

  private mergeData(clientData: any, serverData: any): any {
    // Intelligent merge strategy based on data type and timestamps
    const merged = { ...serverData };

    // Merge non-conflicting fields from client
    Object.keys(clientData).forEach(key => {
      if (key !== 'lastModified' && key !== 'version') {
        // Use client data if server doesn't have the field or client is newer
        if (!serverData[key] ||
            (clientData.lastModified > serverData.lastModified &&
             this.isFieldUpdatable(key))) {
          merged[key] = clientData[key];
        }
      }
    });

    merged.lastModified = Math.max(clientData.lastModified || 0, serverData.lastModified || 0);
    merged.version = Math.max(clientData.version || 1, serverData.version || 1) + 1;

    return merged;
  }

  private isFieldUpdatable(field: string): boolean {
    // Define which fields can be updated by client
    const updatableFields = [
      'contactNumber', 'address', 'emergencyContact',
      'preferences', 'notes', 'localUpdates'
    ];
    return updatableFields.includes(field);
  }

  private storeForManualResolution(conflict: ConflictData): void {
    // Store conflict for manual resolution in UI
    const storage = new MMKV({ id: 'dsr-conflicts' });
    const conflicts = JSON.parse(storage.getString('manual_conflicts') || '[]');
    conflicts.push(conflict);
    storage.set('manual_conflicts', JSON.stringify(conflicts));
  }
}

/**
 * Data integrity checker for offline storage
 */
class DataIntegrityChecker {

  generateChecksum(data: any): string {
    const dataString = JSON.stringify(data, Object.keys(data).sort());
    return CryptoJS.SHA256(dataString).toString();
  }

  validateIntegrity(item: OfflineData): DataIntegrityCheck {
    const currentChecksum = this.generateChecksum(item.data);
    const isValid = currentChecksum === item.checksum;

    return {
      id: item.id,
      isValid,
      checksum: currentChecksum,
      expectedChecksum: item.checksum,
      corruptionType: !isValid ? this.detectCorruptionType(item, currentChecksum) : undefined
    };
  }

  private detectCorruptionType(item: OfflineData, currentChecksum: string): 'checksum_mismatch' | 'missing_data' | 'invalid_format' {
    if (!item.data) {
      return 'missing_data';
    }

    try {
      JSON.stringify(item.data);
      return 'checksum_mismatch';
    } catch {
      return 'invalid_format';
    }
  }

  repairData(item: OfflineData): OfflineData | null {
    // Attempt basic data repair
    if (!item.data) {
      return null; // Cannot repair missing data
    }

    try {
      // Ensure data is valid JSON
      const repairedData = JSON.parse(JSON.stringify(item.data));
      const newChecksum = this.generateChecksum(repairedData);

      return {
        ...item,
        data: repairedData,
        checksum: newChecksum
      };
    } catch {
      return null; // Cannot repair invalid data
    }
  }
}

export const OfflineService = new OfflineServiceClass();
