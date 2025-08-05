package ph.gov.dsr.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Eureka Server
 * Enables basic authentication while allowing service registration
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/eureka/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {});
        
        return http.build();
    }
}
