package ph.gov.dsr.security.mtls;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;

/**
 * Mutual TLS Configuration for DSR Service-to-Service Communication
 * 
 * Implements mutual TLS (mTLS) authentication between DSR microservices for enhanced security.
 * Each service authenticates to other services using client certificates, ensuring:
 * - Service identity verification
 * - Encrypted communication channels
 * - Protection against man-in-the-middle attacks
 * - Zero-trust network security model
 */
@Configuration
@Profile("!test") // Disable mTLS in test environment
public class MutualTLSConfiguration {

    @Value("${dsr.mtls.keystore.path:classpath:certificates/dsr-service.p12}")
    private String keystorePath;

    @Value("${dsr.mtls.keystore.password:dsr-keystore-password}")
    private String keystorePassword;

    @Value("${dsr.mtls.truststore.path:classpath:certificates/dsr-truststore.p12}")
    private String truststorePath;

    @Value("${dsr.mtls.truststore.password:dsr-truststore-password}")
    private String truststorePassword;

    @Value("${dsr.mtls.enabled:true}")
    private boolean mtlsEnabled;

    /**
     * RestTemplate configured with mutual TLS for service-to-service communication
     */
    @Bean("mtlsRestTemplate")
    public RestTemplate mtlsRestTemplate() throws Exception {
        if (!mtlsEnabled) {
            return new RestTemplate(); // Standard RestTemplate for non-production
        }

        return new RestTemplate(createMtlsClientHttpRequestFactory());
    }

    /**
     * Creates HTTP client request factory with mTLS configuration
     */
    private ClientHttpRequestFactory createMtlsClientHttpRequestFactory() throws Exception {
        SSLContext sslContext = createSSLContext();
        
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
            sslContext,
            new String[]{"TLSv1.3", "TLSv1.2"}, // Supported TLS versions
            null, // Use default cipher suites
            SSLConnectionSocketFactory.getDefaultHostnameVerifier()
        );

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .setMaxConnTotal(100)
                    .setMaxConnPerRoute(20)
                    .build()
            )
            .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    /**
     * Creates SSL context with client certificate and trusted CA certificates
     */
    private SSLContext createSSLContext() throws Exception {
        // Load client keystore (contains this service's private key and certificate)
        KeyStore keyStore = loadKeyStore(keystorePath, keystorePassword);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        // Load truststore (contains trusted CA certificates and peer certificates)
        KeyStore trustStore = loadKeyStore(truststorePath, truststorePassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Create SSL context with mutual authentication
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(
            keyManagerFactory.getKeyManagers(),
            trustManagerFactory.getTrustManagers(),
            new java.security.SecureRandom()
        );

        return sslContext;
    }

    /**
     * Loads keystore from file system or classpath
     */
    private KeyStore loadKeyStore(String path, String password) 
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        
        try (FileInputStream fis = new FileInputStream(path.replace("classpath:", ""))) {
            keyStore.load(fis, password.toCharArray());
        } catch (IOException e) {
            // Try loading from classpath if file not found
            try (var inputStream = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
                if (inputStream != null) {
                    keyStore.load(inputStream, password.toCharArray());
                } else {
                    throw new IOException("Keystore not found: " + path);
                }
            }
        }
        
        return keyStore;
    }

    /**
     * Service certificate information for monitoring and debugging
     */
    @Bean
    public ServiceCertificateInfo serviceCertificateInfo() {
        try {
            KeyStore keyStore = loadKeyStore(keystorePath, keystorePassword);
            String alias = keyStore.aliases().nextElement();
            var certificate = keyStore.getCertificate(alias);
            
            return ServiceCertificateInfo.builder()
                .alias(alias)
                .subject(certificate.toString())
                .mtlsEnabled(mtlsEnabled)
                .keystorePath(keystorePath)
                .build();
                
        } catch (Exception e) {
            return ServiceCertificateInfo.builder()
                .mtlsEnabled(false)
                .error("Failed to load certificate: " + e.getMessage())
                .build();
        }
    }

    /**
     * Certificate information for service monitoring
     */
    public static class ServiceCertificateInfo {
        private String alias;
        private String subject;
        private boolean mtlsEnabled;
        private String keystorePath;
        private String error;

        public static ServiceCertificateInfoBuilder builder() {
            return new ServiceCertificateInfoBuilder();
        }

        // Getters
        public String getAlias() { return alias; }
        public String getSubject() { return subject; }
        public boolean isMtlsEnabled() { return mtlsEnabled; }
        public String getKeystorePath() { return keystorePath; }
        public String getError() { return error; }

        public static class ServiceCertificateInfoBuilder {
            private String alias;
            private String subject;
            private boolean mtlsEnabled;
            private String keystorePath;
            private String error;

            public ServiceCertificateInfoBuilder alias(String alias) {
                this.alias = alias;
                return this;
            }

            public ServiceCertificateInfoBuilder subject(String subject) {
                this.subject = subject;
                return this;
            }

            public ServiceCertificateInfoBuilder mtlsEnabled(boolean mtlsEnabled) {
                this.mtlsEnabled = mtlsEnabled;
                return this;
            }

            public ServiceCertificateInfoBuilder keystorePath(String keystorePath) {
                this.keystorePath = keystorePath;
                return this;
            }

            public ServiceCertificateInfoBuilder error(String error) {
                this.error = error;
                return this;
            }

            public ServiceCertificateInfo build() {
                ServiceCertificateInfo info = new ServiceCertificateInfo();
                info.alias = this.alias;
                info.subject = this.subject;
                info.mtlsEnabled = this.mtlsEnabled;
                info.keystorePath = this.keystorePath;
                info.error = this.error;
                return info;
            }
        }
    }
}
