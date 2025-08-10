package ph.gov.dsr.registration.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration Properties
 * Binds JWT settings from application.yml with validation
 */
@Component
@ConfigurationProperties(prefix = "security.jwt")
@Validated
public class JwtProperties {

    @NotBlank(message = "JWT secret cannot be blank")
    private String secret;

    @Positive(message = "JWT expiration must be positive")
    private Long expiration = 86400L; // 24 hours in seconds

    @Positive(message = "JWT refresh expiration must be positive")
    private Long refreshExpiration = 604800L; // 7 days in seconds

    private String issuer = "dsr-registration-service";
    
    private String audience = "dsr-users";

    // Constructors
    public JwtProperties() {}

    public JwtProperties(String secret, Long expiration, Long refreshExpiration) {
        this.secret = secret;
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    /**
     * Get expiration time in milliseconds
     */
    public Long getExpirationMs() {
        return expiration * 1000;
    }

    /**
     * Get refresh expiration time in milliseconds
     */
    public Long getRefreshExpirationMs() {
        return refreshExpiration * 1000;
    }

    @Override
    public String toString() {
        return "JwtProperties{" +
                "expiration=" + expiration +
                ", refreshExpiration=" + refreshExpiration +
                ", issuer='" + issuer + '\'' +
                ", audience='" + audience + '\'' +
                '}';
    }
}
