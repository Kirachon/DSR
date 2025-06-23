package ph.gov.dsr.interoperability.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.interoperability.entity.ServiceDeliveryRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ServiceDeliveryRecord entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface ServiceDeliveryRecordRepository extends JpaRepository<ServiceDeliveryRecord, UUID> {

    /**
     * Find record by transaction ID
     */
    Optional<ServiceDeliveryRecord> findByTransactionId(String transactionId);

    /**
     * Find records by beneficiary PSN
     */
    List<ServiceDeliveryRecord> findByBeneficiaryPsnOrderByServiceDateDesc(String beneficiaryPsn);

    /**
     * Find records by household ID
     */
    List<ServiceDeliveryRecord> findByHouseholdIdOrderByServiceDateDesc(UUID householdId);

    /**
     * Find records by program code
     */
    List<ServiceDeliveryRecord> findByProgramCodeOrderByServiceDateDesc(String programCode);

    /**
     * Find records by providing agency
     */
    List<ServiceDeliveryRecord> findByProvidingAgencyOrderByServiceDateDesc(String providingAgency);

    /**
     * Find records by delivery status
     */
    List<ServiceDeliveryRecord> findByDeliveryStatus(ServiceDeliveryRecord.DeliveryStatus deliveryStatus);

    /**
     * Find records by service type
     */
    List<ServiceDeliveryRecord> findByServiceTypeOrderByServiceDateDesc(String serviceType);

    /**
     * Find records within date range
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.serviceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.serviceDate DESC")
    List<ServiceDeliveryRecord> findByServiceDateBetween(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find pending deliveries
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.deliveryStatus = 'PENDING' " +
           "ORDER BY s.serviceDate ASC")
    List<ServiceDeliveryRecord> findPendingDeliveries();

    /**
     * Find failed deliveries that can be retried
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.deliveryStatus = 'FAILED' " +
           "AND (s.retryCount IS NULL OR s.retryCount < 3) " +
           "AND (s.nextRetryDate IS NULL OR s.nextRetryDate <= :now) " +
           "ORDER BY s.nextRetryDate ASC")
    List<ServiceDeliveryRecord> findRetryableFailedDeliveries(@Param("now") LocalDateTime now);

    /**
     * Find overdue deliveries (pending for more than 24 hours)
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.deliveryStatus = 'PENDING' " +
           "AND s.serviceDate <= :cutoffDate ORDER BY s.serviceDate ASC")
    List<ServiceDeliveryRecord> findOverdueDeliveries(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find records by external transaction ID
     */
    Optional<ServiceDeliveryRecord> findByExternalTransactionId(String externalTransactionId);

    /**
     * Find records by reference number
     */
    List<ServiceDeliveryRecord> findByReferenceNumber(String referenceNumber);

    /**
     * Find duplicate records
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.isDuplicate = true " +
           "ORDER BY s.createdAt DESC")
    List<ServiceDeliveryRecord> findDuplicateRecords();

    /**
     * Find records needing reconciliation
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.reconciliationStatus = 'PENDING' " +
           "AND s.deliveryStatus IN ('DELIVERED', 'CONFIRMED') " +
           "ORDER BY s.deliveryConfirmationDate ASC")
    List<ServiceDeliveryRecord> findRecordsNeedingReconciliation();

    /**
     * Count records by status
     */
    @Query("SELECT s.deliveryStatus, COUNT(s) FROM ServiceDeliveryRecord s GROUP BY s.deliveryStatus")
    List<Object[]> countRecordsByStatus();

    /**
     * Count records by agency
     */
    @Query("SELECT s.providingAgency, COUNT(s) FROM ServiceDeliveryRecord s GROUP BY s.providingAgency")
    List<Object[]> countRecordsByAgency();

    /**
     * Count records by program
     */
    @Query("SELECT s.programCode, COUNT(s) FROM ServiceDeliveryRecord s GROUP BY s.programCode")
    List<Object[]> countRecordsByProgram();

    /**
     * Get delivery statistics
     */
    @Query("SELECT COUNT(s), " +
           "COUNT(CASE WHEN s.deliveryStatus = 'DELIVERED' OR s.deliveryStatus = 'CONFIRMED' THEN 1 END), " +
           "COUNT(CASE WHEN s.deliveryStatus = 'FAILED' THEN 1 END), " +
           "COUNT(CASE WHEN s.deliveryStatus = 'PENDING' THEN 1 END), " +
           "SUM(s.serviceAmount) " +
           "FROM ServiceDeliveryRecord s")
    Object[] getDeliveryStatistics();

    /**
     * Find records by multiple criteria
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE " +
           "(:beneficiaryPsn IS NULL OR s.beneficiaryPsn = :beneficiaryPsn) AND " +
           "(:programCode IS NULL OR s.programCode = :programCode) AND " +
           "(:providingAgency IS NULL OR s.providingAgency = :providingAgency) AND " +
           "(:deliveryStatus IS NULL OR s.deliveryStatus = :deliveryStatus) AND " +
           "(:serviceType IS NULL OR s.serviceType = :serviceType) AND " +
           "(:startDate IS NULL OR s.serviceDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.serviceDate <= :endDate)")
    Page<ServiceDeliveryRecord> findByCriteria(@Param("beneficiaryPsn") String beneficiaryPsn,
                                              @Param("programCode") String programCode,
                                              @Param("providingAgency") String providingAgency,
                                              @Param("deliveryStatus") ServiceDeliveryRecord.DeliveryStatus deliveryStatus,
                                              @Param("serviceType") String serviceType,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              Pageable pageable);

    /**
     * Find records by amount range
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.serviceAmount BETWEEN :minAmount AND :maxAmount " +
           "ORDER BY s.serviceAmount DESC")
    List<ServiceDeliveryRecord> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
                                                 @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Find high-value transactions
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.serviceAmount >= :threshold " +
           "ORDER BY s.serviceAmount DESC")
    List<ServiceDeliveryRecord> findHighValueTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * Find records by delivery method
     */
    List<ServiceDeliveryRecord> findByDeliveryMethodOrderByServiceDateDesc(String deliveryMethod);

    /**
     * Find records by source system
     */
    List<ServiceDeliveryRecord> findBySourceSystemOrderByServiceDateDesc(String sourceSystem);

    /**
     * Find records by target system
     */
    List<ServiceDeliveryRecord> findByTargetSystemOrderByServiceDateDesc(String targetSystem);

    /**
     * Search records by text
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE " +
           "LOWER(s.transactionId) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(s.beneficiaryPsn) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(s.programCode) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(s.referenceNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<ServiceDeliveryRecord> searchRecords(@Param("searchText") String searchText, Pageable pageable);

    /**
     * Find recent records for a beneficiary
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.beneficiaryPsn = :psn " +
           "AND s.serviceDate >= :since ORDER BY s.serviceDate DESC")
    List<ServiceDeliveryRecord> findRecentRecordsForBeneficiary(@Param("psn") String psn,
                                                               @Param("since") LocalDateTime since);

    /**
     * Find records by integration channel
     */
    List<ServiceDeliveryRecord> findByIntegrationChannelOrderByServiceDateDesc(String integrationChannel);

    /**
     * Get monthly delivery summary
     */
    @Query("SELECT YEAR(s.serviceDate), MONTH(s.serviceDate), COUNT(s), SUM(s.serviceAmount) " +
           "FROM ServiceDeliveryRecord s " +
           "WHERE s.serviceDate >= :startDate " +
           "GROUP BY YEAR(s.serviceDate), MONTH(s.serviceDate) " +
           "ORDER BY YEAR(s.serviceDate), MONTH(s.serviceDate)")
    List<Object[]> getMonthlyDeliverySummary(@Param("startDate") LocalDateTime startDate);

    /**
     * Find records with specific verification method
     */
    List<ServiceDeliveryRecord> findByVerificationMethodOrderByServiceDateDesc(String verificationMethod);

    /**
     * Find unconfirmed deliveries older than specified hours
     */
    @Query("SELECT s FROM ServiceDeliveryRecord s WHERE s.deliveryStatus = 'DELIVERED' " +
           "AND s.deliveryConfirmationDate IS NULL " +
           "AND s.serviceDate <= :cutoffDate ORDER BY s.serviceDate ASC")
    List<ServiceDeliveryRecord> findUnconfirmedDeliveries(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Check if external transaction ID exists
     */
    boolean existsByExternalTransactionId(String externalTransactionId);
}
