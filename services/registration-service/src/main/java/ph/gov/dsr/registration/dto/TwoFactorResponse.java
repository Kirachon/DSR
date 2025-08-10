package ph.gov.dsr.registration.dto;

/**
 * DTO for two-factor authentication response
 */
public class TwoFactorResponse {

    private Boolean enabled;
    private String qrCodeUrl;
    private String secretKey;
    private String[] backupCodes;
    private String message;
    private Boolean verified;

    // Constructors
    public TwoFactorResponse() {}

    public TwoFactorResponse(Boolean enabled, String message) {
        this.enabled = enabled;
        this.message = message;
    }

    // Getters and Setters
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String[] getBackupCodes() {
        return backupCodes;
    }

    public void setBackupCodes(String[] backupCodes) {
        this.backupCodes = backupCodes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    @Override
    public String toString() {
        return "TwoFactorResponse{" +
                "enabled=" + enabled +
                ", message='" + message + '\'' +
                ", verified=" + verified +
                '}';
    }
}
