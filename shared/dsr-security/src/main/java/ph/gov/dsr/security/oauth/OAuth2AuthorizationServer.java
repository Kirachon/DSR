package ph.gov.dsr.security.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.time.Duration;
import java.util.UUID;

/**
 * OAuth 2.1 Authorization Server Configuration for DSR System
 * 
 * Implements OAuth 2.1 with PKCE (Proof Key for Code Exchange) for enhanced security.
 * Supports authorization code flow with PKCE for public clients and confidential clients.
 * 
 * Features:
 * - OAuth 2.1 compliance with PKCE mandatory for authorization code flow
 * - JWT access tokens with secure rotation
 * - Refresh token rotation for enhanced security
 * - OpenID Connect support for identity layer
 * - Client authentication with multiple methods
 */
@Configuration
@EnableWebSecurity
public class OAuth2AuthorizationServer {

    /**
     * OAuth 2.1 Authorization Server Security Filter Chain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(oidc -> oidc
                .providerConfigurationEndpoint(providerConfiguration -> 
                    providerConfiguration.providerConfigurationCustomizer(builder -> 
                        builder
                            .codeChallengeMethodsSupported("S256") // PKCE with SHA256
                            .grantTypesSupported("authorization_code", "refresh_token", "client_credentials")
                            .responseTypesSupported("code")
                            .tokenEndpointAuthMethodsSupported("client_secret_basic", "client_secret_post", "none")
                    )
                )
            );

        http
            // Redirect to the login page when not authenticated from the authorization endpoint
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            )
            // Accept access tokens for User Info and/or Client Registration
            .oauth2ResourceServer(resourceServer -> resourceServer
                .jwt(jwt -> {})
            );

        return http.build();
    }

    /**
     * Registered Client Repository with DSR system clients
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // DSR Web Application Client (Public Client with PKCE)
        RegisteredClient dsrWebClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("dsr-web-client")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("https://dsr.gov.ph/login/oauth2/code/dsr")
            .redirectUri("http://localhost:3000/login/oauth2/code/dsr") // Development
            .postLogoutRedirectUri("https://dsr.gov.ph/logout")
            .postLogoutRedirectUri("http://localhost:3000/logout") // Development
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("dsr.citizen.read")
            .scope("dsr.citizen.write")
            .scope("dsr.household.read")
            .scope("dsr.household.write")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true) // PKCE required for OAuth 2.1
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30)) // Short-lived access tokens
                .refreshTokenTimeToLive(Duration.ofDays(1)) // Refresh token rotation
                .reuseRefreshTokens(false) // Refresh token rotation
                .build())
            .build();

        // DSR Admin Client (Confidential Client)
        RegisteredClient dsrAdminClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("dsr-admin-client")
            .clientSecret("{noop}dsr-admin-secret-2024") // Use proper password encoder in production
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("https://admin.dsr.gov.ph/login/oauth2/code/dsr")
            .redirectUri("http://localhost:3001/login/oauth2/code/dsr") // Development
            .postLogoutRedirectUri("https://admin.dsr.gov.ph/logout")
            .postLogoutRedirectUri("http://localhost:3001/logout") // Development
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("dsr.admin.read")
            .scope("dsr.admin.write")
            .scope("dsr.system.manage")
            .scope("dsr.reports.generate")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false) // Admin client pre-approved
                .requireProofKey(true) // PKCE required for OAuth 2.1
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1)) // Longer for admin operations
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .reuseRefreshTokens(false) // Refresh token rotation
                .build())
            .build();

        // DSR Mobile App Client (Public Client with PKCE)
        RegisteredClient dsrMobileClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("dsr-mobile-client")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("ph.gov.dsr://oauth/callback")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("dsr.citizen.read")
            .scope("dsr.citizen.write")
            .scope("dsr.household.read")
            .scope("dsr.offline_access") // For refresh tokens
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true) // PKCE mandatory for mobile apps
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15)) // Very short for mobile
                .refreshTokenTimeToLive(Duration.ofDays(30)) // Longer refresh for mobile UX
                .reuseRefreshTokens(false) // Refresh token rotation
                .build())
            .build();

        // Service-to-Service Client (Machine-to-Machine)
        RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("dsr-service-client")
            .clientSecret("{noop}dsr-service-secret-2024") // Use proper password encoder in production
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("dsr.service.internal")
            .scope("dsr.data.sync")
            .scope("dsr.analytics.process")
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false) // Service clients pre-approved
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(10)) // Short-lived for services
                .build())
            .build();

        return new InMemoryRegisteredClientRepository(
            dsrWebClient, 
            dsrAdminClient, 
            dsrMobileClient, 
            serviceClient
        );
    }

    /**
     * Authorization Server Settings
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            .issuer("https://auth.dsr.gov.ph") // Production issuer
            .authorizationEndpoint("/oauth2/authorize")
            .tokenEndpoint("/oauth2/token")
            .tokenIntrospectionEndpoint("/oauth2/introspect")
            .tokenRevocationEndpoint("/oauth2/revoke")
            .jwkSetEndpoint("/oauth2/jwks")
            .oidcLogoutEndpoint("/connect/logout")
            .oidcUserInfoEndpoint("/userinfo")
            .oidcClientRegistrationEndpoint("/connect/register")
            .build();
    }
}
