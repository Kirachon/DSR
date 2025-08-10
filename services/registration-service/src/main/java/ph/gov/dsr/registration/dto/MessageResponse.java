package ph.gov.dsr.registration.dto;

import java.time.LocalDateTime;

/**
 * Generic message response DTO
 */
public class MessageResponse {

    private String message;
    private boolean success;
    private String code;
    private LocalDateTime timestamp;

    // Constructors
    public MessageResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public MessageResponse(String message) {
        this();
        this.message = message;
        this.success = true;
    }

    public MessageResponse(String message, boolean success) {
        this();
        this.message = message;
        this.success = success;
    }

    public MessageResponse(String message, boolean success, String code) {
        this();
        this.message = message;
        this.success = success;
        this.code = code;
    }

    // Static factory methods
    public static MessageResponse success(String message) {
        return new MessageResponse(message, true);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, false);
    }

    public static MessageResponse error(String message, String code) {
        return new MessageResponse(message, false, code);
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", code='" + code + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
