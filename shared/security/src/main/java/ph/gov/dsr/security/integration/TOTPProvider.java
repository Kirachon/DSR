package ph.gov.dsr.security.integration;

import java.util.List;

/**
 * TOTP (Time-based One-Time Password) Provider interface for TOTP operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
public interface TOTPProvider {

    /**
     * Generate a new TOTP secret for a user
     * 
     * @return Base32-encoded TOTP secret
     */
    String generateSecret();

    /**
     * Generate a new TOTP secret with custom length
     * 
     * @param secretLength The length of the secret in bytes
     * @return Base32-encoded TOTP secret
     */
    String generateSecret(int secretLength);

    /**
     * Generate QR code URL for TOTP setup
     * 
     * @param secret The TOTP secret
     * @param accountName The account name (usually email or username)
     * @param issuer The issuer name (application name)
     * @return QR code URL for authenticator apps
     */
    String generateQRCodeUrl(String secret, String accountName, String issuer);

    /**
     * Generate QR code image data
     * 
     * @param secret The TOTP secret
     * @param accountName The account name
     * @param issuer The issuer name
     * @param width QR code width in pixels
     * @param height QR code height in pixels
     * @return Base64-encoded QR code image data
     */
    String generateQRCodeImage(String secret, String accountName, String issuer, int width, int height);

    /**
     * Generate manual entry key for users who can't scan QR codes
     * 
     * @param secret The TOTP secret
     * @return Formatted secret for manual entry
     */
    String generateManualEntryKey(String secret);

    /**
     * Verify TOTP token
     * 
     * @param secret The user's TOTP secret
     * @param token The token to verify
     * @return true if token is valid
     */
    boolean verifyToken(String secret, String token);

    /**
     * Verify TOTP token with time window tolerance
     * 
     * @param secret The user's TOTP secret
     * @param token The token to verify
     * @param timeWindow Number of time windows to allow (default is 1)
     * @return true if token is valid within the time window
     */
    boolean verifyToken(String secret, String token, int timeWindow);

    /**
     * Verify TOTP token at specific timestamp
     * 
     * @param secret The user's TOTP secret
     * @param token The token to verify
     * @param timestamp The timestamp to verify against
     * @return true if token is valid for the given timestamp
     */
    boolean verifyTokenAtTimestamp(String secret, String token, long timestamp);

    /**
     * Generate current TOTP token for testing purposes
     * 
     * @param secret The TOTP secret
     * @return Current TOTP token
     */
    String generateCurrentToken(String secret);

    /**
     * Generate TOTP token for specific timestamp
     * 
     * @param secret The TOTP secret
     * @param timestamp The timestamp to generate token for
     * @return TOTP token for the given timestamp
     */
    String generateTokenAtTimestamp(String secret, long timestamp);

    /**
     * Generate backup codes for TOTP
     * 
     * @param count Number of backup codes to generate
     * @return List of backup codes
     */
    List<String> generateBackupCodes(int count);

    /**
     * Generate backup codes with custom length
     * 
     * @param count Number of backup codes to generate
     * @param codeLength Length of each backup code
     * @return List of backup codes
     */
    List<String> generateBackupCodes(int count, int codeLength);

    /**
     * Verify backup code
     * 
     * @param backupCodes List of valid backup codes
     * @param code The code to verify
     * @return true if backup code is valid
     */
    boolean verifyBackupCode(List<String> backupCodes, String code);

    /**
     * Hash backup codes for secure storage
     * 
     * @param backupCodes List of backup codes to hash
     * @return List of hashed backup codes
     */
    List<String> hashBackupCodes(List<String> backupCodes);

    /**
     * Verify hashed backup code
     * 
     * @param hashedBackupCodes List of hashed backup codes
     * @param code The code to verify
     * @return true if backup code is valid
     */
    boolean verifyHashedBackupCode(List<String> hashedBackupCodes, String code);

    /**
     * Get current time step for TOTP calculation
     * 
     * @return Current time step
     */
    long getCurrentTimeStep();

    /**
     * Get time step for specific timestamp
     * 
     * @param timestamp The timestamp
     * @return Time step for the given timestamp
     */
    long getTimeStepForTimestamp(long timestamp);

    /**
     * Get remaining seconds in current time window
     * 
     * @return Seconds remaining in current time window
     */
    int getRemainingSecondsInWindow();

    /**
     * Validate TOTP secret format
     * 
     * @param secret The secret to validate
     * @return true if secret is valid Base32 format
     */
    boolean isValidSecret(String secret);

    /**
     * Get TOTP algorithm configuration
     * 
     * @return TOTP configuration details
     */
    TOTPConfig getConfig();

    /**
     * TOTP Configuration
     */
    record TOTPConfig(
            String algorithm,      // HMAC algorithm (e.g., "HmacSHA1")
            int digits,           // Number of digits in token (usually 6)
            int period,           // Time period in seconds (usually 30)
            int secretLength,     // Secret length in bytes (usually 20)
            int backupCodeLength, // Backup code length (usually 8)
            int defaultTimeWindow // Default time window tolerance (usually 1)
    ) {
        public static TOTPConfig defaultConfig() {
            return new TOTPConfig("HmacSHA1", 6, 30, 20, 8, 1);
        }
    }
}
