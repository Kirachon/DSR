package ph.gov.dsr.payment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ph.gov.dsr.payment.dto.FSPPaymentRequest;
import ph.gov.dsr.payment.dto.FSPPaymentResponse;
import ph.gov.dsr.payment.dto.FSPStatusResponse;
import ph.gov.dsr.payment.entity.FSPConfiguration;
import ph.gov.dsr.payment.entity.Payment;
import ph.gov.dsr.payment.repository.FSPConfigurationRepository;
import ph.gov.dsr.payment.service.FSPService;
import ph.gov.dsr.payment.service.FSPServiceRegistry;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of FSPServiceRegistry
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FSPServiceRegistryImpl implements FSPServiceRegistry {

    private final FSPConfigurationRepository fspConfigurationRepository;
    private final List<FSPService> fspServices;
    private final Map<String, FSPService> fspServiceMap = new ConcurrentHashMap<>();
    private final Map<String, Boolean> healthStatusMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeFSPServices() {
        log.info("Initializing FSP services");
        
        for (FSPService fspService : fspServices) {
            registerFSPService(fspService);
        }
        
        // Perform initial health check
        performHealthCheck();
        
        log.info("Initialized {} FSP services", fspServiceMap.size());
    }

    @Override
    public void registerFSPService(FSPService fspService) {
        String fspCode = fspService.getFspCode();
        fspServiceMap.put(fspCode, fspService);
        healthStatusMap.put(fspCode, false); // Will be updated by health check
        
        log.info("Registered FSP service: {}", fspCode);
    }

    @Override
    public FSPService getFSPService(String fspCode) {
        FSPService service = fspServiceMap.get(fspCode);
        if (service == null) {
            throw new RuntimeException("FSP service not found: " + fspCode);
        }
        return service;
    }

    @Override
    public List<FSPService> getAllFSPServices() {
        return new ArrayList<>(fspServiceMap.values());
    }

    @Override
    public List<FSPService> getHealthyFSPServices() {
        return fspServiceMap.entrySet().stream()
            .filter(entry -> Boolean.TRUE.equals(healthStatusMap.get(entry.getKey())))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    @Override
    public FSPPaymentResponse submitPayment(String fspCode, FSPPaymentRequest request) {
        log.info("Submitting payment to FSP: {} for amount: {}", fspCode, request.getAmount());

        try {
            FSPService fspService = getFSPService(fspCode);
            FSPConfiguration config = getFSPConfiguration(fspCode);

            if (!fspService.isHealthy()) {
                throw new RuntimeException("FSP service is not healthy: " + fspCode);
            }

            FSPPaymentResponse response = fspService.submitPayment(request, config);
            
            log.info("Payment submitted to FSP: {}, reference: {}, status: {}", 
                    fspCode, response.getFspReferenceNumber(), response.getStatus());
            
            return response;

        } catch (Exception e) {
            log.error("Error submitting payment to FSP: {}", fspCode, e);
            
            // Mark FSP as unhealthy if submission fails
            healthStatusMap.put(fspCode, false);
            
            throw new RuntimeException("Failed to submit payment to FSP: " + e.getMessage(), e);
        }
    }

    @Override
    public FSPStatusResponse checkPaymentStatus(String fspCode, String fspReferenceNumber) {
        log.debug("Checking payment status with FSP: {} for reference: {}", fspCode, fspReferenceNumber);

        try {
            FSPService fspService = getFSPService(fspCode);
            FSPConfiguration config = getFSPConfiguration(fspCode);

            FSPStatusResponse response = fspService.checkPaymentStatus(fspReferenceNumber, config);
            
            log.debug("Payment status checked with FSP: {}, status: {}", fspCode, response.getStatus());
            
            return response;

        } catch (Exception e) {
            log.error("Error checking payment status with FSP: {}", fspCode, e);
            throw new RuntimeException("Failed to check payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public FSPPaymentResponse cancelPayment(String fspCode, String fspReferenceNumber) {
        log.info("Cancelling payment with FSP: {} for reference: {}", fspCode, fspReferenceNumber);

        try {
            FSPService fspService = getFSPService(fspCode);
            FSPConfiguration config = getFSPConfiguration(fspCode);

            FSPPaymentResponse response = fspService.cancelPayment(fspReferenceNumber, config);
            
            log.info("Payment cancelled with FSP: {}, status: {}", fspCode, response.getStatus());
            
            return response;

        } catch (Exception e) {
            log.error("Error cancelling payment with FSP: {}", fspCode, e);
            throw new RuntimeException("Failed to cancel payment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsPaymentMethod(String fspCode, Payment.PaymentMethod paymentMethod) {
        try {
            FSPService fspService = getFSPService(fspCode);
            return fspService.getSupportedPaymentMethods().contains(paymentMethod);
        } catch (Exception e) {
            log.warn("Error checking payment method support for FSP: {}", fspCode, e);
            return false;
        }
    }

    @Override
    public boolean supportsAmount(String fspCode, BigDecimal amount) {
        try {
            FSPService fspService = getFSPService(fspCode);
            return fspService.supportsAmount(amount);
        } catch (Exception e) {
            log.warn("Error checking amount support for FSP: {}", fspCode, e);
            return false;
        }
    }

    @Override
    public Map<String, Boolean> getFSPHealthStatus() {
        return new HashMap<>(healthStatusMap);
    }

    @Override
    public void performHealthCheck() {
        log.info("Performing health check on all FSP services");

        for (Map.Entry<String, FSPService> entry : fspServiceMap.entrySet()) {
            String fspCode = entry.getKey();
            FSPService fspService = entry.getValue();

            try {
                boolean isHealthy = fspService.isHealthy();
                healthStatusMap.put(fspCode, isHealthy);
                
                // Update FSP configuration health status
                updateFSPHealthStatus(fspCode, isHealthy);
                
                log.debug("FSP health check - {}: {}", fspCode, isHealthy ? "HEALTHY" : "UNHEALTHY");
                
            } catch (Exception e) {
                log.warn("Error during health check for FSP: {}", fspCode, e);
                healthStatusMap.put(fspCode, false);
                updateFSPHealthStatus(fspCode, false);
            }
        }

        long healthyCount = healthStatusMap.values().stream()
            .mapToLong(healthy -> healthy ? 1 : 0)
            .sum();
        
        log.info("Health check completed - {}/{} FSP services are healthy", 
                healthyCount, fspServiceMap.size());
    }

    @Override
    public String getBestFSP(Payment.PaymentMethod paymentMethod, BigDecimal amount) {
        log.debug("Finding best FSP for payment method: {} and amount: {}", paymentMethod, amount);

        List<String> candidateFSPs = fspServiceMap.entrySet().stream()
            .filter(entry -> Boolean.TRUE.equals(healthStatusMap.get(entry.getKey())))
            .filter(entry -> entry.getValue().getSupportedPaymentMethods().contains(paymentMethod))
            .filter(entry -> entry.getValue().supportsAmount(amount))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (candidateFSPs.isEmpty()) {
            throw new RuntimeException("No healthy FSP found for payment method: " + paymentMethod + 
                                     " and amount: " + amount);
        }

        // For now, return the first candidate
        // In production, this could implement more sophisticated selection logic
        // based on fees, success rates, processing times, etc.
        String selectedFSP = candidateFSPs.get(0);
        
        log.debug("Selected FSP: {} for payment method: {} and amount: {}", 
                selectedFSP, paymentMethod, amount);
        
        return selectedFSP;
    }

    private FSPConfiguration getFSPConfiguration(String fspCode) {
        return fspConfigurationRepository.findByFspCode(fspCode)
            .orElseThrow(() -> new RuntimeException("FSP configuration not found: " + fspCode));
    }

    private void updateFSPHealthStatus(String fspCode, boolean isHealthy) {
        try {
            Optional<FSPConfiguration> configOpt = fspConfigurationRepository.findByFspCode(fspCode);
            if (configOpt.isPresent()) {
                FSPConfiguration config = configOpt.get();
                config.setHealthStatus(isHealthy ? "HEALTHY" : "UNHEALTHY");
                config.setLastHealthCheck(LocalDateTime.now());
                fspConfigurationRepository.save(config);
            }
        } catch (Exception e) {
            log.warn("Error updating FSP health status for: {}", fspCode, e);
        }
    }
}
