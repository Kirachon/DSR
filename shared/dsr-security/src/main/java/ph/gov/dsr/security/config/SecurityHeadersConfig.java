package ph.gov.dsr.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Security Headers Configuration for DSR System
 * 
 * Implements comprehensive security headers in compliance with:
 * - OWASP Security Headers
 * - Philippine Government ICT Standards
 * - Data Privacy Act requirements
 */
@Configuration
@EnableWebSecurity
public class SecurityHeadersConfig {
    
    @Bean
    public SecurityFilterChain securityHeadersFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                // Content Security Policy
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self' https://api.dsr.gov.ph; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'"
                    )
                )
                
                // HTTP Strict Transport Security (HSTS)
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubdomains(true)
                    .preload(true)
                )
                
                // X-Frame-Options
                .frameOptions(frameOptions -> frameOptions.deny())
                
                // X-Content-Type-Options
                .contentTypeOptions(contentTypeOptions -> contentTypeOptions.and())
                
                // X-XSS-Protection
                .addHeaderWriter(new XXssProtectionHeaderWriter())
                
                // Referrer Policy
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                
                // Permissions Policy (formerly Feature Policy)
                .addHeaderWriter((request, response) -> {
                    response.setHeader("Permissions-Policy", 
                        "geolocation=(), " +
                        "microphone=(), " +
                        "camera=(), " +
                        "payment=(), " +
                        "usb=(), " +
                        "magnetometer=(), " +
                        "gyroscope=(), " +
                        "speaker=()"
                    );
                })
                
                // Custom Security Headers for Philippine Government
                .addHeaderWriter((request, response) -> {
                    // Philippine Government Classification
                    response.setHeader("X-Gov-Classification", "CONFIDENTIAL");
                    
                    // Data Privacy Act Compliance
                    response.setHeader("X-DPA-Compliant", "true");
                    
                    // Security Contact
                    response.setHeader("Security-Contact", "security@dsr.gov.ph");
                    
                    // Cache Control for sensitive data
                    if (request.getRequestURI().contains("/api/")) {
                        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
                        response.setHeader("Pragma", "no-cache");
                        response.setHeader("Expires", "0");
                    }
                    
                    // Server Information Hiding
                    response.setHeader("Server", "DSR-Gov-Server");
                    
                    // Cross-Origin Policies
                    response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
                    response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
                    response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
                })
            );
        
        return http.build();
    }
    
    /**
     * Additional security configuration for API endpoints
     */
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .headers(headers -> headers
                // API-specific headers
                .addHeaderWriter((request, response) -> {
                    // API Rate Limiting Info
                    response.setHeader("X-RateLimit-Limit", "1000");
                    response.setHeader("X-RateLimit-Window", "3600");
                    
                    // API Version
                    response.setHeader("X-API-Version", "3.0.0");
                    
                    // Data Classification for API responses
                    String uri = request.getRequestURI();
                    if (uri.contains("/citizens/") || uri.contains("/personal/")) {
                        response.setHeader("X-Data-Classification", "RESTRICTED");
                    } else if (uri.contains("/internal/")) {
                        response.setHeader("X-Data-Classification", "CONFIDENTIAL");
                    } else {
                        response.setHeader("X-Data-Classification", "INTERNAL");
                    }
                    
                    // Audit Trail Indicator
                    response.setHeader("X-Audit-Required", "true");
                })
            );
        
        return http.build();
    }
}
