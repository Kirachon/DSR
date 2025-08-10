package ph.gov.dsr.datamanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for PhilSys Integration
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Configuration
@EnableRetry
public class PhilSysIntegrationConfig {

    @Value("${dsr.philsys.timeout:30000}")
    private int timeoutMs;

    @Bean
    public RestTemplate philSysRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(philSysClientHttpRequestFactory());
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory philSysClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return factory;
    }
}
