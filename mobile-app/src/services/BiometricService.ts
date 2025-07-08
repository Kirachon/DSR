import ReactNativeBiometrics from 'react-native-biometrics';
import { Keychain } from 'react-native-keychain';
import { Alert, Platform } from 'react-native';
import DeviceInfo from 'react-native-device-info';
import CryptoJS from 'crypto-js';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { MMKV } from 'react-native-mmkv';

export interface BiometricCapabilities {
  isAvailable: boolean;
  biometryType: string | null;
  isEnrolled: boolean;
  supportedTypes: string[];
  securityLevel: 'weak' | 'strong' | 'very_strong';
}

export interface BiometricAuthResult {
  success: boolean;
  error?: string;
  signature?: string;
  deviceId?: string;
  timestamp?: number;
  authMethod?: string;
}

export interface DeviceRegistration {
  deviceId: string;
  deviceName: string;
  platform: string;
  biometricType: string;
  publicKey: string;
  registeredAt: number;
  lastUsed: number;
  isActive: boolean;
}

export interface BiometricSession {
  sessionId: string;
  deviceId: string;
  userId: string;
  createdAt: number;
  expiresAt: number;
  isActive: boolean;
}

export interface FallbackAuthOptions {
  allowPIN: boolean;
  allowPassword: boolean;
  allowPattern: boolean;
  maxAttempts: number;
}

class BiometricServiceClass {
  private rnBiometrics: ReactNativeBiometrics;
  private isInitialized: boolean = false;
  private deviceId: string = '';
  private secureStorage: MMKV;
  private sessionManager: BiometricSessionManager;
  private deviceManager: DeviceManager;
  private fallbackHandler: FallbackAuthHandler;

  constructor() {
    this.rnBiometrics = new ReactNativeBiometrics({
      allowDeviceCredentials: true,
    });
    this.secureStorage = new MMKV({
      id: 'dsr-biometric-secure',
      encryptionKey: 'dsr-biometric-encryption-key',
    });
    this.sessionManager = new BiometricSessionManager();
    this.deviceManager = new DeviceManager();
    this.fallbackHandler = new FallbackAuthHandler();
  }

  async initialize(): Promise<void> {
    try {
      // Get device ID
      this.deviceId = await DeviceInfo.getUniqueId();

      // Initialize managers
      await this.sessionManager.initialize();
      await this.deviceManager.initialize(this.deviceId);
      await this.fallbackHandler.initialize();

      // Check capabilities
      const capabilities = await this.checkCapabilities();
      console.log('Enhanced biometric capabilities:', capabilities);

      // Register device if biometrics are available
      if (capabilities.isAvailable) {
        await this.deviceManager.registerDevice(capabilities);
      }

      // Clean up expired sessions
      await this.sessionManager.cleanupExpiredSessions();

      this.isInitialized = true;
      console.log('Enhanced biometric service initialized successfully');
    } catch (error) {
      console.error('Enhanced biometric service initialization failed:', error);
      this.isInitialized = false;
    }
  }

  async checkCapabilities(): Promise<BiometricCapabilities> {
    try {
      const { available, biometryType } = await this.rnBiometrics.isSensorAvailable();

      let isEnrolled = false;
      let supportedTypes: string[] = [];
      let securityLevel: 'weak' | 'strong' | 'very_strong' = 'weak';

      if (available) {
        try {
          const { keysExist } = await this.rnBiometrics.biometricKeysExist();
          isEnrolled = keysExist;

          // Determine supported biometric types
          supportedTypes = await this.getSupportedBiometricTypes();

          // Assess security level
          securityLevel = this.assessSecurityLevel(biometryType, supportedTypes);

        } catch (error) {
          console.warn('Could not check biometric enrollment:', error);
        }
      }

      return {
        isAvailable: available,
        biometryType: biometryType || null,
        isEnrolled,
        supportedTypes,
        securityLevel,
      };
    } catch (error) {
      console.error('Failed to check biometric capabilities:', error);
      return {
        isAvailable: false,
        biometryType: null,
        isEnrolled: false,
        supportedTypes: [],
        securityLevel: 'weak',
      };
    }
  }

