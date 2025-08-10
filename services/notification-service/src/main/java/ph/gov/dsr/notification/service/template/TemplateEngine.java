package ph.gov.dsr.notification.service.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.notification.dto.NotificationContent;
import ph.gov.dsr.notification.entity.NotificationTemplate;
import ph.gov.dsr.notification.repository.NotificationTemplateRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template engine for processing notification templates with dynamic content
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateEngine {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateVariableResolver variableResolver;
    private final TemplateValidator templateValidator;

    // Pattern for template variables: {{variable_name}}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    // Pattern for conditional blocks: {{#if condition}}...{{/if}}
    private static final Pattern CONDITIONAL_PATTERN = Pattern.compile(
        "\\{\\{#if\\s+([^}]+)\\}\\}(.*?)\\{\\{/if\\}\\}", Pattern.DOTALL);
    
    // Pattern for loops: {{#each items}}...{{/each}}
    private static final Pattern LOOP_PATTERN = Pattern.compile(
        "\\{\\{#each\\s+([^}]+)\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);

    /**
     * Process notification template with provided data
     */
    public NotificationContent processTemplate(NotificationTemplate template, Map<String, Object> data) {
        log.info("Processing template: {} with {} variables", template.getName(), data.size());
        
        try {
            // Validate template
            templateValidator.validate(template);
            
            // Enrich data with system variables
            Map<String, Object> enrichedData = enrichWithSystemVariables(data);
            
            // Process each channel content
            NotificationContent.NotificationContentBuilder contentBuilder = NotificationContent.builder();
            
            if (template.getSmsTemplate() != null) {
                String smsContent = processTemplateContent(template.getSmsTemplate(), enrichedData);
                contentBuilder.smsContent(smsContent);
            }
            
            if (template.getEmailTemplate() != null) {
                String emailSubject = processTemplateContent(template.getEmailSubject(), enrichedData);
                String emailContent = processTemplateContent(template.getEmailTemplate(), enrichedData);
                contentBuilder.emailSubject(emailSubject).emailContent(emailContent);
            }
            
            if (template.getPushTemplate() != null) {
                String pushTitle = processTemplateContent(template.getPushTitle(), enrichedData);
                String pushBody = processTemplateContent(template.getPushTemplate(), enrichedData);
                contentBuilder.pushTitle(pushTitle).pushBody(pushBody);
            }
            
            if (template.getVoiceTemplate() != null) {
                String voiceContent = processTemplateContent(template.getVoiceTemplate(), enrichedData);
                contentBuilder.voiceContent(voiceContent);
            }
            
            if (template.getWhatsAppTemplate() != null) {
                String whatsAppContent = processTemplateContent(template.getWhatsAppTemplate(), enrichedData);
                contentBuilder.whatsAppContent(whatsAppContent)
                    .whatsAppTemplateId(template.getWhatsAppTemplateId());
            }
            
            NotificationContent content = contentBuilder.build();
            
            log.info("Template processing completed successfully");
            return content;
            
        } catch (Exception e) {
            log.error("Failed to process template: {}", template.getName(), e);
            throw new TemplateProcessingException("Template processing failed", e);
        }
    }

    /**
     * Process template content with variable substitution and logic
     */
    private String processTemplateContent(String template, Map<String, Object> data) {
        if (template == null || template.trim().isEmpty()) {
            return "";
        }
        
        String processed = template;
        
        // Process conditional blocks first
        processed = processConditionals(processed, data);
        
        // Process loops
        processed = processLoops(processed, data);
        
        // Process variables
        processed = processVariables(processed, data);
        
        // Clean up any remaining template syntax
        processed = cleanupTemplate(processed);
        
        return processed;
    }

    /**
     * Process conditional blocks: {{#if condition}}...{{/if}}
     */
    private String processConditionals(String template, Map<String, Object> data) {
        Matcher matcher = CONDITIONAL_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);
            
            boolean conditionResult = evaluateCondition(condition, data);
            String replacement = conditionResult ? content : "";
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Process loop blocks: {{#each items}}...{{/each}}
     */
    private String processLoops(String template, Map<String, Object> data) {
        Matcher matcher = LOOP_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String arrayName = matcher.group(1).trim();
            String loopTemplate = matcher.group(2);
            
            String loopResult = processLoop(arrayName, loopTemplate, data);
            matcher.appendReplacement(result, Matcher.quoteReplacement(loopResult));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Process a single loop
     */
    private String processLoop(String arrayName, String loopTemplate, Map<String, Object> data) {
        Object arrayValue = variableResolver.resolveVariable(arrayName, data);
        
        if (!(arrayValue instanceof Iterable)) {
            log.warn("Loop variable '{}' is not iterable", arrayName);
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        Iterable<?> items = (Iterable<?>) arrayValue;
        int index = 0;
        
        for (Object item : items) {
            Map<String, Object> loopData = new HashMap<>(data);
            loopData.put("this", item);
            loopData.put("@index", index);
            loopData.put("@first", index == 0);
            
            // Add item properties if it's a map
            if (item instanceof Map) {
                Map<?, ?> itemMap = (Map<?, ?>) item;
                for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
                    loopData.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            
            String processedItem = processTemplateContent(loopTemplate, loopData);
            result.append(processedItem);
            index++;
        }
        
        return result.toString();
    }

    /**
     * Process variable substitutions: {{variable_name}}
     */
    private String processVariables(String template, Map<String, Object> data) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variableResolver.resolveVariable(variableName, data);
            String stringValue = formatValue(value);
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(stringValue));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Evaluate conditional expressions
     */
    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        try {
            // Handle simple existence checks
            if (!condition.contains(" ")) {
                Object value = variableResolver.resolveVariable(condition, data);
                return isTruthy(value);
            }
            
            // Handle comparison operators
            if (condition.contains("==")) {
                String[] parts = condition.split("==", 2);
                Object left = variableResolver.resolveVariable(parts[0].trim(), data);
                Object right = parseValue(parts[1].trim(), data);
                return Objects.equals(left, right);
            }
            
            if (condition.contains("!=")) {
                String[] parts = condition.split("!=", 2);
                Object left = variableResolver.resolveVariable(parts[0].trim(), data);
                Object right = parseValue(parts[1].trim(), data);
                return !Objects.equals(left, right);
            }
            
            if (condition.contains(">=")) {
                String[] parts = condition.split(">=", 2);
                return compareNumbers(parts[0].trim(), parts[1].trim(), data) >= 0;
            }
            
            if (condition.contains("<=")) {
                String[] parts = condition.split("<=", 2);
                return compareNumbers(parts[0].trim(), parts[1].trim(), data) <= 0;
            }
            
            if (condition.contains(">")) {
                String[] parts = condition.split(">", 2);
                return compareNumbers(parts[0].trim(), parts[1].trim(), data) > 0;
            }
            
            if (condition.contains("<")) {
                String[] parts = condition.split("<", 2);
                return compareNumbers(parts[0].trim(), parts[1].trim(), data) < 0;
            }
            
            // Default to existence check
            Object value = variableResolver.resolveVariable(condition, data);
            return isTruthy(value);
            
        } catch (Exception e) {
            log.warn("Failed to evaluate condition: {}", condition, e);
            return false;
        }
    }

    /**
     * Check if a value is truthy
     */
    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return !((String) value).trim().isEmpty();
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof Iterable) return ((Iterable<?>) value).iterator().hasNext();
        return true;
    }

    /**
     * Parse a value from template (could be variable or literal)
     */
    private Object parseValue(String valueStr, Map<String, Object> data) {
        valueStr = valueStr.trim();
        
        // String literal
        if ((valueStr.startsWith("\"") && valueStr.endsWith("\"")) ||
            (valueStr.startsWith("'") && valueStr.endsWith("'"))) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        
        // Number literal
        try {
            if (valueStr.contains(".")) {
                return Double.parseDouble(valueStr);
            } else {
                return Long.parseLong(valueStr);
            }
        } catch (NumberFormatException e) {
            // Not a number, treat as variable
        }
        
        // Boolean literal
        if ("true".equals(valueStr)) return true;
        if ("false".equals(valueStr)) return false;
        
        // Variable
        return variableResolver.resolveVariable(valueStr, data);
    }

    /**
     * Compare two numeric values
     */
    private int compareNumbers(String leftStr, String rightStr, Map<String, Object> data) {
        Object left = parseValue(leftStr, data);
        Object right = parseValue(rightStr, data);
        
        if (left instanceof Number && right instanceof Number) {
            double leftVal = ((Number) left).doubleValue();
            double rightVal = ((Number) right).doubleValue();
            return Double.compare(leftVal, rightVal);
        }
        
        throw new IllegalArgumentException("Cannot compare non-numeric values");
    }

    /**
     * Format value for output
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        }
        
        if (value instanceof Number) {
            // Format numbers appropriately
            if (value instanceof Double || value instanceof Float) {
                return String.format("%.2f", ((Number) value).doubleValue());
            }
        }
        
        return String.valueOf(value);
    }

    /**
     * Enrich data with system variables
     */
    private Map<String, Object> enrichWithSystemVariables(Map<String, Object> data) {
        Map<String, Object> enriched = new HashMap<>(data);
        
        enriched.put("currentDate", LocalDateTime.now());
        enriched.put("currentYear", LocalDateTime.now().getYear());
        enriched.put("systemName", "Dynamic Social Registry");
        enriched.put("organizationName", "Department of Social Welfare and Development");
        
        return enriched;
    }

    /**
     * Clean up any remaining template syntax
     */
    private String cleanupTemplate(String template) {
        // Remove any unprocessed template blocks
        return template.replaceAll("\\{\\{[^}]*\\}\\}", "");
    }
}
