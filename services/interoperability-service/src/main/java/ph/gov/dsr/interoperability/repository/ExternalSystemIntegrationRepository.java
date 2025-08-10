package ph.gov.dsr.interoperability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.interoperability.entity.ExternalSystemIntegration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ExternalSystemIntegration entity
 * 
 * @author DSR Development Team
 * @version 3.0.0
 * @since 2024-12-23
 */
@Repository
public interface ExternalSystemIntegrationRepository extends JpaRepository<ExternalSystemIntegration, UUID> {

    /**
     * Find system by system code
     */
    Optional<ExternalSystemIntegration> findBySystemCode(String systemCode);

    /**
     * Find systems by organization
     */
    List<ExternalSystemIntegration> findByOrganization(String organization);

    /**
     * Find systems by type
     */
    List<ExternalSystemIntegration> findBySystemType(ExternalSystemIntegration.SystemType systemType);

    /**
     * Find systems by integration type
     */
    List<ExternalSystemIntegration> findByIntegrationType(ExternalSystemIntegration.IntegrationType integrationType);

    /**
     * Find systems by status
     */
    List<ExternalSystemIntegration> findByStatus(ExternalSystemIntegration.SystemStatus status);

    /**
     * Find active systems
     */
    List<ExternalSystemIntegration> findByIsActiveTrue();

    /**
     * Find production systems
     */
    List<ExternalSystemIntegration> findByIsProductionTrue();

    /**
     * Find systems by environment
     */
    List<ExternalSystemIntegration> findByEnvironment(String environment);

    /**
     * Find systems needing health check
     */
    @Query("SELECT s FROM ExternalSystemIntegration s WHERE " +
           "s.isActive = true AND " +
           "(s.lastHealthCheck IS NULL OR s.lastHealthCheck <= :cutoffTime)")
    List<ExternalSystemIntegration> findSystemsNeedingHealthCheck(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find unhealthy systems
     */
    @Query("SELECT s FROM ExternalSystemIntegration s WHERE " +
           "s.status = 'ERROR' OR " +
           "(s.lastHealthCheck IS NOT NULL AND s.lastHealthCheck <= :cutoffTime)")
    List<ExternalSystemIntegration> findUnhealthySystems(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find systems with high failure rate
     */
    @Query("SELECT s FROM ExternalSystemIntegration s WHERE " +
           "s.totalFailedCalls > 0 AND " +
           "(s.totalFailedCalls * 100.0 / (s.totalSuccessfulCalls + s.totalFailedCalls)) > :failureThreshold")
    List<ExternalSystemIntegration> findSystemsWithHighFailureRate(@Param("failureThreshold") double failureThreshold);

    /**
     * Find systems with slow response times
     */
    @Query("SELECT s FROM ExternalSystemIntegration s WHERE s.averageResponseTimeMs > :threshold")
    List<ExternalSystemIntegration> findSlowSystems(@Param("threshold") double threshold);

    /**
     * Count systems by status
     */
    @Query("SELECT s.status, COUNT(s) FROM ExternalSystemIntegration s GROUP BY s.status")
    List<Object[]> countSystemsByStatus();

    /**
     * Count systems by type
     */
    @Query("SELECT s.systemType, COUNT(s) FROM ExternalSystemIntegration s GROUP BY s.systemType")
    List<Object[]> countSystemsByType();

    /**
     * Count systems by organization
     */
    @Query("SELECT s.organization, COUNT(s) FROM ExternalSystemIntegration s GROUP BY s.organization")
    List<Object[]> countSystemsByOrganization();

    /**
     * Get system statistics
     */
    @Query("SELECT COUNT(s), " +
           "COUNT(CASE WHEN s.isActive = true THEN 1 END), " +
           "COUNT(CASE WHEN s.status = 'ACTIVE' THEN 1 END), " +
           "AVG(s.averageResponseTimeMs), " +
           "AVG(CASE WHEN (s.totalSuccessfulCalls + s.totalFailedCalls) > 0 " +
           "     THEN (s.totalSuccessfulCalls * 100.0 / (s.totalSuccessfulCalls + s.totalFailedCalls)) " +
           "     ELSE 0 END) " +
           "FROM ExternalSystemIntegration s")
    Object[] getSystemStatistics();

    /**
     * Check if system code exists
     */
    boolean existsBySystemCode(String systemCode);

    /**
     * Find systems by authentication type
     */
    List<ExternalSystemIntegration> findByAuthenticationType(String authenticationType);

    /**
     * Find systems with rate limits
     */
    @Query("SELECT s FROM ExternalSystemIntegration s WHERE " +
           "s.rateLimitPerMinute IS NOT NULL OR " +
           "s.rateLimitPerHour IS NOT NULL OR " +
           "s.rateLimitPerDay IS NOT NULL")
    List<ExternalSystemIntegration> findSystemsWithRateLimits();

    /**
     * Find systems by contact email
     */
    List<ExternalSystemIntegration> findByContactEmail(String contactEmail);

    /**
     * Search systems by name or description
     */
    @Query("SELECT s FROM ExternalSystemIntegration s WHERE " +
           "LOWER(s.systemName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(s.systemDescription) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(s.organization) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ExternalSystemIntegration> searchSystems(@Param("searchText") String searchText);
}