  async setupBiometricAuthentication(): Promise<boolean> {
    try {
      const capabilities = await this.checkCapabilities();
      
      if (!capabilities.isAvailable) {
        Alert.alert(
          'Biometric Authentication Unavailable',
          'Your device does not support biometric authentication or no biometrics are enrolled.'
        );
        return false;
      }

      if (!capabilities.isEnrolled) {
        const { success } = await this.rnBiometrics.createKeys();
        if (!success) {
          Alert.alert(
            'Setup Failed',
            'Failed to create biometric keys. Please try again.'
          );
          return false;
        }
      }

      // Test biometric authentication
      const authResult = await this.authenticateWithBiometrics(
        'Setup Biometric Authentication',
        'Please verify your identity to enable biometric login'
      );

      if (authResult.success) {
        await this.enableBiometricLogin();
        Alert.alert(
          'Success',
          'Biometric authentication has been enabled for your account.'
        );
        return true;
      } else {
        Alert.alert(
          'Setup Failed',
          authResult.error || 'Biometric authentication setup failed.'
        );
        return false;
      }
    } catch (error) {
      console.error('Biometric setup failed:', error);
      Alert.alert(
        'Setup Error',
        'An error occurred while setting up biometric authentication.'
      );
      return false;
    }
  }

  async authenticateWithBiometrics(
    title: string = 'Biometric Authentication',
    subtitle: string = 'Please verify your identity',
    options?: { allowFallback?: boolean; sessionDuration?: number }
  ): Promise<BiometricAuthResult> {
    try {
      const capabilities = await this.checkCapabilities();

      if (!capabilities.isAvailable || !capabilities.isEnrolled) {
        // Try fallback authentication if allowed
        if (options?.allowFallback) {
          return await this.fallbackHandler.authenticate(title, subtitle);
        }

        return {
          success: false,
          error: 'Biometric authentication is not available or not set up',
        };
      }

      // Check if device is registered and active
      const deviceRegistration = await this.deviceManager.getDeviceRegistration();
      if (!deviceRegistration || !deviceRegistration.isActive) {
        return {
          success: false,
          error: 'Device is not registered for biometric authentication',
        };
      }

      // Create secure payload with device and session information
      const timestamp = Date.now();
      const sessionId = await this.sessionManager.createSession(this.deviceId);
      const payload = this.createSecurePayload(sessionId, timestamp);

      const promptMessage = this.getBiometricPromptMessage(capabilities.biometryType);

      const { success, signature } = await this.rnBiometrics.createSignature({
        promptMessage: promptMessage,
        payload: payload,
      });

      if (success && signature) {
        // Validate signature and create session
        const isValidSignature = await this.validateBiometricSignature(
          signature,
          payload,
          deviceRegistration.publicKey
        );

        if (isValidSignature) {
          // Update device last used
          await this.deviceManager.updateLastUsed();

          // Create or extend session
          const sessionDuration = options?.sessionDuration || 3600000; // 1 hour default
          await this.sessionManager.extendSession(sessionId, sessionDuration);

          return {
            success: true,
            signature,
            deviceId: this.deviceId,
            timestamp,
            authMethod: capabilities.biometryType || 'biometric',
          };
        } else {
          return {
            success: false,
            error: 'Biometric signature validation failed',
          };
        }
      } else {
        // Try fallback if allowed and biometric failed
        if (options?.allowFallback) {
          return await this.fallbackHandler.authenticate(title, subtitle);
        }

        return {
          success: false,
          error: 'Biometric authentication was cancelled or failed',
        };
      }
    } catch (error) {
      console.error('Enhanced biometric authentication failed:', error);

      // Try fallback on error if allowed
      if (options?.allowFallback) {
        return await this.fallbackHandler.authenticate(title, subtitle);
      }

      return {
        success: false,
        error: error.message || 'Biometric authentication error',
      };
    }
  }

  async isBiometricLoginEnabled(): Promise<boolean> {
    try {
      const credentials = await Keychain.getInternetCredentials('dsr_biometric_enabled');
      return credentials !== false;
    } catch (error) {
      console.error('Failed to check biometric login status:', error);
      return false;
    }
  }

  async enableBiometricLogin(): Promise<void> {
    try {
      await Keychain.setInternetCredentials(
        'dsr_biometric_enabled',
        'enabled',
        'true'
      );
    } catch (error) {
      console.error('Failed to enable biometric login:', error);
      throw error;
    }
  }

  async disableBiometricLogin(): Promise<void> {
    try {
      await Keychain.resetInternetCredentials('dsr_biometric_enabled');
      await this.rnBiometrics.deleteKeys();
    } catch (error) {
      console.error('Failed to disable biometric login:', error);
      throw error;
    }
  }

