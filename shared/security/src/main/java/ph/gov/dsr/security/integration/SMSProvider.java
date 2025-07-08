package ph.gov.dsr.security.integration;

import java.util.concurrent.CompletableFuture;

/**
 * SMS Provider interface for sending SMS messages for MFA
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
public interface SMSProvider {

    /**
     * Send verification SMS with code
     * 
     * @param phoneNumber The phone number to send SMS to
     * @param verificationCode The verification code to send
     * @return true if SMS was sent successfully
     */
    boolean sendVerificationSMS(String phoneNumber, String verificationCode);

    /**
     * Send MFA token via SMS
     * 
     * @param phoneNumber The phone number to send SMS to
     * @param token The MFA token to send
     * @return true if SMS was sent successfully
     */
    boolean sendMFAToken(String phoneNumber, String token);

    /**
     * Send verification SMS with code asynchronously
     * 
     * @param phoneNumber The phone number to send SMS to
     * @param verificationCode The verification code to send
     * @return CompletableFuture with SMS delivery result
     */
    CompletableFuture<SMSDeliveryResult> sendVerificationSMSAsync(String phoneNumber, String verificationCode);

    /**
     * Send MFA token via SMS asynchronously
     * 
     * @param phoneNumber The phone number to send SMS to
     * @param token The MFA token to send
     * @return CompletableFuture with SMS delivery result
     */
    CompletableFuture<SMSDeliveryResult> sendMFATokenAsync(String phoneNumber, String token);

    /**
     * Send custom SMS message
     * 
     * @param phoneNumber The phone number to send SMS to
     * @param message The message to send
     * @return true if SMS was sent successfully
     */
    boolean sendCustomMessage(String phoneNumber, String message);

    /**
     * Send custom SMS message asynchronously
     * 
     * @param phoneNumber The phone number to send SMS to
     * @param message The message to send
     * @return CompletableFuture with SMS delivery result
     */
    CompletableFuture<SMSDeliveryResult> sendCustomMessageAsync(String phoneNumber, String message);

    /**
     * Validate phone number format
     * 
     * @param phoneNumber The phone number to validate
     * @return true if phone number is valid
     */
    boolean isValidPhoneNumber(String phoneNumber);

    /**
     * Check if SMS service is available
     * 
     * @return true if SMS service is available
     */
    boolean isServiceAvailable();

    /**
     * Get delivery status for a message
     * 
     * @param messageId The message ID to check
     * @return SMS delivery status
     */
    SMSDeliveryStatus getDeliveryStatus(String messageId);

    /**
     * Get remaining SMS credits/quota
     * 
     * @return number of remaining SMS credits, -1 if unlimited
     */
    int getRemainingCredits();

    /**
     * SMS Delivery Result
     */
    record SMSDeliveryResult(
            boolean success,
            String messageId,
            String status,
            String errorCode,
            String errorMessage,
            long deliveryTimestamp
    ) {
        public static SMSDeliveryResult success(String messageId) {
            return new SMSDeliveryResult(true, messageId, "SENT", null, null, System.currentTimeMillis());
        }

        public static SMSDeliveryResult failure(String errorCode, String errorMessage) {
            return new SMSDeliveryResult(false, null, "FAILED", errorCode, errorMessage, System.currentTimeMillis());
        }
    }

    /**
     * SMS Delivery Status
     */
    enum SMSDeliveryStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
        EXPIRED,
        UNKNOWN
    }
}
