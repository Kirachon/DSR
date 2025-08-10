package ph.gov.dsr.datamanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT Authentication Filter for Data Management Service
 * Validates JWT tokens and sets up Spring Security context
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String token = extractTokenFromRequest(request);
            
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(token, request);
            }
        } catch (Exception e) {
            logger.debug("JWT authentication failed: {}", e.getMessage());
            // Continue with the filter chain even if authentication fails
            // The security configuration will handle unauthorized access
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Authenticate user based on JWT token
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            // Validate token and extract user information
            UUID userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            String firstName = jwtUtil.extractFirstName(token);
            String lastName = jwtUtil.extractLastName(token);
            String status = jwtUtil.extractUserStatus(token);

            // Create user details
            UserDetails userDetails = createUserDetails(email, role, userId, firstName, lastName, status);
            
            // Validate token for this user
            if (isValidToken(token, userDetails)) {
                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                // Set authentication details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                logger.debug("Successfully authenticated user: {} with role: {}", email, role);
            }
        } catch (Exception e) {
            logger.debug("Failed to authenticate user from token: {}", e.getMessage());
        }
    }

    /**
     * Create UserDetails from JWT claims
     */
    private UserDetails createUserDetails(String email, String role, UUID userId, 
                                        String firstName, String lastName, String status) {
        // Create authority from role
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
        
        // Create custom user details with additional information
        return new CustomUserDetails(
            email,
            "", // No password needed for JWT authentication
            Collections.singletonList(authority),
            userId,
            firstName,
            lastName,
            role,
            status
        );
    }

    /**
     * Validate JWT token
     */
    private boolean isValidToken(String token, UserDetails userDetails) {
        try {
            // Validate token structure and signature
            jwtUtil.validateToken(token);
            
            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                logger.debug("JWT token is expired");
                return false;
            }
            
            // Check if token is access token (not refresh token)
            if (!jwtUtil.isTokenType(token, "access")) {
                logger.debug("JWT token is not an access token");
                return false;
            }
            
            // Check if token email matches user details
            String tokenEmail = jwtUtil.extractEmail(token);
            if (!tokenEmail.equals(userDetails.getUsername())) {
                logger.debug("JWT token email does not match user details");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Custom UserDetails implementation with additional user information
     */
    public static class CustomUserDetails extends User {
        private final UUID userId;
        private final String firstName;
        private final String lastName;
        private final String role;
        private final String status;

        public CustomUserDetails(String username, String password, 
                               java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities,
                               UUID userId, String firstName, String lastName, String role, String status) {
            super(username, password, authorities);
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.status = status;
        }

        public UUID getUserId() { return userId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
    }
}
