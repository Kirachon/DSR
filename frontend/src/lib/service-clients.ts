// Service-Specific API Clients
// Factory for creating API clients for different DSR services

import axios, { AxiosInstance } from 'axios';

import { tokenManager } from './api-client';
import { config, serviceUrls, tokenConfig, appConstants } from './config';

// Service client factory
export const createServiceClient = (serviceUrl: string): AxiosInstance => {
  const client = axios.create({
    baseURL: serviceUrl,
    timeout: appConstants.requestTimeout,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    },
    withCredentials: false,
  });

  // Add request interceptor for authentication
  client.interceptors.request.use(
    config => {
      const token = tokenManager.getAccessToken();
      if (token && !tokenManager.isTokenExpired(token)) {
        config.headers.Authorization = `${tokenConfig.tokenPrefix} ${token}`;
      }
      return config;
    },
    error => Promise.reject(error)
  );

  // Add response interceptor for error handling
  client.interceptors.response.use(
    response => response,
    async error => {
      // Handle 401 errors by attempting token refresh
      if (error.response?.status === 401 && !error.config._retry) {
        error.config._retry = true;

        try {
          const refreshToken = tokenManager.getRefreshToken();
          if (refreshToken && !tokenManager.isTokenExpired(refreshToken)) {
            // Use main service for token refresh
            const response = await axios.post(
              `${serviceUrls.registration}/api/v1/auth/refresh`,
              { refreshToken }
            );

            tokenManager.setAccessToken(response.data.accessToken);
            error.config.headers.Authorization = `${tokenConfig.tokenPrefix} ${response.data.accessToken}`;

            return client(error.config);
          }
        } catch (refreshError) {
          tokenManager.clearTokens();
          if (typeof window !== 'undefined') {
            window.location.href = '/auth/login';
          }
        }
      }

      return Promise.reject(error);
    }
  );

  return client;
};

// Service-specific clients
export const serviceClients = {
  registration: createServiceClient(serviceUrls.registration),
  dataManagement: createServiceClient(serviceUrls.dataManagement),
  eligibility: createServiceClient(serviceUrls.eligibility),
  interoperability: createServiceClient(serviceUrls.interoperability),
  payment: createServiceClient(serviceUrls.payment),
  grievance: createServiceClient(serviceUrls.grievance),
  analytics: createServiceClient(serviceUrls.analytics),
};

// Health check for all services
export const checkAllServicesHealth = async (): Promise<
  Record<string, boolean>
> => {
  const healthChecks = await Promise.allSettled([
    serviceClients.registration
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
    serviceClients.dataManagement
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
    serviceClients.eligibility
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
    serviceClients.interoperability
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
    serviceClients.payment
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
    serviceClients.grievance
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
    serviceClients.analytics
      .get('/api/v1/health')
      .then(() => true)
      .catch(() => false),
  ]);

  return {
    registration:
      healthChecks[0].status === 'fulfilled' ? healthChecks[0].value : false,
    dataManagement:
      healthChecks[1].status === 'fulfilled' ? healthChecks[1].value : false,
    eligibility:
      healthChecks[2].status === 'fulfilled' ? healthChecks[2].value : false,
    interoperability:
      healthChecks[3].status === 'fulfilled' ? healthChecks[3].value : false,
    payment:
      healthChecks[4].status === 'fulfilled' ? healthChecks[4].value : false,
    grievance:
      healthChecks[5].status === 'fulfilled' ? healthChecks[5].value : false,
    analytics:
      healthChecks[6].status === 'fulfilled' ? healthChecks[6].value : false,
  };
};
