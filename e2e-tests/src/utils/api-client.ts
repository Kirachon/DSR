import axios, { AxiosInstance, AxiosResponse } from 'axios';
import * as jwt from 'jsonwebtoken';

/**
 * API Client for interacting with DSR backend services
 * Handles authentication, request/response logging, and error handling
 */
export class ApiClient {
  private client: AxiosInstance;
  private authToken?: string;
  private refreshToken?: string;

  constructor(baseURL?: string) {
    this.client = axios.create({
      baseURL: baseURL || process.env.API_BASE_URL || 'http://localhost:8080',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    });

    // Request interceptor for adding auth token
    this.client.interceptors.request.use(
      (config) => {
        if (this.authToken) {
          config.headers.Authorization = `Bearer ${this.authToken}`;
        }
        
        // Log request in test environment
        if (process.env.NODE_ENV === 'test') {
          console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
        }
        
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor for handling auth errors
    this.client.interceptors.response.use(
      (response) => {
        // Log response in test environment
        if (process.env.NODE_ENV === 'test') {
          console.log(`API Response: ${response.status} ${response.config.url}`);
        }
        return response;
      },
      async (error) => {
        const originalRequest = error.config;

        // Handle 401 errors by attempting token refresh
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          if (this.refreshToken) {
            try {
              await this.refreshAuthToken();
              originalRequest.headers.Authorization = `Bearer ${this.authToken}`;
              return this.client(originalRequest);
            } catch (refreshError) {
              // Refresh failed, clear tokens
              this.clearTokens();
              throw refreshError;
            }
          }
        }

        return Promise.reject(error);
      }
    );
  }

  /**
   * Authenticate user and store tokens
   */
  async authenticate(email: string, password: string): Promise<void> {
    try {
      const response = await this.client.post('/auth/login', {
        email,
        password,
      });

      const { accessToken, refreshToken } = response.data;
      this.setTokens(accessToken, refreshToken);
    } catch (error) {
      throw new Error(`Authentication failed: ${error}`);
    }
  }

  /**
   * Authenticate using OAuth2 flow (for testing)
   */
  async authenticateOAuth(clientId: string, clientSecret: string): Promise<void> {
    try {
      const response = await this.client.post('/oauth2/token', {
        grant_type: 'client_credentials',
        client_id: clientId,
        client_secret: clientSecret,
      });

      const { access_token, refresh_token } = response.data;
      this.setTokens(access_token, refresh_token);
    } catch (error) {
      throw new Error(`OAuth authentication failed: ${error}`);
    }
  }

  /**
   * Generate test JWT token (for testing purposes)
   */
  generateTestToken(payload: any): string {
    const secret = process.env.JWT_SECRET || 'test_secret';
    return jwt.sign(payload, secret, { expiresIn: '1h' });
  }

  /**
   * Set authentication tokens
   */
  setTokens(accessToken: string, refreshToken?: string): void {
    this.authToken = accessToken;
    this.refreshToken = refreshToken;
  }

  /**
   * Clear authentication tokens
   */
  clearTokens(): void {
    this.authToken = undefined;
    this.refreshToken = undefined;
  }

  /**
   * Refresh authentication token
   */
  private async refreshAuthToken(): Promise<void> {
    if (!this.refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await this.client.post('/auth/refresh', {
      refreshToken: this.refreshToken,
    });

    const { accessToken, refreshToken } = response.data;
    this.setTokens(accessToken, refreshToken);
  }

  /**
   * Registration Service API calls
   */
  async createRegistration(registrationData: any): Promise<AxiosResponse> {
    return this.client.post('/api/v1/registrations', registrationData);
  }

  async getRegistration(registrationId: string): Promise<AxiosResponse> {
    return this.client.get(`/api/v1/registrations/${registrationId}`);
  }

  async updateRegistration(registrationId: string, updateData: any): Promise<AxiosResponse> {
    return this.client.put(`/api/v1/registrations/${registrationId}`, updateData);
  }

  async deleteRegistration(registrationId: string): Promise<AxiosResponse> {
    return this.client.delete(`/api/v1/registrations/${registrationId}`);
  }

  /**
   * Eligibility Service API calls
   */
  async assessEligibility(assessmentData: any): Promise<AxiosResponse> {
    return this.client.post('/api/v1/eligibility/assess', assessmentData);
  }

  async getEligibilityStatus(householdId: string): Promise<AxiosResponse> {
    return this.client.get(`/api/v1/eligibility/household/${householdId}`);
  }

  /**
   * Payment Service API calls
   */
  async createPayment(paymentData: any): Promise<AxiosResponse> {
    return this.client.post('/api/v1/payments', paymentData);
  }

  async getPaymentStatus(paymentId: string): Promise<AxiosResponse> {
    return this.client.get(`/api/v1/payments/${paymentId}`);
  }

  /**
   * Grievance Service API calls
   */
  async submitGrievance(grievanceData: any): Promise<AxiosResponse> {
    return this.client.post('/api/v1/grievances', grievanceData);
  }

  async getGrievanceStatus(grievanceId: string): Promise<AxiosResponse> {
    return this.client.get(`/api/v1/grievances/${grievanceId}`);
  }

  /**
   * Analytics Service API calls
   */
  async getDashboardSummary(): Promise<AxiosResponse> {
    return this.client.get('/api/v1/analytics/dashboard/summary');
  }

  async getProgramStatistics(program: string, period: string): Promise<AxiosResponse> {
    return this.client.get(`/api/v1/analytics/programs/statistics?program=${program}&period=${period}`);
  }

  /**
   * Health check for all services
   */
  async checkServiceHealth(serviceName: string, port: number): Promise<boolean> {
    try {
      const response = await axios.get(`http://localhost:${port}/actuator/health`, {
        timeout: 5000,
      });
      return response.status === 200;
    } catch (error) {
      console.log(`Service ${serviceName} health check failed:`, error);
      return false;
    }
  }

  /**
   * Wait for service to be ready
   */
  async waitForService(serviceName: string, port: number, maxRetries: number = 30): Promise<void> {
    for (let i = 0; i < maxRetries; i++) {
      const isHealthy = await this.checkServiceHealth(serviceName, port);
      if (isHealthy) {
        console.log(`Service ${serviceName} is ready`);
        return;
      }
      
      console.log(`Waiting for service ${serviceName} to be ready... (${i + 1}/${maxRetries})`);
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
    
    throw new Error(`Service ${serviceName} failed to become ready after ${maxRetries} retries`);
  }
}
