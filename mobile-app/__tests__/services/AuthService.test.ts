import { AuthService, LoginCredentials } from '../../src/services/AuthService';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Keychain } from 'react-native-keychain';

// Mock dependencies
jest.mock('@react-native-async-storage/async-storage');
jest.mock('react-native-keychain');
jest.mock('axios');

const mockAsyncStorage = AsyncStorage as jest.Mocked<typeof AsyncStorage>;
const mockKeychain = Keychain as jest.Mocked<typeof Keychain>;

describe('AuthService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('initialize', () => {
    it('should initialize successfully with stored tokens', async () => {
      const mockTokens = {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        expiresAt: Date.now() + 3600000, // 1 hour from now
      };

      mockKeychain.getInternetCredentials.mockResolvedValue({
        username: 'tokens',
        password: JSON.stringify(mockTokens),
        service: 'dsr_auth_tokens',
        storage: 'keychain',
      });

      await AuthService.initialize();

      expect(mockKeychain.getInternetCredentials).toHaveBeenCalledWith('dsr_auth_tokens');
    });

    it('should handle initialization without stored tokens', async () => {
      mockKeychain.getInternetCredentials.mockResolvedValue(false);

      await AuthService.initialize();

      expect(mockKeychain.getInternetCredentials).toHaveBeenCalledWith('dsr_auth_tokens');
    });

    it('should handle initialization errors gracefully', async () => {
      mockKeychain.getInternetCredentials.mockRejectedValue(new Error('Keychain error'));

      await expect(AuthService.initialize()).resolves.not.toThrow();
    });
  });

  describe('login', () => {
    const mockCredentials: LoginCredentials = {
      username: 'testuser',
      password: 'testpass123',
    };

    const mockLoginResponse = {
      data: {
        user: {
          id: '1',
          username: 'testuser',
          email: 'test@example.com',
          role: 'CITIZEN',
          firstName: 'Test',
          lastName: 'User',
        },
        tokens: {
          accessToken: 'mock-access-token',
          refreshToken: 'mock-refresh-token',
          expiresAt: Date.now() + 3600000,
        },
      },
    };

    it('should login successfully with valid credentials', async () => {
      const mockApiClient = {
        post: jest.fn().mockResolvedValue(mockLoginResponse),
        defaults: { headers: { common: {} } },
      };

      // Mock the API client
      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);

      mockKeychain.setInternetCredentials.mockResolvedValue(true);
      mockAsyncStorage.setItem.mockResolvedValue();

      const result = await AuthService.login(mockCredentials);

      expect(mockApiClient.post).toHaveBeenCalledWith('/auth/login', mockCredentials);
      expect(mockKeychain.setInternetCredentials).toHaveBeenCalled();
      expect(mockAsyncStorage.setItem).toHaveBeenCalled();
      expect(result).toEqual(mockLoginResponse.data);
    });

    it('should throw error for invalid credentials', async () => {
      const mockApiClient = {
        post: jest.fn().mockRejectedValue(new Error('Unauthorized')),
      };

      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);

      await expect(AuthService.login(mockCredentials)).rejects.toThrow('Login failed');
    });

    it('should handle network errors during login', async () => {
      const mockApiClient = {
        post: jest.fn().mockRejectedValue(new Error('Network Error')),
      };

      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);

      await expect(AuthService.login(mockCredentials)).rejects.toThrow('Login failed');
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      const mockTokens = {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        expiresAt: Date.now() + 3600000,
      };

      const mockApiClient = {
        post: jest.fn().mockResolvedValue({}),
        defaults: { headers: { common: {} } },
      };

      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);
      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(mockTokens);

      mockKeychain.resetInternetCredentials.mockResolvedValue(true);
      mockAsyncStorage.removeItem.mockResolvedValue();

      await AuthService.logout();

      expect(mockApiClient.post).toHaveBeenCalledWith('/auth/logout', {
        refreshToken: mockTokens.refreshToken,
      });
      expect(mockKeychain.resetInternetCredentials).toHaveBeenCalledWith('dsr_auth_tokens');
      expect(mockAsyncStorage.removeItem).toHaveBeenCalledWith('dsr_user');
    });

    it('should handle logout API failure gracefully', async () => {
      const mockApiClient = {
        post: jest.fn().mockRejectedValue(new Error('Server Error')),
        defaults: { headers: { common: {} } },
      };

      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);
      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(null);

      mockKeychain.resetInternetCredentials.mockResolvedValue(true);
      mockAsyncStorage.removeItem.mockResolvedValue();

      await expect(AuthService.logout()).resolves.not.toThrow();
    });
  });

  describe('refreshAccessToken', () => {
    it('should refresh token successfully', async () => {
      const mockTokens = {
        accessToken: 'old-access-token',
        refreshToken: 'mock-refresh-token',
        expiresAt: Date.now() + 3600000,
      };

      const mockNewTokens = {
        accessToken: 'new-access-token',
        refreshToken: 'new-refresh-token',
        expiresAt: Date.now() + 3600000,
      };

      const mockApiClient = {
        post: jest.fn().mockResolvedValue({ data: mockNewTokens }),
        defaults: { headers: { common: {} } },
      };

      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);
      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(mockTokens);

      mockKeychain.setInternetCredentials.mockResolvedValue(true);

      const result = await AuthService.refreshAccessToken();

      expect(mockApiClient.post).toHaveBeenCalledWith('/auth/refresh', {
        refreshToken: mockTokens.refreshToken,
      });
      expect(result).toBe(mockNewTokens.accessToken);
    });

    it('should throw error when no refresh token available', async () => {
      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(null);

      await expect(AuthService.refreshAccessToken()).rejects.toThrow('No refresh token available');
    });

    it('should handle refresh token expiry', async () => {
      const mockTokens = {
        accessToken: 'old-access-token',
        refreshToken: 'expired-refresh-token',
        expiresAt: Date.now() + 3600000,
      };

      const mockApiClient = {
        post: jest.fn().mockRejectedValue(new Error('Refresh token expired')),
      };

      jest.spyOn(AuthService, 'getApiClient').mockReturnValue(mockApiClient as any);
      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(mockTokens);

      await expect(AuthService.refreshAccessToken()).rejects.toThrow();
    });
  });

  describe('getStoredTokens', () => {
    it('should return stored tokens when available', async () => {
      const mockTokens = {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        expiresAt: Date.now() + 3600000,
      };

      mockKeychain.getInternetCredentials.mockResolvedValue({
        username: 'tokens',
        password: JSON.stringify(mockTokens),
        service: 'dsr_auth_tokens',
        storage: 'keychain',
      });

      const result = await AuthService.getStoredTokens();

      expect(result).toEqual(mockTokens);
    });

    it('should return null when no tokens stored', async () => {
      mockKeychain.getInternetCredentials.mockResolvedValue(false);

      const result = await AuthService.getStoredTokens();

      expect(result).toBeNull();
    });

    it('should handle keychain errors gracefully', async () => {
      mockKeychain.getInternetCredentials.mockRejectedValue(new Error('Keychain error'));

      const result = await AuthService.getStoredTokens();

      expect(result).toBeNull();
    });
  });

  describe('getStoredUser', () => {
    it('should return stored user when available', async () => {
      const mockUser = {
        id: '1',
        username: 'testuser',
        email: 'test@example.com',
        role: 'CITIZEN',
        firstName: 'Test',
        lastName: 'User',
      };

      mockAsyncStorage.getItem.mockResolvedValue(JSON.stringify(mockUser));

      const result = await AuthService.getStoredUser();

      expect(result).toEqual(mockUser);
    });

    it('should return null when no user stored', async () => {
      mockAsyncStorage.getItem.mockResolvedValue(null);

      const result = await AuthService.getStoredUser();

      expect(result).toBeNull();
    });

    it('should handle storage errors gracefully', async () => {
      mockAsyncStorage.getItem.mockRejectedValue(new Error('Storage error'));

      const result = await AuthService.getStoredUser();

      expect(result).toBeNull();
    });
  });

  describe('isTokenValid', () => {
    it('should return true for valid token', async () => {
      const mockTokens = {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        expiresAt: Date.now() + 3600000, // 1 hour from now
      };

      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(mockTokens);

      const result = await AuthService.isTokenValid();

      expect(result).toBe(true);
    });

    it('should return false for expired token', async () => {
      const mockTokens = {
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        expiresAt: Date.now() - 3600000, // 1 hour ago
      };

      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(mockTokens);

      const result = await AuthService.isTokenValid();

      expect(result).toBe(false);
    });

    it('should return false when no tokens available', async () => {
      jest.spyOn(AuthService, 'getStoredTokens').mockResolvedValue(null);

      const result = await AuthService.isTokenValid();

      expect(result).toBe(false);
    });
  });
});
