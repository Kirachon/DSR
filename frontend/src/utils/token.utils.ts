// JWT Token Utilities
// Enhanced token management and validation utilities

import { tokenConfig } from '@/lib/config';

// Token payload interface
export interface TokenPayload {
  sub: string; // Subject (user ID)
  email: string;
  role: string;
  iat: number; // Issued at
  exp: number; // Expiration time
  jti?: string; // JWT ID
  aud?: string; // Audience
  iss?: string; // Issuer
}

// Token validation result
export interface TokenValidationResult {
  isValid: boolean;
  isExpired: boolean;
  payload: TokenPayload | null;
  error?: string;
}

// Token storage interface
export interface TokenStorage {
  getAccessToken(): string | null;
  setAccessToken(token: string): void;
  getRefreshToken(): string | null;
  setRefreshToken(token: string): void;
  clearTokens(): void;
  isAvailable(): boolean;
}

// Local Storage implementation
class LocalStorageTokenStorage implements TokenStorage {
  getAccessToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(tokenConfig.accessTokenKey);
  }

  setAccessToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(tokenConfig.accessTokenKey, token);
  }

  getRefreshToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem(tokenConfig.refreshTokenKey);
  }

  setRefreshToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem(tokenConfig.refreshTokenKey, token);
  }

  clearTokens(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem(tokenConfig.accessTokenKey);
    localStorage.removeItem(tokenConfig.refreshTokenKey);
  }

  isAvailable(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }
}

// Memory Storage implementation (for access tokens)
class MemoryTokenStorage implements TokenStorage {
  private accessToken: string | null = null;
  private refreshToken: string | null = null;

  getAccessToken(): string | null {
    return this.accessToken;
  }

  setAccessToken(token: string): void {
    this.accessToken = token;
  }

  getRefreshToken(): string | null {
    return this.refreshToken;
  }

  setRefreshToken(token: string): void {
    this.refreshToken = token;
  }

  clearTokens(): void {
    this.accessToken = null;
    this.refreshToken = null;
  }

  isAvailable(): boolean {
    return true;
  }
}

// Cookie Storage implementation (for refresh tokens)
class CookieTokenStorage implements TokenStorage {
  getAccessToken(): string | null {
    return this.getCookie(tokenConfig.accessTokenKey);
  }

  setAccessToken(token: string): void {
    this.setCookie(tokenConfig.accessTokenKey, token, {
      httpOnly: false, // Access tokens need to be accessible by JS
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 24 * 60 * 60, // 24 hours
    });
  }

  getRefreshToken(): string | null {
    return this.getCookie(tokenConfig.refreshTokenKey);
  }

  setRefreshToken(token: string): void {
    this.setCookie(tokenConfig.refreshTokenKey, token, {
      httpOnly: true, // Refresh tokens should be httpOnly for security
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 7 * 24 * 60 * 60, // 7 days
    });
  }

  clearTokens(): void {
    this.deleteCookie(tokenConfig.accessTokenKey);
    this.deleteCookie(tokenConfig.refreshTokenKey);
  }

  isAvailable(): boolean {
    return typeof document !== 'undefined';
  }

  private getCookie(name: string): string | null {
    if (typeof document === 'undefined') return null;
    
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    
    if (parts.length === 2) {
      return parts.pop()?.split(';').shift() || null;
    }
    
    return null;
  }

  private setCookie(name: string, value: string, options: {
    httpOnly?: boolean;
    secure?: boolean;
    sameSite?: 'strict' | 'lax' | 'none';
    maxAge?: number;
    path?: string;
  }): void {
    if (typeof document === 'undefined') return;

    let cookieString = `${name}=${value}`;
    
    if (options.maxAge) {
      cookieString += `; Max-Age=${options.maxAge}`;
    }
    
    if (options.path) {
      cookieString += `; Path=${options.path}`;
    } else {
      cookieString += '; Path=/';
    }
    
    if (options.secure) {
      cookieString += '; Secure';
    }
    
    if (options.sameSite) {
      cookieString += `; SameSite=${options.sameSite}`;
    }

    // Note: httpOnly cannot be set from client-side JavaScript
    // This would need to be handled server-side
    
    document.cookie = cookieString;
  }

