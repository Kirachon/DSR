package ph.gov.dsr.registration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Intercepts requests, validates JWT tokens, and sets security context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from header
        jwt = jwtUtil.extractTokenFromHeader(authHeader);
        
        try {
            // Extract user email from token
            userEmail = jwtUtil.extractEmail(jwt);
            
            // If user email is present and no authentication is set in security context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                // Validate token
                if (isValidToken(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("JWT authentication successful for user: {}", userEmail);
                } else {
                    logger.debug("JWT token validation failed for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            logger.debug("JWT authentication failed: {}", e.getMessage());
            // Clear security context on authentication failure
            SecurityContextHolder.clearContext();
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
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
            logger.debug("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if request should be filtered
     * Skip JWT authentication for public endpoints
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip JWT authentication for public endpoints
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/api/v1/auth/forgot-password") ||
               path.startsWith("/api/v1/auth/reset-password") ||
               path.startsWith("/api/v1/auth/verify-email") ||
               path.startsWith("/api/v1/auth/resend-verification") ||
               path.startsWith("/api/v1/auth/check-email") ||
               path.startsWith("/api/v1/auth/philsys") ||
               path.startsWith("/api/v1/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars");
    }
}
