package ph.gov.dsr.interoperability.dto;

/**
 * Exception for compliance-related errors
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-28
 */
public class ComplianceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String standard;

    public ComplianceException(String message) {
        super(message);
        this.errorCode = "COMPLIANCE_ERROR";
        this.standard = null;
    }

    public ComplianceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "COMPLIANCE_ERROR";
        this.standard = null;
    }

    public ComplianceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.standard = null;
    }

    public ComplianceException(String errorCode, String standard, String message) {
        super(message);
        this.errorCode = errorCode;
        this.standard = standard;
    }

    public ComplianceException(String errorCode, String standard, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.standard = standard;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getStandard() {
        return standard;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ComplianceException{");
        sb.append("errorCode='").append(errorCode).append('\'');
        if (standard != null) {
            sb.append(", standard='").append(standard).append('\'');
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}