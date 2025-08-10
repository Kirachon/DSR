package ph.gov.dsr.registration.security;

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
import ph.gov.dsr.registration.config.JwtProperties;
import ph.gov.dsr.registration.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Utility class for token generation, validation, and parsing
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
                .withIssuer(jwtProperties.getIssuer())
                .withAudience(jwtProperties.getAudience())
                .build();
    }

    /**
     * Generate access token for user
     */
    public String generateAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(jwtProperties.getExpiration(), ChronoUnit.SECONDS);

            return JWT.create()
                    .withIssuer(jwtProperties.getIssuer())
                    .withAudience(jwtProperties.getAudience())
                    .withSubject(user.getId().toString())
                    .withClaim("email", user.getEmail())
                    .withClaim("firstName", user.getFirstName())
                    .withClaim("lastName", user.getLastName())
                    .withClaim("role", user.getRole().name())
                    .withClaim("status", user.getStatus().name())
                    .withClaim("type", "access")
                    .withIssuedAt(Date.from(now))
                    .withExpiresAt(Date.from(expiration))
                    .withJWTId(UUID.randomUUID().toString())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            logger.error("Error creating access token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plus(jwtProperties.getRefreshExpiration(), ChronoUnit.SECONDS);

            return JWT.create()
                    .withIssuer(jwtProperties.getIssuer())
                    .withAudience(jwtProperties.getAudience())
                    .withSubject(user.getId().toString())
                    .withClaim("email", user.getEmail())
                    .withClaim("type", "refresh")
                    .withIssuedAt(Date.from(now))
                    .withExpiresAt(Date.from(expiration))
                    .withJWTId(UUID.randomUUID().toString())
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            logger.error("Error creating refresh token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to generate refresh token", e);
        }
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
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            logger.debug("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Check if token is of specific type (access or refresh)
     */
    public boolean isTokenType(String token, String type) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String tokenType = decodedJWT.getClaim("type").asString();
            return type.equals(tokenType);
        } catch (Exception e) {
            logger.debug("Failed to check token type: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract token from Authorization header
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Get token expiration time in seconds
     */
    public Long getTokenExpirationSeconds() {
        return jwtProperties.getExpiration();
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public Long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshExpiration();
    }
}
