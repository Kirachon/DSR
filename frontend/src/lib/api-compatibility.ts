/**
 * API Compatibility Layer
 * Ensures backward compatibility during design system integration
 */

import { apiClient, tokenManager } from './api-client';
import { serviceClients } from './service-clients';
import { config, serviceUrls } from './config';

// Re-export all existing API functionality to maintain compatibility
export {
  apiClient,
  tokenManager,
  serviceClients,
  config,
  serviceUrls,
} from './api-client';

export { createServiceClient } from './service-clients';

// Enhanced API client with design system integration
export class EnhancedApiClient {
  private static instance: EnhancedApiClient;
  
  private constructor() {}
  
  static getInstance(): EnhancedApiClient {
    if (!EnhancedApiClient.instance) {
      EnhancedApiClient.instance = new EnhancedApiClient();
    }
    return EnhancedApiClient.instance;
  }

  // Maintain all existing API methods
  async get<T>(url: string, config?: any): Promise<T> {
    return apiClient.get(url, config);
  }

  async post<T>(url: string, data?: any, config?: any): Promise<T> {
    return apiClient.post(url, data, config);
  }

  async put<T>(url: string, data?: any, config?: any): Promise<T> {
    return apiClient.put(url, data, config);
  }

  async patch<T>(url: string, data?: any, config?: any): Promise<T> {
    return apiClient.patch(url, data, config);
  }

  async delete<T>(url: string, config?: any): Promise<T> {
    return apiClient.delete(url, config);
  }

  // Enhanced methods with design system integration
  async getWithTheme<T>(url: string, theme?: string, config?: any): Promise<T> {
    const enhancedConfig = {
      ...config,
      headers: {
        ...config?.headers,
        'X-Theme': theme || 'citizen',
      },
    };
    return this.get<T>(url, enhancedConfig);
  }

  async postWithTheme<T>(url: string, data?: any, theme?: string, config?: any): Promise<T> {
    const enhancedConfig = {
      ...config,
      headers: {
        ...config?.headers,
        'X-Theme': theme || 'citizen',
      },
    };
    return this.post<T>(url, data, enhancedConfig);
  }

  // Service health check with theme context
  async checkServiceHealth(serviceName: string, theme?: string): Promise<boolean> {
    try {
      const serviceUrl = serviceUrls[serviceName as keyof typeof serviceUrls];
      if (!serviceUrl) {
        throw new Error(`Unknown service: ${serviceName}`);
      }

      const response = await this.getWithTheme(
        `${serviceUrl}/api/v1/health`,
        theme
      );
      return response && (response as any).status === 'UP';
    } catch (error) {
      console.error(`Health check failed for ${serviceName}:`, error);
      return false;
    }
  }

  // Enhanced authentication with theme support
  async authenticateWithTheme(credentials: any, theme: string = 'citizen') {
    const response = await this.postWithTheme(
      '/api/v1/auth/login',
      credentials,
      theme
    );
    
    // Store theme preference with authentication
    if (typeof window !== 'undefined') {
      localStorage.setItem('dsr-user-theme', theme);
    }
    
    return response;
  }
}

// Compatibility wrapper for existing code
export const enhancedApi = EnhancedApiClient.getInstance();

// Maintain existing service client exports
export const compatibilityClients = {
  registration: serviceClients.registration,
  dataManagement: serviceClients.dataManagement,
  eligibility: serviceClients.eligibility,
  interoperability: serviceClients.interoperability,
  payment: serviceClients.payment,
  grievance: serviceClients.grievance,
  analytics: serviceClients.analytics,
};

// Enhanced service health monitoring
export const serviceHealthMonitor = {
  async checkAllServices(theme?: string): Promise<Record<string, boolean>> {
    const services = Object.keys(serviceUrls);
    const healthChecks = await Promise.allSettled(
      services.map(service => 
        enhancedApi.checkServiceHealth(service, theme)
      )
    );

    return services.reduce((acc, service, index) => {
      const result = healthChecks[index];
      acc[service] = result.status === 'fulfilled' ? result.value : false;
      return acc;
    }, {} as Record<string, boolean>);
  },

  async getServiceStatus(serviceName: string, theme?: string): Promise<{
    healthy: boolean;
    responseTime: number;
    lastChecked: Date;
  }> {
    const startTime = Date.now();
    const healthy = await enhancedApi.checkServiceHealth(serviceName, theme);
    const responseTime = Date.now() - startTime;

    return {
      healthy,
      responseTime,
      lastChecked: new Date(),
    };
  },
};

// Backward compatibility exports
export const api = apiClient;
export const clients = compatibilityClients;
export const healthCheck = serviceHealthMonitor.checkAllServices;

// Enhanced error handling with design system integration
export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public theme?: string,
    public service?: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// Enhanced request interceptor with theme support
export const addThemeInterceptor = (theme: string) => {
  const requestInterceptor = apiClient.interceptors.request.use(
    (config) => {
      config.headers['X-Theme'] = theme;
      config.headers['X-Design-System'] = 'dsr-v2';
      return config;
    },
    (error) => Promise.reject(error)
  );

  return () => {
    apiClient.interceptors.request.eject(requestInterceptor);
  };
};

// Enhanced response interceptor with design system error handling
export const addDesignSystemResponseInterceptor = () => {
  const responseInterceptor = apiClient.interceptors.response.use(
    (response) => {
      // Add design system metadata to successful responses
      if (response.data && typeof response.data === 'object') {
        response.data._designSystem = {
          version: 'dsr-v2',
          theme: response.headers['x-theme'] || 'citizen',
          timestamp: new Date().toISOString(),
        };
      }
      return response;
    },
    (error) => {
      // Enhanced error handling with design system context
      const theme = error.config?.headers?.['X-Theme'] || 'citizen';
      const service = error.config?.baseURL || 'unknown';
      
      throw new ApiError(
        error.message || 'API request failed',
        error.response?.status || 500,
        theme,
        service
      );
    }
  );

  return () => {
    apiClient.interceptors.response.eject(responseInterceptor);
  };
};

// Initialize design system interceptors
let themeInterceptorCleanup: (() => void) | null = null;
let responseInterceptorCleanup: (() => void) | null = null;

export const initializeDesignSystemIntegration = (theme: string = 'citizen') => {
  // Clean up existing interceptors
  if (themeInterceptorCleanup) themeInterceptorCleanup();
  if (responseInterceptorCleanup) responseInterceptorCleanup();

  // Add new interceptors
  themeInterceptorCleanup = addThemeInterceptor(theme);
  responseInterceptorCleanup = addDesignSystemResponseInterceptor();

  console.log(`🎨 Design system API integration initialized with theme: ${theme}`);
};

export const cleanupDesignSystemIntegration = () => {
  if (themeInterceptorCleanup) themeInterceptorCleanup();
  if (responseInterceptorCleanup) responseInterceptorCleanup();
  
  themeInterceptorCleanup = null;
  responseInterceptorCleanup = null;

  console.log('🧹 Design system API integration cleaned up');
};
