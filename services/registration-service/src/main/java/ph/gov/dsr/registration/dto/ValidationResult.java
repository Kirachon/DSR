package ph.gov.dsr.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation result DTO containing validation status and error messages
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {

    /**
     * Whether validation passed
     */
    private boolean valid;

    /**
     * List of validation errors
     */
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();

    /**
     * List of validation warnings
     */
    @Builder.Default
    private List<ValidationWarning> warnings = new ArrayList<>();

    /**
     * Overall validation message
     */
    private String message;

    /**
     * Validation context (e.g., "household", "member", "documents")
     */
    private String context;

    /**
     * Add a validation error
     */
    public void addError(String field, String code, String message) {
        this.errors.add(new ValidationError(field, code, message));
        this.valid = false;
    }

    /**
     * Add a validation warning
     */
    public void addWarning(String field, String code, String message) {
        this.warnings.add(new ValidationWarning(field, code, message));
    }

    /**
     * Check if there are any errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Check if there are any warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Get total number of issues (errors + warnings)
     */
    public int getTotalIssues() {
        return errors.size() + warnings.size();
    }

    /**
     * Create a successful validation result
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .message("Validation passed")
                .build();
    }

    /**
     * Create a successful validation result with context
     */
    public static ValidationResult success(String context) {
        return ValidationResult.builder()
                .valid(true)
                .context(context)
                .message("Validation passed for " + context)
                .build();
    }

    /**
     * Create a failed validation result
     */
    public static ValidationResult failure(String message) {
        return ValidationResult.builder()
                .valid(false)
                .message(message)
                .build();
    }

    /**
     * Create a failed validation result with context
     */
    public static ValidationResult failure(String context, String message) {
        return ValidationResult.builder()
                .valid(false)
                .context(context)
                .message(message)
                .build();
    }

    /**
     * Merge multiple validation results
     */
    public static ValidationResult merge(List<ValidationResult> results) {
        ValidationResult merged = new ValidationResult();
        merged.setValid(true);
        
        for (ValidationResult result : results) {
            if (!result.isValid()) {
                merged.setValid(false);
            }
            merged.getErrors().addAll(result.getErrors());
            merged.getWarnings().addAll(result.getWarnings());
        }
        
        if (!merged.isValid()) {
            merged.setMessage("Validation failed with " + merged.getErrors().size() + " errors");
        } else if (merged.hasWarnings()) {
            merged.setMessage("Validation passed with " + merged.getWarnings().size() + " warnings");
        } else {
            merged.setMessage("Validation passed");
        }
        
        return merged;
    }

    /**
     * Validation error details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String code;
        private String message;
    }

    /**
     * Validation warning details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationWarning {
        private String field;
        private String code;
        private String message;
    }
}
