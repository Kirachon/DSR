package ph.gov.dsr.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Production Data Aggregation Service for analytics
 * Provides real-time analytics data from microservice API calls
 *
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-25
 */
@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DataAggregationService {

    private final RestTemplate restTemplate;

    @Value("${dsr.services.registration.url:http://localhost:8081}")
    private String registrationServiceUrl;

    @Value("${dsr.services.payment.url:http://localhost:8082}")
    private String paymentServiceUrl;

    @Value("${dsr.services.eligibility.url:http://localhost:8083}")
    private String eligibilityServiceUrl;

    @Value("${dsr.services.data-management.url:http://localhost:8084}")
    private String dataManagementServiceUrl;

    @Value("${dsr.services.grievance.url:http://localhost:8085}")
    private String grievanceServiceUrl;

    @Value("${dsr.services.interoperability.url:http://localhost:8087}")
    private String interoperabilityServiceUrl;

    @Cacheable(value = "totalBeneficiaries", unless = "#result == null")
    public Long getTotalBeneficiaries() {
        try {
            log.debug("Fetching total beneficiaries from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/total-households",
                Map.class);

            if (response != null && response.containsKey("totalHouseholds")) {
                return ((Number) response.get("totalHouseholds")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch total beneficiaries from registration service: {}", e.getMessage());
        }

        // Fallback to reasonable default
        return 0L;
    }

    @Cacheable(value = "totalPayments", unless = "#result == null")
    public Long getTotalPayments() {
        try {
            log.debug("Fetching total payments from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/total-count",
                Map.class);

            if (response != null && response.containsKey("totalPayments")) {
                return ((Number) response.get("totalPayments")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch total payments from payment service: {}", e.getMessage());
        }

        return 0L;
    }

    @Cacheable(value = "totalPaymentAmount", unless = "#result == null")
    public Double getTotalPaymentAmount() {
        try {
            log.debug("Fetching total payment amount from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/total-amount",
                Map.class);

            if (response != null && response.containsKey("totalAmount")) {
                return ((Number) response.get("totalAmount")).doubleValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch total payment amount from payment service: {}", e.getMessage());
        }

        return 0.0;
    }

    @Cacheable(value = "activeProgramCount", unless = "#result == null")
    public Integer getActiveProgramCount() {
        try {
            log.debug("Fetching active program count from eligibility service");
            Map<String, Object> response = restTemplate.getForObject(
                eligibilityServiceUrl + "/api/v1/programs/statistics/active-count",
                Map.class);

            if (response != null && response.containsKey("activePrograms")) {
                return ((Number) response.get("activePrograms")).intValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch active program count from eligibility service: {}", e.getMessage());
        }

        return 0;
    }

    @Cacheable(value = "registrationTrend", unless = "#result == null")
    public Map<String, Object> getRegistrationTrend() {
        try {
            log.debug("Fetching registration trend from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/trend",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch registration trend from registration service: {}", e.getMessage());
        }

        // Fallback data
        Map<String, Object> trend = new HashMap<>();
        trend.put("trend", "stable");
        trend.put("percentage", 0.0);
        return trend;
    }

    @Cacheable(value = "paymentTrend", unless = "#result == null")
    public Map<String, Object> getPaymentTrend() {
        try {
            log.debug("Fetching payment trend from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/trend",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch payment trend from payment service: {}", e.getMessage());
        }

        // Fallback data
        Map<String, Object> trend = new HashMap<>();
        trend.put("trend", "stable");
        trend.put("percentage", 0.0);
        return trend;
    }

    @Cacheable(value = "systemUptime", unless = "#result == null")
    public Double getSystemUptime() {
        try {
            log.debug("Fetching system uptime from interoperability service");
            Map<String, Object> response = restTemplate.getForObject(
                interoperabilityServiceUrl + "/api/v1/systems/statistics/uptime",
                Map.class);

            if (response != null && response.containsKey("uptime")) {
                return ((Number) response.get("uptime")).doubleValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch system uptime from interoperability service: {}", e.getMessage());
        }

        return 99.0; // Default uptime
    }

    @Cacheable(value = "averageProcessingTime", unless = "#result == null")
    public Double getAverageProcessingTime() {
        try {
            log.debug("Fetching average processing time from data management service");
            Map<String, Object> response = restTemplate.getForObject(
                dataManagementServiceUrl + "/api/v1/ingestion/statistics/average-processing-time",
                Map.class);

            if (response != null && response.containsKey("averageProcessingTime")) {
                return ((Number) response.get("averageProcessingTime")).doubleValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch average processing time from data management service: {}", e.getMessage());
        }

        return 0.0;
    }

    @Cacheable(value = "pendingRegistrations", unless = "#result == null")
    public Long getPendingRegistrations() {
        try {
            log.debug("Fetching pending registrations from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/pending-count",
                Map.class);

            if (response != null && response.containsKey("pendingCount")) {
                return ((Number) response.get("pendingCount")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch pending registrations from registration service: {}", e.getMessage());
        }

        return 0L;
    }

    @Cacheable(value = "pendingPayments", unless = "#result == null")
    public Long getPendingPayments() {
        try {
            log.debug("Fetching pending payments from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/pending-count",
                Map.class);

            if (response != null && response.containsKey("pendingCount")) {
                return ((Number) response.get("pendingCount")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch pending payments from payment service: {}", e.getMessage());
        }

        return 0L;
    }

    @Cacheable(value = "openGrievances", unless = "#result == null")
    public Long getOpenGrievances() {
        try {
            log.debug("Fetching open grievances from grievance service");
            Map<String, Object> response = restTemplate.getForObject(
                grievanceServiceUrl + "/api/v1/grievances/statistics/open-count",
                Map.class);

            if (response != null && response.containsKey("openCount")) {
                return ((Number) response.get("openCount")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch open grievances from grievance service: {}", e.getMessage());
        }

        return 0L;
    }

    @Cacheable(value = "systemAlerts", unless = "#result == null")
    public Integer getSystemAlerts() {
        try {
            log.debug("Fetching system alerts from interoperability service");
            Map<String, Object> response = restTemplate.getForObject(
                interoperabilityServiceUrl + "/api/v1/systems/statistics/alerts-count",
                Map.class);

            if (response != null && response.containsKey("alertsCount")) {
                return ((Number) response.get("alertsCount")).intValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch system alerts from interoperability service: {}", e.getMessage());
        }

        return 0;
    }

    @Cacheable(value = "processingQueueStatus", unless = "#result == null")
    public Map<String, Object> getProcessingQueueStatus() {
        Map<String, Object> status = new HashMap<>();

        try {
            // Get registration queue status
            Map<String, Object> regResponse = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/queue-status",
                Map.class);
            status.put("registration", regResponse != null ? regResponse.getOrDefault("queueSize", 0) : 0);

            // Get payment queue status
            Map<String, Object> payResponse = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/queue-status",
                Map.class);
            status.put("payment", payResponse != null ? payResponse.getOrDefault("queueSize", 0) : 0);

            // Get eligibility queue status
            Map<String, Object> eligResponse = restTemplate.getForObject(
                eligibilityServiceUrl + "/api/v1/assessments/statistics/queue-status",
                Map.class);
            status.put("eligibility", eligResponse != null ? eligResponse.getOrDefault("queueSize", 0) : 0);

        } catch (Exception e) {
            log.warn("Failed to fetch processing queue status: {}", e.getMessage());
            status.put("registration", 0);
            status.put("payment", 0);
            status.put("eligibility", 0);
        }

        return status;
    }

    @Cacheable(value = "serviceStatus", unless = "#result == null")
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();

        // Check health of each service
        status.put("registration", checkServiceHealth(registrationServiceUrl));
        status.put("payment", checkServiceHealth(paymentServiceUrl));
        status.put("eligibility", checkServiceHealth(eligibilityServiceUrl));
        status.put("grievance", checkServiceHealth(grievanceServiceUrl));
        status.put("dataManagement", checkServiceHealth(dataManagementServiceUrl));
        status.put("interoperability", checkServiceHealth(interoperabilityServiceUrl));

        return status;
    }

    @Cacheable(value = "dailyRegistrations", unless = "#result == null")
    public Map<String, Object> getDailyRegistrations() {
        try {
            log.debug("Fetching daily registrations from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/daily",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch daily registrations: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "registrationsByRegion", unless = "#result == null")
    public Map<String, Object> getRegistrationsByRegion() {
        try {
            log.debug("Fetching registrations by region from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/by-region",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch registrations by region: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "registrationsByProgram", unless = "#result == null")
    public Map<String, Object> getRegistrationsByProgram() {
        try {
            log.debug("Fetching registrations by program from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/by-program",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch registrations by program: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "registrationStatusBreakdown", unless = "#result == null")
    public Map<String, Object> getRegistrationStatusBreakdown() {
        try {
            log.debug("Fetching registration status breakdown from registration service");
            Map<String, Object> response = restTemplate.getForObject(
                registrationServiceUrl + "/api/v1/registrations/statistics/status-breakdown",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch registration status breakdown: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "dailyPayments", unless = "#result == null")
    public Map<String, Object> getDailyPayments() {
        try {
            log.debug("Fetching daily payments from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/daily",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch daily payments: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "paymentsByProgram", unless = "#result == null")
    public Map<String, Object> getPaymentsByProgram() {
        try {
            log.debug("Fetching payments by program from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/by-program",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch payments by program: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "paymentsByProvider", unless = "#result == null")
    public Map<String, Object> getPaymentsByProvider() {
        try {
            log.debug("Fetching payments by provider from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/by-provider",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch payments by provider: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "paymentStatusBreakdown", unless = "#result == null")
    public Map<String, Object> getPaymentStatusBreakdown() {
        try {
            log.debug("Fetching payment status breakdown from payment service");
            Map<String, Object> response = restTemplate.getForObject(
                paymentServiceUrl + "/api/v1/payments/statistics/status-breakdown",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch payment status breakdown: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "grievancesByCategory", unless = "#result == null")
    public Map<String, Object> getGrievancesByCategory() {
        try {
            log.debug("Fetching grievances by category from grievance service");
            Map<String, Object> response = restTemplate.getForObject(
                grievanceServiceUrl + "/api/v1/grievances/statistics/by-category",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch grievances by category: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "grievancesByStatus", unless = "#result == null")
    public Map<String, Object> getGrievancesByStatus() {
        try {
            log.debug("Fetching grievances by status from grievance service");
            Map<String, Object> response = restTemplate.getForObject(
                grievanceServiceUrl + "/api/v1/grievances/statistics/by-status",
                Map.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch grievances by status: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    @Cacheable(value = "averageGrievanceResolutionTime", unless = "#result == null")
    public Double getAverageGrievanceResolutionTime() {
        try {
            log.debug("Fetching average grievance resolution time from grievance service");
            Map<String, Object> response = restTemplate.getForObject(
                grievanceServiceUrl + "/api/v1/grievances/statistics/average-resolution-time",
                Map.class);

            if (response != null && response.containsKey("averageResolutionTime")) {
                return ((Number) response.get("averageResolutionTime")).doubleValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch average grievance resolution time: {}", e.getMessage());
        }

        return 0.0;
    }

    @Cacheable(value = "overdueGrievances", unless = "#result == null")
    public Long getOverdueGrievances() {
        try {
            log.debug("Fetching overdue grievances from grievance service");
            Map<String, Object> response = restTemplate.getForObject(
                grievanceServiceUrl + "/api/v1/grievances/statistics/overdue-count",
                Map.class);

            if (response != null && response.containsKey("overdueCount")) {
                return ((Number) response.get("overdueCount")).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch overdue grievances: {}", e.getMessage());
        }

        return 0L;
    }

    /**
     * Check health status of a service
     */
    private String checkServiceHealth(String serviceUrl) {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                serviceUrl + "/api/v1/health",
                Map.class);

            if (response != null && "UP".equals(response.get("status"))) {
                return "healthy";
            }
        } catch (Exception e) {
            log.debug("Service health check failed for {}: {}", serviceUrl, e.getMessage());
        }

        return "unhealthy";
    }
}
