package ph.gov.dsr.security.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for data encryption and decryption operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataEncryptionService {

    @Value("${dsr.security.encryption.algorithm:AES/GCM/NoPadding}")
    private String encryptionAlgorithm;

    @Value("${dsr.security.encryption.key-size:256}")
    private int keySize;

    @Value("${dsr.security.encryption.iv-size:12}")
    private int ivSize;

    @Value("${dsr.security.encryption.tag-size:16}")
    private int tagSize;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypt sensitive data
     */
    public String encrypt(String plaintext, String keyString) {
        try {
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(keyString), "AES");
            
            // Generate random IV
            byte[] iv = new byte[ivSize];
            secureRandom.nextBytes(iv);
            
            Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagSize * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and ciphertext
            byte[] encryptedData = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encryptedData);
            
        } catch (Exception e) {
            log.error("Failed to encrypt data: {}", e.getMessage(), e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt sensitive data
     */
    public String decrypt(String encryptedData, String keyString) {
        try {
            SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(keyString), "AES");
            
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV and ciphertext
            byte[] iv = new byte[ivSize];
            byte[] ciphertext = new byte[decodedData.length - ivSize];
            System.arraycopy(decodedData, 0, iv, 0, ivSize);
            System.arraycopy(decodedData, ivSize, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = Cipher.getInstance(encryptionAlgorithm);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(tagSize * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            
            return new String(plaintext, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Failed to decrypt data: {}", e.getMessage(), e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generate a new encryption key
     */
    public String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize);
            SecretKey key = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            log.error("Failed to generate encryption key: {}", e.getMessage(), e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * Hash sensitive data for comparison purposes
     */
    public String hashData(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to hash data: {}", e.getMessage(), e);
            throw new RuntimeException("Hashing failed", e);
        }
    }

    /**
     * Encrypt audit log data
     */
    public String encryptAuditData(String auditData) {
        // Use a system-wide audit encryption key
        String auditKey = getAuditEncryptionKey();
        return encrypt(auditData, auditKey);
    }

    /**
     * Decrypt audit log data
     */
    public String decryptAuditData(String encryptedAuditData) {
        // Use a system-wide audit encryption key
        String auditKey = getAuditEncryptionKey();
        return decrypt(encryptedAuditData, auditKey);
    }

    /**
     * Check if data appears to be encrypted
     */
    public boolean isEncrypted(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Try to decode as Base64
            byte[] decoded = Base64.getDecoder().decode(data);
            // Check if it has the expected minimum length (IV + some ciphertext)
            return decoded.length >= ivSize + 16; // IV + minimum ciphertext
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Securely wipe sensitive data from memory
     */
    public void secureWipe(char[] sensitiveData) {
        if (sensitiveData != null) {
            java.util.Arrays.fill(sensitiveData, '\0');
        }
    }

    /**
     * Securely wipe sensitive data from memory
     */
    public void secureWipe(byte[] sensitiveData) {
        if (sensitiveData != null) {
            java.util.Arrays.fill(sensitiveData, (byte) 0);
        }
    }

    /**
     * Get the audit encryption key (in production, this would be from a secure key store)
     */
    private String getAuditEncryptionKey() {
        // In production, this should be retrieved from a secure key management system
        // For now, return a placeholder - this should be configured securely
        return System.getProperty("dsr.audit.encryption.key", generateKey());
    }

    /**
     * Encrypt PII data with special handling
     */
    public String encryptPII(String piiData) {
        if (piiData == null || piiData.trim().isEmpty()) {
            return piiData;
        }
        
        // Add PII marker for identification
        String markedData = "PII:" + piiData;
        return encryptAuditData(markedData);
    }

    /**
     * Decrypt PII data with special handling
     */
    public String decryptPII(String encryptedPiiData) {
        if (encryptedPiiData == null || encryptedPiiData.trim().isEmpty()) {
            return encryptedPiiData;
        }
        
        String decryptedData = decryptAuditData(encryptedPiiData);
        
        // Remove PII marker if present
        if (decryptedData.startsWith("PII:")) {
            return decryptedData.substring(4);
        }
        
        return decryptedData;
    }

    /**
     * Validate encryption key format
     */
    public boolean isValidKey(String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            return keyBytes.length == (keySize / 8); // Convert bits to bytes
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
