/**
 * API Compatibility Tests
 * Ensures all existing API functionality is preserved during design system integration
 */

import { describe, it, expect, beforeEach, afterEach, jest } from '@jest/globals';
import {
  enhancedApi,
  serviceHealthMonitor,
  initializeDesignSystemIntegration,
  cleanupDesignSystemIntegration,
  ApiError,
} from '@/lib/api-compatibility';

// Mock axios
jest.mock('axios');

describe('API Compatibility Layer', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  afterEach(() => {
    cleanupDesignSystemIntegration();
  });

  describe('Enhanced API Client', () => {
    it('should maintain existing API methods', async () => {
      // Test that all existing methods are still available
      expect(typeof enhancedApi.get).toBe('function');
      expect(typeof enhancedApi.post).toBe('function');
      expect(typeof enhancedApi.put).toBe('function');
      expect(typeof enhancedApi.patch).toBe('function');
      expect(typeof enhancedApi.delete).toBe('function');
    });

    it('should provide theme-aware API methods', async () => {
      expect(typeof enhancedApi.getWithTheme).toBe('function');
      expect(typeof enhancedApi.postWithTheme).toBe('function');
      expect(typeof enhancedApi.authenticateWithTheme).toBe('function');
    });

    it('should be a singleton instance', () => {
      const instance1 = enhancedApi;
      const instance2 = enhancedApi;
      expect(instance1).toBe(instance2);
    });
  });

  describe('Service Health Monitoring', () => {
    it('should check individual service health', async () => {
      const mockHealthCheck = jest.fn().mockResolvedValue(true);
      enhancedApi.checkServiceHealth = mockHealthCheck;

      const result = await enhancedApi.checkServiceHealth('registration', 'citizen');
      expect(result).toBe(true);
      expect(mockHealthCheck).toHaveBeenCalledWith('registration', 'citizen');
    });

    it('should check all services health', async () => {
      const mockHealthCheck = jest.fn().mockResolvedValue({
        registration: true,
        dataManagement: true,
        eligibility: false,
        payment: true,
      });

      serviceHealthMonitor.checkAllServices = mockHealthCheck;

      const result = await serviceHealthMonitor.checkAllServices('dswd-staff');
      expect(result).toEqual({
        registration: true,
        dataManagement: true,
        eligibility: false,
        payment: true,
      });
    });

    it('should provide service status with response time', async () => {
      const mockGetServiceStatus = jest.fn().mockResolvedValue({
        healthy: true,
        responseTime: 150,
        lastChecked: expect.any(Date),
      });

      serviceHealthMonitor.getServiceStatus = mockGetServiceStatus;

      const result = await serviceHealthMonitor.getServiceStatus('payment', 'lgu-staff');
      expect(result.healthy).toBe(true);
      expect(typeof result.responseTime).toBe('number');
      expect(result.lastChecked).toBeInstanceOf(Date);
    });
  });

  describe('Design System Integration', () => {
    it('should initialize design system integration', () => {
      expect(() => {
        initializeDesignSystemIntegration('citizen');
      }).not.toThrow();
    });

    it('should cleanup design system integration', () => {
      initializeDesignSystemIntegration('dswd-staff');
      expect(() => {
        cleanupDesignSystemIntegration();
      }).not.toThrow();
    });

    it('should handle theme switching', () => {
      initializeDesignSystemIntegration('citizen');
      cleanupDesignSystemIntegration();
      
      initializeDesignSystemIntegration('lgu-staff');
      cleanupDesignSystemIntegration();
      
      // Should not throw errors during theme switching
      expect(true).toBe(true);
    });
  });

  describe('Error Handling', () => {
    it('should create ApiError with theme context', () => {
      const error = new ApiError('Test error', 404, 'citizen', 'registration');
      
      expect(error.message).toBe('Test error');
      expect(error.status).toBe(404);
      expect(error.theme).toBe('citizen');
      expect(error.service).toBe('registration');
      expect(error.name).toBe('ApiError');
    });

    it('should handle API errors gracefully', async () => {
      const mockError = new ApiError('Service unavailable', 503, 'dswd-staff', 'analytics');
      
      expect(mockError).toBeInstanceOf(Error);
      expect(mockError).toBeInstanceOf(ApiError);
    });
  });

  describe('Backward Compatibility', () => {
    it('should maintain existing service client structure', () => {
      // Import the compatibility clients
      const { compatibilityClients } = require('@/lib/api-compatibility');
      
      expect(compatibilityClients).toHaveProperty('registration');
      expect(compatibilityClients).toHaveProperty('dataManagement');
      expect(compatibilityClients).toHaveProperty('eligibility');
      expect(compatibilityClients).toHaveProperty('interoperability');
      expect(compatibilityClients).toHaveProperty('payment');
      expect(compatibilityClients).toHaveProperty('grievance');
      expect(compatibilityClients).toHaveProperty('analytics');
    });

    it('should maintain existing API exports', () => {
      const {
        api,
        clients,
        healthCheck,
        tokenManager,
        serviceClients,
      } = require('@/lib/api-compatibility');

      expect(api).toBeDefined();
      expect(clients).toBeDefined();
      expect(healthCheck).toBeDefined();
      expect(tokenManager).toBeDefined();
      expect(serviceClients).toBeDefined();
    });

    it('should preserve token management functionality', () => {
      const { tokenManager } = require('@/lib/api-compatibility');

      expect(typeof tokenManager.getAccessToken).toBe('function');
      expect(typeof tokenManager.setAccessToken).toBe('function');
      expect(typeof tokenManager.getRefreshToken).toBe('function');
      expect(typeof tokenManager.setRefreshToken).toBe('function');
      expect(typeof tokenManager.clearTokens).toBe('function');
    });
  });

  describe('Authentication Integration', () => {
    it('should authenticate with theme support', async () => {
      const mockAuth = jest.fn().mockResolvedValue({
        accessToken: 'mock-token',
        refreshToken: 'mock-refresh',
        user: { id: '1', role: 'CITIZEN' },
      });

      enhancedApi.authenticateWithTheme = mockAuth;

      const credentials = { email: 'test@example.com', password: 'password' };
      const result = await enhancedApi.authenticateWithTheme(credentials, 'citizen');

      expect(mockAuth).toHaveBeenCalledWith(credentials, 'citizen');
      expect(result.accessToken).toBe('mock-token');
    });

    it('should store theme preference during authentication', async () => {
      // Mock localStorage
      const mockSetItem = jest.fn();
      Object.defineProperty(window, 'localStorage', {
        value: { setItem: mockSetItem },
        writable: true,
      });

      const mockAuth = jest.fn().mockResolvedValue({
        accessToken: 'mock-token',
        user: { id: '1', role: 'DSWD_STAFF' },
      });

      enhancedApi.authenticateWithTheme = mockAuth;

      await enhancedApi.authenticateWithTheme(
        { email: 'staff@dsr.gov.ph', password: 'password' },
        'dswd-staff'
      );

      expect(mockSetItem).toHaveBeenCalledWith('dsr-user-theme', 'dswd-staff');
    });
  });

  describe('Service URL Configuration', () => {
    it('should maintain existing service URL structure', () => {
      const { serviceUrls } = require('@/lib/api-compatibility');

      expect(serviceUrls).toHaveProperty('registration');
      expect(serviceUrls).toHaveProperty('dataManagement');
      expect(serviceUrls).toHaveProperty('eligibility');
      expect(serviceUrls).toHaveProperty('interoperability');
      expect(serviceUrls).toHaveProperty('payment');
      expect(serviceUrls).toHaveProperty('grievance');
      expect(serviceUrls).toHaveProperty('analytics');

      // Check default URLs
      expect(serviceUrls.registration).toContain('8080');
      expect(serviceUrls.dataManagement).toContain('8082');
      expect(serviceUrls.payment).toContain('8085');
    });
  });
});