  private deleteCookie(name: string): void {
    if (typeof document === 'undefined') return;
    document.cookie = `${name}=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;`;
  }
}

// Token utility functions
export class TokenUtils {
  private static accessTokenStorage: TokenStorage = new MemoryTokenStorage();
  private static refreshTokenStorage: TokenStorage = new LocalStorageTokenStorage();

  /**
   * Parse JWT token payload
   */
  static parseToken(token: string): TokenPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Invalid token format');
      }

      const payload = parts[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded) as TokenPayload;
    } catch (error) {
      console.error('Failed to parse token:', error);
      return null;
    }
  }

  /**
   * Validate JWT token
   */
  static validateToken(token: string): TokenValidationResult {
    if (!token) {
      return {
        isValid: false,
        isExpired: false,
        payload: null,
        error: 'Token is empty',
      };
    }

    const payload = this.parseToken(token);
    if (!payload) {
      return {
        isValid: false,
        isExpired: false,
        payload: null,
        error: 'Invalid token format',
      };
    }

    const currentTime = Math.floor(Date.now() / 1000);
    const isExpired = payload.exp < currentTime;

    return {
      isValid: !isExpired,
      isExpired,
      payload,
      error: isExpired ? 'Token is expired' : undefined,
    };
  }

  /**
   * Check if token is expired
   */
  static isTokenExpired(token: string): boolean {
    const validation = this.validateToken(token);
    return validation.isExpired;
  }

  /**
   * Check if token should be refreshed (within buffer time)
   */
  static shouldRefreshToken(token: string): boolean {
    const payload = this.parseToken(token);
    if (!payload) return true;

    const currentTime = Math.floor(Date.now() / 1000);
    const bufferTime = tokenConfig.expirationBuffer / 1000;
    
    return payload.exp < (currentTime + bufferTime);
  }

  /**
   * Get time until token expiration
   */
  static getTimeUntilExpiration(token: string): number {
    const payload = this.parseToken(token);
    if (!payload) return 0;

    const currentTime = Math.floor(Date.now() / 1000);
    return Math.max(0, payload.exp - currentTime);
  }

  /**
   * Format token expiration time
   */
  static formatExpirationTime(token: string): string {
    const timeUntilExpiration = this.getTimeUntilExpiration(token);
    
    if (timeUntilExpiration <= 0) {
      return 'Expired';
    }

    const hours = Math.floor(timeUntilExpiration / 3600);
    const minutes = Math.floor((timeUntilExpiration % 3600) / 60);
    
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    } else {
      return `${minutes}m`;
    }
  }

  /**
   * Get access token
   */
  static getAccessToken(): string | null {
    return this.accessTokenStorage.getAccessToken();
  }

  /**
   * Set access token
   */
  static setAccessToken(token: string): void {
    this.accessTokenStorage.setAccessToken(token);
  }

  /**
   * Get refresh token
   */
  static getRefreshToken(): string | null {
    return this.refreshTokenStorage.getRefreshToken();
  }

  /**
   * Set refresh token
   */
  static setRefreshToken(token: string): void {
    this.refreshTokenStorage.setRefreshToken(token);
  }

  /**
   * Clear all tokens
   */
  static clearTokens(): void {
    this.accessTokenStorage.clearTokens();
    this.refreshTokenStorage.clearTokens();
  }

  /**
   * Get authorization header value
   */
  static getAuthorizationHeader(): string | null {
    const token = this.getAccessToken();
    return token ? `${tokenConfig.tokenPrefix} ${token}` : null;
  }

  /**
   * Check if user is authenticated
   */
  static isAuthenticated(): boolean {
    const token = this.getAccessToken();
    return token !== null && !this.isTokenExpired(token);
  }
}

// Export storage implementations
export { LocalStorageTokenStorage, MemoryTokenStorage, CookieTokenStorage };

// Export default token utils
export default TokenUtils;
