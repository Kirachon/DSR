'use client';

import { useEffect, useCallback } from 'react';
import { useTheme } from '@/contexts/theme-context';
import { useAuth } from '@/contexts/auth-context';
import {
  initializeDesignSystemIntegration,
  cleanupDesignSystemIntegration,
  serviceHealthMonitor,
  enhancedApi,
} from '@/lib/api-compatibility';

/**
 * Hook for managing API integration with design system
 * Ensures backward compatibility while adding theme support
 */
export const useApiIntegration = () => {
  const { theme } = useTheme();
  const { isAuthenticated, user } = useAuth();

  // Initialize design system integration when theme changes
  useEffect(() => {
    if (theme) {
      initializeDesignSystemIntegration(theme);
    }

    return () => {
      cleanupDesignSystemIntegration();
    };
  }, [theme]);

  // Service health monitoring
  const checkServiceHealth = useCallback(
    async (serviceName: string) => {
      return await enhancedApi.checkServiceHealth(serviceName, theme);
    },
    [theme]
  );

  const checkAllServicesHealth = useCallback(async () => {
    return await serviceHealthMonitor.checkAllServices(theme);
  }, [theme]);

  // Enhanced API methods with theme context
  const apiGet = useCallback(
    async <T>(url: string, config?: any): Promise<T> => {
      return await enhancedApi.getWithTheme<T>(url, theme, config);
    },
    [theme]
  );

  const apiPost = useCallback(
    async <T>(url: string, data?: any, config?: any): Promise<T> => {
      return await enhancedApi.postWithTheme<T>(url, data, theme, config);
    },
    [theme]
  );

  const apiPut = useCallback(
    async <T>(url: string, data?: any, config?: any): Promise<T> => {
      return await enhancedApi.put<T>(url, data, {
        ...config,
        headers: {
          ...config?.headers,
          'X-Theme': theme,
        },
      });
    },
    [theme]
  );

  const apiPatch = useCallback(
    async <T>(url: string, data?: any, config?: any): Promise<T> => {
      return await enhancedApi.patch<T>(url, data, {
        ...config,
        headers: {
          ...config?.headers,
          'X-Theme': theme,
        },
      });
    },
    [theme]
  );

  const apiDelete = useCallback(
    async <T>(url: string, config?: any): Promise<T> => {
      return await enhancedApi.delete<T>(url, {
        ...config,
        headers: {
          ...config?.headers,
          'X-Theme': theme,
        },
      });
    },
    [theme]
  );

  // Authentication with theme support
  const authenticateWithTheme = useCallback(
    async (credentials: any) => {
      return await enhancedApi.authenticateWithTheme(credentials, theme);
    },
    [theme]
  );

  return {
    // Theme-aware API methods
    apiGet,
    apiPost,
    apiPut,
    apiPatch,
    apiDelete,
    authenticateWithTheme,

    // Service health monitoring
    checkServiceHealth,
    checkAllServicesHealth,

    // Current context
    theme,
    isAuthenticated,
    user,

    // Utility methods
    getCurrentTheme: () => theme,
    isServiceHealthy: checkServiceHealth,
  };
};

/**
 * Hook for backward compatibility with existing API usage
 * Provides the same interface as before but with enhanced functionality
 */
export const useCompatibleApi = () => {
  const { theme } = useTheme();
  
  return {
    // Maintain existing API interface
    get: enhancedApi.get.bind(enhancedApi),
    post: enhancedApi.post.bind(enhancedApi),
    put: enhancedApi.put.bind(enhancedApi),
    patch: enhancedApi.patch.bind(enhancedApi),
    delete: enhancedApi.delete.bind(enhancedApi),

    // Enhanced methods (optional to use)
    getWithTheme: (url: string, config?: any) => 
      enhancedApi.getWithTheme(url, theme, config),
    postWithTheme: (url: string, data?: any, config?: any) => 
      enhancedApi.postWithTheme(url, data, theme, config),

    // Current theme context
    currentTheme: theme,
  };
};

/**
 * Hook for service-specific API integration
 * Maintains existing service client patterns
 */
export const useServiceApi = (serviceName: string) => {
  const { theme } = useTheme();
  const { checkServiceHealth } = useApiIntegration();

  const isHealthy = useCallback(
    () => checkServiceHealth(serviceName),
    [serviceName, checkServiceHealth]
  );

  const getServiceUrl = useCallback(() => {
    const serviceUrls = {
      registration: process.env.NEXT_PUBLIC_REGISTRATION_SERVICE_URL || 'http://localhost:8080',
      dataManagement: process.env.NEXT_PUBLIC_DATA_MANAGEMENT_SERVICE_URL || 'http://localhost:8082',
      eligibility: process.env.NEXT_PUBLIC_ELIGIBILITY_SERVICE_URL || 'http://localhost:8083',
      interoperability: process.env.NEXT_PUBLIC_INTEROPERABILITY_SERVICE_URL || 'http://localhost:8084',
      payment: process.env.NEXT_PUBLIC_PAYMENT_SERVICE_URL || 'http://localhost:8085',
      grievance: process.env.NEXT_PUBLIC_GRIEVANCE_SERVICE_URL || 'http://localhost:8086',
      analytics: process.env.NEXT_PUBLIC_ANALYTICS_SERVICE_URL || 'http://localhost:8087',
    };

    return serviceUrls[serviceName as keyof typeof serviceUrls];
  }, [serviceName]);

  return {
    serviceName,
    theme,
    isHealthy,
    getServiceUrl,
    checkHealth: () => checkServiceHealth(serviceName),
  };
};

export default useApiIntegration;
