package ph.gov.dsr.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for linking PhilSys account request
 */
public class LinkPhilSysRequest {

    @NotBlank(message = "PhilSys Number (PSN) is required")
    @Pattern(regexp = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$", message = "PSN must be in format XXXX-XXXX-XXXX-XXXX")
    private String psn;

    @NotBlank(message = "Verification code is required")
    private String verificationCode;

    private String biometricData;
    private String documentType;
    private String documentNumber;
    private Boolean consentGiven;

    // Constructors
    public LinkPhilSysRequest() {}

    public LinkPhilSysRequest(String psn, String verificationCode) {
        this.psn = psn;
        this.verificationCode = verificationCode;
    }

    // Getters and Setters
    public String getPsn() {
        return psn;
    }

    public void setPsn(String psn) {
        this.psn = psn;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getBiometricData() {
        return biometricData;
    }

    public void setBiometricData(String biometricData) {
        this.biometricData = biometricData;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean getConsentGiven() {
        return consentGiven;
    }

    public void setConsentGiven(Boolean consentGiven) {
        this.consentGiven = consentGiven;
    }

    @Override
    public String toString() {
        return "LinkPhilSysRequest{" +
                "psn='" + (psn != null ? psn.substring(0, 4) + "-****-****-****" : null) + '\'' +
                ", documentType='" + documentType + '\'' +
                ", consentGiven=" + consentGiven +
                '}';
    }
}
