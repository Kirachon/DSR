package ph.gov.dsr.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ph.gov.dsr.payment.dto.FSPPaymentRequest;
import ph.gov.dsr.payment.dto.FSPPaymentResponse;
import ph.gov.dsr.payment.entity.FSPConfiguration;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.repository.FSPConfigurationRepository;
import ph.gov.dsr.payment.service.impl.FSPServiceRegistryImpl;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FSPServiceRegistry
 */
@ExtendWith(MockitoExtension.class)
class FSPServiceRegistryTest {

    @Mock
    private FSPConfigurationRepository fspConfigurationRepository;

    @Mock
    private FSPService mockFspService1;

    @Mock
    private FSPService mockFspService2;

    @InjectMocks
    private FSPServiceRegistryImpl fspServiceRegistry;

    private FSPConfiguration testFspConfig;
    private FSPPaymentRequest testPaymentRequest;

    @BeforeEach
    void setUp() {
        testFspConfig = FSPConfiguration.builder()
            .fspCode("LBP")
            .fspName("Land Bank of the Philippines")
            .apiBaseUrl("https://api.lbp.gov.ph")
            .apiKey("test-api-key")
            .isActive(true)
            .build();

        testPaymentRequest = FSPPaymentRequest.builder()
            .amount(new BigDecimal("1400.00"))
            .recipientAccountNumber("1234567890")
            .beneficiaryName("Juan Dela Cruz")
            .internalReferenceNumber("PAY-2024-001")
            .build();

        // Setup mock FSP services
        when(mockFspService1.getFspCode()).thenReturn("LBP");
        when(mockFspService1.getSupportedPaymentMethods()).thenReturn(
            Set.of(Payment.PaymentMethod.BANK_TRANSFER));
        when(mockFspService1.supportsAmount(any(BigDecimal.class))).thenReturn(true);
        when(mockFspService1.isHealthy()).thenReturn(true);

        when(mockFspService2.getFspCode()).thenReturn("BPI");
        when(mockFspService2.getSupportedPaymentMethods()).thenReturn(
            Set.of(Payment.PaymentMethod.BANK_TRANSFER));
        when(mockFspService2.supportsAmount(any(BigDecimal.class))).thenReturn(true);
        when(mockFspService2.isHealthy()).thenReturn(true);

        // Initialize the registry with mock services
        List<FSPService> fspServices = Arrays.asList(mockFspService1, mockFspService2);
        fspServiceRegistry = new FSPServiceRegistryImpl(fspConfigurationRepository, fspServices);
    }

    @Test
    void registerFSPService_Success() {
        // Act
        fspServiceRegistry.registerFSPService(mockFspService1);

        // Assert
        FSPService retrievedService = fspServiceRegistry.getFSPService("LBP");
        assertNotNull(retrievedService);
        assertEquals("LBP", retrievedService.getFspCode());
    }

    @Test
    void getFSPService_ExistingService_ReturnsService() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act
        FSPService result = fspServiceRegistry.getFSPService("LBP");

