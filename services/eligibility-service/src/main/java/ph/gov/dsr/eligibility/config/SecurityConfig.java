package ph.gov.dsr.eligibility.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ph.gov.dsr.eligibility.security.JwtAuthenticationFilter;

import java.util.Arrays;

/**
 * Security Configuration for Eligibility Service
 * Configures JWT authentication and authorization
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Set session management to stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                
                // API documentation endpoints
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                
                // Eligibility assessment endpoints - require authentication
                .requestMatchers("/api/v1/eligibility/assess").hasAnyRole("ADMIN", "DSWD_STAFF", "LGU_STAFF", "CASE_WORKER", "SYSTEM")
                .requestMatchers("/api/v1/eligibility/batch-assess").hasAnyRole("ADMIN", "DSWD_STAFF", "SYSTEM")
                .requestMatchers("/api/v1/eligibility/history/**").hasAnyRole("ADMIN", "DSWD_STAFF", "LGU_STAFF", "CASE_WORKER", "SYSTEM")
                .requestMatchers("/api/v1/eligibility/status/**").hasAnyRole("ADMIN", "DSWD_STAFF", "LGU_STAFF", "CASE_WORKER")
                .requestMatchers("/api/v1/eligibility/statistics/**").hasAnyRole("ADMIN", "DSWD_STAFF", "SYSTEM")
                
                // PMT calculator endpoints
                .requestMatchers("/api/v1/pmt/**").hasAnyRole("ADMIN", "DSWD_STAFF", "LGU_STAFF", "CASE_WORKER", "SYSTEM")
                
                // Program management endpoints
                .requestMatchers("/api/v1/programs/**").hasAnyRole("ADMIN", "DSWD_STAFF", "LGU_STAFF", "CASE_WORKER", "SYSTEM")
                
                // Rules engine endpoints
                .requestMatchers("/api/v1/rules/**").hasAnyRole("ADMIN", "DSWD_STAFF", "SYSTEM")
                
                // Administrative endpoints - admin only
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/system/**").hasAnyRole("ADMIN", "SYSTEM")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins (configure based on environment)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",    // Next.js frontend
            "http://localhost:8080",    // Local development
            "https://*.dsr.gov.ph",     // Production domains
            "https://*.vercel.app"      // Vercel deployments
        ));
        
        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Expose specific headers to the client
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
