package ph.gov.dsr.datamanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * Request DTO for data validation operations
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Data
public class ValidationRequest {

    @NotBlank(message = "Data type is required")
    private String dataType; // HOUSEHOLD, INDIVIDUAL, ECONOMIC_PROFILE

    @NotNull(message = "Data to validate is required")
    private Map<String, Object> data;

    private String validationProfile = "DEFAULT"; // DEFAULT, STRICT, LENIENT

    private boolean includeWarnings = true;

    private boolean validateReferences = true; // Validate foreign key references

    private String sourceSystem; // For source-specific validation rules
}