        // Assert
        assertNotNull(result);
        assertEquals("LBP", result.getFspCode());
    }

    @Test
    void getFSPService_NonExistentService_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fspServiceRegistry.getFSPService("NONEXISTENT"));
    }

    @Test
    void getAllFSPServices_ReturnsAllServices() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);
        fspServiceRegistry.registerFSPService(mockFspService2);

        // Act
        List<FSPService> result = fspServiceRegistry.getAllFSPServices();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getFSPHealthStatus_HealthyService_ReturnsTrue() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act
        Map<String, Boolean> result = fspServiceRegistry.getFSPHealthStatus();

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("LBP"));
    }

    @Test
    void supportsPaymentMethod_SupportedMethod_ReturnsTrue() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act
        boolean result = fspServiceRegistry.supportsPaymentMethod("LBP", Payment.PaymentMethod.BANK_TRANSFER);

        // Assert
        assertTrue(result);
    }

    @Test
    void submitPayment_Success() {
        // Arrange
        FSPPaymentResponse expectedResponse = FSPPaymentResponse.builder()
            .fspReferenceNumber("FSP-REF-001")
            .status(FSPPaymentResponse.FSPPaymentStatus.SUBMITTED)
            .statusMessage("Payment submitted successfully")
            .success(true)
            .build();

        when(fspConfigurationRepository.findByFspCode("LBP")).thenReturn(Optional.of(testFspConfig));
        when(mockFspService1.submitPayment(any(FSPPaymentRequest.class), any(FSPConfiguration.class)))
            .thenReturn(expectedResponse);

        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act
        FSPPaymentResponse result = fspServiceRegistry.submitPayment("LBP", testPaymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals("FSP-REF-001", result.getFspReferenceNumber());
        assertEquals(FSPPaymentResponse.FSPPaymentStatus.SUBMITTED, result.getStatus());
    }

    @Test
    void submitPayment_UnhealthyService_ThrowsException() {
        // Arrange
        when(mockFspService1.isHealthy()).thenReturn(false);
        when(fspConfigurationRepository.findByFspCode("LBP")).thenReturn(Optional.of(testFspConfig));
        
        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            fspServiceRegistry.submitPayment("LBP", testPaymentRequest));
    }

    @Test
    void checkPaymentStatus_Success() {
        // Arrange
        FSPStatusResponse expectedResponse = FSPStatusResponse.builder()
            .fspReferenceNumber("FSP-REF-001")
            .status(FSPPaymentResponse.FSPPaymentStatus.COMPLETED)
            .statusMessage("Payment completed")
            .success(true)
            .build();

        when(fspConfigurationRepository.findByFspCode("LBP")).thenReturn(Optional.of(testFspConfig));
        when(mockFspService1.checkPaymentStatus(anyString(), any(FSPConfiguration.class)))
            .thenReturn(expectedResponse);

        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act
        FSPStatusResponse result = fspServiceRegistry.checkPaymentStatus("LBP", "FSP-REF-001");

        // Assert
        assertNotNull(result);
        assertEquals("FSP-REF-001", result.getFspReferenceNumber());
        assertEquals(FSPPaymentResponse.FSPPaymentStatus.COMPLETED, result.getStatus());
    }

    @Test
    void getBestFSP_WithValidCriteria_ReturnsOptimalFSP() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);
        fspServiceRegistry.registerFSPService(mockFspService2);

        // Act
        String result = fspServiceRegistry.getBestFSP(
            Payment.PaymentMethod.BANK_TRANSFER, new BigDecimal("1400.00"));

        // Assert
        assertNotNull(result);
        assertTrue(Arrays.asList("LBP", "BPI").contains(result));
    }

    @Test
    void getBestFSP_NoSuitableFSP_ThrowsException() {
        // Arrange
        when(mockFspService1.getSupportedPaymentMethods()).thenReturn(
            Set.of(Payment.PaymentMethod.CASH_PICKUP));
        when(mockFspService2.getSupportedPaymentMethods()).thenReturn(
            Set.of(Payment.PaymentMethod.CASH_PICKUP));

        fspServiceRegistry.registerFSPService(mockFspService1);
        fspServiceRegistry.registerFSPService(mockFspService2);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            fspServiceRegistry.getBestFSP(Payment.PaymentMethod.BANK_TRANSFER, new BigDecimal("1400.00")));
    }

    @Test
    void performHealthCheck_UpdatesHealthStatus() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);

        // Act
        fspServiceRegistry.performHealthCheck();

        // Assert
        // Health check should have been performed
        verify(mockFspService1, atLeastOnce()).isHealthy();
    }

    @Test
    void getFSPHealthStatus_ReturnsCorrectStatus() {
        // Arrange
        fspServiceRegistry.registerFSPService(mockFspService1);
        fspServiceRegistry.registerFSPService(mockFspService2);

        // Act
        Map<String, Boolean> result = fspServiceRegistry.getFSPHealthStatus();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("LBP"));
        assertTrue(result.containsKey("BPI"));
    }
}
