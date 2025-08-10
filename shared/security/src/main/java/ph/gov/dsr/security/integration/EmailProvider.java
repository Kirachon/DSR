package ph.gov.dsr.security.integration;

import java.util.concurrent.CompletableFuture;

/**
 * Email Provider interface for sending emails for MFA
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-30
 */
public interface EmailProvider {

    /**
     * Send verification email with code
     * 
     * @param emailAddress The email address to send to
     * @param verificationCode The verification code to send
     * @return true if email was sent successfully
     */
    boolean sendVerificationEmail(String emailAddress, String verificationCode);

    /**
     * Send MFA token via email
     * 
     * @param emailAddress The email address to send to
     * @param token The MFA token to send
     * @return true if email was sent successfully
     */
    boolean sendMFAToken(String emailAddress, String token);

    /**
     * Send verification email with code asynchronously
     * 
     * @param emailAddress The email address to send to
     * @param verificationCode The verification code to send
     * @return CompletableFuture with email delivery result
     */
    CompletableFuture<EmailDeliveryResult> sendVerificationEmailAsync(String emailAddress, String verificationCode);

    /**
     * Send MFA token via email asynchronously
     * 
     * @param emailAddress The email address to send to
     * @param token The MFA token to send
     * @return CompletableFuture with email delivery result
     */
    CompletableFuture<EmailDeliveryResult> sendMFATokenAsync(String emailAddress, String token);

    /**
     * Send custom email message
     * 
     * @param emailAddress The email address to send to
     * @param subject The email subject
     * @param message The email message body
     * @return true if email was sent successfully
     */
    boolean sendCustomEmail(String emailAddress, String subject, String message);

    /**
     * Send custom email message asynchronously
     * 
     * @param emailAddress The email address to send to
     * @param subject The email subject
     * @param message The email message body
     * @return CompletableFuture with email delivery result
     */
    CompletableFuture<EmailDeliveryResult> sendCustomEmailAsync(String emailAddress, String subject, String message);

    /**
     * Send HTML email message
     * 
     * @param emailAddress The email address to send to
     * @param subject The email subject
     * @param htmlContent The HTML email content
     * @return true if email was sent successfully
     */
    boolean sendHtmlEmail(String emailAddress, String subject, String htmlContent);

    /**
     * Send HTML email message asynchronously
     * 
     * @param emailAddress The email address to send to
     * @param subject The email subject
     * @param htmlContent The HTML email content
     * @return CompletableFuture with email delivery result
     */
    CompletableFuture<EmailDeliveryResult> sendHtmlEmailAsync(String emailAddress, String subject, String htmlContent);

    /**
     * Validate email address format
     * 
     * @param emailAddress The email address to validate
     * @return true if email address is valid
     */
    boolean isValidEmailAddress(String emailAddress);

    /**
     * Check if email service is available
     * 
     * @return true if email service is available
     */
    boolean isServiceAvailable();

    /**
     * Get delivery status for an email
     * 
     * @param messageId The message ID to check
     * @return Email delivery status
     */
    EmailDeliveryStatus getDeliveryStatus(String messageId);

    /**
     * Get remaining email quota
     * 
     * @return number of remaining emails, -1 if unlimited
     */
    int getRemainingQuota();

    /**
     * Email Delivery Result
     */
    record EmailDeliveryResult(
            boolean success,
            String messageId,
            String status,
            String errorCode,
            String errorMessage,
            long deliveryTimestamp
    ) {
        public static EmailDeliveryResult success(String messageId) {
            return new EmailDeliveryResult(true, messageId, "SENT", null, null, System.currentTimeMillis());
        }

        public static EmailDeliveryResult failure(String errorCode, String errorMessage) {
            return new EmailDeliveryResult(false, null, "FAILED", errorCode, errorMessage, System.currentTimeMillis());
        }
    }

    /**
     * Email Delivery Status
     */
    enum EmailDeliveryStatus {
        PENDING,
        SENT,
        DELIVERED,
        OPENED,
        CLICKED,
        BOUNCED,
        FAILED,
        SPAM,
        UNKNOWN
    }
}
