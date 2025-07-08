import AsyncStorage from '@react-native-async-storage/async-storage';
import { Keychain } from 'react-native-keychain';
import axios, { AxiosInstance } from 'axios';
import { API_BASE_URL } from '../config/constants';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
}

export interface User {
  id: string;
  username: string;
  email: string;
  role: string;
  firstName: string;
  lastName: string;
  psn?: string;
}

export interface LoginResponse {
  user: User;
  tokens: AuthTokens;
}

class AuthServiceClass {
  private apiClient: AxiosInstance;
  private refreshTokenPromise: Promise<string> | null = null;

  constructor() {
    this.apiClient = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  async initialize(): Promise<void> {
    try {
      const tokens = await this.getStoredTokens();
      if (tokens) {
        this.setAuthHeader(tokens.accessToken);
      }
    } catch (error) {
      console.error('Auth service initialization failed:', error);
    }
  }

  private setupInterceptors(): void {
    // Request interceptor
    this.apiClient.interceptors.request.use(
      (config) => {
        console.log(`API Request: ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        console.error('Request interceptor error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor for token refresh
    this.apiClient.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const newAccessToken = await this.refreshAccessToken();
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return this.apiClient(originalRequest);
          } catch (refreshError) {
            await this.logout();
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(error);
      }
    );
  }

  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    try {
      const response = await this.apiClient.post('/auth/login', credentials);
      const { user, tokens } = response.data;

      await this.storeTokens(tokens);
      await this.storeUser(user);
      this.setAuthHeader(tokens.accessToken);

      return { user, tokens };
    } catch (error) {
      console.error('Login failed:', error);
      throw new Error('Login failed. Please check your credentials.');
    }
  }

  async logout(): Promise<void> {
    try {
      const tokens = await this.getStoredTokens();
      if (tokens) {
        await this.apiClient.post('/auth/logout', {
          refreshToken: tokens.refreshToken,
        });
      }
    } catch (error) {
      console.error('Logout API call failed:', error);
    } finally {
      await this.clearStoredData();
      this.removeAuthHeader();
    }
  }

  async refreshAccessToken(): Promise<string> {
    if (this.refreshTokenPromise) {
      return this.refreshTokenPromise;
    }

    this.refreshTokenPromise = this.performTokenRefresh();
    
    try {
      const newAccessToken = await this.refreshTokenPromise;
      return newAccessToken;
    } finally {
      this.refreshTokenPromise = null;
    }
  }

  private async performTokenRefresh(): Promise<string> {
    const tokens = await this.getStoredTokens();
    if (!tokens) {
      throw new Error('No refresh token available');
    }

    const response = await this.apiClient.post('/auth/refresh', {
      refreshToken: tokens.refreshToken,
    });

    const newTokens = response.data;
    await this.storeTokens(newTokens);
    this.setAuthHeader(newTokens.accessToken);

    return newTokens.accessToken;
  }

  async getStoredTokens(): Promise<AuthTokens | null> {
    try {
      const credentials = await Keychain.getInternetCredentials('dsr_auth_tokens');
      if (credentials) {
        return JSON.parse(credentials.password);
      }
      return null;
    } catch (error) {
      console.error('Failed to get stored tokens:', error);
      return null;
    }
  }

  async getStoredUser(): Promise<User | null> {
    try {
      const userJson = await AsyncStorage.getItem('dsr_user');
      return userJson ? JSON.parse(userJson) : null;
    } catch (error) {
      console.error('Failed to get stored user:', error);
      return null;
    }
  }

  private async storeTokens(tokens: AuthTokens): Promise<void> {
    try {
      await Keychain.setInternetCredentials(
        'dsr_auth_tokens',
        'tokens',
        JSON.stringify(tokens)
      );
    } catch (error) {
      console.error('Failed to store tokens:', error);
      throw error;
    }
  }

  private async storeUser(user: User): Promise<void> {
    try {
      await AsyncStorage.setItem('dsr_user', JSON.stringify(user));
    } catch (error) {
      console.error('Failed to store user:', error);
      throw error;
    }
  }

  private async clearStoredData(): Promise<void> {
    try {
      await Keychain.resetInternetCredentials('dsr_auth_tokens');
      await AsyncStorage.removeItem('dsr_user');
    } catch (error) {
      console.error('Failed to clear stored data:', error);
    }
  }

  private setAuthHeader(token: string): void {
    this.apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  private removeAuthHeader(): void {
    delete this.apiClient.defaults.headers.common['Authorization'];
  }

  getApiClient(): AxiosInstance {
    return this.apiClient;
  }

  async isTokenValid(): Promise<boolean> {
    const tokens = await this.getStoredTokens();
    if (!tokens) return false;

    const now = Date.now();
    return now < tokens.expiresAt;
  }
}

export const AuthService = new AuthServiceClass();
