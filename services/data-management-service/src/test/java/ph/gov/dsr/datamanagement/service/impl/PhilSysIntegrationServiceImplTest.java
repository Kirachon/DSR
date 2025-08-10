package ph.gov.dsr.datamanagement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationRequest;
import ph.gov.dsr.datamanagement.dto.PhilSysVerificationResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PhilSysIntegrationServiceImpl
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@ExtendWith(MockitoExtension.class)
class PhilSysIntegrationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PhilSysIntegrationServiceImpl philSysIntegrationService;

    private PhilSysVerificationRequest testRequest;

    @BeforeEach
    void setUp() {
        // Set up test configuration
        ReflectionTestUtils.setField(philSysIntegrationService, "philSysApiUrl", "http://localhost:9000/api/v1");
        ReflectionTestUtils.setField(philSysIntegrationService, "philSysApiKey", "test_key");
        ReflectionTestUtils.setField(philSysIntegrationService, "timeoutMs", 30000);
        ReflectionTestUtils.setField(philSysIntegrationService, "mockEnabled", false);

        testRequest = new PhilSysVerificationRequest();
        testRequest.setPsn("1234-5678-9012");
        testRequest.setFirstName("Juan");
        testRequest.setLastName("Dela Cruz");
        testRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testRequest.setSex("M");
    }

    @Test
    void testVerifyPSN_Success() {
        // Arrange
        PhilSysVerificationResponse expectedResponse = new PhilSysVerificationResponse();
        expectedResponse.setPsn(testRequest.getPsn());
        expectedResponse.setValid(true);
        expectedResponse.setVerificationStatus("VERIFIED");
        expectedResponse.setFirstName("Juan");
        expectedResponse.setLastName("Dela Cruz");

        ResponseEntity<PhilSysVerificationResponse> responseEntity = 
            new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class))).thenReturn(responseEntity);

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testRequest.getPsn(), response.getPsn());
        assertTrue(response.isValid());
        assertEquals("VERIFIED", response.getVerificationStatus());
        assertNotNull(response.getVerifiedAt());

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class));
    }

    @Test
    void testVerifyPSN_InvalidFormat() {
        // Arrange
        testRequest.setPsn("invalid-psn");

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals("invalid-psn", response.getPsn());
        assertFalse(response.isValid());
        assertEquals("INVALID_FORMAT", response.getVerificationStatus());
        assertTrue(response.getErrorMessage().contains("PSN format is invalid"));

        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testVerifyPSN_ApiError() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testRequest.getPsn(), response.getPsn());
        assertFalse(response.isValid());
        assertEquals("API_ERROR", response.getVerificationStatus());
        assertTrue(response.getErrorMessage().contains("PhilSys API error"));

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class));
    }

    @Test
    void testVerifyPSN_ConnectionError() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testRequest.getPsn(), response.getPsn());
        assertFalse(response.isValid());
        assertEquals("CONNECTION_ERROR", response.getVerificationStatus());
        assertTrue(response.getErrorMessage().contains("Unable to connect to PhilSys service"));

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class));
    }

    @Test
    void testVerifyPSN_MockMode() {
        // Arrange
        ReflectionTestUtils.setField(philSysIntegrationService, "mockEnabled", true);

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPSN(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testRequest.getPsn(), response.getPsn());
        assertTrue(response.isValid());
        assertEquals("VERIFIED", response.getVerificationStatus());
        assertTrue(response.getConfidenceScore() > 0);

        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testIsPSNValid_ValidFormat() {
        // Arrange
        String validPsn = "1234-5678-9012";
        Map<String, Object> responseBody = Map.of("exists", true);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(Map.class))).thenReturn(responseEntity);

        // Act
        boolean result = philSysIntegrationService.isPSNValid(validPsn);

        // Assert
        assertTrue(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testIsPSNValid_InvalidFormat() {
        // Arrange
        String invalidPsn = "invalid";

        // Act
        boolean result = philSysIntegrationService.isPSNValid(invalidPsn);

        // Assert
        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testIsPSNValid_MockMode() {
        // Arrange
        ReflectionTestUtils.setField(philSysIntegrationService, "mockEnabled", true);
        String validPsn = "1234-5678-9012";

        // Act
        boolean result = philSysIntegrationService.isPSNValid(validPsn);

        // Assert
        assertTrue(result);
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testIsPSNValid_MockMode_InvalidPsn() {
        // Arrange
        ReflectionTestUtils.setField(philSysIntegrationService, "mockEnabled", true);
        String invalidPsn = "0000-0000-0000";

        // Act
        boolean result = philSysIntegrationService.isPSNValid(invalidPsn);

        // Assert
        assertFalse(result);
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testGetPersonInfo_Success() {
        // Arrange
        String psn = "1234-5678-9012";
        PhilSysVerificationResponse expectedResponse = new PhilSysVerificationResponse();
        expectedResponse.setPsn(psn);
        expectedResponse.setValid(true);
        expectedResponse.setVerificationStatus("FOUND");
        expectedResponse.setFirstName("Juan");
        expectedResponse.setLastName("Dela Cruz");

        ResponseEntity<PhilSysVerificationResponse> responseEntity = 
            new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class))).thenReturn(responseEntity);

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.getPersonInfo(psn);

        // Assert
        assertNotNull(response);
        assertEquals(psn, response.getPsn());
        assertTrue(response.isValid());
        assertEquals("FOUND", response.getVerificationStatus());
        assertEquals("Juan", response.getFirstName());
        assertEquals("Dela Cruz", response.getLastName());

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class));
    }

    @Test
    void testGetPersonInfo_NotFound() {
        // Arrange
        String psn = "9999-9999-9999";

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.getPersonInfo(psn);

        // Assert
        assertNotNull(response);
        assertEquals(psn, response.getPsn());
        assertFalse(response.isValid());
        assertEquals("NOT_FOUND", response.getVerificationStatus());
        assertTrue(response.getErrorMessage().contains("Person not found"));

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class));
    }

    @Test
    void testVerifyPersonDetails_Success() {
        // Arrange
        PhilSysVerificationResponse expectedResponse = new PhilSysVerificationResponse();
        expectedResponse.setPsn(testRequest.getPsn());
        expectedResponse.setValid(true);
        expectedResponse.setVerificationStatus("VERIFIED");
        expectedResponse.setMatchScore(0.95);

        ResponseEntity<PhilSysVerificationResponse> responseEntity = 
            new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class))).thenReturn(responseEntity);

        // Act
        PhilSysVerificationResponse response = philSysIntegrationService.verifyPersonDetails(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testRequest.getPsn(), response.getPsn());
        assertTrue(response.isValid());
        assertEquals("VERIFIED", response.getVerificationStatus());
        assertEquals(0.95, response.getMatchScore());

        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), 
                eq(PhilSysVerificationResponse.class));
    }

    @Test
    void testIsPhilSysServiceAvailable_Available() {
        // Arrange
        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(String.class))).thenReturn(responseEntity);

        // Act
        boolean result = philSysIntegrationService.isPhilSysServiceAvailable();

        // Assert
        assertTrue(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testIsPhilSysServiceAvailable_Unavailable() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(String.class))).thenThrow(new ResourceAccessException("Connection failed"));

        // Act
        boolean result = philSysIntegrationService.isPhilSysServiceAvailable();

        // Assert
        assertFalse(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testIsPhilSysServiceAvailable_MockMode() {
        // Arrange
        ReflectionTestUtils.setField(philSysIntegrationService, "mockEnabled", true);

        // Act
        boolean result = philSysIntegrationService.isPhilSysServiceAvailable();

        // Assert
        assertTrue(result);
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }

    @Test
    void testGetPhilSysServiceStatus_Available() {
        // Arrange
        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(String.class))).thenReturn(responseEntity);

        // Act
        String status = philSysIntegrationService.getPhilSysServiceStatus();

        // Assert
        assertEquals("AVAILABLE", status);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetPhilSysServiceStatus_Unavailable() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), 
                eq(String.class))).thenThrow(new ResourceAccessException("Connection failed"));

        // Act
        String status = philSysIntegrationService.getPhilSysServiceStatus();

        // Assert
        assertEquals("UNAVAILABLE", status);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testGetPhilSysServiceStatus_MockMode() {
        // Arrange
        ReflectionTestUtils.setField(philSysIntegrationService, "mockEnabled", true);

        // Act
        String status = philSysIntegrationService.getPhilSysServiceStatus();

        // Assert
        assertEquals("MOCK_MODE", status);
        verify(restTemplate, never()).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class));
    }
}
