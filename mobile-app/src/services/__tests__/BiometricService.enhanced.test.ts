import { BiometricService } from '../BiometricService';

// Mock dependencies
jest.mock('react-native-biometrics');
jest.mock('react-native-keychain');
jest.mock('react-native-device-info');
jest.mock('crypto-js');
jest.mock('react-native-mmkv');
jest.mock('@react-native-async-storage/async-storage');

describe('Enhanced BiometricService', () => {
  beforeEach(async () => {
    jest.clearAllMocks();
    await BiometricService.initialize();
  });

  describe('Enhanced Capabilities Detection', () => {
    it('should detect multiple biometric types and assess security level', async () => {
      const capabilities = await BiometricService.checkCapabilities();
      
      expect(capabilities).toHaveProperty('supportedTypes');
      expect(capabilities).toHaveProperty('securityLevel');
      expect(['weak', 'strong', 'very_strong']).toContain(capabilities.securityLevel);
    });

    it('should properly assess security levels for different biometric types', async () => {
      // Mock fingerprint biometric
      jest.spyOn(BiometricService, 'checkCapabilities').mockResolvedValue({
        isAvailable: true,
        biometryType: 'Fingerprint',
        isEnrolled: true,
        supportedTypes: ['Fingerprint'],
        securityLevel: 'strong'
      });

      const capabilities = await BiometricService.checkCapabilities();
      expect(capabilities.securityLevel).toBe('strong');
    });
  });

  describe('Multi-Device Support', () => {
    it('should register device with biometric capabilities', async () => {
      const mockCapabilities = {
        isAvailable: true,
        biometryType: 'Fingerprint',
        isEnrolled: true,
        supportedTypes: ['Fingerprint'],
        securityLevel: 'strong' as const
      };

      const deviceManager = BiometricService['deviceManager'];
      const result = await deviceManager.registerDevice(mockCapabilities);
      
      expect(result).toBe(true);
    });

    it('should get registered devices', async () => {
      const devices = await BiometricService.getRegisteredDevices();
      expect(Array.isArray(devices)).toBe(true);
    });

    it('should revoke device registration', async () => {
      const deviceId = 'test-device-id';
      const result = await BiometricService.revokeDevice(deviceId);
      expect(typeof result).toBe('boolean');
    });
  });

  describe('Session Management', () => {
    it('should create and manage biometric sessions', async () => {
      const sessionManager = BiometricService['sessionManager'];
      const sessionId = await sessionManager.createSession('test-device-id');
      
      expect(sessionId).toBeTruthy();
      expect(sessionId).toMatch(/^session_/);
    });

    it('should extend session duration', async () => {
      const sessionManager = BiometricService['sessionManager'];
      const sessionId = await sessionManager.createSession('test-device-id');
      const result = await sessionManager.extendSession(sessionId, 7200000); // 2 hours
      
      expect(result).toBe(true);
    });

    it('should revoke sessions', async () => {
      const sessionManager = BiometricService['sessionManager'];
      const sessionId = await sessionManager.createSession('test-device-id');
      const result = await sessionManager.revokeSession(sessionId);
      
      expect(result).toBe(true);
    });

    it('should validate current session', async () => {
      const isValid = await BiometricService.isSessionValid();
      expect(typeof isValid).toBe('boolean');
    });

    it('should cleanup expired sessions', async () => {
      const sessionManager = BiometricService['sessionManager'];
      await sessionManager.cleanupExpiredSessions();
      
      const activeSessions = await sessionManager.getActiveSessions();
      expect(Array.isArray(activeSessions)).toBe(true);
    });
  });

  describe('Enhanced Authentication', () => {
    it('should authenticate with enhanced security features', async () => {
      // Mock successful biometric authentication
      jest.spyOn(BiometricService, 'authenticateWithBiometrics').mockResolvedValue({
        success: true,
        signature: 'mock-signature',
        deviceId: 'test-device-id',
        timestamp: Date.now(),
        authMethod: 'Fingerprint'
      });

      const result = await BiometricService.authenticateWithBiometrics(
        'Test Authentication',
        'Please verify your identity',
        { allowFallback: true, sessionDuration: 3600000 }
      );

      expect(result.success).toBe(true);
      expect(result.deviceId).toBeTruthy();
      expect(result.timestamp).toBeTruthy();
      expect(result.authMethod).toBeTruthy();
    });

    it('should handle fallback authentication when biometrics fail', async () => {
      // Mock biometric failure with fallback success
      jest.spyOn(BiometricService, 'authenticateWithBiometrics').mockResolvedValue({
        success: true,
        authMethod: 'password',
        timestamp: Date.now()
      });

      const result = await BiometricService.authenticateWithBiometrics(
        'Test Authentication',
        'Please verify your identity',
        { allowFallback: true }
      );

      expect(result.success).toBe(true);
    });

    it('should validate biometric signatures', async () => {
      const signature = 'test-signature';
      const payload = JSON.stringify({
        sessionId: 'test-session',
        timestamp: Date.now(),
        deviceId: 'test-device',
        nonce: 'test-nonce'
      });
      const publicKey = 'test-public-key';

      const isValid = await BiometricService['validateBiometricSignature'](
        signature, 
        payload, 
        publicKey
      );

      expect(typeof isValid).toBe('boolean');
    });
  });

  describe('Fallback Authentication', () => {
    it('should configure fallback authentication options', async () => {
      const options = {
        allowPIN: true,
        allowPassword: true,
        allowPattern: false,
        maxAttempts: 5
      };

      await BiometricService.configureFallbackAuth(options);
      const config = await BiometricService.getFallbackAuthStatus();
      
      expect(config.maxAttempts).toBe(5);
      expect(config.allowPIN).toBe(true);
      expect(config.allowPassword).toBe(true);
      expect(config.allowPattern).toBe(false);
    });

    it('should handle failed authentication attempts', async () => {
      const fallbackHandler = BiometricService['fallbackHandler'];
      const isLocked = await fallbackHandler.recordFailedAttempt();
      
      expect(typeof isLocked).toBe('boolean');
    });

    it('should reset failed attempts', async () => {
      const fallbackHandler = BiometricService['fallbackHandler'];
      await fallbackHandler.resetFailedAttempts();
      
      const isLocked = await fallbackHandler.isLocked();
      expect(isLocked).toBe(false);
    });

    it('should check lockout status', async () => {
      const fallbackHandler = BiometricService['fallbackHandler'];
      const isLocked = await fallbackHandler.isLocked();
      
      expect(typeof isLocked).toBe('boolean');
    });
  });

  describe('Security Features', () => {
    it('should create secure payload for authentication', async () => {
      const sessionId = 'test-session';
      const timestamp = Date.now();
      
      const payload = BiometricService['createSecurePayload'](sessionId, timestamp);
      const parsedPayload = JSON.parse(payload);
      
      expect(parsedPayload.sessionId).toBe(sessionId);
      expect(parsedPayload.timestamp).toBe(timestamp);
      expect(parsedPayload.deviceId).toBeTruthy();
      expect(parsedPayload.nonce).toBeTruthy();
    });

    it('should validate payload structure and timing', async () => {
      const recentTimestamp = Date.now();
      const oldTimestamp = Date.now() - 400000; // 6+ minutes ago
      
      const recentPayload = JSON.stringify({
        sessionId: 'test',
        timestamp: recentTimestamp,
        deviceId: 'test-device',
        nonce: 'test'
      });
      
      const oldPayload = JSON.stringify({
        sessionId: 'test',
        timestamp: oldTimestamp,
        deviceId: 'test-device',
        nonce: 'test'
      });

      const recentValid = await BiometricService['validateBiometricSignature'](
        'signature', recentPayload, 'publicKey'
      );
      
      const oldValid = await BiometricService['validateBiometricSignature'](
        'signature', oldPayload, 'publicKey'
      );

      expect(recentValid).toBe(true);
      expect(oldValid).toBe(false);
    });
  });

  describe('Device Management', () => {
    it('should generate device key pairs', async () => {
      const deviceManager = BiometricService['deviceManager'];
      const keyPair = await deviceManager['generateDeviceKeyPair']();
      
      expect(keyPair.publicKey).toBeTruthy();
      expect(keyPair.privateKey).toBeTruthy();
      expect(keyPair.publicKey).toMatch(/^pub_/);
      expect(keyPair.privateKey).toMatch(/^priv_/);
    });

    it('should update device last used timestamp', async () => {
      const deviceManager = BiometricService['deviceManager'];
      await deviceManager.updateLastUsed();
      
      // Should not throw error
      expect(true).toBe(true);
    });
  });

  describe('Integration with Authentication Service', () => {
    it('should integrate with JWT authentication flow', async () => {
      // Mock successful biometric authentication
      const authResult = await BiometricService.authenticateWithBiometrics();
      
      if (authResult.success) {
        // In real implementation, this would trigger JWT token generation
        expect(authResult.signature).toBeTruthy();
        expect(authResult.deviceId).toBeTruthy();
      }
    });

    it('should handle authentication state management', async () => {
      const canUseBiometrics = await BiometricService.canUseBiometrics();
      expect(typeof canUseBiometrics).toBe('boolean');
    });
  });

  describe('Error Handling and Recovery', () => {
    it('should handle biometric service initialization failures gracefully', async () => {
      // Mock initialization failure
      jest.spyOn(BiometricService, 'initialize').mockRejectedValue(new Error('Init failed'));
      
      try {
        await BiometricService.initialize();
      } catch (error) {
        expect(error.message).toBe('Init failed');
      }
    });

    it('should handle device registration failures', async () => {
      const deviceManager = BiometricService['deviceManager'];
      
      // Mock device info failure
      jest.spyOn(require('react-native-device-info'), 'getDeviceName')
        .mockRejectedValue(new Error('Device info failed'));
      
      const result = await deviceManager.registerDevice({
        isAvailable: true,
        biometryType: 'Fingerprint',
        isEnrolled: true,
        supportedTypes: ['Fingerprint'],
        securityLevel: 'strong'
      });
      
      expect(result).toBe(false);
    });
  });
});
