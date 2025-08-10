package ph.gov.dsr.interoperability.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.gov.dsr.interoperability.entity.ServiceDeliveryRecord;
import ph.gov.dsr.interoperability.repository.ServiceDeliveryRecordRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing service delivery records and tracking
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceDeliveryService {

    private final ServiceDeliveryRecordRepository deliveryRepository;

    /**
     * Record a new service delivery
     */
    @Transactional
    public ServiceDeliveryRecord recordServiceDelivery(ServiceDeliveryRecord deliveryRecord) {
        log.info("Recording service delivery for beneficiary: {} service: {}", 
                deliveryRecord.getBeneficiaryPsn(), deliveryRecord.getServiceType());
        
        // Generate transaction ID if not provided
        if (deliveryRecord.getTransactionId() == null || deliveryRecord.getTransactionId().isEmpty()) {
            deliveryRecord.setTransactionId(generateTransactionId());
        }
        
        // Set default values
        if (deliveryRecord.getServiceDate() == null) {
            deliveryRecord.setServiceDate(LocalDateTime.now());
        }
        
        if (deliveryRecord.getDeliveryStatus() == null) {
            deliveryRecord.setDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.PENDING);
        }
        
        // Check for duplicates
        if (isDuplicateDelivery(deliveryRecord)) {
            deliveryRecord.setIsDuplicate(true);
            log.warn("Duplicate service delivery detected for transaction: {}", deliveryRecord.getTransactionId());
        }
        
        return deliveryRepository.save(deliveryRecord);
    }

    /**
     * Update service delivery status
     */
    @Transactional
    public ServiceDeliveryRecord updateDeliveryStatus(UUID deliveryId, 
                                                     ServiceDeliveryRecord.DeliveryStatus status,
                                                     String notes) {
        log.info("Updating delivery status for record: {} to: {}", deliveryId, status);
        
        Optional<ServiceDeliveryRecord> recordOpt = deliveryRepository.findById(deliveryId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Service delivery record not found: " + deliveryId);
        }
        
        ServiceDeliveryRecord record = recordOpt.get();
        record.setDeliveryStatus(status);
        record.setDeliveryNotes(notes);
        
        if (status == ServiceDeliveryRecord.DeliveryStatus.DELIVERED) {
            record.setDeliveryConfirmationDate(LocalDateTime.now());
        }
        
        return deliveryRepository.save(record);
    }

    /**
     * Mark delivery as completed
     */
    @Transactional
    public ServiceDeliveryRecord markAsDelivered(UUID deliveryId, String confirmedBy, String verificationMethod) {
        log.info("Marking delivery as completed: {}", deliveryId);
        
        Optional<ServiceDeliveryRecord> recordOpt = deliveryRepository.findById(deliveryId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Service delivery record not found: " + deliveryId);
        }
        
        ServiceDeliveryRecord record = recordOpt.get();
        record.markAsDelivered(confirmedBy, verificationMethod);
        
        return deliveryRepository.save(record);
    }

    /**
     * Mark delivery as failed
     */
    @Transactional
    public ServiceDeliveryRecord markAsFailed(UUID deliveryId, String failureReason) {
        log.info("Marking delivery as failed: {} reason: {}", deliveryId, failureReason);
        
        Optional<ServiceDeliveryRecord> recordOpt = deliveryRepository.findById(deliveryId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Service delivery record not found: " + deliveryId);
        }
        
        ServiceDeliveryRecord record = recordOpt.get();
        record.markAsFailed(failureReason);
        
        return deliveryRepository.save(record);
    }

    /**
     * Get service delivery by transaction ID
     */
    public Optional<ServiceDeliveryRecord> getByTransactionId(String transactionId) {
        log.debug("Retrieving service delivery by transaction ID: {}", transactionId);
        return deliveryRepository.findByTransactionId(transactionId);
    }

    /**
     * Get service delivery history for a beneficiary
     */
    public List<ServiceDeliveryRecord> getDeliveryHistory(String beneficiaryPsn) {
        log.debug("Retrieving delivery history for PSN: {}", beneficiaryPsn);
        return deliveryRepository.findByBeneficiaryPsnOrderByServiceDateDesc(beneficiaryPsn);
    }

    /**
     * Get service deliveries by household
     */
    public List<ServiceDeliveryRecord> getDeliveriesByHousehold(UUID householdId) {
        log.debug("Retrieving deliveries for household: {}", householdId);
        return deliveryRepository.findByHouseholdIdOrderByServiceDateDesc(householdId);
    }

    /**
     * Get service deliveries by program
     */
    public List<ServiceDeliveryRecord> getDeliveriesByProgram(String programCode) {
        log.debug("Retrieving deliveries for program: {}", programCode);
        return deliveryRepository.findByProgramCodeOrderByServiceDateDesc(programCode);
    }

    /**
     * Get pending deliveries
     */
    public List<ServiceDeliveryRecord> getPendingDeliveries() {
        log.debug("Retrieving pending deliveries");
        return deliveryRepository.findByDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.PENDING);
    }

    /**
     * Get failed deliveries
     */
    public List<ServiceDeliveryRecord> getFailedDeliveries() {
        log.debug("Retrieving failed deliveries");
        return deliveryRepository.findByDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.FAILED);
    }

    /**
     * Get overdue deliveries
     */
    public List<ServiceDeliveryRecord> getOverdueDeliveries() {
        log.debug("Retrieving overdue deliveries");
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
        return deliveryRepository.findOverdueDeliveries(cutoffDate);
    }

    /**
     * Get deliveries that can be retried
     */
    public List<ServiceDeliveryRecord> getRetryableDeliveries() {
        log.debug("Retrieving retryable deliveries");
        return deliveryRepository.findRetryableDeliveries();
    }

    /**
     * Retry failed delivery
     */
    @Transactional
    public ServiceDeliveryRecord retryDelivery(UUID deliveryId) {
        log.info("Retrying delivery: {}", deliveryId);
        
        Optional<ServiceDeliveryRecord> recordOpt = deliveryRepository.findById(deliveryId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Service delivery record not found: " + deliveryId);
        }
        
        ServiceDeliveryRecord record = recordOpt.get();
        
        if (!record.canRetry()) {
            throw new IllegalStateException("Delivery cannot be retried: " + deliveryId);
        }
        
        record.setDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.PENDING);
        record.setRetryCount((record.getRetryCount() != null ? record.getRetryCount() : 0) + 1);
        record.setLastRetryDate(LocalDateTime.now());
        record.setNextRetryDate(null);
        record.setFailureReason(null);
        
        return deliveryRepository.save(record);
    }

    /**
     * Get delivery statistics
     */
    public Map<String, Object> getDeliveryStatistics() {
        log.debug("Calculating delivery statistics");
        
        long totalDeliveries = deliveryRepository.count();
        long pendingDeliveries = deliveryRepository.countByDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.PENDING);
        long completedDeliveries = deliveryRepository.countByDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.DELIVERED);
        long failedDeliveries = deliveryRepository.countByDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus.FAILED);
        
        double successRate = totalDeliveries > 0 ? (double) completedDeliveries / totalDeliveries * 100 : 0.0;
        double failureRate = totalDeliveries > 0 ? (double) failedDeliveries / totalDeliveries * 100 : 0.0;
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalDeliveries", totalDeliveries);
        statistics.put("pendingDeliveries", pendingDeliveries);
        statistics.put("completedDeliveries", completedDeliveries);
        statistics.put("failedDeliveries", failedDeliveries);
        statistics.put("successRate", Math.round(successRate * 100.0) / 100.0);
        statistics.put("failureRate", Math.round(failureRate * 100.0) / 100.0);
        statistics.put("generatedAt", LocalDateTime.now());
        
        return statistics;
    }

    /**
     * Get delivery statistics by program
     */
    public Map<String, Map<String, Object>> getDeliveryStatisticsByProgram() {
        log.debug("Calculating delivery statistics by program");
        
        List<ServiceDeliveryRecord> allDeliveries = deliveryRepository.findAll();
        
        return allDeliveries.stream()
                .collect(Collectors.groupingBy(ServiceDeliveryRecord::getProgramCode))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<ServiceDeliveryRecord> programDeliveries = entry.getValue();
                            long total = programDeliveries.size();
                            long completed = programDeliveries.stream()
                                    .mapToLong(d -> d.getDeliveryStatus() == ServiceDeliveryRecord.DeliveryStatus.DELIVERED ? 1 : 0)
                                    .sum();
                            long failed = programDeliveries.stream()
                                    .mapToLong(d -> d.getDeliveryStatus() == ServiceDeliveryRecord.DeliveryStatus.FAILED ? 1 : 0)
                                    .sum();
                            
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("totalDeliveries", total);
                            stats.put("completedDeliveries", completed);
                            stats.put("failedDeliveries", failed);
                            stats.put("successRate", total > 0 ? (double) completed / total * 100 : 0.0);
                            return stats;
                        }
                ));
    }

    /**
     * Search service deliveries with filters
     */
    public Page<ServiceDeliveryRecord> searchDeliveries(String beneficiaryPsn,
                                                       String programCode,
                                                       String serviceType,
                                                       ServiceDeliveryRecord.DeliveryStatus status,
                                                       String deliveryMethod,
                                                       LocalDateTime fromDate,
                                                       LocalDateTime toDate,
                                                       Pageable pageable) {
        log.debug("Searching deliveries with filters");
        
        // This would be implemented with proper search functionality using Specifications
        // For now, returning all records with pagination
        return deliveryRepository.findAll(pageable);
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Check if delivery is duplicate
     */
    private boolean isDuplicateDelivery(ServiceDeliveryRecord deliveryRecord) {
        // Check for existing delivery with same beneficiary, service type, and recent date
        LocalDateTime recentDate = LocalDateTime.now().minusHours(1);
        
        return deliveryRepository.findByBeneficiaryPsnAndServiceTypeAndServiceDateAfter(
                deliveryRecord.getBeneficiaryPsn(),
                deliveryRecord.getServiceType(),
                recentDate
        ).stream().anyMatch(existing -> 
                !existing.getId().equals(deliveryRecord.getId()) &&
                existing.getProgramCode().equals(deliveryRecord.getProgramCode())
        );
    }

    /**
     * Bulk update delivery status
     */
    @Transactional
    public List<ServiceDeliveryRecord> bulkUpdateStatus(List<UUID> deliveryIds, 
                                                       ServiceDeliveryRecord.DeliveryStatus status,
                                                       String notes) {
        log.info("Bulk updating {} deliveries to status: {}", deliveryIds.size(), status);
        
        List<ServiceDeliveryRecord> records = deliveryRepository.findAllById(deliveryIds);
        
        records.forEach(record -> {
            record.setDeliveryStatus(status);
            record.setDeliveryNotes(notes);
            
            if (status == ServiceDeliveryRecord.DeliveryStatus.DELIVERED) {
                record.setDeliveryConfirmationDate(LocalDateTime.now());
            }
        });
        
        return deliveryRepository.saveAll(records);
    }

    /**
     * Get high-value transactions
     */
    public List<ServiceDeliveryRecord> getHighValueTransactions(BigDecimal threshold) {
        log.debug("Retrieving high-value transactions above: {}", threshold);
        return deliveryRepository.findHighValueTransactions(threshold);
    }

    /**
     * Get delivery performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        log.debug("Calculating delivery performance metrics");
        
        List<ServiceDeliveryRecord> recentDeliveries = deliveryRepository.findRecentDeliveries(
                LocalDateTime.now().minusDays(30));
        
        if (recentDeliveries.isEmpty()) {
            return Map.of("message", "No recent deliveries found");
        }
        
        double averageDeliveryTime = recentDeliveries.stream()
                .filter(d -> d.getDeliveryConfirmationDate() != null)
                .mapToDouble(d -> java.time.temporal.ChronoUnit.HOURS.between(
                        d.getServiceDate(), d.getDeliveryConfirmationDate()))
                .average()
                .orElse(0.0);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("averageDeliveryTimeHours", Math.round(averageDeliveryTime * 100.0) / 100.0);
        metrics.put("totalRecentDeliveries", recentDeliveries.size());
        metrics.put("periodDays", 30);
        metrics.put("calculatedAt", LocalDateTime.now());

        return metrics;
    }

    /**
     * Advanced service delivery tracking with real-time monitoring
     */
    public Map<String, Object> getAdvancedDeliveryTracking(String transactionId) {
        log.info("Getting advanced tracking for transaction: {}", transactionId);

        Optional<ServiceDeliveryRecord> recordOpt = deliveryRepository.findByTransactionId(transactionId);
        if (recordOpt.isEmpty()) {
            return Map.of("error", "Transaction not found: " + transactionId);
        }

        ServiceDeliveryRecord record = recordOpt.get();
        Map<String, Object> tracking = new HashMap<>();

        // Basic delivery information
        tracking.put("transactionId", record.getTransactionId());
        tracking.put("beneficiaryPsn", record.getBeneficiaryPsn());
        tracking.put("serviceType", record.getServiceType());
        tracking.put("programCode", record.getProgramCode());
        tracking.put("currentStatus", record.getDeliveryStatus());
        tracking.put("serviceDate", record.getServiceDate());
        tracking.put("deliveryMethod", record.getDeliveryMethod());
        tracking.put("serviceAmount", record.getServiceAmount());

        // Timeline tracking
        List<Map<String, Object>> timeline = buildDeliveryTimeline(record);
        tracking.put("timeline", timeline);

        // Performance metrics
        Map<String, Object> performance = calculateDeliveryPerformance(record);
        tracking.put("performance", performance);

        // Risk assessment
        Map<String, Object> riskAssessment = assessDeliveryRisk(record);
        tracking.put("riskAssessment", riskAssessment);

        // Related deliveries
        List<ServiceDeliveryRecord> relatedDeliveries = getRelatedDeliveries(record);
        tracking.put("relatedDeliveries", relatedDeliveries.size());

        // External system status
        Map<String, Object> externalStatus = checkExternalSystemStatus(record);
        tracking.put("externalSystemStatus", externalStatus);

        return tracking;
    }

    /**
     * Build comprehensive delivery timeline
     */
    private List<Map<String, Object>> buildDeliveryTimeline(ServiceDeliveryRecord record) {
        List<Map<String, Object>> timeline = new ArrayList<>();

        // Service initiation
        Map<String, Object> initiation = new HashMap<>();
        initiation.put("event", "SERVICE_INITIATED");
        initiation.put("timestamp", record.getServiceDate());
        initiation.put("status", "COMPLETED");
        initiation.put("description", "Service delivery initiated");
        timeline.add(initiation);

        // Processing events based on current status
        if (record.getDeliveryStatus() != ServiceDeliveryRecord.DeliveryStatus.PENDING) {
            Map<String, Object> processing = new HashMap<>();
            processing.put("event", "PROCESSING_STARTED");
            processing.put("timestamp", record.getServiceDate().plusMinutes(5)); // Estimated
            processing.put("status", "COMPLETED");
            processing.put("description", "Delivery processing started");
            timeline.add(processing);
        }

        // Delivery confirmation
        if (record.getDeliveryConfirmationDate() != null) {
            Map<String, Object> confirmation = new HashMap<>();
            confirmation.put("event", "DELIVERY_CONFIRMED");
            confirmation.put("timestamp", record.getDeliveryConfirmationDate());
            confirmation.put("status", "COMPLETED");
            confirmation.put("description", "Delivery confirmed by " + record.getConfirmedBy());
            confirmation.put("verificationMethod", record.getVerificationMethod());
            timeline.add(confirmation);
        }

        // Reconciliation
        if (record.getReconciliationDate() != null) {
            Map<String, Object> reconciliation = new HashMap<>();
            reconciliation.put("event", "RECONCILIATION_COMPLETED");
            reconciliation.put("timestamp", record.getReconciliationDate());
            reconciliation.put("status", "COMPLETED");
            reconciliation.put("description", "Financial reconciliation completed");
            reconciliation.put("reconciliationStatus", record.getReconciliationStatus());
            timeline.add(reconciliation);
        }

        // Sort timeline by timestamp
        timeline.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("timestamp");
            LocalDateTime timeB = (LocalDateTime) b.get("timestamp");
            return timeA.compareTo(timeB);
        });

        return timeline;
    }

    /**
     * Calculate delivery performance metrics
     */
    private Map<String, Object> calculateDeliveryPerformance(ServiceDeliveryRecord record) {
        Map<String, Object> performance = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        long hoursFromService = java.time.temporal.ChronoUnit.HOURS.between(record.getServiceDate(), now);

        performance.put("hoursFromInitiation", hoursFromService);

        if (record.getDeliveryConfirmationDate() != null) {
            long deliveryTimeHours = java.time.temporal.ChronoUnit.HOURS.between(
                record.getServiceDate(), record.getDeliveryConfirmationDate());
            performance.put("actualDeliveryTimeHours", deliveryTimeHours);

            // Calculate performance rating
            String performanceRating = calculatePerformanceRating(deliveryTimeHours, record.getServiceType());
            performance.put("performanceRating", performanceRating);
        } else {
            performance.put("actualDeliveryTimeHours", null);
            performance.put("performanceRating", "IN_PROGRESS");
        }

        // Expected delivery time based on service type
        int expectedHours = getExpectedDeliveryTime(record.getServiceType(), record.getDeliveryMethod());
        performance.put("expectedDeliveryTimeHours", expectedHours);

        // SLA compliance
        boolean slaCompliant = hoursFromService <= expectedHours || record.isDelivered();
        performance.put("slaCompliant", slaCompliant);

        return performance;
    }

    /**
     * Assess delivery risk factors
     */
    private Map<String, Object> assessDeliveryRisk(ServiceDeliveryRecord record) {
        Map<String, Object> risk = new HashMap<>();

        List<String> riskFactors = new ArrayList<>();
        String riskLevel = "LOW";

        // Time-based risk assessment
        LocalDateTime now = LocalDateTime.now();
        long hoursFromService = java.time.temporal.ChronoUnit.HOURS.between(record.getServiceDate(), now);
        int expectedHours = getExpectedDeliveryTime(record.getServiceType(), record.getDeliveryMethod());

        if (hoursFromService > expectedHours * 1.5) {
            riskFactors.add("OVERDUE_DELIVERY");
            riskLevel = "HIGH";
        } else if (hoursFromService > expectedHours) {
            riskFactors.add("APPROACHING_SLA_BREACH");
            riskLevel = "MEDIUM";
        }

        // Status-based risk assessment
        if (record.getDeliveryStatus() == ServiceDeliveryRecord.DeliveryStatus.FAILED) {
            riskFactors.add("DELIVERY_FAILED");
            riskLevel = "HIGH";
        } else if (record.getDeliveryStatus() == ServiceDeliveryRecord.DeliveryStatus.DISPUTED) {
            riskFactors.add("DELIVERY_DISPUTED");
            riskLevel = "HIGH";
        }

        // Amount-based risk assessment
        if (record.getServiceAmount() != null && record.getServiceAmount().compareTo(new BigDecimal("50000")) > 0) {
            riskFactors.add("HIGH_VALUE_TRANSACTION");
            if ("LOW".equals(riskLevel)) {
                riskLevel = "MEDIUM";
            }
        }

        // Location-based risk assessment
        if (record.getDeliveryLocation() != null && isHighRiskLocation(record.getDeliveryLocation())) {
            riskFactors.add("HIGH_RISK_LOCATION");
            if ("LOW".equals(riskLevel)) {
                riskLevel = "MEDIUM";
            }
        }

        risk.put("riskLevel", riskLevel);
        risk.put("riskFactors", riskFactors);
        risk.put("riskScore", calculateRiskScore(riskFactors, hoursFromService, expectedHours));

        return risk;
    }

    /**
     * Get related deliveries for pattern analysis
     */
    private List<ServiceDeliveryRecord> getRelatedDeliveries(ServiceDeliveryRecord record) {
        // Find deliveries for same beneficiary in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return deliveryRepository.findByBeneficiaryPsnAndServiceDateAfter(
            record.getBeneficiaryPsn(), thirtyDaysAgo);
    }

    /**
     * Check external system status for delivery tracking
     */
    private Map<String, Object> checkExternalSystemStatus(ServiceDeliveryRecord record) {
        Map<String, Object> status = new HashMap<>();

        // Determine relevant external systems based on delivery method
        List<String> relevantSystems = getRelevantExternalSystems(record.getDeliveryMethod());

        for (String system : relevantSystems) {
            Map<String, Object> systemStatus = new HashMap<>();
            systemStatus.put("status", "ACTIVE"); // This would be checked via API Gateway
            systemStatus.put("lastChecked", LocalDateTime.now());
            systemStatus.put("responseTime", "150ms"); // Mock data
            status.put(system, systemStatus);
        }

        return status;
    }

    /**
     * Calculate performance rating based on delivery time
     */
    private String calculatePerformanceRating(long actualHours, String serviceType) {
        int expectedHours = getExpectedDeliveryTime(serviceType, null);

        if (actualHours <= expectedHours * 0.5) {
            return "EXCELLENT";
        } else if (actualHours <= expectedHours) {
            return "GOOD";
        } else if (actualHours <= expectedHours * 1.5) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    /**
     * Get expected delivery time based on service type and method
     */
    private int getExpectedDeliveryTime(String serviceType, String deliveryMethod) {
        // Default SLA times in hours
        Map<String, Integer> serviceSLAs = Map.of(
            "CASH_TRANSFER", 24,
            "FOOD_ASSISTANCE", 48,
            "MEDICAL_ASSISTANCE", 12,
            "EDUCATION_ASSISTANCE", 72,
            "HOUSING_ASSISTANCE", 168, // 1 week
            "EMERGENCY_ASSISTANCE", 6
        );

        int baseTime = serviceSLAs.getOrDefault(serviceType, 48);

        // Adjust based on delivery method
        if ("BANK_TRANSFER".equals(deliveryMethod)) {
            return Math.max(baseTime / 2, 2); // Faster for bank transfers
        } else if ("CASH".equals(deliveryMethod)) {
            return baseTime;
        } else if ("IN_KIND".equals(deliveryMethod)) {
            return baseTime * 2; // Slower for physical goods
        }

        return baseTime;
    }

    /**
     * Check if location is considered high risk
     */
    private boolean isHighRiskLocation(String location) {
        // This would be implemented with actual risk assessment logic
        // For now, simple keyword matching
        String lowerLocation = location.toLowerCase();
        return lowerLocation.contains("remote") ||
               lowerLocation.contains("conflict") ||
               lowerLocation.contains("disaster");
    }

    /**
     * Calculate numerical risk score
     */
    private double calculateRiskScore(List<String> riskFactors, long actualHours, int expectedHours) {
        double score = 0.0;

        // Base score from risk factors
        score += riskFactors.size() * 10;

        // Time-based score
        if (actualHours > expectedHours) {
            score += (actualHours - expectedHours) * 2;
        }

        // Cap at 100
        return Math.min(score, 100.0);
    }

    /**
     * Get relevant external systems for delivery method
     */
    private List<String> getRelevantExternalSystems(String deliveryMethod) {
        if ("BANK_TRANSFER".equals(deliveryMethod)) {
            return List.of("BSP", "BANKING_SYSTEM");
        } else if ("CASH".equals(deliveryMethod)) {
            return List.of("PAYMENT_GATEWAY", "FSP");
        } else if ("IN_KIND".equals(deliveryMethod)) {
            return List.of("LOGISTICS_SYSTEM", "INVENTORY_SYSTEM");
        }
        return List.of("GENERAL_SYSTEM");
    }

    /**
     * Generate comprehensive delivery analytics
     */
    public Map<String, Object> generateDeliveryAnalytics(LocalDateTime fromDate, LocalDateTime toDate) {
        log.info("Generating delivery analytics from {} to {}", fromDate, toDate);

        List<ServiceDeliveryRecord> records = deliveryRepository.findByServiceDateBetween(fromDate, toDate);

        Map<String, Object> analytics = new HashMap<>();

        // Overall statistics
        analytics.put("totalDeliveries", records.size());
        analytics.put("totalAmount", records.stream()
            .filter(r -> r.getServiceAmount() != null)
            .map(ServiceDeliveryRecord::getServiceAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Status distribution
        Map<String, Long> statusDistribution = records.stream()
            .collect(Collectors.groupingBy(
                r -> r.getDeliveryStatus().toString(),
                Collectors.counting()));
        analytics.put("statusDistribution", statusDistribution);

        // Service type distribution
        Map<String, Long> serviceTypeDistribution = records.stream()
            .collect(Collectors.groupingBy(
                ServiceDeliveryRecord::getServiceType,
                Collectors.counting()));
        analytics.put("serviceTypeDistribution", serviceTypeDistribution);

        // Delivery method distribution
        Map<String, Long> deliveryMethodDistribution = records.stream()
            .filter(r -> r.getDeliveryMethod() != null)
            .collect(Collectors.groupingBy(
                ServiceDeliveryRecord::getDeliveryMethod,
                Collectors.counting()));
        analytics.put("deliveryMethodDistribution", deliveryMethodDistribution);

        // Performance metrics
        Map<String, Object> performanceMetrics = calculateOverallPerformanceMetrics(records);
        analytics.put("performanceMetrics", performanceMetrics);

        // Trend analysis
        Map<String, Object> trends = analyzeTrends(records);
        analytics.put("trends", trends);

        return analytics;
    }

    /**
     * Calculate overall performance metrics for a set of records
     */
    private Map<String, Object> calculateOverallPerformanceMetrics(List<ServiceDeliveryRecord> records) {
        Map<String, Object> metrics = new HashMap<>();

        List<ServiceDeliveryRecord> completedDeliveries = records.stream()
            .filter(r -> r.getDeliveryConfirmationDate() != null)
            .toList();

        if (!completedDeliveries.isEmpty()) {
            double avgDeliveryTime = completedDeliveries.stream()
                .mapToDouble(r -> java.time.temporal.ChronoUnit.HOURS.between(
                    r.getServiceDate(), r.getDeliveryConfirmationDate()))
                .average()
                .orElse(0.0);

            metrics.put("averageDeliveryTimeHours", avgDeliveryTime);

            // SLA compliance rate
            long slaCompliantCount = completedDeliveries.stream()
                .mapToLong(r -> {
                    long actualHours = java.time.temporal.ChronoUnit.HOURS.between(
                        r.getServiceDate(), r.getDeliveryConfirmationDate());
                    int expectedHours = getExpectedDeliveryTime(r.getServiceType(), r.getDeliveryMethod());
                    return actualHours <= expectedHours ? 1 : 0;
                })
                .sum();

            double slaComplianceRate = (double) slaCompliantCount / completedDeliveries.size() * 100;
            metrics.put("slaComplianceRate", slaComplianceRate);
        }

        // Success rate
        long successfulDeliveries = records.stream()
            .mapToLong(r -> r.isDelivered() ? 1 : 0)
            .sum();
        double successRate = records.isEmpty() ? 0.0 : (double) successfulDeliveries / records.size() * 100;
        metrics.put("successRate", successRate);

        return metrics;
    }

    /**
     * Analyze delivery trends
     */
    private Map<String, Object> analyzeTrends(List<ServiceDeliveryRecord> records) {
        Map<String, Object> trends = new HashMap<>();

        // Daily delivery counts
        Map<String, Long> dailyCounts = records.stream()
            .collect(Collectors.groupingBy(
                r -> r.getServiceDate().toLocalDate().toString(),
                Collectors.counting()));
        trends.put("dailyDeliveryCounts", dailyCounts);

        // Growth rate calculation (simplified)
        if (dailyCounts.size() > 1) {
            List<Long> counts = new ArrayList<>(dailyCounts.values());
            if (counts.size() >= 2) {
                long firstPeriod = counts.get(0);
                long lastPeriod = counts.get(counts.size() - 1);
                double growthRate = firstPeriod == 0 ? 0 :
                    ((double) (lastPeriod - firstPeriod) / firstPeriod) * 100;
                trends.put("growthRate", growthRate);
            }
        }

        return trends;
    }

    /**
     * Real-time delivery monitoring dashboard data
     */
    public Map<String, Object> getRealtimeMonitoringData() {
        log.info("Getting real-time delivery monitoring data");

        Map<String, Object> dashboard = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Current hour statistics
        LocalDateTime hourStart = now.withMinute(0).withSecond(0).withNano(0);
        List<ServiceDeliveryRecord> currentHourDeliveries =
            deliveryRepository.findByServiceDateBetween(hourStart, now);

        dashboard.put("currentHourDeliveries", currentHourDeliveries.size());

        // Today's statistics
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();
        List<ServiceDeliveryRecord> todayDeliveries =
            deliveryRepository.findByServiceDateBetween(dayStart, now);

        dashboard.put("todayDeliveries", todayDeliveries.size());
        dashboard.put("todayAmount", todayDeliveries.stream()
            .filter(r -> r.getServiceAmount() != null)
            .map(ServiceDeliveryRecord::getServiceAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Active deliveries by status
        Map<String, Long> activeStatusCounts = new HashMap<>();
        for (ServiceDeliveryRecord.DeliveryStatus status : ServiceDeliveryRecord.DeliveryStatus.values()) {
            long count = deliveryRepository.countByDeliveryStatus(status);
            activeStatusCounts.put(status.toString(), count);
        }
        dashboard.put("activeStatusCounts", activeStatusCounts);

        // Overdue deliveries
        List<ServiceDeliveryRecord> overdueDeliveries = getOverdueDeliveries();
        dashboard.put("overdueCount", overdueDeliveries.size());

        // Failed deliveries requiring attention
        List<ServiceDeliveryRecord> failedDeliveries = getFailedDeliveries();
        dashboard.put("failedCount", failedDeliveries.size());

        // System health indicators
        Map<String, Object> systemHealth = new HashMap<>();
        systemHealth.put("averageProcessingTime", calculateAverageProcessingTime());
        systemHealth.put("systemLoad", "NORMAL"); // This would be calculated based on actual metrics
        systemHealth.put("lastUpdated", now);
        dashboard.put("systemHealth", systemHealth);

        return dashboard;
    }

    /**
     * Calculate average processing time for recent deliveries
     */
    private double calculateAverageProcessingTime() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<ServiceDeliveryRecord> recentDeliveries = deliveryRepository.findRecentDeliveries(oneDayAgo);

        return recentDeliveries.stream()
            .filter(r -> r.getDeliveryConfirmationDate() != null)
            .mapToDouble(r -> java.time.temporal.ChronoUnit.MINUTES.between(
                r.getServiceDate(), r.getDeliveryConfirmationDate()))
            .average()
            .orElse(0.0);
    }
}
