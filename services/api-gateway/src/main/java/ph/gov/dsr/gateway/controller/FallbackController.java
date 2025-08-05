package ph.gov.dsr.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker
 * Provides fallback responses when services are unavailable
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/registration")
    public ResponseEntity<Map<String, Object>> registrationFallback() {
        return createFallbackResponse("Registration Service", 
            "Registration service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/data-management")
    public ResponseEntity<Map<String, Object>> dataManagementFallback() {
        return createFallbackResponse("Data Management Service", 
            "Data management service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/eligibility")
    public ResponseEntity<Map<String, Object>> eligibilityFallback() {
        return createFallbackResponse("Eligibility Service", 
            "Eligibility service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return createFallbackResponse("Payment Service", 
            "Payment service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/interop")
    public ResponseEntity<Map<String, Object>> interopFallback() {
        return createFallbackResponse("Interoperability Service", 
            "Interoperability service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/grievance")
    public ResponseEntity<Map<String, Object>> grievanceFallback() {
        return createFallbackResponse("Grievance Service", 
            "Grievance service is temporarily unavailable. Please try again later.");
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analyticsFallback() {
        return createFallbackResponse("Analytics Service", 
            "Analytics service is temporarily unavailable. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("service", serviceName);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
