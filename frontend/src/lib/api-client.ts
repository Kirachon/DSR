// API Client Configuration for DSR Frontend
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

import type { ApiErrorResponse, ApiResponse } from '@/types';

import { config, tokenConfig, appConstants } from './config';

// Create Axios instance with default configuration
const createApiClient = (): AxiosInstance => {
  const client = axios.create({
    baseURL: config.apiBaseUrl,
    timeout: appConstants.requestTimeout,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    },
    withCredentials: false,
  });

  return client;
};

// Create the main API client instance
export const apiClient = createApiClient();

// Token management utilities
export const tokenManager = {
  getAccessToken: (): string | null => {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(tokenConfig.accessTokenKey);
  },

  setAccessToken: (token: string): void => {
    if (typeof window === 'undefined') return;
    localStorage.setItem(tokenConfig.accessTokenKey, token);
  },

  getRefreshToken: (): string | null => {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(tokenConfig.refreshTokenKey);
  },

  setRefreshToken: (token: string): void => {
    if (typeof window === 'undefined') return;
    localStorage.setItem(tokenConfig.refreshTokenKey, token);
  },

  clearTokens: (): void => {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(tokenConfig.accessTokenKey);
    localStorage.removeItem(tokenConfig.refreshTokenKey);
  },

  isTokenExpired: (token: string): boolean => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp < currentTime;
    } catch {
      return true;
    }
  },

  shouldRefreshToken: (token: string): boolean => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      const bufferTime = tokenConfig.expirationBuffer / 1000;
      return payload.exp < currentTime + bufferTime;
    } catch {
      return true;
    }
  },
};

// Request interceptor to add authentication token
apiClient.interceptors.request.use(
  config => {
    const token = tokenManager.getAccessToken();
    if (token && !tokenManager.isTokenExpired(token)) {
      config.headers.Authorization = `${tokenConfig.tokenPrefix} ${token}`;
    }

    // Add request ID for tracking
    config.headers['X-Request-ID'] = generateRequestId();

    // Log request in development
    if (process.env.NODE_ENV === 'development') {
      console.log('API Request:', {
        method: config.method?.toUpperCase(),
        url: config.url,
        headers: config.headers,
        data: config.data,
      });
    }

    return config;
  },
  error => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor for error handling and token refresh
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // Log response in development
    if (config.enableDebug) {
      console.log('API Response:', {
        status: response.status,
        url: response.config.url,
        data: response.data,
      });
    }

    return response;
  },
  async error => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized - attempt token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = tokenManager.getRefreshToken();
        if (refreshToken && !tokenManager.isTokenExpired(refreshToken)) {
          const response = await refreshAccessToken(refreshToken);
          tokenManager.setAccessToken(response.accessToken);

          // Retry original request with new token
          originalRequest.headers.Authorization = `${tokenConfig.tokenPrefix} ${response.accessToken}`;
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        tokenManager.clearTokens();
        if (typeof window !== 'undefined') {
          window.location.href = '/auth/login';
        }
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors
    const apiError: ApiErrorResponse = {
      message:
        error.response?.data?.message ||
        error.message ||
        'An unexpected error occurred',
      status: error.response?.status || 500,
      error: error.response?.data?.error || 'Internal Server Error',
      timestamp: new Date().toISOString(),
      path: error.config?.url || '',
      errors: error.response?.data?.errors,
      details: error.response?.data?.details,
    };

    // Log error in development
    if (config.enableDebug) {
      console.error('API Error:', apiError);
    }

    return Promise.reject(apiError);
  }
);

// Utility function to refresh access token
const refreshAccessToken = async (
  refreshToken: string
): Promise<{ accessToken: string; refreshToken: string }> => {
  const response = await axios.post(
    `${config.apiBaseUrl}/api/v1/auth/refresh`,
    { refreshToken },
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  return response.data;
};

// Generate unique request ID
const generateRequestId = (): string => {
  return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
};

// Generic API request wrapper with error handling
export const apiRequest = async <T = any>(
  config: AxiosRequestConfig
): Promise<T> => {
  try {
    const response = await apiClient(config);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// Convenience methods for different HTTP verbs
export const api = {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    apiRequest<T>({ method: 'GET', url, ...config }),

  post: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> => apiRequest<T>({ method: 'POST', url, data, ...config }),

  put: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> => apiRequest<T>({ method: 'PUT', url, data, ...config }),

  patch: <T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> => apiRequest<T>({ method: 'PATCH', url, data, ...config }),

  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<T> =>
    apiRequest<T>({ method: 'DELETE', url, ...config }),
};

// Health check utility
export const healthCheck = async (): Promise<boolean> => {
  try {
    await api.get('/api/v1/health');
    return true;
  } catch {
    return false;
  }
};

// Check all services health
export const checkAllServicesHealth = async (): Promise<Array<{port: number, healthy: boolean, responseTime?: number}>> => {
  const services = [
    { port: 8080, url: 'http://localhost:8080/api/v1/health' },
    { port: 8081, url: 'http://localhost:8081/actuator/health' },
    { port: 8082, url: 'http://localhost:8082/actuator/health' },
    { port: 8083, url: 'http://localhost:8083/actuator/health' },
    { port: 8084, url: 'http://localhost:8084/actuator/health' },
    { port: 8085, url: 'http://localhost:8085/actuator/health' },
    { port: 8086, url: 'http://localhost:8086/actuator/health' },
  ];

  const results = [];

  for (const service of services) {
    const startTime = Date.now();
    try {
      const response = await fetch(service.url, {
        method: 'GET',
        timeout: 5000
      } as any);
      const responseTime = Date.now() - startTime;
      results.push({
        port: service.port,
        healthy: response.ok,
        responseTime
      });
    } catch {
      const responseTime = Date.now() - startTime;
      results.push({
        port: service.port,
        healthy: false,
        responseTime
      });
    }
  }

  return results;
};

export default apiClient;