  async loginWithBiometrics(): Promise<BiometricAuthResult> {
    try {
      const isEnabled = await this.isBiometricLoginEnabled();
      if (!isEnabled) {
        return {
          success: false,
          error: 'Biometric login is not enabled',
        };
      }

      const authResult = await this.authenticateWithBiometrics(
        'Login with Biometrics',
        'Use your biometric to sign in to DSR'
      );

      if (authResult.success) {
        // Here you would typically validate the signature with your backend
        // For now, we'll just return success
        return authResult;
      } else {
        return authResult;
      }
    } catch (error) {
      console.error('Biometric login failed:', error);
      return {
        success: false,
        error: error.message || 'Biometric login error',
      };
    }
  }

  private getBiometricPromptMessage(biometryType: string | null): string {
    switch (biometryType) {
      case 'TouchID':
        return 'Use Touch ID to authenticate';
      case 'FaceID':
        return 'Use Face ID to authenticate';
      case 'Biometrics':
        return 'Use your fingerprint to authenticate';
      default:
        return 'Use biometric authentication';
    }
  }

  async getBiometricType(): Promise<string | null> {
    try {
      const capabilities = await this.checkCapabilities();
      return capabilities.biometryType;
    } catch (error) {
      console.error('Failed to get biometric type:', error);
      return null;
    }
  }

  async canUseBiometrics(): Promise<boolean> {
    if (!this.isInitialized) {
      await this.initialize();
    }

    const capabilities = await this.checkCapabilities();
    const isEnabled = await this.isBiometricLoginEnabled();
    
    return capabilities.isAvailable && capabilities.isEnrolled && isEnabled;
  }

