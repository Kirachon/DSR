package ph.gov.dsr.interoperability.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import ph.gov.dsr.interoperability.dto.ApiGatewayRequest;
import ph.gov.dsr.interoperability.dto.ApiGatewayResponse;
import ph.gov.dsr.interoperability.entity.ExternalSystemIntegration;
import ph.gov.dsr.interoperability.repository.ExternalSystemIntegrationRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExternalSystemConnectorService
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@ExtendWith(MockitoExtension.class)
class ExternalSystemConnectorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExternalSystemIntegrationRepository integrationRepository;

    @InjectMocks
    private ExternalSystemConnectorService connectorService;

    private ExternalSystemIntegration testIntegration;
    private ApiGatewayRequest testRequest;

    @BeforeEach
    void setUp() {
        testIntegration = new ExternalSystemIntegration();
        testIntegration.setId(UUID.randomUUID());
        testIntegration.setSystemCode("PHILSYS");
        testIntegration.setSystemName("PhilSys");
        testIntegration.setSystemType(ExternalSystemIntegration.SystemType.IDENTITY_PROVIDER);
        testIntegration.setBaseUrl("https://api.philsys.gov.ph");
        testIntegration.setApiKey("test-api-key");
        testIntegration.setIsActive(true);
        testIntegration.setStatus(ExternalSystemIntegration.SystemStatus.ACTIVE);

        testRequest = new ApiGatewayRequest();
        testRequest.setSystemCode("PHILSYS");
        testRequest.setEndpoint("/api/v1/verify");
        testRequest.setMethod("POST");
        testRequest.setBody(Map.of("psn", "123456789012"));
        testRequest.setHeaders(Map.of("Content-Type", "application/json"));
    }

    @Test
    void testConnectToGovernmentSystem_PhilSys_Success() {
        // Arrange
        when(integrationRepository.findBySystemCode("PHILSYS"))
            .thenReturn(Optional.of(testIntegration));

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(
            Map.of("status", "verified", "psn", "123456789012"), 
            HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // Act
        ApiGatewayResponse response = connectorService.connectToGovernmentSystem("PHILSYS", testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("PHILSYS", response.getSystemCode());
        assertNotNull(response.getBody());
        verify(integrationRepository).findBySystemCode("PHILSYS");
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testConnectToGovernmentSystem_SSS_Success() {
        // Arrange
        testIntegration.setSystemCode("SSS");
        testIntegration.setSystemName("SSS");
        testIntegration.setBaseUrl("https://api.sss.gov.ph");
        testRequest.setSystemCode("SSS");
        testRequest.setEndpoint("/api/v1/member/verify");

        when(integrationRepository.findBySystemCode("SSS"))
            .thenReturn(Optional.of(testIntegration));

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(
            Map.of("memberStatus", "active", "contributions", "current"), 
            HttpStatus.OK
        );
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // Act
        ApiGatewayResponse response = connectorService.connectToGovernmentSystem("SSS", testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("SSS", response.getSystemCode());
        verify(integrationRepository).findBySystemCode("SSS");
    }

    @Test
    void testConnectToGovernmentSystem_SystemNotFound() {
        // Arrange
        when(integrationRepository.findBySystemCode("UNKNOWN"))
            .thenReturn(Optional.empty());

        // Act
        ApiGatewayResponse response = connectorService.connectToGovernmentSystem("UNKNOWN", testRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("SYSTEM_NOT_FOUND", response.getErrorCode());
        verify(integrationRepository).findBySystemCode("UNKNOWN");
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testConnectToGovernmentSystem_InactiveSystem() {
        // Arrange
        testIntegration.setIsActive(false);
        when(integrationRepository.findBySystemCode("PHILSYS"))
            .thenReturn(Optional.of(testIntegration));

        // Act
        ApiGatewayResponse response = connectorService.connectToGovernmentSystem("PHILSYS", testRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("SYSTEM_INACTIVE", response.getErrorCode());
        verify(integrationRepository).findBySystemCode("PHILSYS");
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testConnectToGovernmentSystem_HttpError() {
        // Arrange
        when(integrationRepository.findBySystemCode("PHILSYS"))
            .thenReturn(Optional.of(testIntegration));

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(
            Map.of("error", "Invalid request"), 
            HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
            .thenReturn(mockResponse);

        // Act
        ApiGatewayResponse response = connectorService.connectToGovernmentSystem("PHILSYS", testRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        verify(integrationRepository).findBySystemCode("PHILSYS");
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void testConnectToGovernmentSystem_NullRequest() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            connectorService.connectToGovernmentSystem("PHILSYS", null);
        });
    }

    @Test
    void testConnectToGovernmentSystem_EmptySystemCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            connectorService.connectToGovernmentSystem("", testRequest);
        });
    }

    @Test
    void testConnectToGovernmentSystem_NullSystemCode() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            connectorService.connectToGovernmentSystem(null, testRequest);
        });
    }
}
