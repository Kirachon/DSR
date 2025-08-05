package ph.gov.dsr.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * Gateway Configuration for DSR API Gateway
 * Configures rate limiting, security, and routing
 */
@Configuration
public class GatewayConfig {

    /**
     * User-based key resolver for rate limiting
     * Citizens: 100 requests/minute
     * Staff: 500 requests/minute
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .cast(SecurityContext.class)
                .map(ctx -> ctx.getAuthentication())
                .cast(Authentication.class)
                .map(auth -> {
                    String username = auth.getName();
                    String role = auth.getAuthorities().iterator().next().getAuthority();
                    
                    // Different rate limits based on role
                    if ("ROLE_DSWD_STAFF".equals(role) || "ROLE_LGU_STAFF".equals(role)) {
                        return "staff:" + username;
                    } else {
                        return "citizen:" + username;
                    }
                })
                .switchIfEmpty(Mono.just("anonymous"))
                .onErrorReturn("anonymous");
    }

    /**
     * IP-based key resolver as fallback
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress()
        );
    }
}
