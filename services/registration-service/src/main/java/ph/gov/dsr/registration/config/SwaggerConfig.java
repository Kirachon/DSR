package ph.gov.dsr.registration.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for the Registration Service
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("DSR Registration Service API")
                        .version("3.0.0")
                        .description("Philippine Dynamic Social Registry (DSR) Registration Service API\n\n" +
                                "This service handles citizen engagement and registration processes, " +
                                "including PhilSys integration, multi-channel access, and life event reporting.\n\n" +
                                "## Authentication\n" +
                                "This API uses JWT (JSON Web Token) authentication. To access protected endpoints:\n" +
                                "1. Login using the `/api/v1/auth/login` endpoint\n" +
                                "2. Copy the `accessToken` from the response\n" +
                                "3. Click the 'Authorize' button below and enter: `Bearer <your-access-token>`\n" +
                                "4. You can now access protected endpoints\n\n" +
                                "## Demo Accounts (No-DB Mode)\n" +
                                "- **Admin**: admin@dsr.gov.ph / admin123\n" +
                                "- **Citizen**: citizen@dsr.gov.ph / citizen123")
                        .contact(new Contact()
                                .name("DSR Development Team")
                                .email("dsr-dev@dswd.gov.ph")
                                .url("https://dsr.gov.ph"))
                        .license(new License()
                                .name("Government of the Philippines")
                                .url("https://www.gov.ph")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api-dev.dsr.gov.ph")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.dsr.gov.ph")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. " +
                                           "Enter 'Bearer' [space] and then your token in the text input below. " +
                                           "Example: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'")
                        )
                );
    }
}