  async showBiometricSettings(): Promise<void> {
    const capabilities = await this.checkCapabilities();
    
    if (!capabilities.isAvailable) {
      Alert.alert(
        'Biometric Authentication',
        'Your device does not support biometric authentication.',
        [{ text: 'OK' }]
      );
      return;
    }

    if (!capabilities.isEnrolled) {
      Alert.alert(
        'No Biometrics Enrolled',
        'Please enroll your biometrics in device settings first.',
        [
          { text: 'Cancel', style: 'cancel' },
          { 
            text: 'Settings', 
            onPress: () => {
              // Open device settings (platform-specific implementation needed)
              console.log('Open device biometric settings');
            }
          }
        ]
      );
      return;
    }

    const isEnabled = await this.isBiometricLoginEnabled();
    
    Alert.alert(
      'Biometric Authentication',
      `Biometric login is currently ${isEnabled ? 'enabled' : 'disabled'}.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: isEnabled ? 'Disable' : 'Enable',
          onPress: async () => {
            if (isEnabled) {
              await this.disableBiometricLogin();
              Alert.alert('Success', 'Biometric login has been disabled.');
            } else {
              const success = await this.setupBiometricAuthentication();
              if (!success) {
                console.log('Biometric setup was cancelled or failed');
              }
            }
          }
        }
      ]
    );
  }
}

  /**
   * Get supported biometric types for the device
   */
  private async getSupportedBiometricTypes(): Promise<string[]> {
    const supportedTypes: string[] = [];

    try {
      const { available, biometryType } = await this.rnBiometrics.isSensorAvailable();

      if (available && biometryType) {
        supportedTypes.push(biometryType);

        // Check for additional biometric types on Android
        if (Platform.OS === 'android') {
          // Android may support multiple biometric types
          const additionalTypes = await this.checkAdditionalAndroidBiometrics();
          supportedTypes.push(...additionalTypes);
        }
      }
    } catch (error) {
      console.warn('Failed to get supported biometric types:', error);
    }

    return supportedTypes;
  }

  /**
   * Check for additional Android biometric types
   */
  private async checkAdditionalAndroidBiometrics(): Promise<string[]> {
    // This would require additional native module implementation
    // For now, return empty array
    return [];
  }

  /**
   * Assess security level based on biometric type
   */
  private assessSecurityLevel(
    biometryType: string | null,
    supportedTypes: string[]
  ): 'weak' | 'strong' | 'very_strong' {
    if (!biometryType) return 'weak';

    // Face recognition is generally considered weaker than fingerprint
    if (biometryType.toLowerCase().includes('face')) {
      return supportedTypes.length > 1 ? 'strong' : 'weak';
    }

    // Fingerprint and iris are considered strong
    if (biometryType.toLowerCase().includes('fingerprint') ||
        biometryType.toLowerCase().includes('touch') ||
        biometryType.toLowerCase().includes('iris')) {
      return supportedTypes.length > 1 ? 'very_strong' : 'strong';
    }

    return 'weak';
  }

  /**
   * Create secure payload for biometric authentication
   */
  private createSecurePayload(sessionId: string, timestamp: number): string {
    const payload = {
      sessionId,
      timestamp,
      deviceId: this.deviceId,
      nonce: Math.random().toString(36).substring(2, 15)
    };

    return JSON.stringify(payload);
  }

  /**
   * Validate biometric signature
   */
  private async validateBiometricSignature(
    signature: string,
    payload: string,
    publicKey: string
  ): Promise<boolean> {
    try {
      // In a real implementation, this would validate the signature
      // against the public key using cryptographic verification
      // For now, we'll do basic validation

      if (!signature || !payload || !publicKey) {
        return false;
      }

      // Validate payload structure
      const parsedPayload = JSON.parse(payload);
      if (!parsedPayload.sessionId || !parsedPayload.timestamp || !parsedPayload.deviceId) {
        return false;
      }

      // Check timestamp is recent (within 5 minutes)
      const now = Date.now();
      const timeDiff = now - parsedPayload.timestamp;
      if (timeDiff > 300000) { // 5 minutes
        return false;
      }

      // In production, implement proper signature verification here
      return true;
    } catch (error) {
      console.error('Signature validation failed:', error);
      return false;
    }
  }

  /**
   * Get registered devices for current user
   */
  async getRegisteredDevices(): Promise<DeviceRegistration[]> {
    return await this.deviceManager.getRegisteredDevices();
  }

  /**
   * Revoke device registration
   */
  async revokeDevice(deviceId: string): Promise<boolean> {
    return await this.deviceManager.revokeDevice(deviceId);
  }

  /**
   * Get active biometric sessions
   */
  async getActiveSessions(): Promise<BiometricSession[]> {
    return await this.sessionManager.getActiveSessions();
  }

  /**
   * Revoke biometric session
   */
  async revokeSession(sessionId: string): Promise<boolean> {
    return await this.sessionManager.revokeSession(sessionId);
  }

  /**
   * Check if current session is valid
   */
  async isSessionValid(): Promise<boolean> {
    return await this.sessionManager.isCurrentSessionValid();
  }

  /**
   * Configure fallback authentication options
   */
  async configureFallbackAuth(options: FallbackAuthOptions): Promise<void> {
    await this.fallbackHandler.configure(options);
  }

  /**
   * Get fallback authentication status
   */
  async getFallbackAuthStatus(): Promise<FallbackAuthOptions> {
    return await this.fallbackHandler.getConfiguration();
  }
}

/**
 * Biometric Session Manager
 */
class BiometricSessionManager {
  private storage: MMKV;
  private currentSessionId: string | null = null;

  constructor() {
    this.storage = new MMKV({ id: 'dsr-biometric-sessions' });
  }

  async initialize(): Promise<void> {
    // Clean up expired sessions on initialization
    await this.cleanupExpiredSessions();
  }

  async createSession(deviceId: string, userId?: string): Promise<string> {
    const sessionId = `session_${Date.now()}_${Math.random().toString(36).substring(2, 15)}`;
    const session: BiometricSession = {
      sessionId,
      deviceId,
      userId: userId || 'current_user', // In production, get from auth context
      createdAt: Date.now(),
      expiresAt: Date.now() + 3600000, // 1 hour default
      isActive: true
    };

    this.storage.set(sessionId, JSON.stringify(session));
    this.currentSessionId = sessionId;

    console.log(`Created biometric session: ${sessionId}`);
    return sessionId;
  }

  async extendSession(sessionId: string, duration: number): Promise<boolean> {
    try {
      const sessionData = this.storage.getString(sessionId);
      if (!sessionData) return false;

      const session: BiometricSession = JSON.parse(sessionData);
      session.expiresAt = Date.now() + duration;

      this.storage.set(sessionId, JSON.stringify(session));
      return true;
    } catch (error) {
      console.error('Failed to extend session:', error);
      return false;
    }
  }

  async revokeSession(sessionId: string): Promise<boolean> {
    try {
      const sessionData = this.storage.getString(sessionId);
      if (!sessionData) return false;

      const session: BiometricSession = JSON.parse(sessionData);
      session.isActive = false;

      this.storage.set(sessionId, JSON.stringify(session));

      if (this.currentSessionId === sessionId) {
        this.currentSessionId = null;
      }

      return true;
    } catch (error) {
      console.error('Failed to revoke session:', error);
      return false;
    }
  }

  async getActiveSessions(): Promise<BiometricSession[]> {
    const sessions: BiometricSession[] = [];
    const keys = this.storage.getAllKeys();

    for (const key of keys) {
      try {
        const sessionData = this.storage.getString(key);
        if (sessionData) {
          const session: BiometricSession = JSON.parse(sessionData);
          if (session.isActive && session.expiresAt > Date.now()) {
            sessions.push(session);
          }
        }
      } catch (error) {
        console.warn(`Failed to parse session ${key}:`, error);
      }
    }

    return sessions;
  }

  async isCurrentSessionValid(): Promise<boolean> {
    if (!this.currentSessionId) return false;

    try {
      const sessionData = this.storage.getString(this.currentSessionId);
      if (!sessionData) return false;

      const session: BiometricSession = JSON.parse(sessionData);
      return session.isActive && session.expiresAt > Date.now();
    } catch (error) {
      console.error('Failed to validate current session:', error);
      return false;
    }
  }

  async cleanupExpiredSessions(): Promise<void> {
    const keys = this.storage.getAllKeys();
    const now = Date.now();

    for (const key of keys) {
      try {
        const sessionData = this.storage.getString(key);
        if (sessionData) {
          const session: BiometricSession = JSON.parse(sessionData);
          if (session.expiresAt < now) {
            this.storage.delete(key);
          }
        }
      } catch (error) {
        // Delete corrupted session data
        this.storage.delete(key);
      }
    }
  }
}

/**
 * Device Manager for multi-device biometric support
 */
class DeviceManager {
  private storage: MMKV;
  private deviceId: string = '';

  constructor() {
    this.storage = new MMKV({ id: 'dsr-device-management' });
  }

  async initialize(deviceId: string): Promise<void> {
    this.deviceId = deviceId;
  }

  async registerDevice(capabilities: BiometricCapabilities): Promise<boolean> {
    try {
      const deviceName = await DeviceInfo.getDeviceName();
      const platform = Platform.OS;

      // Generate key pair for this device
      const { publicKey } = await this.generateDeviceKeyPair();

      const registration: DeviceRegistration = {
        deviceId: this.deviceId,
        deviceName,
        platform,
        biometricType: capabilities.biometryType || 'unknown',
        publicKey,
        registeredAt: Date.now(),
        lastUsed: Date.now(),
        isActive: true
      };

      this.storage.set(`device_${this.deviceId}`, JSON.stringify(registration));

      // In production, also register with backend server
      // await this.registerDeviceWithServer(registration);

      console.log(`Device registered: ${deviceName} (${this.deviceId})`);
      return true;
    } catch (error) {
      console.error('Device registration failed:', error);
      return false;
    }
  }

  async getDeviceRegistration(): Promise<DeviceRegistration | null> {
    try {
      const registrationData = this.storage.getString(`device_${this.deviceId}`);
      if (!registrationData) return null;

      return JSON.parse(registrationData);
    } catch (error) {
      console.error('Failed to get device registration:', error);
      return null;
    }
  }

  async updateLastUsed(): Promise<void> {
    try {
      const registration = await this.getDeviceRegistration();
      if (registration) {
        registration.lastUsed = Date.now();
        this.storage.set(`device_${this.deviceId}`, JSON.stringify(registration));
      }
    } catch (error) {
      console.error('Failed to update last used:', error);
    }
  }

  async getRegisteredDevices(): Promise<DeviceRegistration[]> {
    const devices: DeviceRegistration[] = [];
    const keys = this.storage.getAllKeys().filter(key => key.startsWith('device_'));

    for (const key of keys) {
      try {
        const deviceData = this.storage.getString(key);
        if (deviceData) {
          const device: DeviceRegistration = JSON.parse(deviceData);
          devices.push(device);
        }
      } catch (error) {
        console.warn(`Failed to parse device ${key}:`, error);
      }
    }

    return devices;
  }

  async revokeDevice(deviceId: string): Promise<boolean> {
    try {
      const registration = await this.getDeviceRegistration();
      if (registration && registration.deviceId === deviceId) {
        registration.isActive = false;
        this.storage.set(`device_${deviceId}`, JSON.stringify(registration));

        // In production, also revoke on backend server
        // await this.revokeDeviceOnServer(deviceId);

        return true;
      }
      return false;
    } catch (error) {
      console.error('Failed to revoke device:', error);
      return false;
    }
  }

  private async generateDeviceKeyPair(): Promise<{ publicKey: string; privateKey: string }> {
    // In a real implementation, this would generate actual cryptographic key pairs
    // For now, we'll generate mock keys
    const timestamp = Date.now();
    const random = Math.random().toString(36).substring(2, 15);

    return {
      publicKey: `pub_${timestamp}_${random}`,
      privateKey: `priv_${timestamp}_${random}`
    };
  }
}

/**
 * Fallback Authentication Handler
 */
class FallbackAuthHandler {
  private storage: MMKV;
  private configuration: FallbackAuthOptions;

  constructor() {
    this.storage = new MMKV({ id: 'dsr-fallback-auth' });
    this.configuration = {
      allowPIN: true,
      allowPassword: true,
      allowPattern: false,
      maxAttempts: 3
    };
  }

  async initialize(): Promise<void> {
    try {
      const configData = this.storage.getString('fallback_config');
      if (configData) {
        this.configuration = JSON.parse(configData);
      }
    } catch (error) {
      console.warn('Failed to load fallback configuration:', error);
    }
  }

  async configure(options: FallbackAuthOptions): Promise<void> {
    this.configuration = { ...this.configuration, ...options };
    this.storage.set('fallback_config', JSON.stringify(this.configuration));
  }

  async getConfiguration(): Promise<FallbackAuthOptions> {
    return { ...this.configuration };
  }

  async authenticate(title: string, subtitle: string): Promise<BiometricAuthResult> {
    try {
      // Check if any fallback methods are enabled
      if (!this.configuration.allowPIN &&
          !this.configuration.allowPassword &&
          !this.configuration.allowPattern) {
        return {
          success: false,
          error: 'No fallback authentication methods are enabled'
        };
      }

      // In a real implementation, this would show a fallback authentication UI
      // For now, we'll simulate the process

      const fallbackMethod = this.selectBestFallbackMethod();
      const authResult = await this.performFallbackAuth(fallbackMethod, title, subtitle);

      return {
        success: authResult.success,
        error: authResult.error,
        authMethod: fallbackMethod,
        timestamp: Date.now()
      };
    } catch (error) {
      console.error('Fallback authentication failed:', error);
      return {
        success: false,
        error: error.message || 'Fallback authentication error'
      };
    }
  }

  private selectBestFallbackMethod(): string {
    if (this.configuration.allowPassword) return 'password';
    if (this.configuration.allowPIN) return 'pin';
    if (this.configuration.allowPattern) return 'pattern';
    return 'none';
  }

  private async performFallbackAuth(
    method: string,
    title: string,
    subtitle: string
  ): Promise<{ success: boolean; error?: string }> {
    // In a real implementation, this would show appropriate UI for each method
    // For now, we'll simulate success

    console.log(`Performing fallback authentication with method: ${method}`);

    // Simulate user interaction delay
    await new Promise(resolve => setTimeout(resolve, 1000));

    // For demo purposes, return success
    return { success: true };
  }

  async recordFailedAttempt(): Promise<boolean> {
    try {
      const attempts = this.storage.getNumber('failed_attempts') || 0;
      const newAttempts = attempts + 1;

      this.storage.set('failed_attempts', newAttempts);
      this.storage.set('last_failed_attempt', Date.now());

      return newAttempts >= this.configuration.maxAttempts;
    } catch (error) {
      console.error('Failed to record failed attempt:', error);
      return false;
    }
  }

  async resetFailedAttempts(): Promise<void> {
    this.storage.delete('failed_attempts');
    this.storage.delete('last_failed_attempt');
  }

  async isLocked(): Promise<boolean> {
    try {
      const attempts = this.storage.getNumber('failed_attempts') || 0;
      const lastFailedAttempt = this.storage.getNumber('last_failed_attempt') || 0;

      if (attempts >= this.configuration.maxAttempts) {
        // Check if lockout period has expired (30 minutes)
        const lockoutDuration = 30 * 60 * 1000; // 30 minutes
        const now = Date.now();

        if (now - lastFailedAttempt > lockoutDuration) {
          await this.resetFailedAttempts();
          return false;
        }

        return true;
      }

      return false;
    } catch (error) {
      console.error('Failed to check lock status:', error);
      return false;
    }
  }
}

export const BiometricService = new BiometricServiceClass();
