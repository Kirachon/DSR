package ph.gov.dsr.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ph.gov.dsr.payment.entity.FSPConfiguration;
import ph.gov.dsr.payment.entity.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for FSPConfiguration entity
 */
@Repository
public interface FSPConfigurationRepository extends JpaRepository<FSPConfiguration, UUID> {

    /**
     * Find FSP configuration by FSP code
     */
    Optional<FSPConfiguration> findByFspCode(String fspCode);

    /**
     * Find active FSP configurations
     */
    List<FSPConfiguration> findByIsActiveTrue();

    /**
     * Find FSP configurations by payment method
     */
    List<FSPConfiguration> findByPaymentMethodAndIsActiveTrue(Payment.PaymentMethod paymentMethod);

    /**
     * Find healthy FSP configurations
     */
    @Query("SELECT f FROM FSPConfiguration f WHERE f.isActive = true AND f.healthStatus = 'HEALTHY'")
    List<FSPConfiguration> findHealthyConfigurations();

    /**
     * Find FSP configurations by payment method and health status
     */
    @Query("SELECT f FROM FSPConfiguration f WHERE f.paymentMethod = :paymentMethod AND f.isActive = true AND f.healthStatus = 'HEALTHY'")
    List<FSPConfiguration> findHealthyConfigurationsByPaymentMethod(@Param("paymentMethod") Payment.PaymentMethod paymentMethod);

    /**
     * Find sandbox FSP configurations
     */
    List<FSPConfiguration> findByIsSandboxTrue();

    /**
     * Find production FSP configurations
     */
    List<FSPConfiguration> findByIsSandboxFalse();

    /**
     * Check if FSP code exists
     */
    boolean existsByFspCode(String fspCode);

    /**
     * Get FSP configurations requiring health check
     */
    @Query("SELECT f FROM FSPConfiguration f WHERE f.isActive = true AND " +
           "(f.lastHealthCheck IS NULL OR f.lastHealthCheck <= :cutoffTime)")
    List<FSPConfiguration> findConfigurationsRequiringHealthCheck(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);
}
