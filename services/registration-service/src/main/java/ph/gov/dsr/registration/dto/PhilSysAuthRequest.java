package ph.gov.dsr.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for PhilSys authentication request
 */
public class PhilSysAuthRequest {

    @NotBlank(message = "PhilSys Number (PSN) is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$", message = "PSN must be in format XXXX-XXXX-XXXX-XXXX")
    private String psn;

    @NotBlank(message = "QR code data is required")
    private String qrCodeData;

    private String biometricData;
    private String deviceId;
    private String location;

    // Constructors
    public PhilSysAuthRequest() {}

    public PhilSysAuthRequest(String psn, String qrCodeData) {
        this.psn = psn;
        this.qrCodeData = qrCodeData;
    }

    // Getters and Setters
    public String getPsn() {
        return psn;
    }

    public void setPsn(String psn) {
        this.psn = psn;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getBiometricData() {
        return biometricData;
    }

    public void setBiometricData(String biometricData) {
        this.biometricData = biometricData;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "PhilSysAuthRequest{" +
                "psn='" + (psn != null ? psn.substring(0, 4) + "-****-****-****" : null) + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
