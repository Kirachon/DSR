package ph.gov.dsr.datamanagement.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ph.gov.dsr.datamanagement.config.JwtProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Utility class for token validation and parsing in Data Management Service
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final JwtProperties jwtProperties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Autowired
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = JWT.require(algorithm)
                .withIssuer("dsr-registration-service") // Accept tokens from registration service
                .withAudience(jwtProperties.getAudience())
                .build();
    }

    /**
     * Validate and decode JWT token
     */
    public DecodedJWT validateToken(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    /**
     * Extract user ID from token
     */
    public UUID extractUserId(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return UUID.fromString(decodedJWT.getSubject());
        } catch (Exception e) {
            logger.debug("Failed to extract user ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token format", e);
        }
    }

    /**
     * Extract email from token
     */
    public String extractEmail(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getClaim("email").asString();
        } catch (Exception e) {
            logger.debug("Failed to extract email from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token format", e);
        }
    }

    /**
     * Extract role from token
     */
    public String extractRole(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getClaim("role").asString();
        } catch (Exception e) {
            logger.debug("Failed to extract role from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token format", e);
        }
    }

    /**
     * Extract first name from token
     */
    public String extractFirstName(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getClaim("firstName").asString();
        } catch (Exception e) {
            logger.debug("Failed to extract first name from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract last name from token
     */
    public String extractLastName(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getClaim("lastName").asString();
        } catch (Exception e) {
            logger.debug("Failed to extract last name from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            logger.debug("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Check if token is of specific type
     */
    public boolean isTokenType(String token, String expectedType) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            String tokenType = decodedJWT.getClaim("type").asString();
            return expectedType.equals(tokenType);
        } catch (Exception e) {
            logger.debug("Failed to check token type: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract all claims from token
     */
    public DecodedJWT extractAllClaims(String token) {
        return validateToken(token);
    }

    /**
     * Get token expiration time
     */
    public Date getTokenExpiration(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getExpiresAt();
        } catch (Exception e) {
            logger.debug("Failed to get token expiration: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get token issued time
     */
    public Date getTokenIssuedAt(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getIssuedAt();
        } catch (Exception e) {
            logger.debug("Failed to get token issued time: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get token ID
     */
    public String getTokenId(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getId();
        } catch (Exception e) {
            logger.debug("Failed to get token ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if token is valid for the given user
     */
    public boolean isValidTokenForUser(String token, String userEmail) {
        try {
            String tokenEmail = extractEmail(token);
            return userEmail.equals(tokenEmail) && !isTokenExpired(token) && isTokenType(token, "access");
        } catch (Exception e) {
            logger.debug("Token validation failed for user {}: {}", userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Extract user status from token
     */
    public String extractUserStatus(String token) {
        try {
            DecodedJWT decodedJWT = validateToken(token);
            return decodedJWT.getClaim("status").asString();
        } catch (Exception e) {
            logger.debug("Failed to extract user status from token: {}", e.getMessage());
            return null;
        }
    }
}
