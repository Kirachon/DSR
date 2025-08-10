package ph.gov.dsr.interoperability.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration Properties for Interoperability Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Component
@ConfigurationProperties(prefix = "security.jwt")
@Validated
@Data
public class JwtProperties {

    @NotBlank(message = "JWT secret cannot be blank")
    private String secret;

    @Positive(message = "JWT expiration must be positive")
    private Long expiration = 86400L; // 24 hours in seconds

    @Positive(message = "JWT refresh expiration must be positive")
    private Long refreshExpiration = 604800L; // 7 days in seconds

    private String issuer = "dsr-interoperability-service";
    
    private String audience = "dsr-users";

    public boolean isValid() {
        return secret != null && !secret.trim().isEmpty() && 
               expiration != null && expiration > 0 &&
               refreshExpiration != null && refreshExpiration > 0;
    }
}
