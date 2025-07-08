package ph.gov.dsr.grievance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for communication requests across different channels
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Data
public class CommunicationRequest {

    @NotBlank(message = "Channel is required")
    private String channel; // EMAIL, SMS, PHONE, IN_PERSON, WEB_PORTAL, MOBILE_APP, POSTAL

    @NotBlank(message = "Direction is required")
    private String direction; // INBOUND, OUTBOUND

    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Performed by is required")
    private String performedBy;

    private String performedByRole;

    private String recipient;

    private LocalDateTime scheduledTime;

    private List<String> attachments;

    private Map<String, Object> metadata;

    private String priority; // NORMAL, HIGH, URGENT

    private Boolean requiresResponse = false;

    private LocalDateTime responseDueDate;

    private String templateId;

    private Map<String, String> templateVariables;

    private String language = "EN";

    private Boolean isAutomated = false;

    private String externalReference;

    private String correlationId;

    // Constructors
    public CommunicationRequest() {}

    public CommunicationRequest(String channel, String direction, String content, String performedBy) {
        this.channel = channel;
        this.direction = direction;
        this.content = content;
        this.performedBy = performedBy;
    }

    // Helper methods
    
    /**
     * Check if communication is inbound
     */
    public boolean isInbound() {
        return "INBOUND".equalsIgnoreCase(direction);
    }

    /**
     * Check if communication is outbound
     */
    public boolean isOutbound() {
        return "OUTBOUND".equalsIgnoreCase(direction);
    }

    /**
     * Check if communication is urgent
     */
    public boolean isUrgent() {
        return "URGENT".equalsIgnoreCase(priority) || "HIGH".equalsIgnoreCase(priority);
    }

    /**
     * Check if communication has attachments
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    /**
     * Check if communication is scheduled
     */
    public boolean isScheduled() {
        return scheduledTime != null && scheduledTime.isAfter(LocalDateTime.now());
    }

    /**
     * Check if communication uses template
     */
    public boolean usesTemplate() {
        return templateId != null && !templateId.trim().isEmpty();
    }

    /**
     * Get effective content (template processed or raw content)
     */
    public String getEffectiveContent() {
        if (usesTemplate() && templateVariables != null) {
            String processedContent = content;
            for (Map.Entry<String, String> entry : templateVariables.entrySet()) {
                processedContent = processedContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return processedContent;
        }
        return content;
    }

    /**
     * Add template variable
     */
    public void addTemplateVariable(String key, String value) {
        if (templateVariables == null) {
            templateVariables = new java.util.HashMap<>();
        }
        templateVariables.put(key, value);
    }

    /**
     * Add attachment
     */
    public void addAttachment(String attachment) {
        if (attachments == null) {
            attachments = new java.util.ArrayList<>();
        }
        attachments.add(attachment);
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }
}
