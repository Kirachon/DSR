import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

import { OfflineService, OfflineData, SyncResult } from '../services/OfflineService';

interface OfflineState {
  isOffline: boolean;
  pendingSync: OfflineData[];
  syncInProgress: boolean;
  lastSyncTime: number | null;
  syncResult: SyncResult | null;
  storageInfo: {
    totalItems: number;
    pendingSync: number;
    storageSize: string;
  } | null;
}

interface OfflineActions {
  setOfflineStatus: (isOffline: boolean) => void;
  addPendingData: (type: OfflineData['type'], data: any) => Promise<string>;
  syncData: () => Promise<SyncResult>;
  clearSyncedData: () => Promise<void>;
  updateStorageInfo: () => Promise<void>;
  initializeOfflineMode: () => Promise<void>;
  getPendingDataCount: () => number;
  getLastSyncStatus: () => string;
}

type OfflineStore = OfflineState & OfflineActions;

export const useOfflineStore = create<OfflineStore>()(
  persist(
    (set, get) => ({
      // Initial state
      isOffline: false,
      pendingSync: [],
      syncInProgress: false,
      lastSyncTime: null,
      syncResult: null,
      storageInfo: null,

      // Actions
      setOfflineStatus: (isOffline: boolean) => {
        set({ isOffline });
        console.log(`Offline status changed: ${isOffline ? 'Offline' : 'Online'}`);
        
        // Auto-sync when coming back online
        if (!isOffline && get().pendingSync.length > 0) {
          setTimeout(() => {
            get().syncData();
          }, 1000); // Small delay to ensure network is stable
        }
      },

      addPendingData: async (type: OfflineData['type'], data: any): Promise<string> => {
        try {
          const id = await OfflineService.saveOfflineData(type, data);
          
          // Update local state
          const newOfflineData: OfflineData = {
            id,
            type,
            data,
            timestamp: Date.now(),
            synced: false,
            retryCount: 0,
          };
          
          set((state) => ({
            pendingSync: [...state.pendingSync, newOfflineData],
          }));
          
          // Update storage info
          await get().updateStorageInfo();
          
          console.log(`Added offline data: ${id} (${type})`);
          return id;
        } catch (error) {
          console.error('Failed to add pending data:', error);
          throw error;
        }
      },

      syncData: async (): Promise<SyncResult> => {
        const state = get();
        
        if (state.syncInProgress) {
          console.log('Sync already in progress');
          return state.syncResult || {
            success: false,
            syncedCount: 0,
            failedCount: 0,
            errors: ['Sync already in progress'],
          };
        }
        
        if (state.isOffline) {
          console.log('Cannot sync while offline');
          return {
            success: false,
            syncedCount: 0,
            failedCount: 0,
            errors: ['Device is offline'],
          };
        }
        
        set({ syncInProgress: true });
        
        try {
          console.log(`Starting sync of ${state.pendingSync.length} items`);
          const result = await OfflineService.syncOfflineData();
          
          // Update pending data list
          const updatedPendingData = await OfflineService.getOfflineData();
          
          set({
            syncInProgress: false,
            lastSyncTime: Date.now(),
            syncResult: result,
            pendingSync: updatedPendingData,
          });
          
          // Update storage info
          await get().updateStorageInfo();
          
          console.log(`Sync completed: ${result.syncedCount} synced, ${result.failedCount} failed`);
          return result;
        } catch (error) {
          console.error('Sync failed:', error);
          const errorResult: SyncResult = {
            success: false,
            syncedCount: 0,
            failedCount: state.pendingSync.length,
            errors: [error.message || 'Sync failed'],
          };
          
          set({
            syncInProgress: false,
            syncResult: errorResult,
          });
          
          return errorResult;
        }
      },

      clearSyncedData: async (): Promise<void> => {
        try {
          await OfflineService.clearSyncedData();
          
          // Update local state
          const updatedPendingData = await OfflineService.getOfflineData();
          set({ pendingSync: updatedPendingData });
          
          // Update storage info
          await get().updateStorageInfo();
          
          console.log('Cleared synced offline data');
        } catch (error) {
          console.error('Failed to clear synced data:', error);
          throw error;
        }
      },

      updateStorageInfo: async (): Promise<void> => {
        try {
          const storageInfo = await OfflineService.getStorageInfo();
          set({ storageInfo });
        } catch (error) {
          console.error('Failed to update storage info:', error);
        }
      },

      initializeOfflineMode: async (): Promise<void> => {
        try {
          console.log('Initializing offline mode...');
          
          // Load pending data from storage
          const pendingData = await OfflineService.getOfflineData();
          
          // Get initial network status
          const isOffline = !OfflineService.isDeviceOnline();
          
          // Update storage info
          const storageInfo = await OfflineService.getStorageInfo();
          
          set({
            isOffline,
            pendingSync: pendingData,
            storageInfo,
            syncInProgress: false,
          });
          
          console.log(`Offline mode initialized: ${pendingData.length} pending items, ${isOffline ? 'offline' : 'online'}`);
        } catch (error) {
          console.error('Failed to initialize offline mode:', error);
          set({
            isOffline: true,
            pendingSync: [],
            storageInfo: null,
            syncInProgress: false,
          });
        }
      },

      getPendingDataCount: (): number => {
        return get().pendingSync.length;
      },

      getLastSyncStatus: (): string => {
        const { lastSyncTime, syncResult } = get();
        
        if (!lastSyncTime) {
          return 'Never synced';
        }
        
        const timeDiff = Date.now() - lastSyncTime;
        const minutes = Math.floor(timeDiff / (1000 * 60));
        const hours = Math.floor(minutes / 60);
        const days = Math.floor(hours / 24);
        
        let timeAgo: string;
        if (days > 0) {
          timeAgo = `${days} day${days > 1 ? 's' : ''} ago`;
        } else if (hours > 0) {
          timeAgo = `${hours} hour${hours > 1 ? 's' : ''} ago`;
        } else if (minutes > 0) {
          timeAgo = `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
        } else {
          timeAgo = 'Just now';
        }
        
        const status = syncResult?.success ? 'Success' : 'Failed';
        return `${status} - ${timeAgo}`;
      },
    }),
    {
      name: 'offline-storage',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (state) => ({
        lastSyncTime: state.lastSyncTime,
        syncResult: state.syncResult,
      }),
      onRehydrateStorage: () => (state) => {
        console.log('Offline store rehydrated:', state?.lastSyncTime ? 'with sync history' : 'no sync history');
      },
    }
  )
);

// Selectors for easier access to specific state
export const useIsOffline = () => useOfflineStore((state) => state.isOffline);
export const usePendingSync = () => useOfflineStore((state) => state.pendingSync);
export const useSyncInProgress = () => useOfflineStore((state) => state.syncInProgress);
export const useStorageInfo = () => useOfflineStore((state) => state.storageInfo);
export const useLastSyncTime = () => useOfflineStore((state) => state.lastSyncTime);
export const useSyncResult = () => useOfflineStore((state) => state.syncResult);

// Action selectors
export const useOfflineActions = () => useOfflineStore((state) => ({
  setOfflineStatus: state.setOfflineStatus,
  addPendingData: state.addPendingData,
  syncData: state.syncData,
  clearSyncedData: state.clearSyncedData,
  updateStorageInfo: state.updateStorageInfo,
  initializeOfflineMode: state.initializeOfflineMode,
  getPendingDataCount: state.getPendingDataCount,
  getLastSyncStatus: state.getLastSyncStatus,
}));
